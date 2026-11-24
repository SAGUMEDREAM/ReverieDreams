package cc.thonly.reverie_dreams.client.registry;

import cc.thonly.reverie_dreams.client.renderer.entity.*;
import cc.thonly.reverie_dreams.data.npc.NPCRoleType;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.aquatic.AquaticSpecies;
import cc.thonly.reverie_dreams.registry.content.entity.RDAquaticEntities;
import cc.thonly.reverie_dreams.entity.npc.NPCSimpleRedirectEntity;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.client.renderer.entity.BeeRenderer;
import net.minecraft.client.renderer.entity.CowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.world.entity.EntityType;

public class RDEntityRenderers {
    public static void initialize() {
        // EmptyRenderer
        for (NPCRoleType npcRole : BuiltInRegistryProviders.NPC_ROLE_TYPE) {
            RegistrySupplier<EntityType<NPCSimpleRedirectEntity>> entityType = npcRole.getEntityType();
            EntityRendererRegistry.register(entityType, EmptyRenderer::new);
        }
        // Projectile
        // NueTrident / Gungnir 使用独立的投掷物实体类型，不覆盖原版三叉戟。
        EntityRendererRegistry.register(RDEntityTypes.THROWN_NUE_TRIDENT, ThrownNueTridentRenderer::new);
        EntityRendererRegistry.register(RDEntityTypes.THROWN_GUNGNIR, ThrownGungnirRenderer::new);
        EntityRendererRegistry.register(RDEntityTypes.DANMAKU, DanmakuLikeRenderer::new);
        EntityRendererRegistry.register(RDEntityTypes.KNIFE, DanmakuLikeRenderer::new);
        EntityRendererRegistry.register(RDEntityTypes.DANMAKU_FAMILIAR, DanmakuFamiliarRenderer::new);
        EntityRendererRegistry.register(RDEntityTypes.DANMAKU_LASER, DanmakuLaserRenderer::new);
        EntityRendererRegistry.register(RDEntityTypes.FISHING_BOBBER, NPCFishingHookRenderer::new);
        EntityRendererRegistry.register(RDEntityTypes.THROWN_CUISINE_ITEM, ThrownItemRenderer::new);
        EntityRendererRegistry.register(RDEntityTypes.THROWN_EXORCISM_PAPER, ThrownItemRenderer::new);
        EntityRendererRegistry.register(RDEntityTypes.BAGUA_FIREBALL, ThrownItemRenderer::new);
        EntityRendererRegistry.register(RDEntityTypes.THROWN_ICE_BLOCK, ThrownItemRenderer::new);
        EntityRendererRegistry.register(RDEntityTypes.THROWN_ICE_SNOWBALL, ThrownItemRenderer::new);

        // LivingEntity
        EntityRendererRegistry.register(RDEntityTypes.YOUKAI_CAT, ForestYoukaiRenderer::new);
        EntityRendererRegistry.register(RDEntityTypes.YOUKAI_TANUKI, ForestYoukaiRenderer::new);
        EntityRendererRegistry.register(RDEntityTypes.YOUKAI_FOX, ForestYoukaiRenderer::new);
        EntityRendererRegistry.register(RDEntityTypes.WILD_PIG, WildPigRenderer::new);
        EntityRendererRegistry.register(RDEntityTypes.WAGYU, CowRenderer::new);
        EntityRendererRegistry.register(RDEntityTypes.KILLER_BEE, BeeRenderer::new);
        EntityRendererRegistry.register(RDEntityTypes.BAGUA_FURNACE, BaguaFurnaceRenderer::new);
        EntityRendererRegistry.register(RDEntityTypes.HAIRBALL, HairballRenderer::new);
        EntityRendererRegistry.register(RDEntityTypes.UFO, UfoRenderer::new);
        EntityRendererRegistry.register(RDEntityTypes.SCARECROW, ScarecrowRenderer::new);
        EntityRendererRegistry.register(RDEntityTypes.MUSHROOM_MONSTER, MushroomMonsterRenderer::new);
        EntityRendererRegistry.register(RDEntityTypes.FUMO_SELLER_VILLAGER, VillagerSellerRenderer::new);
        EntityRendererRegistry.register(RDEntityTypes.TAVERN_VILLAGER, VillagerSellerRenderer::new);
        EntityRendererRegistry.register(RDEntityTypes.YOUKAI_RABBIT, YoukaiRabbitRenderer::new);
        EntityRendererRegistry.register(RDEntityTypes.LEGACY_MOON_RABBIT, YoukaiRabbitRenderer::new);

        for (var species : AquaticSpecies.values()) {
            EntityRendererRegistry.register(RDAquaticEntities.type(species),
                    context -> new CuisineAquaticRenderer<>(context, species));
        }

        // Misc
        EntityRendererRegistry.register(RDEntityTypes.ORE_ESP, OreEspRenderer::new);
        EntityRendererRegistry.register(RDEntityTypes.GAP_PORTAL, GapPortalRenderer::new);
        EntityRendererRegistry.register(RDEntityTypes.DREAM_COCOON, DreamCocoonRenderer::new);
        EntityRendererRegistry.register(RDEntityTypes.CURSED_DECOY, CursedDecoyRenderer::new);
        EntityRendererRegistry.register(RDEntityTypes.MAGIC_BROOM, MagicBroomRenderer::new);
        EntityRendererRegistry.register(RDEntityTypes.WHEEL_CHAIR, WheelchairRenderer::new);
        EntityRendererRegistry.register(RDEntityTypes.SEAT, EmptyRenderer::new);
        EntityRendererRegistry.register(RDEntityTypes.CUSHION_SEAT, EmptyRenderer::new);

        // NPCLike
        EntityRendererProvider<BaseNPCLikeEntity> slimNPCLikeRendererProvider = context -> new BaseNPCLikeEntityRenderer<>(context, true);
        EntityRendererProvider<BaseNPCLikeEntity> wideNPCLikeRendererProvider = context -> new BaseNPCLikeEntityRenderer<>(context, false);
        EntityRendererRegistry.register(RDEntityTypes.NPC_SIMPLE_ENTITY, slimNPCLikeRendererProvider);
        EntityRendererRegistry.register(RDEntityTypes.HUMAN, wideNPCLikeRendererProvider);
        EntityRendererRegistry.register(RDEntityTypes.GHOST, slimNPCLikeRendererProvider);
        EntityRendererRegistry.register(RDEntityTypes.YOUSEI, GensokyoFairyRenderer::new);
        EntityRendererRegistry.register(RDEntityTypes.MAID_YOUSEI, GensokyoFairyRenderer::new);
        EntityRendererRegistry.register(RDEntityTypes.SUNFLOWER_YOUSEI, GensokyoFairyRenderer::new);
        EntityRendererRegistry.register(RDEntityTypes.ICE_FAIRY, IceFairyLikeEntityRenderer::new);
        EntityRendererRegistry.register(RDEntityTypes.GOBLIN, wideNPCLikeRendererProvider);
        EntityRendererRegistry.register(RDEntityTypes.RABBIT_UNIT, RabbitUnitRenderer::new);
        EntityRendererRegistry.register(RDEntityTypes.WATER_ELEMENTAL, wideNPCLikeRendererProvider);
        EntityRendererRegistry.register(RDEntityTypes.FIRE_ELEMENTAL, wideNPCLikeRendererProvider);
        EntityRendererRegistry.register(RDEntityTypes.ICE_ELEMENTAL, wideNPCLikeRendererProvider);
        EntityRendererRegistry.register(RDEntityTypes.ONI, OniRenderer::new);
        EntityRendererRegistry.register(RDEntityTypes.WHITE_WOLF_TENGU, TenguRenderer::new);
        EntityRendererRegistry.register(RDEntityTypes.CROW_TENGU, TenguRenderer::new);
    }
}
