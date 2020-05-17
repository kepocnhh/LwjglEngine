package lwjgl.wrapper.util.lwjgl.system

import org.lwjgl.system.MemoryUtil
import java.nio.ByteBuffer
import java.nio.FloatBuffer

fun <R: Any> allocateByteBuffer(size: Int, action: (ByteBuffer) -> R): R {
    val buffer = MemoryUtil.memAlloc(size) ?: error("Allocated memory error!")
    try {
        return action(buffer)
    } finally {
        MemoryUtil.memFree(buffer)
    }
}

fun <R: Any> allocateFloatBuffer(size: Int, action: (FloatBuffer) -> R): R {
    val buffer = MemoryUtil.memAllocFloat(size) ?: error("Allocated memory error!")
    try {
        return action(buffer)
    } finally {
        MemoryUtil.memFree(buffer)
    }
}
