package cc.thonly.reverie_dreams.util.item;

import net.minecraft.world.item.ItemStack;

public record CoinStack(
        int slot,
        ItemStack stack,
        int price
) {
}
