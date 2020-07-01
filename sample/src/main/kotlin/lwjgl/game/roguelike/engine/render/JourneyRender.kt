package lwjgl.game.roguelike.engine.render

import lwjgl.engine.common.EngineProperty
import lwjgl.game.roguelike.state.State
import lwjgl.wrapper.canvas.Canvas
import lwjgl.wrapper.entity.ColorEntity
import lwjgl.wrapper.entity.point
import lwjgl.wrapper.entity.size

class JourneyRender(
    fullPathFont: String,
    pixelsPerUnit: Double
) {
    private val playerSize = size(width = 1 * pixelsPerUnit, height = 1 * pixelsPerUnit)
    private val playerRender = JourneyPlayerRender(
        fullPathFont = fullPathFont,
        playerSize = playerSize // todo
    )
    private val movableRender: MovableRender = MovableRender(size = playerSize)

    fun onRender(
        canvas: Canvas,
        journey: State.Journey,
        engineProperty: EngineProperty
    ) {
        val center = point(x = engineProperty.pictureSize.width / 2, y = engineProperty.pictureSize.height / 2)
        val dX = center.x - journey.player.position.x
        val dY = center.y - journey.player.position.y
        canvas.drawRectangle(
            color = ColorEntity.WHITE,
            pointTopLeft = point(x = dX, y = dY),
            size = journey.territory.size
        )
        journey.territory.regions.forEach { region ->
            canvas.drawLineLoop(
                color = region.color,
                points = region.points.map {
                    point(
                        x = dX + it.x,
                        y = dY + it.y
                    )
                },
                lineWidth = 1f
            )
        }
        movableRender.onRender(
            canvas = canvas,
            dX = dX,
            dY = dY,
            movable = journey.snapshot.dummy,
            color = ColorEntity.BLUE
        )
        playerRender.onRender(
            canvas = canvas,
            player = journey.player,
            engineProperty = engineProperty
        )
    }
}
