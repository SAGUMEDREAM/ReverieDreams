package cc.thonly.reverie_dreams.data.craftengine;

import cc.thonly.reverie_dreams.mixin.accessor.BlockAccessor;
import cc.thonly.reverie_dreams.mixin.accessor.ItemAccessor;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import dev.architectury.platform.Mod;
import dev.architectury.platform.Platform;
import dev.architectury.utils.GameInstance;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.entity.FuelValues;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Getter
@Slf4j
public class ItemDefinitionList {

    @JsonProperty("items")
    public final Map<String, Definition> items =
            new Object2ObjectLinkedOpenHashMap<>();

    public ItemDefinitionList(List<ItemLike> list) {
        for (ItemLike itemLike : list) {
            Item item = itemLike.asItem();

            if (item.getDefaultInstance().isEmpty()) {
                continue;
            }

            ResourceKey<Item> resourceKey =
                    ((ItemAccessor) item)
                            .reverie_dreams$builtInRegistryHolder()
                            .key();

            Identifier id = resourceKey.identifier();
            String key = id.toString();

            Definition definition = new Definition(item);
            this.items.put(key, definition);
        }
    }

    public static class Definition {
        transient final Item item;

        @JsonProperty("material")
        public final String material;

        @JsonProperty("data")
        public final Data data;

        @JsonProperty("settings")
        public final Settings settings;

        @JsonProperty("model")
        public final Model model;

        @JsonProperty("behavior")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public final Behavior behavior;

        @JsonProperty("behaviors")
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        public final List<Behavior> behaviors;

        public Definition(Item item) {
            this.item = item;

            this.material =
                    ((ItemAccessor) item)
                            .reverie_dreams$builtInRegistryHolder()
                            .key()
                            .identifier()
                            .toString();

            this.data = new Data(item);
            this.settings = new Settings(item);
            this.model = new Model(item);

            List<Behavior> behaviors = BehaviorReader.read(item);

            if (behaviors.isEmpty()) {
                this.behavior = null;
                this.behaviors = new ArrayList<>();
            } else if (behaviors.size() == 1) {
                this.behavior = behaviors.getFirst();
                this.behaviors = new ArrayList<>();
            } else {
                this.behavior = null;
                this.behaviors = behaviors;
            }
        }
    }

    /**
     * CraftEngine behavior
     */
    public static class Behavior {

        @JsonProperty("type")
        public final String type;

        @JsonAnyGetter
        public final Map<String, Object> properties;

        public Behavior(String type) {
            this.type = type;
            this.properties = new Object2ObjectLinkedOpenHashMap<>();
        }
    }

    public static class BehaviorReader {

        public static List<Behavior> read(Item item) {
            List<Behavior> behaviors = new ArrayList<>();

            float chance =
                    ComposterBlock.COMPOSTABLES.getOrDefault(item, 0.0F);

            if (chance > 0) {
                Behavior behavior =
                        new Behavior("compostable_item");

                behavior.properties.put("chance", chance);
                behaviors.add(behavior);
            }

            if (item instanceof BlockItem blockItem) {
                Block block = blockItem.getBlock();

                Holder.Reference<Block> reference =
                        ((BlockAccessor) block)
                                .reverie_dreams$builtInRegistryHolder();

                ResourceKey<Block> key = reference.key();
                Identifier identifier = key.identifier();
                String blockId = identifier.toString();

                Behavior behavior =
                        new Behavior("block_item");

                behavior.properties.put("block", blockId);
                behaviors.add(behavior);
            }

            return behaviors;
        }
    }

    public static class Model {

        transient final Item item;

        @JsonAnyGetter
        public final Map<String, Object> properties =
                new Object2ObjectLinkedOpenHashMap<>();

        public transient Map<String, Object> model;

        public Model(Item item) {
            this.item = item;
            this.readItems();
            this.readItemModel();
        }

        void readItems() {
            Holder.Reference<Item> reference =
                    ((ItemAccessor) this.item)
                            .reverie_dreams$builtInRegistryHolder();

            ResourceKey<Item> key = reference.key();
            Identifier itemId = key.identifier();

            String target =
                    "assets/%s/items/%s.json"
                            .formatted(
                                    itemId.getNamespace(),
                                    itemId.getPath()
                            );

            Collection<Mod> mods = Platform.getMods();
            ObjectMapper mapper = new ObjectMapper();

            Map<String, Object> object = null;

            for (Mod mod : mods) {
                Optional<Path> resource = mod.findResource(target);

                if (resource.isEmpty()) {
                    continue;
                }

                Path path = resource.get();

                try {
                    Map<String, Object> map =
                            mapper.readValue(
                                    path.toFile(),
                                    new TypeReference<Map<String, Object>>() {
                                    }
                            );

                    object = map;
                    break;
                } catch (Exception e) {
                    log.error("Error reading item definition: {}", path, e);
                }
            }

            if (object == null) {
                return;
            }

            Object modelObject = object.get("model");

            if (!(modelObject instanceof Map<?, ?> rawModel)) {
                return;
            }

            Map<String, Object> model = toStringObjectMap(rawModel);

            if (model.isEmpty()) {
                return;
            }

            model.forEach(this.properties::put);

            Object nestedModel = model.get("model");

            if (nestedModel instanceof String modelPath) {
                this.properties.put("path", modelPath);
            }

            this.model = model;
        }

        void scanModel(Object object, Collection<String> modelIds) {
            if (object instanceof Map<?, ?> map) {
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    String key = String.valueOf(entry.getKey());
                    Object value = entry.getValue();

                    if (("model".equals(key) || "minecraft:model".equals(key))
                            && value instanceof String modelId) {
                        modelIds.add(modelId);
                        continue;
                    }

                    scanModel(value, modelIds);
                }

                return;
            }

            if (object instanceof Collection<?> collection) {
                for (Object element : collection) {
                    scanModel(element, modelIds);
                }
            }
        }

        void readItemModel() {
            if (this.model == null) {
                return;
            }

            Collection<String> modelIds =
                    new LinkedHashSet<>();

            scanModel(this.model, modelIds);

            Map<String, Object> generation =
                    new Object2ObjectLinkedOpenHashMap<>();

            ObjectMapper mapper = new ObjectMapper();

            List<Identifier> modelIdList =
                    modelIds.stream()
                            .map(Identifier::tryParse)
                            .filter(Objects::nonNull)
                            .toList();

            Collection<Mod> mods = Platform.getMods();

            for (Identifier modelId : modelIdList) {
                String target =
                        "assets/%s/models/%s.json"
                                .formatted(
                                        modelId.getNamespace(),
                                        modelId.getPath()
                                );

                Map<String, Object> object = null;

                for (Mod mod : mods) {
                    Optional<Path> resource =
                            mod.findResource(target);

                    if (resource.isEmpty()) {
                        continue;
                    }

                    Path path = resource.get();

                    try {
                        Map<String, Object> map =
                                mapper.readValue(
                                        path.toFile(),
                                        new TypeReference<Map<String, Object>>() {
                                        }
                                );

                        object = map;
                        break;
                    } catch (Exception e) {
                        log.error(
                                "Error reading item model: {}",
                                path,
                                e
                        );
                    }
                }

                if (object == null) {
                    log.error("Missing item model {}", target);
                    continue;
                }

                generation.putAll(object);
            }

            this.properties.put("generation", generation);
        }

        /**
         * 将任意 Map 安全转换为 Map<String, Object>。
         */
        private static Map<String, Object> toStringObjectMap(
                Map<?, ?> source
        ) {
            Map<String, Object> result =
                    new Object2ObjectLinkedOpenHashMap<>();

            for (Map.Entry<?, ?> entry : source.entrySet()) {
                result.put(
                        String.valueOf(entry.getKey()),
                        entry.getValue()
                );
            }

            return result;
        }
    }

    public static class Settings {

        transient final Item item;

        @JsonProperty("tags")
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        public final List<String> tags =
                new ArrayList<>();

        @JsonProperty("fuel_time")
        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        public int fuelTime;

        public Settings(Item item) {
            this.item = item;
            this.readSettings();
        }

        void readSettings() {
            Holder.Reference<Item> reference =
                    ((ItemAccessor) this.item)
                            .reverie_dreams$builtInRegistryHolder();

            if (reference.tags != null) {
                for (TagKey<Item> tag : reference.tags) {
                    this.tags.add(
                            tag.location().toString()
                    );
                }
            }

            MinecraftServer server =
                    GameInstance.getServer();

            if (server != null) {
                FuelValues fuelValues =
                        server.fuelValues();

                this.fuelTime =
                        fuelValues.burnDuration(
                                this.item.getDefaultInstance()
                        );
            }
        }
    }

    public static class Data {

        transient final Item item;

        @JsonProperty("components")
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        public final Map<String, Object> components =
                new Object2ObjectLinkedOpenHashMap<>();

        public Data(Item item) {
            this.item = item;
            this.readData();
        }

        void readData() {
            MinecraftServer server =
                    GameInstance.getServer();

            if (server == null) {
                return;
            }

            RegistryAccess.Frozen registryAccess =
                    server.registryAccess();

            RegistryOps<JsonElement> ops =
                    RegistryOps.create(
                            JsonOps.INSTANCE,
                            registryAccess
                    );

            ItemStack stack =
                    this.item.getDefaultInstance();

            DataComponentMap prototype =
                    stack.getPrototype();

            for (TypedDataComponent<?> component : prototype) {
                this.readComponent(ops, component);
            }
        }

        /**
         * 使用泛型辅助方法，避免 raw Codec。
         */
        private <T> void readComponent(
                RegistryOps<JsonElement> ops,
                TypedDataComponent<T> component
        ) {
            DataComponentType<T> type =
                    component.type();

            Codec<T> codec =
                    type.codec();

            if (codec == null) {
                return;
            }

            Identifier id =
                    BuiltInRegistries.DATA_COMPONENT_TYPE
                            .getKey(type);

            Optional<JsonElement> result =
                    codec.encodeStart(
                            ops,
                            component.value()
                    ).result();

            result.ifPresent(value ->
                    this.components.put(
                            id.toString(),
                            CraftEngineProvider.convertJson(value)
                    )
            );
        }
    }
}