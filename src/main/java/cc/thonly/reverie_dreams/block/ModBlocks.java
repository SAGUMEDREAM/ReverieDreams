package cc.thonly.reverie_dreams.block;

import cc.thonly.polymer.PolymerBlockHelper;
import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.item.ModItems;
import cc.thonly.reverie_dreams.world.sapling.SaplingGeneratorInit;
import lombok.Getter;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;

@Getter
public class ModBlocks {
    public static final Set<Block> BLOCKS = new LinkedHashSet<>();
    public static final Block DANMAKU_CRAFTING_TABLE = registerBlock("danmaku_crafting_table", DanmakuCraftingTableBlock::new, AbstractBlock.Settings.copy(Blocks.CRAFTING_TABLE));
    public static final Block STRENGTH_TABLE = registerBlock("strength_table", StrengthenTableBlock::new, AbstractBlock.Settings.copy(Blocks.SMITHING_TABLE));
    public static final Block GENSOKYO_ALTAR = registerBlock("gensokyo_altar", GensokyoAltarBlock::new, AbstractBlock.Settings.copy(Blocks.ENCHANTING_TABLE).luminance((state) -> 7));
    public static final Block MUSIC_BLOCK = registerBlock("music_block", MusicBlock::new, AbstractBlock.Settings.copy(Blocks.NOTE_BLOCK));

    public static final WoodCreator SPIRITUAL = WoodCreator.create("spiritual", SaplingGeneratorInit.SPIRITUAL_TREE).build();
    public static final Block MAGIC_ICE_BLOCK = registerBlock("magic_ice", Block::new, AbstractBlock.Settings.copy(Blocks.BLUE_ICE));
    public static final Block POINT_BLOCK = registerBlock("point_block", Block::new, AbstractBlock.Settings.copy(Blocks.STONE));
    public static final Block POWER_BLOCK = registerBlock("power_block", Block::new, AbstractBlock.Settings.copy(Blocks.STONE));
    public static final DecorativeBlockCreator ICE_SCALES = DecorativeBlockCreator.create("ice_scales_block").build();
    public static final DecorativeBlockCreator DREAM_STONE = DecorativeBlockCreator.create("dream_stone").build();
    public static final DecorativeBlockCreator DREAM_STONE_BRICK = DecorativeBlockCreator.create("dream_stone_brick").build();
    public static final DecorativeBlockCreator MOON_STONE = DecorativeBlockCreator.create("moon_stone").build();
    public static final DecorativeBlockCreator MOON_STONE_BRICK = DecorativeBlockCreator.create("moon_stone_brick").build();

    static {
        DREAM_STONE_BRICK.base(DREAM_STONE.block());
        MOON_STONE_BRICK.base(MOON_STONE.block());
    }

    public static final Block SILVER_ORE = registerBlock("silver_ore", Block::new, AbstractBlock.Settings.copy(Blocks.IRON_ORE));
    public static final Block DEEPSLATE_SILVER_ORE = registerBlock("deepslate_silver_ore", Block::new, AbstractBlock.Settings.copy(Blocks.DEEPSLATE_IRON_ORE));
    public static final Block SILVER_BLOCK = registerBlock("silver_block", Block::new, AbstractBlock.Settings.copy(Blocks.IRON_BLOCK));
    public static final ChestBlockCreator SILVER_CHEST_BLOCK = ChestBlockCreator.create(
            "silver_chest",
            AbstractBlock.Settings.copy(Blocks.IRON_BLOCK)
    ).build();
    public static final Block ORB_ORE = registerBlock("orb_ore", Block::new, AbstractBlock.Settings.copy(Blocks.DEEPSLATE_IRON_ORE));
    public static final Block DEEPSLATE_ORB_ORE = registerBlock("deepslate_orb_ore", Block::new, AbstractBlock.Settings.copy(Blocks.DEEPSLATE_IRON_ORE));
    public static final Block RED_ORB_BLOCK = registerBlock("red_orb_block", Block::new, AbstractBlock.Settings.copy(Blocks.EMERALD_BLOCK));
    public static final Block YELLOW_ORB_BLOCK = registerBlock("yellow_orb_block", Block::new, AbstractBlock.Settings.copy(Blocks.EMERALD_BLOCK));
    public static final Block BLUE_ORB_BLOCK = registerBlock("blue_orb_block", Block::new, AbstractBlock.Settings.copy(Blocks.EMERALD_BLOCK));
    public static final Block GREEN_ORB_BLOCK = registerBlock("green_orb_block", Block::new, AbstractBlock.Settings.copy(Blocks.EMERALD_BLOCK));
    public static final Block PURPLE_ORB_BLOCK = registerBlock("purple_orb_block", Block::new, AbstractBlock.Settings.copy(Blocks.EMERALD_BLOCK));

    public static final Block DREAM_RED_BLOCK = registerBlock("dream_world_red_line_block", Block::new, AbstractBlock.Settings.copy(Blocks.BEDROCK));
    public static final Block DREAM_BLUE_BLOCK = registerBlock("dream_world_blue_line_block", Block::new, AbstractBlock.Settings.copy(Blocks.BEDROCK));
    public static final Block DREAM_CRYSTAL_ORE = registerBlock("dream_crystal_ore", Block::new, AbstractBlock.Settings.copy(Blocks.IRON_ORE));

    public static final Block MARISA_HAT_BLOCK = registerBlock("marisa_hat", (settings) -> new MarisaHatBlock(Vec3d.ZERO, settings), AbstractBlock.Settings.copy(Blocks.WHITE_WOOL), new Item.Settings().maxCount(1).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.HEAD).swappable(false).build()));
    public static final Block CASH_BOX_BLOCK = registerBlock("cash_box", CashBoxBlock::new, AbstractBlock.Settings.copy(Blocks.OAK_PLANKS));
    public static final Block ANTI_COLLISION_BARREL = registerBlock("anti_collision_barrel", ModelBlock::new, AbstractBlock.Settings.copy(Blocks.IRON_BLOCK));
    public static final Block WHEEL_CHAIR = registerBlock("wheel_chair", WheelChairBlock::new, AbstractBlock.Settings.copy(Blocks.IRON_BLOCK));
    public static final ChestBlockCreator WOODEN_BOX = ChestBlockCreator.create(
            "wooden_box",
            AbstractBlock.Settings.copy(Blocks.CHEST)
    ).build();
    public static void registerBlocks() {

    }

    public static Block registerSimpleBlock(Block block) {
        PolymerBlockHelper.registerOverlay(block);
        BlockTypeGroup.join(block);
        return block;
    }

    public static Block registerSimpleBlock(Identifier id, Function<AbstractBlock.Settings, Block> factory, AbstractBlock.Settings settings, Item.Settings itemSettings) {
        Block block = factory.apply(settings.registryKey(keyOf(id)).nonOpaque());
        Registry.register(Registries.BLOCK, id, block);
        Item item = ModItems.registerSimpleItem(id, (itemSetting -> new BlockItem(block, itemSetting)), itemSettings.useBlockPrefixedTranslationKey());
        return registerSimpleBlock(block);
    }

    public static Block registerSimpleBlock(String name, Function<AbstractBlock.Settings, Block> factory, AbstractBlock.Settings settings, Item.Settings itemSettings) {
        return registerSimpleBlock(Touhou.id(name), factory, settings, itemSettings);
    }

    public static Block registerSimpleBlock(Identifier id, Function<AbstractBlock.Settings, Block> factory, AbstractBlock.Settings settings) {
        return registerSimpleBlock(id, factory, settings, new Item.Settings());
    }

    public static Block registerSimpleBlock(String name, Function<AbstractBlock.Settings, Block> factory, AbstractBlock.Settings settings) {
        return registerSimpleBlock(name, factory, settings, new Item.Settings());
    }

    public static Block registerBlock(String name, Function<AbstractBlock.Settings, Block> factory, AbstractBlock.Settings settings, Item.Settings itemSettings) {
        Block block = registerSimpleBlock(name, factory, settings);
        BLOCKS.add(block);
        return block;
    }

    public static Block registerBlock(String name, Function<AbstractBlock.Settings, Block> factory, AbstractBlock.Settings settings) {
        return registerBlock(name, factory, settings, new Item.Settings());
    }

    public static RegistryKey<Block> keyOf(String id) {
        return RegistryKey.of(RegistryKeys.BLOCK, Touhou.id(id));
    }

    public static RegistryKey<Block> keyOf(Identifier id) {
        return RegistryKey.of(RegistryKeys.BLOCK, id);
    }

}
