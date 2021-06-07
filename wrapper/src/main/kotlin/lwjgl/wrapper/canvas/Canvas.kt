package lwjgl.wrapper.canvas

import lwjgl.wrapper.entity.Color
import lwjgl.wrapper.entity.Point
import lwjgl.wrapper.entity.Size
import lwjgl.wrapper.entity.point

interface Canvas {
    fun drawPoint(color: Color, point: Point)
    fun drawLine(
        color: Color,
        pointStart: Point,
        pointFinish: Point,
        lineWidth: Float
    )
    fun drawLine(
        color: Color,
        pointStart: Point,
        pointFinish: Point,
        lineWidth: Float,
        direction: Double,
        pointOfRotation: Point
    )
    fun drawLineLoop(
        color: Color,
        points: Iterable<Point>,
        lineWidth: Float
    )
    fun drawRectangle(
        color: Color,
        pointTopLeft: Point,
        size: Size,
        lineWidth: Float
    )
    fun drawRectangle(
        color: Color,
        pointTopLeft: Point,
        size: Size,
        lineWidth: Float,
        direction: Double,
        pointOfRotation: Point
    )

    fun drawRectangle(
        colorBorder: Color,
        colorBackground: Color,
        pointTopLeft: Point,
        size: Size,
        lineWidth: Float
    )

    fun drawText(
        fullPathFont: String,
        fontHeight: Float,
        pointTopLeft: Point,
        color: Color,
        text: CharSequence
    )
    fun drawByText(
        fullPathFont: String,
        fontHeight: Float,
        color: Color,
        text: CharSequence,
        onDraw: (width: Double) -> /*pointTopLeft*/ Point
    )
}

fun Canvas.drawCircle(
    color: Color,
    point: Point,
    radius: Double,
    edgeCount: Int,
    lineWidth: Float
) {
    val points = (0..edgeCount).map {
//        double angle = 2 * Math.PI * i / 300;
//        double x = Math.cos(angle);
//        double y = Math.sin(angle);
//        gl.glVertex2d(x,y);
        val radians = ((2 * kotlin.math.PI) / edgeCount) * it
        point(
            x = kotlin.math.cos(radians) * radius + point.x,
            y = kotlin.math.sin(radians) * radius + point.y
        )
    }
    drawLineLoop(
        color = color,
        points = points,
        lineWidth = lineWidth
    )
}
