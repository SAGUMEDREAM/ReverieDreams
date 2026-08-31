package cc.thonly.reverie_dreams.advancement;

import cc.thonly.reverie_dreams.ReverieDreams;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;

import java.util.Map;

@SuppressWarnings("deprecation")
public class AdvancementIcons {
    private static final Map<Identifier, ItemStackTemplate> ICONS = new Object2ObjectLinkedOpenHashMap<>();
    public static final ItemStackTemplate ASKING_FOR_MONEY = registerIcon("advancement_icon/asking_for_money");

    public static ItemStackTemplate registerIcon(String name) {
        return registerIcon(ReverieDreams.id(name));
    }

    public static ItemStackTemplate registerIcon(Identifier key) {
        ItemStackTemplate direct = getDirect(key);
        ItemStackTemplate prev = ICONS.put(key, direct);
        if (prev != null) {
            throw new RuntimeException("Duplicate registration icon %s detected".formatted(key));
        }
        return direct;
    }

    public static ItemStackTemplate getDirect(Identifier key) {
        DataComponentPatch dataComponentPatch = DataComponentPatch.builder().set(DataComponents.ITEM_MODEL, key).build();
        return new ItemStackTemplate(Items.PAPER.builtInRegistryHolder(), 1, dataComponentPatch);
    }

    public static Map<Identifier, ItemStackTemplate> getIcons() {
        return Map.copyOf(ICONS);
    }

}
