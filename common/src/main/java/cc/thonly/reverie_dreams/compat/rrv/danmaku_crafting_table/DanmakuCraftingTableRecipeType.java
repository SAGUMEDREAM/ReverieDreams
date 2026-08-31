package cc.thonly.reverie_dreams.compat.rrv.danmaku_crafting_table;

import cc.cassian.rrv.api.recipe.ReliableClientRecipeType;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu;
import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class DanmakuCraftingTableRecipeType implements ReliableClientRecipeType {
    public static final Identifier TYPE_ID = ReverieDreams.id("danmaku_crafting_table");

    @Override
    public Component getDisplayName() {
        return RDBlocks.DANMAKU_CRAFTING_TABLE.asBlock().getName();
    }

    @Override
    public int getDisplayWidth() {
        return 9 * 18;
    }

    @Override
    public int getDisplayHeight() {
        return 3 * 18;
    }

    @Override
    public @Nullable Identifier getGuiTexture() {
        return null;
    }

    @Override
    public int getSlotCount() {
        return 7;
    }

    @Override
    public void placeSlots(RecipeViewMenu.SlotDefinition definition) {
        definition.addItemSlot(0, 0, 0);
        definition.addItemSlot(1, 36, 0);
        definition.addItemSlot(2, 72, 0);
        definition.addItemSlot(3, 108, 0);
        definition.addItemSlot(4, 144, 0);
        definition.addItemSlot(5, 72, 36);
        definition.addItemSlot(6, 72, 18);
    }

    @Override
    public Identifier getId() {
        return TYPE_ID;
    }

    @Override
    public ItemStack getIcon() {
        return RDBlocks.DANMAKU_CRAFTING_TABLE.createStack();
    }
}
