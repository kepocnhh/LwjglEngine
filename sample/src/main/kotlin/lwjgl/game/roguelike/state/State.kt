package lwjgl.game.roguelike.state

import lwjgl.game.roguelike.engine.entity.Dummy
import lwjgl.wrapper.entity.Color
import lwjgl.wrapper.entity.Point
import lwjgl.wrapper.entity.Size

interface State {
    val shouldEngineStop: Boolean

    enum class Common {
        MAIN_MENU,
        JOURNEY,
    }

    val common: Common

    interface MainMenu {
        enum class Item {
            START_NEW_GAME,
            EXIT,
        }

        val selectedMenuItem: Item
    }

    val mainMenu: MainMenu

    interface Journey {
        interface Snapshot /* todo */ {
            val dummy: Dummy
        }
        val snapshot: Snapshot // todo

        interface Player {
            val position: Point
            val velocity: Double // unit per nanosecond
            val directionExpected: Double // 0..359
            val directionActual: Double // 0..359
        }
        interface Territory {
            val size: Size

            interface Region {
                val points: List<Point>
                val color: Color
                val isPassable: Boolean
            }

            val regions: List<Region>
        }

        val player: Player
        val territory: Territory
    }

    val journey: Journey?
}
