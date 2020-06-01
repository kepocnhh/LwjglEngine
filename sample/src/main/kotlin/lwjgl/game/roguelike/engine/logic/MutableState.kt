package lwjgl.game.roguelike.engine.logic

import lwjgl.game.roguelike.state.State
import lwjgl.game.roguelike.util.TimeUnit
import lwjgl.wrapper.entity.Color
import lwjgl.wrapper.entity.ColorEntity
import lwjgl.wrapper.entity.Point
import lwjgl.wrapper.entity.Size
import lwjgl.wrapper.entity.point
import lwjgl.wrapper.entity.size

class MutablePoint(
    override var x: Double,
    override var y: Double
) : Point {
    operator fun component1(): Double = x
    operator fun component2(): Double = y

    override fun toString(): String {
        val fX = String.format("%.1f", x)
        val fY = String.format("%.1f", y)
        return "{x:$fX,y:$fY}"
    }
}

class MutableStateJourneyPlayer(
    override val position: MutablePoint
) : State.Journey.Player {
//    override val velocity: Double = 1.0
    override val velocity: Double = 5.0 / TimeUnit.NANO_IN_SECOND
    override var directionActual: Double = 0.0
    override var directionExpected: Double = 0.0
}

class StateJourneyTerritoryRegion(
    override val points: List<Point>,
    override val color: Color,
    override val isPassable: Boolean
) : State.Journey.Territory.Region

class StateJourneyTerritory(
    override val size: Size,
    override val regions: List<State.Journey.Territory.Region>
) : State.Journey.Territory

class MutableStateJourney(
    override var territory: State.Journey.Territory
) : State.Journey {
    override val player: MutableStateJourneyPlayer = MutableStateJourneyPlayer(
        position = MutablePoint(x = 0.0, y = 0.0)
    )
}

private val defaultTerritory: State.Journey.Territory = listOf(
    StateJourneyTerritoryRegion(
        points = listOf(
            point(x = 3 + 0, y = 3 + 0),
            point(x = 3 + 8, y = 3 + 0),
            point(x = 3 + 8, y = 3 + 6),
            point(x = 3 + 5, y = 3 + 6),
//            point(x = 3 + 5, y = 3 + 3),
            point(x = 3 + 0, y = 3 + 3)
        ),
        color = ColorEntity.GREEN,
        isPassable = false
    ),
    StateJourneyTerritoryRegion(
        points = listOf(
            point(x = 3 + 0, y = 9 + 0),
            point(x = 3 + 3, y = 9 + 0),
            point(x = 3 + 3, y = 9 + 3),
            point(x = 3 + 0, y = 9 + 3)
        ),
        color = ColorEntity.YELLOW,
        isPassable = true
    )
).let { regions ->
    val points = regions.flatMap { it.points }
    StateJourneyTerritory(
        size = size(
            width = points.maxBy { it.x }!!.x,
            height = points.maxBy { it.y }!!.y
        ),
        regions = regions
    )
}

class MutableState(
    override var common: State.Common
) : State {
    private lateinit var shouldEngineStopUnit: Unit
    override val shouldEngineStop: Boolean get() = ::shouldEngineStopUnit.isInitialized
    fun engineStop() {
        shouldEngineStopUnit = Unit
    }

    override val journey: MutableStateJourney = MutableStateJourney(defaultTerritory)
}
