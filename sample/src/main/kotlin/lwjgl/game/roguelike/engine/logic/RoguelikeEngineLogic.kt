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
import lwjgl.game.roguelike.engine.util.getTriangleHeightPoint
import lwjgl.game.roguelike.state.State
import lwjgl.game.roguelike.util.TimeUnit
import lwjgl.game.roguelike.util.isLessThan
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
        ),
        StateJourneyTerritoryRegion(
            points = listOf(
                point(x = 10 + 0, y = 10 + 0),
                point(x = 10 + 6, y = 10 + 3),
                point(x = 10 - 1, y = 10 + 10),
                point(x = 10 - 3, y = 10 + 6)
            ),
            color = ColorEntity.CYAN,
            isPassable = false
        ),
        StateJourneyTerritoryRegion(
            points = listOf(
                point(x = 10 - 6, y = 10 + 2),
                point(x = 10 - 4, y = 10 + 6),
                point(x = 10 - 9, y = 10 + 8)
            ),
            color = ColorEntity.GREEN,
            isPassable = false
        )
    )
    private val defaultTerritory: State.Journey.Territory = defaultTerritoryRegions.let { regions ->
        val points = regions.flatMap { it.points }
        StateJourneyTerritory(
            size = size(
                width = points.maxOfOrNull { it.x }!!,
                height = points.maxOfOrNull { it.y }!!
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

    private fun isNewPositionAllowed(
        regions: List<State.Journey.Territory.Region>,
        distanceMin: Double,
        newPosition: Point
    ): Boolean {
        val distanceShortest = allPoints(regions = regions).map { (p3, p4) ->
            calculateDistance(
                pointStart = p3,
                pointFinish = p4,
                point = newPosition
            )
        }.minOrNull() ?: return true
        return !distanceShortest.isLessThan(distanceMin, precision = 12)
    }
    private fun proxySetNewPosition(
        journey: MutableStateJourney,
        distanceMin: Double,
        newPosition: Point
    ) {
        if (isNewPositionAllowed(
                regions = journey.territory.regions.filterNot { it.isPassable },
                newPosition = newPosition,
                distanceMin = distanceMin
            )) {
            journey.player.position.x = newPosition.x
            journey.player.position.y = newPosition.y
        } else {
            println("new position $newPosition is not allowed")
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
        val p1: Point = journey.player.position
        val distanceNewShortest = calculateDistance(
            pointStart = p3,
            pointFinish = p4,
            point = p2
        )
        if (distanceNewShortest >= distanceMin) {
//            println("distanceNewShortest >= distanceMin")
//
//            (s,2) distanceNewShortest
//            (s,m) distanceMin
//
//            3--s----i--4
//               .   .
//            ...m.......
//               . .
//               ..
//               2
//              /
//             /
//            1
//
            proxySetNewPosition(
                journey = journey,
                distanceMin = distanceMin,
                newPosition = p2
            )
            return
        }
//        val distanceIntersection = sqrt((iPoint.x - p1.x) * (iPoint.x - p1.x) + (iPoint.y - p1.y) * (iPoint.y - p1.y))
        val distanceIntersection = calculateDistance(
            pointStart = p1,
            pointFinish = iPoint
        )
//        val distanceActual = sqrt((p2.x - p1.x) * (p2.x - p1.x) + (p2.y - p1.y) * (p2.y - p1.y))
        val distanceActual = calculateDistance(
            pointStart = p1,
            pointFinish = p2
        )
//        println("distanceActual $distanceActual")
//        println("distanceIntersection $distanceIntersection")
        if (distanceActual < distanceIntersection) {
            val distanceShortest = calculateDistance(
                pointStart = p3,
                pointFinish = p4,
                point = p1
            )
            val x = (1 - distanceMin / distanceShortest) * (iPoint.x - p1.x) + p1.x
            val y = (1 - distanceMin / distanceShortest) * (iPoint.y - p1.y) + p1.y
            val r = point(x = x, y = y)
            val newShortest = calculateDistance(
                pointStart = p3,
                pointFinish = p4,
                point = r
            )
//
//            (s,2) distanceNewShortest
//            (s,m) distanceMin
//            (1,i) distanceIntersection
//            (1,2) distanceActual
//            (1,d) distanceShortest
//            (r,n) newShortest
//
//         3--d--n--s--i--4
//            .  :  ! .
//            .  :  !.
//            .  :  2
//            .  : /!
//            .  :/ !
//         ......r..m......
//            . /
//            ./
//            1
//
            if (newShortest.isLessThan(distanceMin, precision = 12)) {
                println("newShortest >= distanceMin")
                return
            }
//            journey.player.position.x = x
//            journey.player.position.y = y
            val n = getTriangleHeightPoint(
                baseStart = p3,
                baseFinish = p4,
                point = r
            )
//            println("n: $n")
            val s = getTriangleHeightPoint(
                baseStart = p3,
                baseFinish = p4,
                point = p2
            )
//            println("s: $s")
            val m = point(x = r.x + s.x - n.x, y = r.y + s.y - n.y)
//            println("m: $m")
            proxySetNewPosition(
                journey = journey,
                distanceMin = distanceMin,
                newPosition = m
            )
        } else {
            println("distanceActual >= distanceIntersection")
//
//            (s,2) distanceNewShortest
//            (s,m) distanceMin
//            (1,i) distanceIntersection
//            (1,2) distanceActual
//
//                     2
//                    /'
//                   / '
//         3--d--n--i--s--4
//            .  : /   !
//            .  :/    !
//         ......r.....m...
//            . /
//            ./
//            1
//
//            todo
        }
    }
    private fun allPoints(regions: List<State.Journey.Territory.Region>): List<Pair<Point, Point>> {
        return regions.flatMap {
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
    }
    private fun onUpdateStatePlayerPosition(
        journey: MutableStateJourney,
        newPosition: Point
    ) {
        val distanceMin = sqrt(playerSize.width * playerSize.width + playerSize.height * playerSize.height) / 2
//        val distanceMin = 50.0
        val territory = journey.territory
        val p1: Point = journey.player.position
        val p2: Point = newPosition
        val points = allPoints(regions = territory.regions.filter { !it.isPassable })
//        val pointsShortest = points.filter { (p3, p4) ->
//            val distanceShortest = calculateDistance(
//                pointStart = p3,
//                pointFinish = p4,
//                point = p2
//            )
//            distanceShortest < distanceMin
//        }
        val group = points.groupBy { (p3, p4) ->
            calculateDistance(
                pointStart = p3,
                pointFinish = p4,
                point = p2
            )
        }
        val distanceShortest = group.keys.minByOrNull { it }!!
        val pointsShortest = if (distanceShortest < distanceMin) {
            group[distanceShortest]!!
        } else {
            emptyList()
        }
        if (pointsShortest.isEmpty()) {
//               2
//              /
//             /
//            1
            proxySetNewPosition(
                journey = journey,
                distanceMin = distanceMin,
                newPosition = newPosition
            )
        } else {
            val results = pointsShortest.mapNotNull { (p3, p4) ->
                getIntersectionPointOrNull(
                    p1 = p1,
                    p2 = p2,
                    p3 = p3,
                    p4 = p4
                )?.let {
                    Triple(p3, p4, it)
                }
            }.filter { (_, _, intersectionPoint) ->
                val distanceIntersection = calculateDistance(
                    pointStart = p1,
                    pointFinish = intersectionPoint
                )
                val distanceActual = calculateDistance(
                    pointStart = p1,
                    pointFinish = p2
                )
                val distanceResult = calculateDistance(
                    pointStart = p2,
                    pointFinish = intersectionPoint
                )
                distanceResult < distanceIntersection && distanceActual < distanceIntersection
            }
            if (results.isEmpty()) {
                println("results is empty")
                // todo
            } else if (results.size == 1) {
                val (p3, p4, intersectionPoint) = results.firstOrNull()!!
                onUpdateStatePlayerPositionByIntersection(
                    journey = journey,
                    p2 = p2,
                    p3 = p3,
                    p4 = p4,
                    iPoint = intersectionPoint,
                    distanceMin = distanceMin
                )
            } else if (results.size == 2) {
                println("results.size == 2")
                val (p3, p4, intersectionPoint) = results.maxByOrNull { (_, _, intersectionPoint) ->
                    calculateDistance(
                            pointStart = p1,
                            pointFinish = intersectionPoint
                    )
                }!!
                onUpdateStatePlayerPositionByIntersection(
                    journey = journey,
                    p2 = p2,
                    p3 = p3,
                    p4 = p4,
                    iPoint = intersectionPoint,
                    distanceMin = distanceMin
                )
            } else {
                println("results.size > 2")
                println("results $results")
                // todo
            }
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
        player.directionExpected = calculateAngle(
            oldX = player.position.x,
            oldY = player.position.y,
            newX = player.position.x + dX,
            newY = player.position.y + dY
        )
        val newPosition = getNewPositionByDirection(
            oldPosition = player.position,
            units = player.velocity * velocityMultiple * dTime * pixelsPerUnit,
            direction = player.directionExpected
        )
        onUpdateStatePlayerPosition(
            journey = journey,
            newPosition = newPosition
        )
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
        val pad = joystick.pads[EngineInputState.Joystick.Mapping.Side.LEFT] ?: TODO()
        val joyLeftX = pad.joy.x
        val joyLeftY = pad.joy.y
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
