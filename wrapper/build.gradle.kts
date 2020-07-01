import lwjgl.LwjglUtil

plugins {
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib"))

    val group = LwjglUtil.group
    implementation(platform("$group:lwjgl-bom:${Version.lwjgl}"))
    LwjglUtil.modules.forEach { name ->
        implementation(group = group, name = name)
    }
}
