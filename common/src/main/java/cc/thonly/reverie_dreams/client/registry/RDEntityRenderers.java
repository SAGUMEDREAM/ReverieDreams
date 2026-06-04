package cc.thonly.reverie_dreams.client.registry;

import cc.thonly.reverie_dreams.client.renderer.entity.*;
import cc.thonly.reverie_dreams.data.npc.NPCRole;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.npc.NPCRoleFastEntity;
import cc.thonly.reverie_dreams.registry.RegistryImpls;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import net.blay09.mods.balm.client.renderer.entity.BalmEntityRendererRegistrar;
import net.minecraft.client.renderer.entity.BeeRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RabbitRenderer;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EntityType;

public class RDEntityRenderers {
    public static void initialize(BalmEntityRendererRegistrar registrar) {
        // EmptyRenderer
        for (NPCRole npcRole : RegistryImpls.NPC_ROLE) {
            Holder<EntityType<NPCRoleFastEntity>> entityType = npcRole.getEntityType();
            registrar.register(entityType, EmptyRenderer::new);
        }
        // Projectile
        registrar.register(RDEntityTypes.DANMAKU.asHolder(), DanmakuLikeRenderer::new);
        registrar.register(RDEntityTypes.KNIFE.asHolder(), DanmakuLikeRenderer::new);

        // LivingEntity
        registrar.register(RDEntityTypes.WILD_PIG.asHolder(), WildPigRenderer::new);
        registrar.register(RDEntityTypes.KILLER_BEE.asHolder(), BeeRenderer::new);
        registrar.register(RDEntityTypes.BAGUA_FURNACE.asHolder(), BaguaFurnaceRenderer::new);
        registrar.register(RDEntityTypes.HAIRBALL.asHolder(), HairballRenderer::new);
        registrar.register(RDEntityTypes.UFO.asHolder(), UfoRenderer::new);
        registrar.register(RDEntityTypes.SCARECROW.asHolder(), ScarecrowRenderer::new);
        registrar.register(RDEntityTypes.MUSHROOM_MONSTER.asHolder(), MushroomMonsterRenderer::new);
        registrar.register(RDEntityTypes.FUMO_SELLER_VILLAGER.asHolder(), VillagerSellerRenderer::new);
        registrar.register(RDEntityTypes.TAVERN_VILLAGER.asHolder(), VillagerSellerRenderer::new);

        // Misc
        registrar.register(RDEntityTypes.ORE_ESP.asHolder(), OreEspRenderer::new);
        registrar.register(RDEntityTypes.MAGIC_BROOM.asHolder(), MagicBroomRenderer::new);
        registrar.register(RDEntityTypes.WHEEL_CHAIR.asHolder(), WheelchairRenderer::new);

        // NPCLike
        EntityRendererProvider<BaseNPCLikeEntity> slimNpcLikeRendererProvider = context -> new BaseNPCLikeEntityRenderer<>(context, true);
        EntityRendererProvider<BaseNPCLikeEntity> wideNpcLikeRendererProvider = context -> new BaseNPCLikeEntityRenderer<>(context, false);
        EntityRendererProvider<BaseNPCLikeEntity> slimYouseiWingLikeRendererProvider = context -> new YouseiWingLikeEntityRenderer<>(context, true);
        EntityRendererProvider<BaseNPCLikeEntity> wideYouseiWingLikeRendererProvider = context -> new YouseiWingLikeEntityRenderer<>(context, false);
        registrar.register(RDEntityTypes.NPC_ROLE.asHolder(), slimNpcLikeRendererProvider);
        registrar.register(RDEntityTypes.GHOST.asHolder(), slimNpcLikeRendererProvider);
        registrar.register(RDEntityTypes.YOUSEI.asHolder(), slimYouseiWingLikeRendererProvider);
        registrar.register(RDEntityTypes.MAID_YOUSEI.asHolder(), slimYouseiWingLikeRendererProvider);
        registrar.register(RDEntityTypes.SUNFLOWER_YOUSEI.asHolder(), slimYouseiWingLikeRendererProvider);
        registrar.register(RDEntityTypes.GOBLIN.asHolder(), wideNpcLikeRendererProvider);
        registrar.register(RDEntityTypes.RABBIT_UNIT.asHolder(), slimNpcLikeRendererProvider);
        registrar.register(RDEntityTypes.MOON_RABBIT.asHolder(), RabbitRenderer::new);
        registrar.register(RDEntityTypes.WATER_ELEMENTAL.asHolder(), wideNpcLikeRendererProvider);
        registrar.register(RDEntityTypes.FIRE_ELEMENTAL.asHolder(), wideNpcLikeRendererProvider);
        registrar.register(RDEntityTypes.ICE_ELEMENTAL.asHolder(), wideNpcLikeRendererProvider);
    }
}
