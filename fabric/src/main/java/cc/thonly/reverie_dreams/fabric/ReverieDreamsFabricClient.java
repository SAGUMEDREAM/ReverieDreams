package cc.thonly.reverie_dreams.fabric;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.ReverieDreamsClient;
import net.blay09.mods.balm.client.BalmClient;
import net.blay09.mods.balm.fabric.platform.runtime.FabricLoadContext;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ReverieDreamsFabricClient implements ClientModInitializer {
    public static final String MOD_ID = ReverieDreams.MOD_ID;
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final List<Block> SERVER_SIDE_BLOCKS = List.of(Blocks.NOTE_BLOCK, Blocks.TRIPWIRE);

    @Override
    public void onInitializeClient() {
        BalmClient.initializeMod(MOD_ID, FabricLoadContext.INSTANCE, registrars -> {
            ReverieDreamsClient.initialize(registrars, () -> {

            });
        });
    }

    public static Identifier id(String id) {
        return Identifier.fromNamespaceAndPath(MOD_ID, id);
    }

}