package lwjgl.sample

import lwjgl.engine.common.Engine
import lwjgl.engine.common.EngineLogic
import lwjgl.engine.common.EngineProperty
import lwjgl.engine.common.input.EngineInputCallback
import lwjgl.engine.common.input.EngineInputState
import lwjgl.engine.common.input.FunctionKey
import lwjgl.engine.common.input.PrintableKey
import lwjgl.game.roguelike.engine.logic.RoguelikeEngineLogic
import lwjgl.wrapper.canvas.Canvas
import lwjgl.wrapper.entity.ColorEntity
import lwjgl.wrapper.entity.point
import lwjgl.wrapper.util.glfw.key.KeyStatus
import lwjgl.wrapper.util.resource.ResourceProvider
import org.lwjgl.Version
import org.lwjgl.glfw.GLFW
import java.util.concurrent.TimeUnit
import kotlin.math.cos
import kotlin.math.sin

object KeyboardEngineLogic : EngineLogic {
    private val fullPathFontMain = ResourceProvider.requireResourceAsFile("font.main.ttf").absolutePath
    override val framesPerSecondExpected: Int = 60
    private lateinit var shouldEngineStopUnit: Unit
    override val shouldEngineStop: Boolean get() = ::shouldEngineStopUnit.isInitialized
    override val engineInputCallback = object : EngineInputCallback {
        override fun onPrintableKey(key: PrintableKey, status: KeyStatus) {
            // ignored
        }
        override fun onFunctionKey(key: FunctionKey, status: KeyStatus) {
            when(key) {
                FunctionKey.ESCAPE -> {
                    when(status) {
                        KeyStatus.RELEASE -> {
                            shouldEngineStopUnit = Unit
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
        // ignored
    }
    override fun onUpdateState(engineInputState: EngineInputState, engineProperty: EngineProperty) {
        // ignored
    }
    override fun onRender(
        canvas: Canvas,
        engineInputState: EngineInputState,
        engineProperty: EngineProperty
    ) {
        val fps = TimeUnit.SECONDS.toNanos(1).toDouble() / (engineProperty.timeNow - engineProperty.timeLast)
        canvas.drawText(
            fullPathFont = fullPathFontMain,
            color = ColorEntity.GREEN,
            pointTopLeft = point(0, 0),
            text = "${(fps*100).toInt().toDouble()/100}",
            fontHeight = 16f
        )
        setOf(
            setOf(
                PrintableKey.Q,
                PrintableKey.W,
                PrintableKey.E,
                PrintableKey.R,
                PrintableKey.T,
                PrintableKey.Y,
                PrintableKey.U,
                PrintableKey.I,
                PrintableKey.O,
                PrintableKey.P
            ),
            setOf(
                PrintableKey.A, PrintableKey.S, PrintableKey.D, PrintableKey.F, PrintableKey.G,
                PrintableKey.H,
                PrintableKey.J,
                PrintableKey.K,
                PrintableKey.L
            ),
            setOf(
                PrintableKey.Z, PrintableKey.X, PrintableKey.C, PrintableKey.V, PrintableKey.B,
                PrintableKey.N,
                PrintableKey.M
            )
        ).forEachIndexed { y, keys ->
            keys.forEachIndexed { x, key ->
                val status = engineInputState.keyboard.printableKeys[key]
                if (status != null) {
                    val isPressed = if(status == KeyStatus.PRESS) "+" else "-"
                    val text = key.name + isPressed
                    canvas.drawText(
                        fullPathFont = fullPathFontMain,
                        color = ColorEntity.RED,
                        pointTopLeft = point(25 + 25 * x, 25 + 25 * y),
                        text = text,
                        fontHeight = 16f
                    )
                }
            }
        }
    }
}
object JoystickEngineLogic : EngineLogic {
    private val fullPathFontMain = ResourceProvider.requireResourceAsFile("font.main.ttf").absolutePath
    override val framesPerSecondExpected: Int = 60
    private lateinit var shouldEngineStopUnit: Unit
    override val shouldEngineStop: Boolean get() = ::shouldEngineStopUnit.isInitialized
    override val engineInputCallback = object : EngineInputCallback {
        override fun onPrintableKey(key: PrintableKey, status: KeyStatus) {
            // ignored
        }
        override fun onFunctionKey(key: FunctionKey, status: KeyStatus) {
            when(key) {
                FunctionKey.ESCAPE -> {
                    when(status) {
                        KeyStatus.RELEASE -> {
                            shouldEngineStopUnit = Unit
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
        // ignored
    }
    override fun onUpdateState(engineInputState: EngineInputState, engineProperty: EngineProperty) {
        // ignored
    }
    override fun onRender(
        canvas: Canvas,
        engineInputState: EngineInputState,
        engineProperty: EngineProperty
    ) {
        val fps = TimeUnit.SECONDS.toNanos(1).toDouble() / (engineProperty.timeNow - engineProperty.timeLast)
        canvas.drawText(
            fullPathFont = fullPathFontMain,
            color = ColorEntity.GREEN,
            pointTopLeft = point(0, 0),
            text = "${(fps*100).toInt().toDouble()/100}",
            fontHeight = 16f
        )
        val joystick = engineInputState.joysticks[GLFW.GLFW_JOYSTICK_1]
        if(joystick != null) {
            EngineInputState.Joystick.Mapping.Side.values().forEach { side ->
                val pad = joystick.pads[side] ?: TODO()
                EngineInputState.Joystick.Pad.Button.values().forEach { button ->
                    val x: Int
                    val y: Int
                    when (button) {
                        EngineInputState.Joystick.Pad.Button.UP -> {
                            x = 50
                            y = 25
                        }
                        EngineInputState.Joystick.Pad.Button.RIGHT -> {
                            x = 75
                            y = 50
                        }
                        EngineInputState.Joystick.Pad.Button.DOWN -> {
                            x = 50
                            y = 75
                        }
                        EngineInputState.Joystick.Pad.Button.LEFT -> {
                            x = 25
                            y = 50
                        }
                        EngineInputState.Joystick.Pad.Button.MAIN -> {
                            x = 100
                            y = 50
                        }
                        EngineInputState.Joystick.Pad.Button.BUMPER -> {
                            x = 50
                            y = 100
                        }
                        EngineInputState.Joystick.Pad.Button.JOY -> {
                            x = 50
                            y = 225
                        }
                    }
                    val isPressed = pad.isPressed(button)
                    canvas.drawText(
                        fullPathFont = fullPathFontMain,
                        color = if (isPressed) ColorEntity.YELLOW else ColorEntity.GREEN,
                        pointTopLeft = point(x + 125 * side.ordinal, y),
                        text = if (isPressed) "+" else "-",
                        fontHeight = 14f
                    )
                }
                canvas.drawLine(
                    color = ColorEntity.GREEN,
                    pointStart = point(x = 50 + 125 * side.ordinal, y = 125),
                    pointFinish = point(x = 50 + 125 * side.ordinal, y = (125 + (pad.triggerPosition + 1) * 25).toInt()),
                    lineWidth = 1f
                )
                canvas.drawText(
                    fullPathFont = fullPathFontMain,
                    color = ColorEntity.GREEN,
                    pointTopLeft = point(x = 50 + 125 * side.ordinal + 25, y = 125),
                    text = String.format("%.2f", pad.triggerPosition),
                    fontHeight = 14f
                )
                val joyY = 225
                val joyX = 50
                val joyWidth = 100
                val parts = 8
                val ps = (-parts..parts).map {
                    val radians = kotlin.math.PI - (kotlin.math.PI / parts) * it
                    val x = cos(radians) * 0.5
                    val y = sin(radians) * 0.5
                    point(x = x * joyWidth + joyX + 125 * side.ordinal, y = y * joyWidth + joyY)
                }
                canvas.drawLineLoop(
                    color = if (pad.isPressed(EngineInputState.Joystick.Pad.Button.JOY)) ColorEntity.YELLOW else ColorEntity.GREEN,
                    points = ps,
                    lineWidth = 1f
                )
                canvas.drawLine(
                    color = ColorEntity.GREEN,
                    pointStart = point(x = 50 + 125 * side.ordinal, y = 225),
                    pointFinish = point(x = 50 + 125 * side.ordinal + pad.joy.x * 50, y = 225 + pad.joy.y * 50),
                    lineWidth = 1f
                )
            }
        }
    }
}

fun main() {
    println("Hello LWJGL " + Version.getVersion() + "!")
    Engine.run(JoystickEngineLogic)
//    Engine.run(KeyboardEngineLogic)
//    Engine.run(RoguelikeEngineLogic)
}
