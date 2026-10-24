package cc.thonly.reverie_dreams.api.registry;

import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 跨平台可燃方块注册表。
 *
 * <p>参数顺序与 {@link net.minecraft.world.level.block.FireBlock#setFlammable} 一致：
 * {@code igniteOdds} 是点燃几率，{@code burnOdds} 是燃烧/蔓延强度。
 * 平台侧分别映射到：
 * <ul>
 *     <li>Fabric：{@code FlammableBlockRegistry.add(block, igniteOdds, burnOdds)}</li>
 *     <li>NeoForge：{@code FireBlock.setFlammable(block, igniteOdds, burnOdds)}</li>
 * </ul>
 */
public interface FlammableBlockRegistry {
    List<Entry> ENTRIES = new ArrayList<>();

    static void register(Holder<Block> block, int igniteOdds, int burnOdds) {
        ENTRIES.add(new Entry(block, null, igniteOdds, burnOdds));
    }

    static void register(TagKey<Block> tagKey, int igniteOdds, int burnOdds) {
        ENTRIES.add(new Entry(null, tagKey, igniteOdds, burnOdds));
    }

    record Entry(
            @Nullable Holder<Block> blockHolder,
            @Nullable TagKey<Block> tagKey,
            int igniteOdds,
            int burnOdds
    ) {
    }
}
