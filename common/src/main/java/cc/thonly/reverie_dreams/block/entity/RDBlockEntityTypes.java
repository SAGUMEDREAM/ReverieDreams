package cc.thonly.reverie_dreams.block.entity;

import cc.thonly.keine.api.KeineRegistries;
import cc.thonly.keine.api.registry.BlockEntityTypeAddBlockRegistry;
import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.content.block.KitchenBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import net.blay09.mods.balm.world.level.block.BlockLike;
import net.blay09.mods.balm.world.level.block.DeferredBlock;
import net.blay09.mods.balm.world.level.block.entity.BalmBlockEntityTypeRegistrar;
import net.blay09.mods.balm.world.level.block.entity.BalmBlockEntityTypeRegistration;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("unchecked")
public class RDBlockEntityTypes {
    @SuppressWarnings("rawtypes")
    public static final List<Holder<BlockEntityType>> ENTITIES = new ArrayList<>();
    public static Holder<BlockEntityType<DanmakuCraftingTableBlockEntity>> DANMAKU_CRAFTING_TABLE;
    public static Holder<BlockEntityType<StrengthenTableBlockEntity>> STRENGTH_TABLE;
    public static Holder<BlockEntityType<GensokyoAltarBlockEntity>> GENSOKYO_ALTAR;
    public static Holder<BlockEntityType<MusicBlockEntity>> MUSIC_BLOCK;
    public static Holder<BlockEntityType<CustomChestBlockEntity>> CUSTOM_CHEST;
    public static Holder<BlockEntityType<KitchenwareBlockEntity>> KITCHENWARE_BLOCK;
    public static Holder<BlockEntityType<FoodDisplayBlockEntity>> FOOD_DISPLAY;

    public static void initialize(BalmBlockEntityTypeRegistrar registrar) {
        DANMAKU_CRAFTING_TABLE = registerBlockEntity(registrar, "danmaku_crafting_table", DanmakuCraftingTableBlockEntity::new, RDBlocks.DANMAKU_CRAFTING_TABLE);
        STRENGTH_TABLE =
                registerBlockEntity(registrar, "strength_table", StrengthenTableBlockEntity::new, RDBlocks.STRENGTH_TABLE);
        GENSOKYO_ALTAR =
                registerBlockEntity(registrar, "gensokyo_altar", GensokyoAltarBlockEntity::new, RDBlocks.GENSOKYO_ALTAR);
        MUSIC_BLOCK =
                registerBlockEntity(registrar, "music_block", MusicBlockEntity::new, RDBlocks.MUSIC_BLOCK);
        CUSTOM_CHEST =
                registerBlockEntity(registrar, "custom_chest_block", CustomChestBlockEntity::new, RDBlocks.SILVER_CHEST_BLOCK.chestBlock(), RDBlocks.CASH_BOX_BLOCK, RDBlocks.WOODEN_BOX.chestBlock());
        KITCHENWARE_BLOCK =
                registerBlockEntity(registrar, "kitchen_block",
                        KitchenwareBlockEntity::new,
                        KitchenBlocks.COOKING_POT, KitchenBlocks.CUTTING_BOARD, KitchenBlocks.FRYING_PAN, KitchenBlocks.GRILL, KitchenBlocks.STEAMER,
                        KitchenBlocks.MYSTIA_COOKING_POT, KitchenBlocks.MYSTIA_CUTTING_BOARD, KitchenBlocks.MYSTIA_FRYING_PAN, KitchenBlocks.MYSTIA_GRILL, KitchenBlocks.MYSTIA_STEAMER,
                        KitchenBlocks.SUPER_COOKING_POT, KitchenBlocks.SUPER_CUTTING_BOARD, KitchenBlocks.SUPER_FRYING_PAN, KitchenBlocks.SUPER_GRILL, KitchenBlocks.SUPER_STEAMER,
                        KitchenBlocks.EXTREME_COOKING_POT, KitchenBlocks.EXTREME_CUTTING_BOARD, KitchenBlocks.EXTREME_FRYING_PAN, KitchenBlocks.EXTREME_GRILL, KitchenBlocks.EXTREME_STEAMER,
                        KitchenBlocks.NUKE_COOKING_POT, KitchenBlocks.NUKE_CUTTING_BOARD, KitchenBlocks.NUKE_FRYING_PAN, KitchenBlocks.NUKE_GRILL, KitchenBlocks.NUKE_STEAMER
                );
        FOOD_DISPLAY = registerBlockEntity(registrar, "base_display",
                FoodDisplayBlockEntity::new,
                RDBlocks.FOOD_DISPLAY
        );
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends BlockEntity> Holder<BlockEntityType<T>> registerBlockEntity(BalmBlockEntityTypeRegistrar registrar, String name, BalmBlockEntityTypeRegistrar.BlockEntitySupplier<T> factory, BlockLike... validBlocks) {
        BalmBlockEntityTypeRegistration<T> register = registrar.register(name, factory, validBlocks);
        Holder<BlockEntityType<T>> holder = register.asHolder();
        ENTITIES.add((Holder<BlockEntityType>) (Object) holder);
        return holder;
    }

}
