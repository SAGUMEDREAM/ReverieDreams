package cc.thonly.reverie_dreams.entity.ai.work;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public final class WorkTestItems {
    private WorkTestItems() {}
    public static void bootstrap() {
        SharedConstants.tryDetectVersion(); Bootstrap.bootStrap();
        for (Item item : new Item[]{Items.WHEAT, Items.WHEAT_SEEDS, Items.CARROT, Items.POTATO, Items.BEETROOT_SEEDS,
                Items.DIAMOND, Items.STONE, Items.COAL, Items.BUCKET, Items.GLASS_BOTTLE, Items.HONEY_BOTTLE, Items.APPLE,
                Items.SHEARS, Items.IRON_SWORD, Items.IRON_HELMET, Items.IRON_INGOT}) {
            if (!item.builtInRegistryHolder().areComponentsBound()) item.builtInRegistryHolder().bindComponents(DataComponentMap.builder()
                    .set(DataComponents.MAX_STACK_SIZE, item == Items.SHEARS || item == Items.IRON_SWORD || item == Items.IRON_HELMET ? 1 : 64).build());
        }
    }
}
