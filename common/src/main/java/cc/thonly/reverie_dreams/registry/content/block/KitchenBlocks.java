package cc.thonly.reverie_dreams.registry.content.block;

import cc.thonly.reverie_dreams.block.KitchenBlockType;
import cc.thonly.reverie_dreams.block.kitchen.*;
import net.blay09.mods.balm.world.level.block.BalmBlockRegistrar;
import net.blay09.mods.balm.world.level.block.DeferredBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.DoubleUnaryOperator;

public class KitchenBlocks {
    public static DoubleUnaryOperator MYSTIA = original -> original - original * 0.25;
    public static DoubleUnaryOperator NORMAL = original -> original;
    public static DoubleUnaryOperator SUPER = original -> original + original * 0.05;
    public static DoubleUnaryOperator EXTREME = original -> original + original * 0.1;
    public static DoubleUnaryOperator NUKE = original -> original + original * 0.5;

    // 夜雀
    public static DeferredBlock MYSTIA_STEAMER;
    public static DeferredBlock MYSTIA_GRILL;
    public static DeferredBlock MYSTIA_FRYING_PAN;
    public static DeferredBlock MYSTIA_CUTTING_BOARD;
    public static DeferredBlock MYSTIA_COOKING_POT;
    // 超
    public static DeferredBlock SUPER_STEAMER;
    public static DeferredBlock SUPER_GRILL;
    public static DeferredBlock SUPER_FRYING_PAN;
    public static DeferredBlock SUPER_CUTTING_BOARD;
    public static DeferredBlock SUPER_COOKING_POT;
    // 极
    public static DeferredBlock EXTREME_STEAMER;
    public static DeferredBlock EXTREME_GRILL;
    public static DeferredBlock EXTREME_FRYING_PAN;
    public static DeferredBlock EXTREME_CUTTING_BOARD;
    public static DeferredBlock EXTREME_COOKING_POT;
    // 核能
    public static DeferredBlock NUKE_STEAMER;
    public static DeferredBlock NUKE_GRILL;
    public static DeferredBlock NUKE_FRYING_PAN;
    public static DeferredBlock NUKE_CUTTING_BOARD;
    public static DeferredBlock NUKE_COOKING_POT;
    // 普通
    public static DeferredBlock COOKING_POT;
    public static DeferredBlock CUTTING_BOARD;
    public static DeferredBlock FRYING_PAN;
    public static DeferredBlock GRILL;
    public static DeferredBlock STEAMER;

    public static void initialize(BalmBlockRegistrar registrar) {
        COOKING_POT = RDBlocks.registerSimpleBlock(registrar, "cooking_pot",
                s -> new CookingPot(NORMAL, 0.0, s),
                BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.STONE)
        );
        CUTTING_BOARD = RDBlocks.registerSimpleBlock(registrar, "cutting_board",
                s -> new CuttingBoard(NORMAL, 0.0, s),
                BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.WOOD)
        );
        FRYING_PAN = RDBlocks.registerSimpleBlock(registrar, "frying_pan",
                s -> new FryingPan(NORMAL, 0.0, s),
                BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.METAL)
        );
        GRILL = RDBlocks.registerSimpleBlock(registrar, "grill",
                s -> new Grill(NORMAL, 0.0, s),
                BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.METAL)
        );
        STEAMER = RDBlocks.registerSimpleBlock(registrar, "steamer",
                s -> new Steamer(NORMAL, 0.0, s),
                BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.STONE)
        );

        MYSTIA_STEAMER = RDBlocks.registerSimpleBlock(registrar, "mystia_steamer",
                s -> new Steamer(MYSTIA, 0.0, s),
                BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.STONE)
        );
        MYSTIA_GRILL = RDBlocks.registerSimpleBlock(registrar, "mystia_grill",
                s -> new Grill(MYSTIA, 0.0, s),
                BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.METAL)
        );
        MYSTIA_FRYING_PAN = RDBlocks.registerSimpleBlock(registrar, "mystia_frying_pan",
                s -> new FryingPan(MYSTIA, 0.0, s),
                BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.METAL)
        );
        MYSTIA_CUTTING_BOARD = RDBlocks.registerSimpleBlock(registrar, "mystia_cutting_board",
                s -> new CuttingBoard(MYSTIA, 0.0, s),
                BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.WOOD)
        );
        MYSTIA_COOKING_POT = RDBlocks.registerSimpleBlock(registrar, "mystia_cooking_pot",
                s -> new CookingPot(MYSTIA, 0.0, s),
                BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.STONE)
        );
        SUPER_STEAMER = RDBlocks.registerSimpleBlock(registrar, "super_steamer",
                s -> new Steamer(SUPER, 0.0, s),
                BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.STONE)
        );
        SUPER_GRILL = RDBlocks.registerSimpleBlock(registrar, "super_grill",
                s -> new Grill(SUPER, 0.0, s),
                BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.METAL)
        );
        SUPER_FRYING_PAN = RDBlocks.registerSimpleBlock(registrar, "super_frying_pan",
                s -> new FryingPan(SUPER, 0.0, s),
                BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.METAL)
        );
        SUPER_CUTTING_BOARD = RDBlocks.registerSimpleBlock(registrar, "super_cutting_board",
                s -> new CuttingBoard(SUPER, 0.0, s),
                BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.WOOD)
        );
        SUPER_COOKING_POT = RDBlocks.registerSimpleBlock(registrar, "super_cooking_pot",
                s -> new CookingPot(SUPER, 0.0, s),
                BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.STONE)
        );

        EXTREME_STEAMER = RDBlocks.registerSimpleBlock(registrar, "extreme_steamer",
                settings -> new Steamer(EXTREME, 0.0, settings),
                BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.STONE)
        );
        EXTREME_GRILL = RDBlocks.registerSimpleBlock(registrar, "extreme_grill",
                settings -> new Grill(EXTREME, 0.0, settings),
                BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.METAL)
        );
        EXTREME_FRYING_PAN = RDBlocks.registerSimpleBlock(registrar, "extreme_frying_pan",
                settings -> new FryingPan(EXTREME, 0.0, settings),
                BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.METAL)
        );
        EXTREME_CUTTING_BOARD = RDBlocks.registerSimpleBlock(registrar, "extreme_cutting_board",
                settings -> new CuttingBoard(EXTREME, 0.0, settings),
                BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.WOOD)
        );

        EXTREME_COOKING_POT = RDBlocks.registerSimpleBlock(registrar, "extreme_cooking_pot",
                settings -> new CookingPot(EXTREME, 0.0, settings),
                BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.STONE)
        );

        NUKE_STEAMER = RDBlocks.registerSimpleBlock(registrar, "nuke_steamer",
                settings -> new Steamer(NUKE, 0.4, settings),
                BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.STONE)
        );
        NUKE_GRILL = RDBlocks.registerSimpleBlock(registrar,
                "nuke_grill",
                settings -> new Grill(NUKE, 0.4, settings),
                BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.METAL)
        );
        NUKE_FRYING_PAN = RDBlocks.registerSimpleBlock(registrar,
                "nuke_frying_pan",
                settings -> new FryingPan(NUKE, 0.4, settings),
                BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.METAL)
        );
        NUKE_CUTTING_BOARD = RDBlocks.registerSimpleBlock(registrar,
                "nuke_cutting_board",
                settings -> new CuttingBoard(NUKE, 0.4, settings),
                BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.WOOD)
        );
        NUKE_COOKING_POT = RDBlocks.registerSimpleBlock(registrar,
                "nuke_cooking_pot",
                settings -> new CookingPot(NUKE, 0.4, settings),
                BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.STONE)
        );
        KitchenBlockType.initialize();
    }
}
