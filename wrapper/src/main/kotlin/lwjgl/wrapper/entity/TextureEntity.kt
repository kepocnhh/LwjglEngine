package lwjgl.wrapper.entity

import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL13.GL_CLAMP_TO_BORDER
import java.nio.ByteBuffer

interface Texture {
    val size: Size
    fun bind()
    fun delete()
}

private object EmptyTexture: Texture {
    override val size = sizeEmpty
    override fun bind() = Unit
    override fun delete() = Unit
}

fun texture2D(size: Size, data: ByteBuffer): Texture {
//    val result = Texture2D(size)
//    glTexImage2D(
//        result.id, 0, GL_RGBA8, size.width, size.height, 0, GL_RGBA, GL_UNSIGNED_BYTE, data
//    )
//    return result
    return EmptyTexture
}

private abstract class AbstractTexture(
    override val size: Size,
    private val target: Int,
    parameters: Map<Int, Int>
): Texture {
    val id = glGenTextures()

    init {
        glBindTexture(target, id)
        parameters.forEach { (key, value) ->
            glTexParameteri(target, key, value)
        }
    }

    override fun bind() {
        glBindTexture(target, id)
    }

    override fun delete() {
        glDeleteTextures(id)
    }
}

private val TEXTURE_2D_PARAMETERS = mapOf(
    GL_TEXTURE_WRAP_S to GL_CLAMP_TO_BORDER,
    GL_TEXTURE_WRAP_T to GL_CLAMP_TO_BORDER,
    GL_TEXTURE_MIN_FILTER to GL_NEAREST,
    GL_TEXTURE_MAG_FILTER to GL_NEAREST
)

private class Texture2D(size: Size): AbstractTexture(
    size,
    GL_TEXTURE_2D,
    TEXTURE_2D_PARAMETERS
)
