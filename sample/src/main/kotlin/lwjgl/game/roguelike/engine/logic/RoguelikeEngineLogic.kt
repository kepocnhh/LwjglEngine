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
import kotlin.math.ceil
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

    private const val pixelsPerUnit = 25.0 // todo

    override fun onPreLoop() {
        // todo
        mutableState.journey.player.position.x = playerSize.width / 2
        mutableState.journey.player.position.y = playerSize.height / 2
        mutableState.journey.player.direction = 135.0
    }

    private fun onUpdateStatePlayerPositionHorizontal(dTime: Double, dX: Double) {
        onUpdateStatePlayerPosition(dTime = dTime, dX = dX, dY = 0.0)
    }
    private fun onUpdateStatePlayerPositionVertical(dTime: Double, dY: Double) {
        onUpdateStatePlayerPosition(dTime = dTime, dX = 0.0, dY = dY)
    }
    private fun calculateAngle(oldX: Double, oldY: Double, newX: Double, newY: Double): Double {
        val angle = atan2(y = oldX - newX, x = oldY - newY)
        val degrees = angle * -180.0 / kotlin.math.PI
        return degrees + ceil(-degrees / 360.0) * 360.0
    }
    private val playerSize = size(width = 1 * pixelsPerUnit, height = 1 * pixelsPerUnit) // todo
    private fun onUpdateStatePlayerPosition(
        dTime: Double,
        dX: Double,
        dY: Double
    ) {
        val player = mutableState.journey.player
        val (oldX, oldY) = player.position
        val newX = oldX + dX
        val newY = oldY + dY
        val territory = mutableState.journey.territory
        val pW = playerSize.width / 2
        val pH = playerSize.height / 2
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
//        if (!isMoveX && !isMoveY) return
        player.position.also {
            it.x = if (isMoveX) newX else oldX
            it.y = if (isMoveY) newY else oldY
        }
        val directionExpected = calculateAngle(oldX = oldX, oldY = oldY, newX = newX, newY = newY)
        val directionOld = player.direction
        if (directionOld != directionExpected) {
//            println("directionExpected: $directionExpected")
//            val directionNew = directionExpected
            val directionVelocity = 180.0 / TimeUnit.NANO_IN_SECOND
            val difference = (directionOld - directionExpected).let {
                when {
                    it > 180.0 -> it - 360.0
                    it < -180.0-> it + 360.0
                    else -> it
                }
            }
//            println("difference: $difference")
            val d = directionVelocity * dTime
            if (d > difference.absoluteValue) {
                player.direction = directionExpected
            } else {
                val directionNew = if (difference < 0) {
                    directionOld + d
                } else {
                    directionOld - d
                }
                player.direction = directionNew
            }
        }
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
                val dTime = engineProperty.timeNow - engineProperty.timeLast
                val units = mutableState.journey.player.velocity * velocityMultiple * dTime
                onUpdateStatePlayerPositionVertical(dTime = dTime, dY = - units * pixelsPerUnit)
            }
            isLeft && isRight && !isTop && isBottom || !isLeft && !isRight && !isTop && isBottom -> { // only \/
                val velocityMultiple = 1.0
                val dTime = engineProperty.timeNow - engineProperty.timeLast
                val units = mutableState.journey.player.velocity * velocityMultiple * dTime
                onUpdateStatePlayerPositionVertical(dTime = dTime, dY = units * pixelsPerUnit)
            }
            isLeft && !isRight && isTop && !isBottom -> { // </\
                val velocityMultiple = 1.0
                val dTime = engineProperty.timeNow - engineProperty.timeLast
                val units = mutableState.journey.player.velocity * velocityMultiple * dTime
                val result = units * pixelsPerUnit / sqrt(2.0)
                onUpdateStatePlayerPosition(dTime = dTime, dX = - result, dY = - result)
            }
            isLeft && !isRight && !isTop && isBottom -> { // <\/
                val velocityMultiple = 1.0
                val dTime = engineProperty.timeNow - engineProperty.timeLast
                val units = mutableState.journey.player.velocity * velocityMultiple * dTime
                val result = units * pixelsPerUnit / sqrt(2.0)
                onUpdateStatePlayerPosition(dTime = dTime, dX = - result, dY = result)
            }
            isLeft && !isRight && !isTop && !isBottom || isLeft && !isRight && isTop && isBottom -> { // only <
                val velocityMultiple = 1.0
                val dTime = engineProperty.timeNow - engineProperty.timeLast
                val units = mutableState.journey.player.velocity * velocityMultiple * dTime
                onUpdateStatePlayerPositionHorizontal(dTime = dTime, dX = - units * pixelsPerUnit)
            }
            !isLeft && isRight && isTop && !isBottom -> { // /\>
                val velocityMultiple = 1.0
                val dTime = engineProperty.timeNow - engineProperty.timeLast
                val units = mutableState.journey.player.velocity * velocityMultiple * dTime
                val result = units * pixelsPerUnit / sqrt(2.0)
                onUpdateStatePlayerPosition(dTime = dTime, dX = result, dY = - result)
            }
            !isLeft && isRight && !isTop && isBottom -> { // \/>
                val velocityMultiple = 1.0
                val dTime = engineProperty.timeNow - engineProperty.timeLast
                val units = mutableState.journey.player.velocity * velocityMultiple * dTime
                val result = units * pixelsPerUnit / sqrt(2.0)
                onUpdateStatePlayerPosition(dTime = dTime, dX = result, dY = result)
            }
            !isLeft && isRight && !isTop && !isBottom || !isLeft && isRight && isTop && isBottom -> { // only >
                val velocityMultiple = 1.0
                val dTime = engineProperty.timeNow - engineProperty.timeLast
                val units = mutableState.journey.player.velocity * velocityMultiple * dTime
                onUpdateStatePlayerPositionHorizontal(dTime = dTime, dX = units * pixelsPerUnit)
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
                val dTime = engineProperty.timeNow - engineProperty.timeLast
                val units = mutableState.journey.player.velocity * velocityMultiple * dTime
                val dP = units * pixelsPerUnit
                onUpdateStatePlayerPositionVertical(dTime = dTime, dY = dP * joyLeftY)
            }
            joyLeftY.absoluteValue < min -> {
                val velocityMultiple = sqrt(joyLeftX * joyLeftX)
                val dTime = engineProperty.timeNow - engineProperty.timeLast
                val units = mutableState.journey.player.velocity * velocityMultiple * dTime
                val dP = units * pixelsPerUnit
                onUpdateStatePlayerPositionHorizontal(dTime = dTime, dX = dP * joyLeftX)
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
                val dTime = engineProperty.timeNow - engineProperty.timeLast
                val units = mutableState.journey.player.velocity * velocityMultiple * dTime
                val dP = units * pixelsPerUnit
                onUpdateStatePlayerPosition(dTime = dTime, dX = dP * dX, dY = dP * dY)
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
            direction = player.direction,
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
            direction = player.direction,
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
    private fun onRenderDeprecated(
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
