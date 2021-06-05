package lwjgl.game.roguelike.engine.util

import lwjgl.game.roguelike.state.State
import lwjgl.wrapper.entity.Line
import lwjgl.wrapper.entity.line
import lwjgl.wrapper.entity.point

private fun State.Journey.Territory.Storage.allLines(): List<Line> {
    val xStart = position.x - size.width / 2
    val xFinish = xStart + size.width
    val yStart = position.y - size.height / 2
    val yFinish = yStart + size.height
    val radians = java.lang.Math.toRadians(direction)
    return setOf(
        line(startX = xStart, startY = yFinish, finishX = xStart, finishY = yStart),
        line(startX = xFinish, startY = yFinish, finishX = xStart, finishY = yFinish),
        line(startX = xFinish, startY = yStart, finishX = xFinish, finishY = yFinish),
        line(startX = xStart, startY = yStart, finishX = xFinish, finishY = yStart)
    ).map {
        line(
            start = rotatePoint(
                point = it.start,
                pointOfRotation = position,
                radians = radians
            ),
            finish = rotatePoint(
                point = it.finish,
                pointOfRotation = position,
                radians = radians
            )
        )
    }
}

internal fun State.Journey.Territory.allLines(): List<Line> {
    return regions.filterNot { it.isPassable }.flatMap {
        it.allLines()
    } + storages.flatMap {
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
