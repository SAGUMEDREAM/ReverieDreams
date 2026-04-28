package cc.thonly.reverie_dreams.block.bundle;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.CustomChestBlock;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import lombok.Getter;
import net.blay09.mods.balm.world.level.block.BalmBlockRegistrar;
import net.blay09.mods.balm.world.level.block.DeferredBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@Getter
public class ChestBlockBundle extends AbstractBlockBundle {
    private final BlockBehaviour.Properties settings;
    private DeferredBlock chestBlock;

    public ChestBlockBundle(String name, BlockBehaviour.Properties settings) {
        super(name, ReverieDreams.id(name));
        this.settings = settings;
    }

    public static ChestBlockBundle create(String name, BlockBehaviour.Properties settings) {
        return new ChestBlockBundle(name, settings);
    }

    public DeferredBlock chestBlock() {
        return this.chestBlock;
    }

    @Override
    protected Collection<DeferredBlock> stream() {
        return List.of(this.chestBlock());
    }

    @Override
    public ChestBlockBundle build(BalmBlockRegistrar registrar) {
        this.chestBlock = RDBlocks.registerBlock(registrar, this.getName(), CustomChestBlock::new, this.settings.noOcclusion());
        return this;
    }
}
