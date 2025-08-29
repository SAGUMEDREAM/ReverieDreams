package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.dialog.DialogPlayer;
import cc.thonly.reverie_dreams.sound.JukeboxSongInit;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

public class BadAppleItem extends Item {
    public static final String FILE_NAME = "badapple.json";

    public BadAppleItem(Settings settings) {
        super(settings);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!world.isClient() && user instanceof ServerPlayerEntity serverPlayer) {
            DialogPlayer.play(serverPlayer, FILE_NAME, JukeboxSongInit.BAD_APPLE.getSoundEventReference().value());
        }
        return super.finishUsing(stack, world, user);
    }
}
