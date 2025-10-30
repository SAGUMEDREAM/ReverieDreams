package cc.thonly.reverie_dreams.item.material;

import cc.thonly.reverie_dreams.data.ModTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ToolMaterial;

public interface DreamMaterial {
    ToolMaterial INSTANCE = new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 220, 5.5f, 3.5f, 14, ModTags.ItemTypeTag.DREAM_TOOL_MATERIALS);

}
