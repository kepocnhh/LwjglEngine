package lwjgl.engine.common

import lwjgl.engine.common.input.EngineInputState
import lwjgl.engine.common.input.FunctionKey
import lwjgl.engine.common.input.PrintableKey
import lwjgl.engine.common.input.toFunctionKeyOrNull
import lwjgl.engine.common.input.toPrintableKeyOrNull
import lwjgl.wrapper.entity.size
import lwjgl.wrapper.util.glfw.glfwGetWindowSize
import lwjgl.wrapper.util.glfw.key.KeyStatus
import lwjgl.wrapper.util.glfw.key.toKeyStatusOrNull
import lwjgl.wrapper.window.WindowSize
import lwjgl.wrapper.window.closeWindow
import lwjgl.wrapper.window.loopWindow
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWGamepadState
import java.util.concurrent.atomic.AtomicBoolean

private object MappingPS3 : EngineInputState.Joystick.Mapping {
    override fun getBufferIndex(
        side: EngineInputState.Joystick.Mapping.Side,
        type: EngineInputState.Joystick.Mapping.ValueType
    ): Int? {
        return when (side) {
            EngineInputState.Joystick.Mapping.Side.LEFT -> when (type) {
                EngineInputState.Joystick.Mapping.ValueType.JOY_X -> 0
                EngineInputState.Joystick.Mapping.ValueType.JOY_Y -> 1
                EngineInputState.Joystick.Mapping.ValueType.TRIGGER_POSITION -> null
            }
            EngineInputState.Joystick.Mapping.Side.RIGHT -> when (type) {
                EngineInputState.Joystick.Mapping.ValueType.JOY_X -> 2
                EngineInputState.Joystick.Mapping.ValueType.JOY_Y -> 3
                EngineInputState.Joystick.Mapping.ValueType.TRIGGER_POSITION -> null
            }
        }
    }

    override fun getBufferIndex(
        side: EngineInputState.Joystick.Mapping.Side,
        type: EngineInputState.Joystick.Mapping.BooleanType
    ): Int? {
        TODO()
    }

    override fun getBufferIndex(button: EngineInputState.Joystick.Pad.Button): Int? {
        TODO()
    }
}
private object MappingPS4 : EngineInputState.Joystick.Mapping {
    override fun getBufferIndex(
        side: EngineInputState.Joystick.Mapping.Side,
        type: EngineInputState.Joystick.Mapping.ValueType
    ): Int? {
        return when (side) {
            EngineInputState.Joystick.Mapping.Side.LEFT -> when (type) {
                EngineInputState.Joystick.Mapping.ValueType.JOY_X -> 0
                EngineInputState.Joystick.Mapping.ValueType.JOY_Y -> 1
                EngineInputState.Joystick.Mapping.ValueType.TRIGGER_POSITION -> 3
            }
            EngineInputState.Joystick.Mapping.Side.RIGHT -> when (type) {
                EngineInputState.Joystick.Mapping.ValueType.JOY_X -> 2
                EngineInputState.Joystick.Mapping.ValueType.JOY_Y -> 5
                EngineInputState.Joystick.Mapping.ValueType.TRIGGER_POSITION -> 4
            }
        }
    }

    override fun getBufferIndex(
        side: EngineInputState.Joystick.Mapping.Side,
        type: EngineInputState.Joystick.Mapping.BooleanType
    ): Int? {
        TODO()
    }

    override fun getBufferIndex(button: EngineInputState.Joystick.Pad.Button): Int? {
        TODO()
    }
}

private class MutableEngineInputKeyboardState : EngineInputState.Keyboard {
    override val printableKeys: MutableMap<PrintableKey, KeyStatus> = mutableMapOf<PrintableKey, KeyStatus>().also {
        PrintableKey.values().forEach { keyType ->
            it[keyType] = KeyStatus.RELEASE
        }
    }
    override val functionKeys: MutableMap<FunctionKey, KeyStatus> = mutableMapOf<FunctionKey, KeyStatus>().also {
        FunctionKey.values().forEach { keyType ->
            it[keyType] = KeyStatus.RELEASE
        }
    }
}

private class MutableEngineInputJoystickPad(
    override var triggerPosition: Double
) : EngineInputState.Joystick.Pad {
    override val joy: MutableJoy = MutableJoy(x = 0.0, y = 0.0, isPressed = false)
    val buttons = mutableMapOf<EngineInputState.Joystick.Pad.Button, Boolean>()
    override fun isPressed(button: EngineInputState.Joystick.Pad.Button): Boolean {
        return buttons[button] ?: false
    }
}

private class MutableEngineInputJoystickState(
    override val id: String,
    override val name: String
) : EngineInputState.Joystick {
    override val leftPad: MutableEngineInputJoystickPad = MutableEngineInputJoystickPad(triggerPosition = 0.0)
    override val rightPad: MutableEngineInputJoystickPad = MutableEngineInputJoystickPad(triggerPosition = 0.0)
}

private class MutableJoy(
    override var x: Double,
    override var y: Double,
    override var isPressed: Boolean
) : EngineInputState.Joystick.Joy

private class MutableEngineInputState: EngineInputState {
    override val keyboard: MutableEngineInputKeyboardState = MutableEngineInputKeyboardState()
    override val joysticks: MutableList<MutableEngineInputJoystickState?> = MutableList(size = 2) { null }
}

private fun onKeyCallback(
    logic: EngineLogic,
    mutableEngineInputState: MutableEngineInputState,
    keyStatus: KeyStatus,
    key: Int
) {
    val printableKey = key.toPrintableKeyOrNull()
    if(printableKey != null) {
        mutableEngineInputState.keyboard.printableKeys[printableKey] = keyStatus
        logic.engineInputCallback.onPrintableKey(printableKey, keyStatus)
        return
    }
    val functionKey = key.toFunctionKeyOrNull()
    if(functionKey != null) {
        mutableEngineInputState.keyboard.functionKeys[functionKey] = keyStatus
        logic.engineInputCallback.onFunctionKey(functionKey, keyStatus)
        return
    }
}

private fun onJoysticks(
    joysticks: MutableList<MutableEngineInputJoystickState?>
) {
    setOf(
        GLFW.GLFW_JOYSTICK_1,
        GLFW.GLFW_JOYSTICK_2
    ).forEach {
        onJoystick(joystickId = it, joysticks = joysticks)
    }
    // todo
}
private fun onJoystick(
    joystick: MutableEngineInputJoystickState,
    mapping: EngineInputState.Joystick.Mapping,
    buttons: ByteArray,
    axes: FloatArray
) {
    mapping.getBufferIndex(
        side = EngineInputState.Joystick.Mapping.Side.LEFT,
        type = EngineInputState.Joystick.Mapping.ValueType.JOY_X
    )?.also {
        joystick.leftPad.joy.x = axes[it].toDouble()
    }
    mapping.getBufferIndex(
        side = EngineInputState.Joystick.Mapping.Side.LEFT,
        type = EngineInputState.Joystick.Mapping.ValueType.JOY_Y
    )?.also {
        joystick.leftPad.joy.y = axes[it].toDouble()
    }
    mapping.getBufferIndex(
        side = EngineInputState.Joystick.Mapping.Side.RIGHT,
        type = EngineInputState.Joystick.Mapping.ValueType.JOY_X
    )?.also {
        joystick.rightPad.joy.x = axes[it].toDouble()
    }
    mapping.getBufferIndex(
        side = EngineInputState.Joystick.Mapping.Side.RIGHT,
        type = EngineInputState.Joystick.Mapping.ValueType.JOY_Y
    )?.also {
        joystick.rightPad.joy.y = axes[it].toDouble()
    }

    mapping.getBufferIndex(
        side = EngineInputState.Joystick.Mapping.Side.LEFT,
        type = EngineInputState.Joystick.Mapping.ValueType.TRIGGER_POSITION
    )?.also {
        joystick.leftPad.triggerPosition = axes[it].toDouble()
    }
    mapping.getBufferIndex(
        side = EngineInputState.Joystick.Mapping.Side.RIGHT,
        type = EngineInputState.Joystick.Mapping.ValueType.TRIGGER_POSITION
    )?.also {
        joystick.rightPad.triggerPosition = axes[it].toDouble()
    }
}
private fun onJoystick(
    joystickId: Int,
    joysticks: MutableList<MutableEngineInputJoystickState?>
) {
    val isJoystickPresent = GLFW.glfwJoystickPresent(joystickId)
    val joystickGUID = GLFW.glfwGetJoystickGUID(joystickId)
    val joystickName = GLFW.glfwGetJoystickName(joystickId)
    val gamepadName = GLFW.glfwGetGamepadName(joystickId)
    val mapping = gamepadName?.let {
        when {
            it.contains("PS4") -> MappingPS4
            it.contains("PS3") -> MappingPS3
            else -> null
        }
    }
    val joystickButtons = GLFW.glfwGetJoystickButtons(joystickId)?.let {
        val array = ByteArray(it.remaining())
        it.get(array)
        array
    }
    val joystickAxes = GLFW.glfwGetJoystickAxes(joystickId)?.let {
        val array = FloatArray(it.remaining())
        it.get(array)
        array
    }
    if(!isJoystickPresent ||
        joystickGUID == null ||
        joystickName == null ||
        joystickButtons == null ||
        joystickAxes == null ||
        mapping == null) {
        joysticks[joystickId] = null
        return
    }
    val mutableJoystick = joysticks[joystickId]?.let {
        when (it.id) {
            joystickGUID -> it
            else -> null
        }
    } ?: MutableEngineInputJoystickState(id = joystickGUID, name = joystickName).also {
        println("joystick #$joystickId id: $joystickGUID name: $joystickName / $gamepadName")
        joysticks[joystickId] = it
    }
    onJoystick(
        joystick = mutableJoystick,
        mapping = mapping,
        buttons = joystickButtons,
        axes = joystickAxes
    )
}

object Engine {
    fun run(logic: EngineLogic) {
        val isWindowClosed = AtomicBoolean(false)
        val mutableEngineInputState = MutableEngineInputState()
        var timeLogicLast = 0.0
        var timeRenderLast = 0.0
        loopWindow(
            windowSize = WindowSize.Exact(size = size(width = 640, height = 480)),
            title = "Engine",
            onKeyCallback = { _, key: Int, _, action: Int, _ ->
                when (val keyStatus = action.toKeyStatusOrNull()) {
                    KeyStatus.REPEAT, null -> {/* ignored */}
                    else -> onKeyCallback(logic, mutableEngineInputState, keyStatus, key)
                }
            },
            onWindowCloseCallback = {
                isWindowClosed.set(true)
            },
            onPreLoop = { windowId ->
//                val gamepadMappings = ResourceProvider.requireResourceAsStream("gamepad_mappings").readAllBytes()
//                val buffer = ByteBuffer.allocateDirect(gamepadMappings.size + 1)
//                buffer.put(gamepadMappings)
//                buffer.rewind()
//                GLFW.glfwUpdateGamepadMappings(buffer)
                logic.onPreLoop()
                // todo
            },
            onPostLoop = {
                // todo
            },
            onRender = { windowId, canvas ->
                val timeNowLogic = System.nanoTime().toDouble()
//                onJoysticks(mutableEngineInputState.joysticks) // todo
                val pictureSize = glfwGetWindowSize(windowId)
                logic.onUpdateState(
                    engineInputState = mutableEngineInputState,
                    engineProperty = engineProperty(
                        timeLast = timeLogicLast,
                        timeNow = timeNowLogic,
                        pictureSize = pictureSize
                    )
                )
                timeLogicLast = timeNowLogic
                val timeNowRender = System.nanoTime().toDouble()
                logic.onRender(
                    canvas,
                    engineInputState = mutableEngineInputState,
                    engineProperty = engineProperty(
                        timeLast = timeRenderLast,
                        timeNow = timeNowRender,
                        pictureSize = pictureSize
                    )
                )
                timeRenderLast = timeNowRender
                if(logic.shouldEngineStop) {
                    closeWindow(windowId)
                }
            }
        )
    }
}
