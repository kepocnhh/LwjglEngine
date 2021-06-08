package lwjgl.game.roguelike.engine.logic

import kotlin.math.absoluteValue
import lwjgl.engine.common.EngineLogic
import lwjgl.engine.common.EngineProperty
import lwjgl.engine.common.input.EngineInputCallback
import lwjgl.engine.common.input.EngineInputState
import lwjgl.engine.common.input.FunctionKey
import lwjgl.engine.common.input.PrintableKey
import lwjgl.game.roguelike.engine.render.Render
import lwjgl.game.roguelike.engine.util.allLines
import lwjgl.game.roguelike.engine.util.calculateAngle
import lwjgl.game.roguelike.engine.util.calculateDistance
import lwjgl.game.roguelike.engine.util.getIntersectionPointOrNull
import lwjgl.game.roguelike.engine.util.getNewPositionByDirection
import lwjgl.game.roguelike.engine.util.getTriangleHeightPoint
import lwjgl.game.roguelike.state.State
import lwjgl.game.roguelike.util.TimeUnit
import lwjgl.game.roguelike.util.isLessThan
import lwjgl.wrapper.canvas.Canvas
import lwjgl.wrapper.util.glfw.key.KeyStatus
import lwjgl.wrapper.util.resource.ResourceProvider
import lwjgl.game.roguelike.engine.util.isNewPositionAllowed
import lwjgl.wrapper.entity.ColorEntity
import lwjgl.wrapper.entity.Line
import lwjgl.wrapper.entity.Point
import lwjgl.wrapper.entity.Size
import lwjgl.wrapper.entity.point
import lwjgl.wrapper.entity.size

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
/*
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
*/
    )
    private val defaultTerritoryStorages = listOf(
        MutableStateJourneyTerritoryStorage(
            position = point(x = 5, y = 7),
            size = size(width = 1, height = 1),
            direction = 14.37,
            color = ColorEntity.GREEN,
            items = mutableSetOf(
                StateJourneyItem(title = "item foo")
            )
        ),
        MutableStateJourneyTerritoryStorage(
            position = point(x = 13, y = 15),
            size = size(width = 1.5, height = 1.0),
            direction = -73.41,
            color = ColorEntity.YELLOW,
            items = mutableSetOf()
        )
    )
    private val defaultTerritory: State.Journey.Territory = Unit.let {
        val points = defaultTerritoryRegions.flatMap { it.points } +
                defaultTerritoryStorages.map { it.position }
        MutableStateJourneyTerritory(
            size = size(
                width = points.maxOfOrNull { it.x }!!,
                height = points.maxOfOrNull { it.y }!!
            ),
            regions = defaultTerritoryRegions,
            storages = defaultTerritoryStorages
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
        return MutableStateJourneyTerritory(
            size = size.fromUnitsToPixels(pixelsPerUnit = pixelsPerUnit),
            regions = regions.map { region ->
                StateJourneyTerritoryRegion(
                    points = region.points.map {
                        it.fromUnitsToPixels(pixelsPerUnit = pixelsPerUnit)
                    },
                    color = region.color,
                    isPassable = region.isPassable
                )
            },
            storages = storages.map {
                MutableStateJourneyTerritoryStorage(
                    position = it.position.fromUnitsToPixels(pixelsPerUnit = pixelsPerUnit),
                    size = it.size.fromUnitsToPixels(pixelsPerUnit = pixelsPerUnit),
                    direction = it.direction,
                    color = it.color,
                    items = it.items.toMutableSet()
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
                                val distanceMin = kotlin.math.sqrt(playerSize.width * playerSize.width + playerSize.height * playerSize.height) / 2
                                val direction = 135.0
                                val velocity = 5.0 / TimeUnit.NANO_IN_SECOND
                                val journey = MutableStateJourney(
                                    territory = territory,
                                    player = MutableStateJourneyPlayer(
                                        position = MutablePoint(x = distanceMin, y = distanceMin),
                                        velocity = velocity,
                                        directionActual = direction,
                                        directionExpected = direction,
                                        state = State.Journey.PlayerState.MoveState,
                                        interactions = mutableSetOf(),
                                        items = mutableSetOf()
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
    private fun onSelectItem(state: MutableExchangeStorageState, player: MutableStateJourneyPlayer) {
        val item = state.focusedItem ?: return
        state.storage.items -= item
        player.items += item
        state.focusedItem = state.storage.items.firstOrNull()
    }
    private fun onJourneyInputCallback(key: PrintableKey, status: KeyStatus) {
        val journey: MutableStateJourney = requireNotNull(mutableState.journey)
        when (val state = journey.player.state) {
            is MutableExchangeStorageState -> {
                when (key) {
                    PrintableKey.F -> {
                        when (status) {
                            KeyStatus.RELEASE -> {
                                onSelectItem(state = state, player = journey.player)
                            }
                        }
                    }
                }
            }
            State.Journey.PlayerState.MoveState -> {
                val interactions = journey.player.interactions
                if (interactions.isNotEmpty()) {
                    if (interactions.size != 1) TODO()
                    val interaction = interactions.firstOrNull()!!
                    when (key) {
                        PrintableKey.F -> {
                            when (status) {
                                KeyStatus.RELEASE -> {
                                    when (interaction) {
                                        is State.Journey.Player.InteractionType.StorageType -> {
                                            journey.player.state = MutableExchangeStorageState(
                                                storage = interaction.storage.toMutable(),
                                                focusedItem = interaction.storage.items.firstOrNull(),
                                                focusedStorage = true
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    private fun onJourneyInputCallback(key: FunctionKey, status: KeyStatus) {
        val journey: MutableStateJourney = requireNotNull(mutableState.journey)
        when (val state = journey.player.state) {
            is MutableExchangeStorageState -> {
                when (key) {
                    FunctionKey.ESCAPE -> {
                        when (status) {
                            KeyStatus.RELEASE -> {
                                journey.player.state = State.Journey.PlayerState.MoveState
                            }
                        }
                    }
                    FunctionKey.ENTER -> {
                        when (status) {
                            KeyStatus.RELEASE -> {
                                onSelectItem(state = state, player = journey.player)
                            }
                        }
                    }
                    FunctionKey.TAB -> {
                        when (status) {
                            KeyStatus.RELEASE -> {
                                state.focusedStorage = !state.focusedStorage
                            }
                        }
                    }
                }
            }
            State.Journey.PlayerState.MoveState -> {
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
    override val engineInputCallback = object : EngineInputCallback {
        override fun onPrintableKey(key: PrintableKey, status: KeyStatus) {
            when (mutableState.common) {
                State.Common.MAIN_MENU -> {
                    onMainMenuInputCallback(key, status)
                }
                State.Common.JOURNEY -> {
                    onJourneyInputCallback(key, status)
                }
            }
        }

        override fun onFunctionKey(key: FunctionKey, status: KeyStatus) {
            when (mutableState.common) {
                State.Common.MAIN_MENU -> {
                    onMainMenuInputCallback(key, status)
                }
                State.Common.JOURNEY -> {
                    onJourneyInputCallback(key, status)
                }
            }
        }
    }

    private const val pixelsPerUnit = 25.0 // todo

    override fun onPreLoop() {
        // todo
    }

    private val playerSize = size(width = 1 * pixelsPerUnit, height = 1 * pixelsPerUnit) // todo

    private fun proxySetNewPosition(
        journey: MutableStateJourney,
        distanceMin: Double,
        newPosition: Point
    ) {
        val isNewPositionAllowed = isNewPositionAllowed(
            lines = journey.territory.allLines(),
            newPosition = newPosition,
            distanceMin = distanceMin
        )
        if (isNewPositionAllowed) {
            journey.player.position.x = newPosition.x
            journey.player.position.y = newPosition.y
        } else {
            println("new position $newPosition is not allowed")
        }
    }
    private fun onUpdateStatePlayerPositionByIntersection(
        journey: MutableStateJourney,
        p2: Point,
        line: Line,
        iPoint: Point,
        distanceMin: Double
    ) {
        val p1: Point = journey.player.position
        val distanceNewShortest = calculateDistance(
            line = line,
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
                line = line,
                point = p1
            )
            val x = (1 - distanceMin / distanceShortest) * (iPoint.x - p1.x) + p1.x
            val y = (1 - distanceMin / distanceShortest) * (iPoint.y - p1.y) + p1.y
            val r = point(x = x, y = y)
            val newShortest = calculateDistance(
                line = line,
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
//                println("newShortest >= distanceMin")
                return
            }
//            journey.player.position.x = x
//            journey.player.position.y = y
            val n = getTriangleHeightPoint(
                line = line,
                point = r
            )
//            println("n: $n")
            val s = getTriangleHeightPoint(
                line = line,
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
//            println("distanceActual >= distanceIntersection")
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
    private fun onUpdateStatePlayerPosition(
        journey: MutableStateJourney,
        newPosition: Point
    ) {
        val distanceMin = kotlin.math.sqrt(playerSize.width * playerSize.width + playerSize.height * playerSize.height) / 2
//        val distanceMin = 50.0
        val territory = journey.territory
        val p1: Point = journey.player.position
        val p2: Point = newPosition
        val lines = territory.allLines()
//        val pointsShortest = points.filter { (p3, p4) ->
//            val distanceShortest = calculateDistance(
//                pointStart = p3,
//                pointFinish = p4,
//                point = p2
//            )
//            distanceShortest < distanceMin
//        }
        val group = lines.groupBy {
            calculateDistance(
                line = it,
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
            val results = pointsShortest.mapNotNull { line ->
                getIntersectionPointOrNull(
                    p1 = p1,
                    p2 = p2,
                    line = line,
                )?.let {
                    line to it
                }
            }.filter { (_, intersectionPoint) ->
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
//                println("results is empty")
                // todo
            } else if (results.size == 1) {
                val (line, intersectionPoint) = results.firstOrNull()!!
                onUpdateStatePlayerPositionByIntersection(
                    journey = journey,
                    p2 = p2,
                    line = line,
                    iPoint = intersectionPoint,
                    distanceMin = distanceMin
                )
            } else if (results.size == 2) {
//                println("results.size == 2")
                val (line, intersectionPoint) = results.maxByOrNull { (_, intersectionPoint) ->
                    calculateDistance(
                        pointStart = p1,
                        pointFinish = intersectionPoint
                    )
                }!!
                onUpdateStatePlayerPositionByIntersection(
                    journey = journey,
                    p2 = p2,
                    line = line,
                    iPoint = intersectionPoint,
                    distanceMin = distanceMin
                )
            } else {
//                println("results.size > 2")
//                println("results $results")
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
                val velocityMultiple = kotlin.math.sqrt(joyLeftY * joyLeftY)
                onUpdateStatePlayerPosition(
                    dTime = dTime,
                    journey = journey,
                    velocityMultiple = velocityMultiple, dX = 0.0, dY = joyLeftY)
            }
            joyLeftY.absoluteValue < min -> {
                val velocityMultiple = kotlin.math.sqrt(joyLeftX * joyLeftX)
                onUpdateStatePlayerPosition(
                    dTime = dTime,
                    journey = journey,
                    velocityMultiple = velocityMultiple, dX = joyLeftX, dY = 0.0)
            }
            else -> {
                val dX: Double
                val dY: Double
                when {
                    kotlin.math.sqrt(joyLeftX * joyLeftX + joyLeftY * joyLeftY) < 1 -> {
                        dX = joyLeftX
                        dY = joyLeftY
                    }
                    joyLeftX.absoluteValue < joyLeftY.absoluteValue -> {
                        dX = joyLeftX
                        dY = kotlin.math.sqrt(1 - dX * dX) * if (joyLeftY < 0) -1 else 1
                    }
                    else -> {
                        dY = joyLeftY
                        dX = kotlin.math.sqrt(1 - dY * dY) * if (joyLeftX < 0) -1 else 1
                    }
                }
                val velocityMultiple = kotlin.math.sqrt(dX * dX + dY * dY)
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
    private fun onStoragesNearby(player: MutableStateJourneyPlayer, storages: List<State.Journey.Territory.Storage>) {
        // todo
        if (storages.isEmpty()) {
            return
        }
//        val storage = storages.minByOrNull {
//            calculateDistance(player.position, it.position)
//        }
//        checkNotNull(storage)
        player.interactions.addAll(storages.map { State.Journey.Player.InteractionType.StorageType(it) })
    }
    private fun onUpdateStateJourney(
        engineInputState: EngineInputState,
        engineProperty: EngineProperty,
        journey: MutableStateJourney
    ) {
        val dTime = engineProperty.timeNow - engineProperty.timeLast
        //
        when (journey.player.state) {
            State.Journey.PlayerState.MoveState -> {
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
                journey.player.interactions.clear()
                val distanceMax = playerSize.width * 2
                val storages = journey.territory.storages.filter {
                    val d = calculateDistance(journey.player.position, it.position)
                    d < distanceMax
                }
                onStoragesNearby(player = journey.player, storages = storages)
            }
            is State.Journey.PlayerState.ExchangeStorageState -> {
                // todo
            }
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
