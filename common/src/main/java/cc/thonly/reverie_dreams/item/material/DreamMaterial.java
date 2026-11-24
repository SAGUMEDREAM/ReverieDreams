package cc.thonly.reverie_dreams.item.material;

import cc.thonly.reverie_dreams.registry.tag.RDItemTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ToolMaterial;

public interface DreamMaterial {
    ToolMaterial INSTANCE = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 1400, 8.5f, 4.0f, 16, RDItemTags.DREAM_TOOL_MATERIALS);

}
