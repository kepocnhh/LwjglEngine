package lwjgl.wrapper.window

import lwjgl.wrapper.canvas.Canvas
import lwjgl.wrapper.entity.Color
import lwjgl.wrapper.entity.ColorEntity
import lwjgl.wrapper.entity.FontRender
import lwjgl.wrapper.entity.Point
import lwjgl.wrapper.entity.Size
import lwjgl.wrapper.entity.fontRender
import lwjgl.wrapper.entity.point
import lwjgl.wrapper.entity.size
import lwjgl.wrapper.util.glfw.glfwCreateWindow
import lwjgl.wrapper.util.glfw.glfwGetMonitorSize
import lwjgl.wrapper.util.glfw.glfwGetWindowSize
import lwjgl.wrapper.util.glfw.glfwSetWindowPos
import lwjgl.wrapper.util.glfw.key.glfwKeyCallback
import lwjgl.wrapper.util.glfw.key.glfwWindowCloseCallback
import lwjgl.wrapper.util.glfw.opengl.*
import lwjgl.wrapper.util.glfw.primitive.toGLFWInt
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.system.MemoryUtil
import java.io.PrintStream

private val windows = mutableMapOf<Long, WindowStatus>()

private enum class WindowStatus {
    CREATED,
    LOOPED,
    CLOSED,
    DESTROYED
}

sealed class WindowSize {
    object FullScreen: WindowSize()
    class Exact(val size: Size): WindowSize()
}

fun createWindow(
    errorPrintStream: PrintStream,
    isVisible: Boolean,
    isResizable: Boolean,
    onKeyCallback: (Long, Int, Int, Int, Int) -> Unit,
    onWindowCloseCallback: (Long) -> Unit,
    windowSize: WindowSize,
    title: String,
    monitorIdSupplier: () -> Long
): Long {
    println("create window | start")
    GLFWErrorCallback.createPrint(errorPrintStream).set()
    if(!GLFW.glfwInit()) error("Unable to initialize GLFW")
    println("create window | init")
    GLFW.glfwDefaultWindowHints()

    GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, isVisible.toGLFWInt())
    GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, isResizable.toGLFWInt())

    val monitorId = monitorIdSupplier()
    if(monitorId == MemoryUtil.NULL) error("Failed to create the GLFW window")
    println("create window | monitor id: $monitorId")
    val monitorSize = glfwGetMonitorSize(monitorId)
    println("create window | monitor id: $monitorId $monitorSize")

    val windowId: Long
    when(windowSize) {
        WindowSize.FullScreen -> {
            GLFW.glfwWindowHint(GLFW.GLFW_DECORATED, false.toGLFWInt())
            windowId = glfwCreateWindow(
                size = monitorSize,
                title = title
            )
        }
        is WindowSize.Exact -> {
            windowId = glfwCreateWindow(windowSize.size, title)

            val xPosition = (monitorSize.width - windowSize.size.width) / 2
            val yPosition = (monitorSize.height- windowSize.size.height)/ 2
            glfwSetWindowPos(
                windowId,
                xPosition = xPosition.toInt(),
                yPosition = yPosition.toInt()
            )
        }
    }
    if(windowId == MemoryUtil.NULL) error("Failed to create the GLFW window")
    println("create window | start: $windowId monitor id: $monitorId")

    GLFW.glfwMakeContextCurrent(windowId)
    GL.createCapabilities()
    val glVendor = GL11.glGetString(GL11.GL_VENDOR)
    val glRenderer = GL11.glGetString(GL11.GL_RENDERER)
    val glVersion = GL11.glGetString(GL11.GL_VERSION)
    val glfwVersion = GLFW.glfwGetVersionString()
//    val isGLFWVulkanSupported = GLFWVulkan.glfwVulkanSupported()// required org.lwjgl:lwjgl-vulkan https://github.com/LWJGL/lwjgl3/issues/502
    println("""
        create window: $windowId | create capabilities
            gl vendor: $glVendor
            gl renderer: $glRenderer
            gl version: $glVersion
            glfw version: $glfwVersion
    """.trimIndent())

    GLFW.glfwSwapInterval(1)
//    GLFW.glfwSwapInterval(0)
    println("create window | show: $windowId")
    GLFW.glfwSetKeyCallback(windowId, glfwKeyCallback(onKeyCallback))
    GLFW.glfwSetWindowCloseCallback(windowId, glfwWindowCloseCallback(onWindowCloseCallback))
    windows[windowId] = WindowStatus.CREATED
    println("create window | finish: $windowId")
    return windowId
}

fun closeWindow(windowId: Long) {
    when(windows[windowId]) {
        WindowStatus.CLOSED -> return
        WindowStatus.CREATED, WindowStatus.LOOPED -> Unit
        else -> error("Window ($windowId) must be created or looped")
    }
    GLFW.glfwSetWindowShouldClose(windowId, true)
    windows[windowId] = WindowStatus.CLOSED
}

fun destroyWindow(windowId: Long) {
    println("destroy window: $windowId")
    when(windows[windowId]) {
        WindowStatus.DESTROYED -> return
        null -> error("Window ($windowId) must be created or looped or closed")
        WindowStatus.CREATED, WindowStatus.LOOPED, WindowStatus.CLOSED -> Unit
    }
    println("destroy window: $windowId | start destroy")
    glfwFreeCallbacks(windowId)
    GLFW.glfwDestroyWindow(windowId)
    GLFW.glfwTerminate()
    GLFW.glfwSetErrorCallback(null)?.free()
    windows[windowId] = WindowStatus.DESTROYED
    println("destroy window: $windowId | finish destroy")
}

private fun onPreRender(windowId: Long) {
    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)
    GLFW.glfwPollEvents()
//    GL11.glDisable(GL11.GL_CULL_FACE)
//    GL11.glDisable(GL11.GL_TEXTURE_2D)
//    GL11.glDisable(GL11.GL_LIGHTING)
//    GL11.glDisable(GL11.GL_DEPTH_TEST)

//    GL11.glEnable(GL11.GL_BLEND)
//    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

    val windowSize = glfwGetWindowSize(windowId)
//    GL11.glViewport(0, 0, windowSize.width, windowSize.height)

    GL11.glMatrixMode(GL11.GL_PROJECTION)
    GL11.glLoadIdentity()
    glOrtho(
        rightFrustumPlane = windowSize.width.toDouble(),
        bottomFrustumPlane = windowSize.height.toDouble()
    )
    GL11.glMatrixMode(GL11.GL_MODELVIEW)
    GL11.glLoadIdentity()
}

private fun onPostRender(windowId: Long) {
    GLFW.glfwSwapBuffers(windowId)
}

fun loopWindow(
    windowId: Long,
    onPreLoop: (Long) -> Unit,
    onPostLoop: () -> Unit,
    onRender: (Long, Canvas) -> Unit
) {
    println("loop window: $windowId")
    when(windows[windowId]) {
        WindowStatus.LOOPED -> error("Window ($windowId) already looped")
        WindowStatus.CREATED -> Unit
        else -> error("Window ($windowId) must be created")
    }

    glClearColor(ColorEntity.BLACK)
    windows[windowId] = WindowStatus.LOOPED

    val canvas = WindowCanvas(fontRender = fontRender())
    onPreLoop(windowId)
    println("loop window: $windowId | start loop")
    while(!GLFW.glfwWindowShouldClose(windowId)) {
        onPreRender(windowId)
        onRender(windowId, canvas)
        onPostRender(windowId)
    }
    println("loop window: $windowId | finish loop")
    onPostLoop()
}

fun loopWindow(
    windowSize: WindowSize,
    title: String,
    onKeyCallback: (Long, Int, Int, Int, Int) -> Unit,
    onWindowCloseCallback: (Long) -> Unit,
    errorPrintStream: PrintStream = System.err,
    isVisible: Boolean = true,
    isResizable: Boolean = false,
    monitorIdSupplier: () -> Long = GLFW::glfwGetPrimaryMonitor,
    onPreLoop: (Long) -> Unit,
    onPostLoop: () -> Unit,
    onRender: (Long, Canvas) -> Unit
) {
    val windowId = createWindow(
        errorPrintStream,
        isVisible,
        isResizable,
        onKeyCallback,
        onWindowCloseCallback = onWindowCloseCallback,
        windowSize = windowSize,
        title = title,
        monitorIdSupplier = monitorIdSupplier
    )
    GLFW.glfwShowWindow(windowId)
    loopWindow(windowId, onPreLoop, onPostLoop, onRender)
    destroyWindow(windowId)
}

private class WindowCanvas(
    private val fontRender: FontRender
): Canvas {
    override fun drawLine(
        color: Color,
        point1: Point,
        point2: Point
    ) {
        GL11.glLineWidth(1.0f)
        glColorOf(color)
        glTransaction(GL11.GL_LINE_STRIP) {
            glVertexOf(point1)
            glVertexOf(point2)
        }
    }

    override fun drawRectangle(
        color: Color,
        pointTopLeft: Point,
        size: Size
    ) {
        val pointBottomRight = point(
            x = pointTopLeft.x + size.width,
            y = pointTopLeft.y + size.height
        )
        glColorOf(color)
        glTransaction(GL11.GL_LINE_LOOP) {
            glVertexOf(pointTopLeft)
            glVertexOf(pointBottomRight.x, pointTopLeft.y)
            glVertexOf(pointBottomRight)
            glVertexOf(pointTopLeft.x, pointBottomRight.y)
        }
    }

    override fun drawText(
        fullPathFont: String,
        fontHeight: Float,
        pointTopLeft: Point,
        color: Color,
        text: CharSequence
    ) {
        fontRender.drawText(fullPathFont, fontHeight, pointTopLeft, color, text)
    }
}
