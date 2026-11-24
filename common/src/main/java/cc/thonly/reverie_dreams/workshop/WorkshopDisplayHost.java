package cc.thonly.reverie_dreams.workshop;

import cc.thonly.reverie_dreams.block.decor.workshop.WorkshopKind;
import net.minecraft.world.item.ItemStack;
import java.util.List;

public interface WorkshopDisplayHost {
    WorkshopKind workshopKind();
    List<ItemStack> displayItems();
    void setDisplayUpdate(Runnable callback);
    void clearDisplayUpdate(Runnable callback);
}
