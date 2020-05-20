package lwjgl.game.roguelike.engine.logic

import lwjgl.engine.common.EngineLogic
import lwjgl.engine.common.EngineProperty
import lwjgl.engine.common.input.EngineInputCallback
import lwjgl.engine.common.input.EngineInputState
import lwjgl.engine.common.input.FunctionKey
import lwjgl.engine.common.input.PrintableKey
import lwjgl.game.roguelike.state.State
import lwjgl.wrapper.canvas.Canvas
import lwjgl.wrapper.entity.ColorEntity
import lwjgl.wrapper.entity.point
import lwjgl.wrapper.entity.square
import lwjgl.wrapper.util.glfw.key.KeyStatus
import lwjgl.wrapper.util.resource.ResourceProvider
import java.util.concurrent.TimeUnit

object RoguelikeEngineLogic : EngineLogic {
    private val fullPathFont = ResourceProvider.requireResourceAsFile("font.main.ttf").absolutePath
    private val mutableState: MutableState = MutableState(
        common = State.Common.JOURNEY
    )

    override val framesPerSecondExpected: Int = 60
    override val shouldEngineStop: Boolean get() {
        return mutableState.shouldEngineStop
    }
    override val engineInputCallback = object : EngineInputCallback {
        override fun onPrintableKey(key: PrintableKey, status: KeyStatus) {
            // ignored
        }

        override fun onFunctionKey(key: FunctionKey, status: KeyStatus) {
            when (key) {
                FunctionKey.ESCAPE -> {
                    when (status) {
                        KeyStatus.RELEASE -> {
                            mutableState.engineStop()
                        }
                        else -> {
                            // ignored
                        }
                    }
                }
                else -> {
                    // ignored
                }
            }
        }
    }

    override fun onPreLoop() {
        // todo
    }

    override fun onUpdateState(engineInputState: EngineInputState, engineProperty: EngineProperty) {
        val printableKeys = engineInputState.keyboard.printableKeys
        var dX = 0.0
        if (printableKeys[PrintableKey.A] == KeyStatus.PRESS) {
            dX -= 1.0
        }
        if (printableKeys[PrintableKey.D] == KeyStatus.PRESS) {
            dX += 1.0
        }
        mutableState.journey.player.position.x += dX
        // todo
    }

    override fun onRender(canvas: Canvas, engineInputState: EngineInputState, engineProperty: EngineProperty) {
        val fps = TimeUnit.SECONDS.toNanos(1).toDouble() / (engineProperty.timeNow - engineProperty.timeLast)
        canvas.drawText(
            fullPathFont = fullPathFont,
            color = ColorEntity.GREEN,
            pointTopLeft = point(0, 0),
            text = "${(fps * 100).toInt().toDouble() / 100}",
            fontHeight = 16f
        )
        val state: State = mutableState
        canvas.drawRectangle(
            color = ColorEntity.RED,
            pointTopLeft = state.journey.player.position,
            size = square(size = 10)
        )
        // todo
    }
}
