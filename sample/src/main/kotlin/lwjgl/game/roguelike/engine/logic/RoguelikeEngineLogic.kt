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
import kotlin.math.absoluteValue
import kotlin.math.atan2
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

    private fun onUpdateStatePlayerPositionHorizontal(dX: Double) {
        onUpdateStatePlayerPosition(dX = dX, dY = 0.0)
    }
    private fun onUpdateStatePlayerPositionVertical(dY: Double) {
        onUpdateStatePlayerPosition(dX = 0.0, dY = dY)
    }
    private fun onUpdateStatePlayerPosition(
        dX: Double,
        dY: Double
    ) {
        val (oldX, oldY) = mutableState.journey.player.position.copy()
        val (newX, newY) = mutableState.journey.player.position.also {
            it.x += dX
            it.y += dY
        }
        val direction = Math.toDegrees(
            atan2(y = oldX - newX, x = oldY - newY)
        ) * -1
        mutableState.journey.player.direction = direction
    }
    private fun calculateTime(engineProperty: EngineProperty): Double {
        return TimeUnit.NANOSECONDS.convert(
            time = engineProperty.timeNow - engineProperty.timeLast,
            timeUnit = TimeUnit.SECONDS
        )
    }
    private fun onUpdateStatePlayerPositionKeyboard(
        keyboard: EngineInputState.Keyboard,
        engineProperty: EngineProperty
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
            isLeft && isRight && isTop && !isBottom || !isLeft && !isRight && isTop && !isBottom -> { // only /\
                val velocityMultiple = 1.0
                val units = mutableState.journey.player.velocity * velocityMultiple * calculateTime(engineProperty)
                onUpdateStatePlayerPositionVertical(dY = - units * pixelsPerUnit)
            }
            isLeft && isRight && !isTop && isBottom || !isLeft && !isRight && !isTop && isBottom -> { // only \/
                val velocityMultiple = 1.0
                val units = mutableState.journey.player.velocity * velocityMultiple * calculateTime(engineProperty)
                onUpdateStatePlayerPositionVertical(dY = units * pixelsPerUnit)
            }
            isLeft && !isRight && isTop && !isBottom -> { // </\
                val velocityMultiple = 1.0
                val units = mutableState.journey.player.velocity * velocityMultiple * calculateTime(engineProperty)
                val result = units * pixelsPerUnit / sqrt(2.0)
                onUpdateStatePlayerPosition(dX = - result, dY = - result)
            }
            isLeft && !isRight && !isTop && isBottom -> { // <\/
                val velocityMultiple = 1.0
                val units = mutableState.journey.player.velocity * velocityMultiple * calculateTime(engineProperty)
                val result = units * pixelsPerUnit / sqrt(2.0)
                onUpdateStatePlayerPosition(dX = - result, dY = result)
            }
            isLeft && !isRight && !isTop && !isBottom || isLeft && !isRight && isTop && isBottom -> { // only <
                val velocityMultiple = 1.0
                val units = mutableState.journey.player.velocity * velocityMultiple * calculateTime(engineProperty)
                onUpdateStatePlayerPositionHorizontal(dX = - units * pixelsPerUnit)
            }
            !isLeft && isRight && isTop && !isBottom -> { // /\>
                val velocityMultiple = 1.0
                val units = mutableState.journey.player.velocity * velocityMultiple * calculateTime(engineProperty)
                val result = units * pixelsPerUnit / sqrt(2.0)
                onUpdateStatePlayerPosition(dX = result, dY = - result)
            }
            !isLeft && isRight && !isTop && isBottom -> { // \/>
                val velocityMultiple = 1.0
                val units = mutableState.journey.player.velocity * velocityMultiple * calculateTime(engineProperty)
                val result = units * pixelsPerUnit / sqrt(2.0)
                onUpdateStatePlayerPosition(dX = result, dY = result)
            }
            !isLeft && isRight && !isTop && !isBottom || !isLeft && isRight && isTop && isBottom -> { // only >
                val velocityMultiple = 1.0
                val units = mutableState.journey.player.velocity * velocityMultiple * calculateTime(engineProperty)
                onUpdateStatePlayerPositionHorizontal(dX = units * pixelsPerUnit)
            }
        }
    }
    private fun onUpdateStatePlayerPositionJoystick(
        joystick: EngineInputState.Joystick,
        engineProperty: EngineProperty
    ) {
//        val min = 0.25
        val min = 0.025
        val joyLeftX = joystick.joyLeft.x
        val joyLeftY = joystick.joyLeft.y
        when {
            joyLeftX.absoluteValue < min && joyLeftY.absoluteValue < min -> return
            joyLeftX.absoluteValue < min -> {
                val velocityMultiple = sqrt(joyLeftY * joyLeftY)
                val time = calculateTime(engineProperty)
                val units = mutableState.journey.player.velocity * velocityMultiple * time
                val dP = units * pixelsPerUnit
                onUpdateStatePlayerPositionVertical(dY = dP * joyLeftY)
            }
            joyLeftY.absoluteValue < min -> {
                val velocityMultiple = sqrt(joyLeftX * joyLeftX)
                val time = calculateTime(engineProperty)
                val units = mutableState.journey.player.velocity * velocityMultiple * time
                val dP = units * pixelsPerUnit
                onUpdateStatePlayerPositionHorizontal(dX = dP * joyLeftX)
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
                        dY = sqrt(1 - dX * dX) * joyLeftY.let {
                            if (it < 0) -1 else 1
                        }
                    }
                    else -> {
                        dY = joyLeftY
                        dX = sqrt(1 - dY * dY) * joyLeftX.let {
                            if (it < 0) -1 else 1
                        }
                    }
                }
                val velocityMultiple = sqrt(dX * dX + dY * dY)
                val time = calculateTime(engineProperty)
                val units = mutableState.journey.player.velocity * velocityMultiple * time
                val dP = units * pixelsPerUnit
                onUpdateStatePlayerPosition(dX = dP * dX, dY = dP * dY)
            }
        }
    }
    override fun onUpdateState(engineInputState: EngineInputState, engineProperty: EngineProperty) {
        val joystick = engineInputState.joysticks.firstOrNull { it != null }
        if (joystick == null) {
            onUpdateStatePlayerPositionKeyboard(
                keyboard = engineInputState.keyboard,
                engineProperty = engineProperty
            )
        } else {
            onUpdateStatePlayerPositionJoystick(
                joystick = joystick,
                engineProperty = engineProperty
            )
        }
        // todo
    }

    private fun onRender(
        canvas: Canvas,
        engineInputState: EngineInputState,
        engineProperty: EngineProperty,
        player: State.Journey.Player
    ) {
//        val playerSize = square(size = 50)
        val playerSize = size(width = 25, height = 25)
        val playerPosition = point(
            x = player.position.x - playerSize.width / 2,
            y = player.position.y - playerSize.height /2
        )
        val pointOfRotation = player.position
        canvas.drawRectangle(
            color = ColorEntity.RED,
            pointTopLeft = playerPosition,
            size = playerSize,
            direction = player.direction,
            pointOfRotation = pointOfRotation
        )
        canvas.drawLine(
            color = ColorEntity.GREEN,
            pointStart = player.position,
            pointFinish = point(
                x = player.position.x,
                y = playerPosition.y
            ),
            lineWidth = 1f,
            direction = player.direction,
            pointOfRotation = pointOfRotation
        )
//        val directionSize = size(15, 25)
//        canvas.drawRectangle(
//            color = ColorEntity.GREEN,
//            pointTopLeft = point(
//                x = player.position.x - directionSize.width/2,
//                y = playerPosition.y - directionSize.height/2
//            ),
//            size = directionSize,
//            direction = player.direction,
//            pointOfRotation = pointOfRotation
//        )
//        canvas.drawPoint(
//            color = ColorEntity.YELLOW,
//            point = point(x = player.position.x, y = playerPosition.y)
//        )
        canvas.drawPoint(
            color = ColorEntity.WHITE,
            point = playerPosition
        )
        canvas.drawPoint(
            color = ColorEntity.GREEN,
            point = player.position
        )
        canvas.drawText(
            fullPathFont = fullPathFont,
            color = ColorEntity.GREEN,
            pointTopLeft = point(x = 50, y = 50),
            text = player.position.toString(),
            fontHeight = 16f
        )
        canvas.drawText(
            fullPathFont = fullPathFont,
            color = ColorEntity.GREEN,
            pointTopLeft = point(x = 50, y = 75),
            text = String.format("%.1f", player.direction),
            fontHeight = 16f
        )
    }
    override fun onRender(canvas: Canvas, engineInputState: EngineInputState, engineProperty: EngineProperty) {
        val dTime = engineProperty.timeNow - engineProperty.timeLast
        val state: State = mutableState
        //
        onRender(canvas, engineInputState, engineProperty, player = state.journey.player)
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
