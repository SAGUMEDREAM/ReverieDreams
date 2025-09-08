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
import cc.thonly.reverie_dreams.Touhou;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Setter
@Getter
public class MystiasIzakaya implements ModInitializer {
    public static final String MOD_NAME = "Mystias Izakaya";
    public static final String MOD_ID = Touhou.MOD_ID;
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
            if (!world.isClient()) {
                ItemStack stack = playerEntity.getStackInHand(hand);
                BlockPos blockPos = blockHitResult.getBlockPos();
                BlockState blockState = world.getBlockState(blockPos);
                Block block = blockState.getBlock();
                if (block instanceof LeavesBlock && (blockState.get(LeavesBlock.WATERLOGGED))) {
                    if (stack.getItem() == Items.LILY_PAD) {
                        stack.decrementUnlessCreative(1, playerEntity);
                        if (!playerEntity.isInCreativeMode()) {
                            playerEntity.giveItemStack(new ItemStack(MIItems.DEW, 1));
                        }
                        playerEntity.swingHand(hand);
                        return ActionResult.SUCCESS_SERVER;
                    }
                }
            }
            return ActionResult.PASS;
        });
    }

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path.toLowerCase());
    }
}
