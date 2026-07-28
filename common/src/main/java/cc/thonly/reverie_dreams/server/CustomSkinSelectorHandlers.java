package cc.thonly.reverie_dreams.server;

import cc.thonly.reverie_dreams.data.skin.CustomType;
import cc.thonly.reverie_dreams.data.skin.SkinType;
import cc.thonly.reverie_dreams.entity.npc.NPCRoleEntity;
import cc.thonly.reverie_dreams.registry.RegistryImpls;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.Optional;

@SuppressWarnings("resource")
public class CustomSkinSelectorHandlers {

    public static void handleAction(ServerPlayer player, ServerboundCustomClickActionPacket packet) {
        if (player == null) {
            return;
        }

        Optional<Tag> payload = packet.payload();
        if (payload.isEmpty()) {
            return;
        }

        Tag element = payload.get();
        if (!(element instanceof CompoundTag compound)) {
            return;
        }

        String skinIdStr = compound.getStringOr("skin_id", "");
        if (skinIdStr.isEmpty()) {
            return;
        }

        String dimKeyStr = compound.getStringOr("dim_key", "");
        if (dimKeyStr.isEmpty()) {
            return;
        }

        Identifier skinId = Identifier.tryParse(skinIdStr);
        if (skinId == null) {
            return;
        }

        Identifier dimKey = Identifier.tryParse(dimKeyStr);
        if (dimKey == null) {
            return;
        }

        int targetId = compound.getIntOr("target_id", -1);
        if (targetId < 0) {
            return;
        }

        ServerLevel level = player.level().getServer().getLevel(ResourceKey.create(Registries.DIMENSION, dimKey));
        if (level == null) {
            return;
        }

        var entity = level.getEntity(targetId);
        if (entity == null) {
            return;
        }

        applySkin(player, entity, skinId);
    }

    private static void applySkin(ServerPlayer player, Entity target, Identifier skinId) {
        if (!(target instanceof NPCRoleEntity npcRoleEntity)) {
            return;
        }
        SkinType skinType = RegistryImpls.CUSTOM_SKIN_TYPE.getValue(skinId);
        if (skinType instanceof CustomType customType) {
            npcRoleEntity.setSkinType(customType);
            npcRoleEntity.setCustomName(Component.translatable(customType.getDescriptionId()));
            npcRoleEntity.refreshDimensions();
        } else if (skinId.equals(SkinType.RECOVERY)) {
            SkinType defaultSkinType = npcRoleEntity.getDefaultSkinType();
            npcRoleEntity.setSkinType(defaultSkinType);
            npcRoleEntity.setCustomName(Component.translatable(npcRoleEntity.getRoleType().translateKey()));
            npcRoleEntity.refreshDimensions();
        }
//        System.out.println("Apply skin " + skinId + " to entity " + target.getId());
    }
}