package lwjgl.wrapper.util.glfw.opengl

import lwjgl.wrapper.entity.Color
import lwjgl.wrapper.entity.Point
import lwjgl.wrapper.entity.Size
import org.lwjgl.opengl.GL11
import java.nio.ByteBuffer

fun glTransactionMatrix(action: () -> Unit) {
    GL11.glPushMatrix()
    action()
    GL11.glPopMatrix()
}

fun glTransaction(mode: Int, action: () -> Unit) {
    GL11.glBegin(mode)
    action()
    GL11.glEnd()
}

fun glVertexOf(value1: Int, value2: Int) {
    GL11.glVertex2i(value1, value2)
}
fun glVertexOf(value1: Float, value2: Float) {
    GL11.glVertex2f(value1, value2)
}
fun glVertexOf(value1: Double, value2: Double) {
    GL11.glVertex2d(value1, value2)
}
fun glVertexOf(point: Point) {
    glVertexOf(point.x, point.y)
}

fun glColorOf(red: Float, green: Float, blue: Float, alpha: Float) {
    GL11.glColor4f(red, green, blue, alpha)
}
fun glColorOf(red: Float, green: Float, blue: Float) {
    GL11.glColor3f(red, green, blue)
}
fun glColorOf(color: Color) {
    glColorOf(color.red, color.green, color.blue, color.alpha)
}

fun glClearColor(red: Float, green: Float, blue: Float, alpha: Float) {
    GL11.glClearColor(red, green, blue, alpha)
}
fun glClearColor(color: Color) {
    glClearColor(color.red, color.green, color.blue, color.alpha)
}

fun glTexImage2D(
    textureTarget: Int,
    textureInternalFormat: Int,
    textureSize: Size,
    texelDataFormat: Int,
    texelDataType: Int,
    pixels: ByteBuffer,
    levelOfDetailNumber: Int = 0,
    textureBorderWidth: Int = 0
) {
    GL11.glTexImage2D(
        textureTarget,
        levelOfDetailNumber,
        textureInternalFormat,
        textureSize.width.toInt(),
        textureSize.height.toInt(),
        textureBorderWidth,
        texelDataFormat,
        texelDataType,
        pixels
    )
}

fun glOrtho(
    leftFrustumPlane: Double = 0.0,
    rightFrustumPlane: Double = 0.0,
    bottomFrustumPlane: Double = 0.0,
    topFrustumPlane: Double = 0.0,
    nearFrustumPlane: Double = 0.0,
    farFrustumPlane: Double = 1.0
) {
    GL11.glOrtho(
        leftFrustumPlane,
        rightFrustumPlane,
        bottomFrustumPlane,
        topFrustumPlane,
        nearFrustumPlane,
        farFrustumPlane
    )
}
