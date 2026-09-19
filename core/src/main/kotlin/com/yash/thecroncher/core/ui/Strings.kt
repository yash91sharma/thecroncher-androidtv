package com.yash.thecroncher.core.ui

/** All user-facing text, in one place so renaming or translating touches one file. */
object Strings {
    const val TITLE = "THE"
    const val TITLE_SECOND_LINE = "CRONCHER"
    const val ONE_UP = "1UP"
    const val HIGH_SCORE = "HIGH SCORE"
    const val READY = "GET READY!"
    const val GAME_OVER = "GAME OVER"
    const val PAUSED = "PAUSED"

    const val PLAY = "PLAY"
    const val SELECT_MY_CAT = "SELECT MY CAT"
    const val SETTINGS = "SETTINGS"
    const val EXIT = "EXIT"

    const val DIFFICULTY = "DIFFICULTY"
    const val SOUND = "SOUND"
    const val CONTROLLER_TEST = "CONTROLLER TEST"
    const val BACK = "BACK"

    const val ON = "ON"
    const val OFF = "OFF"

    const val RESUME = "RESUME"
    const val RESTART = "RESTART"
    const val QUIT_TO_MENU = "QUIT TO MENU"

    const val CONTROLLER_DISCONNECTED = "CONTROLLER DISCONNECTED"

    /**
     * The subtitle under the wordmark on the title screen, one entry per line:
     * the font is 6px per character, so a sentence this long cannot fit the safe
     * area on a single row.
     */
    val TAGLINE = listOf(
        "CAN YOUR CAT EAT ALL THE TREATS",
        "BEFORE THE FOES CATCH IT?",
    )
}
