package cc.thonly.reverie_dreams.neoforge.impl;

import cc.thonly.reverie_dreams.registry.impl.MergeRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.registries.callback.RegistryCallback;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

import java.util.*;

@SuppressWarnings({"rawtypes", "unchecked"})
public class NeoMergeRegistry<T> extends MergeRegistry<T> {

    private final List<Registry> registries;

    public NeoMergeRegistry(ResourceKey<? extends Registry<?>> key, List<Registry> registries) {
        super((ResourceKey<? extends Registry<T>>) key, registries);
        this.registries = registries;
    }

    /* ---------------- NeoForge 扩展 ---------------- */

    /**
     * 只读合并注册表不参与同步
     */
    @Override
    public boolean doesSync() {
        return false;
    }

    /**
     * 返回所有 registry 中的最大 ID
     */
    @Override
    public int getMaxId() {
        int max = -1;
        for (Registry<T> registry : registries) {
            max = Math.max(max, registry.size() - 1);
        }
        return max;
    }

    /**
     * 不支持回调
     */
    @Override
    public void addCallback(RegistryCallback callback) {
        // NO-OP
    }

    /**
     * 不支持 alias
     */
    @Override
    public void addAlias(Identifier from, Identifier to) {
        // NO-OP
    }

    /**
     * 解析名称（无 alias，直接返回原值）
     */
    @Override
    public Identifier resolve(Identifier name) {
        return name;
    }

    /**
     * 解析 ResourceKey（无 alias）
     */
    @Override
    public ResourceKey resolve(ResourceKey key) {
        return key;
    }

    /**
     * 根据 ResourceKey 查 ID
     */
    @Override
    public int getId(ResourceKey key) {
        Object value = this.getValue(key);
        if (value == null) return -1;

        return getId((T) value);
    }

    /**
     * 根据 Identifier 查 ID
     */
    @Override
    public int getId(Identifier name) {
        T value = getValue(name);
        if (value == null) return -1;

        return getId(value);
    }

    /**
     * 是否包含 value
     */
    @Override
    public boolean containsValue(Object value) {
        if (value == null) return false;

        for (Registry<T> registry : registries) {
            if (registry.getKey((T) value) != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * DataMap 聚合（弱实现）
     */
    @Override
    public Map getDataMap(DataMapType type) {
        Map<Object, Object> result = new LinkedHashMap<>();

        for (Registry<T> registry : registries) {
            try {
                Map map = registry.getDataMap(type);
                result.putAll(map);
            } catch (Throwable ignored) {

            }
        }

        return result;
    }

    @Override
    public void bindTag(TagKey<T> tagKey, List<Holder<T>> list) {

    }
}