package cc.thonly.reverie_dreams.registry.content.danmaku;

import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuTrajectory;
import cc.thonly.reverie_dreams.registry.RegistryHandlers;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;

public class DanmakuTemplates {
    private static final Map<ResourceLocation, DanmakuTrajectory> TEMPLATES = new Object2ObjectLinkedOpenHashMap<>();
    private static final Map<ResourceLocation, ItemStack> TEMPLATE_ITEM_STACKS = new Object2ObjectLinkedOpenHashMap<>();
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
        ResourceLocation id = RegistryHandlers.DANMAKU_TRAJECTORY.getKey(entry);
        assert id != null;
        return registerTemplateItem(id, entry);
    }

    public static DanmakuTrajectory registerTemplateItem(ResourceLocation key, DanmakuTrajectory entry) {
        assert key != null;
        TEMPLATES.put(key, entry);
        TEMPLATE_ITEM_STACKS.put(key, createItemStack(key));
        return entry;
    }

    public static ItemStack createItemStack(ResourceLocation key) {
        Holder<Item> entry = BuiltInRegistries.ITEM.wrapAsHolder(RDItems.SPELL_CARD_TEMPLATE);
        return new ItemStack(entry, 1, DataComponentPatch.builder().set(RDDataComponents.DANMAKU_PROPERTIES, DanmakuProperties.ofDefault().withTemplateId(key)).build());
    }

    public static Map<ResourceLocation, DanmakuTrajectory> getRegistryView() {
        return new LinkedHashMap<>(TEMPLATES);
    }

    public static Map<ResourceLocation, ItemStack> getRegistryItemStackView() {
        return new LinkedHashMap<>(TEMPLATE_ITEM_STACKS);
    }
}
