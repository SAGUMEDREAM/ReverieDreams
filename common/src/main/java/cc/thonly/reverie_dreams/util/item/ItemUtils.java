package cc.thonly.reverie_dreams.util.item;

import cc.thonly.reverie_dreams.item.prop.FumoLicenseItem;
import cc.thonly.reverie_dreams.item.prop.SatoriEye;
import cc.thonly.reverie_dreams.mixin.accessor.ItemStackTemplateAccessor;
import cc.thonly.reverie_dreams.util.lang.UnsafeAccess;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMaps;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.function.Predicate;

@SuppressWarnings("deprecation")
@Slf4j
public class ItemUtils {
    private static Field _FIELD_ITEM = null;
    private static Field _FIELD_COUNT = null;
    private static Field _FIELD_COMPONENTS = null;

    public static ItemStackTemplate createUnsafeTemplate(Void nullMarker) {
        return createUnsafeTemplate(null, 1, DataComponentPatch.EMPTY);
    }

    public static ItemStackTemplate createUnsafeTemplate(Item item) {
        return createUnsafeTemplate(item.builtInRegistryHolder(), 1, DataComponentPatch.builder().build());
    }

    public static ItemStackTemplate createUnsafeTemplate(Item item, int count) {
        return createUnsafeTemplate(item.builtInRegistryHolder(), count, DataComponentPatch.builder().build());
    }

    public static ItemStackTemplate createUnsafeTemplate(Item item, DataComponentPatch components) {
        return createUnsafeTemplate(item.builtInRegistryHolder(), 1, components);
    }

    @SuppressWarnings("DataFlowIssue")
    public static ItemStackTemplate createUnsafeTemplate(Holder<Item> item, int count, DataComponentPatch components) {
        Unsafe unsafe = UnsafeAccess.get_unsafe();
        try {
            ItemStackTemplate template = (ItemStackTemplate) unsafe.allocateInstance(ItemStackTemplate.class);
            ItemStackTemplateAccessor accessor = (ItemStackTemplateAccessor) (Object) template;
            accessor.reverie_dreams$setItem(item);
            accessor.reverie_dreams$setCount(count);
            accessor.reverie_dreams$setComponents(components);
            return template;
        } catch (InstantiationException e) {
            log.error("Can't allocate instance", e);
            return null;
        }
    }

    public static boolean isArmorItem(ItemStack stack) {
        return stack.get(DataComponents.EQUIPPABLE) != null;
    }

    public static ItemStack getHandItem(Player player, Predicate<ItemStack> predicate) {
        ItemStack main = player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack off = player.getItemInHand(InteractionHand.OFF_HAND);
        if (!main.isEmpty() && predicate.test(main)) {
            return main;
        }
        if (!off.isEmpty() && predicate.test(off)) {
            return off;
        }
        return ItemStack.EMPTY;
    }

    public static boolean shouldPass(Player player, InteractionHand hand) {
        Level world = player.level();
        ItemStack itemStack = player.getItemInHand(hand);
        if (!world.isClientSide()) {
            if (itemStack.getItem() instanceof FumoLicenseItem) {
                return true;
            }
            if (itemStack.getItem() instanceof SatoriEye) {
                return true;
            }
            if (itemStack.getItem() == Items.BARREL) {
                return true;
            }
        }
        return false;
    }

    public static Field getItemFieldFromTemplate() {
        if (_FIELD_ITEM == null) {
            try {
                Field itemField = ItemStackTemplate.class.getDeclaredField("item");
                itemField.setAccessible(true);
                _FIELD_ITEM = itemField;
            } catch (Exception e) {
                log.error("Error: ", e);
            }
        }
        return _FIELD_ITEM;
    }

    public static Field getItemCountFromTemplate() {
        if (_FIELD_COUNT == null) {
            try {
                Field itemField = ItemStackTemplate.class.getDeclaredField("count");
                itemField.setAccessible(true);
                _FIELD_COUNT = itemField;
            } catch (Exception e) {
                log.error("Error: ", e);
            }
        }
        return _FIELD_COUNT;
    }

    public static Field getComponentsFieldFromTemplate() {
        if (_FIELD_COMPONENTS == null) {
            try {
                Field itemField = ItemStackTemplate.class.getDeclaredField("components");
                itemField.setAccessible(true);
                _FIELD_COMPONENTS = itemField;
            } catch (Exception e) {
                log.error("Error: ", e);
            }
        }
        return _FIELD_COMPONENTS;
    }
}
