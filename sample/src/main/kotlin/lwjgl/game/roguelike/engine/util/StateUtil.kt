package lwjgl.game.roguelike.engine.util

import lwjgl.game.roguelike.state.State
import lwjgl.wrapper.entity.Line
import lwjgl.wrapper.entity.line

internal fun allLines(regions: List<State.Journey.Territory.Region>): List<Line> {
    return regions.flatMap {
        it.allLines()
    }
}

private fun State.Journey.Territory.Region.allLines(): List<Line> {
    val result = mutableListOf<Line>()
    var i = 0
    val size = points.size
    while (true) {
        val next: Int = when (i) {
            size -> break
            size - 1 -> 0
            else -> i + 1
        }
        result.add(line(start = points[i], finish = points[next]))
        i++
    }
    return result
}
