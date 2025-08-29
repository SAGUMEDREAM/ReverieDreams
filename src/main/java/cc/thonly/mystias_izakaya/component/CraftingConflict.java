package cc.thonly.mystias_izakaya.component;

import cc.thonly.mystias_izakaya.MystiasIzakaya;
import cc.thonly.mystias_izakaya.registry.MIRegistryManager;
import cc.thonly.reverie_dreams.entity.npc.NPCRoleInteractionEvent;
import cc.thonly.reverie_dreams.registry.*;
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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;

@ToString
public class CraftingConflict implements CodecStep<CraftingConflict>, OwnerBinding<CraftingConflict>, BuiltinObject {
    public static final Codec<CraftingConflict> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("item").forGetter((entry) -> Registries.ITEM.getId(entry.item)),
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
    private IntrinsicalRegister<CraftingConflict> owner;

    private CraftingConflict() {
        this.item = Items.AIR;
    }

    public CraftingConflict(Identifier item, List<Identifier> identifiers) {
        this(Registries.ITEM.get(item), identifiers);
    }

    public CraftingConflict(Item item, List<Identifier> identifiers) {
        this.item = item;
        for (var identifier : identifiers) {
            FoodProperty property = MIRegistryManager.FOOD_PROPERTY.get(identifier);
            if (property != null) {
                this.foodProperties.add(property);
            }
        }
    }

    public static CraftingConflict of(Item item, List<FoodProperty> foodProperties) {
        List<Identifier> list = foodProperties.stream().map(FoodProperty::getId).toList();
        return new CraftingConflict(item, list);
    }

    public boolean test(ItemStack ingredient) {
        if (!ingredient.getItem().equals(this.item)) {
            return false;
        }
        List<FoodProperty> properties = new ArrayList<>();
        properties.addAll(FoodProperty.getFromItemStack(ingredient));
        properties.addAll(FoodProperty.getFromItemStackComponent(ingredient));
        for (FoodProperty ingredientProperty : properties) {
//            System.out.println(ingredientProperty);
//            System.out.println(this.foodProperties);
            if (this.foodProperties.contains(ingredientProperty)) {
//                System.out.println(true);
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
        Map<Identifier, Resource> resources = manager.findResources("crafting_conflict", id ->
                id.getNamespace().equals(MystiasIzakaya.MOD_ID) && id.getPath().endsWith(".json")
        );
        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            Identifier resId = entry.getKey();
            Identifier id = Identifier.of(resId.getNamespace(), resId.getPath().replace("crafting_conflict/", "").replace(".json", ""));
            Resource resource = entry.getValue();
            try (InputStream stream = resource.getInputStream()) {
                JsonElement json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                DataResult<CraftingConflict> result = CraftingConflict.CODEC.parse(JsonOps.INSTANCE, json);
                Optional<CraftingConflict> optional = result.result();
                if (optional.isPresent()) {
                    CraftingConflict conflict = optional.get();
                    conflict.setId(id);
                    MIRegistryManager.register(MIRegistryManager.CRAFTING_CONFLICT, id, conflict); // 注册
                } else {
                    MystiasIzakaya.LOGGER.error("Failed to parse crafting_conflict {}: {}", id, result.error().map(Object::toString).orElse("Unknown error"));
                }
            } catch (IOException e) {
                MystiasIzakaya.LOGGER.error("Failed to load food_property {}: {}", id, e.getMessage(), e);
            }
        }
    }

    public static void bootstrap(IntrinsicalRegister<CraftingConflict> registry) {

    }
}
