package lwjgl.wrapper.entity

object ColorEntity {
    const val MAX_VALUE = 1f
    const val MIN_VALUE = 0f

    val BLACK = color(MIN_VALUE, MIN_VALUE, MIN_VALUE)
    val RED = color(MAX_VALUE, MIN_VALUE, MIN_VALUE)
    val YELLOW = color(MAX_VALUE, MAX_VALUE, MIN_VALUE)
    val GREEN = color(MIN_VALUE, MAX_VALUE, MIN_VALUE)
    val BLUE = color(MIN_VALUE, MIN_VALUE, MAX_VALUE)
    val BLUE1 = color(MIN_VALUE, MAX_VALUE, MAX_VALUE)
    val WHITE = color(MAX_VALUE, MAX_VALUE, MAX_VALUE)
    val TRANSPARENT = color(MIN_VALUE, MIN_VALUE, MIN_VALUE, alpha = MIN_VALUE)
}

interface Color {
    val red: Float
    val green: Float
    val blue: Float
    val alpha: Float
}

private data class ColorImpl(
    override val red: Float,
    override val green: Float,
    override val blue: Float,
    override val alpha: Float
): Color

fun color(
    red: Float,
    green: Float,
    blue: Float,
    alpha: Float = ColorEntity.MAX_VALUE
): Color {
    val expectedRange = ColorEntity.MIN_VALUE..ColorEntity.MAX_VALUE
    mapOf(
        "red" to red,
        "green" to green,
        "red" to red
    ).forEach { (key, value) ->
        check(value in expectedRange) {
            "color $key value must be in $expectedRange"
        }
    }
    check(alpha in expectedRange) {
        "alpha value must be in $expectedRange"
    }
    return ColorImpl(
        red = red,
        green = green,
        blue = blue,
        alpha = alpha
    )
}
