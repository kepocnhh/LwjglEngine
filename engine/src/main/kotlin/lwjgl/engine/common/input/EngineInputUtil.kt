package lwjgl.engine.common.input

import org.lwjgl.glfw.GLFW

fun Int.toPrintableKeyOrNull(): PrintableKey? {
    return when (this) {
        GLFW.GLFW_KEY_Q -> PrintableKey.Q
        GLFW.GLFW_KEY_W -> PrintableKey.W
        GLFW.GLFW_KEY_E -> PrintableKey.E
        GLFW.GLFW_KEY_R -> PrintableKey.R
        GLFW.GLFW_KEY_T -> PrintableKey.T
        GLFW.GLFW_KEY_Y -> PrintableKey.Y
        GLFW.GLFW_KEY_U -> PrintableKey.U
        GLFW.GLFW_KEY_I -> PrintableKey.I
        GLFW.GLFW_KEY_O -> PrintableKey.O
        GLFW.GLFW_KEY_P -> PrintableKey.P
        //
        GLFW.GLFW_KEY_A -> PrintableKey.A
        GLFW.GLFW_KEY_S -> PrintableKey.S
        GLFW.GLFW_KEY_D -> PrintableKey.D
        GLFW.GLFW_KEY_F -> PrintableKey.F
        GLFW.GLFW_KEY_G -> PrintableKey.G
        GLFW.GLFW_KEY_H -> PrintableKey.H
        GLFW.GLFW_KEY_J -> PrintableKey.J
        GLFW.GLFW_KEY_K -> PrintableKey.K
        GLFW.GLFW_KEY_L -> PrintableKey.L
        //
        GLFW.GLFW_KEY_Z -> PrintableKey.Z
        GLFW.GLFW_KEY_X -> PrintableKey.X
        GLFW.GLFW_KEY_C -> PrintableKey.C
        GLFW.GLFW_KEY_V -> PrintableKey.V
        GLFW.GLFW_KEY_B -> PrintableKey.B
        GLFW.GLFW_KEY_N -> PrintableKey.N
        GLFW.GLFW_KEY_M -> PrintableKey.M
        //
        else -> null
    }
}

fun Int.toFunctionKeyOrNull(): FunctionKey? {
    return when (this) {
        GLFW.GLFW_KEY_ESCAPE -> FunctionKey.ESCAPE
        GLFW.GLFW_KEY_ENTER -> FunctionKey.ENTER
        GLFW.GLFW_KEY_SPACE -> FunctionKey.SPACE
        else -> null
    }
}

//fun EngineInputState.Joystick.Button.Interaction.toBufferIndex(): Int {
//    return when (this) {
//        EngineInputState.Joystick.Button.Interaction.A -> GLFW.GLFW_GAMEPAD_BUTTON_A
//        EngineInputState.Joystick.Button.Interaction.B -> GLFW.GLFW_GAMEPAD_BUTTON_B
//        EngineInputState.Joystick.Button.Interaction.X -> GLFW.GLFW_GAMEPAD_BUTTON_X
//        EngineInputState.Joystick.Button.Interaction.Y -> GLFW.GLFW_GAMEPAD_BUTTON_Y
//    }
//}
//
//fun EngineInputState.Joystick.Button.Directional.toBufferIndex(): Int {
//    return when (this) {
//        EngineInputState.Joystick.Button.Directional.UP -> 10
//        EngineInputState.Joystick.Button.Directional.RIGHT -> 11
//        EngineInputState.Joystick.Button.Directional.DOWN -> 12
//        EngineInputState.Joystick.Button.Directional.LEFT -> 13
//    }
//}
//
//fun EngineInputState.Joystick.Button.Main.toBufferIndex(): Int {
//    return when (this) {
//        EngineInputState.Joystick.Button.Main.BACK -> GLFW.GLFW_GAMEPAD_BUTTON_BACK
//        EngineInputState.Joystick.Button.Main.START -> GLFW.GLFW_GAMEPAD_BUTTON_START
//    }
//}
