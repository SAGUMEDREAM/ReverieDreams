package cc.thonly.reverie_dreams.mixin;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.dialog.DialogPlayer;
import cc.thonly.reverie_dreams.entity.npc.AbstractNPCEntity;
import cc.thonly.reverie_dreams.entity.npc.NPCRole;
import cc.thonly.reverie_dreams.entity.npc.NPCRoleFastEntity;
import cc.thonly.reverie_dreams.item.builder.RoleCard;
import cc.thonly.reverie_dreams.item.template.RoleCardItem;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class ServerCommonNetworkHandlerMixin {
    @Shadow @Final protected MinecraftServer server;

    @Inject(method = "handleCustomClickAction", at = @At("TAIL"))
    private void handleCustomClick(ServerboundCustomClickActionPacket packet, CallbackInfo ci) {
        try {
            if (!packet.id().getNamespace().equals(Touhou.MOD_ID)) {
                return;
            }
            switch (packet.id().getPath()) {
                case "role/summon"-> {
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
                    ResourceLocation identifier = ResourceLocation.tryParse(entityId);
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
                case "stop/dialog_video"-> {
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

        } catch (Exception err) {
            log.error("Can't parse custom click action packet");
        }
    }
}
