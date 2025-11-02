package cc.thonly.mystias_izakaya.block;

//import cc.thonly.mystias_izakaya.block.entity.CooktopBlockEntity;
import cc.thonly.mystias_izakaya.block.entity.ItemStackDisplayBlockEntity;
import cc.thonly.mystias_izakaya.block.entity.KitchenwareBlockEntity;
import cc.thonly.reverie_dreams.ReverieDreams;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import java.util.List;

public class MIBlockEntities {
    public static final BlockEntityType<KitchenwareBlockEntity> KITCHENWARE_BLOCK_ENTITY =
            registerBlockEntity("kitchen_block",
                    KitchenwareBlockEntity::new,
                    List.of(
                            MIBlocks.COOKING_POT, MIBlocks.CUTTING_BOARD, MIBlocks.FRYING_PAN, MIBlocks.GRILL, MIBlocks.STEAMER,
                            MIBlocks.MYSTIA_COOKING_POT, MIBlocks.MYSTIA_CUTTING_BOARD, MIBlocks.MYSTIA_FRYING_PAN, MIBlocks.MYSTIA_GRILL, MIBlocks.MYSTIA_STEAMER,
                            MIBlocks.SUPER_COOKING_POT, MIBlocks.SUPER_CUTTING_BOARD, MIBlocks.SUPER_FRYING_PAN, MIBlocks.SUPER_GRILL, MIBlocks.SUPER_STEAMER,
                            MIBlocks.EXTREME_COOKING_POT, MIBlocks.EXTREME_CUTTING_BOARD, MIBlocks.EXTREME_FRYING_PAN, MIBlocks.EXTREME_GRILL, MIBlocks.EXTREME_STEAMER,
                            MIBlocks.NUKE_COOKING_POT, MIBlocks.NUKE_CUTTING_BOARD, MIBlocks.NUKE_FRYING_PAN, MIBlocks.NUKE_GRILL, MIBlocks.NUKE_STEAMER
                    ).toArray(new Block[0])
            );
    public static final BlockEntityType<ItemStackDisplayBlockEntity> ITEM_DISPLAY_BLOCK_ENTITY =
            registerBlockEntity("base_display",
                    ItemStackDisplayBlockEntity::new,
                    MIBlocks.ITEM_DISPLAY
            );

//    public static final BlockEntityType<CooktopBlockEntity> COOKTOP_BLOCK_ENTITY =
//            registerBlockEntity("cooktop",
//                    CooktopBlockEntity::new,
//                    MIBlocks.COOKTOP
//            );

    public static void registerBlockEntities() {

    }

    private static <T extends BlockEntity> BlockEntityType<T> registerBlockEntity(
            String name,
            FabricBlockEntityTypeBuilder.Factory<? extends T> entityFactory,
            Block... blocks
    ) {
        ResourceLocation id = ReverieDreams.id(name);
        BlockEntityType<T> entityType = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id, FabricBlockEntityTypeBuilder.<T>create(entityFactory, blocks).build());
        PolymerBlockUtils.registerBlockEntity(entityType);
        return entityType;
    }
}
