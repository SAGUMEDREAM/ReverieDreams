package cc.thonly.reverie_dreams.ai.npc;

import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Authorizations are tied to the issuing owner and dimension, not merely to coordinates.
 */
public final class NPCContainerAccess {
    private static final Logger LOGGER = LoggerFactory.getLogger(NPCContainerAccess.class);
    private final Map<String, UUID> allowed = new LinkedHashMap<>();
    private JsonObject retainedRecords = new JsonObject();
    private String unreadableData;

    private String key(NPCSimpleEntity npc, BlockPos pos) {
        return npc.level().dimension().identifier() + "|" + pos.asLong();
    }

    public boolean authorized(NPCSimpleEntity npc, ServerPlayer player, BlockPos pos) {
        return npc.isOwnedBy(player) && player.getUUID().equals(allowed.get(key(npc, pos)));
    }

    private BaseContainerBlockEntity container(NPCSimpleEntity npc, ServerPlayer player, BlockPos pos) {
        if (!npc.level().hasChunkAt(pos) || npc.level() != player.level()
                || !player.mayInteract((ServerLevel) npc.level(), pos))
            return null;
        var be = npc.level().getBlockEntity(pos);
        if (!(be instanceof ChestBlockEntity || be instanceof BarrelBlockEntity))
            return null;
        if (be instanceof RandomizableContainerBlockEntity randomizable
                && randomizable.getLootTable() != null)
            return null;
        var container = (BaseContainerBlockEntity) be;
        return container.canOpen(player) ? container : null;
    }

    public String authorize(NPCSimpleEntity npc, ServerPlayer player, BlockPos pos, boolean grant) {
        if (!npc.isOwnedBy(player))
            return "Denied: only the owner can authorize a container.";
        if (unreadableData != null)
            return "Failed: container authorization data is unreadable and preserved; repair the stored field first.";
        if (!grant) {
            allowed.remove(key(npc, pos));
            retainedRecords.remove(key(npc, pos));
            return "Container authorization revoked.";
        }
        if (pos.distToCenterSqr(player.position()) > 36 || pos.distToCenterSqr(npc.position()) > 256
                || container(npc, player, pos) == null)
            return "Denied: stand within 6 blocks of an unlocked, already-opened chest/barrel; NPC must be within 16 blocks.";
        if (allowed.size() >= 8 && !allowed.containsKey(key(npc, pos)))
            return "Failed: at most 8 containers may be authorized.";
        allowed.put(key(npc, pos), player.getUUID());
        retainedRecords.remove(key(npc, pos));
        return "Container authorized for this NPC and owner.";
    }

    public String inspect(NPCSimpleEntity npc, ServerPlayer player, BlockPos pos) {
        var container = container(npc, player, pos);
        if (!authorized(npc, player, pos) || container == null || pos.distToCenterSqr(npc.position()) > 256) {
            return "Denied: container not authorized, unavailable or too far away.";
        }
        JsonArray inventory = new JsonArray();
        for (int i = 0; i < container.getContainerSize(); i++) {
            var stack = container.getItem(i);
            if (stack.isEmpty())
                continue;
            JsonObject slot = new JsonObject();
            slot.addProperty("slot", i);
            slot.addProperty("item", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
            slot.addProperty("count", stack.getCount());
            inventory.add(slot);
        }
        return inventory.toString();
    }

    public String transfer(NPCSimpleEntity npc, ServerPlayer player, BlockPos pos, String direction, String item, int count) {
        var container = container(npc, player, pos);
        Identifier itemId = Identifier.tryParse(item);
        if (!authorized(npc, player, pos) || container == null || pos.distToCenterSqr(npc.position()) > 9) {
            return "Denied: transfer requires an authorized, unlocked container within 3 blocks.";
        }
        if (count < 1 || count > 1024 || itemId == null || !BuiltInRegistries.ITEM.containsKey(itemId)
                || !(direction.equals("deposit") || direction.equals("withdraw")))
            return "Failed: invalid transfer arguments.";
        boolean deposit = direction.equals("deposit");
        int moved = NPCInventoryTransfer.move(deposit ? npc.getInventory() : container,
                deposit ? container : npc.getInventory(), BuiltInRegistries.ITEM.getValue(itemId), count,
                slot -> !deposit || slot < 18 || slot >= 22, slot -> deposit || slot < 18);
        JsonObject result = new JsonObject();
        result.addProperty("moved", moved);
        result.addProperty("remaining", count - moved);
        result.addProperty("item", itemId.toString());
        result.addProperty("direction", direction);
        return result.toString();
    }

    public void save(ValueOutput out) {
        out.putString("ChatContainerAccess", saveEncoded());
    }

    String saveEncoded() {
        if (unreadableData != null)
            return unreadableData;
        JsonObject data = retainedRecords.deepCopy();
        allowed.forEach((key, owner) -> data.addProperty(key, owner.toString()));
        return data.toString();
    }

    public void load(ValueInput in) {
        loadEncoded(in.getStringOr("ChatContainerAccess", "{}"));
    }

    void loadEncoded(String data) {
        allowed.clear();
        retainedRecords = new JsonObject();
        unreadableData = null;
        try {
            retainedRecords = JsonParser.parseString(data).getAsJsonObject().deepCopy();
        } catch (RuntimeException failure) {
            unreadableData = data;
            LOGGER.error("NPC container authorization root is unreadable; preserving the original field without granting access", failure);
            return;
        }
        for (var entry : retainedRecords.deepCopy().entrySet()) {
            try {
                if (allowed.size() == 8)
                    break;
                allowed.put(entry.getKey(), UUID.fromString(entry.getValue().getAsString()));
                retainedRecords.remove(entry.getKey());
            } catch (RuntimeException failure) {
                LOGGER.error("NPC container authorization {} is unreadable; preserving it without granting access", entry.getKey(), failure);
            }
        }
    }
}
