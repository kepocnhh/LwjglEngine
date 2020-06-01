package lwjgl.game.roguelike.engine.render

import lwjgl.engine.common.EngineProperty
import lwjgl.game.roguelike.state.State
import lwjgl.wrapper.canvas.Canvas

class Render(
    fullPathFont: String,
    pixelsPerUnit: Double
) {
    private val mainMenuRender = MainMenuRender(
        fullPathFont = fullPathFont,
        pixelsPerUnit = pixelsPerUnit
    )
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
        when (state.common) {
            State.Common.MAIN_MENU -> {
                mainMenuRender.onRender(
                    canvas = canvas,
                    engineProperty = engineProperty,
                    mainMenu = state.mainMenu
                )
            }
            State.Common.JOURNEY -> {
                val journey = state.journey
                checkNotNull(journey)
                journeyRender.onRender(
                    canvas = canvas,
                    engineProperty = engineProperty,
                    journey = journey
                )
            }
        }
        // todo
        systemRender.onRender(
            canvas = canvas,
            engineProperty = engineProperty
        )
    }
}
