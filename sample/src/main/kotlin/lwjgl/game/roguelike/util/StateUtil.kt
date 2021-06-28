package lwjgl.game.roguelike.util

import lwjgl.game.roguelike.state.State

internal object StateUtil {
    private fun List<State.Journey.Item>.getSortedItems(type: State.Journey.Item.SortedType): List<State.Journey.Item> {
        return when (type) {
            State.Journey.Item.SortedType.TITLE -> sortedBy { it.title }
        }
    }
    fun State.Journey.Player.getSortedItems(): List<State.Journey.Item> {
        return items.getSortedItems(type = State.Journey.Item.SortedType.TITLE) // todo
    }
    fun State.Journey.Territory.Storage.getSortedItems(): List<State.Journey.Item> {
        return items.getSortedItems(type = State.Journey.Item.SortedType.TITLE) // todo
    }
}
