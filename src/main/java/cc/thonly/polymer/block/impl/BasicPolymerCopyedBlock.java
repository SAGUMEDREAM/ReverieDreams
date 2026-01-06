package cc.thonly.polymer.block.impl;

import cc.thonly.reverie_dreams.util.IdentifierGetter;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import xyz.nucleoid.packettweaker.PacketContext;

@Setter
@Getter
@ToString
public class BasicPolymerCopyedBlock extends Block implements PolymerBlock, PolymerTexturedBlock {
    Block targetblock;
    BlockState polymerBlockState;

    public BasicPolymerCopyedBlock(Block targetblock, Properties settings) {
        super(settings);
        this.targetblock = targetblock;
        this.polymerBlockState = targetblock.defaultBlockState();
    }

    @Override
    public BlockState getPolymerBlockState(BlockState blockState, PacketContext packetContext) {
        return this.polymerBlockState;
    }
}
