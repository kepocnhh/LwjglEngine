package lwjgl.game.roguelike.engine.render

import lwjgl.engine.common.EngineProperty
import lwjgl.game.roguelike.state.State
import lwjgl.game.roguelike.util.StateUtil.getSortedItems
import lwjgl.wrapper.canvas.Canvas
import lwjgl.wrapper.canvas.drawCircle
import lwjgl.wrapper.entity.ColorEntity
import lwjgl.wrapper.entity.Size
import lwjgl.wrapper.entity.color
import lwjgl.wrapper.entity.point
import lwjgl.wrapper.entity.size
import lwjgl.wrapper.entity.updated

class JourneyPlayerRender(
    private val fullPathFont: String,
    private val playerSize: Size,
    private val pixelsPerUnit: Double
) {
    fun onRender(
        canvas: Canvas,
        player: State.Journey.Player,
        engineProperty: EngineProperty
    ) {
        val center = point(x = engineProperty.pictureSize.width / 2, y = engineProperty.pictureSize.height / 2)
        val playerPosition = point(
            x = center.x - playerSize.width / 2,
            y = center.y - playerSize.height /2
        )
        canvas.drawRectangle(
            color = ColorEntity.RED,
            pointTopLeft = playerPosition,
            size = playerSize,
            direction = player.directionActual,
            pointOfRotation = center,
            lineWidth = 1f
        )
        canvas.drawLine(
            color = ColorEntity.RED,
            pointStart = center,
            pointFinish = point(
                x = center.x,
                y = playerPosition.y
            ),
            lineWidth = 1f,
            direction = player.directionActual,
            pointOfRotation = center
        )
        canvas.drawPoint(
            color = ColorEntity.WHITE,
            point = playerPosition
        )
        canvas.drawPoint(
            color = ColorEntity.GREEN,
            point = center
        )
        canvas.drawText(
            fullPathFont = fullPathFont,
            color = ColorEntity.GREEN,
            pointTopLeft = point(0,0),
            text = player.position.toString(),
            fontHeight = 16f
        )
        canvas.drawText(
            fullPathFont = fullPathFont,
            color = ColorEntity.GREEN,
            pointTopLeft = point(0,25),
            text = String.format("%.1f", player.directionActual),
            fontHeight = 16f
        )
        when (val state = player.state) {
            State.Journey.PlayerState.MoveState -> {
                if (player.interactions.isNotEmpty()) {
                    val color = ColorEntity.GREEN
                    val point = point(x = center.x, y = center.y + playerSize.height * 1.5)
                    canvas.drawCircle(
                        color = color,
                        point = point,
                        radius = 0.35 * pixelsPerUnit,
                        edgeCount = 10,
                        lineWidth = 1f
                    )
                    val fontHeight = 12f
                    canvas.drawByText(
                        fullPathFont = fullPathFont,
                        fontHeight = fontHeight,
                        color = color,
                        text = "F"
                    ) { width ->
                        point.updated(dX = - width / 2, dY = - fontHeight / 2.0)
                    }
                }
            }
            is State.Journey.PlayerState.ExchangeStorageState -> {
                val storage = state.storage
                val padding = 32.0
                val width = engineProperty.pictureSize.width / 2 - padding * 1.5
                val height = engineProperty.pictureSize.height / 2
                canvas.drawRectangle(
                    color = ColorEntity.BLACK.updated(alpha = 0.5f),
                    pointTopLeft = point(x = 0.0, y = 0.0),
                    size = engineProperty.pictureSize
                )
                val y = center.y - height / 2
                val xStorage = padding
                canvas.drawRectangle(
                    colorBorder = ColorEntity.GREEN,
                    colorBackground = ColorEntity.BLACK,
                    pointTopLeft = point(x = xStorage, y = y),
                    size = size(width = width, height = height),
                    lineWidth = if (state.focusedStorage) 3f else 1f
                )
                val itemsStorage = storage.getSortedItems()
                for (i in itemsStorage.indices) {
                    val item = itemsStorage[i]
                    val fontHeight = 16f
                    val yItem = y + i * fontHeight.toDouble() + fontHeight / 2
                    canvas.drawText(
                        fullPathFont = fullPathFont,
                        color = ColorEntity.GREEN,
                        pointTopLeft = point(
                            x = xStorage + fontHeight,
                            y = yItem
                        ),
                        text = item.title,
                        fontHeight = fontHeight
                    )
                    if (state.focusedItem == item) {
                        canvas.drawLine(
                            color = ColorEntity.GREEN,
                            pointStart = point(x = xStorage + fontHeight / 2, y = yItem),
                            pointFinish = point(x = xStorage + fontHeight / 2, y = yItem + fontHeight),
                            lineWidth = 2f
                        )
                    }
                }
                val xPlayer = padding + width + padding
                canvas.drawRectangle(
                    colorBorder = ColorEntity.GREEN,
                    colorBackground = ColorEntity.BLACK,
                    pointTopLeft = point(x = xPlayer, y = y),
                    size = size(width = width, height = height),
                    lineWidth = if (state.focusedStorage) 1f else 3f
                )
                val itemsPlayer = player.getSortedItems()
                for (i in itemsPlayer.indices) {
                    val item = itemsPlayer[i]
                    val fontHeight = 16f
                    val yItem = y + i * fontHeight.toDouble() + fontHeight / 2
                    canvas.drawText(
                        fullPathFont = fullPathFont,
                        color = ColorEntity.GREEN,
                        pointTopLeft = point(
                            x = xPlayer + fontHeight,
                            y = yItem
                        ),
                        text = item.title,
                        fontHeight = fontHeight
                    )
                    if (state.focusedItem == item) {
                        canvas.drawLine(
                            color = ColorEntity.GREEN,
                            pointStart = point(x = xPlayer + fontHeight / 2, y = yItem),
                            pointFinish = point(x = xPlayer + fontHeight / 2, y = yItem + fontHeight),
                            lineWidth = 2f
                        )
                    }
                }
                // todo
            }
        }
    }
}
