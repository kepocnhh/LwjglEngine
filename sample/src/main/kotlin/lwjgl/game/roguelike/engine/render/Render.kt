package lwjgl.game.roguelike.engine.render

import lwjgl.engine.common.EngineProperty
import lwjgl.game.roguelike.state.State
import lwjgl.wrapper.canvas.Canvas

class Render(
    fullPathFont: String,
    pixelsPerUnit: Double
) {
    private val journeyRender = JourneyRender(
        fullPathFont = fullPathFont,
        pixelsPerUnit = pixelsPerUnit
    )
    private val systemRender = SystemRender(fullPathFont = fullPathFont)
    fun onRender(
        canvas: Canvas,
        state: State,
        engineProperty: EngineProperty
    ) {
        journeyRender.onRender(
            canvas = canvas,
            engineProperty = engineProperty,
            journey = state.journey
        )
        // todo
        systemRender.onRender(
            canvas = canvas,
            engineProperty = engineProperty
        )
    }
}
