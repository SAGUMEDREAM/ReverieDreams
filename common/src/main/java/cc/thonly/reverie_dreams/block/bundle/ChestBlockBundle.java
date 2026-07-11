package cc.thonly.reverie_dreams.block.bundle;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.CustomChestBlock;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.impl.BlockDelegate;
import lombok.Getter;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.Collection;
import java.util.List;

@Getter
public class ChestBlockBundle extends AbstractBlockBundle {
    private final BlockBehaviour.Properties settings;
    private BlockDelegate chestBlock;

    public ChestBlockBundle(String name, BlockBehaviour.Properties settings) {
        super(name, ReverieDreams.id(name));
        this.settings = settings;
    }

    public static ChestBlockBundle create(String name, BlockBehaviour.Properties settings) {
        return new ChestBlockBundle(name, settings);
    }

    public BlockDelegate chestBlock() {
        return this.chestBlock;
    }

    @Override
    protected Collection<BlockDelegate> stream() {
        return List.of(this.chestBlock());
    }

    @Override
    public ChestBlockBundle build() {
        this.chestBlock = RDBlocks.registerBlock(this.getName(), CustomChestBlock::new, this.settings.noOcclusion());
        return this;
    }
}
