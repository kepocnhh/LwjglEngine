package lwjgl.game.roguelike.engine.entity

interface Intelligence {
    interface Goal {
        class Move(val target: Positionable) : Goal
    }
    val goals: List<Goal>
    val goalCurrent: Goal?
}

interface Dummy : Movable {
    val intelligence: Intelligence
}
