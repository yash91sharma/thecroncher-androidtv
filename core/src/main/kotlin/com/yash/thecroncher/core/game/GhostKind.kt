package com.yash.thecroncher.core.game

/**
 * The four ghosts, named for how they hunt. Order matters: it is also their release
 * order from the house.
 */
enum class GhostKind {
    /** Aims straight at the croncher. Starts outside the house. */
    CHASER,

    /** Aims a few tiles ahead of the croncher, to cut it off. */
    AMBUSHER,

    /** Aims by doubling the chaser's line through the croncher, closing from the far side. */
    FLANKER,

    /** Chases from a distance but loses its nerve up close and runs for its corner. */
    COWARD,
}

/** What a ghost is currently doing. */
enum class GhostMode {
    /** Retreating to its own corner. */
    SCATTER,

    /** Hunting, each by its own rule. */
    CHASE,

    /** Recoloured, slow, and edible. */
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
