package lwjgl.wrapper.entity

interface Size {
    val width: Double
    val height: Double
}

private data class SizeImpl(
    override val width: Double,
    override val height: Double
): Size

fun size(
    width: Double,
    height: Double
): Size {
    return SizeImpl(
        width = width,
        height = height
    )
}

fun size(
    width: Int,
    height: Int
): Size {
    return size(width = width.toDouble(), height = height.toDouble())
}

fun square(size: Double) = size(width = size, height = size)
val sizeEmpty = size(0, 0)