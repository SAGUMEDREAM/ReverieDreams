package cc.thonly.reverie_dreams.world.gensokyo.data;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Optional;

/**
 * 解析地图里的方块状态字符串。
 *
 * <p>兼容旧格式：只有方块 ID 时返回 defaultBlockState()；带属性时使用
 * {@code namespace:path[prop=value,...]}，与 Minecraft 的 BlockState 文本表示一致。</p>
 */
public final class GensokyoBlockStates {
    private GensokyoBlockStates() {
    }

    public static BlockState parse(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("blockstate is empty");
        }
        String text = value.trim();
        int bracket = text.indexOf('[');
        int end = text.lastIndexOf(']');
        String idText = (bracket >= 0 ? text.substring(0, bracket) : text).trim();
        if (idText.isEmpty()) {
            throw new IllegalArgumentException("blockstate has no block id: " + value);
        }
        Identifier id = Identifier.parse(idText);
        Block block = BuiltInRegistries.BLOCK.getValue(id);
        if (block == null) {
            throw new IllegalArgumentException("unknown block: " + id);
        }
        BlockState state = block.defaultBlockState();
        if (bracket < 0) {
            return state;
        }
        if (end < bracket) {
            throw new IllegalArgumentException("unclosed blockstate properties: " + value);
        }
        String properties = text.substring(bracket + 1, end).trim();
        if (properties.isEmpty()) {
            return state;
        }
        for (String entry : properties.split(",")) {
            String[] parts = entry.split("=", 2);
            if (parts.length != 2) {
                throw new IllegalArgumentException("invalid blockstate property: " + entry);
            }
            String name = parts[0].trim();
            String rawValue = parts[1].trim();
            Property<?> property = block.getStateDefinition().getProperty(name);
            if (property == null) {
                throw new IllegalArgumentException("unknown property " + name + " for " + id);
            }
            state = applyProperty(state, property, rawValue);
        }
        return state;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static BlockState applyProperty(BlockState state, Property<?> property, String rawValue) {
        Optional<?> parsed = property.getValue(rawValue);
        if (parsed.isEmpty()) {
            throw new IllegalArgumentException(
                    "invalid value " + rawValue + " for property " + property.getName());
        }
        return state.setValue((Property) property, (Comparable) parsed.get());
    }
}