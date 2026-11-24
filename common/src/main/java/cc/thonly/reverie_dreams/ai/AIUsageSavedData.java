package cc.thonly.reverie_dreams.ai;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.Identifier;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public final class AIUsageSavedData extends SavedData {
    public static final Codec<AIUsageSavedData> CODEC = Codec.PASSTHROUGH.comapFlatMap(dynamic -> {
        try {
            var value = dynamic.convert(JsonOps.INSTANCE).getValue();
            var state = new AIUsageSavedData();
            // Read the initial string prototype, but write a compound: large ledgers exceed NBT's string limit.
            state.budget.load(value.isJsonObject() ? value.getAsJsonObject() : JsonParser.parseString(value.getAsString()).getAsJsonObject());
            return DataResult.success(state);
        } catch (RuntimeException error) {
            return DataResult.error(() -> "Invalid AI usage ledger; restore its backup before enabling AI");
        }
    }, state -> new Dynamic<>(JsonOps.INSTANCE, state.budget.save()));
    public static final SavedDataType<AIUsageSavedData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("reverie_dreams", "ai_usage"), AIUsageSavedData::new, CODEC, DataFixTypes.LEVEL);
    private final AIUsageBudget budget = new AIUsageBudget(this::setDirty);

    public AIUsageBudget budget() {
        return budget;
    }
}
