package lwjgl.wrapper.entity

import lwjgl.wrapper.util.glfw.opengl.glColorOf
import lwjgl.wrapper.util.glfw.opengl.glTexImage2D
import lwjgl.wrapper.util.glfw.opengl.glTransaction
import lwjgl.wrapper.util.glfw.opengl.glVertexOf
import lwjgl.wrapper.util.io.use
import lwjgl.wrapper.util.lwjgl.createByteBuffer
import lwjgl.wrapper.util.lwjgl.ioResourceToByteBuffer
import lwjgl.wrapper.util.lwjgl.stb.*
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.stb.*
import org.lwjgl.system.MemoryUtil
import java.io.FileInputStream
import java.io.PrintStream
import java.nio.IntBuffer
import kotlin.math.absoluteValue
import org.lwjgl.system.MemoryStack

interface FontRender {
    fun drawText(
        fullPathFont: String,
        fontHeight: Float,
        pointTopLeft: Point,
        color: Color,
        text: CharSequence
    )

    fun getTextWidth(
        fullPathFont: String,
        fontHeight: Float,
        text: CharSequence, from: Int, to: Int
    ): Double
}

fun FontRender.getTextWidth(
    fullPathFont: String,
    fontHeight: Float,
    text: CharSequence
): Double {
    return getTextWidth(
        fullPathFont = fullPathFont,
        fontHeight = fontHeight,
        text = text,
        from = 0,
        to = text.length
    )
}

fun fontRender(): FontRender {
    return AdvancedFontRender()
}

private const val firstUnicodeCharInRange = 32
private const val charBufferLimit = Char.MAX_VALUE.toInt()
private val bufferSize = size(2048, 2048)

private class FontInfo(
    val textureId: Int,
    val lineHeight: Float,
    val fontHeight: Float,
    val charBuffer: STBTTPackedchar.Buffer,
    val info: STBTTFontinfo
)

private val mapFontInfo = mutableMapOf<String, FontInfo>()

private fun drawAlignedQuad(alignedQuad: STBTTAlignedQuad) {
    GL11.glTexCoord2f(
        alignedQuad.s0(),
        alignedQuad.t0()
    )
    glVertexOf(
        alignedQuad.x0(),
        alignedQuad.y0()
    )
    GL11.glTexCoord2f(
        alignedQuad.s1(),
        alignedQuad.t0()
    )
    glVertexOf(
        alignedQuad.x1(),
        alignedQuad.y0()
    )
    GL11.glTexCoord2f(
        alignedQuad.s1(),
        alignedQuad.t1()
    )
    glVertexOf(
        alignedQuad.x1(),
        alignedQuad.y1()
    )
    GL11.glTexCoord2f(
        alignedQuad.s0(),
        alignedQuad.t1()
    )
    glVertexOf(
        alignedQuad.x0(),
        alignedQuad.y1()
    )
}

private class AdvancedFontRender: FontRender {
    private fun drawText(
        fontInfo: FontInfo,
        pointTopLeft: Point,
        color: Color,
        text: CharSequence
    ) {
        val x = pointTopLeft.x
        val y = pointTopLeft.y + fontInfo.lineHeight

        val xBuffer = BufferUtils.createFloatBuffer(1)
        val yBuffer = BufferUtils.createFloatBuffer(1)
        xBuffer.put(0, x.toFloat())
        yBuffer.put(0, y.toFloat())

//        GL11.glEnable(GL11.GL_BLEND)
//        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, fontInfo.textureId)

        glColorOf(color)

        STBTTAlignedQuad.malloc().use { alignedQuad ->
            glTransaction(GL11.GL_QUADS) {
                for(c in text.toString()) {
                    if(c == '\n') {
                        yBuffer.put(0, yBuffer.get(0) + fontInfo.fontHeight)
                        xBuffer.put(0, 0f)
                        continue
                    } else if(c < firstUnicodeCharInRange.toChar()) {
                        continue
                    }
                    getPackedQuad(
                        buffer = fontInfo.charBuffer,
                        bufferSize = bufferSize,
                        charIndex = c.toInt(),
                        xBuffer = xBuffer,
                        yBuffer = yBuffer,
                        alignedQuad = alignedQuad
                    )
                    drawAlignedQuad(alignedQuad)
                }
            }
        }

        GL11.glDisable(GL11.GL_TEXTURE_2D)
//        GL11.glDisable(GL11.GL_BLEND)
    }

    private fun createFrontInfo(fullPathFont: String, fontHeight: Float): FontInfo {
        println("create font by path: $fullPathFont with size: $fontHeight")
        val source = FileInputStream(fullPathFont)

        val fontByteBuffer = ioResourceToByteBuffer(source, 1024)
        val pixels = createByteBuffer(bufferSize)
        val fontInfo = STBTTFontinfo.create()
        STBTruetype.stbtt_InitFont(fontInfo, fontByteBuffer)
        val charBuffer = STBTTPackedchar.malloc(charBufferLimit)
        STBTTPackContext.malloc().use { packContext ->
            STBTruetype.stbtt_PackBegin(
                packContext, pixels,
                bufferSize.width.toInt(), bufferSize.height.toInt(),
                0, 1, MemoryUtil.NULL
            )
            charBuffer.limit(charBufferLimit)
            charBuffer.position(firstUnicodeCharInRange)

            STBTruetype.stbtt_PackSetOversampling(packContext, 2, 2)

            packFontRange(
                packContext,
                fontByteBuffer = fontByteBuffer,
                fontIndex = 0,
                fontSize = fontHeight,
                firstUnicodeCharInRange = firstUnicodeCharInRange,
                charBufferForRange = charBuffer
            )
            charBuffer.clear()
            STBTruetype.stbtt_PackEnd(packContext)
        }
        val textureId = GL11.glGenTextures()
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId)
        glTexImage2D(
            textureTarget = GL11.GL_TEXTURE_2D,
            textureInternalFormat = GL11.GL_ALPHA,
            textureSize = bufferSize,
            texelDataFormat = GL11.GL_ALPHA,
            texelDataType = GL11.GL_UNSIGNED_BYTE,
            pixels = pixels
        )
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR)

        val fontVMetrics = getFontVMetrics(fontInfo)
        println("fontVMetrics: $fontVMetrics")
        val lineHeight = fontHeight/(fontVMetrics.ascent.toFloat() - fontVMetrics.descent.toFloat() - fontVMetrics.lineGap.toFloat()/2)*fontVMetrics.ascent.toFloat()
        println("lineHeight: $lineHeight")

        return FontInfo(
            textureId = textureId,
            lineHeight = lineHeight,
            fontHeight = fontHeight,
            charBuffer = charBuffer,
            info = fontInfo
        )
    }

    override fun drawText(
        fullPathFont: String,
        fontHeight: Float,
        pointTopLeft: Point,
        color: Color,
        text: CharSequence
    ) {
        val fontKey = fullPathFont + "_" + fontHeight
        val fontInfo = mapFontInfo[fontKey] ?: createFrontInfo(fullPathFont, fontHeight).also {
            mapFontInfo[fontKey] = it
        }
        drawText(fontInfo, pointTopLeft, color, text)
    }

    private fun getCodePoint(
        text: CharSequence,
        to: Int,
        i: Int,
        cpOut: IntBuffer
    ): Int {
        val c1 = text[i]
        if (Character.isHighSurrogate(c1) && i + 1 < to) {
            val c2 = text[i + 1]
            if (Character.isLowSurrogate(c2)) {
                cpOut.put(0, Character.toCodePoint(c1, c2))
                return 2
            }
        }
        cpOut.put(0, c1.code)
        return 1
    }
    // https://github.com/LWJGL/lwjgl3/blob/7ef51d1054aa4647bc8aafcfd3d93c6a6ad1410f/modules/samples/src/test/java/org/lwjgl/demo/stb/Truetype.java#L219
    override fun getTextWidth(fullPathFont: String, fontHeight: Float, text: CharSequence, from: Int, to: Int): Double {
        val fontKey = fullPathFont + "_" + fontHeight
        val fontInfo = mapFontInfo[fontKey] ?: createFrontInfo(fullPathFont, fontHeight).also {
            mapFontInfo[fontKey] = it
        }
        var result = 0.0
        MemoryStack.stackPush().use {
            val pCodePoint       = it.mallocInt(1)
            val pAdvancedWidth   = it.mallocInt(1)
            val pLeftSideBearing = it.mallocInt(1)
            var i = from
            while (i < to) {
                i += getCodePoint(text, to, i, pCodePoint)
                val cp = pCodePoint.get(0)
                STBTruetype.stbtt_GetCodepointHMetrics(fontInfo.info, cp, pAdvancedWidth, pLeftSideBearing)
                result += pAdvancedWidth.get(0)
            }
        }
        return result * STBTruetype.stbtt_ScaleForPixelHeight(fontInfo.info, fontHeight)
    }
}
