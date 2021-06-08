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
                EngineInputState.Joystick.Mapping.ValueType.TRIGGER_POSITION -> 2
            }
            EngineInputState.Joystick.Mapping.Side.RIGHT -> when (type) {
                EngineInputState.Joystick.Mapping.ValueType.JOY_X -> 3
                EngineInputState.Joystick.Mapping.ValueType.JOY_Y -> 4
                EngineInputState.Joystick.Mapping.ValueType.TRIGGER_POSITION -> 5
            }
        }
    }

    override fun getBufferIndex(
        side: EngineInputState.Joystick.Mapping.Side,
        button: EngineInputState.Joystick.Pad.Button
    ): Int? {
        return when (side) {
            EngineInputState.Joystick.Mapping.Side.LEFT -> when (button) {
                EngineInputState.Joystick.Pad.Button.UP -> 13
                EngineInputState.Joystick.Pad.Button.RIGHT -> 16
                EngineInputState.Joystick.Pad.Button.DOWN -> 14
                EngineInputState.Joystick.Pad.Button.LEFT -> 15
                EngineInputState.Joystick.Pad.Button.MAIN -> 8
                EngineInputState.Joystick.Pad.Button.BUMPER -> 4
                EngineInputState.Joystick.Pad.Button.JOY -> 11
            }
            EngineInputState.Joystick.Mapping.Side.RIGHT -> when (button) {
                EngineInputState.Joystick.Pad.Button.UP -> 2
                EngineInputState.Joystick.Pad.Button.RIGHT -> 1
                EngineInputState.Joystick.Pad.Button.DOWN -> 0
                EngineInputState.Joystick.Pad.Button.LEFT -> 3
                EngineInputState.Joystick.Pad.Button.MAIN -> 9
                EngineInputState.Joystick.Pad.Button.BUMPER -> 5
                EngineInputState.Joystick.Pad.Button.JOY -> 12
            }
        }
 
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
        button: EngineInputState.Joystick.Pad.Button
    ): Int? {
        return when (side) {
            EngineInputState.Joystick.Mapping.Side.LEFT -> when (button) {
                EngineInputState.Joystick.Pad.Button.UP -> 14
                EngineInputState.Joystick.Pad.Button.RIGHT -> 15
                EngineInputState.Joystick.Pad.Button.DOWN -> 16
                EngineInputState.Joystick.Pad.Button.LEFT -> 17
                EngineInputState.Joystick.Pad.Button.MAIN -> 8
                EngineInputState.Joystick.Pad.Button.BUMPER -> 4
                EngineInputState.Joystick.Pad.Button.JOY -> 10
            }
            EngineInputState.Joystick.Mapping.Side.RIGHT -> when (button) {
                EngineInputState.Joystick.Pad.Button.UP -> 3
                EngineInputState.Joystick.Pad.Button.RIGHT -> 2
                EngineInputState.Joystick.Pad.Button.DOWN -> 1
                EngineInputState.Joystick.Pad.Button.LEFT -> 0
                EngineInputState.Joystick.Pad.Button.MAIN -> 9
                EngineInputState.Joystick.Pad.Button.BUMPER -> 5
                EngineInputState.Joystick.Pad.Button.JOY -> 11
            }
        }
    }
}

private object MappingXBoxSeries : EngineInputState.Joystick.Mapping {
    override fun getBufferIndex(
        side: EngineInputState.Joystick.Mapping.Side,
        type: EngineInputState.Joystick.Mapping.ValueType
    ): Int? {
        return when (side) {
            EngineInputState.Joystick.Mapping.Side.LEFT -> when (type) {
                EngineInputState.Joystick.Mapping.ValueType.JOY_X -> 0
                EngineInputState.Joystick.Mapping.ValueType.JOY_Y -> 1
                EngineInputState.Joystick.Mapping.ValueType.TRIGGER_POSITION -> 5
            }
            EngineInputState.Joystick.Mapping.Side.RIGHT -> when (type) {
                EngineInputState.Joystick.Mapping.ValueType.JOY_X -> 2
                EngineInputState.Joystick.Mapping.ValueType.JOY_Y -> 3
                EngineInputState.Joystick.Mapping.ValueType.TRIGGER_POSITION -> 4
            }
        }
    }

    override fun getBufferIndex(
        side: EngineInputState.Joystick.Mapping.Side,
        button: EngineInputState.Joystick.Pad.Button
    ): Int? {
        return when (side) {
            EngineInputState.Joystick.Mapping.Side.LEFT -> when (button) {
                EngineInputState.Joystick.Pad.Button.UP -> 16
                EngineInputState.Joystick.Pad.Button.RIGHT -> 17
                EngineInputState.Joystick.Pad.Button.DOWN -> 18
                EngineInputState.Joystick.Pad.Button.LEFT -> 19
                EngineInputState.Joystick.Pad.Button.MAIN -> 10
                EngineInputState.Joystick.Pad.Button.BUMPER -> 6
                EngineInputState.Joystick.Pad.Button.JOY -> 13
            }
            EngineInputState.Joystick.Mapping.Side.RIGHT -> when (button) {
                EngineInputState.Joystick.Pad.Button.UP -> 4
                EngineInputState.Joystick.Pad.Button.RIGHT -> 1
                EngineInputState.Joystick.Pad.Button.DOWN -> 0
                EngineInputState.Joystick.Pad.Button.LEFT -> 3
                EngineInputState.Joystick.Pad.Button.MAIN -> 11
                EngineInputState.Joystick.Pad.Button.BUMPER -> 7
                EngineInputState.Joystick.Pad.Button.JOY -> 14
            }
        }
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
    override val joy: MutableJoy = MutableJoy(x = 0.0, y = 0.0)
    val buttons = mutableMapOf<EngineInputState.Joystick.Pad.Button, Boolean>()
    override fun isPressed(button: EngineInputState.Joystick.Pad.Button): Boolean {
        return buttons[button] ?: false
    }
}

private class MutableEngineInputJoystickState(
    override val id: String,
    override val name: String
) : EngineInputState.Joystick {
    override val pads: Map<EngineInputState.Joystick.Mapping.Side, MutableEngineInputJoystickPad> =
        EngineInputState.Joystick.Mapping.Side.values().map {
            it to MutableEngineInputJoystickPad(triggerPosition = 0.0)
        }.toMap()
}

private class MutableJoy(
    override var x: Double,
    override var y: Double
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
    println("""
        +
        buttons ${buttons.toList()}
        axes ${axes.toList()}
        -
    """.trimIndent())
    EngineInputState.Joystick.Mapping.Side.values().forEach { side ->
        val pad = joystick.pads[side] ?: TODO()
        mapping.getBufferIndex(
            side = side,
            type = EngineInputState.Joystick.Mapping.ValueType.JOY_X
        )?.also {
            pad.joy.x = axes[it].toDouble()
        }
        mapping.getBufferIndex(
            side = side,
            type = EngineInputState.Joystick.Mapping.ValueType.JOY_Y
        )?.also {
            pad.joy.y = axes[it].toDouble()
        }
        mapping.getBufferIndex(
            side = side,
            type = EngineInputState.Joystick.Mapping.ValueType.TRIGGER_POSITION
        )?.also {
            pad.triggerPosition = axes[it].toDouble()
        }
        EngineInputState.Joystick.Pad.Button.values().forEach { button ->
            mapping.getBufferIndex(
                side = side,
                button = button
            )?.also {
                pad.buttons[button] = buttons[it].toInt() == 1
            }
        }
    }
}

private enum class JoystickGUID(val values: Set<String>) {
    XBOX_SERIES_CONTROLLER(values = setOf(
        "030000005e040000130b000001050000",
        "050000005e040000130b000005050000"
    ))
}

private fun onJoystick(
    joystickId: Int,
    joysticks: MutableList<MutableEngineInputJoystickState?>
) {
    val isJoystickPresent = GLFW.glfwJoystickPresent(joystickId)
//    println("Joystick $joystickId is present: $isJoystickPresent")
    val joystickGUID = GLFW.glfwGetJoystickGUID(joystickId)
//    println("Joystick $joystickId GUID \"$joystickGUID\"")
    val joystickName = GLFW.glfwGetJoystickName(joystickId)
//    println("Joystick $joystickId name \"$joystickName\"")
    val gamepadName = GLFW.glfwGetGamepadName(joystickId)
//    println("Joystick $joystickId gamepad name \"$gamepadName\"")
    val mapping = joystickGUID?.let { guid ->
        JoystickGUID.values().firstOrNull { it.values.contains(guid) }?.let {
            when (it) {
                JoystickGUID.XBOX_SERIES_CONTROLLER -> MappingXBoxSeries
            }
        }
    } ?: gamepadName?.let {
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
            onKeyCallback = { _, key: Int, scanCode: Int, action: Int, _ ->
                println("on -> key callback: $key $scanCode $action")
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
                onJoysticks(mutableEngineInputState.joysticks) // todo
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
