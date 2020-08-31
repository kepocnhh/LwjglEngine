package lwjgl.game.roguelike.util

import kotlin.math.pow

object MathUtil {
    fun compare(a: Double, b: Double, precision: Int): Int {
        val threshold = 10.0.pow(-precision)
        val d = a - b
        if (d < threshold) return 0
        if (d < 0) return -1
        return 1
    }
}

fun Double.isLessThan(value: Double, precision: Int): Boolean {
    return MathUtil.compare(this, value, precision) < 0
}
