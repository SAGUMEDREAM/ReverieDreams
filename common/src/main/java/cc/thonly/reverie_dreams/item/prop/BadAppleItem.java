package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.dialog.DialogPlayer;
import cc.thonly.reverie_dreams.sound.JukeboxSongInit;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Optional;

@SuppressWarnings("resource")
public class BadAppleItem extends Item {
    public static final String FILE_NAME = "badapple.json";

    public BadAppleItem(Properties settings) {
        super(settings);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        if (!world.isClientSide() && user instanceof ServerPlayer serverPlayer) {
            ResourceKey<SoundEvent> key = JukeboxSongInit.BAD_APPLE.getSoundEventKey();
            if (key != null) {
                DialogPlayer.play(serverPlayer, FILE_NAME, new SoundEvent(key.identifier(), Optional.empty()));
            } else {
                DialogPlayer.play(serverPlayer, FILE_NAME, null);
            }
        }
        return super.finishUsingItem(stack, world, user);
    }
}
