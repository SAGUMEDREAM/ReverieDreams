package cc.thonly.mystias_izakaya.block;

import cc.thonly.mystias_izakaya.MystiasIzakaya;
import cc.thonly.mystias_izakaya.item.MIItems;
import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.block.WoodCreator;
import cc.thonly.reverie_dreams.block.base.BasicFruitLeavesBlock;
import cc.thonly.reverie_dreams.block.base.BasicPlantBlock;
import cc.thonly.reverie_dreams.block.base.BasicPolymerBlock;
import cc.thonly.reverie_dreams.datagen.ModBlockTagProvider;
import cc.thonly.reverie_dreams.datagen.ModItemTagProvider;
import cc.thonly.reverie_dreams.debug.DebugExportWriter;
import cc.thonly.reverie_dreams.item.BasicBlockItem;
import cc.thonly.reverie_dreams.util.CropAgeModelProvider;
import cc.thonly.reverie_dreams.util.IdentifierGetter;
import cc.thonly.reverie_dreams.block.PolymerCropCreator;
import cc.thonly.reverie_dreams.world.SaplingGeneratorInit;
import eu.pb4.polymer.blocks.api.BlockModelType;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.block.*;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;

import cc.thonly.mystias_izakaya.block.crop.*;
import cc.thonly.mystias_izakaya.block.kitchenware.*;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.function.DoubleUnaryOperator;

@Slf4j
public class MIBlocks {
    public static final DoubleUnaryOperator MYSTIA = original -> original - original * 0.25;
    public static final DoubleUnaryOperator SUPER = original -> original + original * 0.05;
    public static final DoubleUnaryOperator EXTREME = original -> original + original * 0.1;
    // 普通
    public static final Block COOKING_POT = registerBlock(new CookingPot("cooking_pot", (original) -> original, 0.0, AbstractBlock.Settings.create().strength(2.0f, 3.0f).sounds(BlockSoundGroup.STONE)));
    public static final Block CUTTING_BOARD = registerBlock(new CuttingBoard("cutting_board", (original) -> original, 0.0, AbstractBlock.Settings.create().strength(2.0f, 3.0f).sounds(BlockSoundGroup.WOOD)));
    public static final Block FRYING_PAN = registerBlock(new FryingPan("frying_pan", (original) -> original, 0.0, AbstractBlock.Settings.create().strength(2.0f, 3.0f).sounds(BlockSoundGroup.METAL)));
    public static final Block GRILL = registerBlock(new Grill("grill", (original) -> original, 0.0, AbstractBlock.Settings.create().strength(2.0f, 3.0f).sounds(BlockSoundGroup.METAL)));
    public static final Block STEAMER = registerBlock(new Steamer("steamer", (original) -> original, 0.0, AbstractBlock.Settings.create().strength(2.0f, 3.0f).sounds(BlockSoundGroup.STONE)));
    // 夜雀
    public static final Block MYSTIA_COOKING_POT = registerBlock(new CookingPot("mystia_cooking_pot", MYSTIA, 0.0, AbstractBlock.Settings.create().strength(2.0f, 3.0f).sounds(BlockSoundGroup.STONE)));
    public static final Block MYSTIA_CUTTING_BOARD = registerBlock(new CuttingBoard("mystia_cutting_board", MYSTIA, 0.0, AbstractBlock.Settings.create().strength(2.0f, 3.0f).sounds(BlockSoundGroup.WOOD)));
    public static final Block MYSTIA_FRYING_PAN = registerBlock(new FryingPan("mystia_frying_pan", MYSTIA, 0.0, AbstractBlock.Settings.create().strength(2.0f, 3.0f).sounds(BlockSoundGroup.METAL)));
    public static final Block MYSTIA_GRILL = registerBlock(new Grill("mystia_grill", MYSTIA, 0.0, AbstractBlock.Settings.create().strength(2.0f, 3.0f).sounds(BlockSoundGroup.METAL)));
    public static final Block MYSTIA_STEAMER = registerBlock(new Steamer("mystia_steamer", MYSTIA, 0.0, AbstractBlock.Settings.create().strength(2.0f, 3.0f).sounds(BlockSoundGroup.STONE)));
    // 超
    public static final Block SUPER_COOKING_POT = registerBlock(new CookingPot("super_cooking_pot", SUPER, 0.0, AbstractBlock.Settings.create().strength(2.0f, 3.0f).sounds(BlockSoundGroup.STONE)));
    public static final Block SUPER_CUTTING_BOARD = registerBlock(new CuttingBoard("super_cutting_board", SUPER, 0.0, AbstractBlock.Settings.create().strength(2.0f, 3.0f).sounds(BlockSoundGroup.WOOD)));
    public static final Block SUPER_FRYING_PAN = registerBlock(new FryingPan("super_frying_pan", SUPER, 0.0, AbstractBlock.Settings.create().strength(2.0f, 3.0f).sounds(BlockSoundGroup.METAL)));
    public static final Block SUPER_GRILL = registerBlock(new Grill("super_grill", SUPER, 0.0, AbstractBlock.Settings.create().strength(2.0f, 3.0f).sounds(BlockSoundGroup.METAL)));
    public static final Block SUPER_STEAMER = registerBlock(new Steamer("super_steamer", SUPER, 0.0, AbstractBlock.Settings.create().strength(2.0f, 3.0f).sounds(BlockSoundGroup.STONE)));
    // 极
    public static final Block EXTREME_COOKING_POT = registerBlock(new CookingPot("extreme_cooking_pot", EXTREME, 0.0, AbstractBlock.Settings.create().strength(2.0f, 3.0f).sounds(BlockSoundGroup.STONE)));
    public static final Block EXTREME_CUTTING_BOARD = registerBlock(new CuttingBoard("extreme_cutting_board", EXTREME, 0.0, AbstractBlock.Settings.create().strength(2.0f, 3.0f).sounds(BlockSoundGroup.WOOD)));
    public static final Block EXTREME_FRYING_PAN = registerBlock(new FryingPan("extreme_frying_pan", EXTREME, 0.0, AbstractBlock.Settings.create().strength(2.0f, 3.0f).sounds(BlockSoundGroup.METAL)));
    public static final Block EXTREME_GRILL = registerBlock(new Grill("extreme_grill", EXTREME, 0.0, AbstractBlock.Settings.create().strength(2.0f, 3.0f).sounds(BlockSoundGroup.METAL)));
    public static final Block EXTREME_STEAMER = registerBlock(new Steamer("extreme_steamer", EXTREME, 0.0, AbstractBlock.Settings.create().strength(2.0f, 3.0f).sounds(BlockSoundGroup.STONE)));

    public static final Block ITEM_DISPLAY = registerBlock(new ItemStackDisplay("display", AbstractBlock.Settings.copy(Blocks.WHITE_WOOL).nonOpaque().sounds(BlockSoundGroup.GLASS)));

    //    public static final Block COOKTOP = registerBlock(new CooktopBlock("cooktop", AbstractBlock.Settings.create().nonOpaque().mapColor(MapColor.STONE_GRAY).instrument(NoteBlockInstrument.BASEDRUM).requiresTool().strength(3.5f).luminance(CooktopBlock::getLuminance)));
    public static final Block BLACK_SALT_BLOCK = registerBlock(new BasicPolymerBlock("black_salt_block", BlockModelType.FULL_BLOCK, AbstractBlock.Settings.copy(Blocks.SAND)));

    public static final WoodCreator LEMON = WoodCreator.create("lemon", SaplingGeneratorInit.LEMON_TREE).build();
    public static final Block LEMON_FRUIT_LEAVES = registerBlock(new BasicFruitLeavesBlock("lemon_fruit_leaves", MIItems.LEMON, LEMON.leaves(), AbstractBlock.Settings.copy(Blocks.OAK_LEAVES)));

    public static final WoodCreator GINKGO = WoodCreator.create("ginkgo", SaplingGeneratorInit.GINKGO_TREE).build();
    public static final Block GINKGO_FRUIT_LEAVES = registerBlock(new BasicFruitLeavesBlock("ginkgo_fruit_leaves", MIItems.GINKGO, GINKGO.leaves(), AbstractBlock.Settings.copy(Blocks.OAK_LEAVES)));

    public static final WoodCreator PEACH = WoodCreator.create("peach", SaplingGeneratorInit.PEACH_TREE).build();
    public static final Block PEACH_FRUIT_LEAVES = registerBlock(new BasicFruitLeavesBlock("peach_fruit_leaves", MIItems.PEACH, PEACH.leaves(), AbstractBlock.Settings.copy(Blocks.OAK_LEAVES)));

    public static final Block UDUMBARA_FLOWER = registerBlock(new BasicPlantBlock("udumbara_flower", createPlantSettings()));
    public static final Block TREMELLA = registerBlock(new BasicPlantBlock("tremella", createPlantSettings()));

    public static final PolymerCropCreator.Instance CHILL = PolymerCropCreator
            .createCreator(MystiasIzakaya.id("chill"))
            .setFactory(id -> new ChillCrop(id, AbstractBlock.Settings.create()))
            .setMaxAge(7)
            .setGain(MIItems.CHILI)
            .setModelType(PolymerCropCreator.ModelType.CROSS)
            .setProvider(
                    CropAgeModelProvider.create(7)
                            .setKey(2, 3).setValue(1)
                            .setKey(4).setValue(2)
                            .setKey(5).setValue(3)
                            .setKey(6).setValue(4)
                            .setKey(7).setValue(5)
                            .build()
            )
            .build();

    public static final PolymerCropCreator.Instance CUCUMBER = PolymerCropCreator
            .createCreator(MystiasIzakaya.id("cucumber"))
            .setFactory(id -> new CucumberCrop(id, AbstractBlock.Settings.create()))
            .setMaxAge(7)
            .setGain(MIItems.CUCUMBER)
            .setModelType(PolymerCropCreator.ModelType.CROSS)
            .setProvider(
                    CropAgeModelProvider.create(7)
                            .setKey(2).setValue(1)
                            .setKey(3).setValue(2)
                            .setKey(4, 5).setValue(3)
                            .setKey(6).setValue(4)
                            .setKey(7).setValue(5)
                            .build()
            )
            .build();

    public static final PolymerCropCreator.Instance GRAPE = PolymerCropCreator
            .createCreator(MystiasIzakaya.id("grape"))
            .setFactory(id -> new GrapeCrop(id, AbstractBlock.Settings.create()))
            .setMaxAge(7)
            .setGain(MIItems.GRAPE)
            .setModelType(PolymerCropCreator.ModelType.CROP)
            .setProvider(
                    CropAgeModelProvider.create(7)
                            .setKey(2).setValue(1)
                            .setKey(3).setValue(2)
                            .setKey(4).setValue(3)
                            .setKey(5).setValue(4)
                            .setKey(6).setValue(5)
                            .setKey(7).setValue(6)
                            .build()
            )
            .build();

    public static final PolymerCropCreator.Instance ONION = PolymerCropCreator
            .createCreator(MystiasIzakaya.id("onion"))
            .setFactory(id -> new OnionCrop(id, AbstractBlock.Settings.create()))
            .setMaxAge(7)
            .setGain(MIItems.ONION)
            .setModelType(PolymerCropCreator.ModelType.CROP)
            .setProvider(
                    CropAgeModelProvider.create(7)
                            .setKey(1).setValue(1)
                            .setKey(2, 3).setValue(2)
                            .setKey(4).setValue(3)
                            .setKey(5).setValue(4)
                            .setKey(6).setValue(5)
                            .setKey(7).setValue(6)
                            .build()
            )
            .build();

    public static final PolymerCropCreator.Instance RED_BEANS = PolymerCropCreator
            .createCreator(MystiasIzakaya.id("red_beans"))
            .setFactory(id -> new RedBeansCrop(id, AbstractBlock.Settings.create()))
            .setMaxAge(6)
            .setGain(MIItems.RED_BEANS)
            .setModelType(PolymerCropCreator.ModelType.CROSS)
            .setProvider(
                    CropAgeModelProvider.create(6)
                            .setKey(1).setValue(1)
                            .setKey(2).setValue(2)
                            .setKey(3, 4).setValue(3)
                            .setKey(5).setValue(4)
                            .setKey(6).setValue(5)
                            .build()
            )
            .build();

    public static final PolymerCropCreator.Instance TOMATO = PolymerCropCreator
            .createCreator(MystiasIzakaya.id("tomato"))
            .setFactory(id -> new TomatoCrop(id, AbstractBlock.Settings.create()))
            .setMaxAge(6)
            .setGain(MIItems.TOMATO)
            .setModelType(PolymerCropCreator.ModelType.CROSS)
            .setProvider(
                    CropAgeModelProvider.create(6)
                            .setKey(2).setValue(1)
                            .setKey(4).setValue(2)
                            .setKey(5).setValue(3)
                            .setKey(6).setValue(4)
                            .build()
            )
            .build();
    public static final PolymerCropCreator.Instance TOON = PolymerCropCreator
            .createCreator(MystiasIzakaya.id("toon"))
            .setFactory(id -> new ToonCrop(id, AbstractBlock.Settings.create()))
            .setMaxAge(8)
            .setGain(MIItems.TOON)
            .setModelType(PolymerCropCreator.ModelType.CROSS)
            .setProvider(
                    CropAgeModelProvider.create(8)
                            .setKey(2, 3).setValue(1)
                            .setKey(4, 5).setValue(2)
                            .setKey(6).setValue(3)
                            .setKey(8).setValue(4)
                            .build()
            )
            .build();
    public static final PolymerCropCreator.Instance WHITE_RADISH = PolymerCropCreator
            .createCreator(MystiasIzakaya.id("white_radish"))
            .setFactory(id -> new WhiteRadishCrop(id, AbstractBlock.Settings.create()))
            .setMaxAge(8)
            .setGain(MIItems.WHITE_RADISH)
            .setModelType(PolymerCropCreator.ModelType.CROP)
            .setProvider(
                    CropAgeModelProvider.create(8)
                            .setKey(2, 3).setValue(1)
                            .setKey(4, 5).setValue(2)
                            .setKey(6).setValue(3)
                            .setKey(8).setValue(4)
                            .build()
            )
            .build();
    public static final PolymerCropCreator.Instance SWEET_POTATO = PolymerCropCreator
            .createCreator(MystiasIzakaya.id("sweet_potato"))
            .setFactory(id -> new SweetPotatoCrop(id, AbstractBlock.Settings.create()))
            .setMaxAge(6)
            .setGain(MIItems.SWEET_POTATO)
            .setModelType(PolymerCropCreator.ModelType.CROP)
            .setProvider(
                    CropAgeModelProvider.create(6)
                            .setKey(2).setValue(1)
                            .setKey(3).setValue(2)
                            .setKey(4, 5).setValue(3)
                            .setKey(6).setValue(4)
                            .build()
            )
            .build();
    public static final PolymerCropCreator.Instance BROCCOLI = PolymerCropCreator
            .createCreator(MystiasIzakaya.id("broccoli"))
            .setFactory(id -> new SweetPotatoCrop(id, AbstractBlock.Settings.create()))
            .setMaxAge(6)
            .setGain(MIItems.BROCCOLI)
            .setModelType(PolymerCropCreator.ModelType.CROSS)
            .setProvider(
                    CropAgeModelProvider.create(6)
                            .setKey(2).setValue(1)
                            .setKey(3).setValue(2)
                            .setKey(4).setValue(3)
                            .setKey(5).setValue(4)
                            .setKey(6).setValue(5)
                            .build()
            )
            .build();
    public static final PolymerCropCreator.Instance SOY_BEANS = PolymerCropCreator
            .createCreator(MystiasIzakaya.id("soy_beans"))
            .setFactory(id -> new RedBeansCrop(id, AbstractBlock.Settings.create()))
            .setMaxAge(6)
            .self()
            .setModelType(PolymerCropCreator.ModelType.CROSS)
            .setProvider(
                    CropAgeModelProvider.create(6)
                            .setKey(1).setValue(1)
                            .setKey(2).setValue(2)
                            .setKey(3, 4).setValue(3)
                            .setKey(5).setValue(4)
                            .setKey(6).setValue(5)
                            .build()
            )
            .build();


    public static final List<PolymerCropCreator.Instance> GRASS_DROPS = new ArrayList<>(List.of(TOMATO, RED_BEANS, ONION, CUCUMBER, CHILL, BROCCOLI, SOY_BEANS));
    public static final List<PolymerCropCreator.Instance> CHEST_DROPS = new ArrayList<>(List.of(SWEET_POTATO, WHITE_RADISH, TOON, RED_BEANS, GRAPE));

//    public static final PolymerCropCreator.Instance TEST_CROP = PolymerCropCreator
//            .createCreator(Touhou.id("test"))
//            .setFactory(id -> new TestCropBlock(id, AbstractBlock.Settings.create()))
//            .setMaxAge(7)
//            .setModelType(PolymerCropCreator.ModelType.CROSS)
//            .setProvider(
//                    CropAgeModelProvider.create(7)
//                            .setKey(2, 3).setValue(1)
//                            .setKey(4, 5, 6).setValue(2)
//                            .setKey(7).setValue(3)
//                            .build()
//            )
//            .build();

    public static AbstractBlock.Settings createPlantSettings() {
        return AbstractBlock.Settings.create().mapColor(MapColor.DARK_GREEN).noCollision().breakInstantly().sounds(BlockSoundGroup.GRASS).offset(AbstractBlock.OffsetType.XZ).pistonBehavior(PistonBehavior.DESTROY);
    }

    public static void registerBlocks() {
        if (Touhou.isDevMode()) {
            DebugExportWriter output = DebugExportWriter.OUTPUT;
            output.write("== Crop Block Textures ==");
            for (Map.Entry<Identifier, PolymerCropCreator.Instance> view : PolymerCropCreator.getViews()) {
                Set<String> strIds = new HashSet<>();
                int[] array = view.getValue().getProvider().toArray();
                for (int i : array) {
                    strIds.add(view.getKey().toString() + "_stage" + i);
                }
                output.write("%s: \t%s", view.getKey(), strIds);
            }
            output.write("=========================");
            output.write("");
        }
        KitchenBlockType.init();
    }

    public static Block registerBlock(IdentifierGetter block) {
        block = (IdentifierGetter) Registry.register(Registries.BLOCK, block.getIdentifier(), (Block) block);
        Item blockItem = null;
        boolean isIgnored = block instanceof SignBlock || block instanceof WallSignBlock || block instanceof HangingSignBlock || block instanceof WallHangingSignBlock;
        if (!isIgnored) {
            blockItem = MIItems.registerItem(new BasicBlockItem(block.getIdentifier(), (Block) block, new Item.Settings()));
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
        return (Block) block;
    }


}
