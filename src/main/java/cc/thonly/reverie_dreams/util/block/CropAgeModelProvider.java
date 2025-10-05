package cc.thonly.reverie_dreams.util.block;

import lombok.Getter;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.IntProperty;

import java.util.*;

/**
 * CropAgeModelProvider 用于管理作物不同生长阶段对应的模型索引（通常对应材质或物品）。
 * 可通过 setKey/setValue 配置 age 到 index 的映射，使用 build() 自动填补缺失项。
 * 用于渲染、掉落、材质映射等场景。
 */
public class CropAgeModelProvider {
    /**
     * 作物的最大生长阶段（inclusive）。
     */
    @Getter
    private final int maxAge;
    /**
     * 作物使用的 age 属性（IntProperty），用于 BlockState 管理。
     */
    @Getter
    private final IntProperty ageProperty;
    /**
     * 生长阶段与渲染模型索引之间的映射表。
     * key: age 值，value: 索引值（用于渲染或 ItemStack[] 数组索引）。
     */
    @Getter
    private final Map<Integer, Integer> instance = new LinkedHashMap<>();
    @Getter
    private boolean finished = false;
    // 临时缓存在 setKey 和 setValue 之间使用。
    private Set<Integer> keys = new LinkedHashSet<>();

    /**
     * 构造器私有化，使用 create() 构建。
     *
     * @param maxAge 最大生长阶段
     */
    protected CropAgeModelProvider(int maxAge) {
        this.maxAge = maxAge;
        this.ageProperty = CropAgeUtil.fromInt(maxAge);
    }

    /**
     * 创建一个新的 CropAgeModelProvider 实例。
     *
     * @param maxAge 最大生长阶段
     * @return 实例
     */
    public static CropAgeModelProvider create(int maxAge) {
        return new CropAgeModelProvider(maxAge);
    }

    /**
     * 设置一批待映射的 age 阶段（键集合）。
     * 需要与 setValue() 配对使用。
     *
     * @param ages 若干 age 值
     * @return 当前对象（支持链式调用）
     */
    public CropAgeModelProvider setKey(int... ages) {
        this.keys = new HashSet<>();
        for (int age : ages) {
            this.keys.add(age);
        }
        return this;
    }

    /**
     * 将上一步设置的 age 映射到一个共同的索引 index 上。
     *
     * @param index 用于渲染/物品获取的索引值
     * @return 当前对象（支持链式调用）
     */
    public CropAgeModelProvider setValue(int index) {
        for (Integer key : this.keys) {
            this.instance.put(key, index);
        }
        this.keys = new HashSet<>();
        return this;
    }

    /**
     * 构建最终映射，自动填补未指定的 age：
     * - 优先向前查找最近定义的 age 值；
     * - 如果前方没有，则向后查找；
     * - 若两侧都未定义，则默认值为 index = 0。
     *
     * @return 当前对象（支持链式调用）
     */
    public CropAgeModelProvider build() {
        Map<Integer, Integer> ordered = new LinkedHashMap<>();
        for (int i = 0; i <= this.maxAge; i++) {
            if (!this.instance.containsKey(i)) {
                Integer fallback = null;
                for (int j = i - 1; j >= 0; j--) {
                    if (this.instance.containsKey(j)) {
                        fallback = this.instance.get(j);
                        break;
                    }
                }
                if (fallback == null) {
                    for (int j = i + 1; j <= this.maxAge; j++) {
                        if (this.instance.containsKey(j)) {
                            fallback = this.instance.get(j);
                            break;
                        }
                    }
                }
                ordered.put(i, fallback != null ? fallback : 0);
            } else {
                ordered.put(i, this.instance.get(i));
            }
        }
        this.instance.clear();
        this.instance.putAll(ordered);
//        System.out.println(this.instance.values());
        this.finished = true;
        return this;
    }

    /**
     * 根据 age 值获取对应的 ItemStack。
     *
     * @param itemStacks 模型数组，按 index 映射
     * @param age        当前作物的 age 值
     * @return 对应的 ItemStack，若未命中或越界则返回 itemStacks[0]
     */
    public ItemStack get(ItemStack[] itemStacks, int age) {
        int index = this.instance.getOrDefault(age, 0);
        if (index < 0 || index >= itemStacks.length || itemStacks[index] == null) {
            return itemStacks[0];
        }
        return itemStacks[index] != null ? itemStacks[index] : itemStacks[0];
    }

    public int[] toArray() {
        int[] array = new int[this.maxAge + 1];
        for (int i = 0; i <= this.maxAge; i++) {
            array[i] = this.instance.getOrDefault(i, 0);
        }
        return array;
    }

}
