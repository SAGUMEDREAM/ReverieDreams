package wiki;

import cc.thonly.reverie_dreams.component.tooltip.InitTooltips;
import cc.thonly.reverie_dreams.recipe.BaseRecipe;
import cc.thonly.reverie_dreams.recipe.BaseRecipeType;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.flag.FeatureFlags;
import java.nio.file.*;
import java.util.*;
import cc.thonly.reverie_dreams.registry.content.BeverageProperties;
import cc.thonly.reverie_dreams.registry.content.FoodProperties;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.state.BlockState;

/** Documentation maintenance entrypoint, loaded only by export-runtime.init.gradle. */
public final class WikiExportProbe implements ModInitializer {
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            try {
                export(server);
                System.out.println("WIKI_EXPORT_PASS");
            } catch (Throwable failure) {
                System.err.println("WIKI_EXPORT_FAILED");
                failure.printStackTrace();
            } finally {
                server.halt(false);
            }
        });
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void export(MinecraftServer server) throws Exception {
        var gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        var ops = server.registryAccess().createSerializationContext(JsonOps.INSTANCE);
        JsonObject root = new JsonObject();
        root.addProperty("schema", 1);
        root.addProperty("minecraft", server.getServerVersion());
        root.addProperty("version", FabricLoader.getInstance().getModContainer("reverie_dreams").orElseThrow().getMetadata().getVersion().getFriendlyString());
        JsonArray errors = new JsonArray();
        JsonArray items = new JsonArray();
        JsonArray vanillaItems = new JsonArray();
        for (Item item : BuiltInRegistries.ITEM) {
            Identifier id = BuiltInRegistries.ITEM.getKey(item);
            if (!id.getNamespace().equals("reverie_dreams") && !id.getNamespace().equals("minecraft")) continue;
            JsonObject data = new JsonObject();
            data.addProperty("id", id.toString());
            data.addProperty("translationKey", item.getDescriptionId());
            data.addProperty("implementation", item.getClass().getName());
            JsonArray classes = new JsonArray();
            for (Class<?> type = item.getClass(); type != null; type = type.getSuperclass()) classes.add(type.getName());
            data.add("classes", classes);
            ItemStack stack = item.getDefaultInstance();
            data.add("name", ComponentSerialization.CODEC.encodeStart(ops, stack.getHoverName()).getOrThrow());
            data.add("components", DataComponentMap.CODEC.encodeStart(ops, item.components()).getOrThrow());
            JsonArray foodProperties = new JsonArray();
            FoodProperties.getDefault(item).forEach(p -> foodProperties.add(p.getId().toString()));
            data.add("foodProperties", foodProperties);
            JsonArray beverageProperties = new JsonArray();
            BeverageProperties.getDefault(item).forEach(p -> beverageProperties.add(p.getId().toString()));
            data.add("beverageProperties", beverageProperties);
            JsonArray tags = new JsonArray(); item.builtInRegistryHolder().tags().forEach(t -> tags.add(t.location().toString()));
            data.add("tags", tags);
            JsonArray descriptions = new JsonArray();
            InitTooltips.ITEM_DESCRIPTION_RENDER_DATA.getOrDefault(item, List.of()).forEach(descriptions::add);
            InitTooltips.ITEM_DESCRIPTION_FOR_DETAIL_RENDER_DATA.getOrDefault(item, List.of()).forEach(descriptions::add);
            data.add("descriptionKeys", descriptions);
            if (item instanceof BlockItem blockItem) data.addProperty("block", BuiltInRegistries.BLOCK.getKey(blockItem.getBlock()).toString());
            (id.getNamespace().equals("reverie_dreams") ? items : vanillaItems).add(data);
        }
        root.add("items", items);
        root.add("vanillaItems", vanillaItems);
        JsonObject itemTags = new JsonObject();
        BuiltInRegistries.ITEM.getTags().forEach(tag -> {
            JsonArray values = new JsonArray();
            tag.stream().forEach(holder -> values.add(BuiltInRegistries.ITEM.getKey(holder.value()).toString()));
            itemTags.add(tag.key().location().toString(), values);
        });
        root.add("itemTags", itemTags);
        JsonArray blocks = new JsonArray();
        for (Block block : BuiltInRegistries.BLOCK) {
            Identifier id = BuiltInRegistries.BLOCK.getKey(block);
            if (!id.getNamespace().equals("reverie_dreams")) continue;
            JsonObject data = new JsonObject(); data.addProperty("id", id.toString());
            data.addProperty("implementation", block.getClass().getName());
            data.addProperty("item", BuiltInRegistries.ITEM.getKey(block.asItem()).toString());
            data.addProperty("translationKey", block.getDescriptionId());
            data.addProperty("hardness", block.defaultDestroyTime());
            data.addProperty("blastResistance", block.getExplosionResistance());
            data.add("defaultState", BlockState.CODEC.encodeStart(ops, block.defaultBlockState()).getOrThrow());
            blocks.add(data);
        }
        root.add("blocks", blocks);
        JsonArray entities = new JsonArray();
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
            if (!id.getNamespace().equals("reverie_dreams")) continue;
            JsonObject data = new JsonObject(); data.addProperty("id", id.toString());
            data.addProperty("translationKey", type.getDescriptionId());
            data.addProperty("category", type.getCategory().getName());
            type.getDefaultLootTable().ifPresent(key -> data.addProperty("lootTable", key.identifier().toString()));
            Entity entity = null;
            try {
                entity = type.create(server.overworld(), EntitySpawnReason.COMMAND);
                if (entity != null) {
                    data.addProperty("implementation", entity.getClass().getName());
                    if (entity instanceof LivingEntity living) {
                        JsonObject attributes = new JsonObject();
                        for (var attribute : List.of(Attributes.MAX_HEALTH, Attributes.MOVEMENT_SPEED, Attributes.FLYING_SPEED,
                                Attributes.ATTACK_DAMAGE, Attributes.FOLLOW_RANGE, Attributes.ARMOR, Attributes.ARMOR_TOUGHNESS,
                                Attributes.KNOCKBACK_RESISTANCE, Attributes.SCALE)) {
                            var instance = living.getAttribute(attribute);
                            if (instance != null) attributes.addProperty(BuiltInRegistries.ATTRIBUTE.getKey(attribute.value()).toString(), instance.getBaseValue());
                        }
                        data.add("attributes", attributes);
                        JsonObject equipment = new JsonObject();
                        for (var slot : EquipmentSlot.values()) {
                            ItemStack held = living.getItemBySlot(slot);
                            if (!held.isEmpty()) equipment.add(slot.getName(), ItemStack.CODEC.encodeStart(ops, held).getOrThrow());
                        }
                        data.add("equipment", equipment);
                    }
                }
            } catch (Throwable failure) { errors.add("entity " + id + ": " + failure); }
            finally { if (entity != null) entity.discard(); }
            entities.add(data);
        }
        root.add("entities", entities);
        JsonArray tabs = new JsonArray();
        for (var entry : BuiltInRegistries.CREATIVE_MODE_TAB.entrySet()) {
            if (!entry.getKey().identifier().getNamespace().equals("reverie_dreams")) continue;
            var tab = entry.getValue();
            tab.buildContents(new CreativeModeTab.ItemDisplayParameters(FeatureFlags.DEFAULT_FLAGS, false, server.registryAccess()));
            JsonObject data = new JsonObject(); data.addProperty("id", entry.getKey().identifier().toString());
            data.add("name", ComponentSerialization.CODEC.encodeStart(ops, tab.getDisplayName()).getOrThrow());
            JsonArray stacks = new JsonArray();
            for (ItemStack stack : tab.getDisplayItems()) {
                JsonObject exported = new JsonObject();
                exported.addProperty("id", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
                exported.addProperty("count", stack.getCount());
                exported.add("name", ComponentSerialization.CODEC.encodeStart(ops, stack.getHoverName()).getOrThrow());
                JsonObject patch = new JsonObject();
                for (var component : stack.getComponentsPatch().entrySet()) {
                    DataComponentType componentType = component.getKey();
                    if (componentType.isTransient()) continue;
                    String key = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(componentType).toString();
                    Optional value = component.getValue();
                    if (value.isEmpty()) { patch.add("!" + key, new JsonObject()); continue; }
                    var encoded = componentType.codec().encodeStart(ops, value.get());
                    if (encoded.result().isPresent()) patch.add(key, (JsonElement) encoded.result().get());
                    else if (value.get() instanceof PotionContents potion) {
                        JsonObject contents = new JsonObject();
                        potion.potion().flatMap(Holder::unwrapKey).ifPresent(k -> contents.addProperty("potion", k.identifier().toString()));
                        JsonArray effects = new JsonArray();
                        potion.getAllEffects().forEach(effect -> {
                            JsonObject e = new JsonObject();
                            e.addProperty("id", BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect().value()).toString());
                            e.addProperty("duration", effect.getDuration()); e.addProperty("amplifier", effect.getAmplifier()); effects.add(e);
                        });
                        contents.add("effects", effects); patch.add(key, contents);
                    } else { throw new IllegalStateException("Cannot export creative stack " + exported + " component " + key + ": " + encoded.error()); }
                }
                if (!patch.isEmpty()) exported.add("components", patch);
                stacks.add(exported);
            }
            data.add("stacks", stacks); tabs.add(data);
        }
        root.add("tabs", tabs);
        JsonArray recipes = new JsonArray();
        for (var entry : RecipeManager.RECIPE_TYPES.entrySet()) {
            BaseRecipeType recipeType = entry.getValue();
            for (Object value : recipeType.getRegistryView().entrySet()) {
                var recipeEntry = (Map.Entry<Identifier, BaseRecipe>) value;
                JsonObject data = new JsonObject(); data.addProperty("id", recipeEntry.getKey().toString());
                data.addProperty("type", entry.getKey().toString());
                data.addProperty("virtual", recipeEntry.getValue().isVirtual());
                data.add("data", NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, recipeType.encodeTag(recipeEntry.getValue())));
                recipes.add(data);
            }
        }
        root.add("customRecipes", recipes);
        root.add("errors", errors);
        Path output = Path.of(System.getProperty("reverie.wiki.output")).toAbsolutePath().normalize();
        Path expected = Path.of(System.getProperty("reverie.wiki.workspace")).toAbsolutePath().normalize().resolve("document/data/runtime.json");
        if (!output.equals(expected)) throw new IllegalArgumentException("Unexpected wiki export destination " + output);
        Files.createDirectories(output.getParent());
        Path temp = output.resolveSibling("runtime.json.tmp"); Files.writeString(temp, gson.toJson(root));
        Files.move(temp, output, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("WIKI_COUNTS items=" + items.size() + " blocks=" + blocks.size() + " entities=" + entities.size() + " recipes=" + recipes.size() + " tabs=" + tabs.size());
        if (!errors.isEmpty()) System.out.println("WIKI_EXPORT_WARNINGS " + errors);
    }
}
