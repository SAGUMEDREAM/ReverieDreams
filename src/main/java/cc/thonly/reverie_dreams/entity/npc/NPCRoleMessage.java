package cc.thonly.reverie_dreams.entity.npc;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.MutableText;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface NPCRoleMessage {
    @NotNull
    MutableText getMessage(ServerWorld world, ServerPlayerEntity player, ItemStack stack, Hand hand, NPCEntityImpl entity);

    @Nullable
    default SoundEvent getSoundEvent() {
        return null;
    }

    default Identifier getId() {
        return Identifier.of("empty");
    }

}
