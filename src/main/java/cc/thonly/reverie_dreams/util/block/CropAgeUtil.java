package cc.thonly.reverie_dreams.util.block;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;


public class CropAgeUtil {
    private static final Map<Integer, IntegerProperty> DEFAULT = new Object2ObjectOpenHashMap<>();
    static {
        DEFAULT.put(1, BlockStateProperties.AGE_1);
        DEFAULT.put(2, BlockStateProperties.AGE_2);
        DEFAULT.put(3, BlockStateProperties.AGE_3);
        DEFAULT.put(4, BlockStateProperties.AGE_4);
        DEFAULT.put(5, BlockStateProperties.AGE_5);
        DEFAULT.put(7, BlockStateProperties.AGE_7);
        DEFAULT.put(15, BlockStateProperties.AGE_15);
        DEFAULT.put(25, BlockStateProperties.AGE_25);
    }
    public static IntegerProperty fromInt(int age) {
        return DEFAULT.computeIfAbsent(age, (a)-> IntegerProperty.create("age", 0, age));
    }

    public static int[] toArray(IntegerProperty property) {
        return property.getPossibleValues().stream().mapToInt(Integer::intValue).toArray();
    }
}
