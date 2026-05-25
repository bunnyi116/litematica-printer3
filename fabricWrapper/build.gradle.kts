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

        // 依赖所有子项目的 buildAndCollect 任务
        dependsOn(fabricSubprojects.map { it.tasks.named("buildAndCollect") })

        doLast {
            // 每次先清空临时目录，防止旧 JAR 混入
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
            val wrapperIconInBuild = layout.buildDirectory.file("resources/main/assets/$wrapperModId/icon.png").get().asFile
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

                println("✅ fabric.mod.json 已更新，包含 ${jars.size} 个子版本 JAR")
                jars.forEach { println("  - ${it["file"]}") }
            } else {
                println("⚠ 未找到 fabric.mod.json: ${jsonFile.absolutePath}")
            }

            // ====== 复制独立版本 JAR 到 fabricWrapper 输出目录（避免 CI 二次编译）======
            val standaloneSource = rootProject.layout.buildDirectory.dir("libs").get().asFile
            val standaloneTarget = layout.buildDirectory.dir("libs").get().asFile
            if (standaloneSource.exists()) {
                standaloneTarget.mkdirs()
                copy {
                    from(standaloneSource)
                    into(standaloneTarget)
                    include("*.jar")
                    exclude("*-dev.jar", "*-sources.jar", "*-shadow.jar")
                }
                val count = standaloneTarget.listFiles { f -> f.isFile && f.name.endsWith(".jar") }?.size ?: 0
                println("✓ 独立版本 JAR 已复制到 fabricWrapper/build/libs/ (共 $count 个)")
            } else {
                println("⚠ 未找到独立版本源目录: ${standaloneSource.absolutePath}")
            }
        }
    }
}