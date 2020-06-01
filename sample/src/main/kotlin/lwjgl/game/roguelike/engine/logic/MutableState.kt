package lwjgl.game.roguelike.engine.logic

import lwjgl.game.roguelike.state.State
import lwjgl.game.roguelike.util.TimeUnit
import lwjgl.wrapper.entity.Color
import lwjgl.wrapper.entity.ColorEntity
import lwjgl.wrapper.entity.Point
import lwjgl.wrapper.entity.Size
import lwjgl.wrapper.entity.point
import lwjgl.wrapper.entity.size

class MutableStateMainMenu(
    override var selectedMenuItem: State.MainMenu.Item
) : State.MainMenu

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

class MutableState(
    defaultCommon: State.Common
) : State {
    override var common: State.Common = defaultCommon
    private lateinit var shouldEngineStopUnit: Unit
    override val shouldEngineStop: Boolean get() = ::shouldEngineStopUnit.isInitialized
    fun engineStop() {
        shouldEngineStopUnit = Unit
    }

    override val mainMenu: MutableStateMainMenu = MutableStateMainMenu(
        selectedMenuItem = State.MainMenu.Item.START_NEW_GAME
    )
    override var journey: MutableStateJourney? = null
}
