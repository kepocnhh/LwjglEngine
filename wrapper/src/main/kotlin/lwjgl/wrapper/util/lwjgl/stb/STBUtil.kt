package lwjgl.wrapper.util.lwjgl.stb

import lwjgl.wrapper.entity.Point
import lwjgl.wrapper.entity.Size
import lwjgl.wrapper.entity.point
import org.lwjgl.BufferUtils
import org.lwjgl.stb.*
import java.nio.ByteBuffer
import java.nio.FloatBuffer

fun getBakedQuad(
    buffer: STBTTBakedChar.Buffer,
    bufferSize: Size,
    charIndex: Int,
    xBuffer: FloatBuffer,
    yBuffer: FloatBuffer,
    alignedQuad: STBTTAlignedQuad,
    isOpenGLFillRule: Boolean = true
) {
    STBTruetype.stbtt_GetBakedQuad(
        buffer,
        bufferSize.width.toInt(),
        bufferSize.height.toInt(),
        charIndex,
        xBuffer,
        yBuffer,
        alignedQuad,
        isOpenGLFillRule
    )
}

fun getPackedQuad(
    buffer: STBTTPackedchar.Buffer,
    bufferSize: Size,
    charIndex: Int,
    xBuffer: FloatBuffer,
    yBuffer: FloatBuffer,
    alignedQuad: STBTTAlignedQuad,
    isAlignToInteger: Boolean = false
) {
    STBTruetype.stbtt_GetPackedQuad(
        buffer,
        bufferSize.width.toInt(), bufferSize.height.toInt(),
        charIndex, xBuffer, yBuffer, alignedQuad,
        isAlignToInteger
    )
}

fun initFont(
    info: STBTTFontinfo,
    buffer: ByteBuffer,
    onFailedAction: () -> Unit = {
        throw IllegalStateException("Failed to initialize font information.")
    }
) {
    val isSuccess = STBTruetype.stbtt_InitFont(info, buffer)
    if(!isSuccess) onFailedAction()
}

fun bakeFontBitmap(
    fontData: ByteBuffer,
    fontHeightInPixels: Float,
    pixels: ByteBuffer,
    bitmapSize: Size,
    firstCharacterToBake: Int,
    charData: STBTTBakedChar.Buffer
) {
    STBTruetype.stbtt_BakeFontBitmap(
        fontData,
        fontHeightInPixels,
        pixels,
        bitmapSize.width.toInt(),
        bitmapSize.height.toInt(),
        firstCharacterToBake,
        charData
    )
}

fun packFontRange(
    packContext: STBTTPackContext,
    fontByteBuffer: ByteBuffer,
    fontIndex: Int,
    fontSize: Float,
    firstUnicodeCharInRange: Int,
    charBufferForRange: STBTTPackedchar.Buffer
) {
    STBTruetype.stbtt_PackFontRange(
        packContext,
        fontByteBuffer,
        fontIndex,
        fontSize,
        firstUnicodeCharInRange,
        charBufferForRange
    )
}

data class FontVMetrics(
    val ascent: Int,
    val descent: Int,
    val lineGap: Int
)

fun getFontVMetrics(fontInfo: STBTTFontinfo): FontVMetrics {
    val ascentBuffer = BufferUtils.createIntBuffer(1)
    val descentBuffer = BufferUtils.createIntBuffer(1)
    val lineGapBuffer = BufferUtils.createIntBuffer(1)
    STBTruetype.stbtt_GetFontVMetrics(fontInfo, ascentBuffer, descentBuffer, lineGapBuffer)
    return FontVMetrics(
        ascent = ascentBuffer[0],
        descent = descentBuffer[0],
        lineGap = lineGapBuffer[0]
    )
}

data class Box(
    val point0: Point,
    val point1: Point
)

fun getCodepointBox(fontInfo: STBTTFontinfo, value: Char): Box {
    val x0Buffer = BufferUtils.createIntBuffer(1)
    val y0Buffer = BufferUtils.createIntBuffer(1)
    val x1Buffer = BufferUtils.createIntBuffer(1)
    val y1Buffer = BufferUtils.createIntBuffer(1)
    STBTruetype.stbtt_GetCodepointBox(fontInfo, value.toInt(), x0Buffer, y0Buffer, x1Buffer, y1Buffer)
    return Box(
        point0 = point(x = x0Buffer[0], y = y0Buffer[0]),
        point1 = point(x = x1Buffer[0], y = y1Buffer[0])
    )
}

fun getGlyphBox(fontInfo: STBTTFontinfo, value: Char): Box {
    val x0Buffer = BufferUtils.createIntBuffer(1)
    val y0Buffer = BufferUtils.createIntBuffer(1)
    val x1Buffer = BufferUtils.createIntBuffer(1)
    val y1Buffer = BufferUtils.createIntBuffer(1)
    STBTruetype.stbtt_GetGlyphBox(fontInfo, value.toInt(), x0Buffer, y0Buffer, x1Buffer, y1Buffer)
    return Box(
        point0 = point(x = x0Buffer[0], y = y0Buffer[0]),
        point1 = point(x = x1Buffer[0], y = y1Buffer[0])
    )
}
