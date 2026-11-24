package cc.thonly.reverie_dreams.fabric.datagen;

import cc.thonly.reverie_dreams.fabric.datagen.generator.AbstractJsonElementWriterProvider;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.data.models.ItemModelOutput;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.io.IOException;

/** Keep item-only transforms after datagen without changing any placed block model. */
final class DecorationItemDisplays {
    private static final JsonObject MANIFEST = load();
    private static JsonObject load() {
        try (var reader = new InputStreamReader(Objects.requireNonNull(DecorationItemDisplays.class.getResourceAsStream(
                "/reverie_dreams/datagen/decoration_item_displays.json")), StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException error) { throw new IllegalStateException("Decoration item displays", error); }
    }
    private static Identifier replacement(Item item) {
        var id = BuiltInRegistries.ITEM.getKey(item);
        if (!id.getNamespace().equals("reverie_dreams")) return null;
        var value = MANIFEST.getAsJsonObject("replacements").get(id.getPath());
        return value == null ? null : Identifier.parse(value.getAsString());
    }
    static ItemModelOutput wrap(ItemModelOutput delegate) {
        return new ItemModelOutput() {
            @Override public void accept(Item item, ItemModel.Unbaked model, ClientItem.Properties properties) {
                var target = replacement(item);
                delegate.accept(item, target == null ? model : ItemModelUtils.plainModel(target), properties);
            }
            @Override public void copy(Item donor, Item acceptor) {
                var target = replacement(acceptor);
                if (target == null) delegate.copy(donor, acceptor);
                else delegate.accept(acceptor, ItemModelUtils.plainModel(target));
            }
        };
    }
    static void writeCustomItems(JsonElementWriterProvider provider) {
        for (var entry : MANIFEST.getAsJsonArray("json_items")) {
            String id = entry.getAsString();
            JsonObject model = new JsonObject(); model.addProperty("type", "minecraft:model");
            model.addProperty("model", MANIFEST.getAsJsonObject("replacements").get(id).getAsString());
            JsonObject item = new JsonObject(); item.add("model", model);
            provider.addElement(AbstractJsonElementWriterProvider.Type.ASSETS,
                    Identifier.fromNamespaceAndPath("reverie_dreams", id), "items", item);
        }
    }
    private DecorationItemDisplays() {}
}
