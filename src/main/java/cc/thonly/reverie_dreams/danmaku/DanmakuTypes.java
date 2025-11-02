package cc.thonly.reverie_dreams.danmaku;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.IntrinsicalRegister;
import cc.thonly.reverie_dreams.registry.ItemColor;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class DanmakuTypes {
    public static final List<DanmakuType> UNLIST = new ArrayList<>();
    public static final DanmakuType AMULET = register(ReverieDreams.id("amulet"), 2f, 1f, 1.0f, false, false);
    public static final DanmakuType ARROWHEAD = register(ReverieDreams.id("arrowhead"), 2f, 1f, 1.0f, false, false);
    public static final DanmakuType BALL = register(ReverieDreams.id("ball"), 2f, 1f, 1.0f, true, false);
    public static final DanmakuType BUBBLE = register(ReverieDreams.id("bubble"), 2.5f, 2f, 1.0f, true, false);
    public static final DanmakuType BULLET = register(ReverieDreams.id("bullet"), 2f, 1f, 1.0f, false, false);
    public static final DanmakuType FIREBALL = register(ReverieDreams.id("fireball"), 2f, 1f, 1.0f, true, false);
    public static final DanmakuType FIREBALL_GLOWY = register(ReverieDreams.id("fireball_glowy"), 1f, 1f, 1.0f, true, false);
    public static final DanmakuType KUNAI = register(ReverieDreams.id("kunai"), 2f, 1f, 1.0f, false, false);
    public static final DanmakuType RICE = register(ReverieDreams.id("rice"), 2f, 1f, 1.0f, false, false);
    public static final DanmakuType STAR = register(ReverieDreams.id("star"), 2f, 1f, 1.0f, true, false);
    public static final DanmakuType LASER = register(ReverieDreams.id("laser"), 3f, 1.5f, 1.0f, false, false);
    public static final DanmakuType BIG_LASER = register(ReverieDreams.id("big_laser"), 3f, 1.5f, 1.0f, false, false);

    static {
        UNLIST.add(LASER);
        UNLIST.add(BIG_LASER);
    }

    public static DanmakuType register(ResourceLocation key, float damage, float scale, float speed, boolean tile, boolean infinite) {
        return RegistryManager.registerForBuiltin(RegistryManager.DANMAKU_TYPE, key, new DanmakuType(key, damage, scale, speed, tile, infinite));
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
        List<DanmakuType> values = RegistryManager.DANMAKU_TYPE.values().stream().toList();
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
        List<DanmakuType> typeList = RegistryManager.DANMAKU_TYPE.values().stream().filter(type -> !UNLIST.contains(type)).toList();
        for (DanmakuType danmakuType : typeList) {
            List<Tuple<Item, ItemStack>> colorPair = danmakuType.getColorPairs();
            for (Tuple<Item, ItemStack> pair : colorPair) {
                builder.add(pair.getB());
            }
        }
        return builder.build();
    }

    public static void bootstrap(IntrinsicalRegister<DanmakuType> registry) {

    }

}
