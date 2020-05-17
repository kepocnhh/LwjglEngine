package lwjgl.engine.common

import lwjgl.engine.common.input.EngineInputCallback
import lwjgl.engine.common.input.EngineInputState
import lwjgl.wrapper.canvas.Canvas

interface EngineLogic {
    val framesPerSecondExpected: Int
    val shouldEngineStop: Boolean
    val engineInputCallback: EngineInputCallback

    fun onPreLoop()
    fun onUpdateState(
        engineInputState: EngineInputState,
        engineProperty: EngineProperty
    )
    fun onRender(
        canvas: Canvas,
        engineInputState: EngineInputState,
        engineProperty: EngineProperty
    )
}
