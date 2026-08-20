package cc.thonly.reverie_dreams.registry.content.danmaku;

import cc.thonly.reverie_dreams.ReverieDreams;
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
    public static final DanmakuType AMULET = registerType(ReverieDreams.id("amulet"), RDDamageTypes.DANMAKU_GENERIC, 2f, 1f, 1.2f, false, false);
    public static final DanmakuType ARROWHEAD = registerType(ReverieDreams.id("arrowhead"), RDDamageTypes.DANMAKU_REAL, 2f, 1f, 1.2f, false, false);
    public static final DanmakuType BALL = registerType(ReverieDreams.id("ball"), RDDamageTypes.DANMAKU_GENERIC, 2f, 1f, 1.2f, true, false);
    public static final DanmakuType BUBBLE = registerType(ReverieDreams.id("bubble"), RDDamageTypes.DANMAKU_GENERIC, 2.5f, 2f, 1.2f, true, false);
    public static final DanmakuType BULLET = registerType(ReverieDreams.id("bullet"), RDDamageTypes.DANMAKU_REAL, 3f, 1f, 1.2f, false, false);
    public static final DanmakuType FIREBALL = registerType(ReverieDreams.id("fireball"), RDDamageTypes.DANMAKU_GENERIC, 2f, 1f, 1.2f, true, false);
    public static final DanmakuType FIREBALL_GLOWY = registerType(ReverieDreams.id("fireball_glowy"), RDDamageTypes.DANMAKU_GENERIC, 1f, 1f, 1.2f, true, false);
    public static final DanmakuType KUNAI = registerType(ReverieDreams.id("kunai"), RDDamageTypes.DANMAKU_REAL, 2f, 1f, 1.2f, false, false);
    public static final DanmakuType RICE = registerType(ReverieDreams.id("rice"), RDDamageTypes.DANMAKU_GENERIC, 2f, 1f, 1.2f, false, false);
    public static final DanmakuType STAR = registerType(ReverieDreams.id("star"), RDDamageTypes.DANMAKU_GENERIC, 2f, 1f, 1.2f, true, false);
    public static final DanmakuType NOTE = registerType(ReverieDreams.id("note"), RDDamageTypes.DANMAKU_GENERIC, 2f, 0.8f, 1.55f, false, false);
    public static final DanmakuType LASER = registerType(ReverieDreams.id("laser"), RDDamageTypes.DANMAKU_GENERIC, 4f, 1.5f, 1.2f, false, false).unlist();
    public static final DanmakuType BIG_LASER = registerType(ReverieDreams.id("big_laser"), RDDamageTypes.DANMAKU_GENERIC, 4f, 1.5f, 1.2f, false, false).unlist();

    public static DanmakuType registerType(Identifier key, ResourceKey<DamageType> damageTypeKey, float damage, float scale, float speed, boolean tile, boolean infinite) {
        return BuiltInRegistryProviders.registerForBuiltin(BuiltInRegistryProviders.DANMAKU_TYPE, key, new DanmakuType(key, damageTypeKey, damage, scale, speed, tile, infinite));
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
