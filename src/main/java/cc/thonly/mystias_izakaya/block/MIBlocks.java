package cc.thonly.mystias_izakaya.block;

import cc.thonly.mystias_izakaya.MystiasIzakaya;
import cc.thonly.mystias_izakaya.block.crop.*;
import cc.thonly.mystias_izakaya.block.kitchenware.*;
import cc.thonly.mystias_izakaya.item.MIItems;
import cc.thonly.reverie_dreams.block.CropBlockCreator;
import cc.thonly.reverie_dreams.block.ModBlocks;
import cc.thonly.reverie_dreams.block.WoodCreator;
import cc.thonly.reverie_dreams.block.base.FruitLeavesBlock;
import cc.thonly.reverie_dreams.debug.DebugExportWriter;
import cc.thonly.reverie_dreams.util.block.CropAgeModelProvider;
import cc.thonly.reverie_dreams.util.ConstantInfo;
import cc.thonly.reverie_dreams.world.sapling.SaplingGeneratorInit;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.block.*;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.function.DoubleUnaryOperator;

@Slf4j
public class MIBlocks extends ModBlocks {
    public static final DoubleUnaryOperator MYSTIA = original -> original - original * 0.25;
    public static final DoubleUnaryOperator SUPER = original -> original + original * 0.05;
    public static final DoubleUnaryOperator EXTREME = original -> original + original * 0.1;
    public static final DoubleUnaryOperator NUKE = original -> original + original * 0.5;
    // 普通
    public static final Block COOKING_POT = registerSimpleBlock("cooking_pot",
            (settings) -> new CookingPot((original) -> original, 0.0, settings),
            AbstractBlock.Settings.create().strength(2.0f, 3.0f).sounds(BlockSoundGroup.STONE)
    );
    public static final Block CUTTING_BOARD = registerSimpleBlock("cutting_board",
            (settings) -> new CuttingBoard((original) -> original, 0.0, settings),
            AbstractBlock.Settings.create().strength(2.0f, 3.0f).sounds(BlockSoundGroup.WOOD)
    );
    public static final Block FRYING_PAN = registerSimpleBlock("frying_pan",
            (settings) -> new FryingPan((original) -> original, 0.0, settings),
            AbstractBlock.Settings.create().strength(2.0f, 3.0f).sounds(BlockSoundGroup.METAL)
    );
    public static final Block GRILL = registerSimpleBlock("grill",
            (settings) -> new Grill((original) -> original, 0.0, settings),
            AbstractBlock.Settings.create().strength(2.0f, 3.0f).sounds(BlockSoundGroup.METAL)
    );
    public static final Block STEAMER = registerSimpleBlock("steamer",
            (settings) -> new Steamer((original) -> original, 0.0, settings),
            AbstractBlock.Settings.create().strength(2.0f, 3.0f).sounds(BlockSoundGroup.STONE)
    );
    // 夜雀
    public static final Block MYSTIA_COOKING_POT = registerSimpleBlock("mystia_cooking_pot",
            (settings) -> new CookingPot(MYSTIA, 0.0, settings),
            AbstractBlock.Settings.create().strength(2.0f, 3.0f).sounds(BlockSoundGroup.STONE)
    );
    public static final Block MYSTIA_CUTTING_BOARD = registerSimpleBlock("mystia_cutting_board",
            (settings) -> new CuttingBoard(MYSTIA, 0.0, settings),
            AbstractBlock.Settings.create().strength(2.0f, 3.0f).sounds(BlockSoundGroup.WOOD)
    );
    public static final Block MYSTIA_FRYING_PAN = registerSimpleBlock("mystia_frying_pan",
            (settings) -> new FryingPan(MYSTIA, 0.0, settings),
            AbstractBlock.Settings.create().strength(2.0f, 3.0f).sounds(BlockSoundGroup.METAL)
    );
    public static final Block MYSTIA_GRILL = registerSimpleBlock("mystia_grill",
            (settings) -> new Grill(MYSTIA, 0.0, settings),
            AbstractBlock.Settings.create().strength(2.0f, 3.0f).sounds(BlockSoundGroup.METAL)
    );
    public static final Block MYSTIA_STEAMER = registerSimpleBlock("mystia_steamer",
            (settings) -> new Steamer(MYSTIA, 0.0, settings),
            AbstractBlock.Settings.create().strength(2.0f, 3.0f).sounds(BlockSoundGroup.STONE)
    );
    // 超
    public static final Block SUPER_COOKING_POT = registerSimpleBlock("super_cooking_pot",
            (settings) -> new CookingPot(SUPER, 0.0, settings),
            AbstractBlock.Settings.create().strength(2.0f, 3.0f).sounds(BlockSoundGroup.STONE)
    );
    public static final Block SUPER_CUTTING_BOARD = registerSimpleBlock("super_cutting_board",
            (settings) -> new CuttingBoard(SUPER, 0.0, settings),
            AbstractBlock.Settings.create().strength(2.0f, 3.0f).sounds(BlockSoundGroup.WOOD)
    );
    public static final Block SUPER_FRYING_PAN = registerSimpleBlock("super_frying_pan",
            (settings) -> new FryingPan(SUPER, 0.0, settings),
            AbstractBlock.Settings.create().strength(2.0f, 3.0f).sounds(BlockSoundGroup.METAL)
    );
    public static final Block SUPER_GRILL = registerSimpleBlock("super_grill",
            (settings) -> new Grill(SUPER, 0.0, settings),
            AbstractBlock.Settings.create().strength(2.0f, 3.0f).sounds(BlockSoundGroup.METAL)
    );
    public static final Block SUPER_STEAMER = registerSimpleBlock(
            "super_steamer",
            (settings) -> new Steamer(SUPER, 0.0, settings),
            AbstractBlock.Settings.create().strength(2.0f, 3.0f).sounds(BlockSoundGroup.STONE)
    );
    // 极
    public static final Block EXTREME_COOKING_POT = registerSimpleBlock(
            "extreme_cooking_pot",
            settings -> new CookingPot(EXTREME, 0.0, settings),
            AbstractBlock.Settings.create().strength(2.0f, 3.0f).sounds(BlockSoundGroup.STONE)
    );
    public static final Block EXTREME_CUTTING_BOARD = registerSimpleBlock(
            "extreme_cutting_board",
            settings -> new CuttingBoard(EXTREME, 0.0, settings),
            AbstractBlock.Settings.create().strength(2.0f, 3.0f).sounds(BlockSoundGroup.WOOD)
    );
    public static final Block EXTREME_FRYING_PAN = registerSimpleBlock(
            "extreme_frying_pan",
            settings -> new FryingPan(EXTREME, 0.0, settings),
            AbstractBlock.Settings.create().strength(2.0f, 3.0f).sounds(BlockSoundGroup.METAL)
    );
    public static final Block EXTREME_GRILL = registerSimpleBlock(
            "extreme_grill",
            settings -> new Grill(EXTREME, 0.0, settings),
            AbstractBlock.Settings.create().strength(2.0f, 3.0f).sounds(BlockSoundGroup.METAL)
    );
    public static final Block EXTREME_STEAMER = registerSimpleBlock(
            "extreme_steamer",
            settings -> new Steamer(EXTREME, 0.0, settings),
            AbstractBlock.Settings.create().strength(2.0f, 3.0f).sounds(BlockSoundGroup.STONE)
    );
    // 核能
    public static final Block NUKE_COOKING_POT = registerSimpleBlock(
            "nuke_cooking_pot",
            settings -> new CookingPot(NUKE, 0.4, settings),
            AbstractBlock.Settings.create().strength(2.0f, 3.0f).sounds(BlockSoundGroup.STONE)
    );
    public static final Block NUKE_CUTTING_BOARD = registerSimpleBlock(
            "nuke_cutting_board",
            settings -> new CuttingBoard(NUKE, 0.4, settings),
            AbstractBlock.Settings.create().strength(2.0f, 3.0f).sounds(BlockSoundGroup.WOOD)
    );
    public static final Block NUKE_FRYING_PAN = registerSimpleBlock(
            "nuke_frying_pan",
            settings -> new FryingPan(NUKE, 0.4, settings),
            AbstractBlock.Settings.create().strength(2.0f, 3.0f).sounds(BlockSoundGroup.METAL)
    );
    public static final Block NUKE_GRILL = registerSimpleBlock(
            "nuke_grill",
            settings -> new Grill(NUKE, 0.4, settings),
            AbstractBlock.Settings.create().strength(2.0f, 3.0f).sounds(BlockSoundGroup.METAL)
    );
    public static final Block NUKE_STEAMER = registerSimpleBlock(
            "nuke_steamer",
            settings -> new Steamer(NUKE, 0.4, settings),
            AbstractBlock.Settings.create().strength(2.0f, 3.0f).sounds(BlockSoundGroup.STONE)
    );

    public static final Block ITEM_DISPLAY = registerSimpleBlock(
            "display",
            ItemStackDisplay::new,
            AbstractBlock.Settings.copy(Blocks.WHITE_WOOL).nonOpaque().sounds(BlockSoundGroup.GLASS)
    );
    public static final Block BLACK_SALT_BLOCK = registerSimpleBlock(
            "black_salt_block",
            Block::new,
            AbstractBlock.Settings.copy(Blocks.SAND)
    );
    public static final WoodCreator LEMON = WoodCreator.create(
            "lemon", SaplingGeneratorInit.LEMON_TREE).build();
    public static final Block LEMON_FRUIT_LEAVES = registerSimpleBlock(
            "lemon_fruit_leaves",
            (settings) -> new FruitLeavesBlock(MIItems.LEMON, LEMON.leaves(), settings), AbstractBlock.Settings.copy(Blocks.OAK_LEAVES));

    public static final WoodCreator GINKGO = WoodCreator.create(
            "ginkgo", SaplingGeneratorInit.GINKGO_TREE).build();
    public static final Block GINKGO_FRUIT_LEAVES = registerSimpleBlock(
            "ginkgo_fruit_leaves",
            (settings) -> new FruitLeavesBlock(MIItems.GINKGO, GINKGO.leaves(), settings), AbstractBlock.Settings.copy(Blocks.OAK_LEAVES));

    public static final WoodCreator PEACH = WoodCreator.create(
            "peach", SaplingGeneratorInit.PEACH_TREE).build();
    public static final Block PEACH_FRUIT_LEAVES = registerSimpleBlock(
            "peach_fruit_leaves",
            (settings) -> new FruitLeavesBlock(MIItems.PEACH, PEACH.leaves(), settings), AbstractBlock.Settings.copy(Blocks.OAK_LEAVES));

    public static final Block UDUMBARA_FLOWER = registerSimpleBlock(
            "udumbara_flower",
            (settings) -> new FertilizableFlower(StatusEffects.REGENERATION, 3f, settings), createPlantSettings());
    public static final Block TREMELLA = registerSimpleBlock(
            "tremella",
            (settings) -> new FlowerBlock(StatusEffects.REGENERATION, 3f, settings), createPlantSettings());

    public static final CropBlockCreator.Instance CHILL = CropBlockCreator
            .createCreator(MystiasIzakaya.id("chill"))
            .setFactory(ChillCrop::new)
            .setMaxAge(7)
            .setGain(MIItems.CHILI)
            .setModelType(CropBlockCreator.ModelType.CROSS)
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

    public static final CropBlockCreator.Instance CUCUMBER = CropBlockCreator
            .createCreator(MystiasIzakaya.id("cucumber"))
            .setFactory(CucumberCrop::new)
            .setMaxAge(7)
            .setGain(MIItems.CUCUMBER)
            .setModelType(CropBlockCreator.ModelType.CROSS)
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

    public static final CropBlockCreator.Instance GRAPE = CropBlockCreator
            .createCreator(MystiasIzakaya.id("grape"))
            .setFactory(GrapeCrop::new)
            .setMaxAge(7)
            .setGain(MIItems.GRAPE)
            .setModelType(CropBlockCreator.ModelType.CROP)
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

    public static final CropBlockCreator.Instance ONION = CropBlockCreator
            .createCreator(MystiasIzakaya.id("onion"))
            .setFactory(OnionCrop::new)
            .setMaxAge(7)
            .setGain(MIItems.ONION)
            .setModelType(CropBlockCreator.ModelType.CROP)
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

    public static final CropBlockCreator.Instance RED_BEANS = CropBlockCreator
            .createCreator(MystiasIzakaya.id("red_beans"))
            .setFactory(RedBeansCrop::new)
            .setMaxAge(6)
            .setGain(MIItems.RED_BEANS)
            .setModelType(CropBlockCreator.ModelType.CROSS)
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

    public static final CropBlockCreator.Instance TOMATO = CropBlockCreator
            .createCreator(MystiasIzakaya.id("tomato"))
            .setFactory(TomatoCrop::new)
            .setMaxAge(6)
            .setGain(MIItems.TOMATO)
            .setModelType(CropBlockCreator.ModelType.CROSS)
            .setProvider(
                    CropAgeModelProvider.create(6)
                            .setKey(2).setValue(1)
                            .setKey(4).setValue(2)
                            .setKey(5).setValue(3)
                            .setKey(6).setValue(4)
                            .build()
            )
            .build();
    public static final CropBlockCreator.Instance TOON = CropBlockCreator
            .createCreator(MystiasIzakaya.id("toon"))
            .setFactory(ToonCrop::new)
            .setMaxAge(8)
            .setGain(MIItems.TOON)
            .setModelType(CropBlockCreator.ModelType.CROSS)
            .setProvider(
                    CropAgeModelProvider.create(8)
                            .setKey(2, 3).setValue(1)
                            .setKey(4, 5).setValue(2)
                            .setKey(6).setValue(3)
                            .setKey(8).setValue(4)
                            .build()
            )
            .build();
    public static final CropBlockCreator.Instance WHITE_RADISH = CropBlockCreator
            .createCreator(MystiasIzakaya.id("white_radish"))
            .setFactory(WhiteRadishCrop::new)
            .setMaxAge(8)
            .setGain(MIItems.WHITE_RADISH)
            .setModelType(CropBlockCreator.ModelType.CROP)
            .setProvider(
                    CropAgeModelProvider.create(8)
                            .setKey(2, 3).setValue(1)
                            .setKey(4, 5).setValue(2)
                            .setKey(6).setValue(3)
                            .setKey(8).setValue(4)
                            .build()
            )
            .build();
    public static final CropBlockCreator.Instance SWEET_POTATO = CropBlockCreator
            .createCreator(MystiasIzakaya.id("sweet_potato"))
            .setFactory(SweetPotatoCrop::new)
            .setMaxAge(6)
            .setGain(MIItems.SWEET_POTATO)
            .setModelType(CropBlockCreator.ModelType.CROP)
            .setProvider(
                    CropAgeModelProvider.create(6)
                            .setKey(2).setValue(1)
                            .setKey(3).setValue(2)
                            .setKey(4, 5).setValue(3)
                            .setKey(6).setValue(4)
                            .build()
            )
            .build();
    public static final CropBlockCreator.Instance BROCCOLI = CropBlockCreator
            .createCreator(MystiasIzakaya.id("broccoli"))
            .setFactory(BroccoliCrop::new)
            .setMaxAge(6)
            .setGain(MIItems.BROCCOLI)
            .setModelType(CropBlockCreator.ModelType.CROSS)
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
    public static final CropBlockCreator.Instance SOY_BEANS = CropBlockCreator
            .createCreator(MystiasIzakaya.id("soy_beans"))
            .setFactory(RedBeansCrop::new)
            .setMaxAge(6)
            .self()
            .setModelType(CropBlockCreator.ModelType.CROSS)
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


    public static final List<CropBlockCreator.Instance> GRASS_DROPS = new ArrayList<>(List.of(TOMATO, RED_BEANS, ONION, CUCUMBER, CHILL, BROCCOLI, SOY_BEANS));
    public static final List<CropBlockCreator.Instance> CHEST_DROPS = new ArrayList<>(List.of(SWEET_POTATO, WHITE_RADISH, TOON, RED_BEANS, GRAPE));

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
        if (ConstantInfo.isDevMode()) {
            DebugExportWriter output = DebugExportWriter.OUTPUT;
            output.write("== Crop Block Textures ==");
            for (Map.Entry<Identifier, CropBlockCreator.Instance> view : CropBlockCreator.getViews()) {
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


}
