package com.yash.pacmantv.core.theme.themes

import com.yash.pacmantv.core.theme.EntityColors
import com.yash.pacmantv.core.theme.HudColors
import com.yash.pacmantv.core.theme.MazeColors
import com.yash.pacmantv.core.theme.MenuColors
import com.yash.pacmantv.core.theme.PelletColors
import com.yash.pacmantv.core.theme.ProceduralSpriteSource
import com.yash.pacmantv.core.theme.Theme

/**
 * The 1980 cabinet palette. HUD text is deliberately 0xE0E0E0 rather than pure
 * white: the score and lives never move, and this is going on an OLED.
 */
val ClassicArcade = Theme(
    id = "classic",
    displayName = "Classic Arcade",
    background = 0xFF000000.toInt(),
    maze = MazeColors(
        wall = 0xFF2121DE.toInt(),
        wallInner = 0xFF1010A0.toInt(),
        door = 0xFFFFB8AE.toInt(),
        tunnel = 0xFF000000.toInt(),
    ),
    entities = EntityColors(
        pacman = 0xFFFFFF00.toInt(),
        blinky = 0xFFFF0000.toInt(),
        pinky = 0xFFFFB8DE.toInt(),
        inky = 0xFF00FFDE.toInt(),
        clyde = 0xFFFFB847.toInt(),
        frightened = 0xFF2121DE.toInt(),
        frightenedFlash = 0xFFE0E0E0.toInt(),
        eyeWhite = 0xFFDEDEFF.toInt(),
        eyePupil = 0xFF2121DE.toInt(),
    ),
    hud = HudColors(
        text = 0xFFE0E0E0.toInt(),
        score = 0xFFE0E0E0.toInt(),
        highScore = 0xFFFFFF00.toInt(),
        lifeIcon = 0xFFFFFF00.toInt(),
        fruitText = 0xFFFFB8AE.toInt(),
    ),
    menu = MenuColors(
        background = 0xFF000000.toInt(),
        title = 0xFFFFFF00.toInt(),
        item = 0xFFA0A0A0.toInt(),
        itemSelected = 0xFFE0E0E0.toInt(),
        cursor = 0xFFFFFF00.toInt(),
        footer = 0xFF707070.toInt(),
    ),
    pellet = PelletColors(
        pellet = 0xFFFFB8AE.toInt(),
        energizer = 0xFFFFB8AE.toInt(),
    ),
    sprites = ProceduralSpriteSource,
)
