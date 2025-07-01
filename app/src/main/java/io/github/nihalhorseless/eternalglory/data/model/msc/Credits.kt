package io.github.nihalhorseless.eternalglory.data.model.msc

data class CreditCategory(
    val title: String,
    val items: List<CreditItem>
)

data class CreditItem(
    val assetName: String,
    val author: String,
    val source: String,
    val license: String,
    val url: String? = null,
    val licenseUrl: String? = null,
    val changes: String? = null
)

object GameCredits {
    val credits = listOf(
        CreditCategory(
            title = "Music",
            items = listOf(
                CreditItem(
                    assetName = "Main Menu Theme - Abdelazer: Rondeau",
                    author = "Henry Purcell",
                    source = "Public Domain",
                    license = "CC0",
                    url = "https://musopen.org/music/11130-abdelazer-z-570/"
                ),
                CreditItem(
                    assetName = "Level Selection - Sarabande",
                    author = "George Friedrich Handel",
                    source = "Public Domain",
                    license = "CC0",
                    url = "https://musopen.org/music/6208-suite-in-d-minor-hwv-437/"
                ),
                CreditItem(
                    assetName = "Deck Editor - Tales From Vienna Woods",
                    author = "Johann Strauss II",
                    source = "Public Domain",
                    license = "CC0",
                    url = "https://musopen.org/music/9896-tales-from-the-vienna-woods-op-325/"
                )
                // Add all your music tracks here
            )
        ),
        CreditCategory(
            title = "Sound Effects",
            items = listOf(
                CreditItem(
                    assetName = "Sword Slash",
                    author = "Mateusz_Chenc",
                    source = "Freesound.org",
                    license = "CC BY 3.0",
                    url = "https://freesound.org/people/Mateusz_Chenc/sounds/547600/"
                ),
                CreditItem(
                    assetName = "Card Sounds",
                    author = "Kenney",
                    source = "opengameart.org",
                    license = "CC BY 3.0",
                    url = "https://opengameart.org/content/54-casino-sound-effects-cards-dice-chips"
                ),
                CreditItem(
                    assetName = "Musket Fire",
                    author = "Michel Baradari",
                    source = "opengameart.org",
                    license = "CC BY 3.0",
                    url = "https://opengameart.org/content/rumbleexplosion"
                ),
                CreditItem(
                    assetName = "Debuff Effect",
                    author = "qubodup",
                    source = "opengameart.org",
                    license = "CC BY 3.0",
                    url = "https://opengameart.org/content/energy-drain"
                ),
                CreditItem(
                    assetName = "Turn End Sound",
                    author = "rodincoil",
                    source = "Freesound.org",
                    license = "CC BY 3.0",
                    url = "https://freesound.org/people/rodincoil/sounds/271945/"
                ),
                CreditItem(
                    assetName = "Horse Move",
                    author = "gurek",
                    source = "Freesound.org",
                    license = "CC BY 3.0",
                    url = "https://freesound.org/people/gurek/sounds/547967/"
                ),
                CreditItem(
                    assetName = "Infantry Tap",
                    author = "stib",
                    source = "Freesound.org",
                    license = "CC BY 3.0",
                    url = "https://freesound.org/people/stib/sounds/240732/"
                ),
                CreditItem(
                    assetName = "Infantry Move",
                    author = " Yap_Audio_Production",
                    source = "Freesound.org",
                    license = "CC BY 3.0",
                    url = "https://freesound.org/people/Yap_Audio_Production/sounds/218998/"
                ),
                CreditItem(
                    assetName = "Horse Tap",
                    author = " bruno.auzet",
                    source = "Freesound.org",
                    license = "CC BY 3.0",
                    url = "https://freesound.org/people/bruno.auzet/sounds/538438/"
                ),
                CreditItem(
                    assetName = "Artillery Attack",
                    author = "EvanBoyerman",
                    source = "Freesound.org",
                    license = "CC BY 3.0",
                    url = "https://freesound.org/people/EvanBoyerman/sounds/388528/"
                ),
                CreditItem(
                    assetName = "Fort Tap",
                    author = " thanvannispen",
                    source = "Freesound.org",
                    license = "CC BY 3.0",
                    url = "https://freesound.org/people/thanvannispen/sounds/29986/"
                ),
                CreditItem(
                    assetName = "Fort Destruction",
                    author = " iwanPlays",
                    source = "Freesound.org",
                    license = "CC BY 3.0",
                    url = "https://freesound.org/people/iwanPlays/sounds/567249/"
                ),
                CreditItem(
                    assetName = "Opponent Hit",
                    author = " spookymodem",
                    source = "opengameart.org",
                    license = "CC BY 3.0",
                    url = "https://opengameart.org/content/wall-impact"
                ),
                CreditItem(
                    assetName = "Menu Tap",
                    author = " DoKashiteru",
                    source = "opengameart.org",
                    license = "CC BY 3.0",
                    url = "https://opengameart.org/content/3-heal-spells"
                ),
                CreditItem(
                    assetName = "Unit Hit",
                    author = " saangosu",
                    source = "Freesound.org",
                    license = "CC BY 3.0",
                    url = "https://freesound.org/people/saangosu/sounds/781093/"
                ),
                CreditItem(
                    assetName = "Bayonet Sheathe",
                    author = " giddster",
                    source = "Freesound.org",
                    license = "CC BY 3.0",
                    url = "https://freesound.org/people/giddster/sounds/484298/"
                ),
                CreditItem(
                    assetName = "Level Start",
                    author = " bart",
                    source = "opengameart.org",
                    license = "CC BY 3.0",
                    url = "https://opengameart.org/content/level-up-sound-effects"
                ),
                CreditItem(
                    assetName = "Tactical Rocket Sound",
                    author = " belloq",
                    source = "Freesound.org",
                    license = "CC BY 3.0",
                    url = "https://freesound.org/people/belloq/sounds/41459/"
                ),
                CreditItem(
                    assetName = "Unit Death",
                    author = " Rock Savage",
                    source = "Freesound.org",
                    license = "CC BY 3.0",
                    url = "https://freesound.org/people/Rock%20Savage/sounds/81042/"
                ),
                CreditItem(
                    assetName = "Game Over Bell",
                    author = " dsp9000",
                    source = "Freesound.org",
                    license = "CC BY 3.0",
                    url = "https://freesound.org/people/dsp9000/sounds/76405/"
                ),
                CreditItem(
                    assetName = "Special Cards",
                    author = " EminYILDIRIM",
                    source = "Freesound.org",
                    license = "CC BY 3.0",
                    url = "https://freesound.org/people/EminYILDIRIM/sounds/621206/"
                ),
                CreditItem(
                    assetName = "Buff Cards",
                    author = " EminYILDIRIM",
                    source = "Freesound.org",
                    license = "CC BY 3.0",
                    url = "https://freesound.org/people/EminYILDIRIM/sounds/563662/"
                ),
                CreditItem(
                    assetName = "Victory",
                    author = " FunWithSound",
                    source = "Freesound.org",
                    license = "CC BY 3.0",
                    url = "https://freesound.org/people/FunWithSound/sounds/456967/"
                )
                // Add all your sound effects
            )
        ),
        CreditCategory(
            title = "Images & Icons",
            items = listOf(
                CreditItem(
                    assetName = "Charge and Counter Icons",
                    author = "Game Skills Vectors",
                    source = "svgrepo",
                    license = "CC Attribution License",
                    url = "https://www.svgrepo.com/svg/320316/all-for-one"
                ),
                CreditItem(
                    assetName = "Range Icon",
                    author = "Icooon Mono",
                    source = "svgrepo",
                    license = "PD License",
                    url = "https://www.svgrepo.com/svg/477512/shooting"
                ),
                CreditItem(
                    assetName = "Movement Icon",
                    author = "Noah Jacobus",
                    source = "svgrepo",
                    license = "PD License",
                    url = "https://www.svgrepo.com/svg/535741/wind"
                ),
                CreditItem(
                    assetName = "Hourglass Icon",
                    author = "AnastasiyaArtDesign",
                    source = "designbundles",
                    license = "Can be used for Commercial Use",
                    url = "https://designbundles.net/anastasiyaartdesign/1941621-hourglass-and-nature-silhoueette-svg-outdoors-land"
                )
                // Add all your images
            )
        ),
        CreditCategory(
            title = "Animations",
            items = listOf(
                CreditItem(
                    assetName = "Melee Attack Animations",
                    author = "jasontomlee",
                    source = "itch.io",
                    license = "CC BY 4.0, Free for commercial use",
                    url = "https://jasontomlee.itch.io/blood-fx"
                ),
                CreditItem(
                    assetName = "Tactical Explosion Animation",
                    author = "ansimuz",
                    source = "itch.io",
                    license = "Free for commercial use",
                    url = "https://ansimuz.itch.io/explosion-animations-pack"
                ),
                CreditItem(
                    assetName = "Various Animations",
                    author = "codemanu",
                    source = "itch.io",
                    license = "public domain asset",
                    url = "https://codemanu.itch.io/vfx-free-pack"
                )
                // Add all your GIFs
            )
        ),
        CreditCategory(
            title = "Fonts",
            items = listOf(
                CreditItem(
                    assetName = "Libre Caslon Display",
                    author = "Pablo Impallari",
                    source = "Google Fonts",
                    license = "OFL (Open Font License)",
                    url = "https://fonts.google.com/specimen/Libre+Caslon+Display"
                )
            )
        ),
        CreditCategory(
            title = "Special Thanks",
            items = listOf(
                CreditItem(
                    assetName = "To My Family and Dear Friends",
                    author = "Peace and Love",
                    source = "",
                    license = "",
                    url = null
                )
            )
        )
    )
}