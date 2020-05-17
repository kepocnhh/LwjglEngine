package lwjgl.wrapper.canvas

import lwjgl.wrapper.entity.Color
import lwjgl.wrapper.entity.Point
import lwjgl.wrapper.entity.Size

interface Canvas {
    fun drawLine(color: Color, point1: Point, point2: Point)
    fun drawRectangle(color: Color, pointTopLeft: Point, size: Size)
    fun drawText(
        fullPathFont: String,
        fontHeight: Float,
        pointTopLeft: Point,
        color: Color,
        text: CharSequence
    )
}
