package lwjgl.game.roguelike.engine.logic

import lwjgl.game.roguelike.state.State
import lwjgl.wrapper.entity.Point

class MutablePoint(
    override var x: Double,
    override var y: Double
) : Point

class MutableStateJourneyPlayer(
    override val position: MutablePoint
) : State.Journey.Player

class MutableStateJourney : State.Journey {
    override val player: MutableStateJourneyPlayer = MutableStateJourneyPlayer(
        position = MutablePoint(x = 0.0, y = 0.0)
    )
}

class MutableState(override var common: State.Common) : State {
    private lateinit var shouldEngineStopUnit: Unit
    override val shouldEngineStop: Boolean get() = ::shouldEngineStopUnit.isInitialized
    fun engineStop() {
        shouldEngineStopUnit = Unit
    }

    override val journey: MutableStateJourney = MutableStateJourney()
}
