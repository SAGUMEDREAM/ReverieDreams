package cc.thonly.reverie_dreams.registry.content.danmaku;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuTrajectory;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

public class DanmakuTemplates {
    private static final Map<Identifier, DanmakuTrajectory> TEMPLATES = new Object2ObjectLinkedOpenHashMap<>();
    private static final Map<Identifier, ItemStackTemplate> TEMPLATE_ITEM_STACKS = new Object2ObjectLinkedOpenHashMap<>();
    static {
        ReverieDreams.COMMON_LATE_INIT.add(() -> {
            var simple = registerTemplateItem(DanmakuTrajectories.SINGLE);
            var triple = registerTemplateItem(DanmakuTrajectories.TRIPLE);
            var bullet = registerTemplateItem(DanmakuTrajectories.BULLET);
            var star = registerTemplateItem(DanmakuTrajectories.STAR);
            var heart = registerTemplateItem(DanmakuTrajectories.HEART);
            var x = registerTemplateItem(DanmakuTrajectories.X);
            var round = registerTemplateItem(DanmakuTrajectories.ROUND);
            var ring = registerTemplateItem(DanmakuTrajectories.RING);
        });
    }

    public static void initialize() {

    }

    public static DanmakuTrajectory registerTemplateItem(DanmakuTrajectory entry) {
        Identifier id = BuiltInRegistryProviders.DANMAKU_TRAJECTORY.getKey(entry);
        assert id != null;
        return registerTemplateItem(id, entry);
    }

    public static DanmakuTrajectory registerTemplateItem(Identifier key, DanmakuTrajectory entry) {
        assert key != null;
        TEMPLATES.put(key, entry);
        TEMPLATE_ITEM_STACKS.put(key, createItemStackTemplate(key));
        return entry;
    }

    public static ItemStackTemplate createItemStackTemplate(Identifier key) {
        Holder<Item> entry = BuiltInRegistries.ITEM.wrapAsHolder(RDItems.SPELL_CARD_TEMPLATE.asItem());
        return new ItemStackTemplate(entry, 1, DataComponentPatch.builder().set(RDDataComponentTypes.DANMAKU_PROPERTIES.value(), DanmakuProperties.ofDefault().withTemplateId(key)).build());
    }

    public static Map<Identifier, DanmakuTrajectory> getRegistryView() {
        return new LinkedHashMap<>(TEMPLATES);
    }

    public static Map<Identifier, ItemStackTemplate> getRegistryItemStackView() {
        return new LinkedHashMap<>(TEMPLATE_ITEM_STACKS);
    }
}
