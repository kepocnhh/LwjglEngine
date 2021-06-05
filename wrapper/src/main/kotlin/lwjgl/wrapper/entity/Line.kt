package lwjgl.wrapper.entity

interface Line {
    val start: Point
    val finish: Point
}

private class LineImpl(override val start: Point, override val finish: Point) : Line {
    override fun toString(): String {
        return "{$start, $finish}"
    }
}

fun line(start: Point, finish: Point): Line {
    return LineImpl(start = start, finish = finish)
}

fun line(
    startX: Double,
    startY: Double,
    finishX: Double,
    finishY: Double,
): Line {
    return LineImpl(start = point(x = startX, y = startY), finish = point(x = finishX, y = finishY))
}
