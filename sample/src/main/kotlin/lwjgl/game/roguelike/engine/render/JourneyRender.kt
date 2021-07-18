package lwjgl.game.roguelike.engine.render

import lwjgl.engine.common.EngineProperty
import lwjgl.game.roguelike.engine.entity.Intelligence
import lwjgl.game.roguelike.engine.util.allLines
import lwjgl.game.roguelike.engine.util.getConvexHull
import lwjgl.game.roguelike.engine.util.getIntersectionPointOrNull
import lwjgl.game.roguelike.engine.util.getParallelLine
import lwjgl.game.roguelike.engine.util.getParallelLines
import lwjgl.game.roguelike.engine.util.isPointOnLine
import lwjgl.game.roguelike.engine.util.rotatePoint
import lwjgl.game.roguelike.state.State
import lwjgl.wrapper.canvas.Canvas
import lwjgl.wrapper.entity.ColorEntity
import lwjgl.wrapper.entity.Point
import lwjgl.wrapper.entity.point
import lwjgl.wrapper.entity.size
import lwjgl.wrapper.entity.update

class JourneyRender(
    private val fullPathFont: String,
    private val pixelsPerUnit: Double
) {
    private val playerSize = size(width = 1 * pixelsPerUnit, height = 1 * pixelsPerUnit)
    private val playerRender = JourneyPlayerRender(
        fullPathFont = fullPathFont,
        playerSize = playerSize, // todo
        pixelsPerUnit = pixelsPerUnit
    )
    private val movableRender: MovableRender = MovableRender(size = playerSize)

/*
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
        val lines = allLines(regions = journey.territory.regions.filter { !it.isPassable })
        val linesShortest = lines.filter {
            val distanceShortest = calculateDistance(
                line = it,
                point = p2
            )
            distanceShortest < distanceMin
        }
        if (linesShortest.isEmpty()) {
//               2
//              /
//             /
//            1
        } else {
            val line = linesShortest.minByOrNull {
                calculateDistance(
                    line = it,
                    point = p2
                )
            }!!
            val intersectionPoint = getIntersectionPointOrNull(
                p1 = p1,
                p2 = p2,
                line = line
            )
            if (intersectionPoint == null) {
                // todo
            } else {
                canvas.drawLine(
                    color = ColorEntity.GREEN,
                    lineWidth = 1f,
                    pointStart = center,
                    pointFinish = intersectionPoint.update(dX = dX, dY = dY)
                )
//                val distanceIntersection = sqrt((intersectionPoint.x - p1.x) * (intersectionPoint.x - p1.x) + (intersectionPoint.y - p1.y) * (intersectionPoint.y - p1.y))
                val distanceIntersection = calculateDistance(
                    pointStart = intersectionPoint,
                    pointFinish = p1
                )
                canvas.drawText(
                    fullPathFont = fullPathFont,
                    color = ColorEntity.GREEN,
                    fontHeight = 14f,
                    text = String.format("%.2f", distanceIntersection),
                    pointTopLeft = point(x = 1 * pixelsPerUnit, y = 6 * pixelsPerUnit)
                )
                val distanceShortest = calculateDistance(
                    line = line,
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
                    pointStart = line.start.update(dX = dX, dY = dY),
                    pointFinish = line.finish.update(dX = dX, dY = dY),
                    lineWidth = 3f
                )
            }
            // todo
        }
    }
*/

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
                    it.update(dX = dX, dY = dY)
                },
                lineWidth = 1f
            )
            val lines = region.allLines()
//            val distance = kotlin.math.sqrt(playerSize.height * playerSize.height + playerSize.width * playerSize.width)
            val distance = kotlin.math.sqrt(playerSize.height * playerSize.height + playerSize.width * playerSize.width) / 2.0
            val p = lines.map {
                getParallelLine(
                    xStart = it.start.x, yStart = it.start.y, xFinish = it.finish.x, yFinish = it.finish.y,
                    distance = distance
                )
            }
            for (i in p.indices) {
                val it = p[i]
                val next = if (i == p.lastIndex) 0 else i+1
                val iPoint = getIntersectionPointOrNull(p1 = it.start, p2 = it.finish, line = p[next])
                if (iPoint != null) {
//                    canvas.drawPoint(color = ColorEntity.RED, point = iPoint.update(dX = dX, dY = dY))
                }
//                canvas.drawLine(
//                    color = ColorEntity.CYAN,
//                    pointStart = it.start.update(dX = dX, dY = dY),
//                    pointFinish = it.finish.update(dX = dX, dY = dY),
//                    lineWidth = 1f,
//                )
            }
//            println("region " + region.color)
//            val convexHull = getConvexHull(region.points)
//            canvas.drawLineLoop(
//                color = ColorEntity.CYAN,
//                points = convexHull.map {
//                    it.update(dX = dX, dY = dY)
//                },
//                lineWidth = 1f
//            )
            val dummy = journey.snapshot.dummy
            val goalCurrent = dummy.intelligence.goalCurrent
            if (goalCurrent is Intelligence.Goal.Move) {
                val target = goalCurrent.target
                p.let {
                    val points = mutableListOf<Point>()
                    for (i in it.indices) {
                        val line = it[i]
                        val n = if (i == it.lastIndex) 0 else i+1
                        val iPoint = getIntersectionPointOrNull(p1 = line.start, p2 = line.finish, line = it[n])
                        if (iPoint != null) {
                            points.add(iPoint) // todo
                        }
                    }
                    allLines(points)
                }.forEach {
                    val iPoint = getIntersectionPointOrNull(p1 = dummy.position, p2 = target.position, line = it)
                    if (iPoint != null && isPointOnLine(iPoint, it)) {
//                        canvas.drawRectangle(
//                            color = ColorEntity.GREEN,
//                            pointTopLeft = iPoint.update(dX = dX, dY = dY),
//                            size = size(2, 2),
//                            lineWidth = 1f
//                        )
                    }
                }
            }
        }
        journey.territory.storages.forEach {
            canvas.drawRectangle(
                color = it.color,
                pointTopLeft = it.position.update(
                    dX = dX - it.size.width / 2,
                    dY = dY - it.size.height /2
                ),
                size = it.size,
                lineWidth = 2f,
                direction = it.direction,
                pointOfRotation = it.position.update(dX = dX, dY = dY)
            )
            val fontHeight = 14f
            canvas.drawByText(
                fullPathFont = fullPathFont,
                fontHeight = fontHeight,
                color = it.color,
                text = "S"
            ) { width ->
                it.position.update(dX = dX - width/2, dY = dY - fontHeight/2)
            }
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
//        debug(canvas, journey, engineProperty, center, dX, dY)
    }
}
