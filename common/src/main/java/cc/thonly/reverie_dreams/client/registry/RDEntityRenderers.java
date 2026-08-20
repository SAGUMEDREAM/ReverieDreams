package cc.thonly.reverie_dreams.client.registry;

import cc.thonly.reverie_dreams.client.renderer.entity.*;
import cc.thonly.reverie_dreams.data.npc.NPCRoleType;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.npc.NPCSimpleRedirectEntity;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.client.renderer.entity.BeeRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RabbitRenderer;
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
        EntityRendererRegistry.register(RDEntityTypes.DANMAKU, DanmakuLikeRenderer::new);
        EntityRendererRegistry.register(RDEntityTypes.KNIFE, DanmakuLikeRenderer::new);
        EntityRendererRegistry.register(RDEntityTypes.FISHING_BOBBER, NPCFishingHookRenderer::new);
        EntityRendererRegistry.register(RDEntityTypes.THROWN_CUISINE_ITEM, ThrownItemRenderer::new);

        // LivingEntity
        EntityRendererRegistry.register(RDEntityTypes.WILD_PIG, WildPigRenderer::new);
        EntityRendererRegistry.register(RDEntityTypes.KILLER_BEE, BeeRenderer::new);
        EntityRendererRegistry.register(RDEntityTypes.BAGUA_FURNACE, BaguaFurnaceRenderer::new);
        EntityRendererRegistry.register(RDEntityTypes.HAIRBALL, HairballRenderer::new);
        EntityRendererRegistry.register(RDEntityTypes.UFO, UfoRenderer::new);
        EntityRendererRegistry.register(RDEntityTypes.SCARECROW, ScarecrowRenderer::new);
        EntityRendererRegistry.register(RDEntityTypes.MUSHROOM_MONSTER, MushroomMonsterRenderer::new);
        EntityRendererRegistry.register(RDEntityTypes.FUMO_SELLER_VILLAGER, VillagerSellerRenderer::new);
        EntityRendererRegistry.register(RDEntityTypes.TAVERN_VILLAGER, VillagerSellerRenderer::new);
        EntityRendererRegistry.register(RDEntityTypes.MOON_RABBIT, RabbitRenderer::new);

        // Misc
        EntityRendererRegistry.register(RDEntityTypes.ORE_ESP, OreEspRenderer::new);
        EntityRendererRegistry.register(RDEntityTypes.MAGIC_BROOM, MagicBroomRenderer::new);
        EntityRendererRegistry.register(RDEntityTypes.WHEEL_CHAIR, WheelchairRenderer::new);
        EntityRendererRegistry.register(RDEntityTypes.SEAT, EmptyRenderer::new);

        // NPCLike
        EntityRendererProvider<BaseNPCLikeEntity> slimNpcLikeRendererProvider = context -> new BaseNPCLikeEntityRenderer<>(context, true);
        EntityRendererProvider<BaseNPCLikeEntity> wideNpcLikeRendererProvider = context -> new BaseNPCLikeEntityRenderer<>(context, false);
        EntityRendererProvider<BaseNPCLikeEntity> slimYouseiWingLikeRendererProvider = context -> new YouseiWingLikeEntityRenderer<>(context, true);
        EntityRendererProvider<BaseNPCLikeEntity> wideYouseiWingLikeRendererProvider = context -> new YouseiWingLikeEntityRenderer<>(context, false);
        EntityRendererRegistry.register(RDEntityTypes.NPC_SIMPLE_ENTITY, slimNpcLikeRendererProvider);
        EntityRendererRegistry.register(RDEntityTypes.GHOST, slimNpcLikeRendererProvider);
        EntityRendererRegistry.register(RDEntityTypes.YOUSEI, slimYouseiWingLikeRendererProvider);
        EntityRendererRegistry.register(RDEntityTypes.MAID_YOUSEI, slimYouseiWingLikeRendererProvider);
        EntityRendererRegistry.register(RDEntityTypes.SUNFLOWER_YOUSEI, slimYouseiWingLikeRendererProvider);
        EntityRendererRegistry.register(RDEntityTypes.GOBLIN, wideNpcLikeRendererProvider);
        EntityRendererRegistry.register(RDEntityTypes.RABBIT_UNIT, slimNpcLikeRendererProvider);
        EntityRendererRegistry.register(RDEntityTypes.WATER_ELEMENTAL, wideNpcLikeRendererProvider);
        EntityRendererRegistry.register(RDEntityTypes.FIRE_ELEMENTAL, wideNpcLikeRendererProvider);
        EntityRendererRegistry.register(RDEntityTypes.ICE_ELEMENTAL, wideNpcLikeRendererProvider);
        EntityRendererRegistry.register(RDEntityTypes.ONI, wideNpcLikeRendererProvider);
    }
}
