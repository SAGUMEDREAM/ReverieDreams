package cc.thonly.reverie_dreams.registry.content.block;

import cc.thonly.reverie_dreams.block.cooking.*;
import cc.thonly.reverie_dreams.block.KitchenBlockType;
import cc.thonly.reverie_dreams.registry.delegate.BlockDelegate;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.DoubleUnaryOperator;

public class RDKitchenBlocks {
    public static DoubleUnaryOperator MYSTIA = original -> original - original * 0.25;
    public static DoubleUnaryOperator NORMAL = original -> original;
    public static DoubleUnaryOperator SUPER = original -> original + original * 0.05;
    public static DoubleUnaryOperator EXTREME = original -> original + original * 0.1;
    public static DoubleUnaryOperator NUKE = original -> original + original * 0.5;

    // 夜雀
    public static final BlockDelegate MYSTIA_STEAMER = RDBlocks.registerSimpleBlock("mystia_steamer",
            s -> new Steamer(MYSTIA, 0.0, s),
            BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.STONE)
    );
    public static final BlockDelegate MYSTIA_GRILL = RDBlocks.registerSimpleBlock("mystia_grill",
            s -> new Grill(MYSTIA, 0.0, s),
            BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.METAL)
    );
    public static final BlockDelegate MYSTIA_FRYING_PAN = RDBlocks.registerSimpleBlock("mystia_frying_pan",
            s -> new FryingPan(MYSTIA, 0.0, s),
            BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.METAL)
    );
    public static final BlockDelegate MYSTIA_CUTTING_BOARD = RDBlocks.registerSimpleBlock("mystia_cutting_board",
            s -> new CuttingBoard(MYSTIA, 0.0, s),
            BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.WOOD)
    );
    public static final BlockDelegate MYSTIA_COOKING_POT = RDBlocks.registerSimpleBlock("mystia_cooking_pot",
            s -> new CookingPot(MYSTIA, 0.0, s),
            BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.STONE)
    );
    // 超
    public static final BlockDelegate SUPER_STEAMER = RDBlocks.registerSimpleBlock("super_steamer",
            s -> new Steamer(SUPER, 0.0, s),
            BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.STONE)
    );
    public static final BlockDelegate SUPER_GRILL = RDBlocks.registerSimpleBlock("super_grill",
            s -> new Grill(SUPER, 0.0, s),
            BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.METAL)
    );
    public static final BlockDelegate SUPER_FRYING_PAN = RDBlocks.registerSimpleBlock("super_frying_pan",
            s -> new FryingPan(SUPER, 0.0, s),
            BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.METAL)
    );
    public static final BlockDelegate SUPER_CUTTING_BOARD = RDBlocks.registerSimpleBlock("super_cutting_board",
            s -> new CuttingBoard(SUPER, 0.0, s),
            BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.WOOD)
    );
    public static final BlockDelegate SUPER_COOKING_POT = RDBlocks.registerSimpleBlock("super_cooking_pot",
            s -> new CookingPot(SUPER, 0.0, s),
            BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.STONE)
    );
    // 极
    public static final BlockDelegate EXTREME_STEAMER = RDBlocks.registerSimpleBlock("extreme_steamer",
            settings -> new Steamer(EXTREME, 0.0, settings),
            BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.STONE)
    );
    public static final BlockDelegate EXTREME_GRILL = RDBlocks.registerSimpleBlock("extreme_grill",
            settings -> new Grill(EXTREME, 0.0, settings),
            BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.METAL)
    );
    public static final BlockDelegate EXTREME_FRYING_PAN = RDBlocks.registerSimpleBlock("extreme_frying_pan",
            settings -> new FryingPan(EXTREME, 0.0, settings),
            BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.METAL)
    );
    public static final BlockDelegate EXTREME_CUTTING_BOARD = RDBlocks.registerSimpleBlock("extreme_cutting_board",
            settings -> new CuttingBoard(EXTREME, 0.0, settings),
            BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.WOOD)
    );
    public static final BlockDelegate EXTREME_COOKING_POT = RDBlocks.registerSimpleBlock("extreme_cooking_pot",
            settings -> new CookingPot(EXTREME, 0.0, settings),
            BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.STONE)
    );
    // 核能
    public static final BlockDelegate NUKE_STEAMER = RDBlocks.registerSimpleBlock("nuke_steamer",
            settings -> new Steamer(NUKE, 0.4, settings),
            BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.STONE)
    );
    public static final BlockDelegate NUKE_GRILL = RDBlocks.registerSimpleBlock(
            "nuke_grill",
            settings -> new Grill(NUKE, 0.4, settings),
            BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.METAL)
    );
    public static final BlockDelegate NUKE_FRYING_PAN = RDBlocks.registerSimpleBlock(
            "nuke_frying_pan",
            settings -> new FryingPan(NUKE, 0.4, settings),
            BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.METAL)
    );
    public static final BlockDelegate NUKE_CUTTING_BOARD = RDBlocks.registerSimpleBlock(
            "nuke_cutting_board",
            settings -> new CuttingBoard(NUKE, 0.4, settings),
            BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.WOOD)
    );
    public static final BlockDelegate NUKE_COOKING_POT = RDBlocks.registerSimpleBlock(
            "nuke_cooking_pot",
            settings -> new CookingPot(NUKE, 0.4, settings),
            BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.STONE)
    );
    // 普通
    public static final BlockDelegate COOKING_POT = RDBlocks.registerSimpleBlock("cooking_pot",
            s -> new CookingPot(NORMAL, 0.0, s),
            BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.STONE)
    );
    public static final BlockDelegate CUTTING_BOARD = RDBlocks.registerSimpleBlock("cutting_board",
            s -> new CuttingBoard(NORMAL, 0.0, s),
            BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.WOOD)
    );
    public static final BlockDelegate FRYING_PAN = RDBlocks.registerSimpleBlock("frying_pan",
            s -> new FryingPan(NORMAL, 0.0, s),
            BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.METAL)
    );
    public static final BlockDelegate GRILL = RDBlocks.registerSimpleBlock("grill",
            s -> new Grill(NORMAL, 0.0, s),
            BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.METAL)
    );
    public static final BlockDelegate STEAMER = RDBlocks.registerSimpleBlock("steamer",
            s -> new Steamer(NORMAL, 0.0, s),
            BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.STONE)
    );

    public static void initialize() {
        KitchenBlockType.initialize();
    }
}
