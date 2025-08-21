package cc.thonly.reverie_dreams.item;

import cc.thonly.reverie_dreams.dialog.DialogFiles;
import cc.thonly.reverie_dreams.dialog.DialogPlayer;
import cc.thonly.reverie_dreams.sound.JukeboxSongInit;
import net.minecraft.component.type.FoodComponents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

public class BadAppleItem extends BasicItem {
    public static final String FILE_NAME = "badapple.json";

    public BadAppleItem(String path, Settings settings) {
        super(path, settings.food(FoodComponents.GOLDEN_APPLE));
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!world.isClient() && user instanceof ServerPlayerEntity serverPlayer) {
            DialogPlayer.play(serverPlayer, FILE_NAME, JukeboxSongInit.BAD_APPLE.getSoundEventReference().value());
        }
        return super.finishUsing(stack, world, user);
    }
}
