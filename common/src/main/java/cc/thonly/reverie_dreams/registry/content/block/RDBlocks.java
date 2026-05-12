package cc.thonly.reverie_dreams.registry.content.block;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.*;
import cc.thonly.reverie_dreams.block.base.ModelBlock;
import cc.thonly.reverie_dreams.block.bundle.ChestBlockBundle;
import cc.thonly.reverie_dreams.block.bundle.DecorativeBlockBundle;
import cc.thonly.reverie_dreams.block.props.*;
import cc.thonly.reverie_dreams.item.ItemTypeGroup;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.util.PlatformContext;
import lombok.Getter;
import net.blay09.mods.balm.world.level.block.BalmBlockRegistrar;
import net.blay09.mods.balm.world.level.block.BalmBlockRegistration;
import net.blay09.mods.balm.world.level.block.DeferredBlock;
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

    public static DeferredBlock DANMAKU_CRAFTING_TABLE;
    public static DeferredBlock STRENGTH_TABLE;
    public static DeferredBlock GENSOKYO_ALTAR;
    public static DeferredBlock MUSIC_BLOCK;
    public static DeferredBlock MAGIC_ICE_BLOCK;
    public static DeferredBlock POINT_BLOCK;
    public static DeferredBlock POWER_BLOCK;

    public static DecorativeBlockBundle ICE_SCALES;
    public static DecorativeBlockBundle DREAM_STONE;
    public static DecorativeBlockBundle DREAM_STONE_BRICK;
    public static DecorativeBlockBundle MOON_STONE;
    public static DecorativeBlockBundle MOON_STONE_BRICK;

    public static DeferredBlock SILVER_ORE;
    public static DeferredBlock DEEPSLATE_SILVER_ORE;
    public static DeferredBlock SILVER_BLOCK;

    public static ChestBlockBundle SILVER_CHEST_BLOCK;

    public static DeferredBlock ORB_ORE;
    public static DeferredBlock DEEPSLATE_ORB_ORE;

    public static DeferredBlock RED_ORB_BLOCK;
    public static DeferredBlock YELLOW_ORB_BLOCK;
    public static DeferredBlock BLUE_ORB_BLOCK;
    public static DeferredBlock GREEN_ORB_BLOCK;
    public static DeferredBlock PURPLE_ORB_BLOCK;

    public static DeferredBlock DREAM_RED_BLOCK;
    public static DeferredBlock DREAM_BLUE_BLOCK;
    public static DeferredBlock DREAM_CRYSTAL_ORE;

    public static DeferredBlock MARISA_HAT_BLOCK;
    public static DeferredBlock CASH_BOX_BLOCK;
    public static DeferredBlock ANTI_COLLISION_BARREL;
    public static DeferredBlock WHEEL_CHAIR;

    public static ChestBlockBundle WOODEN_BOX;

    public static DeferredBlock FOOD_DISPLAY;
    public static DeferredBlock BLACK_SALT_BLOCK;

    public static DeferredBlock RAIL_CONTROLLER_BLOCK;
    public static DeferredBlock SIGNAL_RAIL_BLOCK;
    public static DeferredBlock SIGNAL_DELAYER_BLOCK;
    public static DeferredBlock REMOTE_CLIENT;
    public static DeferredBlock REMOTE_SERVER;
    public static DeferredBlock SPEAKER;


    public static void initialize(BalmBlockRegistrar registrar) {
        DANMAKU_CRAFTING_TABLE = registerBlock(registrar, "danmaku_crafting_table",
                DanmakuCraftingTableBlock::new,
                BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE));

        STRENGTH_TABLE = registerBlock(registrar, "strength_table",
                StrengthenTableBlock::new,
                BlockBehaviour.Properties.ofFullCopy(Blocks.SMITHING_TABLE));

        GENSOKYO_ALTAR = registerBlock(registrar, "gensokyo_altar",
                GensokyoAltarBlock::new,
                BlockBehaviour.Properties.ofFullCopy(Blocks.ENCHANTING_TABLE).lightLevel(s -> 7));

        MUSIC_BLOCK = registerBlock(registrar, "music_block",
                MusicBlock::new,
                BlockBehaviour.Properties.ofFullCopy(Blocks.NOTE_BLOCK));

        MAGIC_ICE_BLOCK = registerBlock(registrar, "magic_ice",
                Block::new,
                BlockBehaviour.Properties.ofFullCopy(Blocks.BLUE_ICE));

        POINT_BLOCK = registerBlock(registrar, "point_block",
                Block::new,
                BlockBehaviour.Properties.ofFullCopy(Blocks.STONE));

        POWER_BLOCK = registerBlock(registrar, "power_block",
                Block::new,
                BlockBehaviour.Properties.ofFullCopy(Blocks.STONE));

        // ===== Bundle（关键：这里才 build）=====
        ICE_SCALES = DecorativeBlockBundle.create("ice_scales_block").build(registrar);
        DREAM_STONE = DecorativeBlockBundle.create("dream_stone").build(registrar);
        DREAM_STONE_BRICK = DecorativeBlockBundle.create("dream_stone_brick").build(registrar);
        MOON_STONE = DecorativeBlockBundle.create("moon_stone").build(registrar);
        MOON_STONE_BRICK = DecorativeBlockBundle.create("moon_stone_brick").build(registrar);

        SILVER_ORE = registerBlock(registrar, "silver_ore", Block::new,
                BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE));

        DEEPSLATE_SILVER_ORE = registerBlock(registrar, "deepslate_silver_ore", Block::new,
                BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE));

        SILVER_BLOCK = registerBlock(registrar, "silver_block", Block::new,
                BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK));

        SILVER_CHEST_BLOCK = ChestBlockBundle.create(
                "silver_chest",
                BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
        ).build(registrar);

        ORB_ORE = registerBlock(registrar, "orb_ore", Block::new,
                BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE));

        DEEPSLATE_ORB_ORE = registerBlock(registrar, "deepslate_orb_ore", Block::new,
                BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE));

        RED_ORB_BLOCK = registerBlock(registrar, "red_orb_block", Block::new,
                BlockBehaviour.Properties.ofFullCopy(Blocks.EMERALD_BLOCK));

        YELLOW_ORB_BLOCK = registerBlock(registrar, "yellow_orb_block", Block::new,
                BlockBehaviour.Properties.ofFullCopy(Blocks.EMERALD_BLOCK));

        BLUE_ORB_BLOCK = registerBlock(registrar, "blue_orb_block", Block::new,
                BlockBehaviour.Properties.ofFullCopy(Blocks.EMERALD_BLOCK));

        GREEN_ORB_BLOCK = registerBlock(registrar, "green_orb_block", Block::new,
                BlockBehaviour.Properties.ofFullCopy(Blocks.EMERALD_BLOCK));

        PURPLE_ORB_BLOCK = registerBlock(registrar, "purple_orb_block", Block::new,
                BlockBehaviour.Properties.ofFullCopy(Blocks.EMERALD_BLOCK));

        DREAM_RED_BLOCK = registerBlock(registrar, "dream_world_red_line_block", Block::new,
                BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK));

        DREAM_BLUE_BLOCK = registerBlock(registrar, "dream_world_blue_line_block", Block::new,
                BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK));

        DREAM_CRYSTAL_ORE = registerBlock(registrar, "dream_crystal_ore", Block::new,
                BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE));

        MARISA_HAT_BLOCK = registerBlock(registrar, "marisa_hat",
                MarisaHatBlock::new,
                BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_WOOL),
                new Item.Properties().stacksTo(1).component(DataComponents.EQUIPPABLE,
                        Equippable.builder(EquipmentSlot.HEAD).setSwappable(false).build())
        );

        CASH_BOX_BLOCK = registerBlock(registrar, "cash_box",
                CashBoxBlock::new,
                BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion());

        ANTI_COLLISION_BARREL = registerBlock(registrar, "anti_collision_barrel",
                ModelBlock::new,
                BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).noOcclusion());

        WHEEL_CHAIR = registerBlock(registrar, "wheel_chair",
                WheelChairBlock::new,
                BlockBehaviour.Properties.ofFullCopy(Blocks.BLACK_WOOL).noOcclusion());

        WOODEN_BOX = ChestBlockBundle.create(
                "wooden_box",
                BlockBehaviour.Properties.ofFullCopy(Blocks.CHEST).noOcclusion()
        ).build(registrar);

        FOOD_DISPLAY = registerSimpleBlock(registrar,
                "display",
                FoodDisplayBlock::new,
                BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_WOOL).noOcclusion().noCollision().sound(SoundType.GLASS)
        );

        BLACK_SALT_BLOCK = registerSimpleBlock(registrar,
                "black_salt_block",
                Block::new,
                BlockBehaviour.Properties.ofFullCopy(Blocks.SAND)
        );

        RAIL_CONTROLLER_BLOCK = registerSimpleBlock(
                registrar,
                "rail_controller_block",
                RailControllerBlock::new,
                BlockBehaviour.Properties.ofFullCopy(Blocks.RAIL).noOcclusion()
        );

        SIGNAL_RAIL_BLOCK = registerSimpleBlock(
                registrar,
                "signal_rails",
                SignalRailBlock::new,
                BlockBehaviour.Properties.ofFullCopy(Blocks.DETECTOR_RAIL).noOcclusion()
        );

        SIGNAL_DELAYER_BLOCK = registerSimpleBlock(
                registrar,
                "signal_delayer",
                SignalDelayerBlock::new,
                BlockBehaviour.Properties.ofFullCopy(Blocks.OBSERVER).noOcclusion()
        );

        REMOTE_CLIENT = registerSimpleBlock(
                registrar,
                "remote_client",
                RemoteClientBlock::new,
                BlockBehaviour.Properties.ofFullCopy(Blocks.OBSERVER).noOcclusion()
        );

        REMOTE_SERVER = registerSimpleBlock(
                registrar,
                "remote_server",
                RemoteServerBlock::new,
                BlockBehaviour.Properties.ofFullCopy(Blocks.OBSERVER).noOcclusion()
        );

        SPEAKER = registerSimpleBlock(
                registrar,
                "speaker",
                SpeakerBlock::new,
                BlockBehaviour.Properties.ofFullCopy(Blocks.NOTE_BLOCK).noOcclusion()
        );

        DREAM_STONE_BRICK.base(DREAM_STONE.block());
        MOON_STONE_BRICK.base(MOON_STONE.block());
    }

    public static DeferredBlock registerSimpleBlock(DeferredBlock block) {
        HOLDERS.add(block);
        ReverieDreams.COMMON_LATE_INIT.add(() -> BlockTypeGroup.join(block.asBlock()));
        return block;
    }

    @SuppressWarnings("deprecation")
    public static DeferredBlock registerSimpleBlock(BalmBlockRegistrar registrar, String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties settings, Item.Properties itemSettings) {
        if (PlatformContext.hasPolymer()) {
            settings.noOcclusion();
        }
        if (name.equalsIgnoreCase("marisa_hat")) {
            itemSettings.component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD).build());
        }
        BalmBlockRegistration blockRegistration = registrar.register(name, factory, settings);
        blockRegistration.withItem((block, properties) -> new BlockItem(blockRegistration.asBlockLike().asBlock(), properties.useBlockDescriptionPrefix()), itemSettings);

        DeferredBlock block = blockRegistration.asDeferredBlock();
        if (PlatformContext.isFabric()) {
            ItemTypeGroup.join(block.asItem());
            RDItems.LATE_POLYMERIFY_ITEM_LIST.add(blockRegistration.asDeferredBlock().asItem().builtInRegistryHolder());
        }
        return registerSimpleBlock(block);
    }

    public static DeferredBlock registerSimpleBlock(BalmBlockRegistrar registrar, Identifier id, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties settings, Item.Properties itemSettings) {
        return registerSimpleBlock(registrar, id.getPath(), factory, settings, itemSettings);
    }

    public static DeferredBlock registerSimpleBlock(BalmBlockRegistrar registrar, Identifier id, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties settings) {
        return registerSimpleBlock(registrar, id, factory, settings, new Item.Properties());
    }

    public static DeferredBlock registerSimpleBlock(BalmBlockRegistrar registrar, String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties settings) {
        return registerSimpleBlock(registrar, name, factory, settings, new Item.Properties());
    }

    public static DeferredBlock registerBlock(BalmBlockRegistrar registrar, String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties settings, Item.Properties itemSettings) {
        DeferredBlock block = registerSimpleBlock(registrar, name, factory, settings);
        BLOCKS.add(block);
        return block;
    }

    public static DeferredBlock registerBlock(BalmBlockRegistrar registrar, String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties settings) {
        return registerBlock(registrar, name, factory, settings, new Item.Properties());
    }

    public static ResourceKey<Block> keyOf(String id) {
        return ResourceKey.create(Registries.BLOCK, ReverieDreams.id(id));
    }

    public static ResourceKey<Block> keyOf(Identifier id) {
        return ResourceKey.create(Registries.BLOCK, id);
    }

}
