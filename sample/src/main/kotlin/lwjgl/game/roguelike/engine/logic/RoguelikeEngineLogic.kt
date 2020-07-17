package lwjgl.game.roguelike.engine.logic

import lwjgl.engine.common.EngineLogic
import lwjgl.engine.common.EngineProperty
import lwjgl.engine.common.input.EngineInputCallback
import lwjgl.engine.common.input.EngineInputState
import lwjgl.engine.common.input.FunctionKey
import lwjgl.engine.common.input.PrintableKey
import lwjgl.game.roguelike.engine.render.Render
import lwjgl.game.roguelike.engine.util.calculateAngle
import lwjgl.game.roguelike.engine.util.calculateDistance
import lwjgl.game.roguelike.engine.util.getIntersectionPointOrNull
import lwjgl.game.roguelike.engine.util.getNewPositionByDirection
import lwjgl.game.roguelike.state.State
import lwjgl.game.roguelike.util.TimeUnit
import lwjgl.wrapper.canvas.Canvas
import lwjgl.wrapper.entity.*
import lwjgl.wrapper.util.glfw.key.KeyStatus
import lwjgl.wrapper.util.resource.ResourceProvider
import kotlin.math.*

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
        ),
        StateJourneyTerritoryRegion(
            points = rect(leftTop = point(x = 3, y = 3), rightBottom = point(x = 7, y = 7)),
            color = ColorEntity.YELLOW,
            isPassable = false
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

    private val playerSize = size(width = 1 * pixelsPerUnit, height = 1 * pixelsPerUnit) // todo
//    private fun closerThan(
//        points: List<Point>,
//        x: Double,
//        y: Double,
//        distanceMin: Double
//    ): Boolean {
//        var i = 0
//        val size = points.size
//        while (true) {
//            val next: Int = when (i) {
//                size -> return false
//                size - 1 -> 0
//                else -> i + 1
//            }
//            val distanceActual = calculateDistance(
//                pointStart = points[i],
//                pointFinish = points[next],
//                x = x,
//                y = y
//            )
//            if (distanceActual < distanceMin) {
//                return true
//            }
//            i++
//        }
//    }

    private fun getIntersectionPointOrNullOld2(
        p1: Point,
        p2: Point,
        p3: Point,
        p4: Point
    ): Point? {
//        if (max(p1.x, p2.x) < p3.x && max(p1.x, p2.x) < p4.x) {
//            println("catch max < x")
//            return null
//        }
//        if (min(p1.x, p2.x) > p3.x && min(p1.x, p2.x) > p4.x) {
//            println("catch max > x")
//            return null
//        }
//        if (max(p1.y, p2.y) < p3.y && max(p1.y, p2.y) < p4.y) {
//            println("catch max < y")
//            return null
//        }
//        if (min(p1.y, p2.y) > p3.y && min(p1.y, p2.y) > p4.y) {
//            println("catch max > y")
//            return null
//        }
        val uBottom = (p4.y - p3.y) * (p2.x - p1.x) - (p4.x - p3.x) * (p2.y - p1.y)
        if (uBottom == 0.0) {
//            println("uBottom == 0")
            return null
        }
        val uTopA = (p4.x - p3.x) * (p1.y - p3.y) - (p4.y - p3.y) * (p1.x - p3.x)
        val uA = uTopA / uBottom
//        if (uA !in 0.0..1.0) {
//            println("uA: $uA !in 0..1")
//            return null
//        }
//        val uTopB = (p2.x - p1.x) * (p1.y - p3.y) - (p2.y - p1.y) * (p1.x - p3.x)
//        val uB = uTopB / uBottom
//        if (uB !in 0.0..1.0) {
//            println("uB: $uB !in 0..1")
//            return null
//        }
//        println("uA: $uA")
//        println("uA: $uA, uB: $uB")
        val result = point(
            x = p1.x + uA * (p2.x - p1.x),
            y = p1.y + uA * (p2.y - p1.y)
        )
//        println("return: $result")
        return result
    }
    private fun getIntersectionPointOrNullTest(
        p1: Point,
        p2: Point,
        p3: Point,
        p4: Point
    ): Point? {
        if (max(p1.x, p2.x) < p3.x && max(p1.x, p2.x) < p4.x ||
            min(p1.x, p2.x) > p3.x && min(p1.x, p2.x) > p4.x ||
            max(p1.y, p2.y) < p3.y && max(p1.y, p2.y) < p4.y ||
            min(p1.y, p2.y) > p3.y && min(p1.y, p2.y) > p4.y) return null
        val uBottom = (p4.y - p3.y) * (p2.x - p1.x) - (p4.x - p3.x) * (p2.y - p1.y)
        if (uBottom == 0.0) return null
        val uTopA = (p4.x - p3.x) * (p1.y - p3.y) - (p4.y - p3.y) * (p1.x - p3.x)
        val uA = uTopA / uBottom
        if (uA !in 0.0..1.0) return null
        val uTopB = (p2.x - p1.x) * (p1.y - p3.y) - (p2.y - p1.y) * (p1.x - p3.x)
        val uB = uTopB / uBottom
        if (uB !in 0.0..1.0) return null
        return point(
            x = p1.x + uA * (p2.x - p1.x),
            y = p1.y + uA * (p2.y - p1.y)
        )
    }
    private fun onUpdateStatePlayerPositionByShortest(
        journey: MutableStateJourney,
        p2: Point,
        p3: Point,
        p4: Point,
        distanceMin: Double
    ) {
        println("by -> Shortest")
        val distanceShortest = calculateDistance(
            pointStart = p3,
            pointFinish = p4,
            point = p2
        )
//        println("s: $distanceShortest")
        if (distanceShortest < distanceMin) {
            println("dM: $distanceMin dS: $distanceShortest")
//                    val x = (distanceShortest * iPoint.x - distanceMin * (iPoint.x - newX)) / distanceShortest
//                    val y = (distanceShortest * iPoint.y - distanceMin * (iPoint.y - newY)) / distanceShortest
//                    journey.player.position.x = x
//                    journey.player.position.y = y
//                    journey.player.position.x = newX
//                    journey.player.position.y = newY
            // todo
        } else {
            journey.player.position.x = p2.x
            journey.player.position.y = p2.y
            return
        }
    }
    private fun onUpdateStatePlayerPositionByIntersection(
        journey: MutableStateJourney,
        p2: Point,
        p3: Point,
        p4: Point,
        iPoint: Point,
        distanceMin: Double
    ) {
//        println("by -> Intersection")
        val p1: Point = journey.player.position
        val distanceNewShortest = calculateDistance(
            pointStart = p3,
            pointFinish = p4,
            point = p2
        )
        println("distance new shortest: ${String.format("%.2f", distanceNewShortest)}")
//        println("dS: $distanceShortest")
        if (distanceNewShortest >= distanceMin) {
            println("d new S: ${String.format("%.2f", distanceNewShortest)} dM: ${String.format("%.2f", distanceMin)}")
            journey.player.position.x = p2.x
            journey.player.position.y = p2.y
            return
        }
        val distanceIntersection = sqrt((iPoint.x - p1.x) * (iPoint.x - p1.x) + (iPoint.y - p1.y) * (iPoint.y - p1.y))
        println("distance Intersection: ${String.format("%.2f", distanceIntersection)}")
        val distanceActual = sqrt((p2.x - p1.x) * (p2.x - p1.x) + (p2.y - p1.y) * (p2.y - p1.y))
        println("distance Actual: ${String.format("%.2f", distanceActual)}")
        if (distanceActual < distanceIntersection) {
//            val x = (distanceShortest * iPoint.x - distanceMin * (iPoint.x - p2.x)) / distanceShortest
//            val y = (distanceShortest * iPoint.y - distanceMin * (iPoint.y - p2.y)) / distanceShortest
            println("p1: $p1")
            println("p2: $p2")
            println("p3: $p3")
            println("p4: $p4")
            println("pI: $iPoint")
            println("dM: $distanceMin")
            val distanceShortest = calculateDistance(
                pointStart = p3,
                pointFinish = p4,
                point = p1
            )
            println("dS: $distanceShortest")
            println("dI: $distanceIntersection")
            val x = (1 - distanceMin / distanceShortest) * (iPoint.x - p1.x) + p1.x
            val y = (1 - distanceMin / distanceShortest) * (iPoint.y - p1.y) + p1.y
            val newShortest = calculateDistance(
                pointStart = p3,
                pointFinish = p4,
                point = point(x = x, y = y)
            )
            println("new x: $x y: $y")
            println("new shortest: ${String.format("%.2f", newShortest)}")
            if (newShortest < distanceMin) {
                println("nS: $newShortest dM: $distanceMin")
                println("nS: ${String.format("%.4f", newShortest)} dM: ${String.format("%.4f", distanceMin)}")
                return
//                if (newShortest * 1_000_000 < distanceMin.times(1_000_000).toInt()) {
//                    println("nS: ${String.format("%.4f", newShortest)} dM: ${String.format("%.4f", distanceMin)}")
//                    return
//                }
            }
            journey.player.position.x = x
            journey.player.position.y = y
        } else {
            println("dA: ${String.format("%.2f", distanceActual)} dI: ${String.format("%.2f", distanceIntersection)}")
//                journey.player.position.x = iPoint.x
//                journey.player.position.y = iPoint.y
            // todo
        }
    }
    private fun onUpdateStatePlayerPosition(
        journey: MutableStateJourney,
        newPosition: Point
    ) {
        println("\n- - " + System.currentTimeMillis().toString())
        val distanceMin = sqrt(playerSize.width * playerSize.width + playerSize.height * playerSize.height) / 2
//        println("distance min: ${String.format("%.2f", distanceMin)}")
//        val distanceMin = playerSize.width / 2
//        println("dM: $distanceMin")
        val territory = journey.territory
        val p1: Point = journey.player.position
        val p2: Point = newPosition
        val points = territory.regions.filter { !it.isPassable }.flatMap {
            val result = mutableListOf<Pair<Point, Point>>()
            var i = 0
            val size = it.points.size
            while (true) {
                if (i == size) break
                val next: Int = when (i) {
                    size - 1 -> 0
                    else -> i + 1
                }
                result.add(it.points[i] to it.points[next])
                i++
            }
            result
        }
//        println(points)
        val pointsShortest = points.filter { (p3, p4) ->
            val distanceShortest = calculateDistance(
                pointStart = p3,
                pointFinish = p4,
                point = p2
            )
            distanceShortest < distanceMin
        }
//        println(pointsShortest)
        if (pointsShortest.isEmpty()) {
            journey.player.position.x = newPosition.x
            journey.player.position.y = newPosition.y
        } else {
            val (p3, p4) = pointsShortest.minBy { (p3, p4) ->
                calculateDistance(
                    pointStart = p3,
                    pointFinish = p4,
                    point = p2
                )
            }!!
            val intersectionPoint = getIntersectionPointOrNull(
                p1 = p1,
                p2 = p2,
                p3 = p3,
                p4 = p4
            )
            if (intersectionPoint == null) {
                println("by -> Intersection null")
                // todo
            } else {
                onUpdateStatePlayerPositionByIntersection(
                    journey = journey,
                    p2 = p2,
                    p3 = p3,
                    p4 = p4,
                    iPoint = intersectionPoint,
                    distanceMin = distanceMin
                )
            }
            // todo
        }
        return
        val intersection = points.mapNotNull { (p3, p4) ->
            getIntersectionPointOrNull(
                p1 = p1,
                p2 = p2,
                p3 = p3,
                p4 = p4
            )?.let {
                Triple(p3, p4, it)
            }
        }.minBy { (_, _, it) ->
            sqrt((it.x - p1.x) * (it.x - p1.x) + (it.y - p1.y) * (it.y - p1.y))
        }
        if (intersection == null) {
            val pointShortest = points.minBy { (p3, p4) ->
                calculateDistance(
                    pointStart = p3,
                    pointFinish = p4,
                    point = p2
                )
            }
            if (pointShortest == null) {
                println("by -> Intersection/Shortest null")
                journey.player.position.x = newPosition.x
                journey.player.position.y = newPosition.y
                return
            }
            onUpdateStatePlayerPositionByShortest(
                journey = journey,
                p2 = p2,
                p3 = pointShortest.first,
                p4 = pointShortest.second,
                distanceMin = distanceMin
            )
        } else {
            onUpdateStatePlayerPositionByIntersection(
                journey = journey,
                p2 = p2,
                p3 = intersection.first,
                p4 = intersection.second,
                iPoint = intersection.third,
                distanceMin = distanceMin
            )
        }
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
        val newPosition = getNewPositionByDirection(
            oldPosition = player.position,
            units = player.velocity * velocityMultiple * dTime * pixelsPerUnit,
            direction = player.directionExpected
        )
        onUpdateStatePlayerPosition(
            journey = journey,
            newPosition = newPosition
        )
        return // todo
//        val distanceMin = sqrt(playerSize.width * playerSize.width + playerSize.height * playerSize.height) / 2
//        val territory = journey.territory
//        val isMoveX = newX in distanceMin..(territory.size.width - distanceMin) && !territory.regions.any {
//            !it.isPassable && closerThan(
//                points = it.points,
//                x = newX,
//                y = oldY,
//                distanceMin = distanceMin
//            )
//        }
//        if (isMoveX) {
//            player.position.x = newX
//        }
//
//        val inTerritoryY = newY in distanceMin..(territory.size.height - distanceMin)
//        val isMoveY = inTerritoryY && !territory.regions.any {
//            !it.isPassable && closerThan(
//                points = it.points,
//                x = oldX,
//                y = newY,
//                distanceMin = distanceMin
//            )
//        }
//        if (isMoveY) {
//            player.position.y = newY
//        }
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
