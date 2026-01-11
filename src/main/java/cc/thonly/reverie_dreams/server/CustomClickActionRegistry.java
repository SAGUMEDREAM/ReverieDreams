package cc.thonly.reverie_dreams.server;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.npc.NPCRole;
import cc.thonly.reverie_dreams.dialog.DialogPlayer;
import cc.thonly.reverie_dreams.entity.npc.NPCRoleFastEntity;
import cc.thonly.reverie_dreams.item.builder.RoleCard;
import cc.thonly.reverie_dreams.item.template.RoleCardItem;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class CustomClickActionRegistry {
    private static final CustomClickActionRegistry IMPl = new CustomClickActionRegistry();
    private final Map<Identifier, Consumer<ServerboundCustomClickActionPacket>> entries = new Object2ObjectOpenHashMap<>(256);

    public static void registerCustomAction(Identifier location, Consumer<ServerboundCustomClickActionPacket> consumer) {
        IMPl.entries.put(location, consumer);
    }

    public static void handle(ServerboundCustomClickActionPacket packet) {
        CustomClickActionRegistry impl = IMPl;
        Consumer<ServerboundCustomClickActionPacket> packetConsumer = impl.entries.get(packet.id());
        if (packetConsumer == null) {
            return;
        }
        packetConsumer.accept(packet);
    }

    public static void registerActions() {
        registerCustomAction(ReverieDreams.id("role/summon"), CustomClickActionRegistry::handleRoleCardDialog);
        registerCustomAction(ReverieDreams.id("stop/dialog_video"), CustomClickActionRegistry::handleStopVideoDialog);

    }

    public static void handleRoleCardDialog(ServerboundCustomClickActionPacket packet) {
        Optional<Tag> payload = packet.payload();
        if (payload.isEmpty()) {
            return;
        }
        Tag element = payload.get();
        if (!(element instanceof CompoundTag compound)) {
            return;
        }
        Optional<String> siOptional = compound.getString("session_id");
        Optional<String> eiOptional = compound.getString("entity_id");
        if (siOptional.isEmpty()) {
            return;
        }
        if (eiOptional.isEmpty()) {
            return;
        }
        String sessionId = siOptional.get();
        String entityId = eiOptional.get();
        RoleCardItem.UsingData usingData = RoleCardItem.USING_DATA_MAP.get(sessionId);
        if (usingData == null) {
            return;
        }
        if (entityId.equals("random")) {
            RoleCard roleCard = usingData.getRoleCard();
            Optional<NPCRole> roleWrapper = roleCard.random();
            if (roleWrapper.isPresent()) {
                ServerPlayer player = usingData.getPlayer();
                ServerLevel world = usingData.getWorld();
                ItemStack itemStack = usingData.getItemStack();
                NPCRole role = roleWrapper.get();
                itemStack.consume(1, player);
                EntityType<NPCRoleFastEntity> entityType = role.get();
                entityType.spawn(world, usingData.getBlockPos(), EntitySpawnReason.SPAWN_ITEM_USE);

                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BUCKET_FILL, player.getSoundSource(), 2.0f, 1.0f);
            }
            RoleCardItem.USING_DATA_MAP.remove(sessionId);
            return;
        }
        Identifier identifier = Identifier.tryParse(entityId);
        if (identifier == null) {
            return;
        }
        NPCRole role = usingData.getId2Role().get(identifier);
        if (role == null) {
            return;
        }
        ServerPlayer player = usingData.getPlayer();
        ServerLevel world = usingData.getWorld();
        ItemStack itemStack = usingData.getItemStack();
        itemStack.consume(1, player);
        EntityType<NPCRoleFastEntity> entityType = role.get();
        entityType.spawn(world, usingData.getBlockPos(), EntitySpawnReason.SPAWN_ITEM_USE);

        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BUCKET_FILL, player.getSoundSource(), 2.0f, 1.0f);
        RoleCardItem.USING_DATA_MAP.remove(sessionId);
    }

    public static void handleStopVideoDialog(ServerboundCustomClickActionPacket packet) {
        Optional<Tag> payload = packet.payload();
        if (payload.isEmpty()) {
            return;
        }
        Tag element = payload.get();
        if (!(element instanceof CompoundTag compound)) {
            return;
        }
        Optional<String> uidOptional = compound.getString("uid");
        if (uidOptional.isEmpty()) {
            return;
        }
        String uid = uidOptional.get();
        for (Map.Entry<String, DialogPlayer> entry : DialogPlayer.INSTANCES.entrySet()) {
            DialogPlayer dialogPlayer = entry.getValue();
            ServerPlayer player = dialogPlayer.getPlayer();
            if (player.getStringUUID().equals(uid)) {
                dialogPlayer.remove();
                SoundEvent soundEvent = dialogPlayer.getSoundEvent();
                if (soundEvent != null) {
                    ClientboundStopSoundPacket stopSoundS2CPacket = new ClientboundStopSoundPacket(soundEvent.location(), SoundSource.PLAYERS);
                    player.connection.send(stopSoundS2CPacket);
                }
            }
        }
    }
}
