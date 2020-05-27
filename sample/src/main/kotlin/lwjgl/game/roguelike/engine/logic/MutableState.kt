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
        val dX = String.format("%.1f", x)
        val dY = String.format("%.1f", y)
        return "{x:$dX,y:$dY}"
    }
}

class MutableStateJourneyPlayer(
    override val position: MutablePoint
) : State.Journey.Player {
//    override val velocity: Double = 1.0
    override val velocity: Double = 5.0 / TimeUnit.NANO_IN_SECOND
    override var direction: Double = 0.0
}

class StateJourneyTerritoryRegion(
    override val position: Point,
    override val size: Size,
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
        position = point(x = 3, y = 3),
        size = size(width = 5, height = 3),
        color = ColorEntity.GREEN,
        isPassable = false
    ),
    StateJourneyTerritoryRegion(
        position = point(x = 8, y = 3),
        size = size(width = 3, height = 6),
        color = ColorEntity.GREEN,
        isPassable = false
    ),
    StateJourneyTerritoryRegion(
        position = point(x = 3, y = 8),
        size = size(width = 3, height = 3),
        color = ColorEntity.RED,
        isPassable = false
    )
).let { regions ->
    StateJourneyTerritory(
        size = size(
            width = regions.maxBy { it.position.x }!!.let { it.position.x + it.size.width },
            height = regions.maxBy { it.position.y }!!.let { it.position.y + it.size.height }
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
