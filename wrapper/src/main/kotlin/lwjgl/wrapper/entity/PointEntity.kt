package lwjgl.wrapper.entity

interface Point {
    val x: Double
    val y: Double
}

private class PointImpl(
    override val x: Double,
    override val y: Double
): Point {
    override fun toString(): String {
        val fX = String.format("%.1f", x)
        val fY = String.format("%.1f", y)
        return "{x:$fX,y:$fY}"
    }
}

fun point(
    x: Double,
    y: Double
): Point {
    return PointImpl(
        x = x,
        y = y
    )
}

fun point(
    x: Int,
    y: Int
): Point {
    return point(
        x = x.toDouble(),
        y = y.toDouble()
    )
}

fun Point.update(
    dX: Double,
    dY: Double
): Point {
    return point(
        x = x + dX,
        y = y + dY
    )
}
