package cc.thonly.reverie_dreams.danmaku;

import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.component.ModDataComponentTypes;
import cc.thonly.reverie_dreams.item.ModItems;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class SpellCardTemplates {
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
        ResourceLocation id = RegistryManager.DANMAKU_TRAJECTORY.getKey(entry);
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
        Holder<Item> entry = BuiltInRegistries.ITEM.wrapAsHolder(ModItems.SPELL_CARD_TEMPLATE);
        return new ItemStack(entry, 1, DataComponentPatch.builder().set(ModDataComponentTypes.DANMAKU_PROPERTIES, DanmakuProperties.ofDefault().withTemplateId(key)).build());
    }

    public static Map<ResourceLocation, DanmakuTrajectory> getRegistryView() {
        return new LinkedHashMap<>(TEMPLATES);
    }

    public static Map<ResourceLocation, ItemStack> getRegistryItemStackView() {
        return new LinkedHashMap<>(TEMPLATE_ITEM_STACKS);
    }
}
