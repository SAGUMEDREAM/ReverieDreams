package cc.thonly.reverie_dreams.inventory.storage;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import java.util.List;

/** Classification decides which items share a container; merging always compares all components. */
public record StorageRule(Mode mode, List<Sample> samples) {
    public enum Mode { AUTO, ITEM, CATEGORY, INPUT, OVERFLOW, CONFLICT }
    public record Sample(ItemStack item, int rotation) {
        public Sample { item = item.copyWithCount(1); }
        public Mode mode() { return modeForRotation(rotation); }
    }

    public StorageRule { samples = List.copyOf(samples); }

    public static Mode modeForRotation(int rotation) {
        return switch (Math.floorMod(rotation, 8) / 2) {
            case 0 -> Mode.ITEM;
            case 1 -> Mode.CATEGORY;
            case 2 -> Mode.INPUT;
            default -> Mode.OVERFLOW;
        };
    }

    public static StorageRule of(List<Sample> frames) {
        var nonempty = frames.stream().filter(s -> !s.item.isEmpty()).toList();
        if (nonempty.isEmpty()) return new StorageRule(Mode.AUTO, List.of());
        Mode mode = nonempty.getFirst().mode();
        boolean conflict = nonempty.stream().anyMatch(s -> s.mode() != mode);
        return new StorageRule(conflict ? Mode.CONFLICT : mode, nonempty);
    }

    public boolean accepts(ItemStack stack, Container container, StorageSettings settings) {
        if (stack.isEmpty() || mode == Mode.INPUT || mode == Mode.CONFLICT) return false;
        if (mode == Mode.OVERFLOW) return true;
        if (mode == Mode.ITEM || mode == Mode.CATEGORY) {
            return samples.stream().anyMatch(s -> matches(s.item, stack, settings));
        }
        boolean empty = true;
        for (int i = 0; i < container.getContainerSize(); i++) {
            var existing = container.getItem(i);
            if (existing.isEmpty()) continue;
            empty = false;
            if (!ItemStack.isSameItemSameComponents(existing, stack)) return false;
        }
        return !empty || settings.claimEmpty();
    }

    private boolean matches(ItemStack sample, ItemStack stack, StorageSettings settings) {
        if (mode != Mode.CATEGORY) return sample.is(stack.getItem());
        // The first matching allowlisted tag wins. Never infer categories from arbitrary shared tags.
        for (String name : settings.categoryTags()) {
            Identifier id = Identifier.tryParse(name);
            if (id == null) continue;
            TagKey<Item> tag = TagKey.create(Registries.ITEM, id);
            if (sample.is(tag)) return stack.is(tag);
        }
        return sample.is(stack.getItem());
    }

    public int priority(Container container, ItemStack stack) {
        if (mode == Mode.OVERFLOW) return 4;
        if (StorageTransfer.hasMergeSpace(container, stack)) return 0;
        if (mode == Mode.ITEM || mode == Mode.CATEGORY) return 1;
        return container.isEmpty() ? 3 : 2;
    }
}
