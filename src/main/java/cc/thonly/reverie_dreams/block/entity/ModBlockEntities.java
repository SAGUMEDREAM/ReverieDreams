package cc.thonly.reverie_dreams.block.entity;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.block.ModBlocks;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlockEntities {

    public static final BlockEntityType<DanmakuCraftingTableBlockEntity> DANMAKU_CRAFTING_TABLE_BLOCK_ENTITY =
            registerBlockEntity("danmaku_crafting_table", DanmakuCraftingTableBlockEntity::new, ModBlocks.DANMAKU_CRAFTING_TABLE);
    public static final BlockEntityType<StrengthenTableBlockEntity> STRENGTH_TABLE_BLOCK_ENTITY =
            registerBlockEntity("strength_table", StrengthenTableBlockEntity::new, ModBlocks.STRENGTH_TABLE);
    public static final BlockEntityType<GensokyoAltarBlockEntity> GENSOKYO_ALTAR_BLOCK_ENTITY =
            registerBlockEntity("gensokyo_altar", GensokyoAltarBlockEntity::new, ModBlocks.GENSOKYO_ALTAR);
    public static final BlockEntityType<MusicBlockEntity> MUSIC_BLOCK_ENTITY =
            registerBlockEntity("music_block", MusicBlockEntity::new, ModBlocks.MUSIC_BLOCK);
    public static final BlockEntityType<CustomChestBlockEntity> CUSTOM_CHEST_BLOCK_ENTITY =
            registerBlockEntity("custom_chest_block", CustomChestBlockEntity::new, ModBlocks.CASH_BOX_BLOCK);

    public static void registerBlockEntities() {

    }

    private static <T extends BlockEntity> BlockEntityType<T> registerBlockEntity(
            String name,
            FabricBlockEntityTypeBuilder.Factory<? extends T> entityFactory,
            Block... blocks
    ) {
        Identifier id = Touhou.id(name);
        BlockEntityType<T> entityType = FabricBlockEntityTypeBuilder.<T>create(entityFactory, blocks).build();
        Registry.register(Registries.BLOCK_ENTITY_TYPE, id, entityType);
        PolymerBlockUtils.registerBlockEntity(entityType);
        return entityType;
    }

}
