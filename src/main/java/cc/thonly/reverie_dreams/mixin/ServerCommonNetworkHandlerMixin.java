package cc.thonly.reverie_dreams.mixin;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.dialog.DialogPlayer;
import cc.thonly.reverie_dreams.entity.base.NPCEntity;
import cc.thonly.reverie_dreams.entity.npc.NPCRole;
import cc.thonly.reverie_dreams.item.RoleCard;
import cc.thonly.reverie_dreams.item.RoleCardItem;
import cc.thonly.reverie_dreams.sound.JukeboxSongInit;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.CustomClickActionC2SPacket;
import net.minecraft.network.packet.s2c.common.ShowDialogS2CPacket;
import net.minecraft.network.packet.s2c.play.StopSoundS2CPacket;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Mixin(ServerCommonNetworkHandler.class)
public abstract class ServerCommonNetworkHandlerMixin {
    @Shadow @Final protected MinecraftServer server;

    @Inject(method = "onCustomClickAction", at = @At("TAIL"))
    private void handleCustomClick(CustomClickActionC2SPacket packet, CallbackInfo ci) {
        try {
            if (!packet.id().getNamespace().equals(Touhou.MOD_ID)) {
                return;
            }
            switch (packet.id().getPath()) {
                case "role/summon"-> {
                    Optional<NbtElement> payload = packet.payload();
                    if (payload.isEmpty()) {
                        return;
                    }
                    NbtElement element = payload.get();
                    if (!(element instanceof NbtCompound compound)) {
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
                            ServerPlayerEntity player = usingData.getPlayer();
                            ServerWorld world = usingData.getWorld();
                            ItemStack itemStack = usingData.getItemStack();
                            NPCRole role = roleWrapper.get();
                            itemStack.decrementUnlessCreative(1, player);
                            EntityType<NPCEntity> entityType = role.get();
                            entityType.spawn(world, usingData.getBlockPos(), SpawnReason.SPAWN_ITEM_USE);

                            world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_BUCKET_FILL, player.getSoundCategory(), 2.0f, 1.0f);
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
                    ServerPlayerEntity player = usingData.getPlayer();
                    ServerWorld world = usingData.getWorld();
                    ItemStack itemStack = usingData.getItemStack();
                    itemStack.decrementUnlessCreative(1, player);
                    EntityType<NPCEntity> entityType = role.get();
                    entityType.spawn(world, usingData.getBlockPos(), SpawnReason.SPAWN_ITEM_USE);

                    world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_BUCKET_FILL, player.getSoundCategory(), 2.0f, 1.0f);
                    RoleCardItem.USING_DATA_MAP.remove(sessionId);
                }
                case "stop/dialog_video"-> {
                    Optional<NbtElement> payload = packet.payload();
                    if (payload.isEmpty()) {
                        return;
                    }
                    NbtElement element = payload.get();
                    if (!(element instanceof NbtCompound compound)) {
                        return;
                    }
                    Optional<String> uidOptional = compound.getString("uid");
                    if (uidOptional.isEmpty()) {
                        return;
                    }
                    String uid = uidOptional.get();
                    for (Map.Entry<String, DialogPlayer> entry : DialogPlayer.INSTANCES.entrySet()) {
                        DialogPlayer dialogPlayer = entry.getValue();
                        ServerPlayerEntity player = dialogPlayer.getPlayer();
                        if (player.getUuidAsString().equals(uid)) {
                            dialogPlayer.remove();
                            StopSoundS2CPacket stopSoundS2CPacket = new StopSoundS2CPacket(JukeboxSongInit.BAD_APPLE.getJukeboxSongRegistryKey().getValue(), SoundCategory.PLAYERS);
                            player.networkHandler.sendPacket(stopSoundS2CPacket);
                        }
                    }
                }
            }

        } catch (Exception err) {
            log.error("Can't parse custom click action packet");
        }
    }
}
