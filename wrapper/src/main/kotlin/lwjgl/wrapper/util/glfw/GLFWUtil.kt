package lwjgl.wrapper.util.glfw

import lwjgl.wrapper.entity.Size
import lwjgl.wrapper.entity.size
import lwjgl.wrapper.util.io.use
import org.lwjgl.glfw.GLFW
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.system.MemoryUtil

fun glfwGetWindowSize(windowId: Long) = stackPush().use {
    glfwGetWindowSize(windowId, stack = it)
}
fun glfwGetWindowSize(windowId: Long, stack: MemoryStack): Size {
    val widthBuffer = stack.mallocInt(1)
    val heightBuffer = stack.mallocInt(1)
    GLFW.glfwGetWindowSize(windowId, widthBuffer, heightBuffer)
    return size(widthBuffer[0], heightBuffer[0])
}

fun glfwGetMonitorContentScale(monitorId: Long) = stackPush().use {
    glfwGetMonitorContentScale(monitorId, stack = it)
}
fun glfwGetMonitorContentScale(monitorId: Long, stack: MemoryStack): Pair<Float, Float> {
    val px = stack.mallocFloat(1)
    val py = stack.mallocFloat(1)
    GLFW.glfwGetMonitorContentScale(monitorId, px, py)
    return px[0] to py[0]
}

fun glfwCreateWindow(
    width: Int,
    height: Int,
    title: CharSequence,
    monitorPointerId: Long,
    sharePointerId: Long
): Long {
    return GLFW.glfwCreateWindow(
        width,
        height,
        title,
        monitorPointerId,
        sharePointerId
    )
}
fun glfwCreateWindow(
    size: Size,
    title: CharSequence,
    monitorPointerId: Long = MemoryUtil.NULL,
    sharePointerId: Long = MemoryUtil.NULL
): Long {
    return glfwCreateWindow(
        width = size.width.toInt(),
        height = size.height.toInt(),
        title = title,
        monitorPointerId = monitorPointerId,
        sharePointerId = sharePointerId
    )
}

fun glfwSetWindowPos(
    windowId: Long,
    xPosition: Int,
    yPosition: Int
) {
    GLFW.glfwSetWindowPos(
        windowId,
        xPosition,
        yPosition
    )
}
