package lwjgl

import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

class LwjglUtil {
    companion object {
        val group = "org.lwjgl"

        fun requireNativesName(): String {
            val currentOperatingSystem = DefaultNativePlatform.getCurrentOperatingSystem()
            return when {
                currentOperatingSystem.isMacOsX -> "natives-macos"
                currentOperatingSystem.isWindows -> "natives-windows"
                else -> error("Operating System ${currentOperatingSystem.name} not supported!")
            }
        }
    }

    init {
        error("No instance!")
    }
}
