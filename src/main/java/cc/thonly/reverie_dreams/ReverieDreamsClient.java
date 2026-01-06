package cc.thonly.reverie_dreams;

import cc.thonly.reverie_dreams.block.BlockTypeGroup;
import cc.thonly.reverie_dreams.block.creator.CropBlockCreator;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import com.mojang.serialization.Codec;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class ReverieDreamsClient implements ClientModInitializer {
    public static final Codec<ReverieDreamsClient> CODEC = Codec.unit(ReverieDreamsClient::new);
    public static final String MOD_ID = ReverieDreams.MOD_ID;
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final List<Block> SERVER_SIDE_BLOCKS = List.of(Blocks.NOTE_BLOCK, Blocks.TRIPWIRE);

    @Override
    public void onInitializeClient() {
        BlockTypeGroup.LEAVES.stream().forEach(this::registerBlockCutout);
        BlockTypeGroup.SAPLING.stream().forEach(this::registerBlockCutout);
        BlockTypeGroup.KITCHENWARE.stream().forEach(this::registerBlockCutout);
        BlockTypeGroup.PLANT.stream().forEach(this::registerBlockCutout);
        registerBlockCutout(RDBlocks.MARISA_HAT_BLOCK);
        registerBlockCutout(RDBlocks.CASH_BOX_BLOCK);
        for (Map.Entry<ResourceLocation, CropBlockCreator.Instance> view : CropBlockCreator.getViews()) {
            this.registerBlockCutout(view.getValue().getCropBlock());
        }
        registerBlockCutout(RDBlocks.ITEM_DISPLAY);
        registerBlockCutout(RDBlocks.GENSOKYO_ALTAR);
    }

    public void registerBlockCutout(Block block) {
        BlockRenderLayerMap.INSTANCE.putBlock(block, RenderType.cutout());
    }

    public static ResourceLocation id(String id) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, id);
    }

}