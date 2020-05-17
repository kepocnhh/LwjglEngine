package lwjgl.engine.common.input

import lwjgl.wrapper.util.glfw.key.KeyStatus

interface EngineInputCallback {
    fun onPrintableKey(key: PrintableKey, status: KeyStatus)
    fun onFunctionKey(key: FunctionKey, status: KeyStatus)
}
