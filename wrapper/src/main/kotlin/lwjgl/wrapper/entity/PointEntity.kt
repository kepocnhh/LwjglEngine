package lwjgl.wrapper.entity

interface Point {
    val x: Double
    val y: Double
}

private data class PointImpl(
    override val x: Double,
    override val y: Double
): Point

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