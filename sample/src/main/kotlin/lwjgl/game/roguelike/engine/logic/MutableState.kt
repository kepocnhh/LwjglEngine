package lwjgl.game.roguelike.engine.logic

import lwjgl.game.roguelike.engine.entity.Dummy
import lwjgl.game.roguelike.engine.entity.Intelligence
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

    override fun hashCode(): Int {
        return (x + y * 13).toInt()
    }

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is Point -> x == other.x && y == other.y
            else -> false
        }
    }
}

class StateJourneyItem(override val title: String) : State.Journey.Item

class MutableExchangeStorageState(
    override val storage: MutableStateJourneyTerritoryStorage,
    override var focusedItem: State.Journey.Item?,
    override var focusedStorage: Boolean
) : State.Journey.PlayerState.ExchangeStorageState

class MutableStateJourneyPlayer(
    override val position: MutablePoint,
    override val velocity: Double,
    override var directionActual: Double,
    override var directionExpected: Double,
    override var state: State.Journey.PlayerState,
    override val interactions: MutableSet<State.Journey.Player.InteractionType>,
    override val items: MutableList<State.Journey.Item>
) : State.Journey.Player

class MutableStorageType(
    override val storage: MutableStateJourneyTerritoryStorage
) : State.Journey.Player.InteractionType.StorageType

class StateJourneyTerritoryRegion(
    override val points: List<Point>,
    override val color: Color,
    override val isPassable: Boolean
) : State.Journey.Territory.Region

class MutableStateJourneyTerritoryStorage(
    override val position: Point,
    override val size: Size,
    override val direction: Double,
    override val color: Color,
    override val items: MutableList<State.Journey.Item>
) : State.Journey.Territory.Storage

class MutableStateJourneyTerritory(
    override val size: Size,
    override val regions: List<State.Journey.Territory.Region>,
    override val storages: List<MutableStateJourneyTerritoryStorage>
) : State.Journey.Territory

class MutableIntelligence(
    override val goals: List<Intelligence.Goal>,
    override var goalCurrent: Intelligence.Goal?
) : Intelligence

class MutableDummy(
    override val position: MutablePoint,
    override val velocity: Double,
    override var directionExpected: Double,
    override var directionActual: Double,
    override val intelligence: MutableIntelligence
) : Dummy

class MutableStateJourneySnapshot(
    override val dummy: MutableDummy
) : State.Journey.Snapshot

class MutableStateJourney(
    override val territory: MutableStateJourneyTerritory,
    override val player: MutableStateJourneyPlayer,
    override val snapshot: MutableStateJourneySnapshot
) : State.Journey

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
