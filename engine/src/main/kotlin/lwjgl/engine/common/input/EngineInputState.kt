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

        interface Joy {
            val x: Double
            val y: Double
            val isPressed: Boolean
        }

        interface Pad {
            enum class Button {
                UP,
                RIGHT,
                DOWN,
                LEFT,
                MAIN,
                BUMPER,
            }

            val triggerPosition: Double
            val joy: Joy
            fun isPressed(button: Button): Boolean
        }

        val leftPad: Pad
        val rightPad: Pad

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
            fun getBufferIndex(button: Pad.Button): Int?
        }
    }
    val joysticks: List<Joystick?>
}
