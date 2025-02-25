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
        val url: URL,
        val model: SkinModel,
    )

    private val skins: Map<Race, List<SkinAttributes>> = mutableMapOf(
        Race.SKELETON to listOf(
            SkinAttributes(
                URI("http://textures.minecraft.net/texture/cb29cf3bf2f27953388d9563bf9e83bb1aedfc4705f3fa7c7748f0a79766cd9e").toURL(),
                SkinModel.SLIM
            ),
            SkinAttributes(
                URI("http://textures.minecraft.net/texture/bc230c518191fc2f06c091a70760e9d8692a24b096e964b06183979ede043fcd").toURL(),
                SkinModel.SLIM
            )
        ),
        Race.ELF to listOf(
            SkinAttributes(URI("http://textures.minecraft.net/texture/30f8b848007375d00e146594bc0947538fd197b1ef1f1fbaa955b207820ee896").toURL(), SkinModel.SLIM),
            SkinAttributes(URI("http://textures.minecraft.net/texture/8923b9cf174c9a85f482149430cee40192dda162bb3fcc60ea3b194e5b0fa430").toURL(), SkinModel.SLIM),
            SkinAttributes(URI("http://textures.minecraft.net/texture/dc0eeee07ae1e666bfa67d7420a4152c0c03873214fe1aae6b700e8f992099c7").toURL(), SkinModel.SLIM),
            SkinAttributes(URI("http://textures.minecraft.net/texture/cdb53d816f27454ad80555a1ddad984ee50bd47ca5b4eb9dd62f401fd207535f").toURL(), SkinModel.SLIM),
            SkinAttributes(URI("http://textures.minecraft.net/texture/a0194eefe3e064d6110f49d5c1141d9a197f9584d58290ade5ab4a9e96cde316").toURL(), SkinModel.SLIM),
            SkinAttributes(URI("http://textures.minecraft.net/texture/d77a52ffba195eea705efd05828727f030b851c3303490aa800d3d8cf75f61e4").toURL(), SkinModel.SLIM),
            SkinAttributes(URI("http://textures.minecraft.net/texture/f5fab69f2cb9cdf1eb4b6ee26e214a32e3f3013d39b098ff71714495ddd00b04").toURL(), SkinModel.SLIM),
            SkinAttributes(URI("http://textures.minecraft.net/texture/d8c0d35ae0aec2113bec984f562c77f8750004edb93cddeb8f687d26a6252747").toURL(), SkinModel.SLIM),
            SkinAttributes(URI("http://textures.minecraft.net/texture/d8c0d35ae0aec2113bec984f562c77f8750004edb93cddeb8f687d26a6252747").toURL(), SkinModel.SLIM),
            SkinAttributes(URI("http://textures.minecraft.net/texture/c2b980c68a61c0ce5b1cc28d844cb51966fd1df6d0ac2dc6a465b1568f45b520").toURL(), SkinModel.SLIM),
            SkinAttributes(URI("http://textures.minecraft.net/texture/5c1d14002f71b03025c8a20b79dd3c88832cf03ced3a0f7e60cc007090d642fe").toURL(), SkinModel.SLIM),
            SkinAttributes(URI("http://textures.minecraft.net/texture/760c995adbe1c5da676b23ee40537fe7f0ec3ba23d34c336842769c642c963a4").toURL(), SkinModel.SLIM),
            SkinAttributes(URI("http://textures.minecraft.net/texture/a8f94a1c1bbf1d23fd3956b03d3d2f924c242eee945fc658b71032510227643").toURL(), SkinModel.SLIM),
        ),
        Race.HUMAN to listOf(
            SkinAttributes(URI("http://textures.minecraft.net/texture/4953b094d0ffdc3c3fc60127f49f6f42ab0837f1c593b4c52969bf618f09a3b5").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/61d701d8b466449fe583b46e7fa1dd888d474196766d8ea74e88fb0ddb1bdb19").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/eaad997f29cd84d394921256e7c3514dedd78d5e778e09ab85ef78054a5e70b").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/f7e065602b695d2dd4a676b1a37fcbe94efbf05076d6b47a375300117c242c28").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/56c594ce31308445b0175d346211ecb3bd70f69dbafc5ee63ff4cdf0a52ad7cc").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/cabd4c338f75cba803cdeb43b8d230048aca5d1480608c50fd85a80b5109b83").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/b953ce30f614f85f28028f66d00da70b04721e882949d8c6ff2aead0f8a94d03").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/adde7361b8d71ab154dc38c9626a427202ec2b2ed31a2f125fe0a3fd50a8c638").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/f11f737e1dec5182a0ae3907696e385fb86da45c85f963bf608f488ded2e8e0e").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/e28e5cd63031972dc7d3e52cdc2a9c3d5fb6a17adef1896d1e55dc9dfa733789").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/122427196a7671615349b0f9e267e1ee2add2cca6ecbac7d2d176c428727d79f").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/76fc4f3547f6bc9d6c45e5d469b619bd4375de3c31eb9acc061af8b50210529e").toURL(), SkinModel.CLASSIC),
        ),
        Race.DWARF to listOf(
            SkinAttributes(URI("http://textures.minecraft.net/texture/def3fd1c2caefffe86b7ebd9ee1a57c9a7a04fd0a0b563c46dce7a2af4a52").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/9772642ffccfc9e11b350c874f2c84678fc08044b51e7a8e3a0919f8f788ed9a").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/4936d79e4fda859d608c853c10ed42761b9379920b00b155433c8293f91b3e6f").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/8f0790d9758b14ce5f417816c015728745a5d8a43048a9d0a41d0e168ed27d60").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/cade1161319f7f18be5d5e51f1f7f147fb1d2626e8a76ceee446558fa5472205").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/d8674b2cfa7a471fe1a23d68ea6654286d5b435eea045c8403631275f9db").toURL(), SkinModel.CLASSIC),
        ),
        Race.GIANT to listOf(
            SkinAttributes(URI("https://textures.minecraft.net/texture/3dce5677c73f3aacf168a5c0c01ef5e0583d40049e4d73787b62eb1c620aea33").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("https://textures.minecraft.net/texture/70d6711c4ab259b99683314545070308b080cbff31fb70760e6ff04150ded839").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("https://textures.minecraft.net/texture/aa1460d5d5ef9ef8341d452cce6e878661c0decd96ca1a26574447a3c94768c6").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("https://textures.minecraft.net/texture/c7f4d7637d0eeaa797f21a396fb78cccd0112ac080abcac3fe39a9c32a75").toURL(), SkinModel.CLASSIC),
        ),
        Race.KOBOLD to listOf(
            SkinAttributes(URI("https://textures.minecraft.net/texture/e82b41f67b337145c5eb83b9e001557480160a55e91ef863eb0bf05573842ce1").toURL(), SkinModel.SLIM),
            SkinAttributes(URI("https://textures.minecraft.net/texture/f437264990bf6fa66b2c5a3879a2dd0a8d5e6cca26a493e3464ae60fcb286bea").toURL(), SkinModel.SLIM),
            SkinAttributes(URI("https://textures.minecraft.net/texture/8880e1543c9d32c204de85bdc7aca7953d1a38c7e1981ffd8df4204473e6aaed").toURL(), SkinModel.SLIM),
            SkinAttributes(URI("https://textures.minecraft.net/texture/a7590f2cdb1c9cffa97da43a964122aae24f016322a1e8a2e091e1fbfdcefb64").toURL(), SkinModel.SLIM),
            SkinAttributes(URI("https://textures.minecraft.net/texture/791204d5a0c19d50d8236bfd110211abdfd7cbfebaec2cf632e0c8f6d61ccf9f").toURL(), SkinModel.SLIM),
        ),
        Race.DRAGONBORN to listOf(
            SkinAttributes(URI("https://textures.minecraft.net/texture/68e9d20af2e1fa13171c517bbceb49962afd0f66ec6164e120eef866a749d985").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("https://textures.minecraft.net/texture/5ec95e091dc34073b61ef441fb44840bf1ccbe59e265c94ffeee6b7c6f22b58c").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("https://textures.minecraft.net/texture/92c120852d8d22542b1e4e3686b49c9e63ffd72a013f428bfef94c9ddbdfc802").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("https://textures.minecraft.net/texture/2fedc9bf70ef582297978ff1d2f5a2b78dfbeffa087a60c4521c7d48ffa65583").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("https://textures.minecraft.net/texture/ee715ff87bd1aba54de407dd62ac996f601b9cd0df6b5a4065c60abf085293b9").toURL(), SkinModel.CLASSIC),
        ),
        Race.GOBLIN to listOf(
            SkinAttributes(URI("http://textures.minecraft.net/texture/138b33ad9a654b6692120dada757b24dab44b60e7f1bd751935da6172d8d056d").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/cc34207cd203191f0c1f43201009ae369721b800494435b5520107a6687cb706").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/28c6810e5de4c63c513a37eaaaea221e341f6826871378f9308230d0b5f1baaa").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/7cfde12d095210744685f64246cf9ced1d8e5249973511a2e27bba1bfaa8754c").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/faab2fd037c3997101bcf5cd483840494fae2132ebaf92342d9be1e4b8b67ec").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/ae5c92f074a6b85c2ef952493141d370c70e567092a42ee52a157fee02e5be09").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/609c7b9f9830a4b2b144e395597f30e51c869a00ccd6abcd537eedfe075cf59f").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/b6980ae536635ee59d765e05aa8a32adba879bdcea5841c29f475b12a42a11c3").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/27cbee5ad2565e89d18b1e7e6d423991bad67432dcd4798e13a59faaab04c8f").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/f3cb60325f3846795a7431cba956b2d5b3712e273f140c1b4ec9e5a09a475060").toURL(), SkinModel.CLASSIC),
        ),
        Race.VAMPIRE to listOf(
            SkinAttributes(URI("http://textures.minecraft.net/texture/228e69446492b63480aa3f0f9a340aa8e2b0678daacdfc455a93ee80f6cc617e").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/11157794d3d5992f928a49f3ccfa1a39ea44c55d7ad0d9221e601203846db969").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/22da766590d993318cccaeebce18591f197f44b926b80e1ff690cc02ac294e80").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/7a75a5b5fa86a1c7db32bf660b4bfde0cfa8f45f967facb1076cba2f223e51cf").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/6977d34daf140b14b39a459b0972665010e6ab0c931479893863396738bb98d9").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/69621a2b433d77624381084377a5b187d53397458b696b4132ca8a4b000b313b").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/167b99ada72521a0962101b55d6ca8d782be16948b42271d3eab72935602ae95").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/3b18a4897a639b8c3b7b333a3686015417876128d595093e0f272e614470c71a").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/49844eb422869fdda39c280d7f8ad82f368fe3667a92ed9c6ebfbd8aca756c12").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/e0588cb82175264005ad312e2a43a7d1d439fba5c9c2fe123755e245e46c1977").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/c598e1efc0399038cedeb5b384b3243ecb1a6bf4d9ca709a2d50beb79a105319").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/fb8219ad24cf870573920f33e3b6437d78b6ee50773f495e2ae3b2a4e72984ab").toURL(), SkinModel.CLASSIC),
        ),
        Race.AMPHIBIAN to listOf(
            SkinAttributes(URI("http://textures.minecraft.net/texture/cc963c59569b5bbf24495b5772b523a101d7cda573f415bb13cfcb14ece2289d").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/76d423442391fab674ceb92b267bb622daefe3504cec35b6d54d43b2a8bd1c80").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/e43c06a535c9c236ea4e5af9e26a3f89ba0a585daaa94ee4ec02cb3a004bfe10").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/e4afa01b747d9706650c1a7aafd137cb9b8c150ab3da8f05b06bef81f86b8380").toURL(), SkinModel.CLASSIC),
        ),
        Race.CIBLE to listOf(
            SkinAttributes(URI("http://textures.minecraft.net/texture/49d830e31c69884a0b22a796194e628107dcfca2e76c2e4ca378ea5c44f6dfe6").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/286fed0284cc87c4f548b59aea75634694dfe113d0be72d7968501e3bfcec36").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/5ce9b62d236c1c5c744363f76d4a9a2a26f132fc4dc7a4c98d531f7706ef89b3").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/bd7aa9438cf10b50a2d5d13d5b9e26bc2c5abf465cdd32e4dc43548a877038b5").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/d582adadbbdc19dab650a6daf813ca929497eb1420e03e2adb37f8e548eca5d1").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/7e68f010c02ecbf710050aa312a7ab36438d43e5781c2fe37b0590a736c95abd").toURL(), SkinModel.CLASSIC),
        ),
        Race.SNOLEM to listOf(
            SkinAttributes(URI("http://textures.minecraft.net/texture/d1053b4372cbfcfb46c82cd0c8d3c7186e579c15bf559ffda5cfea6399ca8959").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/a4863b10011299357f1da326cccb1b6b4e08a54a852461f6cedc670b72e22946").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/6db67fb2bfca9906d3721d25ff527c19c30a5bd3d6a3ac54329d10823609a86c").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/c0c4dce34e45f95118520b78325cfb96713adac6fe7ff16bcb5725e42f00de7").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/9f1791edbaca2d13cfb984e92c99a8eb53266d2a75d0ea84b97774939728ea00").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/e8241c3910197df7dcf1f3ce760da3a999d640f165096819e85371807b02abc8").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/87ba146245da0165e6a459d0dc2d12d77a969032d1c56aa0b0916059f202ddd").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/968eef0f85bad00cef6e4d4e232db0383e8d132fb5fc3b332da6b9113e1d86ed").toURL(), SkinModel.CLASSIC),
        ),
        Race.AGAR to listOf(
            SkinAttributes(URI("http://textures.minecraft.net/texture/1e0086f60c23da16a246b334c453d09de6ef456856452049ba13bae374d83040").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/a8c8dbc5aa666b72e17483869289c79d881aa23b2ddafb47534e70ec3a60443f").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/ed7ca83bb6b24c2c62698d4c541cf8a4fbef80011302238382d8ab5fda23edfd").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/3a4f10a9660b9aafb9711dde2eef729ed916dce0a41dc2b821cda0f5c7e75bb5").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/fbd70dc7a0a2b446cf2fc8d98497658cea39ed65d41fe10a9cd2039cf967933a").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/5f1f9c719d635bb08c90101be53a62eaaf17f57ae901a2f1d7bf595a917b0b31").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/dc9d442b68199ae911843a6ce53fe5a2e28ecdab788fa50826fcaee1035ce1af").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/c77d0aacf4cad45fbb663767452dc9d2e1014db2b15bfecedcf07b823f500e76").toURL(), SkinModel.CLASSIC),
            SkinAttributes(URI("http://textures.minecraft.net/texture/f65efea6b2d8f5cc171a683e2906843441ff9464535ac6b9925ab228d6f6f351").toURL(), SkinModel.CLASSIC),
        )
    )

    fun getAttributes(race: Race): RaceAttributes? = races[race]
    fun getDefaultAttributes(): RaceAttributes = defaultAttributes
    fun getRandomSkin(race: Race, ignoreSkins: MutableList<SkinAttributes>? = null): SkinAttributes? = skins[race]?.filter { it !in ignoreSkins ?: emptyList() }?.random()
}
