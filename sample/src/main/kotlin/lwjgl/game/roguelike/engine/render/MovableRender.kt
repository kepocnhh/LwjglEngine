package lwjgl.game.roguelike.engine.render

import lwjgl.game.roguelike.engine.entity.Movable
import lwjgl.wrapper.canvas.Canvas
import lwjgl.wrapper.entity.*

class MovableRender(
    private val size: Size
) {
    fun onRender(
        canvas: Canvas,
        dX: Double,
        dY: Double,
        movable: Movable,
        color: Color
    ) {
        val position = point(
            x = dX + movable.position.x,
            y = dY + movable.position.y
        )
        val relativePosition = point(
            x = position.x - size.width / 2,
            y = position.y - size.height /2
        )
        canvas.drawRectangle(
            color = color,
            pointTopLeft = relativePosition,
            size = size,
            direction = movable.directionActual,
            pointOfRotation = position,
            lineWidth = 1f
        )
        canvas.drawLine(
            color = color,
            pointStart = position,
            pointFinish = point(
                x = position.x,
                y = relativePosition.y
            ),
            lineWidth = 1f,
            direction = movable.directionActual,
            pointOfRotation = position
        )
        canvas.drawPoint(
            color = ColorEntity.WHITE,
            point = relativePosition
        )
        canvas.drawPoint(
            color = ColorEntity.GREEN,
            point = position
        )
    }
}
