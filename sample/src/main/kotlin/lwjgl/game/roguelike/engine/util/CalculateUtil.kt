package lwjgl.game.roguelike.engine.util

import kotlin.math.absoluteValue
import lwjgl.game.roguelike.util.isLessThan
import lwjgl.wrapper.entity.Point
import lwjgl.wrapper.entity.point
import lwjgl.wrapper.entity.Line
import lwjgl.wrapper.entity.line
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

/**
 * By vector!
 */
internal fun getParallelLine(
    xStart: Double,
    yStart: Double,
    xFinish: Double,
    yFinish: Double,
    distance: Double
): Line {
    if (xStart == xFinish) {
        if (yStart == yFinish) TODO()
        val dX = if (yStart < yFinish) distance else - distance
        return line(
            startX = xStart + dX,
            finishX = xFinish + dX,
            startY = yStart,
            finishY = yFinish
        )
    }
    if (yStart == yFinish) {
        val dY = if (xStart < xFinish) - distance else distance
        return line(
            startX = xStart,
            finishX = xFinish,
            startY = yStart + dY,
            finishY = yFinish + dY
        )
    }
    val angle = calculateAngle(oldX = xStart, oldY = yStart, newX = xFinish, newY = yFinish)
    val radians = Math.toRadians(angle + 90.0)
    return line(
        start = rotatePoint(x = xStart, y = yStart + distance, xRotationOf = xStart, yRotationOf = yStart, radians = radians),
        finish = rotatePoint(x = xFinish, y = yFinish + distance, xRotationOf = xFinish, yRotationOf = yFinish, radians = radians)
    )
}
internal fun getParallelLines(
    xStart: Double,
    yStart: Double,
    xFinish: Double,
    yFinish: Double,
    distance: Double
): Pair<Line, Line> {
    if (xStart == xFinish) {
        if (yStart == yFinish) TODO()
        return line(
            startX = xStart + distance,
            finishX = xFinish + distance,
            startY = yStart,
            finishY = yFinish
        ) to line(
            startX = xStart - distance,
            finishX = xFinish - distance,
            startY = yStart,
            finishY = yFinish
        )
    }
    if (yStart == yFinish) {
        return line(
            startX = xStart,
            finishX = xFinish,
            startY = yStart + distance,
            finishY = yFinish + distance
        ) to line(
            startX = xStart,
            finishX = xFinish,
            startY = yStart - distance,
            finishY = yFinish - distance
        )
    }
    val x1: Double
    val x2: Double
    val y1: Double
    val y2: Double
    if (xStart < xFinish) {
        x1 = xStart
        x2 = xFinish
        y1 = yStart
        y2 = yFinish
    } else {
        x1 = xFinish
        x2 = xStart
        y1 = yFinish
        y2 = yStart
    }
    val angle = calculateAngle(oldX = x1, oldY = y1, newX = x2, newY = y2)
    val angleResult = if (y1 > y2) {
        - (90.0 - angle)
    } else {
        angle - 90.0
    }
    val radians1 = Math.toRadians(angleResult)
    val resultStart1 = rotatePoint(x = x1, y = y1 + distance, xRotationOf = x1, yRotationOf = y1, radians = radians1)
    val resultStart2 = rotatePoint(x = x1, y = y1 + distance, xRotationOf = x1, yRotationOf = y1, radians = Math.toRadians(angleResult + 180.0))
    val resultFinish1 = rotatePoint(x = x2, y = y2 + distance, xRotationOf = x2, yRotationOf = y2, radians = radians1)
    val resultFinish2 = rotatePoint(x = x2, y = y2 + distance, xRotationOf = x2, yRotationOf = y2, radians = Math.toRadians(angleResult + 180.0))
    return line(resultStart1, resultFinish1) to line(resultStart2, resultFinish2)
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
            if (angleResult < 90) continue // todo 180 ?
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

internal const val EPSILON_DEFAULT: Double = 0.00001
internal fun Double.equals(other: Double, epsilon: Double): Boolean {
    if (epsilon < 0.0 || epsilon >= 1.0) TODO()
    return (this - other).absoluteValue < epsilon
}

internal fun isPointOnLine(point: Point, line: Line, epsilon: Double = EPSILON_DEFAULT): Boolean {
    if (line.start.x.equals(line.finish.x, epsilon = epsilon)) {
        if (line.start.y.equals(line.finish.y, epsilon = epsilon)) TODO()
        return if (line.start.y < line.finish.y) {
            point.x.equals(line.start.x, epsilon = epsilon) && point.y in line.start.y..line.finish.y
        } else {
            point.x.equals(line.start.x, epsilon = epsilon) && point.y in line.finish.y..line.start.y
        }
    }
//    println("sy ${line.start.y} / fy ${line.finish.y}")
    if (line.start.y.equals(line.finish.y, epsilon = epsilon)) {
//        println("sy ${line.start.y} == fy ${line.finish.y}")
        return if (line.start.x < line.finish.x) {
//            println("lsx ${line.start.x} < lfx ${line.finish.x}")
            point.y.equals(line.start.y, epsilon = epsilon) && point.x in line.start.x..line.finish.x
        } else {
//            println("lsx ${line.start.x} > lfx ${line.finish.x}")
            point.y.equals(line.start.y, epsilon = epsilon) && point.x in line.finish.x..line.start.x
        }
    }
    return ((point.x - line.start.x) / (line.finish.x - line.start.x) -
            (point.y - line.start.y) / (line.finish.y - line.start.y)).equals(0.0, epsilon = epsilon)
}

internal fun isPointOnLineOld(point: Point, line: Line): Boolean {
    if (line.start.x == line.finish.x) {
        if (line.start.y == line.finish.y) TODO()
        return if (line.start.y < line.finish.y) {
            point.x == line.start.x && point.y in line.start.y..line.finish.y
        } else {
            point.x == line.start.x && point.y in line.finish.y..line.start.y
        }
    }
    println("sy ${line.start.y} / fy ${line.finish.y}")
    if (line.start.y == line.finish.y) {
        println("sy ${line.start.y} == fy ${line.finish.y}")
        return if (line.start.x < line.finish.x) {
            println("lsx ${line.start.x} < lfx ${line.finish.x}")
            point.y == line.start.y && point.x in line.start.x..line.finish.x
        } else {
            println("lsx ${line.start.x} > lfx ${line.finish.x}")
            point.y == line.start.y && point.x in line.finish.x..line.start.x
        }
    }
    return (point.x - line.start.x) / (line.finish.x - line.start.x) -
            (point.y - line.start.y) / (line.finish.y - line.start.y) == 0.0
}

fun Point.equals(other: Point, epsilon: Double): Boolean {
    return x.equals(other.x, epsilon = epsilon) && y.equals(other.y, epsilon = epsilon)
}
