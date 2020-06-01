package lwjgl.game.roguelike.engine.render

import lwjgl.engine.common.EngineProperty
import lwjgl.game.roguelike.state.State
import lwjgl.wrapper.canvas.Canvas
import lwjgl.wrapper.entity.ColorEntity
import lwjgl.wrapper.entity.point
import lwjgl.wrapper.entity.size

class JourneyRender(
    fullPathFont: String,
    private val pixelsPerUnit: Double
) {
    private val playerRender = JourneyPlayerRender(
        fullPathFont = fullPathFont,
        playerSize = size(width = 1 * pixelsPerUnit, height = 1 * pixelsPerUnit) // todo
    )
    fun onRender(
        canvas: Canvas,
        journey: State.Journey,
        engineProperty: EngineProperty
    ) {
        val center = point(x = engineProperty.pictureSize.width / 2, y = engineProperty.pictureSize.height / 2)
        canvas.drawRectangle(
            color = ColorEntity.WHITE,
            pointTopLeft = point(
                x = center.x - journey.player.position.x,
                y = center.y - journey.player.position.y
            ),
            size = size(
                width = journey.territory.size.width * pixelsPerUnit,
                height = journey.territory.size.height * pixelsPerUnit
            )
        )
        journey.territory.regions.forEach { region ->
            canvas.drawLineLoop(
                color = region.color,
                points = region.points.map {
                    point(
                        x = center.x + it.x * pixelsPerUnit - journey.player.position.x,
                        y = center.y + it.y * pixelsPerUnit - journey.player.position.y
                    )
                },
                lineWidth = 1f
            )
        }
        playerRender.onRender(
            canvas = canvas,
            player = journey.player,
            engineProperty = engineProperty
        )
    }
}
