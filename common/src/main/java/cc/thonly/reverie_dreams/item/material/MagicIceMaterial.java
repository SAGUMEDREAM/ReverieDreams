package cc.thonly.reverie_dreams.item.material;

import cc.thonly.reverie_dreams.registry.tag.RDItemTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ToolMaterial;

public interface MagicIceMaterial {
    ToolMaterial INSTANCE = new ToolMaterial(BlockTags.INCORRECT_FOR_IRON_TOOL, 190, 5.5f, 2.0f, 14, RDItemTags.MAGIC_ICE_TOOL_MATERIALS);

}
