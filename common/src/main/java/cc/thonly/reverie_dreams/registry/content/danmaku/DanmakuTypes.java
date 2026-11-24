package cc.thonly.reverie_dreams.registry.content.danmaku;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuType;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.content.ItemColor;
import cc.thonly.reverie_dreams.registry.content.RDDamageTypes;
import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import com.google.common.collect.ImmutableList;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Tuple;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

import java.util.List;

@SuppressWarnings("deprecation")
public class DanmakuTypes {
    // Combat roles are intentionally distinct: normal types are sidegrades,
    // while bubble/bullet/note/glowy trade damage, speed, hitbox and uptime.
    public static final DanmakuType AMULET = registerType(ReverieDreams.id("amulet"), RDDamageTypes.DANMAKU_GENERIC, 2f, 1f, 1.2f, false, false, 120, 10);
    public static final DanmakuType ARROWHEAD = registerType(ReverieDreams.id("arrowhead"), RDDamageTypes.DANMAKU_REAL, 2f, 1f, 1.4f, false, false, 110, 9);
    public static final DanmakuType BALL = registerType(ReverieDreams.id("ball"), RDDamageTypes.DANMAKU_GENERIC, 2f, 1f, 1.2f, true, false, 120, 10);
    public static final DanmakuType BUBBLE = registerType(ReverieDreams.id("bubble"), RDDamageTypes.DANMAKU_GENERIC, 2f, 2f, 1.0f, true, false, 80, 14);
    public static final DanmakuType BULLET = registerType(ReverieDreams.id("bullet"), RDDamageTypes.DANMAKU_REAL, 3f, 1f, 1.1f, false, false, 100, 12);
    public static final DanmakuType FIREBALL = registerType(ReverieDreams.id("fireball"), RDDamageTypes.DANMAKU_GENERIC, 2.25f, 1.1f, 1.1f, true, false, 110, 11);
    public static final DanmakuType FIREBALL_GLOWY = registerType(ReverieDreams.id("fireball_glowy"), RDDamageTypes.DANMAKU_GENERIC, 1.5f, 0.9f, 1.6f, true, false, 140, 8);
    public static final DanmakuType KUNAI = registerType(ReverieDreams.id("kunai"), RDDamageTypes.DANMAKU_REAL, 2.5f, 0.9f, 1.1f, false, false, 100, 11);
    public static final DanmakuType RICE = registerType(ReverieDreams.id("rice"), RDDamageTypes.DANMAKU_GENERIC, 1.75f, 0.9f, 1.3f, false, false, 140, 8);
    public static final DanmakuType STAR = registerType(ReverieDreams.id("star"), RDDamageTypes.DANMAKU_GENERIC, 2f, 1f, 1.2f, true, false, 120, 10);
    public static final DanmakuType NOTE = registerType(ReverieDreams.id("note"), RDDamageTypes.DANMAKU_GENERIC, 1.75f, 0.8f, 1.7f, false, false, 140, 8);
    public static final DanmakuType LEAF = registerType(ReverieDreams.id("leaf"), RDDamageTypes.DANMAKU_GENERIC, 1.75f, 0.92f, 1.25f, true, false, 140, 8, 0.68f);
    public static final DanmakuType BUTTERFLY = registerType(ReverieDreams.id("butterfly"), RDDamageTypes.DANMAKU_GENERIC, 2f, 1.35f, 1.0f, true, false, 110, 10, 0.78f);
    public static final DanmakuType ICICLE = registerType(ReverieDreams.id("icicle"), RDDamageTypes.DANMAKU_REAL, 2.5f, 1.04f, 1.4f, false, false, 100, 11, 0.70f);
    public static final DanmakuType SAKURA_PETAL = registerType(ReverieDreams.id("sakura_petal"), RDDamageTypes.DANMAKU_GENERIC, 1.5f, 0.80f, 1.05f, true, false, 150, 8, 0.62f);
    public static final DanmakuType THROWING_KNIFE = registerType(ReverieDreams.id("throwing_knife"), RDDamageTypes.DANMAKU_REAL, 3f, 1.12f, 1.5f, false, false, 85, 13, 0.72f);
    public static final DanmakuType PELLET = registerType(ReverieDreams.id("pellet"), RDDamageTypes.DANMAKU_GENERIC, 1.5f, 0.7f, 1.6f, false, false, 140, 8, 0.55f);
    public static final DanmakuType NEEDLE = registerType(ReverieDreams.id("needle"), RDDamageTypes.DANMAKU_REAL, 1.75f, 1f, 1.5f, false, false, 120, 9, 0.55f);
    public static final DanmakuType WATER_DROP = registerType(ReverieDreams.id("water_drop"), RDDamageTypes.DANMAKU_GENERIC, 1.75f, 1f, 1.3f, false, false, 130, 9, 0.8f);
    public static final DanmakuType YIN_YANG = registerType(ReverieDreams.id("yin_yang"), RDDamageTypes.DANMAKU_GENERIC, 2f, 1.2f, 1.1f, true, false, 100, 12, 1f);
    public static final DanmakuType BACILLUS = registerType(ReverieDreams.id("bacillus"), RDDamageTypes.DANMAKU_GENERIC, 1.75f, 1f, 1.2f, false, false, 130, 9, 0.7f);
    public static final DanmakuType HEART = registerType(ReverieDreams.id("heart"), RDDamageTypes.DANMAKU_GENERIC, 1.75f, 1.1f, 1f, false, false, 120, 10, 0.9f);
    public static final DanmakuType ARROW = registerType(ReverieDreams.id("arrow"), RDDamageTypes.DANMAKU_REAL, 2f, 1f, 1.4f, false, false, 110, 9, 0.72f);
    public static final DanmakuType OVAL = registerType(ReverieDreams.id("oval"), RDDamageTypes.DANMAKU_GENERIC, 2f, 1f, 1.2f, false, false, 130, 9, 0.66f);
    public static final DanmakuType MEDIUM_ORB = registerType(ReverieDreams.id("medium_orb"), RDDamageTypes.DANMAKU_GENERIC, 2f, 1f, 1.2f, true, false, 120, 10, 1f);
    public static final DanmakuType HIGHLIGHT_ORB = registerType(ReverieDreams.id("highlight_orb"), RDDamageTypes.DANMAKU_GENERIC, 2f, 1f, 1.2f, true, false, 120, 10, 1f);
    public static final DanmakuType LIGHT_ORB = registerType(ReverieDreams.id("light_orb"), RDDamageTypes.DANMAKU_GENERIC, 1.5f, 0.9f, 1.6f, true, false, 140, 8, 0.8f);
    public static final DanmakuType LASER = registerType(ReverieDreams.id("laser"), RDDamageTypes.DANMAKU_GENERIC, 4f, 1.5f, 1.2f, false, false, 120, 10).unlist();
    public static final DanmakuType BIG_LASER = registerType(ReverieDreams.id("big_laser"), RDDamageTypes.DANMAKU_GENERIC, 4f, 1.5f, 1.2f, false, false, 120, 10).unlist();
    public static final DanmakuType SHORT_LASER = registerType(ReverieDreams.id("short_laser"), RDDamageTypes.DANMAKU_GENERIC, 1.75f, 1f, 1f, false, false, 120, 10).unlist();
    public static final DanmakuType LUNATIC_MOON = registerType(ReverieDreams.id("lunatic_moon"), RDDamageTypes.DANMAKU_GENERIC, 2f, 2f, 1f, true, false, 80, 14, 1.35f).unlist();
    /**
     * 使魔本体：不是弹药（物品路径不可用，伤害 0），只作为使魔实体的默认外观与子图案弹型的兜底。
     */
    public static final DanmakuType FAMILIAR = registerType(ReverieDreams.id("familiar"), RDDamageTypes.DANMAKU_GENERIC, 0f, 0.8f, 0f, true, false, 100, 20).unlist();

    /** Backward-compatible overload for addons using the old seven-argument form. */
    public static DanmakuType registerType(Identifier key, ResourceKey<DamageType> damageTypeKey, float damage, float scale,
                                           float speed, boolean tile, boolean infinite) {
        return registerType(key, damageTypeKey, damage, scale, speed, tile, infinite, 120, 10, scale);
    }

    public static DanmakuType registerType(Identifier key, ResourceKey<DamageType> damageTypeKey, float damage, float scale,
                                           float speed, boolean tile, boolean infinite, int durability, int cooldownTicks) {
        return registerType(key, damageTypeKey, damage, scale, speed, tile, infinite, durability, cooldownTicks, scale);
    }

    public static DanmakuType registerType(Identifier key, ResourceKey<DamageType> damageTypeKey, float damage, float scale,
                                           float speed, boolean tile, boolean infinite, int durability, int cooldownTicks,
                                           float hitboxScale) {
        // speed 统一为实际格/tick；新内置类型直接标记为新语义
        return BuiltInRegistryProviders.registerForBuiltin(
                BuiltInRegistryProviders.DANMAKU_TYPE,
                key,
                new DanmakuType(key, damageTypeKey, damage, scale, speed, tile, infinite,
                        DanmakuProperties.CURRENT_SPEED_VERSION, durability, cooldownTicks, hitboxScale)
        );
    }

    public static DanmakuType fromItem(ItemStack itemStack) {
        Item item = itemStack.getItem();
        for (DanmakuType type : BuiltInRegistryProviders.DANMAKU_TYPE.values()) {
            if (type.getItemHolder().is(item.builtInRegistryHolder())) {
                return type;
            }
        }
        return null;
    }

    @SuppressWarnings("DataFlowIssue")
    public static ItemStackTemplate withColor(DanmakuType type, ItemColor color) {
        List<Tuple<Item, ItemStackTemplate>> colorPair = type.getColorPairs().get();
        Tuple<Item, ItemStackTemplate> result = new Tuple<>(null, null);
        for (Tuple<Item, ItemStackTemplate> pair : colorPair) {
            if (pair.getA() == color.item()) {
                result = pair;
                break;
            }
        }
        return result.getB();
    }

    public static ItemStackTemplate random() {
        List<DanmakuType> values = BuiltInRegistryProviders.DANMAKU_TYPE.values().stream().toList();
        DanmakuType type = values.get(ReverieDreams.RD.nextInt(values.size()));
        return random(type);
    }

    public static ItemStackTemplate random(DanmakuType type) {
        List<Tuple<Item, ItemStackTemplate>> colorPair = type.getColorPairs().get();
        Tuple<Item, ItemStackTemplate> pair = colorPair.get(ReverieDreams.RD.nextInt(colorPair.size()));
        return pair.getB();
    }

    public static List<ItemStackTemplate> allColor() {
        ImmutableList.Builder<ItemStackTemplate> builder = ImmutableList.builder();
        List<DanmakuType> typeList = BuiltInRegistryProviders.DANMAKU_TYPE.values().stream().filter(type -> !type.isDeleteFromList()).toList();
        for (DanmakuType danmakuType : typeList) {
            List<Tuple<Item, ItemStackTemplate>> colorPair = danmakuType.getColorPairs().get();
            for (Tuple<Item, ItemStackTemplate> pair : colorPair) {
                builder.add(pair.getB());
            }
        }
        return builder.build();
    }

    public static void bootstrap(RegistryProvider<DanmakuType> registry) {

    }

}
