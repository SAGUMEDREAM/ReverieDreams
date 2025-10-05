package cc.thonly.reverie_dreams.util.block;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;

import java.util.Map;


public class CropAgeUtil {
    private static final Map<Integer, IntProperty> DEFAULT = new Object2ObjectOpenHashMap<>();
    static {
        DEFAULT.put(1, Properties.AGE_1);
        DEFAULT.put(2, Properties.AGE_2);
        DEFAULT.put(3, Properties.AGE_3);
        DEFAULT.put(4, Properties.AGE_4);
        DEFAULT.put(5, Properties.AGE_5);
        DEFAULT.put(7, Properties.AGE_7);
        DEFAULT.put(15, Properties.AGE_15);
        DEFAULT.put(25, Properties.AGE_25);
    }
    public static IntProperty fromInt(int age) {
        return DEFAULT.computeIfAbsent(age, (a)-> IntProperty.of("age", 0, age));
    }

    public static int[] toArray(IntProperty property) {
        return property.getValues().stream().mapToInt(Integer::intValue).toArray();
    }
}
