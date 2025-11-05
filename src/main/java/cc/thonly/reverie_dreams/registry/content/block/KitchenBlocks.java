package cc.thonly.reverie_dreams.registry.content.block;

import cc.thonly.reverie_dreams.block.KitchenBlockType;
import cc.thonly.reverie_dreams.block.kitchen.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.DoubleUnaryOperator;

public class KitchenBlocks {
    public static final DoubleUnaryOperator MYSTIA = original -> original - original * 0.25;
    public static final Block MYSTIA_STEAMER = RDBlocks.registerSimpleBlock("mystia_steamer",
            (settings) -> new Steamer(MYSTIA, 0.0, settings),
            BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.STONE)
    );
    public static final Block MYSTIA_GRILL = RDBlocks.registerSimpleBlock("mystia_grill",
            (settings) -> new Grill(MYSTIA, 0.0, settings),
            BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.METAL)
    );
    public static final Block MYSTIA_FRYING_PAN = RDBlocks.registerSimpleBlock("mystia_frying_pan",
            (settings) -> new FryingPan(MYSTIA, 0.0, settings),
            BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.METAL)
    );
    public static final Block MYSTIA_CUTTING_BOARD = RDBlocks.registerSimpleBlock("mystia_cutting_board",
            (settings) -> new CuttingBoard(MYSTIA, 0.0, settings),
            BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.WOOD)
    );
    // 夜雀
    public static final Block MYSTIA_COOKING_POT = RDBlocks.registerSimpleBlock("mystia_cooking_pot",
            (settings) -> new CookingPot(MYSTIA, 0.0, settings),
            BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.STONE)
    );
    public static final DoubleUnaryOperator SUPER = original -> original + original * 0.05;
    public static final Block SUPER_STEAMER = RDBlocks.registerSimpleBlock(
            "super_steamer",
            (settings) -> new Steamer(SUPER, 0.0, settings),
            BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.STONE)
    );
    public static final Block SUPER_GRILL = RDBlocks.registerSimpleBlock("super_grill",
            (settings) -> new Grill(SUPER, 0.0, settings),
            BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.METAL)
    );
    public static final Block SUPER_FRYING_PAN = RDBlocks.registerSimpleBlock("super_frying_pan",
            (settings) -> new FryingPan(SUPER, 0.0, settings),
            BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.METAL)
    );
    public static final Block SUPER_CUTTING_BOARD = RDBlocks.registerSimpleBlock("super_cutting_board",
            (settings) -> new CuttingBoard(SUPER, 0.0, settings),
            BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.WOOD)
    );
    // 超
    public static final Block SUPER_COOKING_POT = RDBlocks.registerSimpleBlock("super_cooking_pot",
            (settings) -> new CookingPot(SUPER, 0.0, settings),
            BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.STONE)
    );
    public static final DoubleUnaryOperator EXTREME = original -> original + original * 0.1;
    public static final Block EXTREME_STEAMER = RDBlocks.registerSimpleBlock(
            "extreme_steamer",
            settings -> new Steamer(EXTREME, 0.0, settings),
            BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.STONE)
    );
    public static final Block EXTREME_GRILL = RDBlocks.registerSimpleBlock(
            "extreme_grill",
            settings -> new Grill(EXTREME, 0.0, settings),
            BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.METAL)
    );
    public static final Block EXTREME_FRYING_PAN = RDBlocks.registerSimpleBlock(
            "extreme_frying_pan",
            settings -> new FryingPan(EXTREME, 0.0, settings),
            BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.METAL)
    );
    public static final Block EXTREME_CUTTING_BOARD = RDBlocks.registerSimpleBlock(
            "extreme_cutting_board",
            settings -> new CuttingBoard(EXTREME, 0.0, settings),
            BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.WOOD)
    );
    // 极
    public static final Block EXTREME_COOKING_POT = RDBlocks.registerSimpleBlock(
            "extreme_cooking_pot",
            settings -> new CookingPot(EXTREME, 0.0, settings),
            BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.STONE)
    );
    public static final DoubleUnaryOperator NUKE = original -> original + original * 0.5;
    public static final Block NUKE_STEAMER = RDBlocks.registerSimpleBlock(
            "nuke_steamer",
            settings -> new Steamer(NUKE, 0.4, settings),
            BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.STONE)
    );
    public static final Block NUKE_GRILL = RDBlocks.registerSimpleBlock(
            "nuke_grill",
            settings -> new Grill(NUKE, 0.4, settings),
            BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.METAL)
    );
    public static final Block NUKE_FRYING_PAN = RDBlocks.registerSimpleBlock(
            "nuke_frying_pan",
            settings -> new FryingPan(NUKE, 0.4, settings),
            BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.METAL)
    );
    public static final Block NUKE_CUTTING_BOARD = RDBlocks.registerSimpleBlock(
            "nuke_cutting_board",
            settings -> new CuttingBoard(NUKE, 0.4, settings),
            BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.WOOD)
    );
    // 核能
    public static final Block NUKE_COOKING_POT = RDBlocks.registerSimpleBlock(
            "nuke_cooking_pot",
            settings -> new CookingPot(NUKE, 0.4, settings),
            BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.STONE)
    );
    // 普通
    public static final Block COOKING_POT = RDBlocks.registerSimpleBlock("cooking_pot",
            (settings) -> new CookingPot((original) -> original, 0.0, settings),
            BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.STONE)
    );
    public static final Block CUTTING_BOARD = RDBlocks.registerSimpleBlock("cutting_board",
            (settings) -> new CuttingBoard((original) -> original, 0.0, settings),
            BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.WOOD)
    );
    public static final Block FRYING_PAN = RDBlocks.registerSimpleBlock("frying_pan",
            (settings) -> new FryingPan((original) -> original, 0.0, settings),
            BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.METAL)
    );
    public static final Block GRILL = RDBlocks.registerSimpleBlock("grill",
            (settings) -> new Grill((original) -> original, 0.0, settings),
            BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.METAL)
    );
    public static final Block STEAMER = RDBlocks.registerSimpleBlock("steamer",
            (settings) -> new Steamer((original) -> original, 0.0, settings),
            BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.STONE)
    );

    public static void registerBlocks() {
        KitchenBlockType.init();
    }
}
