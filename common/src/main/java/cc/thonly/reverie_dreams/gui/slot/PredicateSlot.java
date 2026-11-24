package cc.thonly.reverie_dreams.gui.slot;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.function.Function;

public class PredicateSlot extends Slot {
    private final Function<ItemStack, Boolean> predicate;

    public PredicateSlot(Container inventory, int index, int x, int y, Function<ItemStack, Boolean> predicate) {
        super(inventory, index, x, y);
        this.predicate = predicate;
    }

    @Override
    public boolean allowModification(Player player) {
        // 用当前槽内物品判定：谓词是「这个槽该装什么」，用 ItemStack.EMPTY 判定会恒为 false，
        // 导致放进去的物品永远拿不出来（参考 NPCGui 的谓词忽略 stack，之前恰好没暴露该问题）。
        return super.allowModification(player) && this.predicate.apply(this.getItem());
    }

    @Override
    public boolean mayPickup(Player player) {
        return super.mayPickup(player) && this.predicate.apply(this.getItem());
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return this.predicate.apply(stack);
    }
}
