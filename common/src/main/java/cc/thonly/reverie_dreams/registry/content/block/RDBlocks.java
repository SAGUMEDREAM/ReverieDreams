package cc.thonly.reverie_dreams.registry.content.block;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.registry.AliasManager;
import cc.thonly.reverie_dreams.block.*;
import cc.thonly.reverie_dreams.block.base.ModelBlock;
import cc.thonly.reverie_dreams.block.bundle.ChestBlockBundle;
import cc.thonly.reverie_dreams.block.bundle.DecorativeBlockBundle;
import cc.thonly.reverie_dreams.block.props.*;
import cc.thonly.reverie_dreams.item.ItemTypeGroup;
import cc.thonly.reverie_dreams.registry.MCBuiltInRegistries;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.registry.delegate.BlockDelegate;
import cc.thonly.reverie_dreams.util.PlatformContext;
import dev.architectury.registry.registries.RegistrySupplier;
import lombok.Getter;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;

@Getter
public class RDBlocks {
    public static final Set<Holder<Block>> BLOCKS = new LinkedHashSet<>();
    public static final Set<Holder<Block>> HOLDERS = new LinkedHashSet<>(128);

    public static final BlockDelegate DANMAKU_CRAFTING_TABLE = registerBlock("danmaku_crafting_table",
            DanmakuCraftingTableBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE));

    public static final BlockDelegate STRENGTH_TABLE = registerBlock("strength_table",
            StrengthenTableBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.SMITHING_TABLE));

    public static final BlockDelegate GENSOKYO_ALTAR = registerBlock("gensokyo_altar",
            GensokyoAltarBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.ENCHANTING_TABLE).lightLevel(s -> 7));

    public static final BlockDelegate MUSIC_BLOCK = registerBlock("music_block",
            MusicBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.NOTE_BLOCK));

    public static final BlockDelegate MAGIC_ICE_BLOCK = registerBlock("magic_ice",
            Block::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.BLUE_ICE).noOcclusion());

    public static final BlockDelegate POINT_BLOCK = registerBlock("point_block",
            Block::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.STONE));

    public static final BlockDelegate POWER_BLOCK = registerBlock("power_block",
            Block::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.STONE));

    // ===== Bundle =====
    public static final DecorativeBlockBundle ICE_SCALES = DecorativeBlockBundle.create("ice_scales_block").map(properties -> properties.sound(SoundType.GLASS)).build();
    public static final DecorativeBlockBundle DREAM_STONE = DecorativeBlockBundle.create("dream_stone").map(properties -> properties.sound(SoundType.STONE)).build();
    public static final DecorativeBlockBundle DREAM_STONE_BRICK = DecorativeBlockBundle.create("dream_stone_brick").map(properties -> properties.sound(SoundType.STONE)).build();
    public static final DecorativeBlockBundle MOON_STONE = DecorativeBlockBundle.create("moon_stone").map(properties -> properties.sound(SoundType.STONE)).build();
    public static final DecorativeBlockBundle MOON_STONE_BRICK = DecorativeBlockBundle.create("moon_stone_brick").map(properties -> properties.sound(SoundType.STONE)).build();

    public static final BlockDelegate MOON_IRON_ORE = registerBlock("moon_iron_ore", Block::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE)
    );

    public static final BlockDelegate MOON_GOLD_ORE = registerBlock("moon_gold_ore", Block::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_ORE)
    );

    public static final BlockDelegate MOON_DIAMOND_ORE = registerBlock("moon_diamond_ore", Block::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_ORE)
    );

    public static final BlockDelegate MOON_QUARTZ_ORE = registerBlock("moon_quartz_ore", Block::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.NETHER_QUARTZ_ORE)
    );

    public static final BlockDelegate SILVER_ORE = registerBlock("silver_ore", Block::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE));

    public static final BlockDelegate DEEPSLATE_SILVER_ORE = registerBlock("deepslate_silver_ore", Block::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE));

    public static final BlockDelegate SILVER_BLOCK = registerBlock("silver_block", Block::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK));

    public static final ChestBlockBundle SILVER_CHEST_BLOCK = ChestBlockBundle.create(
            "silver_chest",
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
    ).build();

    public static final BlockDelegate ORB_ORE = registerBlock("orb_ore", Block::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE));

    public static final BlockDelegate DEEPSLATE_ORB_ORE = registerBlock("deepslate_orb_ore", Block::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE));

    public static final BlockDelegate RED_ORB_BLOCK = registerBlock("red_orb_block", Block::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.EMERALD_BLOCK));

    public static final BlockDelegate YELLOW_ORB_BLOCK = registerBlock("yellow_orb_block", Block::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.EMERALD_BLOCK));

    public static final BlockDelegate BLUE_ORB_BLOCK = registerBlock("blue_orb_block", Block::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.EMERALD_BLOCK));

    public static final BlockDelegate GREEN_ORB_BLOCK = registerBlock("green_orb_block", Block::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.EMERALD_BLOCK));

    public static final BlockDelegate PURPLE_ORB_BLOCK = registerBlock("purple_orb_block", Block::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.EMERALD_BLOCK));

    public static final BlockDelegate DREAM_RED_BLOCK = registerBlock("dream_world_red_line_block", Block::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK));

    public static final BlockDelegate DREAM_BLUE_BLOCK = registerBlock("dream_world_blue_line_block", Block::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK));

    public static final BlockDelegate DREAM_CRYSTAL_ORE = registerBlock("dream_crystal_ore", Block::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE));

    public static final BlockDelegate MARISA_HAT_BLOCK = registerBlock("marisa_hat",
            MarisaHatBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_WOOL),
            new Item.Properties().stacksTo(1).component(DataComponents.EQUIPPABLE,
                    Equippable.builder(EquipmentSlot.HEAD).setSwappable(false).build())
    );

    public static final BlockDelegate CASH_BOX_BLOCK = registerBlock("cash_box",
            CashBoxBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion());

    public static final BlockDelegate ANTI_COLLISION_BARREL = registerBlock("anti_collision_barrel",
            ModelBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).noOcclusion());

    public static final BlockDelegate WHEEL_CHAIR = registerBlock("wheel_chair",
            WheelChairBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.BLACK_WOOL).noOcclusion());

    public static final ChestBlockBundle WOODEN_BOX = ChestBlockBundle.create(
            "wooden_box",
            BlockBehaviour.Properties.ofFullCopy(Blocks.CHEST).noOcclusion()
    ).build();

//        PAPER_WINDOW = registerBlock( "paper_window",
//                PaneBlock::new,
//                BlockBehaviour.Properties.of().instrument(NoteBlockInstrument.HAT).strength(0.8F).sound(SoundType.WOOL).noOcclusion()
//        );

    public static final BlockDelegate PLATE = registerSimpleBlock(
            "plate",
            PlateBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_WOOL).noOcclusion().noCollision().sound(SoundType.GLASS)
    );

    public static final BlockDelegate CHAIR = registerBlock("chair",
            ChairBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WOOD).noOcclusion()
    );

    public static final BlockDelegate TABLE = registerBlock("table",
            TableBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WOOD).noOcclusion()
    );

    public static final BlockDelegate BREWING_BARREL = registerBlock("brewing_barrel",
            BrewingBarrelBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.BARREL).noOcclusion()
    );

    public static final BlockDelegate CUPBOARD = registerBlock("cupboard",
            CupboardBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion()
    );

    public static final BlockDelegate BLACK_SALT_BLOCK = registerSimpleBlock(
            "black_salt_block",
            Block::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.SAND)
    );

    public static final BlockDelegate RAIL_CONTROLLER_BLOCK = registerSimpleBlock(
            "rail_controller_block",
            RailControllerBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.RAIL).noOcclusion()
    );

    public static final BlockDelegate SIGNAL_RAIL_BLOCK = registerSimpleBlock(
            "signal_rails",
            SignalRailBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.DETECTOR_RAIL).noOcclusion()
    );

    public static final BlockDelegate SIGNAL_DELAYER_BLOCK = registerSimpleBlock(
            "signal_delayer",
            SignalDelayerBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.OBSERVER).noOcclusion()
    );

    public static final BlockDelegate REMOTE_CLIENT = registerSimpleBlock(
            "remote_client",
            RemoteClientBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.OBSERVER).noOcclusion()
    );

    public static final BlockDelegate REMOTE_SERVER = registerSimpleBlock(
            "remote_server",
            RemoteServerBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.OBSERVER).noOcclusion()
    );

    public static final BlockDelegate SPEAKER = registerSimpleBlock(
            "speaker",
            SpeakerBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.NOTE_BLOCK).noOcclusion()
    );

    static {
        DREAM_STONE_BRICK.base(DREAM_STONE.block());
        MOON_STONE_BRICK.base(MOON_STONE.block());
    }

    public static void initialize() {
        AliasManager.get(Registries.BLOCK).addAlias(ReverieDreams.id("display"), ReverieDreams.id("plate"));
    }

    public static BlockDelegate registerSimpleBlock(BlockDelegate block) {
        HOLDERS.add(block);
        ReverieDreams.COMMON_LATE_INIT.add(() -> BlockTypeGroup.join(block.asBlock()));
        return block;
    }

    @SuppressWarnings("deprecation")
    public static BlockDelegate registerSimpleBlock(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties blockSettings, Item.Properties itemSettings) {
        if (PlatformContext.hasPolymer()) {
            blockSettings.noOcclusion();
        }
        if (name.equalsIgnoreCase("marisa_hat")) {
            itemSettings.component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD).build());
        }
        RegistrySupplier<Block> block = MCBuiltInRegistries.BLOCK.register(name, () -> factory.apply(blockSettings.setId(RDBlocks.keyOf(name))));
        RegistrySupplier<BlockItem> blockItem = MCBuiltInRegistries.ITEM.register(name, () -> new BlockItem(block.get(), itemSettings.setId(RDItems.keyOf(name)).useBlockDescriptionPrefix()));
        BlockDelegate blockDelegate = BlockDelegate.of(block);
        ReverieDreams.COMMON_LATE_INIT.add(() -> {
            ItemTypeGroup.join(blockItem.get());
            RDItems.LATE_POLYMERIFY_ITEM_LIST.add(blockDelegate.asItem().builtInRegistryHolder());
        });
        return registerSimpleBlock(blockDelegate);
    }

    public static BlockDelegate registerSimpleBlock(Identifier id, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties settings, Item.Properties itemSettings) {
        return registerSimpleBlock(id.getPath(), factory, settings, itemSettings);
    }

    public static BlockDelegate registerSimpleBlock(Identifier id, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties settings) {
        return registerSimpleBlock(id, factory, settings, new Item.Properties());
    }

    public static BlockDelegate registerSimpleBlock(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties settings) {
        return registerSimpleBlock(name, factory, settings, new Item.Properties());
    }

    public static BlockDelegate registerBlock(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties settings, Item.Properties itemSettings) {
        BlockDelegate block = registerSimpleBlock(name, factory, settings);
        BLOCKS.add(block);
        return block;
    }

    public static BlockDelegate registerBlock(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties settings) {
        return registerBlock(name, factory, settings, new Item.Properties());
    }

    public static ResourceKey<Block> keyOf(String id) {
        return ResourceKey.create(Registries.BLOCK, ReverieDreams.id(id));
    }

    public static ResourceKey<Block> keyOf(Identifier id) {
        return ResourceKey.create(Registries.BLOCK, id);
    }

}
