package cc.thonly.reverie_dreams.util.sound;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

@SuppressWarnings("resource")
public class SoundEventPlayUtils {
    public static void playSound(Player player, SoundEvent event, SoundSource source) {
        Level level = player.level();
        level.playSound(null, player.getX(), player.getY(), player.getZ(), event, source);
    }

    public static void playSound(Player player, SoundEvent event, SoundSource source, float volume, float pitch) {
        Level level = player.level();
        level.playSound(null, player.getX(), player.getY(), player.getZ(), event, source, volume, pitch);
    }

    public static void playUISound(Player player, SoundEvent event, float volume, float pitch) {
        Level level = player.level();
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundSoundPacket(Holder.direct(event), SoundSource.UI, player.getX(), player.getY(), player.getZ(), volume, pitch, 0L));
        }
    }

    public static void playUISound(Player player, float volume, float pitch) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundSoundPacket(SoundEvents.UI_BUTTON_CLICK, SoundSource.UI, player.getX(), player.getY(), player.getZ(), volume, pitch, 0L));
        }
    }

    public static void playSound(Level level, Entity entity, SoundEvent event, SoundSource source) {
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), event, source);
    }

    public static void playSound(Level level, BlockPos pos, SoundEvent event, SoundSource source) {
        level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), event, source);
    }

    public static void playSound(Level level, double x, double y, double z, SoundEvent event, SoundSource source) {
        level.playSound(null, x, y, z, event, source);
    }
}
