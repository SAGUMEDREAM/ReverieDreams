package cc.thonly.polymer.block.impl;

import eu.pb4.polymer.blocks.api.BlockModelType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class BasicPolymerLeavesBlock extends BasicPolymerBlock {
    public BasicPolymerLeavesBlock(Properties properties) {
        super(BlockModelType.BIOME_TRANSPARENT_BLOCK, properties);
    }

}
