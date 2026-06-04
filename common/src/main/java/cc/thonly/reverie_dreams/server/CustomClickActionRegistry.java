package cc.thonly.reverie_dreams.server;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.npc.NPCRole;
import cc.thonly.reverie_dreams.dialog.DialogPlayer;
import cc.thonly.reverie_dreams.dialog.DialogPlayerManager;
import cc.thonly.reverie_dreams.api.entity.type.ChatAIEntity;
import cc.thonly.reverie_dreams.entity.npc.NPCRoleFastEntity;
import cc.thonly.reverie_dreams.item.base.RoleCard;
import cc.thonly.reverie_dreams.item.template.RoleCardItem;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

@SuppressWarnings("@ALL")
public class CustomClickActionRegistry {
    private static final Map<Identifier, Consumer<ServerboundCustomClickActionPacket>> REGISTRY = new Object2ObjectOpenHashMap<>(256);
    public static final Identifier STOP_DIALOG_VIDEO_KEY = ReverieDreams.id("stop/dialog_video");
    public static final Identifier ROLE_SUMMON_KEY = ReverieDreams.id("role/summon");
    public static final Identifier CHAT_KEY = ReverieDreams.id("role/chat");
    public static final Holder<Consumer<ServerboundCustomClickActionPacket>> STOP_DIALOG_VIDEO = registerCustomAction(ROLE_SUMMON_KEY, CustomClickActionRegistry::handleRoleCardDialog);
    public static final Holder<Consumer<ServerboundCustomClickActionPacket>> ROLE_SUMMON = registerCustomAction(STOP_DIALOG_VIDEO_KEY, CustomClickActionRegistry::handleStopVideoDialog);
    public static final Holder<Consumer<ServerboundCustomClickActionPacket>> CHAT = registerCustomAction(CHAT_KEY, CustomClickActionRegistry::handleChat);

    public static Holder<Consumer<ServerboundCustomClickActionPacket>> registerCustomAction(Identifier location, Consumer<ServerboundCustomClickActionPacket> consumer) {
        REGISTRY.put(location, consumer);
        return Holder.direct(consumer);
    }

    public static void handle(ServerboundCustomClickActionPacket packet) {
        Consumer<ServerboundCustomClickActionPacket> packetConsumer = REGISTRY.get(packet.id());
        if (packetConsumer == null) {
            return;
        }
        packetConsumer.accept(packet);
    }

    public static void initialize() {

    }

    public static void handleChat(ServerboundCustomClickActionPacket packet) {
        MinecraftServer server = ReverieDreams.getServer();
        if (server == null) {
//            System.out.println(0);
            return;
        }
        Optional<Tag> payload = packet.payload();
        if (payload.isEmpty()) {
//            System.out.println(1);
            return;
        }
        Tag element = payload.get();
        if (!(element instanceof CompoundTag compound)) {
//            System.out.println(2);
            return;
        }
        String player_uuid = compound.getStringOr("player_uuid", "");
        String entity_uuid = compound.getStringOr("entity_uuid", "");
        String userInput = compound.getStringOr("user_input", "");
        if (userInput.isEmpty() || player_uuid.isEmpty() || entity_uuid.isEmpty()) {
//            System.out.println(3);
            return;
        }
        ServerPlayer player = server.getPlayerList().getPlayer(UUID.fromString(player_uuid));
        if (player == null || player.hasDisconnected()) {
//            System.out.println(4);
            return;
        }
        ChatAIEntity chatAIEntity = null;
        for (ServerLevel level : server.getAllLevels()) {
            Entity entity = level.getEntity(UUID.fromString(entity_uuid));
            if (!(entity instanceof ChatAIEntity aiEntity)) {
                continue;
            }
//            System.out.println(5);
            chatAIEntity = aiEntity;
            break;
        }
        if (chatAIEntity == null) {
//            System.out.println(6);
            return;
        }
//        System.out.println(7);
        chatAIEntity.send(player, chatAIEntity.encapsulateUserInputContent(player, userInput));
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
                EntityType<NPCRoleFastEntity> entityType = role.get().value();
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
        EntityType<NPCRoleFastEntity> entityType = role.get().value();
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
        for (Map.Entry<String, DialogPlayer> entry : DialogPlayerManager.PLAYER_INSTANCES.entrySet()) {
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
