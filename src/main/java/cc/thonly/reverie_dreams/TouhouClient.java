package cc.thonly.reverie_dreams;

import cc.thonly.reverie_dreams.block.BlockTypeGroup;
import cc.thonly.reverie_dreams.block.CropBlockCreator;
import cc.thonly.reverie_dreams.networking.CustomBytePayload;
import cc.thonly.reverie_dreams.networking.CustomBytePayloadClient;
import com.mojang.serialization.Codec;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class TouhouClient implements ClientModInitializer {
    public static final Codec<TouhouClient> CODEC = Codec.unit(TouhouClient::new);
    public static final String MOD_ID = Touhou.MOD_ID;
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final List<Block> SERVER_SIDE_BLOCKS = List.of(Blocks.NOTE_BLOCK, Blocks.TRIPWIRE);

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(CustomBytePayload.PACKET_ID, CustomBytePayloadClient.Receiver::receiveClient);

        BlockTypeGroup.LEAVES.stream().forEach(this::registerBlockCutout);
        BlockTypeGroup.SAPLING.stream().forEach(this::registerBlockCutout);
        BlockTypeGroup.KITCHENWARE.stream().forEach(this::registerBlockCutout);
        BlockTypeGroup.PLANT.stream().forEach(this::registerBlockCutout);
        for (Map.Entry<Identifier, CropBlockCreator.Instance> view : CropBlockCreator.getViews()) {
            this.registerBlockCutout(view.getValue().getCropBlock());
        }
    }

    public void registerBlockCutout(Block block) {
        BlockRenderLayerMap.putBlock(block, BlockRenderLayer.CUTOUT);
    }

    public static Identifier id(String id) {
        return Identifier.of(MOD_ID, id);
    }

}