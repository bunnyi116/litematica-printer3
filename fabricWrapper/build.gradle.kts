import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import java.text.SimpleDateFormat
import java.util.*
import java.util.jar.JarEntry
import java.util.jar.JarInputStream
import java.util.jar.JarOutputStream

plugins {
    id("java-library")
    id("maven-publish")
    id("mod-plugin")
}

repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
    maven("https://maven.fabricmc.net") { name = "Fabric" }
}

val jsonSlurper = JsonSlurper()

val time = SimpleDateFormat("yyMMdd")
    .apply { timeZone = TimeZone.getTimeZone("GMT+08:00") }
    .format(Date())
    .toString()

var fullProjectVersion = "$modVersion+$time"
if (System.getenv("IS_THIS_RELEASE") == "false") {
    val buildNumber: String? = System.getenv("GITHUB_RUN_NUMBER")
    if (buildNumber != null) {
        fullProjectVersion += "+build.$buildNumber"
    }
}

group = modMavenGroup
version = fullProjectVersion

base {
    archivesName.set("$modArchivesBaseName-versionpack")
}

dependencies {
    implementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
}

// 获取所有子项目（排除包装器本身）
val fabricSubprojects = rootProject.subprojects.filter { it.name != "fabricWrapper" }

// 确保先评估所有子项目
fabricSubprojects.forEach {
    evaluationDependsOn(":${it.name}")
}

// ====== 工具函数：从 JAR 中剥离指定前缀的资源条目 ======
fun stripJarResources(jarFile: File, prefixes: List<String>) {
    val tempFile = File.createTempFile("stripped-", ".jar")
    var strippedCount = 0
    var keptCount = 0

    JarInputStream(jarFile.inputStream().buffered()).use { jis ->
        JarOutputStream(tempFile.outputStream().buffered()).use { jos ->
            var entry = jis.nextJarEntry
            while (entry != null) {
                val name = entry.name
                val shouldStrip = prefixes.any { prefix ->
                    name == prefix || name.startsWith(prefix)
                }
                if (!shouldStrip) {
                    jos.putNextEntry(JarEntry(name))
                    jis.copyTo(jos)
                    jos.closeEntry()
                    keptCount++
                } else {
                    strippedCount++
                }
                entry = jis.nextJarEntry
            }
        }
    }

    if (strippedCount > 0) {
        tempFile.copyTo(jarFile, overwrite = true)
        println("  📦 ${jarFile.name}: 剥离 $strippedCount 个资源条目, 保留 $keptCount 个")
    }
    tempFile.delete()
}

// ====== 工具函数：从子JAR中收集嵌套库名列表 ======
fun collectNestedLibs(jarFile: File): List<String> {
    val libs = mutableListOf<String>()
    JarInputStream(jarFile.inputStream().buffered()).use { jis ->
        var entry = jis.nextJarEntry
        while (entry != null) {
            val name = entry.name
            if (name.startsWith("META-INF/jars/") && name != "META-INF/jars/") {
                libs.add(name.substring("META-INF/jars/".length))
            }
            entry = jis.nextJarEntry
        }
    }
    return libs
}

// ====== 工具函数：从子JAR中提取指定嵌套库到目标文件 ======
fun extractNestedLib(jarFile: File, libName: String, targetFile: File) {
    JarInputStream(jarFile.inputStream().buffered()).use { jis ->
        var entry = jis.nextJarEntry
        while (entry != null) {
            if (entry.name == "META-INF/jars/$libName") {
                targetFile.outputStream().buffered().use { out ->
                    jis.copyTo(out)
                }
                break
            }
            entry = jis.nextJarEntry
        }
    }
}

// ====== 工具函数：从子JAR中删除指定嵌套库并更新其fabric.mod.json ======
fun removeNestedLibsFromJar(jarFile: File, libNamesToRemove: Set<String>) {
    val tempFile = File.createTempFile("dedup-", ".jar")
    var removedCount = 0

    JarInputStream(jarFile.inputStream().buffered()).use { jis ->
        JarOutputStream(tempFile.outputStream().buffered()).use { jos ->
            var entry = jis.nextJarEntry
            while (entry != null) {
                val name = entry.name
                val isNestedLib = name.startsWith("META-INF/jars/")
                        && libNamesToRemove.contains(name.substring("META-INF/jars/".length))

                if (isNestedLib) {
                    removedCount++
                } else if (name == "fabric.mod.json") {
                    // 读取并修改 fabric.mod.json, 移除对应的jars条目
                    val rawJson = jis.readBytes()

                    @Suppress("UNCHECKED_CAST")
                    val json = jsonSlurper.parse(rawJson.inputStream()) as MutableMap<String, Any>

                    @Suppress("UNCHECKED_CAST")
                    val jars = (json["jars"] as? List<Map<String, Any>>)?.filter { jarEntry ->
                        val file = jarEntry["file"] as? String ?: ""
                        !libNamesToRemove.any { lib -> file.contains(lib) }
                    } ?: emptyList()
                    if (jars.isEmpty()) {
                        json.remove("jars")
                    } else {
                        json["jars"] = jars
                    }
                    val updatedJson = JsonBuilder(json).toPrettyString().toByteArray()
                    jos.putNextEntry(JarEntry("fabric.mod.json"))
                    jos.write(updatedJson)
                    jos.closeEntry()
                } else {
                    jos.putNextEntry(JarEntry(name))
                    jis.copyTo(jos)
                    jos.closeEntry()
                }
                entry = jis.nextJarEntry
            }
        }
    }

    if (removedCount > 0) {
        tempFile.copyTo(jarFile, overwrite = true)
    }
    tempFile.delete()
}

// 要剥离的共享资源前缀列表 (相对于 JAR 根目录)
val sharedResourcePrefixes = listOf(
    "assets/litematica-printer/icon.png",
    "assets/litematica-printer/lang/"
)

tasks {
    // 打包 fabricWrapper JAR
    named<Jar>("jar") {
        outputs.upToDateWhen { false }

        from(rootProject.file("LICENSE"))
        from(layout.buildDirectory.dir("tmp/submods"))
    }

    // 处理资源文件，并动态更新 fabric.mod.json
    named<ProcessResources>("processResources") {
        outputs.upToDateWhen { false }

        delete(rootProject.layout.buildDirectory)
        delete(project.layout.buildDirectory)
        dependsOn(fabricSubprojects.map { it.tasks.named("buildAndCollect") })

        doLast {
            val targetDir = layout.buildDirectory.dir("tmp/submods/META-INF/jars").get().asFile
            println("📁 目标JAR目录: ${targetDir.absolutePath}")

            if (targetDir.exists()) {
                targetDir.deleteRecursively()
            }
            targetDir.mkdirs()

            // 复制所有子模块JAR
            copy {
                from(fabricSubprojects.map { it.tasks.named("buildAndCollect").get().outputs.files })
                into(targetDir)
                include("*.jar")
                exclude("*-dev.jar", "*-sources.jar", "*-shadow.jar")
                eachFile { println("📦 复制JAR: ${this.name}") }
            }

            // ====== 剥离共享资源 ======
            println("🗜️ 开始剥离子模组 JAR 中的共享资源...")
            targetDir.listFiles { f ->
                f.isFile && f.name.endsWith(".jar")
                        && !f.name.endsWith("-dev.jar")
                        && !f.name.endsWith("-sources.jar")
                        && !f.name.endsWith("-shadow.jar")
            }?.forEach { jarFile ->
                stripJarResources(jarFile, sharedResourcePrefixes)
            }
            println("✅ 共享资源剥离完成")

            // ====== 去重：提取多个子JAR中共同存在的嵌套Java库 ======
            println("🔍 扫描子模块 JAR 中的嵌套 Java 库...")
            val validJars = targetDir.listFiles { f ->
                f.isFile && f.name.endsWith(".jar")
                        && !f.name.endsWith("-dev.jar")
                        && !f.name.endsWith("-sources.jar")
                        && !f.name.endsWith("-shadow.jar")
            } ?: emptyArray()

            val libMap = mutableMapOf<String, MutableList<String>>()
            validJars.forEach { jarFile ->
                collectNestedLibs(jarFile).forEach { libName ->
                    libMap.getOrPut(libName) { mutableListOf() }.add(jarFile.name)
                }
            }

            // 在所有子JAR中都出现的库视为公共Java库，提取到包装器层级
            val totalJars = validJars.size
            val commonLibs = libMap.filter { it.value.size >= totalJars }

            if (commonLibs.isNotEmpty()) {
                println("  发现 ${commonLibs.size} 个公共Java库: ${commonLibs.keys.joinToString(", ")}")

                // 提取公共库到targetDir（与子JAR同级）
                commonLibs.forEach { (libName, _) ->
                    extractNestedLib(validJars.first(), libName, File(targetDir, libName))
                    println("  📤 提取公共库: $libName")
                }

                // 从所有子JAR中删除公共库，并更新各自的fabric.mod.json
                val libNamesToRemove = commonLibs.keys.toSet()
                validJars.forEach { jarFile ->
                    removeNestedLibsFromJar(jarFile, libNamesToRemove)
                }
                println("  ✅ 已从 ${validJars.size} 个子JAR中移除公共库")
            } else {
                println("  未发现需要去重的公共库")
            }

            // ====== 将共享资源复制到 fabricWrapper 构建资源中 ======
            val modId = rootProject.property("mod_id") as String
            val sharedAssetsSource = rootProject.file("src/main/resources/assets/$modId")
            val wrapperAssetsTarget = layout.buildDirectory
                .dir("resources/main/assets/$modId").get().asFile

            if (sharedAssetsSource.exists()) {
                wrapperAssetsTarget.mkdirs()
                copy {
                    from(sharedAssetsSource)
                    into(wrapperAssetsTarget)
                }
                println("✓ 共享资源已复制到 fabricWrapper: assets/$modId")
            } else {
                println("⚠ 未找到共享资源源目录: ${sharedAssetsSource.absolutePath}")
            }

            // 复制图标文件 (wrapper 自己的 icon)
            val rootIcon = rootProject.file("src/main/resources/assets/$modId/icon.png")
            val wrapperIconInResources =
                layout.projectDirectory.file("src/main/resources/assets/$wrapperModId/icon.png").asFile
            val wrapperIconInBuild =
                layout.buildDirectory.file("resources/main/assets/$wrapperModId/icon.png").get().asFile
            if (!wrapperIconInResources.exists()) {
                if (rootIcon.exists()) {
                    wrapperIconInBuild.parentFile.mkdirs()
                    rootIcon.copyTo(wrapperIconInBuild, overwrite = true)
                    println("✓ 图标已从根项目复制: ${rootIcon.name}")
                } else {
                    println("⚠ 未找到图标文件，跳过复制")
                }
            }

            // 读取并更新fabric.mod.json
            // jars列表现在同时包含子模组JAR和提取出来的公共Java库
            val jars = if (targetDir.exists() && targetDir.isDirectory) {
                targetDir.listFiles { f ->
                    f.isFile && f.name.endsWith(".jar")
                            && !f.name.endsWith("-dev.jar")
                            && !f.name.endsWith("-sources.jar")
                            && !f.name.endsWith("-shadow.jar")
                }?.map { mapOf("file" to "META-INF/jars/${it.name}") } ?: emptyList()
            } else {
                emptyList()
            }

            val minecraftVersions = fabricSubprojects.mapNotNull { sub ->
                (sub.findProperty("minecraft_dependency") as? String)?.takeIf { it.isNotBlank() }
                    .also { if (it != null) println("✓ 收集 Minecraft 版本: $it") }
            }

            val jsonFile = layout.buildDirectory.file("resources/main/fabric.mod.json").get().asFile
            if (jsonFile.exists()) {
                @Suppress("UNCHECKED_CAST")
                val json = jsonSlurper.parse(jsonFile) as MutableMap<String, Any>

                json["jars"] = jars

                @Suppress("UNCHECKED_CAST")
                (json["depends"] as? MutableMap<String, Any>)?.put("minecraft", minecraftVersions)

                jsonFile.bufferedWriter().use { it.write(JsonBuilder(json).toPrettyString()) }

                println("✅ fabric.mod.json 已更新，包含 ${jars.size} 个条目（含子版本JAR和公共Java库）")
                jars.forEach { println("  - ${it["file"]}") }
            } else {
                println("⚠ 未找到 fabric.mod.json: ${jsonFile.absolutePath}")
            }

            // ====== 复制独立版本 JAR 到 jars 子目录（方便 CI 直接打包文件夹）======
            val standaloneSource = rootProject.layout.buildDirectory.dir("libs").get().asFile
            val standaloneTarget = layout.buildDirectory.dir("libs/jars").get().asFile
            if (standaloneSource.exists()) {
                standaloneTarget.mkdirs()
                copy {
                    from(standaloneSource)
                    into(standaloneTarget)
                    include("*.jar")
                    exclude("*-dev.jar", "*-sources.jar", "*-shadow.jar")
                }
                val count = standaloneTarget.listFiles { f -> f.isFile && f.name.endsWith(".jar") }?.size ?: 0
                println("✓ 独立版本 JAR 已复制到 fabricWrapper/build/libs/jars/ (共 $count 个)")
            } else {
                println("⚠ 未找到独立版本源目录: ${standaloneSource.absolutePath}")
            }
        }
    }
}
