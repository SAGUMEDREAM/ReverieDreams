package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.sound.JukeboxSongInit;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BadAppleItem extends Item {
    public static final String FILE_NAME = "badapple.json";

    public BadAppleItem(Properties settings) {
        super(settings);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        if (!world.isClientSide() && user instanceof ServerPlayer serverPlayer) {
            serverPlayer.serverLevel().playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), JukeboxSongInit.BAD_APPLE.getSoundEventReference().value(), SoundSource.PLAYERS, 1f, 1.0f);
        }
        return super.finishUsingItem(stack, world, user);
    }
}
