package lwjgl.game.roguelike.util

enum class TimeUnit {
    NANOSECONDS {
        override fun convert(time: Double, timeUnit: TimeUnit): Double {
            return when (timeUnit) {
                NANOSECONDS -> time
                SECONDS -> time / (C3 / C0)
            }
        }
    },
    SECONDS {
        override fun convert(time: Double, timeUnit: TimeUnit): Double {
            return when (timeUnit) {
                NANOSECONDS -> proxy(time = time, multiple = C3/C0, over = Double.MAX_VALUE/(C3/C0))
                SECONDS -> time
            }
        }
    }
    ;

    companion object {
        const val C0 = 1.0
        const val C1 = C0 * 1000.0
        const val C2 = C1 * 1000.0
        const val C3 = C2 * 1000.0
        const val C4 = C3 * 60.0
        const val C5 = C4 * 60.0
        const val C6 = C5 * 24.0

        private fun proxy(time: Double, multiple: Double, over: Double): Double {
            return when {
                time >  over -> Double.MAX_VALUE
                time < -over -> Double.MIN_VALUE
                else -> time * multiple
            }
        }
    }

    open fun convert(time: Double, timeUnit: TimeUnit): Double {
        throw AbstractMethodError()
    }
}
