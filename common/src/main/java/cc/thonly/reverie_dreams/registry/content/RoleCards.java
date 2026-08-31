package cc.thonly.reverie_dreams.registry.content;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.npc.NPCRoleType;
import cc.thonly.reverie_dreams.entity.npc.NPCSimpleRedirectEntity;
import cc.thonly.reverie_dreams.item.base.RoleCard;
import cc.thonly.reverie_dreams.item.template.RoleCardItem;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public class RoleCards {
    public static final RoleCard PROTAGONIST_GROUP = register(new RoleCard(ReverieDreams.id("protagonist_group"),
            16727357L,
            List.of(NPCRoleTypes.REIMU,
                    NPCRoleTypes.CYAN_REIMU,
                    NPCRoleTypes.MARISA)
    ).build());
    public static final RoleCard KOUMAKYOU = register(new RoleCard(ReverieDreams.id("koumakyou"),
            16716820L,
            List.of(NPCRoleTypes.RUMIA,
                    NPCRoleTypes.CIRNO,
                    NPCRoleTypes.MEIRIN,
                    NPCRoleTypes.PATCHOULI,
                    NPCRoleTypes.SAKUYA,
                    NPCRoleTypes.REMILIA,
                    NPCRoleTypes.FLANDRE)
    ).build());
    public static final RoleCard YOUYOUMU = register(new RoleCard(ReverieDreams.id("youyoumu"),
            16717035L,
            List.of(NPCRoleTypes.LETTY_WHITEROCK,
                    NPCRoleTypes.CHEN,
                    NPCRoleTypes.ALICE,
                    NPCRoleTypes.LILY_WHITE,
                    NPCRoleTypes.LUNASA_PRISMRIVER,
                    NPCRoleTypes.MERLIN_PRISMRIVER,
                    NPCRoleTypes.LYRICA_PRISMRIVER,
                    NPCRoleTypes.RAN,
                    NPCRoleTypes.YOUMU,
                    NPCRoleTypes.YUYUKO,
                    NPCRoleTypes.YUKARI)
    ).build());
    public static final RoleCard EIYASHOU = register(new RoleCard(ReverieDreams.id("eiyashou"),
            6160593L,
            List.of(NPCRoleTypes.MYSTIA_LORELEI,
                    NPCRoleTypes.WRIGGLE_NIGHTBUG,
                    NPCRoleTypes.KAMISHIRASAWA_KEINE,
                    NPCRoleTypes.REISEN,
                    NPCRoleTypes.ERIN,
                    NPCRoleTypes.HOURAISAN_KAGUYA,
                    NPCRoleTypes.HUZIWARA_NO_MOKOU)
    ).build());
    public static final RoleCard KAEIZUKA = register(new RoleCard(ReverieDreams.id("kaeizuka"),
            53595L,
            List.of(NPCRoleTypes.SHIKIEIKI_YAMAXANADU,
                    NPCRoleTypes.KAZAMI_YUKA)
    ).build());
    public static final RoleCard FUUJINROKU = register(new RoleCard(ReverieDreams.id("fuujinroku"),
            6934784L,
            List.of(NPCRoleTypes.KAGIYAMA_HINA,
                    NPCRoleTypes.INUBASHIRI_MOMIZI,
                    NPCRoleTypes.KAWASIRO_NITORI,
                    NPCRoleTypes.AYA,
                    NPCRoleTypes.KOCHIYA_SANAE,
                    NPCRoleTypes.YASAKA_KANAKO,
                    NPCRoleTypes.MORIYA_SUWAKO)
    ).build());
    public static final RoleCard CHIREIDEN = register(new RoleCard(ReverieDreams.id("chireiden"),
            41777L,
            List.of(NPCRoleTypes.KISUME,
                    NPCRoleTypes.KURODANI_YAMAME,
                    NPCRoleTypes.MIZUHASHI_PARSEE,
                    NPCRoleTypes.HOSHIGUMA_YUGI,
                    NPCRoleTypes.KAENBYOU_RIN,
                    NPCRoleTypes.KOMEIJI_SATORI,
                    NPCRoleTypes.REIUJI_UTSUH,
                    NPCRoleTypes.KOMEIJI_KOISHI,
                    NPCRoleTypes.WHITE_KOMEIJI_KOISHI)
    ).build());
    public static final RoleCard SEIRENSEN = register(new RoleCard(ReverieDreams.id("seirensen"),
            1506915L,
            List.of(NPCRoleTypes.NAZRIN,
                    NPCRoleTypes.TATARA_KOGASA,
                    NPCRoleTypes.HOUJUU_NUE)
    ).build());
    public static final RoleCard SHINREIBYOU = register(new RoleCard(ReverieDreams.id("shinreibyou"),
            16775603L,
            List.of(NPCRoleTypes.KASODANI_KYOUKO,
                    NPCRoleTypes.MIYAKO_YOSHIKA,
                    NPCRoleTypes.KAKU_SEIGA,
                    NPCRoleTypes.SOGA_NO_TOZIKO,
                    NPCRoleTypes.MONONOBE_NO_FUTO,
                    NPCRoleTypes.TOYOSATOMIMI_NO_MIKO,
                    NPCRoleTypes.HOUJUU_NUE,
                    NPCRoleTypes.HUTATSUIWA_MAMIZOU)
    ).build());
    public static final RoleCard KISHINJOU = register(new RoleCard(ReverieDreams.id("kishinjou"),
            16761692L,
            List.of(
                    NPCRoleTypes.WAKASAGIHIME,
                    NPCRoleTypes.SEKIBANKI,
                    NPCRoleTypes.IMAIZUMI_KAGEROU,
                    NPCRoleTypes.KIJIN_SEIJIA,
                    NPCRoleTypes.SUKUNA_SHINMYOUMARU,
                    NPCRoleTypes.HORIKAWA_RAIKO
            )
    ).build());
    public static final RoleCard KANJUDEN = register(new RoleCard(ReverieDreams.id("kanjuden"),
            14024704L,
            List.of(
                    NPCRoleTypes.SEIRAN,
                    NPCRoleTypes.RINGO,
                    NPCRoleTypes.DOREMY_SWEET,
                    NPCRoleTypes.KISIN_SAGUME,
                    NPCRoleTypes.CLOWNPIECE,
                    NPCRoleTypes.JUNKO,
                    NPCRoleTypes.HECATIA_LAPISLAZULI
            )
    ).build());
    public static final RoleCard TENKUUSHOU = register(new RoleCard(ReverieDreams.id("tenkuushou"),
            6750023L,
            List.of(
                    NPCRoleTypes.ETERNITY_LARVA,
                    NPCRoleTypes.SAKUTA_NEMUNO,
                    NPCRoleTypes.KOMANO_AUNN,
                    NPCRoleTypes.YATADERA_NARUMI,
                    NPCRoleTypes.NISHIDA_SATONO,
                    NPCRoleTypes.TEIREIDA_MAI,
                    NPCRoleTypes.MATARA_OKINA
            )
    ).build());
    public static final RoleCard KIKEIJUU = register(new RoleCard(ReverieDreams.id("kikeijuu"),
            12386304L,
            List.of(
                    NPCRoleTypes.EBISU_EIKA,
                    NPCRoleTypes.USHIZAKI_URUMI,
                    NPCRoleTypes.NIWATARI_KUTAKA,
                    NPCRoleTypes.KITCHO_YACHIE,
                    NPCRoleTypes.JOUTOUGU_MAYUMI,
                    NPCRoleTypes.HANIYASUSHIN_KEIKI,
                    NPCRoleTypes.KUROKOMA_SAKI
            )
    ).build());
    public static final RoleCard KOURYUUDOU = register(new RoleCard(ReverieDreams.id("kouryuudou"),
            15853109L,
            List.of(
                    NPCRoleTypes.GOUTOKUZI_MIKE,
                    NPCRoleTypes.YAMASHIRO_TAKANE,
                    NPCRoleTypes.KOMAKUSA_SANNYO,
                    NPCRoleTypes.TAMATSUKURI_MISUMARU,
                    NPCRoleTypes.KUDAMAKI_TSUKASA,
                    NPCRoleTypes.IIZUNAMARU_MEGUMU,
                    NPCRoleTypes.TENKYU_CHIMATA,
                    NPCRoleTypes.HIMEMUSHI_MOMOYO
            )
    ).build());
    public static final RoleCard JUUOUEN = register(new RoleCard(ReverieDreams.id("juuouen"),
            7252587L,
            List.of(
                    NPCRoleTypes.SON_BITEN,
                    NPCRoleTypes.MITSUGASHIRA_ENOKO,
                    NPCRoleTypes.TENKAJIN_CHIYARI,
                    NPCRoleTypes.YOMOTSU_HISAMI,
                    NPCRoleTypes.NIPPAKU_ZANMU
            )
    ).build());
    public static final RoleCard KINJOUKYOU = register(new RoleCard(ReverieDreams.id("kinjoukyou"),
            12969971L,
            List.of(
                    NPCRoleTypes.CHIRIZUKA_UBAME,
                    NPCRoleTypes.HOUJU_CHIMI,
                    NPCRoleTypes.MICHIGAMI_NAREKO,
                    NPCRoleTypes.YUIMAN_ASAMA,
                    NPCRoleTypes.WATATSUKI_TOYOHIME,
                    NPCRoleTypes.IWANAGA_ARIYA,
                    NPCRoleTypes.WATARI_NINA
            )
    ).build());

    // 其他作品
    public static final RoleCard SANGETSUSEI = register(new RoleCard(ReverieDreams.id("sangetsusei"),
            16770140L,
            List.of(
                    NPCRoleTypes.STAR,
                    NPCRoleTypes.LUNAR,
                    NPCRoleTypes.SUNNY
            )
    ).build());
    public static final RoleCard HIFUU = register(new RoleCard(ReverieDreams.id("hifuu"),
            8931582L,
            List.of(
                    NPCRoleTypes.USAMI_RENKO,
                    NPCRoleTypes.MARIBEL_HEARN
            )
    ).build());
    public static final RoleCard TASOGARE_FURONTIA = register(new RoleCard(ReverieDreams.id("tasogare_furontia"),
            16683336L,
            List.of(
                    NPCRoleTypes.SUIKA,
                    NPCRoleTypes.TENSHI
            )
    ).build());

    public static RoleCard register(RoleCard roleCard) {
        return register(roleCard.getId(), roleCard);
    }

    public static RoleCard register(String name, RoleCard roleCard) {
        return register(ReverieDreams.id(name), roleCard);
    }

    public static RoleCard register(Identifier key, RoleCard roleCard) {
        return BuiltInRegistryProviders.register(BuiltInRegistryProviders.ROLE_CARD, key, roleCard);
    }

    public static void bootstrap(RegistryProvider<RoleCard> registry) {

    }

    @SuppressWarnings("unchecked")
    public static void handleRoleCardDialog(ServerPlayer player, ServerboundCustomClickActionPacket packet) {
        Optional<Tag> payload = packet.payload();
        if (payload.isEmpty()) {
            return;
        }
        Tag element = payload.get();
        if (!(element instanceof CompoundTag compound)) {
            return;
        }
        Optional<String> siOptional = compound.getString("session_id");
        Optional<String> eiOptional = compound.getString("entity_id");
        if (siOptional.isEmpty()) {
            return;
        }
        if (eiOptional.isEmpty()) {
            return;
        }
        String sessionId = siOptional.get();
        String entityId = eiOptional.get();
        RoleCardItem.UsingData usingData = RoleCardItem.USING_DATA_MAP.get(sessionId);
        if (usingData == null) {
            return;
        }
        if (entityId.equals("random")) {
            RoleCard roleCard = usingData.getRoleCard();
            Optional<NPCRoleType> roleWrapper = roleCard.random();
            if (roleWrapper.isPresent()) {
                ServerPlayer serverPlayer = usingData.getPlayer();
                ServerLevel world = usingData.getWorld();
                ItemStack itemStack = usingData.getItemStack();
                NPCRoleType role = roleWrapper.get();
                itemStack.consume(1, serverPlayer);
                EntityType<NPCSimpleRedirectEntity> entityType = (EntityType<NPCSimpleRedirectEntity>) (Object) role.get().value();
                entityType.spawn(world, usingData.getBlockPos(), EntitySpawnReason.SPAWN_ITEM_USE);

                world.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), SoundEvents.BUCKET_FILL, serverPlayer.getSoundSource(), 2.0f, 1.0f);
            }
            RoleCardItem.USING_DATA_MAP.remove(sessionId);
            return;
        }
        Identifier identifier = Identifier.tryParse(entityId);
        if (identifier == null) {
            return;
        }
        NPCRoleType role = usingData.getId2Role().get(identifier);
        if (role == null) {
            return;
        }
        ServerPlayer serverPlayer = usingData.getPlayer();
        ServerLevel world = usingData.getWorld();
        ItemStack itemStack = usingData.getItemStack();
        itemStack.consume(1, serverPlayer);
        EntityType<NPCSimpleRedirectEntity> entityType = (EntityType<NPCSimpleRedirectEntity>) (Object) role.get().value();
        entityType.spawn(world, usingData.getBlockPos(), EntitySpawnReason.SPAWN_ITEM_USE);

        world.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), SoundEvents.BUCKET_FILL, serverPlayer.getSoundSource(), 2.0f, 1.0f);
        RoleCardItem.USING_DATA_MAP.remove(sessionId);
    }
}
