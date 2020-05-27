package lwjgl.game.roguelike.util

enum class TimeUnit {
    NANOSECONDS {
        override fun convert(time: Double, timeUnit: TimeUnit): Double {
            return when (timeUnit) {
                NANOSECONDS -> time
                SECONDS -> time / NANO_IN_SECOND
            }
        }
    },
    SECONDS {
        override fun convert(time: Double, timeUnit: TimeUnit): Double {
            return when (timeUnit) {
                NANOSECONDS -> proxy(time = time, multiple = NANO_IN_SECOND, over = Double.MAX_VALUE / NANO_IN_SECOND)
                SECONDS -> time
            }
        }
    }
    ;

    companion object {
        private const val C1 = 1_000.0
        private const val C2 = C1 * 1_000.0
        const val NANO_IN_SECOND = C2 * 1_000.0
        private const val C4 = NANO_IN_SECOND * 60.0
        private const val C5 = C4 * 60.0
        private const val C6 = C5 * 24.0

        private fun proxy(time: Double, multiple: Double, over: Double): Double {
            return when {
                time > over -> Double.MAX_VALUE
                time <-over -> Double.MIN_VALUE
                else -> time * multiple
            }
        }
    }

    open fun convert(time: Double, timeUnit: TimeUnit): Double {
        throw AbstractMethodError()
    }
}
