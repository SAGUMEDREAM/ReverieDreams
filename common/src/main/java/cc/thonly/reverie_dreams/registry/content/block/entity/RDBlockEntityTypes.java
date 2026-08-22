package cc.thonly.reverie_dreams.registry.content.block.entity;

import cc.thonly.reverie_dreams.block.entity.*;
import cc.thonly.reverie_dreams.mixin.accessor.BlockEntityTypeAccessor;
import cc.thonly.reverie_dreams.registry.MCBuiltInRegistries;
import cc.thonly.reverie_dreams.registry.content.block.RDKitchenBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.delegate.BlockDelegate;
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
            RDKitchenBlocks.COOKING_POT, RDKitchenBlocks.CUTTING_BOARD, RDKitchenBlocks.FRYING_PAN, RDKitchenBlocks.GRILL, RDKitchenBlocks.STEAMER,
            RDKitchenBlocks.MYSTIA_COOKING_POT, RDKitchenBlocks.MYSTIA_CUTTING_BOARD, RDKitchenBlocks.MYSTIA_FRYING_PAN, RDKitchenBlocks.MYSTIA_GRILL, RDKitchenBlocks.MYSTIA_STEAMER,
            RDKitchenBlocks.SUPER_COOKING_POT, RDKitchenBlocks.SUPER_CUTTING_BOARD, RDKitchenBlocks.SUPER_FRYING_PAN, RDKitchenBlocks.SUPER_GRILL, RDKitchenBlocks.SUPER_STEAMER,
            RDKitchenBlocks.EXTREME_COOKING_POT, RDKitchenBlocks.EXTREME_CUTTING_BOARD, RDKitchenBlocks.EXTREME_FRYING_PAN, RDKitchenBlocks.EXTREME_GRILL, RDKitchenBlocks.EXTREME_STEAMER,
            RDKitchenBlocks.NUKE_COOKING_POT, RDKitchenBlocks.NUKE_CUTTING_BOARD, RDKitchenBlocks.NUKE_FRYING_PAN, RDKitchenBlocks.NUKE_GRILL, RDKitchenBlocks.NUKE_STEAMER
    );
    public static final RegistrySupplier<BlockEntityType<PlateBlockEntity>> PLATE = registerBlockEntity("base_display",
            PlateBlockEntity::new,
            RDBlocks.PLATE
    );
    public static final RegistrySupplier<BlockEntityType<BrewingBarrelBlockEntity>> BREWING_BARREL = registerBlockEntity("brewing_barrel",
            BrewingBarrelBlockEntity::new,
            RDBlocks.BREWING_BARREL
    );
    public static final RegistrySupplier<BlockEntityType<CupboardBlockEntity>> CUPBOARD = registerBlockEntity("cupboard",
            CupboardBlockEntity::new,
            RDBlocks.CUPBOARD
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
        RegistrySupplier<BlockEntityType> blockEntityType = MCBuiltInRegistries.BLOCK_ENTITY_TYPE.register(name, () -> {
            Set<Block> set = Arrays.stream(validBlocks).map(BlockDelegate::asBlock).collect(Collectors.toSet());
            return BlockEntityTypeAccessor.reverie_dreams$init(factory, set);
        });
        ENTITIES.add(blockEntityType);
        return (RegistrySupplier<BlockEntityType<T>>) (Object) blockEntityType;
    }

}
