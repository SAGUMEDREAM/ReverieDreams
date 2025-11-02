package cc.thonly.mystias_izakaya;

import cc.thonly.mystias_izakaya.block.MIBlockEntities;
import cc.thonly.mystias_izakaya.block.MIBlocks;
import cc.thonly.mystias_izakaya.component.MIDataComponentTypes;
import cc.thonly.mystias_izakaya.datafixer.MIDataFixer;
import cc.thonly.mystias_izakaya.entity.MIEntities;
import cc.thonly.mystias_izakaya.item.MIItems;
import cc.thonly.mystias_izakaya.loot.MILootModifies;
import cc.thonly.mystias_izakaya.recipe.MiRecipeManager;
import cc.thonly.mystias_izakaya.registry.MIRegistryManager;
import cc.thonly.mystias_izakaya.villager.MIVillagerTradeModifier;
import cc.thonly.reverie_dreams.ReverieDreams;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Setter
@Getter
public class MystiasIzakaya implements ModInitializer {
    public static final String MOD_NAME = "Mystias Izakaya";
    public static final String MOD_ID = ReverieDreams.MOD_ID;
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Loaded " + MOD_NAME);
        MIDataComponentTypes.init();
        MIBlocks.registerBlocks();
        MIBlockEntities.registerBlockEntities();
        MIItems.registerItems();
        MIEntities.init();
        MIRegistryManager.bootstrap();
        MiRecipeManager.bootstrap();
        MIVillagerTradeModifier.bootstrap();
        MILootModifies.bootstrap();
        MIDataFixer.bootstrap();

        UseBlockCallback.EVENT.register((playerEntity, world, hand, blockHitResult) -> {
            if (!world.isClientSide()) {
                ItemStack stack = playerEntity.getItemInHand(hand);
                BlockPos blockPos = blockHitResult.getBlockPos();
                BlockState blockState = world.getBlockState(blockPos);
                Block block = blockState.getBlock();
                if (block instanceof LeavesBlock && (blockState.getValue(LeavesBlock.WATERLOGGED))) {
                    if (stack.getItem() == Items.LILY_PAD) {
                        stack.consume(1, playerEntity);
                        if (!playerEntity.hasInfiniteMaterials()) {
                            playerEntity.addItem(new ItemStack(MIItems.DEW, 1));
                        }
                        playerEntity.swing(hand);
                        return InteractionResult.SUCCESS_SERVER;
                    }
                }
            }
            return InteractionResult.PASS;
        });
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path.toLowerCase());
    }
}
