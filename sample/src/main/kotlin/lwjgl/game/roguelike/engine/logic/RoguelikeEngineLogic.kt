package lwjgl.game.roguelike.engine.logic

import lwjgl.engine.common.EngineLogic
import lwjgl.engine.common.EngineProperty
import lwjgl.engine.common.input.EngineInputCallback
import lwjgl.engine.common.input.EngineInputState
import lwjgl.engine.common.input.FunctionKey
import lwjgl.engine.common.input.PrintableKey
import lwjgl.game.roguelike.engine.render.Render
import lwjgl.game.roguelike.state.State
import lwjgl.game.roguelike.util.TimeUnit
import lwjgl.wrapper.canvas.Canvas
import lwjgl.wrapper.entity.*
import lwjgl.wrapper.util.glfw.key.KeyStatus
import lwjgl.wrapper.util.resource.ResourceProvider
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.sqrt
import kotlin.math.absoluteValue

object RoguelikeEngineLogic : EngineLogic {
    private val fullPathFont = ResourceProvider.requireResourceAsFile("font.main.ttf").absolutePath
    private fun rect(leftTop: Point, rightBottom: Point): List<Point> {
        return listOf(
            leftTop, // </\
            point(x = rightBottom.x, y = leftTop.y), // /\>
            rightBottom, // \/>
            point(x = leftTop.x, y = rightBottom.y) // <\/
        )
    }
    private val defaultTerritoryRegions = listOf(
        StateJourneyTerritoryRegion(
            points = rect(leftTop = point(x = 0, y = 0), rightBottom = point(x = 2, y = 2)),
            color = ColorEntity.RED,
            isPassable = true
        ),
        StateJourneyTerritoryRegion(
            points = rect(leftTop = point(x = 18, y = 18), rightBottom = point(x = 20, y = 20)),
            color = ColorEntity.BLUE,
            isPassable = true
        )
    )
    private val defaultTerritory: State.Journey.Territory = defaultTerritoryRegions.let { regions ->
        val points = regions.flatMap { it.points }
        StateJourneyTerritory(
            size = size(
                width = points.maxBy { it.x }!!.x,
                height = points.maxBy { it.y }!!.y
            ),
            regions = regions
        )
    }

    private val mutableState: MutableState = MutableState(
        defaultCommon = State.Common.MAIN_MENU
    )

    override val framesPerSecondExpected: Int = 60
    override val shouldEngineStop: Boolean get() {
        return mutableState.shouldEngineStop
    }

    private fun onMainMenuInputCallback(key: PrintableKey, status: KeyStatus) {
        when (key) {
            PrintableKey.S -> {
                if (status == KeyStatus.PRESS) {
                    val ordinal = mutableState.mainMenu.selectedMenuItem.ordinal
                    val values = State.MainMenu.Item.values()
                    mutableState.mainMenu.selectedMenuItem = when (ordinal) {
                        values.lastIndex -> values.first()
                        else -> values[ordinal + 1]
                    }
                }
            }
            PrintableKey.W -> {
                if (status == KeyStatus.PRESS) {
                    val ordinal = mutableState.mainMenu.selectedMenuItem.ordinal
                    val values = State.MainMenu.Item.values()
                    mutableState.mainMenu.selectedMenuItem = when (ordinal) {
                        0 -> values.last()
                        else -> values[ordinal - 1]
                    }
                }
            }
        }
    }
    private fun Size.fromUnitsToPixels(pixelsPerUnit: Double): Size {
        return size(
            width = width * pixelsPerUnit,
            height = height * pixelsPerUnit
        )
    }
    private fun Point.fromUnitsToPixels(pixelsPerUnit: Double): Point {
        return point(
            x = x * pixelsPerUnit,
            y = y * pixelsPerUnit
        )
    }
    private fun State.Journey.Territory.fromUnitsToPixels(pixelsPerUnit: Double): State.Journey.Territory {
        return StateJourneyTerritory(
            size = size.fromUnitsToPixels(pixelsPerUnit = pixelsPerUnit),
            regions = regions.map { region ->
                StateJourneyTerritoryRegion(
                    points = region.points.map {
                        it.fromUnitsToPixels(pixelsPerUnit = pixelsPerUnit)
                    },
                    color = region.color,
                    isPassable = region.isPassable
                )
            }
        )
    }
    private fun onMainMenuInputCallback(key: FunctionKey, status: KeyStatus) {
        when (key) {
            FunctionKey.ESCAPE -> {
                when (status) {
                    KeyStatus.RELEASE -> {
                        mutableState.engineStop()
                    }
                }
            }
            FunctionKey.ENTER -> {
                when (status) {
                    KeyStatus.PRESS -> {
                        when (mutableState.mainMenu.selectedMenuItem) {
                            State.MainMenu.Item.START_NEW_GAME -> {
                                val territory = defaultTerritory.fromUnitsToPixels(pixelsPerUnit = pixelsPerUnit)
                                val distanceMin = sqrt(playerSize.width * playerSize.width + playerSize.height * playerSize.height) / 2
                                val direction = 135.0
                                val velocity = 5.0 / TimeUnit.NANO_IN_SECOND
                                val journey = MutableStateJourney(
                                    territory = territory,
                                    player = MutableStateJourneyPlayer(
                                        position = MutablePoint(x = distanceMin, y = distanceMin),
                                        velocity = velocity,
                                        directionActual = direction,
                                        directionExpected = direction
                                    ),
                                    snapshot = MutableStateJourneySnapshot(
                                        dummy = MutableDummy(
                                            position = MutablePoint(
                                                x = territory.size.width - distanceMin,
                                                y = territory.size.height -distanceMin
//                                                x = distanceMin,
//                                                y = distanceMin + pixelsPerUnit * 2
                                            ),
                                            velocity = velocity,
                                            directionActual = 315.0,
                                            directionExpected = 315.0
                                        )
                                    )
                                )
                                mutableState.journey = journey
                                mutableState.common = State.Common.JOURNEY
                            }
                            State.MainMenu.Item.EXIT -> {
                                mutableState.engineStop()
                            }
                        }
                    }
                }
            }
        }
    }
    override val engineInputCallback = object : EngineInputCallback {
        override fun onPrintableKey(key: PrintableKey, status: KeyStatus) {
            when (mutableState.common) {
                State.Common.MAIN_MENU -> {
                    onMainMenuInputCallback(key, status)
                }
                State.Common.JOURNEY -> {
                    // ignored
                }
            }
        }

        override fun onFunctionKey(key: FunctionKey, status: KeyStatus) {
            when (mutableState.common) {
                State.Common.MAIN_MENU -> {
                    onMainMenuInputCallback(key, status)
                }
                State.Common.JOURNEY -> {
                    when (key) {
                        FunctionKey.ESCAPE -> {
                            when (status) {
                                KeyStatus.RELEASE -> {
                                    mutableState.engineStop()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private const val pixelsPerUnit = 25.0 // todo

    override fun onPreLoop() {
        // todo
    }

    private fun calculateAngle(oldX: Double, oldY: Double, newX: Double, newY: Double): Double {
        val angle = atan2(y = oldX - newX, x = oldY - newY)
        val degrees = angle * -180.0 / kotlin.math.PI
        return degrees + ceil(-degrees / 360.0) * 360.0
    }
    private val playerSize = size(width = 1 * pixelsPerUnit, height = 1 * pixelsPerUnit) // todo
    private fun closerThan(
        points: List<Point>,
        x: Double,
        y: Double,
        distanceMin: Double
    ): Boolean {
        var i = 0
        val size = points.size
        while (true) {
            val next: Int = when (i) {
                size -> return false
                size - 1 -> 0
                else -> i + 1
            }
//            val pointStart = points[i]
//            val pointFinish = points[next]
            val distanceActual = calculateDistance(
                pointStart = points[i],
                pointFinish = points[next],
                x = x,
                y = y
            )
            if (distanceActual < distanceMin) {
//                val fX = String.format("%.1f", x)
//                val fY = String.format("%.1f", y)
//                println("s:$pointStart,f:$pointFinish,g:{$fX,$fY},m:${String.format("%.1f", distanceMin)},d:${String.format("%.1f", distanceActual)}")
                return true
            }
            i++
        }
    }
    private fun calculateDistance(
        pointStart: Point,
        pointFinish: Point,
        x: Double,
        y: Double
    ): Double {
        val dY = pointFinish.y - pointStart.y
        val dX = pointFinish.x - pointStart.x
        val d = sqrt(dY * dY + dX * dX)
        val dS = sqrt((pointStart.y - y) * (pointStart.y - y) + (pointStart.x - x) * (pointStart.x - x))
        val dF = sqrt((pointFinish.y - y) * (pointFinish.y - y) + (pointFinish.x - x) * (pointFinish.x - x))
        if (dF > d) return dS
        if (dS > d) return dF
        return (dY * x - dX * y + pointFinish.x * pointStart.y - pointFinish.y * pointStart.x).absoluteValue / d
//        val c1 = sqrt(
//            (pointStart.x - x) * (pointStart.x - x) +
//            (pointStart.y - y) * (pointStart.y - y)
//        )
//        val c2 = sqrt(
//            (pointFinish.x - x) * (pointFinish.x - x) +
//            (pointFinish.y - y) * (pointFinish.y - y)
//        )
//        val a = sqrt(
//            (pointStart.x - pointFinish.x) * (pointStart.x - pointFinish.x) +
//            (pointStart.y - pointFinish.y) * (pointStart.y - pointFinish.y)
//        )
//        val tmp = (c1 * c1 - c2 * c2 + a * a) / (2 * a)
//        return sqrt(c1 * c1 - tmp * tmp)
    }
    private fun onUpdateStatePlayerPosition(
        dTime: Double,
        journey: MutableStateJourney,
        velocityMultiple: Double,
        dX: Double,
        dY: Double
    ) {
        val player = journey.player
        val (oldX, oldY) = player.position
        //
        player.directionExpected = calculateAngle(oldX = oldX, oldY = oldY, newX = oldX + dX, newY = oldY + dY)
        //
        val units = player.velocity * velocityMultiple * dTime
        val result = units * pixelsPerUnit
        val radians = Math.toRadians(player.directionExpected)
        val newX = oldX + result * sin(radians)
        val newY = oldY - result * cos(radians)
//        val pW = playerSize.width / 2
//        val pH = playerSize.height / 2
        val distanceMin = sqrt(playerSize.width * playerSize.width + playerSize.height * playerSize.height) / 2
        val territory = journey.territory
//        val isMoveX = true
//        val isMoveY = true
        val isMoveX = newX in distanceMin..(territory.size.width - distanceMin) && !territory.regions.any {
            !it.isPassable && closerThan(
                points = it.points,
                x = newX,
                y = oldY,
                distanceMin = distanceMin
            )
        }
        val isMoveY = newY in distanceMin..(territory.size.height - distanceMin) && !territory.regions.any {
            !it.isPassable && closerThan(
                points = it.points,
                x = oldX,
                y = newY,
                distanceMin = distanceMin
            )
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
        journey: MutableStateJourney,
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
        onUpdateStatePlayerPosition(
            dTime = dTime,
            journey = journey,
            velocityMultiple = velocityMultiple,
            dX = dX,
            dY = dY
        )
    }
    private fun onUpdateStatePlayerPosition(
        dTime: Double,
        journey: MutableStateJourney,
        joystick: EngineInputState.Joystick
    ) {
//        val min = 0.25
        val min = 0.1
//        val min = 0.025
        val joyLeftX = joystick.leftPad.joy.x
        val joyLeftY = joystick.leftPad.joy.y
        when {
            joyLeftX.absoluteValue < min && joyLeftY.absoluteValue < min -> return
            joyLeftX.absoluteValue < min -> {
                val velocityMultiple = sqrt(joyLeftY * joyLeftY)
                onUpdateStatePlayerPosition(
                    dTime = dTime,
                    journey = journey,
                    velocityMultiple = velocityMultiple, dX = 0.0, dY = joyLeftY)
            }
            joyLeftY.absoluteValue < min -> {
                val velocityMultiple = sqrt(joyLeftX * joyLeftX)
                onUpdateStatePlayerPosition(
                    dTime = dTime,
                    journey = journey,
                    velocityMultiple = velocityMultiple, dX = joyLeftX, dY = 0.0)
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
                onUpdateStatePlayerPosition(
                    dTime = dTime,
                    journey = journey,
                    velocityMultiple = velocityMultiple, dX = dX, dY = dY)
            }
        }
    }
    private fun onUpdateStateMainMenu(engineInputState: EngineInputState, engineProperty: EngineProperty) {
        // todo
    }
    private fun onUpdateStateJourney(
        engineInputState: EngineInputState,
        engineProperty: EngineProperty,
        journey: MutableStateJourney
    ) {
        val dTime = engineProperty.timeNow - engineProperty.timeLast
        val joystick = engineInputState.joysticks.firstOrNull { it != null }
        if (joystick == null) {
            onUpdateStatePlayerPosition(
                dTime = dTime,
                journey = journey,
                keyboard = engineInputState.keyboard
            )
        } else {
            onUpdateStatePlayerPosition(
                dTime = dTime,
                journey = journey,
                joystick = joystick
            )
        }
        //
        val directionActual = journey.player.directionActual
        val directionExpected = journey.player.directionExpected
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
                journey.player.directionActual = directionExpected
            } else {
                if (difference < 0) {
                    journey.player.directionActual += d
                } else {
                    journey.player.directionActual -= d
                }
            }
        }
    }
    override fun onUpdateState(engineInputState: EngineInputState, engineProperty: EngineProperty) {
        when (mutableState.common) {
            State.Common.MAIN_MENU -> {
                onUpdateStateMainMenu(engineInputState, engineProperty)
            }
            State.Common.JOURNEY -> {
                val journey = mutableState.journey
                checkNotNull(journey)
                onUpdateStateJourney(engineInputState, engineProperty, journey = journey)
            }
        }
    }

    private val render = Render(
        fullPathFont = fullPathFont,
        pixelsPerUnit = pixelsPerUnit
    )
    override fun onRender(canvas: Canvas, engineInputState: EngineInputState, engineProperty: EngineProperty) {
        render.onRender(
            canvas = canvas,
            engineProperty = engineProperty,
            state = mutableState
        )
    }
}
