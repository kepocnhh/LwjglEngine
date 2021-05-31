package lwjgl

import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

object LwjglUtil {
    const val group = "org.lwjgl"
    val modules = setOf(
        "lwjgl",
        "lwjgl-glfw",
        "lwjgl-opengl",
        "lwjgl-stb"
    )

    fun requireNativesName(): String {
        val currentOperatingSystem = DefaultNativePlatform.getCurrentOperatingSystem()
        return when {
            currentOperatingSystem.isMacOsX -> "natives-macos"
            currentOperatingSystem.isWindows -> "natives-windows"
            currentOperatingSystem.isLinux -> "natives-linux"
            else -> error("Operating System ${currentOperatingSystem.name} not supported!")
        }
    }
}
