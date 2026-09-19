package com.yash.thecroncher.core.theme

/**
 * The cast, drawn as ASCII.
 *
 * Every character in a grid names an ink, not a colour — the theme's
 * [SpritePalette] decides what "fur" or "water" actually looks like. Editing a
 * character means editing the picture you can see here; restyling one is a
 * palette edit next door. See [PixelArt] for how a grid becomes pixels.
 *
 * The ink alphabet:
 * ```
 *   .    nothing        i      whisker/tooth/glint
 *   F D L E N Y P M     the cat, see `cats/CatBreed`
 *   g h  dog fur, dog ears     s t  snout, tongue
 *   v w  vacuum body, shading  u r   hose, power light
 *   b c  bottle, bottle shade  q     water
 *   k j  cucumber, its ridges  f     cut flesh
 *   p n  puff, its shading     o m   outline, metal
 *   1-5  treats and their shade      6 7  catnip
 *   A a B C G H I J K S T U V W X Z  the bonus toys
 * ```
 */
internal object CronchArt {

    // The croncher himself lives in `cats/CatFaces`: the player picks his breed,
    // so his face and colours are not the theme's to decide.

    // ------------------------------------------------------- the fears --

    val DOG_SIDE = listOf(
        listOf(
            "................",
            "................",
            "................",
            "....hhhhhh......",
            "..hhhggggggg....",
            "..hhhgggoiggg...",
            "..hhhgggooggssoo",
            "..hhhgggggssssoo",
            "..hhhgggggssssss",
            "..hhhgggggssssss",
            "..hhhgggggoooooo",
            "..hhhgggggotttto",
            ".......ggg.tttt.",
            "................",
            "................",
            "................",
        ),
        listOf(
            "................",
            "................",
            "................",
            ".....hhhhh......",
            "....hggggggg....",
            "..hhhgggoiggg...",
            "..hhhgggooggssoo",
            "..hhhgggggssssoo",
            "..hhhgggggssssss",
            "..hhhgggggssssss",
            "..hhhgggggoooooo",
            "..hhhgggggotttto",
            "..hhh..ggg.tttt.",
            "................",
            "................",
            "................",
        ),
    )

    val DOG_FRONT = listOf(
        listOf(
            "................",
            "................",
            "................",
            "...h.hhhhhh.h...",
            ".hhh..ggggg.hhh.",
            ".hhh.ggggggghhh.",
            ".hhhgoiggoighhh.",
            ".hhhgooggooghhh.",
            ".hhhgggoosgghhh.",
            ".hhhgssoossshhh.",
            ".hhh.ssssssshhh.",
            ".....sssssss....",
            "......ottto.....",
            ".......ttt......",
            "................",
            "................",
        ),
        listOf(
            "................",
            "................",
            "............h...",
            ".....hhhhhh.hhh.",
            "...h..ggggg.hhh.",
            ".hhh.ggggggghhh.",
            ".hhhgoiggoighhh.",
            ".hhhgooggooghhh.",
            ".hhhgggoosgghhh.",
            ".hhhgssoossshhh.",
            ".hhh.sssssss....",
            ".hhh.sssssss....",
            "......ottto.....",
            ".......ttt......",
            "................",
            "................",
        ),
    )

    val VACUUM = listOf(
        listOf(
            "..mmmmm.......uu",
            "..mmmmm.......uu",
            "..mm.........uu.",
            "..mmwwwwwww.uu..",
            "..mmwwwwwwwuu...",
            "..vvvvvvvvv.....",
            "..vwwwwwrrv.....",
            "..vwiiiwrrv.....",
            "..vwwwwwvvv.....",
            "..vvvvvvvvv.....",
            "..vvvvvvvvv.....",
            "..vvvvvvvvv.....",
            ".mmmmmmmmmmmm...",
            ".mmmmmmmmmmmm...",
            "..www..wwww.....",
            "................",
        ),
        listOf(
            "................",
            "..mmmmm.......uu",
            "..mmmmm.......uu",
            "..mm.........uu.",
            "..mmwwwwwww.uu..",
            "..mmwwwwwwwuu...",
            "..vvvvvvvvv.....",
            "..vwwwwwrrv.....",
            "..vwiiiwrrv.....",
            "..vwwwwwvvv.....",
            "..vvvvvvvvv.....",
            "..vvvvvvvvv.....",
            "..vvvvvvvvv.....",
            ".mmmmmmmmmmmm...",
            ".mmmmmmmmmmmm...",
            "..www..wwww.....",
        ),
    )

    val SPRAY = listOf(
        listOf(
            "................",
            "..............qq",
            "............qq..",
            "...mmmmmm..qq...",
            "...mmmmmmmm..qq.",
            "..cccmmmmmm.....",
            "..ccbbbb........",
            "..ccbbbb........",
            "...bbbbbb.......",
            "..bbbbbbbb......",
            "..biiiiiib......",
            "..biccccib......",
            "..biiiiiib......",
            "..bbbbbbbb......",
            "..bbbbbbbb......",
            "..cccccccc......",
        ),
        listOf(
            "................",
            "..............qq",
            "............qq..",
            "...mmmmmm..qq..q",
            "...mmmmmmmm..qq.",
            "..cccmmmmmm.qq..",
            "..ccbbbb.....qq.",
            "..ccbbbb........",
            "...bbbbbb.......",
            "..bbbbbbbb......",
            "..biiiiiib......",
            "..biccccib......",
            "..biiiiiib......",
            "..bbbbbbbb......",
            "..bbbbbbbb......",
            "..cccccccc......",
        ),
    )

    val CUCUMBER = listOf(
        listOf(
            "................",
            "................",
            "................",
            "...........fff..",
            "........kkkfff..",
            ".......kkkkkkkk.",
            "......kkkjkkkk..",
            ".....kkkjkkjkk..",
            "....kkkjkkjkk...",
            "....kkjkkjkkk...",
            "...kkjkkjkk.....",
            "...kjkkjkk......",
            "..kkkkjkk.......",
            "...kkkkk........",
            "................",
            "................",
        ),
        listOf(
            "................",
            "................",
            "...........fff..",
            "........kkkfff..",
            ".......kkkkkkkk.",
            "......kkkjkkkk..",
            ".....kkkjkkjkk..",
            "....kkkjkkjkk...",
            "....kkjkkjkkk...",
            "...kkjkkjkk.....",
            "...kjkkjkk......",
            "..kkkkjkk.......",
            "...kkkkk........",
            "................",
            "................",
            "................",
        ),
    )

    val PUFF = listOf(
        listOf(
            "................",
            "................",
            "................",
            "......pppp......",
            "....pppppppp....",
            "...ppppppppppp..",
            "..pppoppppoppp..",
            "...ppoppppoppp..",
            "...pppppppppp...",
            "...pppppppppp...",
            "...nnnnnnnnnn...",
            ".....pppppp.....",
            "................",
            "................",
            "................",
            "................",
        ),
        listOf(
            "................",
            "................",
            "......pppp......",
            "....pppppppp....",
            "...ppppppppppp..",
            "..pppoppppoppp..",
            "...ppoppppoppp..",
            "...pppppppppp...",
            "...pppppppppp...",
            "...nnnnnnnnnn...",
            ".....pppppp.....",
            "................",
            "................",
            "................",
            "................",
            "................",
        ),
    )

    /** The face a frightened foe wears, painted inside its own silhouette. */

    val SCARED_FACE = listOf(
        "................",
        "................",
        "................",
        "................",
        "................",
        ".....**..**.....",
        ".....**..**.....",
        "................",
        "................",
        "...*.*.*.*.*.*..",
        "..*.*.*.*.*.*.*.",
        "................",
        "................",
        "................",
        "................",
        "................",
    )

    // ------------------------------------------------ treats and toys --

    val TREAT_TRIANGLE = listOf(
        "........",
        "........",
        "...11...",
        "..1111..",
        "..5555..",
        "........",
        "........",
        "........",
    )

    val TREAT_SQUARE = listOf(
        "........",
        "........",
        "..2222..",
        "..2222..",
        "..5555..",
        "........",
        "........",
        "........",
    )

    val TREAT_FISH = listOf(
        "........",
        "........",
        "..333.3.",
        ".333333.",
        ".333333.",
        "..555.5.",
        "........",
        "........",
    )

    val TREAT_STAR = listOf(
        "........",
        "...4....",
        "..444...",
        ".44444..",
        "..444...",
        ".4.5.4..",
        "........",
        "........",
    )

    val CATNIP = listOf(
        "...66...",
        "..6666..",
        ".676666.",
        "6666766.",
        ".6667666",
        "..66666.",
        "...777..",
        "........",
    )

    val TOY_YARN = listOf(
        "................",
        "................",
        "...............A",
        "..............A.",
        ".....aAAaAA..A..",
        "....AaiAaAAa....",
        "...aAiaAAaAAa...",
        "...aAAaAAaAAa...",
        "...AaAAaAAaAA...",
        "...AaAAaAAaAA...",
        "...AAaAAaAAaA...",
        "...AAaAAaAAaA...",
        "....AAaAAaAA....",
        ".....AaAAaA.....",
        "................",
        "................",
    )

    val TOY_MILK = listOf(
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "...BBiiBBBBBB...",
        "..BBBBBBBBBBBB..",
        "..BBBBBBBBBBBB..",
        "..CCCCCCCCCCCC..",
        "...CCCCCCCCCC...",
        "...CCCCCCCCCC...",
        "....CCCCCCCC....",
        "................",
        "................",
        "................",
    )

    val TOY_FISH = listOf(
        "................",
        "................",
        "................",
        "......G.........",
        "......GG......G.",
        ".....GGGG....GG.",
        "...GGGGGGGGHGGG.",
        "..GHGGGGGGGHGGG.",
        "..GGGGGGGGGHGGG.",
        "..GGGGGGGGGHGGG.",
        "...GGHHHHHGHGGG.",
        ".....GGGG....GG.",
        "................",
        "................",
        "................",
        "................",
    )

    val TOY_MOUSE = listOf(
        "................",
        "................",
        "................",
        "......J.........",
        ".....JJJ........",
        "....JJJJJ......J",
        "...IIJJJIII...J.",
        "..IIoIJIIIIIIJ..",
        "..oIIIIIIIIIJ...",
        "..oIIIIIIIIIII..",
        "...IIIIIIIIII...",
        "....IIIIIIIII...",
        "......IIIII.....",
        "................",
        "................",
        "................",
    )

    val TOY_FEATHER = listOf(
        "................",
        "................",
        "..........KKK...",
        ".........KKKKK..",
        ".........iKKKK..",
        "........KiKKKKK.",
        "........KSKKKKK.",
        "........KKSKKKK.",
        "........SKSKKK..",
        ".......S.KKSKK..",
        "......S...KSK...",
        ".....S..........",
        "....S...........",
        "...S............",
        "..S.............",
        "................",
    )

    val TOY_BIRD = listOf(
        "................",
        "................",
        "................",
        ".....T..........",
        "...TTTTT........",
        "...UToTT........",
        "UUUUTTTTTTT.....",
        "...TTTTTTTTTU...",
        "....TTTUUUUU...T",
        "....TTTTUUUT.TTT",
        "....TTTTTUTTTTTT",
        "....TTTTTTTT.TTT",
        ".....TTTTTT...TT",
        "................",
        "................",
        "................",
    )

    val TOY_BELL = listOf(
        "................",
        "................",
        ".......WW.......",
        ".......WW.......",
        "......VVVV......",
        ".....VVVVVV.....",
        ".....VVVVVV.....",
        ".....VVVVVV.....",
        ".....iVVVVV.....",
        ".....iVVVVV.....",
        ".....VVVVVV.....",
        ".....VVVVVV.....",
        "...WWWWWWWWWW...",
        "...WWWWWWWWWW...",
        ".......WW.......",
        ".......WW.......",
    )

    /** The same fish, cast in gold. */

    val TOY_GOLDFISH = listOf(
        "................",
        "................",
        "................",
        "......X.........",
        "......XX......X.",
        ".....XXXX....XX.",
        "...XXXXXXXXZXXX.",
        "..XZXXXXXXXZXXX.",
        "..XXXXXXXXXZXXX.",
        "..XXXXXXXXXZXXX.",
        "...XXZZZZZXZXXX.",
        ".....XXXX....XX.",
        "................",
        "................",
        "................",
        "................",
    )

}
