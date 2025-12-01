package cc.thonly.reverie_dreams.block.entity;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.content.block.KitchenBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.List;

public class RDBlockEntityTypes {

    public static final BlockEntityType<DanmakuCraftingTableBlockEntity> DANMAKU_CRAFTING_TABLE_BLOCK_ENTITY =
            registerBlockEntity("danmaku_crafting_table", DanmakuCraftingTableBlockEntity::new, RDBlocks.DANMAKU_CRAFTING_TABLE);
    public static final BlockEntityType<StrengthenTableBlockEntity> STRENGTH_TABLE_BLOCK_ENTITY =
            registerBlockEntity("strength_table", StrengthenTableBlockEntity::new, RDBlocks.STRENGTH_TABLE);
    public static final BlockEntityType<GensokyoAltarBlockEntity> GENSOKYO_ALTAR_BLOCK_ENTITY =
            registerBlockEntity("gensokyo_altar", GensokyoAltarBlockEntity::new, RDBlocks.GENSOKYO_ALTAR);
    public static final BlockEntityType<MusicBlockEntity> MUSIC_BLOCK_ENTITY =
            registerBlockEntity("music_block", MusicBlockEntity::new, RDBlocks.MUSIC_BLOCK);
    public static final BlockEntityType<CustomChestBlockEntity> CUSTOM_CHEST_BLOCK_ENTITY =
            registerBlockEntity("custom_chest_block", CustomChestBlockEntity::new, RDBlocks.CASH_BOX_BLOCK);
    public static final BlockEntityType<KitchenwareBlockEntity> KITCHENWARE_BLOCK_ENTITY =
            registerBlockEntity("kitchen_block",
                    KitchenwareBlockEntity::new,
                    List.of(
                            KitchenBlocks.COOKING_POT, KitchenBlocks.CUTTING_BOARD, KitchenBlocks.FRYING_PAN, KitchenBlocks.GRILL, KitchenBlocks.STEAMER,
                            KitchenBlocks.MYSTIA_COOKING_POT, KitchenBlocks.MYSTIA_CUTTING_BOARD, KitchenBlocks.MYSTIA_FRYING_PAN, KitchenBlocks.MYSTIA_GRILL, KitchenBlocks.MYSTIA_STEAMER,
                            KitchenBlocks.SUPER_COOKING_POT, KitchenBlocks.SUPER_CUTTING_BOARD, KitchenBlocks.SUPER_FRYING_PAN, KitchenBlocks.SUPER_GRILL, KitchenBlocks.SUPER_STEAMER,
                            KitchenBlocks.EXTREME_COOKING_POT, KitchenBlocks.EXTREME_CUTTING_BOARD, KitchenBlocks.EXTREME_FRYING_PAN, KitchenBlocks.EXTREME_GRILL, KitchenBlocks.EXTREME_STEAMER,
                            KitchenBlocks.NUKE_COOKING_POT, KitchenBlocks.NUKE_CUTTING_BOARD, KitchenBlocks.NUKE_FRYING_PAN, KitchenBlocks.NUKE_GRILL, KitchenBlocks.NUKE_STEAMER
                    ).toArray(new Block[0])
            );
    public static final BlockEntityType<FoodDisplayBlockEntity> ITEM_DISPLAY_BLOCK_ENTITY =
            registerBlockEntity("base_display",
                    FoodDisplayBlockEntity::new,
                    RDBlocks.ITEM_DISPLAY
            );

    public static void registerBlockEntityTypes() {

    }

    private static <T extends BlockEntity> BlockEntityType<T> registerBlockEntity(
            String name,
            FabricBlockEntityTypeBuilder.Factory<? extends T> entityFactory,
            Block... blocks
    ) {
        ResourceLocation id = ReverieDreams.id(name);
        BlockEntityType<T> entityType = FabricBlockEntityTypeBuilder.<T>create(entityFactory, blocks).build();
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id, entityType);
        PolymerBlockUtils.registerBlockEntity(entityType);
        return entityType;
    }

}
