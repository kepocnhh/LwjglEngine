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

        interface PlayerState {
            object MoveState : PlayerState
            interface ExchangeStorageState : PlayerState {
                val storage: Territory.Storage
                val focusedItem: Item?
                val focusedStorage: Boolean
            }
        }

        interface Item {
            val title: String
        }

        interface Player {
            val position: Point
            val velocity: Double // unit per nanosecond
            val directionExpected: Double // 0..359
            val directionActual: Double // 0..359
            val state: PlayerState

//            interface Indicator {
//                val interaction: Boolean
//            }
//
//            val indicator: Indicator

            sealed class InteractionType {
                class StorageType(val storage: Territory.Storage) : InteractionType()
            }

            val interactions: Set<InteractionType>
            val items: Set<Item>
        }

        interface Territory {
            val size: Size

            interface Region {
                val points: List<Point>
                val color: Color
                val isPassable: Boolean
            }

            val regions: List<Region>

            interface Storage {
                val position: Point
                val size: Size
                val direction: Double // 0..359
                val color: Color
                val items: Set<Item>
            }

            val storages: List<Storage>
        }

        val player: Player
        val territory: Territory
    }

    val journey: Journey?
}
