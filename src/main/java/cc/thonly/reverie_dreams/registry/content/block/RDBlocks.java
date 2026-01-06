package cc.thonly.reverie_dreams.registry.content.block;

import cc.thonly.polymer.block.impl.BasicPolymerAxisModelBlock;
import cc.thonly.polymer.block.impl.BasicPolymerFullBlock;
import cc.thonly.polymer.block.impl.BasicPolymerModelBlock;
import cc.thonly.polymer.item.BasicPolymerBlockItem;
import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.*;
import cc.thonly.reverie_dreams.block.creator.ChestBlockCreator;
import cc.thonly.reverie_dreams.block.creator.DecorativeBlockCreator;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import lombok.Getter;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;

@Getter
public class RDBlocks {
    public static final Set<Block> BLOCKS = new LinkedHashSet<>();
    public static final Block DANMAKU_CRAFTING_TABLE = registerBlock("danmaku_crafting_table", DanmakuCraftingTableBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE));
    public static final Block STRENGTH_TABLE = registerBlock("strength_table", StrengthenTableBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.SMITHING_TABLE));
    public static final Block GENSOKYO_ALTAR = registerBlock("gensokyo_altar", GensokyoAltarBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.ENCHANTING_TABLE).lightLevel((state) -> 7));
    public static final Block MUSIC_BLOCK = registerBlock("music_block", MusicBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.NOTE_BLOCK));

    public static final Block MAGIC_ICE_BLOCK = registerBlock("magic_ice", BasicPolymerFullBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.BLUE_ICE));
    public static final Block POINT_BLOCK = registerBlock("point_block", BasicPolymerFullBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE));
    public static final Block POWER_BLOCK = registerBlock("power_block", BasicPolymerFullBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE));
    public static final DecorativeBlockCreator ICE_SCALES = DecorativeBlockCreator.create("ice_scales_block").build();
    public static final DecorativeBlockCreator DREAM_STONE = DecorativeBlockCreator.create("dream_stone").build();
    public static final DecorativeBlockCreator DREAM_STONE_BRICK = DecorativeBlockCreator.create("dream_stone_brick").build();
    public static final DecorativeBlockCreator MOON_STONE = DecorativeBlockCreator.create("moon_stone").build();
    public static final DecorativeBlockCreator MOON_STONE_BRICK = DecorativeBlockCreator.create("moon_stone_brick").build();

    public static final Block SILVER_ORE = registerBlock("silver_ore", BasicPolymerFullBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE));
    public static final Block DEEPSLATE_SILVER_ORE = registerBlock("deepslate_silver_ore", BasicPolymerFullBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE));
    public static final Block SILVER_BLOCK = registerBlock("silver_block", BasicPolymerFullBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK));
    public static final ChestBlockCreator SILVER_CHEST_BLOCK = ChestBlockCreator.create(
            "silver_chest",
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
    ).build();
    public static final Block ORB_ORE = registerBlock("orb_ore", BasicPolymerFullBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE));
    public static final Block DEEPSLATE_ORB_ORE = registerBlock("deepslate_orb_ore", BasicPolymerFullBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE));
    public static final Block RED_ORB_BLOCK = registerBlock("red_orb_block", BasicPolymerFullBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.EMERALD_BLOCK));
    public static final Block YELLOW_ORB_BLOCK = registerBlock("yellow_orb_block", BasicPolymerFullBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.EMERALD_BLOCK));
    public static final Block BLUE_ORB_BLOCK = registerBlock("blue_orb_block", BasicPolymerFullBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.EMERALD_BLOCK));
    public static final Block GREEN_ORB_BLOCK = registerBlock("green_orb_block", BasicPolymerFullBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.EMERALD_BLOCK));
    public static final Block PURPLE_ORB_BLOCK = registerBlock("purple_orb_block", BasicPolymerFullBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.EMERALD_BLOCK));

    public static final Block DREAM_RED_BLOCK = registerBlock("dream_world_red_line_block", BasicPolymerFullBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK));
    public static final Block DREAM_BLUE_BLOCK = registerBlock("dream_world_blue_line_block", BasicPolymerFullBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK));
    public static final Block DREAM_CRYSTAL_ORE = registerBlock("dream_crystal_ore", BasicPolymerFullBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE));

    public static final Block MARISA_HAT_BLOCK = registerBlock("marisa_hat", (settings) -> new MarisaHatBlock(Vec3.ZERO, settings), BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_WOOL), new Item.Properties().stacksTo(1).component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD).setSwappable(false).build()));
    public static final Block CASH_BOX_BLOCK = registerBlock("cash_box", CashBoxBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS));
    public static final Block ANTI_COLLISION_BARREL = registerBlock("anti_collision_barrel", BasicPolymerModelBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK));
    public static final Block WHEEL_CHAIR = registerBlock("wheel_chair", WheelChairBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK));
    public static final ChestBlockCreator WOODEN_BOX = ChestBlockCreator.create(
            "wooden_box",
            BlockBehaviour.Properties.ofFullCopy(Blocks.CHEST)
    ).build();
    public static final Block ITEM_DISPLAY = registerSimpleBlock(
            "display",
            FoodDisplayBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_WOOL).noOcclusion().sound(SoundType.GLASS)
    );
    public static final Block BLACK_SALT_BLOCK = registerSimpleBlock(
            "black_salt_block",
            BasicPolymerFullBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.SAND)
    );


    public static void registerBlocks() {
        DREAM_STONE_BRICK.base(DREAM_STONE.block());
        MOON_STONE_BRICK.base(MOON_STONE.block());
    }

    public static Block registerSimpleBlock(Block block) {
//        PolymerBlockHelper.registerOverlay(block);
        BlockTypeGroup.join(block);
        return block;
    }

    public static Block registerSimpleBlock(ResourceLocation id, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties settings, Item.Properties itemSettings) {
        Block block = factory.apply(settings.setId(keyOf(id)).noOcclusion());
        Registry.register(BuiltInRegistries.BLOCK, id, block);
        if (block instanceof MarisaHatBlock hat) {
            itemSettings.component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD).build());
        }
        Item item = RDItems.registerSimpleItem(id, (itemSetting -> new BasicPolymerBlockItem(block, itemSetting)), itemSettings.useBlockDescriptionPrefix());
        return registerSimpleBlock(block);
    }

    public static Block registerSimpleBlock(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties settings, Item.Properties itemSettings) {
        return registerSimpleBlock(ReverieDreams.id(name), factory, settings, itemSettings);
    }

    public static Block registerSimpleBlock(ResourceLocation id, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties settings) {
        return registerSimpleBlock(id, factory, settings, new Item.Properties());
    }

    public static Block registerSimpleBlock(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties settings) {
        return registerSimpleBlock(name, factory, settings, new Item.Properties());
    }

    public static Block registerBlock(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties settings, Item.Properties itemSettings) {
        Block block = registerSimpleBlock(name, factory, settings);
        BLOCKS.add(block);
        return block;
    }

    public static Block registerBlock(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties settings) {
        return registerBlock(name, factory, settings, new Item.Properties());
    }

    public static ResourceKey<Block> keyOf(String id) {
        return ResourceKey.create(Registries.BLOCK, ReverieDreams.id(id));
    }

    public static ResourceKey<Block> keyOf(ResourceLocation id) {
        return ResourceKey.create(Registries.BLOCK, id);
    }

}
