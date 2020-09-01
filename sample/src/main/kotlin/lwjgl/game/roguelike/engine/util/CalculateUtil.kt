package lwjgl.game.roguelike.engine.util

import lwjgl.wrapper.entity.Point
import lwjgl.wrapper.entity.point
import kotlin.math.*

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
    val d = sqrt(dY * dY + dX * dX)
    val dS = sqrt((yStart - y) * (yStart - y) + (xStart - x) * (xStart - x))
    val dF = sqrt((yFinish - y) * (yFinish - y) + (xFinish - x) * (xFinish - x))
    val shortest = (dY * x - dX * y + xFinish * yStart - yFinish * xStart).absoluteValue / d
    if (sqrt(dS * dS - shortest * shortest) > d) return dF
    if (sqrt(dF * dF - shortest * shortest) > d) return dS
    return shortest
}

internal fun calculateDistance(
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
    xStart: Double,
    yStart: Double,
    xFinish: Double,
    yFinish: Double
): Double {
    val dX = xFinish - xStart
    val dY = yFinish - yStart
    return sqrt(dY * dY + dX * dX)
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

internal fun getIntersectionPointOrNull(
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

internal fun getNewPositionByDirection(
    oldPosition: Point,
    units: Double,
    direction: Double
): Point {
    val radians = Math.toRadians(direction)
    return point(
        x = oldPosition.x + units * sin(radians),
        y = oldPosition.y - units * cos(radians)
    )
}

internal fun calculateAngle(oldX: Double, oldY: Double, newX: Double, newY: Double): Double {
    val angle = atan2(y = oldX - newX, x = oldY - newY)
    val degrees = angle * -180.0 / kotlin.math.PI
    return degrees + ceil(-degrees / 360.0) * 360.0
}

internal fun getTriangleHeightPoint(
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
