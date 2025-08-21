package cc.thonly.reverie_dreams.danmaku;

import cc.thonly.reverie_dreams.component.ModDataComponentTypes;
import cc.thonly.reverie_dreams.item.ModItems;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.component.ComponentChanges;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

public class SpellCardTemplates {
    private static final Map<Identifier, DanmakuTrajectory> TEMPLATES = new Object2ObjectLinkedOpenHashMap<>();
    private static final Map<Identifier, ItemStack> TEMPLATE_ITEM_STACKS = new Object2ObjectLinkedOpenHashMap<>();
    static {
        var simple = registerTemplateItem(DanmakuTrajectories.SINGLE);
        var triple = registerTemplateItem(DanmakuTrajectories.TRIPLE);
        var bullet = registerTemplateItem(DanmakuTrajectories.BULLET);
        var star = registerTemplateItem(DanmakuTrajectories.STAR);
        var heart = registerTemplateItem(DanmakuTrajectories.HEART);
        var x = registerTemplateItem(DanmakuTrajectories.X);
        var round = registerTemplateItem(DanmakuTrajectories.ROUND);
        var ring = registerTemplateItem(DanmakuTrajectories.RING);

    }

    public static void init() {

    }

    public static DanmakuTrajectory registerTemplateItem(DanmakuTrajectory entry) {
        Identifier id = entry.getId();
        assert id != null;
        return registerTemplateItem(id, entry);
    }

    public static DanmakuTrajectory registerTemplateItem(Identifier key, DanmakuTrajectory value) {
        assert key != null;
        TEMPLATES.put(key, value);
        TEMPLATE_ITEM_STACKS.put(key, createItemStack(key));
        return value;
    }

    public static ItemStack createItemStack(Identifier key) {
        RegistryEntry<Item> entry = Registries.ITEM.getEntry(ModItems.SPELL_CARD_TEMPLATE);
        return new ItemStack(entry, 1, ComponentChanges.builder().add(ModDataComponentTypes.Danmaku.TEMPLATE, key.toString()).build());
    }

    public static Map<Identifier, DanmakuTrajectory> getRegistryView() {
        return new LinkedHashMap<>(TEMPLATES);
    }

    public static Map<Identifier, ItemStack> getRegistryItemStackView() {
        return new LinkedHashMap<>(TEMPLATE_ITEM_STACKS);
    }
}
