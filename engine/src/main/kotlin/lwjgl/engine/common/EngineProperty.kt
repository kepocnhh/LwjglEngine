package lwjgl.engine.common

import lwjgl.wrapper.entity.Size

interface EngineProperty {
    val timeLast: Long
    val timeNow: Long
    val pictureSize: Size
}

private data class EnginePropertyImpl(
    override val timeLast: Long,
    override val timeNow: Long,
    override val pictureSize: Size
) : EngineProperty

fun engineProperty(
    timeLast: Long,
    timeNow: Long,
    pictureSize: Size
) : EngineProperty {
    return EnginePropertyImpl(
        timeLast = timeLast,
        timeNow = timeNow,
        pictureSize = pictureSize
    )
}
