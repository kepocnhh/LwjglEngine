package lwjgl.wrapper.util.glfw.primitive

import org.lwjgl.glfw.GLFW

fun Boolean.toGLFWInt(): Int {
    return if(this) GLFW.GLFW_TRUE else GLFW.GLFW_FALSE
}

fun Int.toGLFWBoolean(): Boolean {
    return when (this) {
        GLFW.GLFW_TRUE -> true
        GLFW.GLFW_FALSE -> false
        else -> error("Integer value must be GLFW_TRUE(${GLFW.GLFW_TRUE}) or GLFW_FALSE(${GLFW.GLFW_FALSE})")
    }
}
