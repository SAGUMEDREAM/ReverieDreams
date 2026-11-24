package cc.thonly.reverie_dreams.networking.payload;

import cc.thonly.reverie_dreams.ReverieDreams;
import io.netty.handler.codec.CodecException;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtException;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public record RecipeManagerSyncPacket(Identifier typeId,
                                      CompoundTag data
) implements CustomPacketPayload {
    public static final Identifier ID = ReverieDreams.id("recipe_manager_sync");
    public static final Type<RecipeManagerSyncPacket> PACKET_ID = new Type<>(ID);
    public static final Type<RecipeManagerSyncPacket> BATCH_PACKET_ID = new Type<>(ReverieDreams.id("recipe_manager_sync_batch"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RecipeManagerSyncPacket> CODEC = StreamCodec.ofMember(RecipeManagerSyncPacket::write, RecipeManagerSyncPacket::read);
    // Leave room for the NBT decoder's bookkeeping and the custom payload envelope.
    private static final int BATCH_BUDGET = 512 * 1024;
    private static final int SINGLE_PACKET_BUDGET = 1024 * 1024;
    private static final int BATCH_OVERHEAD = 1024;
    private static final int MAX_BATCHES = 128;

    public static RecipeManagerSyncPacket read(RegistryFriendlyByteBuf buf) {
        Identifier typeId = buf.readIdentifier();
        try {
            CompoundTag data = buf.readNbt();
            return new RecipeManagerSyncPacket(typeId, data == null ? new CompoundTag() : data);
        } catch (NbtException | CodecException error) {
            // Architectury provides a buffer for this custom payload alone.
            buf.skipBytes(buf.readableBytes());
            log.warn("Ignoring unreadable recipe snapshot for {}: {}", typeId, error.getMessage());
            return new RecipeManagerSyncPacket(typeId, new CompoundTag());
        }
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeIdentifier(this.typeId());
        buf.writeNbt(this.data);
    }

    public static List<RecipeManagerSyncPacket> split(Identifier typeId, CompoundTag snapshot) {
        if (snapshot.sizeInBytes() <= SINGLE_PACKET_BUDGET) {
            return List.of(new RecipeManagerSyncPacket(typeId, snapshot));
        }

        List<ListTag> batches = new ArrayList<>();
        ListTag current = new ListTag();
        int estimatedBytes = BATCH_OVERHEAD;
        for (Tag recipe : recipes(snapshot)) {
            int recipeBytes = recipe.sizeInBytes() + 8;
            if (recipeBytes > SINGLE_PACKET_BUDGET - BATCH_OVERHEAD) {
                throw new IllegalArgumentException("Recipe entry exceeds the sync batch budget in " + typeId);
            }
            if (!current.isEmpty() && estimatedBytes + recipeBytes > BATCH_BUDGET) {
                batches.add(current);
                current = new ListTag();
                estimatedBytes = BATCH_OVERHEAD;
            }
            current.add(recipe);
            estimatedBytes += recipeBytes;
        }
        batches.add(current);
        if (batches.size() > MAX_BATCHES) {
            throw new IllegalArgumentException("Too many recipe sync batches for " + typeId);
        }

        List<RecipeManagerSyncPacket> packets = new ArrayList<>(batches.size());
        for (int index = 0; index < batches.size(); index++) {
            CompoundTag data = snapshot(typeId, batches.get(index));
            data.putInt("part", index);
            data.putInt("parts", batches.size());
            packets.add(new RecipeManagerSyncPacket(typeId, data));
        }
        return packets;
    }

    private static ListTag recipes(CompoundTag snapshot) {
        return snapshot.getCompoundOrEmpty("data").getListOrEmpty("recipes");
    }

    private static CompoundTag snapshot(Identifier typeId, ListTag recipes) {
        CompoundTag root = new CompoundTag();
        root.putString("type", typeId.toString());
        CompoundTag data = new CompoundTag();
        data.put("recipes", recipes);
        root.put("data", data);
        return root;
    }

    public boolean isBatch() {
        return this.data.contains("parts");
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return this.isBatch() ? BATCH_PACKET_ID : PACKET_ID;
    }

    /** Receives ordered snapshots on the client thread. Incomplete lists never reach the recipe registry. */
    public static final class Assembler {
        @Nullable
        private Identifier typeId;
        @Nullable
        private ListTag recipes;
        private int expectedParts;
        private int nextPart;

        @Nullable
        public CompoundTag accept(RecipeManagerSyncPacket packet) {
            var entries = packet.data.getCompound("data").flatMap(data -> data.getList("recipes"));
            if (!packet.typeId.toString().equals(packet.data.getStringOr("type", "")) || entries.isEmpty()) {
                this.clear();
                throw new IllegalArgumentException("Missing or mismatched recipe snapshot for " + packet.typeId);
            }
            if (!packet.isBatch()) {
                this.clear();
                return packet.data;
            }

            int parts = packet.data.getIntOr("parts", 0);
            int part = packet.data.getIntOr("part", -1);
            if (part == 0 && parts > 0 && parts <= MAX_BATCHES) {
                this.typeId = packet.typeId;
                this.recipes = new ListTag();
                this.expectedParts = parts;
                this.nextPart = 0;
            }
            if (this.recipes == null || !packet.typeId.equals(this.typeId)
                    || parts != this.expectedParts || part != this.nextPart || part >= parts) {
                this.clear();
                throw new IllegalArgumentException("Unexpected recipe sync batch " + part + " of " + parts);
            }
            this.recipes.addAll(entries.get());
            if (++this.nextPart < parts) {
                return null;
            }
            CompoundTag completed = snapshot(packet.typeId, this.recipes);
            this.clear();
            return completed;
        }

        public void clear() {
            this.typeId = null;
            this.recipes = null;
            this.expectedParts = 0;
            this.nextPart = 0;
        }
    }
}
