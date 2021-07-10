package lwjgl.game.roguelike.engine.util

import java.util.LinkedList
import kotlin.math.absoluteValue
import lwjgl.game.roguelike.util.isLessThan
import lwjgl.wrapper.entity.Point
import lwjgl.wrapper.entity.point
import lwjgl.wrapper.entity.Line
import lwjgl.wrapper.entity.update

internal fun calculateDistance(
    xStart: Double,
    yStart: Double,
    xFinish: Double,
    yFinish: Double,
    x: Double,
    y: Double
): Double {
    val dX = xFinish - xStart
    val dY = yFinish - yStart
    val d = kotlin.math.sqrt(dY * dY + dX * dX)
    val dS = kotlin.math.sqrt((yStart - y) * (yStart - y) + (xStart - x) * (xStart - x))
    val dF = kotlin.math.sqrt((yFinish - y) * (yFinish - y) + (xFinish - x) * (xFinish - x))
    val shortest = (dY * x - dX * y + xFinish * yStart - yFinish * xStart).absoluteValue / d
    if (kotlin.math.sqrt(dS * dS - shortest * shortest) > d) return dF
    if (kotlin.math.sqrt(dF * dF - shortest * shortest) > d) return dS
    return shortest
}

private fun calculateDistance(
    pointStart: Point,
    pointFinish: Point,
    point: Point
): Double {
    return calculateDistance(
        xStart = pointStart.x,
        yStart = pointStart.y,
        xFinish = pointFinish.x,
        yFinish = pointFinish.y,
        x = point.x,
        y = point.y
    )
}

internal fun calculateDistance(
    line: Line,
    point: Point
): Double {
    return calculateDistance(
        pointStart = line.start,
        pointFinish = line.finish,
        point = point
    )
}

private fun calculateDistance(
    xStart: Double,
    yStart: Double,
    xFinish: Double,
    yFinish: Double
): Double {
    val dX = xFinish - xStart
    val dY = yFinish - yStart
    return kotlin.math.sqrt(dY * dY + dX * dX)
}

internal fun calculateDistance(
    pointStart: Point,
    pointFinish: Point
): Double {
    return calculateDistance(
        xStart = pointStart.x,
        yStart = pointStart.y,
        xFinish = pointFinish.x,
        yFinish = pointFinish.y
    )
}

private fun getIntersectionPointOrNull(
    p1: Point,
    p2: Point,
    p3: Point,
    p4: Point
): Point? {
    val uBottom = (p4.y - p3.y) * (p2.x - p1.x) - (p4.x - p3.x) * (p2.y - p1.y)
    if (uBottom == 0.0) return null
    val uTopA = (p4.x - p3.x) * (p1.y - p3.y) - (p4.y - p3.y) * (p1.x - p3.x)
    val uA = uTopA / uBottom
    return point(
        x = p1.x + uA * (p2.x - p1.x),
        y = p1.y + uA * (p2.y - p1.y)
    )
}

internal fun getIntersectionPointOrNull(
    p1: Point,
    p2: Point,
    line: Line
): Point? {
    return getIntersectionPointOrNull(
        p1 = p1, p2 = p2, p3 = line.start, p4 = line.finish
    )
}

internal fun getNewPositionByDirection(
    oldPosition: Point,
    units: Double,
    direction: Double
): Point {
    val radians = Math.toRadians(direction)
    return oldPosition.update(
        dX = units * kotlin.math.sin(radians),
        dY = - units * kotlin.math.cos(radians)
    )
}

internal fun calculateAngle(oldX: Double, oldY: Double, newX: Double, newY: Double): Double {
    val angle = kotlin.math.atan2(y = oldX - newX, x = oldY - newY)
    val degrees = angle * -180.0 / kotlin.math.PI
    return degrees + kotlin.math.ceil(-degrees / 360.0) * 360.0
}

internal fun getTriangleHeightPoint(
    line: Line,
    point: Point
): Point {
    return getTriangleHeightPoint(
        baseStart = line.start,
        baseFinish = line.finish,
        point = point
    )
}

private fun getTriangleHeightPoint(
    baseStart: Point,
    baseFinish: Point,
    point: Point
): Point {
    if (baseStart.x == baseFinish.x) {
        return point(x = baseStart.x, y = point.y)
    }
    if (baseStart.y == baseFinish.y) {
        return point(x = point.x, y = baseStart.y)
    }
    val k = (baseStart.y - baseFinish.y) / (baseStart.x - baseFinish.x)
    val b1 = baseFinish.y - k * baseFinish.x
    val b2 = point.y + point.x / k
    val x = (b2 - b1) / (k + 1 / k)
    return point(x = x, y = k * x + b1)
}

internal fun isNewPositionAllowed(
    lines: List<Line>,
    distanceMin: Double,
    newPosition: Point
): Boolean {
    val distanceShortest = lines.map {
        calculateDistance(
            line = it,
            point = newPosition
        )
    }.minOrNull() ?: return true
    return !distanceShortest.isLessThan(distanceMin, precision = 12)
}

/**
 * y' - - - -*
 * y - - - -/- -*
 *         /  / |
 *        /a/   |
 * yr - -*   |  |
 *       |   |  |
 *      xr   x' x
 */
internal fun rotatePoint(
    x: Double,
    y: Double,
    xRotationOf: Double,
    yRotationOf: Double,
    radians: Double
): Point {
    return point(
        x = xRotationOf + (x - xRotationOf) * kotlin.math.cos(radians) -
                (y - yRotationOf) * kotlin.math.sin(radians),
        y = yRotationOf + (x - xRotationOf) * kotlin.math.sin(radians) +
                (y - yRotationOf) * kotlin.math.cos(radians)
    )
}

internal fun rotatePoint(
    point: Point,
    xRotationOf: Double,
    yRotationOf: Double,
    radians: Double
): Point {
    return rotatePoint(
        x = point.x,
        y = point.y,
        xRotationOf = xRotationOf,
        yRotationOf = yRotationOf,
        radians = radians
    )
}

internal fun rotatePoint(
    point: Point,
    pointOfRotation: Point,
    radians: Double
): Point {
    return rotatePoint(
        point = point,
        xRotationOf = pointOfRotation.x,
        yRotationOf = pointOfRotation.y,
        radians = radians
    )
}

internal fun getParallelLines(
    xStart: Double,
    yStart: Double,
    xFinish: Double,
    yFinish: Double
): Pair<Line, Line> {
    TODO()
}

/*
private fun getNextConvexPoint(points: List<Point>, point: Point, dAngle: Double): Pair<Point, Double> {
    val size = points.size
    if (size < 2) TODO()
    return points.filter {
        it != point
    }.map {
        it to calculateAngle(oldX = point.x, oldY = point.y, newX = it.x, newY = it.y)
    }.minByOrNull { (_, angle) -> angle - dAngle }!!
}
*/

private fun getNextConvexPoint(points: List<Point>, point: Point): Point {
    val size = points.size
    if (size < 2) TODO()
    val list = points.filter { it != point }.map {
        it to calculateAngle(oldX = point.x, oldY = point.y, newX = it.x, newY = it.y)
    }
//    println("$point " + list.joinToString { (p, a) -> "${p.x}/${p.y}/$a" })
    val minAngle = list.minOfOrNull { (_, a) -> a }!!
    val filtered = list.filter { (_, a) -> a == minAngle }
    return filtered.minByOrNull { (p, _) ->
        calculateDistance(pointStart = point, pointFinish = p)
    }!!.first
}

internal fun getConvexHull(points: List<Point>): List<Point> {
    val size = points.size
    if (size < 3) TODO()
    if (size == 3) return points
    val sorted = points.sortedWith { p1, p2 ->
        when {
            p1.y > p2.y -> 1
            p1.y < p2.y -> -1
            p1.x > p2.x -> 1
            p1.x < p2.x -> -1
            else -> 0
        }
    }
    val pointFirst = sorted.firstOrNull()!!
    val result = mutableListOf<Point>()
    result.add(pointFirst)
    var pointCurrent = getNextConvexPoint(sorted, point = pointFirst)
    result.add(pointCurrent)
    var pointPrevious = pointFirst
    var k = 0
    while (true) {
        val tmp = mutableMapOf<Double, MutableList<Point>>()
        val angleOld = calculateAngle(oldX = pointCurrent.x, oldY = pointCurrent.y, newX = pointPrevious.x, newY = pointPrevious.y)
        for (i in sorted.indices) {
            val p = sorted[i]
            if (p == pointPrevious) continue
            if (p == pointCurrent) continue
            val angle = calculateAngle(oldX = pointCurrent.x, oldY = pointCurrent.y, newX = p.x, newY = p.y)
            val angleResult = (angle - angleOld).let {
                if (it < 0) it + 360 else it
            }
            if (angleResult < 90) continue
            tmp.getOrPut(angleResult, ::mutableListOf).add(p)
        }
        pointPrevious = pointCurrent
        val (angle, list) = tmp.minByOrNull { (angle, _) -> angle }!!
        pointCurrent = list.minByOrNull {
            calculateDistance(pointStart = pointCurrent, pointFinish = it)
        }!!
//        println("prev ${pointPrevious.x.toInt()/25}/${pointPrevious.y.toInt()/25} curr ${pointCurrent.x.toInt()/25}/${pointCurrent.y.toInt()/25} a " + String.format("%.1f", angle))
        if (pointCurrent == pointFirst) break
        result.add(pointCurrent)
        k++
        if (k > 10) TODO()
    }
    return result
}
