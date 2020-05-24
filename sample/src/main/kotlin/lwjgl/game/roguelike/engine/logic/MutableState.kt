package lwjgl.game.roguelike.engine.logic

import lwjgl.game.roguelike.state.State
import lwjgl.wrapper.entity.Point

data class MutablePoint(
    override var x: Double,
    override var y: Double
) : Point {
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
    override val velocity: Double = 5.0
    override var direction: Double = 0.0
}

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
