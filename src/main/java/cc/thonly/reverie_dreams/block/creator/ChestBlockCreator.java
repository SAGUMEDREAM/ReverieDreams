package cc.thonly.reverie_dreams.block.creator;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.CustomChestBlock;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import lombok.Getter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import java.util.stream.Stream;

@Getter
public class ChestBlockCreator extends AbstractBlockCreator {
    private final BlockBehaviour.Properties settings;
    private CustomChestBlock chestBlock;

    public ChestBlockCreator(String name, BlockBehaviour.Properties settings) {
        super(name, ReverieDreams.id(name));
        this.settings = settings;
    }

    public static ChestBlockCreator create(String name, BlockBehaviour.Properties settings) {
        return new ChestBlockCreator(name, settings);
    }

    public CustomChestBlock chestBlock() {
        return this.chestBlock;
    }

    @Override
    protected Stream<Block> stream() {
        return Stream.of(this.chestBlock());
    }

    @Override
    public ChestBlockCreator build() {
        Block block = RDBlocks.registerBlock(this.getName(), CustomChestBlock::new, this.settings.noOcclusion());
        this.chestBlock = (CustomChestBlock) block;
        return this;
    }
}
