package lwjgl.game.roguelike.engine.entity

import lwjgl.wrapper.entity.Point

interface Positionable {
    val position: Point
}

interface Movable : Positionable { // todo Player
    val velocity: Double // unit per nanosecond
    val directionExpected: Double // 0..359
    val directionActual: Double // 0..359
}
