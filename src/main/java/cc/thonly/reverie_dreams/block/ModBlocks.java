package cc.thonly.reverie_dreams.block;

import cc.thonly.reverie_dreams.block.base.*;
import cc.thonly.reverie_dreams.datagen.ModBlockTagProvider;
import cc.thonly.reverie_dreams.datagen.ModItemTagProvider;
import cc.thonly.reverie_dreams.item.BasicBlockItem;
import cc.thonly.reverie_dreams.item.BasicCopyedBlockItem;
import cc.thonly.reverie_dreams.item.ModItems;
import cc.thonly.reverie_dreams.item.base.BasicPolymerHangingSignItem;
import cc.thonly.reverie_dreams.item.base.BasicPolymerSignItem;
import cc.thonly.reverie_dreams.util.IdentifierGetter;
import cc.thonly.reverie_dreams.world.ModSaplingGenerator;
import eu.pb4.polymer.blocks.api.BlockModelType;
import lombok.Getter;
import net.minecraft.block.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ModBlocks {
    private static final List<Block> BLOCK_LIST = new ArrayList<>();
    public static final Block DANMAKU_CRAFTING_TABLE = registerBlock(new DanmakuCraftingTableBlock("danmaku_crafting_table", AbstractBlock.Settings.copy(Blocks.CRAFTING_TABLE)));
    public static final Block STRENGTH_TABLE = registerBlock(new StrengthenTableBlock("strength_table", AbstractBlock.Settings.copy(Blocks.SMITHING_TABLE)));
    public static final Block GENSOKYO_ALTAR = registerBlock(new GensokyoAltarBlock("gensokyo_altar", AbstractBlock.Settings.copy(Blocks.ENCHANTING_TABLE).luminance((state) -> 7)));
    public static final Block MUSIC_BLOCK = registerBlock(new MusicBlock("music_block", AbstractBlock.Settings.copy(Blocks.NOTE_BLOCK)));

    public static final WoodCreator SPIRITUAL = WoodCreator.create("spiritual", ModSaplingGenerator.SPIRITUAL_TREE).build();
    public static final Block MAGIC_ICE_BLOCK = registerBlock(new MagicIceBlock("magic_ice", AbstractBlock.Settings.copy(Blocks.BLUE_ICE)));
    public static final Block POINT_BLOCK = registerBlock(new BasicPolymerBlock("point_block", BlockModelType.FULL_BLOCK, AbstractBlock.Settings.copy(Blocks.STONE)));
    public static final Block POWER_BLOCK = registerBlock(new BasicPolymerBlock("power_block", BlockModelType.FULL_BLOCK, AbstractBlock.Settings.copy(Blocks.STONE)));
    public static final DecorativeBlockCreator ICE_SCALES = DecorativeBlockCreator.create("ice_scales_block").build();
    public static final DecorativeBlockCreator DREAM_STONE = DecorativeBlockCreator.create("dream_stone").build();
    public static final DecorativeBlockCreator DREAM_STONE_BRICK = DecorativeBlockCreator.create("dream_stone_brick").build();
    public static final DecorativeBlockCreator MOON_STONE = DecorativeBlockCreator.create("moon_stone").build();
    public static final DecorativeBlockCreator MOON_STONE_BRICK = DecorativeBlockCreator.create("moon_stone_brick").build();
    static {
        DREAM_STONE_BRICK.base(DREAM_STONE.block());
        MOON_STONE_BRICK.base(MOON_STONE.block());
    }
    public static final Block SILVER_ORE = registerBlock(new BasicPolymerBlock("silver_ore", BlockModelType.FULL_BLOCK, AbstractBlock.Settings.copy(Blocks.IRON_ORE)));
    public static final Block DEEPSLATE_SILVER_ORE = registerBlock(new BasicPolymerBlock("deepslate_silver_ore", BlockModelType.FULL_BLOCK, AbstractBlock.Settings.copy(Blocks.DEEPSLATE_IRON_ORE)));
    public static final Block SILVER_BLOCK = registerBlock(new BasicPolymerBlock("silver_block", BlockModelType.FULL_BLOCK, AbstractBlock.Settings.copy(Blocks.IRON_BLOCK)));
    public static final Block ORB_ORE = registerBlock(new BasicPolymerBlock("orb_ore", BlockModelType.FULL_BLOCK, AbstractBlock.Settings.copy(Blocks.DEEPSLATE_IRON_ORE)));
    public static final Block DEEPSLATE_ORB_ORE = registerBlock(new BasicPolymerBlock("deepslate_orb_ore", BlockModelType.FULL_BLOCK, AbstractBlock.Settings.copy(Blocks.DEEPSLATE_IRON_ORE)));
    public static final Block RED_ORB_BLOCK = registerBlock(new BasicPolymerBlock("red_orb_block", BlockModelType.FULL_BLOCK, AbstractBlock.Settings.copy(Blocks.EMERALD_BLOCK)));
    public static final Block YELLOW_ORB_BLOCK = registerBlock(new BasicPolymerBlock("yellow_orb_block", BlockModelType.FULL_BLOCK, AbstractBlock.Settings.copy(Blocks.EMERALD_BLOCK)));
    public static final Block BLUE_ORB_BLOCK = registerBlock(new BasicPolymerBlock("blue_orb_block", BlockModelType.FULL_BLOCK, AbstractBlock.Settings.copy(Blocks.EMERALD_BLOCK)));
    public static final Block GREEN_ORB_BLOCK = registerBlock(new BasicPolymerBlock("green_orb_block", BlockModelType.FULL_BLOCK, AbstractBlock.Settings.copy(Blocks.EMERALD_BLOCK)));
    public static final Block PURPLE_ORB_BLOCK = registerBlock(new BasicPolymerBlock("purple_orb_block", BlockModelType.FULL_BLOCK, AbstractBlock.Settings.copy(Blocks.EMERALD_BLOCK)));

    public static final Block DREAM_RED_BLOCK = registerBlock(new BasicPolymerBlock("dream_world_red_line_block", BlockModelType.FULL_BLOCK, AbstractBlock.Settings.copy(Blocks.BEDROCK)));
    public static final Block DREAM_BLUE_BLOCK = registerBlock(new BasicPolymerBlock("dream_world_blue_line_block", BlockModelType.FULL_BLOCK, AbstractBlock.Settings.copy(Blocks.BEDROCK)));
    public static final Block MARISA_HAT_BLOCK = registerBlock(new MarisaHatBlock("marisa_hat", new Vec3d(0, 0, 0), AbstractBlock.Settings.copy(Blocks.WHITE_WOOL)), new Item.Settings().maxCount(1).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.HEAD).swappable(false).build()));

    public static void registerBlocks() {

//        SPIRITUAL_BLOCKS.put(BlockTypeTag.STRIPPED_OAK_LOG, ModBlocks.SPIRITUAL_OAK_LOG);
    }

    public static Block registerBlock(IdentifierGetter block) {
        return registerBlock(block, new Item.Settings());
    }

    public static Block registerBlock(IdentifierGetter block, Item.Settings settings) {
        block = (IdentifierGetter) Registry.register(Registries.BLOCK, block.getIdentifier(), (Block) block);
        Item blockItem = null;
        boolean isIgnored = block instanceof SignBlock || block instanceof WallSignBlock || block instanceof HangingSignBlock || block instanceof WallHangingSignBlock;
        if (!isIgnored) {
            blockItem = ModItems.registerItem(new BasicBlockItem(block.getIdentifier(), (Block) block, settings));
        }
        Block blk = (Block) block;
        if (blk instanceof FenceBlock) {
            ModBlockTagProvider.FENCES.add(blk);
            ModItemTagProvider.FENCES.add(blk.asItem());
        }
        if (blk instanceof FenceGateBlock) {
            ModBlockTagProvider.FENCE_GATES.add(blk);
            ModItemTagProvider.FENCE_GATES.add(blk.asItem());
        }
        if (blk instanceof StairsBlock) {
            ModBlockTagProvider.STAIRS.add(blk);
            ModItemTagProvider.STAIRS.add(blk.asItem());
        }
        if (blk instanceof SlabBlock) {
            ModBlockTagProvider.SLABS.add(blk);
            ModItemTagProvider.SLABS.add(blk.asItem());
        }
        if (blk instanceof ButtonBlock) {
            ModBlockTagProvider.BUTTONS.add(blk);
            ModItemTagProvider.BUTTONS.add(blk.asItem());
        }
        if (blk instanceof PressurePlateBlock) {
            ModBlockTagProvider.PRESSURE_PLATES.add(blk);
            ModItemTagProvider.PRESSURE_PLATES.add(blk.asItem());
        }
        if (blk instanceof TrapdoorBlock) {
            ModBlockTagProvider.TRAPDOORS.add(blk);
            ModItemTagProvider.TRAPDOORS.add(blk.asItem());
        }
        if (blk instanceof DoorBlock) {
            ModBlockTagProvider.DOORS.add(blk);
            ModItemTagProvider.DOORS.add(blk.asItem());
        }
        BLOCK_LIST.add((Block) block);
        return (Block) block;
    }


    public static Block registerSimpleBlock(IdentifierGetter block) {
        block = (IdentifierGetter) Registry.register(Registries.BLOCK, block.getIdentifier(), (Block) block);
        return (Block) block;
    }

    public static SignBlockGroup registerSignBlock(Block standingBlock, Block wallBlock) {
        return new SignBlockGroup(standingBlock, wallBlock, ModItems.registerItem(new BasicPolymerSignItem(((IdentifierGetter) standingBlock).getIdentifier(), standingBlock, wallBlock, new Item.Settings().useBlockPrefixedTranslationKey().maxCount(16))));
    }

    public static HangingSignBlockGroup registerHangSignBlock(Block standingBlock, Block wallBlock) {
        return new HangingSignBlockGroup(standingBlock, wallBlock, ModItems.registerItem(new BasicPolymerHangingSignItem(((IdentifierGetter) standingBlock).getIdentifier(), standingBlock, wallBlock, new Item.Settings().useBlockPrefixedTranslationKey().maxCount(16))));
    }

    public static Block registerCopyBlock(IdentifierGetter block) {
        Registry.register(Registries.BLOCK, block.getIdentifier(), (Block) block);
        ModItems.registerItem(new BasicCopyedBlockItem(block.getIdentifier(), (Block) block, ((BasicPolymerCopyedBlock) block).getTargetblock().asItem(), new Item.Settings()));
        BLOCK_LIST.add((Block) block);
        return (Block) block;
    }
}
