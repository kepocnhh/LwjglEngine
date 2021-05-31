package lwjgl.game.roguelike.engine.render

import lwjgl.engine.common.EngineProperty
import lwjgl.game.roguelike.engine.util.calculateDistance
import lwjgl.game.roguelike.engine.util.getIntersectionPointOrNull
import lwjgl.game.roguelike.engine.util.getNewPositionByDirection
import lwjgl.game.roguelike.state.State
import lwjgl.wrapper.canvas.Canvas
import lwjgl.wrapper.entity.ColorEntity
import lwjgl.wrapper.entity.Point
import lwjgl.wrapper.entity.point
import lwjgl.wrapper.entity.size
import kotlin.math.sqrt

class JourneyRender(
    private val fullPathFont: String,
    private val pixelsPerUnit: Double
) {
    private val playerSize = size(width = 1 * pixelsPerUnit, height = 1 * pixelsPerUnit)
    private val playerRender = JourneyPlayerRender(
        fullPathFont = fullPathFont,
        playerSize = playerSize // todo
    )
    private val movableRender: MovableRender = MovableRender(size = playerSize)

    private fun debug(
        canvas: Canvas,
        journey: State.Journey,
        engineProperty: EngineProperty,
        center: Point,
        dX: Double,
        dY: Double
    ) {
        val distanceMin = sqrt(playerSize.width * playerSize.width + playerSize.height * playerSize.height) / 2
//        val distanceMin = 50.0
        val p1: Point = journey.player.position
        val velocityMultiple = 1.0 // todo
        val p2 = getNewPositionByDirection(
            oldPosition = journey.player.position,
//            units = journey.player.velocity,
            units = journey.player.velocity * velocityMultiple * (engineProperty.timeNow - engineProperty.timeLast) * pixelsPerUnit,
            direction = journey.player.directionExpected
        )
        canvas.drawPoint(
            color = ColorEntity.WHITE,
            point = point(
                x = p2.x + dX,
                y = p2.y + dY
            )
        )
        val points = journey.territory.regions.filter { !it.isPassable }.flatMap {
            val result = mutableListOf<Pair<Point, Point>>()
            var i = 0
            val size = it.points.size
            while (true) {
                if (i == size) break
                val next: Int = when (i) {
                    size - 1 -> 0
                    else -> i + 1
                }
                result.add(it.points[i] to it.points[next])
                i++
            }
            result
        }
        val pointsShortest = points.filter { (p3, p4) ->
            val distanceShortest = calculateDistance(
                pointStart = p3,
                pointFinish = p4,
                point = p2
            )
            distanceShortest < distanceMin
        }
        if (pointsShortest.isEmpty()) {
//               2
//              /
//             /
//            1
        } else {
            val (p3, p4) = pointsShortest.minByOrNull { (p3, p4) ->
                calculateDistance(
                    pointStart = p3,
                    pointFinish = p4,
                    point = p2
                )
            }!!
            val intersectionPoint = getIntersectionPointOrNull(
                p1 = p1,
                p2 = p2,
                p3 = p3,
                p4 = p4
            )
            if (intersectionPoint == null) {
                // todo
            } else {
                canvas.drawLine(
                    color = ColorEntity.GREEN,
                    lineWidth = 1f,
                    pointStart = center,
                    pointFinish = point(
                        x = intersectionPoint.x + dX,
                        y = intersectionPoint.y + dY
                    )
                )
                val distanceIntersection = sqrt((intersectionPoint.x - p1.x) * (intersectionPoint.x - p1.x) + (intersectionPoint.y - p1.y) * (intersectionPoint.y - p1.y))
                canvas.drawText(
                    fullPathFont = fullPathFont,
                    color = ColorEntity.GREEN,
                    fontHeight = 14f,
                    text = String.format("%.2f", distanceIntersection),
                    pointTopLeft = point(x = 1 * pixelsPerUnit, y = 6 * pixelsPerUnit)
                )
                val distanceShortest = calculateDistance(
                    pointStart = p3,
                    pointFinish = p4,
                    point = p1
                )
                val color = if (distanceShortest < 17.677) {
                    ColorEntity.RED
                } else {
                    ColorEntity.GREEN
                }
                canvas.drawText(
                    fullPathFont = fullPathFont,
                    color = color,
                    fontHeight = 14f,
                    text = String.format("%.2f", distanceShortest),
                    pointTopLeft = point(x = 1 * pixelsPerUnit, y = 5 * pixelsPerUnit)
                )
                canvas.drawLine(
                    color = ColorEntity.RED,
                    pointStart = point(
                        x = dX + p3.x,
                        y = dY + p3.y
                    ),
                    pointFinish = point(
                        x = dX + p4.x,
                        y = dY + p4.y
                    ),
                    lineWidth = 3f
                )
            }
            // todo
        }
    }
    fun onRender(
        canvas: Canvas,
        journey: State.Journey,
        engineProperty: EngineProperty
    ) {
        val center = point(x = engineProperty.pictureSize.width / 2, y = engineProperty.pictureSize.height / 2)
        val dX = center.x - journey.player.position.x
        val dY = center.y - journey.player.position.y
        canvas.drawRectangle(
            color = ColorEntity.WHITE,
            pointTopLeft = point(x = dX, y = dY),
            size = journey.territory.size,
            lineWidth = 1f
        )
        journey.territory.regions.forEach { region ->
            canvas.drawLineLoop(
                color = region.color,
                points = region.points.map {
                    point(
                        x = dX + it.x,
                        y = dY + it.y
                    )
                },
                lineWidth = 1f
            )
        }
        movableRender.onRender(
            canvas = canvas,
            dX = dX,
            dY = dY,
            movable = journey.snapshot.dummy,
            color = ColorEntity.BLUE
        )
        playerRender.onRender(
            canvas = canvas,
            player = journey.player,
            engineProperty = engineProperty
        )
        debug(canvas, journey, engineProperty, center, dX, dY)
    }
}
