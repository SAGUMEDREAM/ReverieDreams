package cc.thonly.reverie_dreams.item.material;

import cc.thonly.reverie_dreams.data.ModTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ToolMaterial;

public interface SilverMaterial {
    ToolMaterial INSTANCE = new ToolMaterial(BlockTags.INCORRECT_FOR_IRON_TOOL, 250, 6.0f, 2.0f, 14, ModTags.ItemTypeTag.SILVER_TOOL_MATERIALS);

}
