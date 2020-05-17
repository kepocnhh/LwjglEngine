package lwjgl.wrapper.util.awt

import java.awt.Graphics2D
import java.awt.RenderingHints

fun <T: Graphics2D, R: Any> T.use(action: (T) -> R): R {
    try {
        return action(this)
    } finally {
        dispose()
    }
}

fun Graphics2D.setAntiAliasing(antiAliasing: Boolean) {
    setRenderingHint(
        RenderingHints.KEY_ANTIALIASING,
        if(antiAliasing) RenderingHints.VALUE_ANTIALIAS_ON else RenderingHints.VALUE_ANTIALIAS_OFF
    )
}
