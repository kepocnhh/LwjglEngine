package lwjgl.game.roguelike.state

import lwjgl.wrapper.entity.Point

interface State {
    val shouldEngineStop: Boolean

    enum class Common {
        JOURNEY
    }

    val common: Common
    val journey: Journey

    interface Journey {
        interface Player {
            val position: Point
            val velocity: Double // unit per nanosecond
            val direction: Double // 0..359
        }

        val player: Player
    }
}
