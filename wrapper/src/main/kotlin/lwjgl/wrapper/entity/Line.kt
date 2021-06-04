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
