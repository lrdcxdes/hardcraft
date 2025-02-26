package dev.lrdcxdes.hardcraft.races

import org.bukkit.attribute.Attribute
import org.bukkit.profile.PlayerTextures.SkinModel
import java.net.URI
import java.net.URL

enum class Race {
    HUMAN, ELF, DWARF, KOBOLD, GIANT, VAMPIRE, AMPHIBIAN, SKELETON, GOBLIN, DRAGONBORN,
    SNOLEM, AGAR, CIBLE
}

data class RaceAttributes(
    val baseAttributes: Map<Attribute, Double> = mapOf(),
)

object RaceManager {
    private val defaultAttributes: RaceAttributes = RaceAttributes(
        baseAttributes = mapOf(
            Attribute.SCALE to 1.0,
            Attribute.MAX_HEALTH to 20.0,
            Attribute.ATTACK_DAMAGE to 1.0,
            Attribute.MOVEMENT_SPEED to 0.10000000149011612,
            Attribute.LUCK to 0.0,
            Attribute.SAFE_FALL_DISTANCE to 3.0,
            Attribute.FALL_DAMAGE_MULTIPLIER to 1.0,
            Attribute.BLOCK_INTERACTION_RANGE to 4.5,
            Attribute.ENTITY_INTERACTION_RANGE to 3.0,
            Attribute.OXYGEN_BONUS to 0.0,
            Attribute.WATER_MOVEMENT_EFFICIENCY to 0.0,
            Attribute.ARMOR to 0.0,
            Attribute.ARMOR_TOUGHNESS to 0.0,
            Attribute.MINING_EFFICIENCY to 0.0,
            Attribute.JUMP_STRENGTH to 0.45
        ),
    )

    private val races: Map<Race, RaceAttributes> = mapOf(
        Race.HUMAN to RaceAttributes(
            baseAttributes = mapOf(),
        ),
        Race.ELF to RaceAttributes(
            baseAttributes = mapOf(
                Attribute.SCALE to 1.05,
                Attribute.MAX_HEALTH to 24.0,
                Attribute.LUCK to 0.7,
            ),
        ),
        Race.GOBLIN to RaceAttributes(
            baseAttributes = mapOf(
                Attribute.SCALE to 0.85,
                Attribute.MAX_HEALTH to 16.0,
                Attribute.BLOCK_INTERACTION_RANGE to 3.15,
                Attribute.ENTITY_INTERACTION_RANGE to 2.1,
                Attribute.ATTACK_DAMAGE to 0.85,
                Attribute.FALL_DAMAGE_MULTIPLIER to 0.85,
            ),
        ),
        Race.DWARF to RaceAttributes(
            baseAttributes = mapOf(
                Attribute.SCALE to 0.85,
                Attribute.MAX_HEALTH to 16.0,
                Attribute.MOVEMENT_SPEED to 0.08500000126,
                Attribute.BLOCK_INTERACTION_RANGE to 3.15,
                Attribute.ENTITY_INTERACTION_RANGE to 2.1,
                Attribute.ARMOR_TOUGHNESS to 1.15,
                Attribute.MINING_EFFICIENCY to 1.1,
                Attribute.ATTACK_DAMAGE to 0.85,
                Attribute.FALL_DAMAGE_MULTIPLIER to 0.85,
            ),
        ),
        Race.KOBOLD to RaceAttributes(
            baseAttributes = mapOf(
                Attribute.SCALE to 0.65,
                Attribute.MAX_HEALTH to 14.0,
                Attribute.MOVEMENT_SPEED to 0.11500000171,
                Attribute.BLOCK_INTERACTION_RANGE to 2.925,
                Attribute.ENTITY_INTERACTION_RANGE to 1.95,
                Attribute.MINING_EFFICIENCY to 1.4,
                Attribute.ATTACK_DAMAGE to 0.65,
                Attribute.FALL_DAMAGE_MULTIPLIER to 0.65,
            ),
        ),
        Race.GIANT to RaceAttributes(
            baseAttributes = mapOf(
                Attribute.SCALE to 1.3,
                Attribute.MAX_HEALTH to 40.0,
                Attribute.MOVEMENT_SPEED to 0.10000000149011612,
                Attribute.SAFE_FALL_DISTANCE to 5.0,
                Attribute.BLOCK_INTERACTION_RANGE to 5.4,
                Attribute.ENTITY_INTERACTION_RANGE to 3.6,
                Attribute.JUMP_STRENGTH to 0.52,
                Attribute.ATTACK_DAMAGE to 1.3,
                Attribute.FALL_DAMAGE_MULTIPLIER to 1.1,
            ),
        ),
        Race.VAMPIRE to RaceAttributes(
            baseAttributes = mapOf(
                Attribute.FALL_DAMAGE_MULTIPLIER to 0.3,
            ),
        ),
        Race.AMPHIBIAN to RaceAttributes(
            baseAttributes = mapOf(
                Attribute.OXYGEN_BONUS to Double.MAX_VALUE,
                Attribute.WATER_MOVEMENT_EFFICIENCY to 2.0,
            ),
        ),
        Race.SKELETON to RaceAttributes(
            baseAttributes = mapOf(
                Attribute.MAX_HEALTH to 12.0,
                Attribute.OXYGEN_BONUS to 5.0,
            ),
        ),
        Race.DRAGONBORN to RaceAttributes(
            baseAttributes = mapOf(
                Attribute.SCALE to 1.15,
                Attribute.MAX_HEALTH to 16.0,
                Attribute.ARMOR to 10.0,
                Attribute.ATTACK_DAMAGE to 1.15,
                Attribute.FALL_DAMAGE_MULTIPLIER to 1.05,
                Attribute.SAFE_FALL_DISTANCE to 4.0,
                Attribute.JUMP_STRENGTH to 0.5,
                Attribute.MOVEMENT_SPEED to 0.09850000047683716,
                Attribute.BLOCK_INTERACTION_RANGE to 5.0,
                Attribute.ENTITY_INTERACTION_RANGE to 3.25,
            ),
        ),
        Race.SNOLEM to RaceAttributes(
            baseAttributes = mapOf(
                Attribute.SCALE to 1.25,
                Attribute.MAX_HEALTH to 24.0,
                Attribute.ATTACK_DAMAGE to 1.25,
                Attribute.ARMOR to 8.0,
                Attribute.ARMOR_TOUGHNESS to 1.0,
                Attribute.FALL_DAMAGE_MULTIPLIER to 1.1,
                Attribute.SAFE_FALL_DISTANCE to 4.5,
                Attribute.JUMP_STRENGTH to 0.5,
                Attribute.MOVEMENT_SPEED to 0.09650000023841858,
                Attribute.BLOCK_INTERACTION_RANGE to 5.7,
                Attribute.ENTITY_INTERACTION_RANGE to 3.75,
            ),
        ),
        Race.CIBLE to RaceAttributes(
            baseAttributes = mapOf(
                Attribute.SCALE to 0.9,
                Attribute.MAX_HEALTH to 16.0,
                Attribute.ARMOR to 7.0,
                Attribute.ATTACK_DAMAGE to 0.9,
                Attribute.FALL_DAMAGE_MULTIPLIER to 0.9,
                Attribute.SAFE_FALL_DISTANCE to 3.5,
                Attribute.JUMP_STRENGTH to 0.45,
                Attribute.MOVEMENT_SPEED to 0.10500000357627869,
                Attribute.BLOCK_INTERACTION_RANGE to 4.2,
                Attribute.ENTITY_INTERACTION_RANGE to 2.8,
            ),
        ),
        Race.AGAR to RaceAttributes(
            baseAttributes = mapOf(
                Attribute.SCALE to 0.5,
                Attribute.MAX_HEALTH to 6.0,
                Attribute.JUMP_STRENGTH to 0.45,
                Attribute.FALL_DAMAGE_MULTIPLIER to 0.35,
                Attribute.MOVEMENT_SPEED to 0.135,
                Attribute.BLOCK_INTERACTION_RANGE to 1.75,
                Attribute.ENTITY_INTERACTION_RANGE to 1.5,
                Attribute.SAFE_FALL_DISTANCE to 2.5,
                Attribute.ATTACK_DAMAGE to 0.5,
            ),
        ),
    )

    data class SkinAttributes(
        val url: String,
        val model: SkinModel,
    )

    private val skins: Map<Race, List<SkinAttributes>> = mutableMapOf(
        Race.SKELETON to listOf(
            SkinAttributes("http://textures.minecraft.net/texture/bc230c518191fc2f06c091a70760e9d8692a24b096e964b06183979ede043fcd", SkinModel.SLIM),
        ),
        Race.ELF to listOf(
            SkinAttributes("http://textures.minecraft.net/texture/30f8b848007375d00e146594bc0947538fd197b1ef1f1fbaa955b207820ee896", SkinModel.SLIM),
            SkinAttributes("http://textures.minecraft.net/texture/dc0eeee07ae1e666bfa67d7420a4152c0c03873214fe1aae6b700e8f992099c7", SkinModel.SLIM),
            SkinAttributes("http://textures.minecraft.net/texture/cdb53d816f27454ad80555a1ddad984ee50bd47ca5b4eb9dd62f401fd207535f", SkinModel.SLIM),
            SkinAttributes("http://textures.minecraft.net/texture/a0194eefe3e064d6110f49d5c1141d9a197f9584d58290ade5ab4a9e96cde316", SkinModel.SLIM),
            SkinAttributes("http://textures.minecraft.net/texture/d77a52ffba195eea705efd05828727f030b851c3303490aa800d3d8cf75f61e4", SkinModel.SLIM),
            SkinAttributes("http://textures.minecraft.net/texture/f5fab69f2cb9cdf1eb4b6ee26e214a32e3f3013d39b098ff71714495ddd00b04", SkinModel.SLIM),
            SkinAttributes("http://textures.minecraft.net/texture/d8c0d35ae0aec2113bec984f562c77f8750004edb93cddeb8f687d26a6252747", SkinModel.SLIM),
            SkinAttributes("http://textures.minecraft.net/texture/c2b980c68a61c0ce5b1cc28d844cb51966fd1df6d0ac2dc6a465b1568f45b520", SkinModel.SLIM),
            SkinAttributes("http://textures.minecraft.net/texture/760c995adbe1c5da676b23ee40537fe7f0ec3ba23d34c336842769c642c963a4", SkinModel.SLIM),
            SkinAttributes("http://textures.minecraft.net/texture/a8f94a1c1bbf1d23fd3956b03d3d2f924c242eee945fc658b71032510227643", SkinModel.SLIM),
        ),
        Race.HUMAN to listOf(
            SkinAttributes("http://textures.minecraft.net/texture/cabd4c338f75cba803cdeb43b8d230048aca5d1480608c50fd85a80b5109b83", SkinModel.CLASSIC),
            SkinAttributes("http://textures.minecraft.net/texture/b953ce30f614f85f28028f66d00da70b04721e882949d8c6ff2aead0f8a94d03", SkinModel.CLASSIC),
            SkinAttributes("http://textures.minecraft.net/texture/f11f737e1dec5182a0ae3907696e385fb86da45c85f963bf608f488ded2e8e0e", SkinModel.CLASSIC),
            SkinAttributes("http://textures.minecraft.net/texture/e28e5cd63031972dc7d3e52cdc2a9c3d5fb6a17adef1896d1e55dc9dfa733789", SkinModel.CLASSIC),
            SkinAttributes("https://s.namemc.com/i/03e7dfb22d51f163.png", SkinModel.CLASSIC),
            SkinAttributes("https://s.namemc.com/i/c2df7d19e1566ef1.png", SkinModel.CLASSIC),
            SkinAttributes("https://s.namemc.com/i/71b38d9a135848b6.png", SkinModel.CLASSIC),
        ),
        Race.DWARF to listOf(
            SkinAttributes("http://textures.minecraft.net/texture/def3fd1c2caefffe86b7ebd9ee1a57c9a7a04fd0a0b563c46dce7a2af4a52", SkinModel.CLASSIC),
            SkinAttributes("http://textures.minecraft.net/texture/9772642ffccfc9e11b350c874f2c84678fc08044b51e7a8e3a0919f8f788ed9a", SkinModel.CLASSIC),
            SkinAttributes("http://textures.minecraft.net/texture/8f0790d9758b14ce5f417816c015728745a5d8a43048a9d0a41d0e168ed27d60", SkinModel.CLASSIC),
            SkinAttributes("http://textures.minecraft.net/texture/cade1161319f7f18be5d5e51f1f7f147fb1d2626e8a76ceee446558fa5472205", SkinModel.CLASSIC),
            SkinAttributes("http://textures.minecraft.net/texture/d8674b2cfa7a471fe1a23d68ea6654286d5b435eea045c8403631275f9db", SkinModel.CLASSIC),
        ),
        Race.GIANT to listOf(
            SkinAttributes("https://textures.minecraft.net/texture/3dce5677c73f3aacf168a5c0c01ef5e0583d40049e4d73787b62eb1c620aea33", SkinModel.CLASSIC),
            SkinAttributes("https://textures.minecraft.net/texture/70d6711c4ab259b99683314545070308b080cbff31fb70760e6ff04150ded839", SkinModel.CLASSIC),
        ),
        Race.KOBOLD to listOf(
            SkinAttributes("https://textures.minecraft.net/texture/e82b41f67b337145c5eb83b9e001557480160a55e91ef863eb0bf05573842ce1", SkinModel.SLIM),
            SkinAttributes("https://textures.minecraft.net/texture/f437264990bf6fa66b2c5a3879a2dd0a8d5e6cca26a493e3464ae60fcb286bea", SkinModel.SLIM),
            SkinAttributes("https://textures.minecraft.net/texture/8880e1543c9d32c204de85bdc7aca7953d1a38c7e1981ffd8df4204473e6aaed", SkinModel.SLIM),
            SkinAttributes("https://textures.minecraft.net/texture/a7590f2cdb1c9cffa97da43a964122aae24f016322a1e8a2e091e1fbfdcefb64", SkinModel.SLIM),
            SkinAttributes("https://textures.minecraft.net/texture/791204d5a0c19d50d8236bfd110211abdfd7cbfebaec2cf632e0c8f6d61ccf9f", SkinModel.SLIM),
        ),
        Race.DRAGONBORN to listOf(
            SkinAttributes("https://textures.minecraft.net/texture/68e9d20af2e1fa13171c517bbceb49962afd0f66ec6164e120eef866a749d985", SkinModel.CLASSIC),
            SkinAttributes("https://textures.minecraft.net/texture/5ec95e091dc34073b61ef441fb44840bf1ccbe59e265c94ffeee6b7c6f22b58c", SkinModel.CLASSIC),
            SkinAttributes("https://textures.minecraft.net/texture/92c120852d8d22542b1e4e3686b49c9e63ffd72a013f428bfef94c9ddbdfc802", SkinModel.CLASSIC),
            SkinAttributes("https://textures.minecraft.net/texture/ee715ff87bd1aba54de407dd62ac996f601b9cd0df6b5a4065c60abf085293b9", SkinModel.CLASSIC),
            SkinAttributes("https://i.imgur.com/AWitbwy.png", SkinModel.CLASSIC),
            SkinAttributes("https://i.imgur.com/0gQK54V.png", SkinModel.CLASSIC),
            SkinAttributes("https://i.imgur.com/xbcXi0a.png", SkinModel.CLASSIC),
            SkinAttributes("https://i.imgur.com/Pwzz5TP.png", SkinModel.CLASSIC),
            SkinAttributes("https://i.imgur.com/CcGBTm9.png", SkinModel.CLASSIC),
        ),
        Race.GOBLIN to listOf(
            SkinAttributes("http://textures.minecraft.net/texture/138b33ad9a654b6692120dada757b24dab44b60e7f1bd751935da6172d8d056d", SkinModel.CLASSIC),
            SkinAttributes("http://textures.minecraft.net/texture/cc34207cd203191f0c1f43201009ae369721b800494435b5520107a6687cb706", SkinModel.CLASSIC),
            SkinAttributes("http://textures.minecraft.net/texture/faab2fd037c3997101bcf5cd483840494fae2132ebaf92342d9be1e4b8b67ec", SkinModel.CLASSIC),
        ),
        Race.VAMPIRE to listOf(
            SkinAttributes("http://textures.minecraft.net/texture/228e69446492b63480aa3f0f9a340aa8e2b0678daacdfc455a93ee80f6cc617e", SkinModel.CLASSIC),
            SkinAttributes("http://textures.minecraft.net/texture/7a75a5b5fa86a1c7db32bf660b4bfde0cfa8f45f967facb1076cba2f223e51cf", SkinModel.CLASSIC),
            SkinAttributes("http://textures.minecraft.net/texture/6977d34daf140b14b39a459b0972665010e6ab0c931479893863396738bb98d9", SkinModel.CLASSIC),
            SkinAttributes("http://textures.minecraft.net/texture/69621a2b433d77624381084377a5b187d53397458b696b4132ca8a4b000b313b", SkinModel.CLASSIC),
            SkinAttributes("http://textures.minecraft.net/texture/167b99ada72521a0962101b55d6ca8d782be16948b42271d3eab72935602ae95", SkinModel.CLASSIC),
            SkinAttributes("http://textures.minecraft.net/texture/3b18a4897a639b8c3b7b333a3686015417876128d595093e0f272e614470c71a", SkinModel.CLASSIC),
            SkinAttributes("http://textures.minecraft.net/texture/49844eb422869fdda39c280d7f8ad82f368fe3667a92ed9c6ebfbd8aca756c12", SkinModel.CLASSIC),
            SkinAttributes("http://textures.minecraft.net/texture/c598e1efc0399038cedeb5b384b3243ecb1a6bf4d9ca709a2d50beb79a105319", SkinModel.CLASSIC),
            SkinAttributes("http://textures.minecraft.net/texture/fb8219ad24cf870573920f33e3b6437d78b6ee50773f495e2ae3b2a4e72984ab", SkinModel.CLASSIC),
        ),
        Race.AMPHIBIAN to listOf(
            SkinAttributes("http://textures.minecraft.net/texture/e4afa01b747d9706650c1a7aafd137cb9b8c150ab3da8f05b06bef81f86b8380", SkinModel.CLASSIC),
            SkinAttributes("http://textures.minecraft.net/texture/320bf124be5efdbf2dc07b3a59303557840bb1d6f4848593a2496235f7c40822", SkinModel.CLASSIC),
            SkinAttributes("http://textures.minecraft.net/texture/a899705febcea98e43d10309f8629834fefd3ac2f7b3030d0831cc1bac61bece", SkinModel.CLASSIC),
        ),
        Race.CIBLE to listOf(
            SkinAttributes("http://textures.minecraft.net/texture/d582adadbbdc19dab650a6daf813ca929497eb1420e03e2adb37f8e548eca5d1", SkinModel.CLASSIC),
        ),
        Race.SNOLEM to listOf(
            SkinAttributes("http://textures.minecraft.net/texture/d1053b4372cbfcfb46c82cd0c8d3c7186e579c15bf559ffda5cfea6399ca8959", SkinModel.CLASSIC),
            SkinAttributes("http://textures.minecraft.net/texture/a4863b10011299357f1da326cccb1b6b4e08a54a852461f6cedc670b72e22946", SkinModel.CLASSIC),
            SkinAttributes("http://textures.minecraft.net/texture/c0c4dce34e45f95118520b78325cfb96713adac6fe7ff16bcb5725e42f00de7", SkinModel.CLASSIC),
        ),
        Race.AGAR to listOf(
            SkinAttributes("http://textures.minecraft.net/texture/5f1f9c719d635bb08c90101be53a62eaaf17f57ae901a2f1d7bf595a917b0b31", SkinModel.CLASSIC),
            SkinAttributes("https://i.imgur.com/4XlyGbj.png", SkinModel.CLASSIC),
            SkinAttributes("https://i.imgur.com/VefZbCU.png", SkinModel.CLASSIC),
            SkinAttributes("https://i.imgur.com/5q2eBfr.png", SkinModel.CLASSIC),
            SkinAttributes("https://i.imgur.com/XCZGDIt.png", SkinModel.CLASSIC),
            SkinAttributes("https://i.imgur.com/tzhd6su.png", SkinModel.CLASSIC),
        ),
    )

    fun getAttributes(race: Race): RaceAttributes? = races[race]
    fun getDefaultAttributes(): RaceAttributes = defaultAttributes
    fun getRandomSkin(race: Race, ignoreSkins: MutableList<SkinAttributes>? = null): SkinAttributes? =
        skins[race]?.filter { it !in (ignoreSkins ?: emptyList()) }?.random()
}
