package cc.thonly.reverie_dreams.registry.tag;

import cc.thonly.reverie_dreams.ReverieDreams;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class RDBlockTags {
    public static final TagKey<Block> MIN_TOOL = of("min_tool");
    public static final TagKey<Block> EMPTY = of("empty");
    public static final TagKey<Block> FUMO = of("fumo");
    public static final TagKey<Block> SILVER = of("silver");
    public static final TagKey<Block> TRUFFLE_DROPABLE = of("truffle_dropable");
    public static final TagKey<Block> KITCHENWARE = of("kitchenware");
    public static final TagKey<Block> COOKING_TOP = of("cooking_top");
    public static final TagKey<Block> CUTTING_BOARD = of("cutting_board");
    public static final TagKey<Block> FRYING_PAN = of("frying_pan");
    public static final TagKey<Block> GRILL = of("grill");
    public static final TagKey<Block> STEAMER = of("steamer");

    private static TagKey<Block> of(String id) {
        return TagKey.create(Registries.BLOCK, ReverieDreams.id(id));
    }

    public static void register() {

    }
}
