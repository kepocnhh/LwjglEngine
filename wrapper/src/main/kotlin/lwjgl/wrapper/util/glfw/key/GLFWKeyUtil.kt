package lwjgl.wrapper.util.glfw.key

import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWKeyCallback
import org.lwjgl.glfw.GLFWWindowCloseCallbackI

fun glfwKeyCallback(
    onKeyCallback: (Long, Int, Int, Int, Int) -> Unit
) = object: GLFWKeyCallback() {
    override fun invoke(window: Long, key: Int, scancode: Int, action: Int, mods: Int) {
        onKeyCallback(window, key, scancode, action, mods)
    }
}

fun glfwWindowCloseCallback(
    onWindowCloseCallback: (Long) -> Unit
): GLFWWindowCloseCallbackI {
    return object: GLFWWindowCloseCallbackI {
        override fun invoke(window: Long) {
            onWindowCloseCallback(window)
        }
    }
}

enum class KeyStatus {
    RELEASE,
    PRESS,
    @Deprecated(message = "doubtful")
    REPEAT
}

fun Int.toKeyStatusOrNull(): KeyStatus? {
    return when(this) {
        GLFW.GLFW_RELEASE -> KeyStatus.RELEASE
        GLFW.GLFW_PRESS -> KeyStatus.PRESS
        GLFW.GLFW_REPEAT -> KeyStatus.REPEAT
        else -> null
    }
}
