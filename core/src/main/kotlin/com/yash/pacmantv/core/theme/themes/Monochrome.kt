package com.yash.pacmantv.core.theme.themes

import com.yash.pacmantv.core.theme.EntityColors
import com.yash.pacmantv.core.theme.HudColors
import com.yash.pacmantv.core.theme.MazeColors
import com.yash.pacmantv.core.theme.MenuColors
import com.yash.pacmantv.core.theme.PelletColors
import com.yash.pacmantv.core.theme.ProceduralSpriteSource
import com.yash.pacmantv.core.theme.Theme

/**
 * Four shades of green, in the manner of a 1989 handheld. The ghosts are told
 * apart by shade rather than hue, so they still have to be four distinct values.
 */
val Monochrome = Theme(
    id = "mono",
    displayName = "Monochrome",
    background = 0xFF0F380F.toInt(),
    maze = MazeColors(
        wall = 0xFF306230.toInt(),
        wallInner = 0xFF1B451B.toInt(),
        door = 0xFF8BAC0F.toInt(),
        tunnel = 0xFF0F380F.toInt(),
    ),
    entities = EntityColors(
        pacman = 0xFF9BBC0F.toInt(),
        blinky = 0xFF8BAC0F.toInt(),
        pinky = 0xFF6E8C10.toInt(),
        inky = 0xFF56720E.toInt(),
        clyde = 0xFF43590C.toInt(),
        frightened = 0xFF2C4A0C.toInt(),
        frightenedFlash = 0xFF9BBC0F.toInt(),
        eyeWhite = 0xFFCCE8A0.toInt(),
        eyePupil = 0xFF0F380F.toInt(),
    ),
    hud = HudColors(
        text = 0xFF8BAC0F.toInt(),
        score = 0xFF9BBC0F.toInt(),
        highScore = 0xFFCCE8A0.toInt(),
        lifeIcon = 0xFF9BBC0F.toInt(),
        fruitText = 0xFF8BAC0F.toInt(),
    ),
    menu = MenuColors(
        background = 0xFF0F380F.toInt(),
        title = 0xFF9BBC0F.toInt(),
        item = 0xFF4E6B0E.toInt(),
        itemSelected = 0xFFCCE8A0.toInt(),
        cursor = 0xFF8BAC0F.toInt(),
        footer = 0xFF37500D.toInt(),
    ),
    pellet = PelletColors(
        pellet = 0xFF8BAC0F.toInt(),
        energizer = 0xFFCCE8A0.toInt(),
    ),
    sprites = ProceduralSpriteSource,
)
