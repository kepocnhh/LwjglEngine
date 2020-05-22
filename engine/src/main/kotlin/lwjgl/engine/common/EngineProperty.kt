package lwjgl.engine.common

import lwjgl.wrapper.entity.Size

interface EngineProperty {
    val timeLast: Double
    val timeNow: Double
    val pictureSize: Size
}

private data class EnginePropertyImpl(
    override val timeLast: Double,
    override val timeNow: Double,
    override val pictureSize: Size
) : EngineProperty

fun engineProperty(
    timeLast: Double,
    timeNow: Double,
    pictureSize: Size
) : EngineProperty {
    return EnginePropertyImpl(
        timeLast = timeLast,
        timeNow = timeNow,
        pictureSize = pictureSize
    )
}
