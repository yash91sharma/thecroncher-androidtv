package com.yash.thecroncher.core.theme.cats

/**
 * The faces, drawn as ASCII. See [CatBreed] for the ink alphabet.
 *
 * Every face is sixteen by sixteen and uses the whole box: ears at the top,
 * whiskers poking past the sides, chin at the bottom. Two breeds can share a face
 * and differ only in colour — the tabbies do — which is why the grids live here
 * rather than inside each breed file.
 *
 * The eyes sit on rows 7 and 8 in every face, because [DIZZY] is painted over
 * them when the cat faints and must land on the eyes of every breed.
 */
internal object CatFaces {

    /** Striped: a forehead "M" and a stripe on each cheek. */
    val TABBY = listOf(
        "..F..........F..",
        ".FEF........FEF.",
        ".FEEF......FEEF.",
        ".FEEFF....FFEEF.",
        ".FFFFDFFFFDFFFF.",
        "FFFDFDFFFFDFDFFF",
        "FFFDFFFFFFFFDFFF",
        "FDFYYYFFFFYYYFDF",
        "FFFYPYFFFFYPYFFF",
        "FFFFFFLNNLFFFFFF",
        "iFFFFLLMMLLFFFFi",
        "iiFFFLMLLMLFFFii",
        "iFFFFLLLLLLFFFFi",
        ".FFDFFFLLFFFDFF.",
        "..FFFFFFFFFFFF..",
        "....FFFFFFFF....",
    )

    /** The same head with no markings at all. */
    val PLAIN = listOf(
        "..F..........F..",
        ".FEF........FEF.",
        ".FEEF......FEEF.",
        ".FEEFF....FFEEF.",
        ".FFFFFFFFFFFFFF.",
        "FFFFFFFFFFFFFFFF",
        "FFFFFFFFFFFFFFFF",
        "FFFYYYFFFFYYYFFF",
        "FFFYPYFFFFYPYFFF",
        "FFFFFFLNNLFFFFFF",
        "iFFFFLLMMLLFFFFi",
        "iiFFFLMLLMLFFFii",
        "iFFFFLLLLLLFFFFi",
        ".FFFFFFLLFFFFFF.",
        "..FFFFFFFFFFFF..",
        "....FFFFFFFF....",
    )

    /** Pointed: dark ears and a mask widening from the brow to the muzzle. */
    val SIAMESE = listOf(
        "..D..........D..",
        ".DED........DED.",
        ".DEED......DEED.",
        ".DEEDD....DDEED.",
        ".FDDFFFFFFFFDDF.",
        "FFFFFFFDDFFFFFFF",
        "FFFFFFDDDDFFFFFF",
        "FFFYYYDDDDYYYFFF",
        "FFFYPYDDDDYPYFFF",
        "FFFFFDDDNNDDDFFF",
        "iFFFDDLLMMLLDDFi",
        "iiFFDDLMLLMLDDii",
        "iFFFFDDLLLLDDFFi",
        ".FFFFFDDDDDDFFF.",
        "..FFFFFFFFFFFF..",
        "....FFFFFFFF....",
    )

    /** Hairless: huge ears, forehead wrinkles, big eyes, a lean jaw, no whiskers. */
    val SPHYNX = listOf(
        ".FF..........FF.",
        "FEEF........FEEF",
        "FEEEF......FEEEF",
        "FEEEFF....FFEEEF",
        ".FEEFFFFFFFFEEF.",
        ".FFFDDDFFDDDFFF.",
        ".FFFFFFFFFFFFFF.",
        ".FYYYYFFFFYYYYF.",
        ".FYYPYFFFFYPYYF.",
        ".FFFFFFLNNLFFFF.",
        ".FFFFFLLMMLLFFF.",
        ".FFFFFLMLLMLFFF.",
        ".FFFFFFLLLLFFFF.",
        "..FFFFFFFFFFFF..",
        "...FFFFFFFFFF...",
        ".....FFFFFF.....",
    )

    /** A blaze down the brow, a white muzzle and a bib for a chest. */
    val TUXEDO = listOf(
        "..F..........F..",
        ".FEF........FEF.",
        ".FEEF......FEEF.",
        ".FEEFF....FFEEF.",
        ".FFFFFFLLFFFFFF.",
        "FFFFFFFLLFFFFFFF",
        "FFFFFFFLLFFFFFFF",
        "FFFYYYFLLFYYYFFF",
        "FFFYPYFLLFYPYFFF",
        "FFFFFLLLNNLLLFFF",
        "iFFFLLLLMMLLLLFi",
        "iiFFLLLMLLMLLFii",
        "iFFFLLLLLLLLLFFi",
        ".FFFLLLLLLLLLFF.",
        "..FFLLLLLLLLFF..",
        "....LLLLLLLL....",
    )

    /**
     * Crossed-out eyes, painted over any face as it faints. It covers only the
     * eye sockets so a mask or a blaze beside them survives.
     */
    val DIZZY = listOf(
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "..FPFP....PFPF..",
        "..FFPF....FPFF..",
        "..FPFP....PFPF..",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
    )
}
