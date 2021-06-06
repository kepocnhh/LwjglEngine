package lwjgl.game.roguelike.engine.render

import lwjgl.engine.common.EngineProperty
import lwjgl.game.roguelike.state.State
import lwjgl.wrapper.canvas.Canvas
import lwjgl.wrapper.canvas.drawCircle
import lwjgl.wrapper.entity.ColorEntity
import lwjgl.wrapper.entity.Size
import lwjgl.wrapper.entity.color
import lwjgl.wrapper.entity.point
import lwjgl.wrapper.entity.update

class JourneyPlayerRender(
    private val fullPathFont: String,
    private val playerSize: Size,
    private val pixelsPerUnit: Double
) {
    fun onRender(
        canvas: Canvas,
        player: State.Journey.Player,
        engineProperty: EngineProperty
    ) {
        val center = point(x = engineProperty.pictureSize.width / 2, y = engineProperty.pictureSize.height / 2)
        val playerPosition = point(
            x = center.x - playerSize.width / 2,
            y = center.y - playerSize.height /2
        )
        canvas.drawRectangle(
            color = ColorEntity.RED,
            pointTopLeft = playerPosition,
            size = playerSize,
            direction = player.directionActual,
            pointOfRotation = center,
            lineWidth = 1f
        )
        canvas.drawLine(
            color = ColorEntity.RED,
            pointStart = center,
            pointFinish = point(
                x = center.x,
                y = playerPosition.y
            ),
            lineWidth = 1f,
            direction = player.directionActual,
            pointOfRotation = center
        )
        canvas.drawPoint(
            color = ColorEntity.WHITE,
            point = playerPosition
        )
        canvas.drawPoint(
            color = ColorEntity.GREEN,
            point = center
        )
        canvas.drawText(
            fullPathFont = fullPathFont,
            color = ColorEntity.GREEN,
            pointTopLeft = point(0,0),
            text = player.position.toString(),
            fontHeight = 16f
        )
        canvas.drawText(
            fullPathFont = fullPathFont,
            color = ColorEntity.GREEN,
            pointTopLeft = point(0,25),
            text = String.format("%.1f", player.directionActual),
            fontHeight = 16f
        )
        when {
            player.indicator.interaction -> {
                val color = ColorEntity.GREEN
                val point = point(x = center.x, y = center.y + playerSize.height * 1.5)
                canvas.drawCircle(
                    color = color,
                    point = point,
                    radius = 0.35 * pixelsPerUnit,
                    edgeCount = 10,
                    lineWidth = 1f
                )
                val fontHeight = 12f
                canvas.drawByText(
                    fullPathFont = fullPathFont,
                    fontHeight = fontHeight,
                    color = color,
                    text = "F"
                ) { width ->
                    point.update(dX = - width / 2, dY = - fontHeight / 2.0)
                }
            }
        }
    }
}
