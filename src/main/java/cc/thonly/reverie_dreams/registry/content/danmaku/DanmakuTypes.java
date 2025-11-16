package cc.thonly.reverie_dreams.registry.content.danmaku;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuType;
import cc.thonly.reverie_dreams.registry.RegistryHandlers;
import cc.thonly.reverie_dreams.registry.content.ItemColor;
import cc.thonly.reverie_dreams.registry.content.RDDamageTypes;
import cc.thonly.reverie_dreams.registry.impl.RegistryHandler;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class DanmakuTypes {
    public static final List<DanmakuType> UNLIST = new ArrayList<>();
    public static final DanmakuType AMULET = registerType(ReverieDreams.id("amulet"), RDDamageTypes.DANMAKU_GENERIC, 2f, 1f, 0.75f, false, false);
    public static final DanmakuType ARROWHEAD = registerType(ReverieDreams.id("arrowhead"), RDDamageTypes.DANMAKU_REAL, 2f, 1f, 0.75f, false, false);
    public static final DanmakuType BALL = registerType(ReverieDreams.id("ball"), RDDamageTypes.DANMAKU_GENERIC, 2f, 1f, 0.75f, true, false);
    public static final DanmakuType BUBBLE = registerType(ReverieDreams.id("bubble"), RDDamageTypes.DANMAKU_GENERIC, 2.5f, 2f, 0.75f, true, false);
    public static final DanmakuType BULLET = registerType(ReverieDreams.id("bullet"), RDDamageTypes.DANMAKU_REAL, 3f, 1f, 0.75f, false, false);
    public static final DanmakuType FIREBALL = registerType(ReverieDreams.id("fireball"), RDDamageTypes.DANMAKU_GENERIC, 2f, 1f, 0.75f, true, false);
    public static final DanmakuType FIREBALL_GLOWY = registerType(ReverieDreams.id("fireball_glowy"), RDDamageTypes.DANMAKU_GENERIC, 1f, 1f, 0.75f, true, false);
    public static final DanmakuType KUNAI = registerType(ReverieDreams.id("kunai"), RDDamageTypes.DANMAKU_REAL, 2f, 1f, 0.75f, false, false);
    public static final DanmakuType RICE = registerType(ReverieDreams.id("rice"), RDDamageTypes.DANMAKU_GENERIC, 2f, 1f, 0.75f, false, false);
    public static final DanmakuType STAR = registerType(ReverieDreams.id("star"), RDDamageTypes.DANMAKU_GENERIC, 2f, 1f, 0.75f, true, false);
    public static final DanmakuType LASER = registerType(ReverieDreams.id("laser"), RDDamageTypes.DANMAKU_GENERIC, 4f, 1.5f, 0.75f, false, false);
    public static final DanmakuType BIG_LASER = registerType(ReverieDreams.id("big_laser"), RDDamageTypes.DANMAKU_GENERIC, 4f, 1.5f, 0.75f, false, false);

    static {
        UNLIST.add(LASER);
        UNLIST.add(BIG_LASER);
    }

    public static DanmakuType registerType(ResourceLocation key, ResourceKey<DamageType> damageTypeKey, float damage, float scale, float speed, boolean tile, boolean infinite) {
        return RegistryHandlers.registerForBuiltin(RegistryHandlers.DANMAKU_TYPE, key, new DanmakuType(key, damageTypeKey, damage, scale, speed, tile, infinite));
    }

    public static ItemStack withColor(DanmakuType type, ItemColor color) {
        List<Tuple<Item, ItemStack>> colorPair = type.getColorPairs();
        Tuple<Item, ItemStack> result = new Tuple<>(null, null);
        for (Tuple<Item, ItemStack> pair : colorPair) {
            if (pair.getA() == color.item()) {
                result = pair;
                break;
            }
        }
        return result.getB();
    }

    public static ItemStack random() {
        List<DanmakuType> values = RegistryHandlers.DANMAKU_TYPE.values().stream().toList();
        DanmakuType type = values.get(new Random().nextInt(values.size()));
        return random(type);
    }

    public static ItemStack random(DanmakuType type) {
        List<Tuple<Item, ItemStack>> colorPair = type.getColorPairs();
        Tuple<Item, ItemStack> pair = colorPair.get(new Random().nextInt(colorPair.size()));
        return pair.getB().copy();
    }

    public static List<ItemStack> allColor() {
        ImmutableList.Builder<ItemStack> builder = ImmutableList.builder();
        List<DanmakuType> typeList = RegistryHandlers.DANMAKU_TYPE.values().stream().filter(type -> !UNLIST.contains(type)).toList();
        for (DanmakuType danmakuType : typeList) {
            List<Tuple<Item, ItemStack>> colorPair = danmakuType.getColorPairs();
            for (Tuple<Item, ItemStack> pair : colorPair) {
                builder.add(pair.getB());
            }
        }
        return builder.build();
    }

    public static void bootstrap(RegistryHandler<DanmakuType> registry) {

    }

}
