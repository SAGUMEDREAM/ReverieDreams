package cc.thonly.reverie_dreams.danmaku;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.registry.ItemColor;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import cc.thonly.reverie_dreams.registry.StandaloneRegistry;
import com.google.common.collect.ImmutableList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DanmakuTypes {
    public static final List<DanmakuType> UNLIST = new ArrayList<>();
    public static final DanmakuType AMULET = register(Touhou.id("amulet"), 2f, 1f, 1.0f, false, false);
    public static final DanmakuType ARROWHEAD = register(Touhou.id("arrowhead"), 2f, 1f, 1.0f, false, false);
    public static final DanmakuType BALL = register(Touhou.id("ball"), 2f, 1f, 1.0f, true, false);
    public static final DanmakuType BUBBLE = register(Touhou.id("bubble"), 2.5f, 2f, 1.0f, true, false);
    public static final DanmakuType BULLET = register(Touhou.id("bullet"), 2f, 1f, 1.0f, false, false);
    public static final DanmakuType FIREBALL = register(Touhou.id("fireball"), 2f, 1f, 1.0f, true, false);
    public static final DanmakuType FIREBALL_GLOWY = register(Touhou.id("fireball_glowy"), 1f, 1f, 1.0f, true, false);
    public static final DanmakuType KUNAI = register(Touhou.id("kunai"), 2f, 1f, 1.0f, false, false);
    public static final DanmakuType RICE = register(Touhou.id("rice"), 2f, 1f, 1.0f, false, false);
    public static final DanmakuType STAR = register(Touhou.id("star"), 2f, 1f, 1.0f, true, false);
    public static final DanmakuType LASER = register(Touhou.id("laser"), 3f, 1.5f, 1.0f, false, false);
    public static final DanmakuType BIG_LASER = register(Touhou.id("big_laser"), 3f, 1.5f, 1.0f, false, false);

    static {
        UNLIST.add(LASER);
        UNLIST.add(BIG_LASER);
    }

    public static DanmakuType register(Identifier key, float damage, float scale, float speed, boolean tile, boolean infinite) {
        return RegistryManager.registerFinal(RegistryManager.DANMAKU_TYPE, key, new DanmakuType(key, damage, scale, speed, tile, infinite));
    }

    public static ItemStack withColor(DanmakuType type, ItemColor color) {
        List<Pair<Item, ItemStack>> colorPair = type.getColorPairs();
        Pair<Item, ItemStack> result = new Pair<>(null, null);
        for (Pair<Item, ItemStack> pair : colorPair) {
            if (pair.getLeft() == color.item()) {
                result = pair;
                break;
            }
        }
        return result.getRight();
    }

    public static ItemStack random() {
        List<DanmakuType> values = RegistryManager.DANMAKU_TYPE.values().stream().toList();
        DanmakuType type = values.get(new Random().nextInt(values.size()));
        return random(type);
    }

    public static ItemStack random(DanmakuType type) {
        List<Pair<Item, ItemStack>> colorPair = type.getColorPairs();
        Pair<Item, ItemStack> pair = colorPair.get(new Random().nextInt(colorPair.size()));
        return pair.getRight().copy();
    }

    public static List<ItemStack> allColor() {
        ImmutableList.Builder<ItemStack> builder = ImmutableList.builder();
        List<DanmakuType> typeList = RegistryManager.DANMAKU_TYPE.values().stream().filter(type -> !UNLIST.contains(type)).toList();
        for (DanmakuType danmakuType : typeList) {
            List<Pair<Item, ItemStack>> colorPair = danmakuType.getColorPairs();
            for (Pair<Item, ItemStack> pair : colorPair) {
                builder.add(pair.getRight());
            }
        }
        return builder.build();
    }

    public static void bootstrap(StandaloneRegistry<DanmakuType> registry) {

    }

}
