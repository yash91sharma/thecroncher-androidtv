package com.yash.pacmantv.core.theme.themes

import com.yash.pacmantv.core.theme.EntityColors
import com.yash.pacmantv.core.theme.HudColors
import com.yash.pacmantv.core.theme.MazeColors
import com.yash.pacmantv.core.theme.MenuColors
import com.yash.pacmantv.core.theme.PelletColors
import com.yash.pacmantv.core.theme.ProceduralSpriteSource
import com.yash.pacmantv.core.theme.Theme

/** Synthwave: magenta and cyan on black, which an OLED renders beautifully. */
val Neon = Theme(
    id = "neon",
    displayName = "Neon",
    background = 0xFF05010F.toInt(),
    maze = MazeColors(
        wall = 0xFFFF2CA8.toInt(),
        wallInner = 0xFF7A1354.toInt(),
        door = 0xFF00E5FF.toInt(),
        tunnel = 0xFF05010F.toInt(),
    ),
    entities = EntityColors(
        pacman = 0xFFF9F871.toInt(),
        blinky = 0xFFFF3B3B.toInt(),
        pinky = 0xFFFF7BE5.toInt(),
        inky = 0xFF00E5FF.toInt(),
        clyde = 0xFFFFA23E.toInt(),
        frightened = 0xFF4A2BFF.toInt(),
        frightenedFlash = 0xFFD8D8FF.toInt(),
        eyeWhite = 0xFFEAFBFF.toInt(),
        eyePupil = 0xFF1A0A3C.toInt(),
    ),
    hud = HudColors(
        text = 0xFFD7D0E8.toInt(),
        score = 0xFF00E5FF.toInt(),
        highScore = 0xFFFF2CA8.toInt(),
        lifeIcon = 0xFFF9F871.toInt(),
        fruitText = 0xFFFF7BE5.toInt(),
    ),
    menu = MenuColors(
        background = 0xFF05010F.toInt(),
        title = 0xFFFF2CA8.toInt(),
        item = 0xFF6E6A85.toInt(),
        itemSelected = 0xFF00E5FF.toInt(),
        cursor = 0xFFFF2CA8.toInt(),
        footer = 0xFF44405C.toInt(),
    ),
    pellet = PelletColors(
        pellet = 0xFF9BE7FF.toInt(),
        energizer = 0xFFFF2CA8.toInt(),
    ),
    sprites = ProceduralSpriteSource,
)
