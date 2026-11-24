package cc.thonly.reverie_dreams.registry.content.block;

import cc.thonly.reverie_dreams.block.decor.RecordPlayerBlock;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.registry.AliasManager;
import cc.thonly.reverie_dreams.block.*;
import cc.thonly.reverie_dreams.block.base.ModelBlock;
import cc.thonly.reverie_dreams.block.base.PaneBlock;
import cc.thonly.reverie_dreams.block.bundle.ChestBlockBundle;
import cc.thonly.reverie_dreams.block.decor.ChabudaiBlock;
import cc.thonly.reverie_dreams.block.decor.ChochinBlock;
import cc.thonly.reverie_dreams.block.decor.ShojiPaneBlock;
import cc.thonly.reverie_dreams.block.decor.TatamiBlock;
import cc.thonly.reverie_dreams.block.decor.ThinDecorBlock;
import cc.thonly.reverie_dreams.block.decor.ZabutonBlock;
import cc.thonly.reverie_dreams.block.decor.TeaSetBlock;
import cc.thonly.reverie_dreams.block.decor.RakedGravelBlock;
import cc.thonly.reverie_dreams.block.dream.FootstepMossBlock;
import cc.thonly.reverie_dreams.block.dream.MirrorBridgeBlock;
import cc.thonly.reverie_dreams.block.exterior.EngawaBlock;
import cc.thonly.reverie_dreams.block.exterior.TsukubaiBlock;
import cc.thonly.reverie_dreams.block.bundle.DecorativeBlockBundle;
import cc.thonly.reverie_dreams.block.kitchen.*;
import cc.thonly.reverie_dreams.block.props.*;
import cc.thonly.reverie_dreams.block.redstone.*;
import cc.thonly.reverie_dreams.item.ItemTypeGroup;
import cc.thonly.reverie_dreams.registry.MCBuiltInRegistries;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.registry.delegate.BlockDelegate;
import cc.thonly.reverie_dreams.registry.delegate.ItemDelegate;
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
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;

import java.util.LinkedHashSet;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import cc.thonly.reverie_dreams.block.decor.BoardGameBlock;
import cc.thonly.reverie_dreams.block.restaurant.MenuBookBlock;
import cc.thonly.reverie_dreams.block.restaurant.MystiaBellBlock;
import cc.thonly.reverie_dreams.board.BoardKind;
import cc.thonly.reverie_dreams.world.gensokyo.understory.MagicMushroomBootstrap;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.material.MapColor;

@Getter
public class RDBlocks {
    public static final Set<Holder<Block>> BLOCKS = new LinkedHashSet<>();
    public static final Set<Holder<Block>> HOLDERS = new LinkedHashSet<>(128);
    public static final BlockDelegate BLUE_MUSHROOM = registerBlock("blue_mushroom", p -> new MagicMushroomBlock(
            MagicMushroomBootstrap.HUGE_BLUE, p), BlockBehaviour.Properties.ofFullCopy(Blocks.BROWN_MUSHROOM).mapColor(MapColor.COLOR_BLUE));
    public static final BlockDelegate PURPLE_MUSHROOM = registerBlock("purple_mushroom", p -> new MagicMushroomBlock(
            MagicMushroomBootstrap.HUGE_PURPLE, p), BlockBehaviour.Properties.ofFullCopy(Blocks.RED_MUSHROOM).mapColor(MapColor.COLOR_PURPLE));
    public static final BlockDelegate BLUE_MUSHROOM_BLOCK = registerBlock("blue_mushroom_block", MagicMushroomCapBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.BROWN_MUSHROOM_BLOCK).mapColor(MapColor.COLOR_BLUE));
    public static final BlockDelegate PURPLE_MUSHROOM_BLOCK = registerBlock("purple_mushroom_block", MagicMushroomCapBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.RED_MUSHROOM_BLOCK).mapColor(MapColor.COLOR_PURPLE));
    public static final BlockDelegate MAGIC_MUSHROOM_STEM = registerBlock("magic_mushroom_stem", HugeMushroomBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.MUSHROOM_STEM));

    public static final BlockDelegate DANMAKU_CRAFTING_TABLE = registerBlock("danmaku_crafting_table",
            DanmakuCraftingTableBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE));

    public static final BlockDelegate STRENGTH_TABLE = registerBlock("strength_table",
            StrengthenTableBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.SMITHING_TABLE));

    public static final BlockDelegate GENSOKYO_ALTAR = registerBlock("gensokyo_altar",
            GensokyoAltarBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.ENCHANTING_TABLE).lightLevel(s -> 7));

    public static final BlockDelegate RECORD_PLAYER = registerBlock("record_player", RecordPlayerBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion());

    public static final BlockDelegate MUSIC_BLOCK = registerBlock("music_block",
            MusicBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.NOTE_BLOCK));

    public static final BlockDelegate MAGIC_ICE_BLOCK = registerBlock("magic_ice",
            MagicIceBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.BLUE_ICE).noOcclusion());

    public static final BlockDelegate POINT_BLOCK = registerBlock("point_block",
            Block::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.STONE));

    public static final BlockDelegate POWER_BLOCK = registerBlock("power_block",
            Block::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.STONE));

    // Bundle
    public static final DecorativeBlockBundle ICE_SCALES = DecorativeBlockBundle.create("ice_scales_block").map(properties -> properties.sound(SoundType.GLASS)).build();
    public static final DecorativeBlockBundle DREAM_STONE = DecorativeBlockBundle.create("dream_stone").map(properties -> properties.sound(SoundType.STONE)).build();
    public static final DecorativeBlockBundle DREAM_STONE_BRICK = DecorativeBlockBundle.create("dream_stone_brick").map(properties -> properties.sound(SoundType.STONE)).build();
    public static final BlockDelegate FOOTSTEP_MOSS = registerBlock("footstep_moss", FootstepMossBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.MOSS_CARPET));
    public static final RegistrySupplier<Block> MIRROR_BRIDGE = MCBuiltInRegistries.BLOCK.register("mirror_bridge",
            () -> new MirrorBridgeBlock(BlockBehaviour.Properties.of().setId(keyOf("mirror_bridge"))
                    .strength(-1.0F, 3600000.0F).noOcclusion().noLootTable().pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK)
                    .sound(SoundType.AMETHYST)));
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
            BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F));

    public static final BlockDelegate DREAM_BLUE_BLOCK = registerBlock("dream_world_blue_line_block", Block::new,
            BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F));

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

    // Japanese interior / exterior decorative set, batch 1.
    public static final BlockDelegate TATAMI = registerBlock("tatami",
            settings -> new TatamiBlock(settings, Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0), true, true),
            BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion().sound(SoundType.GRASS)
    );

    public static final BlockDelegate TATAMI_BLOCK = registerBlock("tatami_block",
            settings -> new TatamiBlock(settings, Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0), true, false),
            BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).sound(SoundType.GRASS)
    );

    public static final BlockDelegate TATAMI_CORNER = registerBlock("tatami_corner",
            settings -> new ThinDecorBlock(settings, Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0), true),
            BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion().sound(SoundType.GRASS)
    );

    public static final BlockDelegate SHOJI_PANE = registerBlock("shoji_pane",
            ShojiPaneBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion().sound(SoundType.WOOD)
    );

    public static final BlockDelegate FUSUMA_DOOR = registerBlock("fusuma_door",
            settings -> new DoorBlock(BlockSetType.OAK, settings),
            BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_DOOR)
    );

    public static final BlockDelegate RANMA = registerBlock("ranma",
            PaneBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion().sound(SoundType.WOOD)
    );

    public static final BlockDelegate SUDARE = registerBlock("sudare",
            settings -> new ThinDecorBlock(settings, Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 1.0), false, true),
            BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion().sound(SoundType.WOOD)
    );

    public static final BlockDelegate NOREN_INDIGO = registerNoren("noren_indigo");
    public static final BlockDelegate NOREN_WHITE = registerNoren("noren_white");
    public static final BlockDelegate NOREN_RED = registerNoren("noren_red");
    public static final BlockDelegate NOREN_BLACK = registerNoren("noren_black");
    public static final BlockDelegate NOREN_BROWN = registerNoren("noren_brown");
    public static final BlockDelegate NOREN_GREEN = registerNoren("noren_green");

    public static final BlockDelegate ANDON = registerBlock("andon",
            settings -> new ThinDecorBlock(settings, Block.box(2.0, 0.0, 2.0, 14.0, 14.0, 14.0), true),
            BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion()
                    .lightLevel(state -> 14).sound(SoundType.WOOD)
    );

    public static final BlockDelegate CHOCHIN = registerBlock("chochin",
            ChochinBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.LANTERN).lightLevel(state -> 15)
    );

    public static final BlockDelegate ZABUTON = registerBlock("zabuton",
            ZabutonBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_WOOL).noOcclusion().sound(SoundType.WOOL)
    );

    public static final Map<DyeColor, BlockDelegate> ZABUTONS = registerZabutons();

    public static final BlockDelegate CHABUDAI = registerBlock("chabudai",
            ChabudaiBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion().sound(SoundType.WOOD)
    );

    public static final BlockDelegate TEA_SET = registerBlock("tea_set", TeaSetBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion().noCollision().sound(SoundType.WOOD));

    public static final BlockDelegate XIANGQI_BOARD = registerBoard(BoardKind.XIANGQI);
    public static final BlockDelegate CHESS_BOARD = registerBoard(BoardKind.CHESS);
    public static final BlockDelegate GO_BOARD = registerBoard(BoardKind.GO);

    private static BlockDelegate registerBoard(BoardKind kind) {
        return registerBlock(kind.id, p -> new BoardGameBlock(kind, p),
                BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(1).noOcclusion().noCollision().sound(SoundType.WOOD));
    }

    public static final BlockDelegate BYOBU = registerBlock("byobu",
            settings -> new ThinDecorBlock(settings, Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 1.0), true),
            BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion().sound(SoundType.WOOD)
    );

    public static final BlockDelegate KAKEJIKU = registerBlock("kakejiku",
            settings -> new ThinDecorBlock(settings, Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 1.0), false, true),
            BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion().sound(SoundType.WOOD)
    );

    // Japanese exterior decorative set, batch 1.
    public static final BlockDelegate ENGAWA = registerBlock("engawa",
            EngawaBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion().sound(SoundType.WOOD)
    );

    public static final BlockDelegate YOTSUME_GAKI = registerBlock("yotsume_gaki",
            FenceBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE).sound(SoundType.WOOD)
    );

    public static final BlockDelegate BAMBOO_FENCE = registerBlock("bamboo_fence",
            FenceBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE).sound(SoundType.BAMBOO_WOOD)
    );

    public static final BlockDelegate MUSHIKO_MADO = registerBlock("mushiko_mado",
            PaneBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion().sound(SoundType.WOOD)
    );

    public static final BlockDelegate SHIKKUI_BLOCK = registerBlock("shikkui_block",
            Block::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.SMOOTH_STONE).sound(SoundType.STONE)
    );

    public static final BlockDelegate SHIKKUI_SLAB = registerBlock("shikkui_slab",
            SlabBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.SMOOTH_STONE_SLAB).sound(SoundType.STONE)
    );

    public static final BlockDelegate SHIKKUI_STAIRS = registerBlock("shikkui_stairs",
            settings -> new StairBlock(SHIKKUI_BLOCK.asBlock().defaultBlockState(), settings),
            BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_STAIRS).sound(SoundType.STONE)
    );

    public static final BlockDelegate SHIKKUI_WALL = registerBlock("shikkui_wall",
            WallBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.COBBLESTONE_WALL).sound(SoundType.STONE)
    );

    public static final BlockDelegate KAWARA_TILES = registerBlock("kawara_tiles",
            Block::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS).sound(SoundType.STONE)
    );

    public static final BlockDelegate KAWARA_TILES_SLAB = registerBlock("kawara_tiles_slab",
            SlabBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICK_SLAB).sound(SoundType.STONE)
    );

    public static final BlockDelegate KAWARA_TILES_STAIRS = registerBlock("kawara_tiles_stairs",
            settings -> new StairBlock(KAWARA_TILES.asBlock().defaultBlockState(), settings),
            BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICK_STAIRS).sound(SoundType.STONE)
    );

    public static final BlockDelegate STONE_LANTERN = registerBlock("stone_lantern",
            settings -> new ThinDecorBlock(settings, Block.box(3.0, 0.0, 3.0, 13.0, 14.0, 13.0), true),
            BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS).noOcclusion()
                    .lightLevel(state -> 15).sound(SoundType.STONE)
    );

    public static final BlockDelegate TSUKUBAI = registerBlock("tsukubai",
            TsukubaiBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS).noOcclusion().sound(SoundType.STONE)
    );

    public static final BlockDelegate STEPPING_STONE = registerBlock("stepping_stone",
            settings -> new ThinDecorBlock(settings, Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0), true),
            BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS).noOcclusion().sound(SoundType.STONE)
    );

    public static final BlockDelegate RAKED_GRAVEL = registerBlock("raked_gravel",
            RakedGravelBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.GRAVEL).sound(SoundType.GRAVEL)
    );

    private static BlockDelegate registerNoren(String id) {
        return registerBlock(id,
                settings -> new ThinDecorBlock(settings, Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 1.0), false, true),
                BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_WOOL).noOcclusion().sound(SoundType.WOOL)
        );
    }

    public static final BlockDelegate BREWING_BARREL = registerBlock("brewing_barrel",
            BrewingBarrelBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.BARREL).noOcclusion()
    );

    public static final BlockDelegate CUPBOARD = registerBlock("cupboard",
            CupboardBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion()
    );

    public static final BlockDelegate ICE_MAKING_MACHINE = registerBlock("ice_making_machine",
            IceMakingMachine::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).noOcclusion()
    );

    public static final BlockDelegate BLACK_SALT_BLOCK = registerSimpleBlock(
            "black_salt_block",
            Block::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.SAND)
    );

    public static final BlockDelegate PADDY_FIELD = registerSimpleBlock(
            "paddy_field",
            PaddyFieldBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.FARMLAND)
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

    public static final BlockDelegate MYSTIA_BELL = registerSimpleBlock("mystia_bell", MystiaBellBlock::new,
            BlockBehaviour.Properties.of().strength(1.5f).noOcclusion().sound(SoundType.WOOD));
    public static final BlockDelegate MENU_BOOK = registerSimpleBlock("menu_book", MenuBookBlock::new,
            BlockBehaviour.Properties.of().strength(1.0f).noOcclusion().sound(SoundType.WOOD));


    static {
        DREAM_STONE_BRICK.base(DREAM_STONE.block());
        MOON_STONE_BRICK.base(MOON_STONE.block());
    }

    public static void initialize() {
        RDJapaneseLivingBlocks.initialize();
        RDJapaneseSet2Blocks.initialize();
        RDJapaneseShrineBlocks.initialize();
        RDJapaneseHomeBlocks.initialize();
        AliasManager.get(Registries.BLOCK).addAlias(ReverieDreams.id("display"), ReverieDreams.id("plate"));
    }

    private static Map<DyeColor, BlockDelegate> registerZabutons() {
        Map<DyeColor, BlockDelegate> blocks = new EnumMap<>(DyeColor.class);
        for (DyeColor color : DyeColor.values()) {
            blocks.put(color, registerBlock(color.getName() + "_zabuton", settings -> new ZabutonBlock(color, settings),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_WOOL)
                            .mapColor(color.getMapColor()).noOcclusion().sound(SoundType.WOOL)));
        }
        return Collections.unmodifiableMap(blocks);
    }

    public static BlockDelegate registerSimpleBlock(BlockDelegate block) {
        HOLDERS.add(block);
        ReverieDreams.COMMON_LATE_INIT.add(() -> BlockTypeGroup.join(block.asBlock()));
        return block;
    }

    public static BlockDelegate registerSimpleBlock(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties blockSettings, Item.Properties itemSettings) {
        if (name.equalsIgnoreCase("marisa_hat")) {
            itemSettings.component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD).build());
        }
        return registerSimpleBlock(name, factory, blockSettings, () -> itemSettings);
    }

    @SuppressWarnings({"deprecation", "unchecked"})
    public static BlockDelegate registerSimpleBlock(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties blockSettings, Supplier<Item.Properties> itemSettings) {
        if (PlatformContext.hasPolymer()) {
            blockSettings.noOcclusion();
        }
        RegistrySupplier<Block> block = MCBuiltInRegistries.BLOCK.register(name, () -> factory.apply(blockSettings.setId(RDBlocks.keyOf(name))));
        RegistrySupplier<BlockItem> blockItem = MCBuiltInRegistries.ITEM.register(name, () -> new BlockItem(block.get(), itemSettings.get().setId(RDItems.keyOf(name)).useBlockDescriptionPrefix()));
        ItemDelegate itemDelegate = ItemDelegate.of((Holder<Item>) (Object) blockItem);
        BlockDelegate blockDelegate = BlockDelegate.of(block);
        blockDelegate.bindBlockItem(itemDelegate);
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
