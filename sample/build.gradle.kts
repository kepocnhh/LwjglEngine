import lwjgl.LwjglUtil
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

plugins {
    application
    kotlin("jvm")
}

application {
    mainClassName = "lwjgl.sample.AppKt"
}

tasks.named<JavaExec>("run") {
    doFirst {
        val currentOperatingSystem = DefaultNativePlatform.getCurrentOperatingSystem()
        when {
            currentOperatingSystem.isMacOsX -> {
                jvmArgs = listOf("-XstartOnFirstThread")
            }
        }
    }
}

dependencies {
    setOf("wrapper", "engine").forEach {
        implementation(project(":$it"))
    }

    implementation(kotlin("stdlib"))

    val nativesName = LwjglUtil.requireNativesName()
    val group = LwjglUtil.group
    implementation(platform("$group:lwjgl-bom:${Version.lwjgl}"))
    LwjglUtil.modules.forEach { name ->
        implementation(group = group, name = name)
        runtimeOnly(group = group, name = name, classifier = nativesName)
    }
}
