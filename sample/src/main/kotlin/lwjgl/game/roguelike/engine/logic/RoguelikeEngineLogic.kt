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
import lwjgl.wrapper.entity.*
import lwjgl.wrapper.util.glfw.key.KeyStatus
import lwjgl.wrapper.util.resource.ResourceProvider
import org.lwjgl.glfw.GLFW
import kotlin.math.*

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

    private const val pixelsPerUnit = 25.0 // todo

    override fun onPreLoop() {
        // todo
        mutableState.journey.player.position.x = playerSize.width / 2
        mutableState.journey.player.position.y = playerSize.height / 2
        mutableState.journey.player.directionActual = 135.0
        mutableState.journey.player.directionExpected = 135.0
    }

    private fun calculateAngle(oldX: Double, oldY: Double, newX: Double, newY: Double): Double {
        val angle = atan2(y = oldX - newX, x = oldY - newY)
        val degrees = angle * -180.0 / kotlin.math.PI
        return degrees + ceil(-degrees / 360.0) * 360.0
    }
    private val playerSize = size(width = 1 * pixelsPerUnit, height = 1 * pixelsPerUnit) // todo
    private fun onUpdateStatePlayerPosition(
        dTime: Double,
        velocityMultiple: Double,
        dX: Double,
        dY: Double
    ) {
        val player = mutableState.journey.player
        val (oldX, oldY) = player.position
        //
        player.directionExpected = calculateAngle(oldX = oldX, oldY = oldY, newX = oldX + dX, newY = oldY + dY)
        //
        val units = mutableState.journey.player.velocity * velocityMultiple * dTime
        val result = units * pixelsPerUnit
        val radians = Math.toRadians(player.directionExpected)
        val newX = oldX + result * sin(radians)
        val newY = oldY - result * cos(radians)
        val pW = playerSize.width / 2
        val pH = playerSize.height / 2
        val territory = mutableState.journey.territory
        val isMoveX = newX in pW..(territory.size.width * pixelsPerUnit - pW) &&
            !territory.regions.any {
                val rX = it.position.x * pixelsPerUnit
                val rY = it.position.y * pixelsPerUnit
                newX in (rX - pW)..(rX + it.size.width * pixelsPerUnit + pW) &&
                    oldY in (rY - pH)..(rY + it.size.height * pixelsPerUnit + pH)
            }
        val isMoveY = newY in pH..(territory.size.height* pixelsPerUnit - pH) &&
            !territory.regions.any {
                val rX = it.position.x * pixelsPerUnit
                val rY = it.position.y * pixelsPerUnit
                newY in (rY - pH)..(rY + it.size.height * pixelsPerUnit + pH) &&
                    oldX in (rX - pW)..(rX + it.size.width * pixelsPerUnit + pW)
            }
        if (isMoveX) {
            player.position.x = newX
        }
        if (isMoveY) {
            player.position.y = newY
        }
    }
    private fun onUpdateStatePlayerPosition(
        dTime: Double,
        keyboard: EngineInputState.Keyboard
    ) {
        val printableKeys = keyboard.printableKeys
        val isLeft = printableKeys[PrintableKey.A] == KeyStatus.PRESS
        val isRight = printableKeys[PrintableKey.D] == KeyStatus.PRESS
        val isTop = printableKeys[PrintableKey.W] == KeyStatus.PRESS
        val isBottom = printableKeys[PrintableKey.S] == KeyStatus.PRESS
        when {
            isLeft && isRight && isTop && isBottom ||
                isLeft && isRight && !isTop && !isBottom ||
                !isLeft && !isRight && isTop && isBottom ||
                !isLeft && !isRight && !isTop && !isBottom -> return
        }
        var dX = 0.0
        if (isLeft) {
            dX -= 1.0
        }
        if (isRight) {
            dX += 1.0
        }
        var dY = 0.0
        if (isTop) {
            dY -= 1.0
        }
        if (isBottom) {
            dY += 1.0
        }
        val velocityMultiple = 1.0
        onUpdateStatePlayerPosition(dTime = dTime, velocityMultiple = velocityMultiple, dX = dX, dY = dY)
    }
    private fun onUpdateStatePlayerPosition(
        dTime: Double,
        joystick: EngineInputState.Joystick
    ) {
//        val min = 0.25
        val min = 0.025
        val joyLeftX = joystick.joyLeft.x
        val joyLeftY = joystick.joyLeft.y
        when {
            joyLeftX.absoluteValue < min && joyLeftY.absoluteValue < min -> return
            joyLeftX.absoluteValue < min -> {
                val velocityMultiple = sqrt(joyLeftY * joyLeftY)
                onUpdateStatePlayerPosition(dTime = dTime, velocityMultiple = velocityMultiple, dX = 0.0, dY = joyLeftY)
            }
            joyLeftY.absoluteValue < min -> {
                val velocityMultiple = sqrt(joyLeftX * joyLeftX)
                onUpdateStatePlayerPosition(dTime = dTime, velocityMultiple = velocityMultiple, dX = joyLeftX, dY = 0.0)
            }
            else -> {
                val dX: Double
                val dY: Double
                when {
                    sqrt(joyLeftX * joyLeftX + joyLeftY * joyLeftY) < 1 -> {
                        dX = joyLeftX
                        dY = joyLeftY
                    }
                    joyLeftX.absoluteValue < joyLeftY.absoluteValue -> {
                        dX = joyLeftX
                        dY = sqrt(1 - dX * dX) * if (joyLeftY < 0) -1 else 1
                    }
                    else -> {
                        dY = joyLeftY
                        dX = sqrt(1 - dY * dY) * if (joyLeftX < 0) -1 else 1
                    }
                }
                val velocityMultiple = sqrt(dX * dX + dY * dY)
                onUpdateStatePlayerPosition(dTime = dTime, velocityMultiple = velocityMultiple, dX = dX, dY = dY)
            }
        }
    }
    override fun onUpdateState(engineInputState: EngineInputState, engineProperty: EngineProperty) {
        val dTime = engineProperty.timeNow - engineProperty.timeLast
        val joystick = engineInputState.joysticks.firstOrNull { it != null }
        if (joystick == null) {
            onUpdateStatePlayerPosition(
                dTime = dTime,
                keyboard = engineInputState.keyboard
            )
        } else {
            onUpdateStatePlayerPosition(
                dTime = dTime,
                joystick = joystick
            )
        }
        //
        val directionActual = mutableState.journey.player.directionActual
        val directionExpected = mutableState.journey.player.directionExpected
        if (directionActual != directionExpected) {
            val directionVelocity = 360.0 / TimeUnit.NANO_IN_SECOND
            val difference = (directionActual - directionExpected).let {
                when {
                    it > 180.0 -> it - 360.0
                    it <-180.0 -> it + 360.0
                    else -> it
                }
            }
            val d = directionVelocity * dTime
            if (d > difference.absoluteValue) {
                mutableState.journey.player.directionActual = directionExpected
            } else {
                if (difference < 0) {
                    mutableState.journey.player.directionActual += d
                } else {
                    mutableState.journey.player.directionActual -= d
                }
            }
        }
        // todo
    }

    private fun onRender(
        canvas: Canvas,
        center: Point,
        player: State.Journey.Player
    ) {
        val playerPosition = point(
            x = center.x - playerSize.width / 2,
            y = center.y - playerSize.height /2
        )
        canvas.drawRectangle(
            color = ColorEntity.RED,
            pointTopLeft = playerPosition,
            size = playerSize,
            direction = player.directionActual,
            pointOfRotation = center
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
    }
    private fun onRender(
        canvas: Canvas,
        engineProperty: EngineProperty,
        journey: State.Journey
    ) {
        val center = point(x = engineProperty.pictureSize.width / 2, y = engineProperty.pictureSize.height / 2)
        canvas.drawRectangle(
            color = ColorEntity.WHITE,
            pointTopLeft = point(
                x = center.x - journey.player.position.x,
                y = center.y - journey.player.position.y
            ),
            size = size(width = journey.territory.size.width * pixelsPerUnit, height = journey.territory.size.height * pixelsPerUnit)
        )
        journey.territory.regions.forEach { region ->
            canvas.drawRectangle(
                color = region.color,
                pointTopLeft = point(
                    x = center.x + region.position.x * pixelsPerUnit - journey.player.position.x,
                    y = center.y + region.position.y * pixelsPerUnit - journey.player.position.y
                ),
                size = size(width = region.size.width * pixelsPerUnit, height = region.size.height * pixelsPerUnit)
            )
        }
        onRender(
            canvas = canvas,
            center = center,
            player = journey.player
        )
    }
    override fun onRender(canvas: Canvas, engineInputState: EngineInputState, engineProperty: EngineProperty) {
        val state: State = mutableState
        //
        onRender(canvas, engineProperty = engineProperty, journey = state.journey)
        // todo
        val framesPerSecond = TimeUnit.NANO_IN_SECOND / (engineProperty.timeNow - engineProperty.timeLast)
        canvas.drawText(
            fullPathFont = fullPathFont,
            color = ColorEntity.GREEN,
            pointTopLeft = point(engineProperty.pictureSize.width - 50, engineProperty.pictureSize.height - 50),
            text = String.format("%.1f", framesPerSecond),
            fontHeight = 16f
        )
    }
}
