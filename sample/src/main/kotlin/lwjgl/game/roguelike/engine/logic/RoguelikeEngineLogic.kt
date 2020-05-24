package lwjgl.game.roguelike.engine.logic

import lwjgl.engine.common.EngineLogic
import lwjgl.engine.common.EngineProperty
import lwjgl.engine.common.input.EngineInputCallback
import lwjgl.engine.common.input.EngineInputState
import lwjgl.engine.common.input.FunctionKey
import lwjgl.engine.common.input.PrintableKey
import lwjgl.game.roguelike.state.State
import lwjgl.game.roguelike.util.TimeUnit
import lwjgl.wrapper.canvas.Canvas
import lwjgl.wrapper.entity.ColorEntity
import lwjgl.wrapper.entity.point
import lwjgl.wrapper.entity.size
import lwjgl.wrapper.entity.square
import lwjgl.wrapper.util.glfw.key.KeyStatus
import lwjgl.wrapper.util.resource.ResourceProvider
import org.lwjgl.glfw.GLFW
import kotlin.math.absoluteValue
import kotlin.math.sqrt

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

    private const val pixelsPerUnit = 10.0 // todo

    override fun onPreLoop() {
        // todo
    }

    override fun onUpdateState(engineInputState: EngineInputState, engineProperty: EngineProperty) {
        val time = TimeUnit.NANOSECONDS.convert(
            time = engineProperty.timeNow - engineProperty.timeLast,
            timeUnit = TimeUnit.SECONDS
        )
        val joystick = engineInputState.joysticks[GLFW.GLFW_JOYSTICK_1] ?: engineInputState.joysticks[GLFW.GLFW_JOYSTICK_2]
        val playerPositionOld = mutableState.journey.player.position.copy()
        if (joystick == null) {
            val printableKeys = engineInputState.keyboard.printableKeys
            var dX = 0.0
            if (printableKeys[PrintableKey.A] == KeyStatus.PRESS) {
                dX -= 1.0
            }
            if (printableKeys[PrintableKey.D] == KeyStatus.PRESS) {
                dX += 1.0
            }
            var dY = 0.0
            if (printableKeys[PrintableKey.W] == KeyStatus.PRESS) {
                dY -= 1.0
            }
            if (printableKeys[PrintableKey.S] == KeyStatus.PRESS) {
                dY += 1.0
            }
            if (dX == 0.0 && dY == 0.0) {
                // todo
            } else {
                val velocityMultiple = 1.0
                val units = mutableState.journey.player.velocity * velocityMultiple * time
                val dP = units * pixelsPerUnit
                val r = if (dX != 0.0 && dY != 0.0) {
                    dP / sqrt(2.0)
                } else {
                    dP
                }
                if (dX != 0.0) {
                    mutableState.journey.player.position.x += r * dX
                }
                if (dY != 0.0) {
                    mutableState.journey.player.position.y += r * dY
                }
            }
        } else {
            val joyLeftX = joystick.joyLeft.x.let {
                if (it.absoluteValue < 0.25) {
                    0.0
                } else {
                    it
                }
            }
            val joyLeftY = joystick.joyLeft.y.let {
                if (it.absoluteValue < 0.25) {
                    0.0
                } else {
                    it
                }
            }
            if (joyLeftX.absoluteValue < 0.25 && joyLeftY.absoluteValue < 0.25) {
                // todo
            } else {
//                println("joyLeftX:$joyLeftX joyLeftY:$joyLeftY")
//                val velocityMultiple = sqrt(dX * dX + dY * dY) / sqrt(2.0)
                val dX: Double
                val dY: Double
                when {
                    sqrt(joyLeftX*joyLeftX + joyLeftY*joyLeftY) < 1 -> {
                        dX = joyLeftX
                        dY = joyLeftY
                    }
                    joyLeftX.absoluteValue < joyLeftY.absoluteValue -> {
                        dX = joyLeftX
//                        dY = joyLeftY
                        dY = sqrt(1 - dX * dX) * joyLeftY.let {
                            if (it < 0) -1 else 1
                        }
                    }
                    else -> {
//                        dX = joyLeftX
                        dY = joyLeftY
                        dX = sqrt(1 - dY * dY) * joyLeftX.let {
                            if (it < 0) -1 else 1
                        }
                    }
                }
                val velocityMultiple = sqrt(dX * dX + dY * dY)
//                println("vM:$velocityMultiple")
                val units = mutableState.journey.player.velocity * velocityMultiple * time
//                println("units:$units")
                val dP = units * pixelsPerUnit
                mutableState.journey.player.position.x += dP * dX
                mutableState.journey.player.position.y += dP * dY
            }
        }
        val direction: Double
        val playerPositionNew = mutableState.journey.player.position
        direction = Math.toDegrees(
            Math.atan2(
                playerPositionOld.x - playerPositionNew.x,
                playerPositionOld.y - playerPositionNew.y
            )
        ) * -1
        mutableState.journey.player.direction = direction
        // todo
    }

    override fun onRender(canvas: Canvas, engineInputState: EngineInputState, engineProperty: EngineProperty) {
        val dTime = engineProperty.timeNow - engineProperty.timeLast
        val state: State = mutableState
        //
//        val playerSize = square(size = 50)
        val playerSize = size(width = 75, height = 50)
        val playerPosition = point(
            x = state.journey.player.position.x - playerSize.width / 2,
            y = state.journey.player.position.y - playerSize.height /2
        )
        val pointOfRotation = state.journey.player.position
        canvas.drawRectangle(
            color = ColorEntity.RED,
            pointTopLeft = playerPosition,
            size = playerSize,
            direction = state.journey.player.direction,
            pointOfRotation = pointOfRotation
        )
        val directionSize = size(15, 25)
        canvas.drawRectangle(
            color = ColorEntity.GREEN,
            pointTopLeft = point(
                x = state.journey.player.position.x - directionSize.width/2,
                y = playerPosition.y - directionSize.height/2
            ),
            size = directionSize,
            direction = state.journey.player.direction,
            pointOfRotation = pointOfRotation
        )
        canvas.drawPoint(
            color = ColorEntity.YELLOW,
            point = point(x = state.journey.player.position.x, y = playerPosition.y)
        )
        canvas.drawPoint(
            color = ColorEntity.WHITE,
            point = playerPosition
        )
        canvas.drawPoint(
            color = ColorEntity.GREEN,
            point = state.journey.player.position
        )
        canvas.drawText(
            fullPathFont = fullPathFont,
            color = ColorEntity.GREEN,
            pointTopLeft = point(x = 50, y = 50),
            text = state.journey.player.position.toString(),
            fontHeight = 16f
        )
        canvas.drawText(
            fullPathFont = fullPathFont,
            color = ColorEntity.GREEN,
            pointTopLeft = point(x = 50, y = 75),
            text = String.format("%.1f", state.journey.player.direction),
            fontHeight = 16f
        )
        // todo
        val framesPerSecond = TimeUnit.SECONDS.convert(1.0, TimeUnit.NANOSECONDS) / dTime
        canvas.drawText(
            fullPathFont = fullPathFont,
            color = ColorEntity.GREEN,
            pointTopLeft = point(engineProperty.pictureSize.width - 50, engineProperty.pictureSize.height - 50),
            text = String.format("%.1f", framesPerSecond),
            fontHeight = 16f
        )
    }
}
