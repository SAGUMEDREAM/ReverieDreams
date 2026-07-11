package cc.thonly.reverie_dreams.registry.content.block.entity;

import cc.thonly.reverie_dreams.block.entity.*;
import cc.thonly.reverie_dreams.mixin.accessor.BlockEntityTypeAccessor;
import cc.thonly.reverie_dreams.registry.ReverieDreamsRegistries;
import cc.thonly.reverie_dreams.registry.content.block.KitchenBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.impl.BlockDelegate;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class RDBlockEntityTypes {
    @SuppressWarnings("rawtypes")
    public static final List<Holder<BlockEntityType>> ENTITIES = new ArrayList<>();
    public static final RegistrySupplier<BlockEntityType<DanmakuCraftingTableBlockEntity>> DANMAKU_CRAFTING_TABLE = registerBlockEntity("danmaku_crafting_table", DanmakuCraftingTableBlockEntity::new, RDBlocks.DANMAKU_CRAFTING_TABLE);
    public static final RegistrySupplier<BlockEntityType<StrengthenTableBlockEntity>> STRENGTH_TABLE = registerBlockEntity("strength_table", StrengthenTableBlockEntity::new, RDBlocks.STRENGTH_TABLE);
    public static final RegistrySupplier<BlockEntityType<GensokyoAltarBlockEntity>> GENSOKYO_ALTAR = registerBlockEntity("gensokyo_altar", GensokyoAltarBlockEntity::new, RDBlocks.GENSOKYO_ALTAR);
    public static final RegistrySupplier<BlockEntityType<MusicBlockEntity>> MUSIC_BLOCK = registerBlockEntity("music_block", MusicBlockEntity::new, RDBlocks.MUSIC_BLOCK);
    public static final RegistrySupplier<BlockEntityType<CustomChestBlockEntity>> CUSTOM_CHEST = registerBlockEntity("custom_chest_block", CustomChestBlockEntity::new, RDBlocks.SILVER_CHEST_BLOCK.chestBlock(), RDBlocks.CASH_BOX_BLOCK, RDBlocks.WOODEN_BOX.chestBlock());
    public static final RegistrySupplier<BlockEntityType<KitchenwareBlockEntity>> KITCHENWARE_BLOCK = registerBlockEntity("kitchen_block",
            KitchenwareBlockEntity::new,
            KitchenBlocks.COOKING_POT, KitchenBlocks.CUTTING_BOARD, KitchenBlocks.FRYING_PAN, KitchenBlocks.GRILL, KitchenBlocks.STEAMER,
            KitchenBlocks.MYSTIA_COOKING_POT, KitchenBlocks.MYSTIA_CUTTING_BOARD, KitchenBlocks.MYSTIA_FRYING_PAN, KitchenBlocks.MYSTIA_GRILL, KitchenBlocks.MYSTIA_STEAMER,
            KitchenBlocks.SUPER_COOKING_POT, KitchenBlocks.SUPER_CUTTING_BOARD, KitchenBlocks.SUPER_FRYING_PAN, KitchenBlocks.SUPER_GRILL, KitchenBlocks.SUPER_STEAMER,
            KitchenBlocks.EXTREME_COOKING_POT, KitchenBlocks.EXTREME_CUTTING_BOARD, KitchenBlocks.EXTREME_FRYING_PAN, KitchenBlocks.EXTREME_GRILL, KitchenBlocks.EXTREME_STEAMER,
            KitchenBlocks.NUKE_COOKING_POT, KitchenBlocks.NUKE_CUTTING_BOARD, KitchenBlocks.NUKE_FRYING_PAN, KitchenBlocks.NUKE_GRILL, KitchenBlocks.NUKE_STEAMER
    );
    public static final RegistrySupplier<BlockEntityType<FoodDisplayBlockEntity>> FOOD_DISPLAY = registerBlockEntity("base_display",
            FoodDisplayBlockEntity::new,
            RDBlocks.FOOD_DISPLAY
    );
    public static final RegistrySupplier<BlockEntityType<SignalRailBlockEntity>> SIGNAL_RAIL_BLOCK_ENTITY = registerBlockEntity(
            "signal_rails",
            SignalRailBlockEntity::new,
            RDBlocks.SIGNAL_RAIL_BLOCK
    );
    public static final RegistrySupplier<BlockEntityType<SignalDelayerBlockEntity>> SIGNAL_DELAYER_BLOCK_ENTITY = registerBlockEntity(
            "signal_delayer",
            SignalDelayerBlockEntity::new,
            RDBlocks.SIGNAL_DELAYER_BLOCK
    );
    public static final RegistrySupplier<BlockEntityType<RemoteBlockEntity>> REMOTE_BLOCK_ENTITY = registerBlockEntity(
            "remote_block",
            RemoteBlockEntity::new,
            RDBlocks.REMOTE_CLIENT,
            RDBlocks.REMOTE_SERVER
    );
    public static final RegistrySupplier<BlockEntityType<SpeakerBlockEntity>> SPEAKER_BLOCK_ENTITY = registerBlockEntity(
            "speaker",
            SpeakerBlockEntity::new,
            RDBlocks.SPEAKER
    );

    public static void initialize() {
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends BlockEntity> RegistrySupplier<BlockEntityType<T>> registerBlockEntity(String name, BlockEntityType.BlockEntitySupplier<T> factory, BlockDelegate... validBlocks) {
        RegistrySupplier<BlockEntityType> blockEntityType = ReverieDreamsRegistries.BLOCK_ENTITY_TYPE.register(name, () -> {
            Set<Block> set = Arrays.stream(validBlocks).map(BlockDelegate::asBlock).collect(Collectors.toSet());
            return BlockEntityTypeAccessor.reverie_dreams$init(factory, set);
        });
        ENTITIES.add(blockEntityType);
        return (RegistrySupplier<BlockEntityType<T>>) (Object) blockEntityType;
    }

}
