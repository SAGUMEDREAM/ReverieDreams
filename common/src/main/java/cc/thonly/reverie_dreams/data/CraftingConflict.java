package cc.thonly.reverie_dreams.data;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.RegistryImpls;
import cc.thonly.reverie_dreams.registry.content.FoodProperties;
import cc.thonly.reverie_dreams.registry.impl.RegistryImpl;
import cc.thonly.reverie_dreams.registry.CodecStep;
import cc.thonly.reverie_dreams.registry.OwnerBinding;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;

@ToString
public class CraftingConflict implements CodecStep<CraftingConflict>, OwnerBinding<CraftingConflict> {
    public static final Codec<CraftingConflict> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("item").forGetter((entry) -> BuiltInRegistries.ITEM.getKey(entry.item)),
            Codec.list(Identifier.CODEC).fieldOf("values").forGetter((entry) -> {
                List<Identifier> identifiers = new ArrayList<>();
                for (FoodProperty foodProperty : entry.foodProperties) {
                    Identifier id = foodProperty.getId();
                    identifiers.add(id);
                }
                return identifiers;
            })
    ).apply(instance, CraftingConflict::new));
    @Setter
    @Getter
    private Identifier id;
    @Getter
    private final Item item;
    private final Set<FoodProperty> foodProperties = new ObjectOpenHashSet<>();
    @Setter
    @Getter
    private RegistryImpl<CraftingConflict> owner;

    private CraftingConflict() {
        this.item = Items.AIR;
    }

    public CraftingConflict(Identifier item, List<Identifier> identifiers) {
        this(BuiltInRegistries.ITEM.getValue(item), identifiers);
    }

    public CraftingConflict(Item item, List<Identifier> identifiers) {
        this.item = item;
        for (var identifier : identifiers) {
            FoodProperty property = RegistryImpls.FOOD_PROPERTY.getValue(identifier);
            if (property != null) {
                this.foodProperties.add(property);
            }
        }
    }

    public static CraftingConflict of(Item item, List<FoodProperty> foodProperties) {
        List<Identifier> list = foodProperties.stream().map(FoodProperty::getId).toList();
        return new CraftingConflict(item, list);
    }

    public static CraftingConflict of(ItemLike item, List<FoodProperty> foodProperties) {
        List<Identifier> list = foodProperties.stream().map(FoodProperty::getId).toList();
        return new CraftingConflict(item.asItem(), list);
    }

    public boolean test(ItemStack ingredient, FoodProperty property) {
        if (!ingredient.getItem().equals(this.item)) {
            return false;
        }
        return this.foodProperties.contains(property);
    }

    public boolean test(ItemStack ingredient) {
        if (!ingredient.getItem().equals(this.item)) {
            return false;
        }
        List<FoodProperty> properties = new ArrayList<>(FoodProperties.get(ingredient));
        for (FoodProperty ingredientProperty : properties) {
            if (this.foodProperties.contains(ingredientProperty)) {
                return true;
            }
        }
        return false;
    }


    public Stream<FoodProperty> stream() {
        return this.foodProperties.stream();
    }

    @Override
    public Codec<CraftingConflict> getCodec() {
        return CODEC;
    }

    public static void reload(ResourceManager manager) {
        RegistryImpls.CRAFTING_CONFLICT.clear();
        Map<Identifier, Resource> resources = manager.listResources("crafting_conflict", id ->
                id.getNamespace().equals(ReverieDreams.MOD_ID) && id.getPath().endsWith(".json")
        );
        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            Identifier resId = entry.getKey();
            Identifier id = Identifier.fromNamespaceAndPath(resId.getNamespace(), resId.getPath().replace("crafting_conflict/", "").replace(".json", ""));
            Resource resource = entry.getValue();
            try (InputStream stream = resource.open()) {
                JsonElement json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                DataResult<CraftingConflict> result = CraftingConflict.CODEC.parse(JsonOps.INSTANCE, json);
                Optional<CraftingConflict> optional = result.result();
                if (optional.isPresent()) {
                    CraftingConflict conflict = optional.get();
                    conflict.setId(id);
                    RegistryImpls.register(RegistryImpls.CRAFTING_CONFLICT, id, conflict); // 注册
                } else {
                    ReverieDreams.LOGGER.error("Failed to parse crafting_conflict {}: {}", id, result.error().map(Object::toString).orElse("Unknown error"));
                }
            } catch (IOException e) {
                ReverieDreams.LOGGER.error("Failed to load food_property {}: {}", id, e.getMessage(), e);
            }
        }
    }

    public static void bootstrap(RegistryImpl<CraftingConflict> registry) {

    }
}
