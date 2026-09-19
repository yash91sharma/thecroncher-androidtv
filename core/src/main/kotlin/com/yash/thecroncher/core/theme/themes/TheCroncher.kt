package com.yash.thecroncher.core.theme.themes

import com.yash.thecroncher.core.theme.CronchSpriteSource
import com.yash.thecroncher.core.theme.HudColors
import com.yash.thecroncher.core.theme.MazeColors
import com.yash.thecroncher.core.theme.MenuColors
import com.yash.thecroncher.core.theme.SpritePalette
import com.yash.thecroncher.core.theme.Theme

/**
 * The house at night: violet skirting boards and four things a cat would rather
 * not meet. The cat himself is whichever breed the player picked, in `cats/`.
 *
 * Every other colour in the game is in this file. Two rules shape the choices:
 *  - the four foes must be tellable apart in a glance, so their bodies are as far
 *    from each other in hue as they are from every cat's fur;
 *  - nothing that sits still — the score, the lives — is pure white, because this
 *    is going on an OLED and static white is how a panel gets burned.
 */
val TheCroncherPalette = SpritePalette(
    dogFur = 0xFFB5773C.toInt(),
    dogEars = 0xFF7A4A20.toInt(),
    dogSnout = 0xFFE8CFA8.toInt(),
    dogTongue = 0xFFF06A8A.toInt(),

    vacuumBody = 0xFF5C6B7A.toInt(),
    vacuumShade = 0xFF39434F.toInt(),
    vacuumHose = 0xFF8A96A4.toInt(),
    vacuumLight = 0xFFFF4D4D.toInt(),

    sprayBottle = 0xFF35C3F0.toInt(),
    sprayShade = 0xFF1B6E8C.toInt(),
    sprayWater = 0xFFAEEBFF.toInt(),

    cucumberBody = 0xFF5BC236.toInt(),
    cucumberRidge = 0xFF2F7A1C.toInt(),
    cucumberFlesh = 0xFFD9F0B8.toInt(),

    puffBody = 0xFFD8D8E8.toInt(),
    puffShade = 0xFF9A9AB0.toInt(),
    scaredBody = 0xFFD338B5.toInt(),
    scaredFace = 0xFFFFF0F8.toInt(),
    flashBody = 0xFFE8E8F5.toInt(),
    flashFace = 0xFFE04A4A.toInt(),

    treatTriangle = 0xFFE8B457.toInt(),
    treatSquare = 0xFFB5763B.toInt(),
    treatFish = 0xFFF08C6A.toInt(),
    treatStar = 0xFFF5E1A4.toInt(),
    treatShade = 0xFF7A4A22.toInt(),
    catnipLeaf = 0xFF7BE04A.toInt(),
    catnipVein = 0xFF3F8F26.toInt(),

    toyYarn = 0xFFF06292.toInt(),
    toyYarnDark = 0xFFAD3B63.toInt(),
    toyMilk = 0xFFF2F2EA.toInt(),
    toyBowl = 0xFF4FA3E3.toInt(),
    toyFish = 0xFFF08C4A.toInt(),
    toyFishDark = 0xFFB5561F.toInt(),
    toyMouse = 0xFFB0B0B8.toInt(),
    toyMousePink = 0xFFF0A8C0.toInt(),
    toyFeather = 0xFF4FD6C4.toInt(),
    toyStick = 0xFF9A6B3A.toInt(),
    toyBird = 0xFFF5D742.toInt(),
    toyBeak = 0xFFF08C2A.toInt(),
    toyBell = 0xFFF5C542.toInt(),
    toyBellDark = 0xFFB58A1F.toInt(),
    toyGold = 0xFFFFE066.toInt(),
    toyGoldDark = 0xFFC79A1E.toInt(),

    outline = 0xFF1A1A22.toInt(),
    metal = 0xFFB8C0C8.toInt(),
    highlight = 0xFFF2F2F2.toInt(),
)

val TheCroncher = Theme(
    id = "croncher",
    displayName = "The Croncher",
    background = 0xFF07060D.toInt(),
    maze = MazeColors(
        wall = 0xFF7A5CFF.toInt(),
        wallInner = 0xFF3B2A8C.toInt(),
        door = 0xFFF2A0BC.toInt(),
    ),
    hud = HudColors(
        text = 0xFFD8D2C4.toInt(),
        score = 0xFFE8DFC8.toInt(),
        highScore = 0xFFF5C542.toInt(),
        alert = 0xFFFF6B6B.toInt(),
    ),
    menu = MenuColors(
        background = 0xFF07060D.toInt(),
        title = 0xFFF5C542.toInt(),
        item = 0xFF9A94A8.toInt(),
        itemSelected = 0xFFE8E2F0.toInt(),
        cursor = 0xFFF5C542.toInt(),
        footer = 0xFF6A6478.toInt(),
    ),
    palette = TheCroncherPalette,
    sprites = CronchSpriteSource(TheCroncherPalette),
)
