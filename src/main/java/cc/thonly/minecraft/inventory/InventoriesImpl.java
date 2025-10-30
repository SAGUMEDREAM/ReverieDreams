package cc.thonly.minecraft.inventory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class InventoriesImpl {
    public static final Gson GSON = new Gson();

    public static void writeView(ValueOutput view, String key, NonNullList<ItemStack> stacks) {
        String json;
        try {
            json = toJson(stacks);
        } catch (Exception err) {
            log.error("Can't write role view", err);
            json = "[]";
        }
        view.putString(key, json);
    }

    public static void readView(ValueInput view, String key, NonNullList<ItemStack> stacks) {
        Optional<String> jsonOptional = view.getString(key);
        if (jsonOptional.isPresent()) {
            String jsonString = jsonOptional.get();
            Optional<List<Slot2ItemStack>> slot2ItemStacksOptional = parseJson(jsonString);
            if (slot2ItemStacksOptional.isPresent()) {
                List<Slot2ItemStack> slot2ItemStacks = slot2ItemStacksOptional.get();
                for (Slot2ItemStack slot2ItemStack : slot2ItemStacks) {
                    int index = slot2ItemStack.index();
                    if (index < 0 || index >= stacks.size()) continue;
                    stacks.set(slot2ItemStack.index(), slot2ItemStack.itemStack());
                }
            }
        }
    }

    public static List<Slot2ItemStack> toSlot2ItemStack(NonNullList<ItemStack> stacks) {
        List<Slot2ItemStack> list = new ArrayList<>();
        for (int i = 0; i < stacks.size(); i++) {
            list.add(new Slot2ItemStack(i, stacks.get(i)));
        }
        return list;
    }

    public static String toJson(NonNullList<ItemStack> stacks) {
        return toJson(toSlot2ItemStack(stacks));
    }

    public static String toJson(List<Slot2ItemStack> stacks) {
        DataResult<JsonElement> dataResult = Slot2ItemStack.LIST_CODEC.encodeStart(JsonOps.INSTANCE, stacks);
        Optional<JsonElement> result = dataResult.result();
        if (result.isPresent()) {
            JsonElement element = result.get();
            return GSON.toJson(element);
        }
        try {
            result.get();
        } catch (Exception e) {
            log.error("Error", e);
        }
        return "[]";
    }

    public static Optional<List<Slot2ItemStack>> parseJson(String json) {
        if (json == null) {
            return Optional.empty();
        }
        if (json.isEmpty()) {
            return Optional.empty();
        }
        JsonElement jsonElement = JsonParser.parseString(json);
        Dynamic<JsonElement> input = new Dynamic<>(JsonOps.INSTANCE, jsonElement);
        DataResult<List<Slot2ItemStack>> parse = Slot2ItemStack.LIST_CODEC.parse(input);
        return parse.result();
    }

}
