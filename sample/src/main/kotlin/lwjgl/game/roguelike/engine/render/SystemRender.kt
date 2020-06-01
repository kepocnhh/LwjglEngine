package lwjgl.game.roguelike.engine.render

import lwjgl.engine.common.EngineProperty
import lwjgl.game.roguelike.util.TimeUnit
import lwjgl.wrapper.canvas.Canvas
import lwjgl.wrapper.entity.ColorEntity
import lwjgl.wrapper.entity.point

class SystemRender(
    private val fullPathFont: String
) {
    fun onRender(
        canvas: Canvas,
        engineProperty: EngineProperty
    ) {
        val framesPerSecond = TimeUnit.NANO_IN_SECOND / (engineProperty.timeNow - engineProperty.timeLast)
        canvas.drawText(
            fullPathFont = fullPathFont,
            color = ColorEntity.GREEN,
            pointTopLeft = point(
                x = engineProperty.pictureSize.width - 50,
                y = engineProperty.pictureSize.height - 50
            ),
            text = String.format("%.1f", framesPerSecond),
            fontHeight = 16f
        )
    }
}
