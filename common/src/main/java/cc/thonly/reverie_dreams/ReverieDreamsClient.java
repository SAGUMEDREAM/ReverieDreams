package cc.thonly.reverie_dreams;

import cc.thonly.reverie_dreams.block.BlockTypeGroup;
import cc.thonly.reverie_dreams.block.bundle.CropBlockBundle;
import cc.thonly.reverie_dreams.block.entity.RDBlockEntityTypes;
import cc.thonly.reverie_dreams.client.registry.RDBlockEntityRenderers;
import cc.thonly.reverie_dreams.client.registry.RDBlockRenderTypes;
import cc.thonly.reverie_dreams.client.registry.RDEntityRenderers;
import cc.thonly.reverie_dreams.client.renderer.blockentity.FoodDisplayBlockEntityRenderer;
import cc.thonly.reverie_dreams.client.renderer.entity.*;
import cc.thonly.reverie_dreams.data.FumoType;
import cc.thonly.reverie_dreams.data.npc.NPCRole;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.npc.NPCRoleFastEntity;
import cc.thonly.reverie_dreams.item.weapon.BaguaFurnace;
import cc.thonly.reverie_dreams.registry.RegistryHandlers;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import net.blay09.mods.balm.client.BalmClientRegistrars;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.entity.BeeRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

import java.util.Map;

public class ReverieDreamsClient {
    public static void initialize(BalmClientRegistrars registrars, Runnable lateInit) {
        registrars.blockRenderTypes(RDBlockRenderTypes::initialize);
        registrars.blockEntityRenderers(RDBlockEntityRenderers::initialize);
        registrars.entityRenderers(RDEntityRenderers::initialize);
        ReverieDreams.LATE_INIT_CLIENT.forEach(Runnable::run);
        ReverieDreams.LATE_INIT_CLIENT.clear();
        lateInit.run();
    }
}
