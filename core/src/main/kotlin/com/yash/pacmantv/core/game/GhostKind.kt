package com.yash.pacmantv.core.game

/** The four ghosts. Order matters: it is also their release order from the house. */
enum class GhostKind {
    BLINKY,
    PINKY,
    INKY,
    CLYDE,
}

/** What a ghost is currently doing. */
enum class GhostMode {
    /** Retreating to its own corner. */
    SCATTER,

    /** Hunting, each by its own rule. */
    CHASE,

    /** Blue, slow, and edible. */
    FRIGHTENED,

    /** Eaten: a pair of eyes hurrying back to the house. */
    EATEN,

    /** Bobbing inside the house, waiting for its turn. */
    IN_HOUSE,

    /** Making its way out through the door. */
    LEAVING_HOUSE,
    ;

    val isEdible: Boolean get() = this == FRIGHTENED
    val isInsideHouse: Boolean get() = this == IN_HOUSE || this == LEAVING_HOUSE
}
