package cc.thonly.reverie_dreams.registry.content.danmaku;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuTrajectory;
import cc.thonly.reverie_dreams.data.danmaku.trajectory.PlayerTrajectoryType;
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
        ReverieDreams.COMMON_LATE_INIT.add(DanmakuTemplates::registerLegacyTemplates);
        ReverieDreams.COMMON_LATE_INIT.add(() -> {
            for (PlayerTrajectoryType type : PlayerTrajectoryType.values()) {
                registerTemplateItem(type.id(), BuiltInRegistryProviders.DANMAKU_TRAJECTORY.getValue(type.id()));
            }
        });
    }

    public static void initialize() {

    }

    @SuppressWarnings("deprecation")
    private static void registerLegacyTemplates() {
        registerTemplateItem(DanmakuTrajectories.SINGLE);
        registerTemplateItem(DanmakuTrajectories.TRIPLE);
        registerTemplateItem(DanmakuTrajectories.FAN);
        registerTemplateItem(DanmakuTrajectories.BULLET);
        registerTemplateItem(DanmakuTrajectories.STAR);
        registerTemplateItem(DanmakuTrajectories.HEART);
        registerTemplateItem(DanmakuTrajectories.X);
        registerTemplateItem(DanmakuTrajectories.ROUND);
        registerTemplateItem(DanmakuTrajectories.RING);
        registerTemplateItem(DanmakuTrajectories.RING_VIEW);
        registerTemplateItem(DanmakuTrajectories.RING_HORIZONTAL);
    }

    public static DanmakuTrajectory registerTemplateItem(DanmakuTrajectory entry) {
        Identifier id = BuiltInRegistryProviders.DANMAKU_TRAJECTORY.getKey(entry);
        assert id != null;
        return registerTemplateItem(id, entry);
    }

    public static DanmakuTrajectory registerTemplateItem(Identifier key, DanmakuTrajectory entry) {
        assert key != null;
        TEMPLATES.put(key, entry);
        TEMPLATE_ITEM_STACKS.put(key, createItemStackTemplate(key, entry));
        return entry;
    }

    public static ItemStackTemplate createItemStackTemplate(Identifier key) {
        return createItemStackTemplate(key, TEMPLATES.get(key));
    }

    public static ItemStackTemplate createItemStackTemplate(Identifier key, DanmakuTrajectory entry) {
        Holder<Item> itemHolder = BuiltInRegistries.ITEM.wrapAsHolder(RDItems.SPELL_CARD_TEMPLATE.asItem());
        DataComponentPatch.Builder builder = DataComponentPatch.builder()
                .set(RDDataComponentTypes.DANMAKU_PROPERTIES.value(),
                        DanmakuProperties.ofDefault().withTemplateId(key));
        if (entry != null) {
            builder.set(RDDataComponentTypes.DANMAKU_TRAJECTORY_CONFIG.value(), entry.defaultConfig());
        }
        return new ItemStackTemplate(itemHolder, 1, builder.build());
    }

    /** Snapshot of templates available to new players. Explicit ID lookup retains legacy templates. */
    public static Map<Identifier, DanmakuTrajectory> getRegistryView() {
        return getAvailableView(TEMPLATES);
    }

    /** Snapshot for loot, creative tabs, and recipe displays. */
    public static Map<Identifier, ItemStackTemplate> getRegistryItemStackView() {
        return getAvailableView(TEMPLATE_ITEM_STACKS);
    }

    private static <T> Map<Identifier, T> getAvailableView(Map<Identifier, T> templates) {
        Map<Identifier, T> view = new LinkedHashMap<>(templates);
        view.keySet().removeIf(DanmakuTrajectories::isLegacy);
        return view;
    }
}
