package cc.thonly.reverie_dreams.block;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.block.entity.ModBlockEntities;
import lombok.Getter;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import java.util.stream.Stream;

@Getter
public class ChestBlockCreator extends AbstractBlockCreator {
    private final AbstractBlock.Settings settings;
    private CustomChestBlock chestBlock;

    public ChestBlockCreator(String name, AbstractBlock.Settings settings) {
        super(name, Touhou.id(name));
        this.settings = settings;
    }

    public static ChestBlockCreator create(String name, AbstractBlock.Settings settings) {
        return new ChestBlockCreator(name, settings);
    }

    public CustomChestBlock chestBlock() {
        return this.chestBlock;
    }

    @Override
    protected Stream<Block> stream() {
        return Stream.of(this.chestBlock());
    }

    public ChestBlockCreator build() {
        Block block = ModBlocks.registerBlock(this.getName(), CustomChestBlock::new, this.settings.nonOpaque());
        this.chestBlock = (CustomChestBlock) block;
        return this;
    }
}
