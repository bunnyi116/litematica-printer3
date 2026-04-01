@file:Suppress("UnstableApiUsage")

import java.text.SimpleDateFormat
import java.util.*

plugins {
    id("mod-plugin")
    id("maven-publish")
    id("net.fabricmc.fabric-loom")
    id("com.replaymod.preprocess")
}

val time = SimpleDateFormat("yyMMdd")
    .apply { timeZone = TimeZone.getTimeZone("GMT+08:00") }
    .format(Date())
    .toString()

version = fullProjectVersion
group = modMavenGroup

repositories {
    maven("https://maven.fabricmc.net") { name = "FabricMC" }
    maven("https://maven.fallenbreath.me/releases") { name = "FallenBreath" }
    maven("https://api.modrinth.com/maven") { name = "Modrinth" }
    maven("https://www.cursemaven.com") { name = "CurseMaven" }
    maven("https://maven.terraformersmc.com/releases") { name = "TerraformersMC" } // ModMenu 源
    maven("https://maven.nucleoid.xyz") { name = "Nucleoid" }  // ModMenu依赖 Text Placeholder API

    maven("https://maven.shedaniel.me") { name = "Shedaniel" }  // Cloth API/Config 官方源
    maven("https://maven.isxander.dev/releases") { name = "XanderReleases" }
    maven("https://maven.jackf.red/releases") { name = "Jackfred" }   // JackFredLib 依赖
    maven("https://maven.blamejared.com") { name = "BlameJared" }   // Searchables 配置库
    maven("https://maven.kyrptonaught.dev") { name = "Kyrptonaught" }   // KyrptConfig 依赖
    maven("https://jitpack.io") { name = "Jitpack" }
    maven("https://server.bbkr.space/artifactory/libs-release") { name = "CottonMC" }   // LibGui 依赖
    maven("https://staging.alexiil.uk/maven/") { name = "CottonMC" }
    maven("https://mvnrepository.com/artifact/com.belerweb/pinyin4j") { // 拼音库
        name = "Pinyin4j"
        content {
            includeGroupAndSubgroups("com.belerweb")
        }
    }
}

// https://github.com/FabricMC/fabric-loader/issues/783
configurations.all {
    resolutionStrategy {
        force("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    }
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")

    implementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    implementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")
    implementation("com.belerweb:pinyin4j:${prop("pinyin_version")}")?.let { include(it) }
    implementation("com.terraformersmc:modmenu:${prop("modmenu")}")
    implementation("maven.modrinth:malilib:$malilib")
    implementation("maven.modrinth:litematica:$litematica")
    implementation("maven.modrinth:tweakeroo:${prop("tweakeroo")}")

    // 箱子追踪
    implementation("maven.modrinth:chest-tracker-port:${prop("chesttracker")}")
    implementation("com.github.bunnyi116:JackFredLib:${prop("jackfredlib")}")
    implementation("maven.modrinth:where-is-it-port:${prop("whereisit")}")

    implementation("dev.isxander:yet-another-config-lib:${prop("yacl")}")
    implementation("com.blamejared.searchables:${prop("searchables")}")

    // 快捷潜影盒
    implementation("com.github.EnderPhantomWing:quickshulker-multi:${prop("quickshulker")}") {
        exclude(group = "atonkish.reinfcore", module = "reinforced-core")
        exclude(group = "com.github.EnderPhantomWing.quickshulker-multi", module = "quickshulker-mc1.20.6")
        exclude(group = "com.github.EnderPhantomWing.quickshulker-multi", module = "quickshulker-mc1.21.1")
        exclude(group = "com.github.EnderPhantomWing.quickshulker-multi", module = "quickshulker-mc1.21.3")
        exclude(group = "com.github.EnderPhantomWing.quickshulker-multi", module = "quickshulker-mc1.21.4")
        exclude(group = "com.github.EnderPhantomWing.quickshulker-multi", module = "quickshulker-mc1.21.5")
        exclude(group = "com.github.EnderPhantomWing.quickshulker-multi", module = "quickshulker-mc1.21.8")
        exclude(group = "com.github.EnderPhantomWing.quickshulker-multi", module = "quickshulker-mc1.21.10")
        exclude(group = "com.github.EnderPhantomWing.quickshulker-multi", module = "quickshulker-mc1.21.11")
    }
    implementation("me.fallenbreath:conditional-mixin-fabric:0.6.4")
}

loom {
    val commonVmArgs = listOf("-Dmixin.debug.export=true", "-Dmixin.debug.verbose=true", "-Dmixin.env.remapRefMap=true")
    val programArgs = listOf("--width", "1280", "--height", "720", "--username", "PrinterTest")
    runs {
        named("client") {
            ideConfigGenerated(true)
            vmArgs(commonVmArgs)
            programArgs(programArgs)
            runDir = "../../run/client"
        }
    }
}

tasks {
    register<Copy>("buildAndCollect") {
        group = "build"
        from(jar.map { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod_version")}"))
        dependsOn("build")
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = modId
            version = modVersion
        }
    }
    repositories {
        mavenLocal()
        maven {
            url = uri("$rootDir/publish")
        }
    }
}
