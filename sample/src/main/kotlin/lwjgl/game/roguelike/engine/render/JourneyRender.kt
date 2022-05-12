package lwjgl.game.roguelike.engine.render

import lwjgl.engine.common.EngineProperty
import lwjgl.game.roguelike.engine.entity.Intelligence
import lwjgl.game.roguelike.engine.util.EPSILON_DEFAULT
import lwjgl.game.roguelike.engine.util.allLines
import lwjgl.game.roguelike.engine.util.calculateDistance
import lwjgl.game.roguelike.engine.util.getConvexHull
import lwjgl.game.roguelike.engine.util.getIndexPermutations
import lwjgl.game.roguelike.engine.util.getIndexPermutationsAll
import lwjgl.game.roguelike.engine.util.getIntersectionPointOrNull
import lwjgl.game.roguelike.engine.util.getParallelLine
import lwjgl.game.roguelike.engine.util.getParallelLines
import lwjgl.game.roguelike.engine.util.getPath
import lwjgl.game.roguelike.engine.util.isIntersectedBetweenEndpoints
import lwjgl.game.roguelike.engine.util.isPointOnLine
import lwjgl.game.roguelike.engine.util.isSame
import lwjgl.game.roguelike.engine.util.rotatePoint
import lwjgl.game.roguelike.state.State
import lwjgl.wrapper.canvas.Canvas
import lwjgl.wrapper.entity.ColorEntity
import lwjgl.wrapper.entity.Point
import lwjgl.wrapper.entity.line
import lwjgl.wrapper.entity.point
import lwjgl.wrapper.entity.size
import lwjgl.wrapper.entity.updated

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

    private var time = System.currentTimeMillis()
    private var index = 0

    private fun onPermutations(
        canvas: Canvas,
        journey: State.Journey,
        dX: Double,
        dY: Double
    ) {
        val timeNow = System.currentTimeMillis()
        val distance = kotlin.math.sqrt(playerSize.height * playerSize.height + playerSize.width * playerSize.width) / 2 // todo
        val circumscribed = journey.territory.regions.filter { !it.isPassable }.associateWith {
            allLines(getConvexHull(it.points)).map { line ->
                getParallelLine(
                    xStart = line.start.x, yStart = line.start.y, xFinish = line.finish.x, yFinish = line.finish.y,
                    distance = distance
                )
            }.let { lines ->
                val points = mutableListOf<Point>()
                for (i in lines.indices) {
                    val line = lines[i]
                    val n = if (i == lines.lastIndex) 0 else i+1
                    val iPoint = getIntersectionPointOrNull(p1 = line.start, p2 = line.finish, line = lines[n])
                    if (iPoint != null) {
                        points.add(iPoint) // todo
                    }
                }
                points
            }
        }
        val allPoints = circumscribed.flatMap { (_, points) -> points }
        val goalCurrent = journey.snapshot.dummy.intelligence.goalCurrent ?: TODO()
        check(goalCurrent is Intelligence.Goal.Move)
        val pc = circumscribed.map { (region, points) ->
            region to getIndexPermutations(points)
        }
        val pca = pc.map { (region, ll) ->
            ll.map { l -> region to l }
        }
        val pa = getIndexPermutationsAll(pc)
        val keys = circumscribed.keys.toList()
//        val permutations = getIndexPermutationsAll(pc).map { indices ->
//            val tmp = indices.flatMap { i ->
//                pc[i].flatMap { ik -> ik.flatMap { circumscribed[keys[it]]!! } }
//            }
//            listOf(journey.snapshot.dummy.position) + tmp + goalCurrent.target.position
//        }
        val permutations = getIndexPermutations(allPoints).map { indices ->
            listOf(journey.snapshot.dummy.position) + indices.map { allPoints[it] } + goalCurrent.target.position
        }
        if (timeNow - time > 500) {
            time = System.currentTimeMillis()
            index++
            if (index > permutations.lastIndex) {
                index = 0
            }
        }
        val path = getPath(permutations[index])
        for (i in path.indices) {
            val linePath = path[i]
            canvas.drawText(
                fullPathFont = fullPathFont,
                color = ColorEntity.WHITE,
                text = "" + (i + 1),
                pointTopLeft = linePath.finish.updated(dX = dX, dY = dY),
                fontHeight = 16f
            )
            for (k in path.indices) {
                if (k == i) continue
                val lineOther = path[k]
                val isIntersected = linePath.isIntersectedBetweenEndpoints(
                    other = lineOther,
                    epsilon = EPSILON_DEFAULT
                )
                if (isIntersected) {
                    canvas.drawLine(
                        color = ColorEntity.RED,
                        pointStart = linePath.start.updated(dX = dX, dY = dY),
                        pointFinish = linePath.finish.updated(dX = dX, dY = dY),
                        lineWidth = 1f
                    )
                    canvas.drawLine(
                        color = ColorEntity.YELLOW,
                        pointStart = lineOther.start.updated(dX = dX, dY = dY),
                        pointFinish = lineOther.finish.updated(dX = dX, dY = dY),
                        lineWidth = 1f
                    )
                    canvas.drawText(
                        fullPathFont = fullPathFont,
                        color = ColorEntity.RED,
                        text = "index: " + index + "/" + permutations.size,
                        pointTopLeft = point(x = 50, y = 50),
                        fontHeight = 16f
                    )
                    return
                }
            }
            val regions = circumscribed.keys.toList()
            for (j in regions.indices) {
                val region = regions[j]
                val ps = circumscribed[region]!!
                for (k in ps.indices) {
                    if (k == ps.lastIndex - 1) break
                    val lineRegion = line(ps[k], ps[k + 2])
                    val isIntersected = linePath.isIntersectedBetweenEndpoints(
                        other = lineRegion,
                        epsilon = EPSILON_DEFAULT
                    )
                    if (isIntersected) {
                        canvas.drawLine(
                            color = ColorEntity.RED,
                            pointStart = linePath.start.updated(dX = dX, dY = dY),
                            pointFinish = linePath.finish.updated(dX = dX, dY = dY),
                            lineWidth = 1f
                        )
                        canvas.drawLine(
                            color = ColorEntity.YELLOW,
                            pointStart = lineRegion.start.updated(dX = dX, dY = dY),
                            pointFinish = lineRegion.finish.updated(dX = dX, dY = dY),
                            lineWidth = 1f
                        )
                        canvas.drawText(
                            fullPathFont = fullPathFont,
                            color = ColorEntity.RED,
                            text = "index: " + index + "/" + permutations.size,
                            pointTopLeft = point(x = 50, y = 50),
                            fontHeight = 16f
                        )
                        return
                    }
                }
                val rl = allLines(ps)
                for (k in rl.indices) {
                    val lineRegion = rl[k]
                    val isIntersected = linePath.isIntersectedBetweenEndpoints(
                        other = lineRegion,
                        epsilon = EPSILON_DEFAULT
                    )
                    if (isIntersected) {
                        canvas.drawLine(
                            color = ColorEntity.RED,
                            pointStart = linePath.start.updated(dX = dX, dY = dY),
                            pointFinish = linePath.finish.updated(dX = dX, dY = dY),
                            lineWidth = 1f
                        )
                        canvas.drawLine(
                            color = ColorEntity.YELLOW,
                            pointStart = lineRegion.start.updated(dX = dX, dY = dY),
                            pointFinish = lineRegion.finish.updated(dX = dX, dY = dY),
                            lineWidth = 1f
                        )
                        canvas.drawText(
                            fullPathFont = fullPathFont,
                            color = ColorEntity.RED,
                            text = "index: " + index + "/" + permutations.size,
                            pointTopLeft = point(x = 50, y = 50),
                            fontHeight = 16f
                        )
                        return
                    }
                }
            }
            canvas.drawLine(
                color = ColorEntity.GREEN,
                pointStart = linePath.start.updated(dX = dX, dY = dY),
                pointFinish = linePath.finish.updated(dX = dX, dY = dY),
                lineWidth = 1f
            )
        }
        canvas.drawText(
            fullPathFont = fullPathFont,
            color = ColorEntity.GREEN,
            text = "index: " + index + "/" + permutations.size,
            pointTopLeft = point(x = 50, y = 50),
            fontHeight = 16f
        )
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
                    it.updated(dX = dX, dY = dY)
                },
                lineWidth = 1f
            )
        }
        journey.territory.storages.forEach {
            canvas.drawRectangle(
                color = it.color,
                pointTopLeft = it.position.updated(
                    dX = dX - it.size.width / 2,
                    dY = dY - it.size.height /2
                ),
                size = it.size,
                lineWidth = 2f,
                direction = it.direction,
                pointOfRotation = it.position.updated(dX = dX, dY = dY)
            )
            val fontHeight = 14f
            canvas.drawByText(
                fullPathFont = fullPathFont,
                fontHeight = fontHeight,
                color = it.color,
                text = "S"
            ) { width ->
                it.position.updated(dX = dX - width/2, dY = dY - fontHeight/2)
            }
        }
        movableRender.onRender(
            canvas = canvas,
            dX = dX,
            dY = dY,
            movable = journey.snapshot.dummy,
            color = ColorEntity.BLUE
        )
        //
        onPermutations(canvas, journey, dX = dX, dY = dY)
        //
        playerRender.onRender(
            canvas = canvas,
            player = journey.player,
            engineProperty = engineProperty
        )
//        debug(canvas, journey, engineProperty, center, dX, dY)
    }
}
