import lwjgl.LwjglUtil

plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":wrapper"))

    implementation(kotlin("stdlib"))

    val group = LwjglUtil.group
    implementation(platform("$group:lwjgl-bom:${Version.lwjgl}"))
    setOf(
        "lwjgl",
        "lwjgl-glfw",
        "lwjgl-opengl",
        "lwjgl-stb"
    ).forEach { name ->
        implementation(group = group, name = name)
    }
}
