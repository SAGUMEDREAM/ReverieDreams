package cc.thonly.reverie_dreams.inventory;

import cc.thonly.reverie_dreams.ReverieDreams;

import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Meal visuals may occupy empty hands, but never consume or relocate the NPC's equipment. */
public final class CustomerMealDisplay {
    private static final String DISPLAY_MARKER = ReverieDreams.MOD_ID + ":customer_meal_display";
    private ItemStack mainDisplay = ItemStack.EMPTY;
    private ItemStack offDisplay = ItemStack.EMPTY;
    private ItemStack mainMeal = ItemStack.EMPTY;
    private ItemStack offMeal = ItemStack.EMPTY;

    public void show(Container inventory, InteractionHand hand, ItemStack meal) {
        int slot = slot(hand);
        this.clearHand(inventory, hand);
        ItemStack visual = meal.copyWithCount(1);
        if (hand == InteractionHand.MAIN_HAND) this.mainMeal = visual;
        else this.offMeal = visual;
        if (!visual.isEmpty() && inventory.getItem(slot).isEmpty()) {
            ItemStack display = visual.copy();
            CustomData.update(DataComponents.CUSTOM_DATA, display, tag -> tag.putBoolean(DISPLAY_MARKER, true));
            inventory.setItem(slot, display);
            if (hand == InteractionHand.MAIN_HAND) this.mainDisplay = display.copy();
            else this.offDisplay = display.copy();
        }
    }

    public ItemStack meal(InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? this.mainMeal : this.offMeal;
    }

    public void clear(Container inventory) {
        this.clearHand(inventory, InteractionHand.MAIN_HAND);
        this.clearHand(inventory, InteractionHand.OFF_HAND);
        this.mainMeal = ItemStack.EMPTY;
        this.offMeal = ItemStack.EMPTY;
    }

    private void clearHand(Container inventory, InteractionHand hand) {
        ItemStack displayed = hand == InteractionHand.MAIN_HAND ? this.mainDisplay : this.offDisplay;
        if (displayed.isEmpty()) return;
        ItemStack current = inventory.getItem(slot(hand));
        boolean marked = displayed.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                .copyTag().getBooleanOr(DISPLAY_MARKER, false);
        if (!displayed.isEmpty() && (ItemStack.matches(current, displayed)
                || (marked && ItemStack.isSameItemSameComponents(current, displayed)))) {
            inventory.setItem(slot(hand), ItemStack.EMPTY);
        }
        if (hand == InteractionHand.MAIN_HAND) this.mainDisplay = ItemStack.EMPTY;
        else this.offDisplay = ItemStack.EMPTY;
    }

    public void load(ValueInput input) {
        this.mainDisplay = read(input, "Customer.DisplayMain");
        this.offDisplay = read(input, "Customer.DisplayOff");
        this.mainMeal = read(input, "Customer.MealMain");
        this.offMeal = read(input, "Customer.MealOff");
    }

    /** Older eating saves used exactly one food/drink in each hand as their visual. */
    public void recoverLegacy(Container inventory, boolean mainFood, boolean offDrink) {
        if (mainFood && inventory.getItem(NPCInventoryImpl.MAIN_HAND).getCount() == 1) {
            this.mainMeal = inventory.getItem(NPCInventoryImpl.MAIN_HAND).copy();
            this.mainDisplay = this.mainMeal.copy();
        }
        if (offDrink && inventory.getItem(NPCInventoryImpl.OFF_HAND).getCount() == 1) {
            this.offMeal = inventory.getItem(NPCInventoryImpl.OFF_HAND).copy();
            this.offDisplay = this.offMeal.copy();
        }
    }

    public void save(ValueOutput output) {
        output.putInt("Customer.MealDisplayVersion", 1);
        output.store("Customer.DisplayMain", ItemStack.OPTIONAL_CODEC, this.mainDisplay);
        output.store("Customer.DisplayOff", ItemStack.OPTIONAL_CODEC, this.offDisplay);
        output.store("Customer.MealMain", ItemStack.OPTIONAL_CODEC, this.mainMeal);
        output.store("Customer.MealOff", ItemStack.OPTIONAL_CODEC, this.offMeal);
    }

    private static ItemStack read(ValueInput input, String key) {
        return input.read(key, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
    }

    private static int slot(InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? NPCInventoryImpl.MAIN_HAND : NPCInventoryImpl.OFF_HAND;
    }
}
