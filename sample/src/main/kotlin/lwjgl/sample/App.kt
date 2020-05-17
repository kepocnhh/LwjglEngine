package lwjgl.sample

import lwjgl.engine.common.Engine
import lwjgl.engine.common.EngineLogic
import lwjgl.engine.common.EngineProperty
import lwjgl.engine.common.input.EngineInputCallback
import lwjgl.engine.common.input.EngineInputState
import lwjgl.engine.common.input.FunctionKey
import lwjgl.engine.common.input.PrintableKey
import lwjgl.wrapper.canvas.Canvas
import lwjgl.wrapper.entity.ColorEntity
import lwjgl.wrapper.entity.point
import lwjgl.wrapper.util.glfw.key.KeyStatus
import lwjgl.wrapper.util.resource.ResourceProvider
import org.lwjgl.Version
import java.util.concurrent.TimeUnit

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
        val joystick = engineInputState.joysticks[EngineInputState.JoystickIndex.JOYSTICK_1]
        if(joystick != null) {
            EngineInputState.Joystick.Button.Interaction.values().forEach {
                val isPressed = joystick.button.interaction[it] ?: false
                canvas.drawText(
                    fullPathFont = fullPathFontMain,
                    color = ColorEntity.GREEN,
                    pointTopLeft = point(100 * it.ordinal, 50),
                    text = "$it: " + if (isPressed) "+" else "-",
                    fontHeight = 14f
                )
            }
            EngineInputState.Joystick.Button.Directional.values().forEach {
                val isPressed = joystick.button.directional[it] ?: false
                canvas.drawText(
                    fullPathFont = fullPathFontMain,
                    color = ColorEntity.GREEN,
                    pointTopLeft = point(100 * it.ordinal, 100),
                    text = "$it: " + if (isPressed) "+" else "-",
                    fontHeight = 14f
                )
            }
            setOf(
                "<" to joystick.button.bumperLeft,
                ">" to joystick.button.bumperRight
            ).forEachIndexed { index, (key, isPressed) ->
                canvas.drawText(
                    fullPathFont = fullPathFontMain,
                    color = ColorEntity.GREEN,
                    pointTopLeft = point(x = 100 * index, y = 150),
                    text = key + if (isPressed) "+" else "-",
                    fontHeight = 14f
                )
            }
            EngineInputState.Joystick.Button.Main.values().forEach {
                val isPressed = joystick.button.main[it] ?: false
                canvas.drawText(
                    fullPathFont = fullPathFontMain,
                    color = ColorEntity.GREEN,
                    pointTopLeft = point(100 * it.ordinal, 200),
                    text = "$it: " + if (isPressed) "+" else "-",
                    fontHeight = 14f
                )
            }
            setOf(
                joystick.joyLeft,
                joystick.joyRight
            ).forEachIndexed { index, joy ->
                canvas.drawLine(
                    color = ColorEntity.GREEN,
                    point1 = point(x = 50 + 200 * index, y = 250),
                    point2 = point(x = 50 + 200 * index + joy.x * 50, y = 250 + joy.y * 50)
                )
                canvas.drawText(
                    fullPathFont = fullPathFontMain,
                    color = ColorEntity.GREEN,
                    pointTopLeft = point(x = 50 + 200 * index, y = 250),
                    text = joy.isPressed.toString(),
                    fontHeight = 14f
                )
            }
            setOf(
                joystick.triggerLeft,
                joystick.triggerRight
            ).forEachIndexed { index, trigger ->
                canvas.drawLine(
                    color = ColorEntity.GREEN,
                    point1 = point(x = 50 + 200 * index, y = 300),
                    point2 = point(x = 50 + 200 * index, y = (300 + (trigger.position + 1) * 25).toInt())
                )
                canvas.drawText(
                    fullPathFont = fullPathFontMain,
                    color = ColorEntity.GREEN,
                    pointTopLeft = point(x = 50 + 200 * index, y = 300),
                    text = trigger.position.toString(),
                    fontHeight = 14f
                )
            }
        }
    }
}

fun main() {
    println("Hello LWJGL " + Version.getVersion() + "!")
//    Engine.run(JoystickEngineLogic)
    Engine.run(KeyboardEngineLogic)
}
