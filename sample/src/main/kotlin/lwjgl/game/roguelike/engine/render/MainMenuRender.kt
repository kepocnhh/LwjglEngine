package lwjgl.game.roguelike.engine.render

import lwjgl.engine.common.EngineProperty
import lwjgl.game.roguelike.state.State
import lwjgl.wrapper.canvas.Canvas
import lwjgl.wrapper.entity.ColorEntity
import lwjgl.wrapper.entity.point

class MainMenuRender(
    private val fullPathFont: String,
    private val pixelsPerUnit: Double
) {
    fun onRender(
        canvas: Canvas,
        engineProperty: EngineProperty,
        mainMenu: State.MainMenu
    ) {
        val menuItemHeight = 1.5 * pixelsPerUnit
        val items = State.MainMenu.Item.values()
        val menuItemTopY = engineProperty.pictureSize.height / 2 - items.size * menuItemHeight / 2
        items.forEachIndexed { index, item ->
            val menuItemY = menuItemTopY + index * menuItemHeight
            if(item == mainMenu.selectedMenuItem) {
                canvas.drawLine(
                    color = ColorEntity.GREEN,
                    pointStart = point(x = menuItemHeight / 2, y = menuItemY),
                    pointFinish = point(x = menuItemHeight / 2, y = menuItemY + menuItemHeight),
                    lineWidth = 2f
                )
            }
            canvas.drawText(
                fullPathFont = fullPathFont,
                fontHeight = 16f,
                pointTopLeft = point(
                    x = menuItemHeight,
                    y = menuItemY + (menuItemHeight - 16f)/2
                ),
                color = ColorEntity.GREEN,
                text = item.name
            )
        }
    }
}
