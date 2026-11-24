package cc.thonly.reverie_dreams.util.item;

import cc.thonly.reverie_dreams.ReverieDreams;

import cc.thonly.reverie_dreams.item.weapon.MagazineItem;
import cc.thonly.reverie_dreams.item.weapon.WeaponOfTheMoon;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.NumericTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

public final class PolymerMagazineData {
    private PolymerMagazineData() {
    }

    /**
     * Read-only client view of the server's encoded component; never restores gameplay state.
     */
    public static Integer encodedRounds(ItemStack stack) {
        var serialized = encodedStack(stack);
        if (serialized != null && serialized.getCompound("components").orElseGet(CompoundTag::new)
                .get(ReverieDreams.MOD_ID + ":magazine_rounds") instanceof NumericTag rounds) {
            int capacity = stack.getItem() instanceof WeaponOfTheMoon weapon ? weapon.capacity() : MagazineItem.capacity(stack);
            return Mth.clamp(rounds.intValue(), 0, capacity);
        }
        return null;
    }

    public static boolean hasGeneratedMagazineTooltip(ItemStack stack) {
        if (encodedStack(stack) == null)
            return false;
        var lore = stack.get(DataComponents.LORE);
        return lore != null && lore.lines().stream().anyMatch(PolymerMagazineData::isMagazineLine);
    }

    private static boolean isMagazineLine(Component line) {
        if (line.getContents() instanceof TranslatableContents translation
                && (translation.getKey().equals("item.tooltip.magazine.loaded")
                || translation.getKey().equals("item.tooltip.magazine.rounds")))
            return true;
        return line.getSiblings().stream().anyMatch(PolymerMagazineData::isMagazineLine);
    }

    private static CompoundTag encodedStack(ItemStack stack) {
        var data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null)
            return null;
        // Polymer stores a regular ItemStack CODEC payload in vanilla custom_data.
        // Reading it keeps this client compatible without depending on the server addon.
        var encoded = data.copyTag().getCompound("$polymer:stack").orElse(null);
        return encoded != null && encoded.getString("id").isPresent() ? encoded : null;
    }
}
