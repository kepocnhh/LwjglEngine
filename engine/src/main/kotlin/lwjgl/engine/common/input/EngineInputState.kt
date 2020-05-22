package lwjgl.engine.common.input

import lwjgl.wrapper.util.glfw.key.KeyStatus

interface EngineInputState {
    interface Keyboard {
        val printableKeys: Map<PrintableKey, KeyStatus>
        val functionKeys: Map<FunctionKey, KeyStatus>
    }

    val keyboard: Keyboard

    interface Joystick {
        val id: String
        val name: String

        interface Button {
            enum class Interaction {
                A,
                B,
                X,
                Y,
            }
            val interaction: Map<Interaction, Boolean>
            enum class Directional {
                UP,
                RIGHT,
                DOWN,
                LEFT,
            }
            val directional: Map<Directional, Boolean>
            val bumperLeft: Boolean
            val bumperRight: Boolean
            enum class Main {
                BACK,
                START,
            }
            val main: Map<Main, Boolean>
        }
        val button: Button

        interface Joy {
            val x: Double
            val y: Double
            val isPressed: Boolean
        }
        val joyLeft: Joy
        val joyRight: Joy

        interface Trigger {
            val position: Double
        }
        val triggerLeft: Trigger
        val triggerRight: Trigger

        interface Mapping {
            enum class Side {
                LEFT, RIGHT
            }
            enum class ValueType {
                JOY_X,
                JOY_Y,
                TRIGGER_POSITION,
            }
            enum class BooleanType {
                JOY_PRESSED,
                BUMPER_PRESSED,
            }
            fun getBufferIndex(side: Side, type: ValueType): Int?
            fun getBufferIndex(side: Side, type: BooleanType): Int?
            fun getBufferIndex(button: Button.Interaction): Int?
            fun getBufferIndex(button: Button.Directional): Int?
            fun getBufferIndex(button: Button.Main): Int?
        }
    }
    val joysticks: List<Joystick?>
}
