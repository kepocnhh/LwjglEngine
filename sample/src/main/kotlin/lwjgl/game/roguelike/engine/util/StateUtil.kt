package lwjgl.game.roguelike.engine.util

import lwjgl.game.roguelike.state.State
import lwjgl.wrapper.entity.Line
import lwjgl.wrapper.entity.Point
import lwjgl.wrapper.entity.line

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

internal fun getPath(points: List<Point>): List<Line> {
    val result = mutableListOf<Line>()
    for (i in points.indices) {
        if (i == points.lastIndex) break
        result.add(line(start = points[i], finish = points[i + 1]))
    }
    return result
}

internal fun allLines(points: List<Point>): List<Line> {
    return getPath(points) + line(start = points.last(), finish = points.first())
}

internal fun State.Journey.Territory.Region.allLines(): List<Line> {
    return allLines(points = points)
}

internal fun allPoints(lines: List<Line>): List<Point> {
    val size = lines.size
    if (size < 0) TODO()
    if (size == 0) return emptyList()
    if (size == 1) return lines.firstOrNull()!!.let { listOf(it.start, it.finish) }
    val result = mutableListOf<Point>()
    result.add(lines.firstOrNull()!!.start)
    for (i in 1..lines.lastIndex) {
        result.add(lines[i].start)
    }
    return result
}
