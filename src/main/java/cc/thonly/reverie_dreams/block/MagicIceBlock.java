package cc.thonly.reverie_dreams.block;

import cc.thonly.reverie_dreams.block.base.BasicPolymerBlock;
import cc.thonly.reverie_dreams.block.base.BasicPolymerCopyedBlock;
import eu.pb4.polymer.blocks.api.BlockModelType;
import net.minecraft.block.Block;

public class MagicIceBlock extends BasicPolymerBlock {
    public MagicIceBlock(String path, Settings settings) {
        super(path, BlockModelType.FULL_BLOCK, settings);
    }
}
