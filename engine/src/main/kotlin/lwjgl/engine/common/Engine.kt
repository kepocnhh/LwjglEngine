package lwjgl.engine.common

import lwjgl.engine.common.input.EngineInputState
import lwjgl.engine.common.input.FunctionKey
import lwjgl.engine.common.input.PrintableKey
import lwjgl.engine.common.input.toBufferIndex
import lwjgl.engine.common.input.toFunctionKeyOrNull
import lwjgl.engine.common.input.toJoystickIndexOrNull
import lwjgl.engine.common.input.toPrintableKeyOrNull
import lwjgl.wrapper.entity.size
import lwjgl.wrapper.util.glfw.glfwGetWindowSize
import lwjgl.wrapper.util.glfw.key.KeyStatus
import lwjgl.wrapper.util.glfw.key.toKeyStatusOrNull
import lwjgl.wrapper.util.resource.ResourceProvider
import lwjgl.wrapper.window.WindowSize
import lwjgl.wrapper.window.closeWindow
import lwjgl.wrapper.window.loopWindow
import org.lwjgl.glfw.GLFW
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

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

private class MutableEngineInputJoystickButton: EngineInputState.Joystick.Button {
    override val interaction: MutableMap<EngineInputState.Joystick.Button.Interaction, Boolean> = mutableMapOf<EngineInputState.Joystick.Button.Interaction, Boolean>().also {
        EngineInputState.Joystick.Button.Interaction.values().forEach { button ->
            it[button] = false
        }
    }
    override val directional: MutableMap<EngineInputState.Joystick.Button.Directional, Boolean> = mutableMapOf<EngineInputState.Joystick.Button.Directional, Boolean>().also {
        EngineInputState.Joystick.Button.Directional.values().forEach { button ->
            it[button] = false
        }
    }
    override val main: MutableMap<EngineInputState.Joystick.Button.Main, Boolean> = mutableMapOf<EngineInputState.Joystick.Button.Main, Boolean>().also {
        EngineInputState.Joystick.Button.Main.values().forEach { button ->
            it[button] = false
        }
    }
    override var bumperLeft: Boolean = false
    override var bumperRight: Boolean = false
}

private class MutableEngineInputJoystickTrigger(override var position: Double) : EngineInputState.Joystick.Trigger

private class MutableEngineInputJoystickState(
    override val id: String,
    override val name: String
) : EngineInputState.Joystick {
    override val button: MutableEngineInputJoystickButton = MutableEngineInputJoystickButton()
    override val joyLeft: MutableJoy = MutableJoy(x = 0.0, y = 0.0, isPressed = false)
    override val joyRight: MutableJoy = MutableJoy(x = 0.0, y = 0.0, isPressed = false)
    override val triggerLeft = MutableEngineInputJoystickTrigger(position = 0.0)
    override val triggerRight = MutableEngineInputJoystickTrigger(position = 0.0)
}

private class MutableJoy(
    override var x: Double,
    override var y: Double,
    override var isPressed: Boolean
) : EngineInputState.Joystick.Joy

private class MutableEngineInputState: EngineInputState {
    override val keyboard: MutableEngineInputKeyboardState = MutableEngineInputKeyboardState()
    override val joysticks: MutableMap<EngineInputState.JoystickIndex, MutableEngineInputJoystickState> = mutableMapOf()
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
    mutableJoysticks: MutableMap<EngineInputState.JoystickIndex, MutableEngineInputJoystickState>
) {
    val joystickId = GLFW.GLFW_JOYSTICK_1
    onJoystick(joystickId, mutableJoysticks)
    // todo
}
private fun onJoystick(
    joystickId: Int,
    mutableJoysticks: MutableMap<EngineInputState.JoystickIndex, MutableEngineInputJoystickState>
) {
    val joystickIndex = joystickId.toJoystickIndexOrNull() ?: return
    val isJoystickPresent = GLFW.glfwJoystickPresent(joystickId)
    val joystickGUID = GLFW.glfwGetJoystickGUID(joystickId)
    val joystickName = GLFW.glfwGetJoystickName(joystickId)
    val gamepadName = GLFW.glfwGetGamepadName(joystickId)
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
        joystickAxes == null) {
        mutableJoysticks.remove(joystickIndex)
        return
    }
    val mutableJoystick = mutableJoysticks[joystickIndex]?.let {
        when (it.id) {
            joystickGUID -> it
            else -> null
        }
    } ?: MutableEngineInputJoystickState(id = joystickGUID, name = joystickName).also {
        println("$joystickIndex id: $joystickGUID name: $joystickName / $gamepadName")
        mutableJoysticks[joystickIndex] = it
    }
//    println("buttons: ${joystickButtons.mapIndexed { index, byte -> "$index:$byte"}.toList()}")
//    println("axes: ${joystickAxes.toList()}")
    mutableJoystick.joyLeft.x = joystickAxes[GLFW.GLFW_GAMEPAD_AXIS_LEFT_X].toDouble()
    mutableJoystick.joyLeft.y = joystickAxes[GLFW.GLFW_GAMEPAD_AXIS_LEFT_Y].toDouble()
    mutableJoystick.joyLeft.isPressed = joystickButtons[8].toInt() == 1
    mutableJoystick.joyRight.x = joystickAxes[GLFW.GLFW_GAMEPAD_AXIS_RIGHT_X].toDouble()
    mutableJoystick.joyRight.y = joystickAxes[GLFW.GLFW_GAMEPAD_AXIS_RIGHT_Y].toDouble()
    mutableJoystick.joyRight.isPressed = joystickButtons[9].toInt() == 1
    mutableJoystick.triggerLeft.position = joystickAxes[GLFW.GLFW_GAMEPAD_AXIS_LEFT_TRIGGER].toDouble()
    mutableJoystick.triggerRight.position = joystickAxes[GLFW.GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER].toDouble()
    mutableJoystick.button.bumperLeft = joystickButtons[GLFW.GLFW_GAMEPAD_BUTTON_LEFT_BUMPER].toInt() == 1
    mutableJoystick.button.bumperRight = joystickButtons[GLFW.GLFW_GAMEPAD_BUTTON_RIGHT_BUMPER].toInt() == 1
    EngineInputState.Joystick.Button.Interaction.values().forEach {
        mutableJoystick.button.interaction[it] = joystickButtons[it.toBufferIndex()].toInt() == 1
    }
    EngineInputState.Joystick.Button.Directional.values().forEach {
        mutableJoystick.button.directional[it] = joystickButtons[it.toBufferIndex()].toInt() == 1
    }
    EngineInputState.Joystick.Button.Main.values().forEach {
        mutableJoystick.button.main[it] = joystickButtons[it.toBufferIndex()].toInt() == 1
    }
//    val joystickAxes = GLFW.glfwGetJoystickAxes(joystickId)
}

object Engine {
    fun run(logic: EngineLogic) {
        val isWindowClosed = AtomicBoolean(false)
        val mutableEngineInputState = MutableEngineInputState()
        var timeLogicLast = 0L
        var timeRenderLast = 0L
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
                val timeNowLogic = System.nanoTime()
                onJoysticks(mutableEngineInputState.joysticks)
                logic.onUpdateState(
                    engineInputState = mutableEngineInputState,
                    engineProperty = engineProperty(
                        timeLast = timeLogicLast,
                        timeNow = timeNowLogic,
                        pictureSize = glfwGetWindowSize(windowId)
                    )
                )
                timeLogicLast = timeNowLogic
                val timeNowRender = System.nanoTime()
                logic.onRender(
                    canvas,
                    engineInputState = mutableEngineInputState,
                    engineProperty = engineProperty(
                        timeLast = timeRenderLast,
                        timeNow = timeNowRender,
                        pictureSize = glfwGetWindowSize(windowId)
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
