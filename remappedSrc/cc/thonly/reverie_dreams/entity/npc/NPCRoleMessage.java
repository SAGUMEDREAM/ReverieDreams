package cc.thonly.reverie_dreams.entity.npc;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface NPCRoleMessage {
    @NotNull
    MutableComponent getMessage(ServerLevel world, ServerPlayer player, ItemStack stack, InteractionHand hand, BaseNPCLikeEntity entity);

    @Nullable
    default SoundEvent getSoundEvent() {
        return null;
    }

    default ResourceLocation getId() {
        return ResourceLocation.parse("empty");
    }

}
