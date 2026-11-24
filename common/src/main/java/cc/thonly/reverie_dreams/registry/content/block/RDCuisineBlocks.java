package cc.thonly.reverie_dreams.registry.content.block;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.registry.block.CuisineBlockBindings;
import cc.thonly.reverie_dreams.block.kitchen.CuisineBlock;
import cc.thonly.reverie_dreams.registry.MCBuiltInRegistries;
import cc.thonly.reverie_dreams.registry.content.item.RDCuisineItems;
import cc.thonly.reverie_dreams.registry.delegate.BlockDelegate;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class RDCuisineBlocks {
    private static boolean initialized;
    private RDCuisineBlocks() { }

    public static void initialize() {
        if (initialized) return;
        initialized = true;
        for (var item : RDCuisineItems.CUISINE_ITEMS) {
            String path = item.unwrapKey().orElseThrow().identifier().getPath();
            var key = ResourceKey.create(Registries.BLOCK, ReverieDreams.id(path));
            var block = BlockDelegate.of(MCBuiltInRegistries.BLOCK.register(path, () -> new CuisineBlock(
                    BlockBehaviour.Properties.of().setId(key).strength(0.1f).noCollision().noOcclusion().noLootTable().sound(SoundType.WOOL))));
            // Deliberately bypass registerSimpleBlock: that method creates a BlockItem.
            RDBlocks.HOLDERS.add(block);
            CuisineBlockBindings.bind(item, block);
        }
    }
}
