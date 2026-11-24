package cc.thonly.reverie_dreams.paper.specialweapons;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;

/** Type-aware source magazine swap: old rounds return as a fresh magazine, the source stack is consumed once. */
public final class MagazineService {
    public static final NamespacedKey KEY = new NamespacedKey("reverie_dreams", "magazine_rounds");
    public enum Status { RELOADED, FULL, NO_MAGAZINE, NOT_WEAPON }
    public record Result(Status status,int previousRounds,int loadedRounds,int returnedRounds,int magazineSlot,int consumed){}

    public int rounds(ItemStack item) {
        SpecialWeaponsModule.thread();
        LunarWeaponType weapon = LunarWeaponType.weapon(item);
        LunarWeaponType magazine = LunarWeaponType.magazine(item);
        LunarWeaponType type = weapon != null ? weapon : magazine;
        if (type == null) return 0;
        Integer stored = item.getPersistentDataContainer().get(KEY, PersistentDataType.INTEGER);
        // Source magazines are created at full type capacity; guns start empty until loaded.
        int value = stored == null ? (magazine != null ? type.capacity() : 0) : stored;
        return Math.clamp(value, 0, type.capacity());
    }

    public void setRounds(ItemStack item,int rounds) {
        SpecialWeaponsModule.thread();
        LunarWeaponType type = LunarWeaponType.weapon(item);
        if (type == null) type = LunarWeaponType.magazine(item);
        if (type == null) return;
        int count = Math.clamp(rounds, 0, type.capacity());
        item.editPersistentDataContainer(data -> data.set(KEY, PersistentDataType.INTEGER, count));
        refreshTooltip(item, 0);
    }

    public void refreshTooltip(ItemStack item) {
        refreshTooltip(item, 0);
    }

    /** Mirrors the source three-line gun tooltip; the XP line is level-dependent, so callers pass the owner level. */
    public boolean refreshTooltip(ItemStack item,int experienceLevel) {
        LunarWeaponType weapon = LunarWeaponType.weapon(item);
        LunarWeaponType magazine = LunarWeaponType.magazine(item);
        if (weapon == null && magazine == null) return false;
        var lore = new ArrayList<Component>();
        if (item.lore() != null) for (var line : item.lore()) if (!generated(line)) lore.add(line);
        if (weapon != null) {
            int level = Math.clamp(experienceLevel, 0, 30);
            lore.add(line("item.tooltip.magazine.loaded", Component.text(rounds(item)), Component.text(weapon.capacity())));
            lore.add(line("item.tooltip.lunar.damage",
                    Component.text(String.format(Locale.ROOT, "%.2f", weapon.damageAtLevel(level))),
                    Component.text(weapon.pellets())));
            lore.add(line("item.tooltip.lunar.experience",
                    Component.text(String.format(Locale.ROOT, "%.1f%%", level * 25.0 / 30)), Component.text(level)));
        } else {
            lore.add(line("item.tooltip.magazine.rounds", Component.text(rounds(item)), Component.text(magazine.capacity())));
        }
        if (Objects.equals(lore, item.lore())) return false;
        item.lore(lore);
        return true;
    }

    private static Component line(String key,Component... args) {
        return Component.translatable(key, args).decoration(TextDecoration.ITALIC, false);
    }

    private static boolean generated(Component c) {
        if (c instanceof TranslatableComponent t && switch (t.key()) {
            case "item.tooltip.magazine.rounds", "item.tooltip.magazine.loaded",
                 "item.tooltip.lunar.damage", "item.tooltip.lunar.experience" -> true;
            default -> false;
        }) return true;
        return c.children().stream().anyMatch(MagazineService::generated);
    }

    private boolean loaded(ItemStack item,LunarWeaponType type) {
        return item != null && !item.isEmpty() && LunarWeaponType.magazine(item) == type && rounds(item) > 0;
    }

    public Result reload(Inventory inventory,int weaponSlot,int preferredMagazineSlot,boolean creative) {
        return reload(inventory, weaponSlot, preferredMagazineSlot, creative, 0, ignored -> { });
    }

    /**
     * Production inventory transaction also supports NPC/other native source containers, without constructing fake players.
     * The overflow consumer receives a returned magazine that no longer fits in the container.
     */
    public Result reload(Inventory inventory,int weaponSlot,int preferredMagazineSlot,boolean creative,int experienceLevel,Consumer<ItemStack> overflow) {
        SpecialWeaponsModule.thread();
        var weapon = inventory.getItem(weaponSlot);
        LunarWeaponType type = LunarWeaponType.weapon(weapon);
        if (type == null) return new Result(Status.NOT_WEAPON, 0, 0, 0, -1, 0);
        int previous = rounds(weapon);
        if (previous >= type.capacity()) return new Result(Status.FULL, previous, previous, 0, -1, 0);
        int slot = -1;
        if (preferredMagazineSlot >= 0 && preferredMagazineSlot < inventory.getSize() && loaded(inventory.getItem(preferredMagazineSlot), type)) {
            slot = preferredMagazineSlot;
        } else {
            for (int i = 0; i < inventory.getSize(); i++) if (loaded(inventory.getItem(i), type)) { slot = i; break; }
        }
        if (slot < 0) return new Result(Status.NO_MAGAZINE, previous, previous, 0, -1, 0);
        var magazine = inventory.getItem(slot);
        int loadedRounds = Math.min(rounds(magazine), type.capacity());
        int returnedRounds = creative ? 0 : Math.max(0, previous);
        var updated = weapon.clone();
        setRounds(updated, loadedRounds);
        refreshTooltip(updated, experienceLevel);
        if (!creative) {
            var remainder = magazine.clone();
            remainder.subtract(1);
            inventory.setItem(slot, remainder.isEmpty() ? null : remainder);
            if (returnedRounds > 0) {
                var returned = magazine.clone();
                returned.setAmount(1);
                setRounds(returned, returnedRounds);
                refreshTooltip(returned, experienceLevel);
                var leftovers = inventory.addItem(returned);
                for (var leftover : leftovers.values()) overflow.accept(leftover);
            }
        }
        inventory.setItem(weaponSlot, updated);
        return new Result(Status.RELOADED, previous, loadedRounds, returnedRounds, slot, creative ? 0 : 1);
    }
}
