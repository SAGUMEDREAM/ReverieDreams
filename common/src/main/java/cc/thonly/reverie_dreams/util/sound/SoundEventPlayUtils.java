package cc.thonly.reverie_dreams.util.sound;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
        level.playSound(null, player.getX(), player.getY(), player.getZ(), event, SoundSource.UI, volume, pitch);
    }

    public static void playUISound(Player player, float volume, float pitch) {
        Level level = player.level();
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.UI, volume, pitch);
    }

    public static void playSound(Level level, BlockPos pos, SoundEvent event, SoundSource source) {
        level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), event, source);
    }
}
