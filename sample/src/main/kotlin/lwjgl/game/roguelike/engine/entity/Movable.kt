package lwjgl.game.roguelike.engine.entity

import lwjgl.wrapper.entity.Point

interface Movable { // todo Player
    val position: Point
    val velocity: Double // unit per nanosecond
    val directionExpected: Double // 0..359
    val directionActual: Double // 0..359
}
