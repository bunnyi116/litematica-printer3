import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import java.text.SimpleDateFormat
import java.util.*

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

tasks {
    // 打包 fabricWrapper JAR
    named<Jar>("jar") {
        outputs.upToDateWhen { false }

        from(rootProject.file("LICENSE"))
        // 关键修复：从正确的临时目录读取文件打包
        from(layout.buildDirectory.dir("tmp/submods"))
    }

    // 处理资源文件，并动态更新 fabric.mod.json
    named<ProcessResources>("processResources") {
        outputs.upToDateWhen { false }

        // 依赖所有子项目的 buildAndCollect 任务
        dependsOn(fabricSubprojects.map { it.tasks.named("buildAndCollect") })

        // 关键修复：把 copy 操作放到 doLast 里，确保子项目构建完成后再复制
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

            // ====================== 下面是原有逻辑，保持不变 ======================
            // 复制图标文件
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
        }
    }
}