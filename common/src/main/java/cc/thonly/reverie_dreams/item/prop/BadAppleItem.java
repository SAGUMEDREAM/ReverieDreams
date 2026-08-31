package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.api.dialog.DialogAPI;
import cc.thonly.reverie_dreams.sound.JukeboxSongInit;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTickRateManager;
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
            MinecraftServer server = world.getServer();
            assert server != null;
            ServerTickRateManager serverTickRateManager = server.tickRateManager();
            serverTickRateManager.setFrozen(false);
            ResourceKey<SoundEvent> key = JukeboxSongInit.BAD_APPLE.getSoundEventKey();
            if (key != null) {
                DialogAPI.play(serverPlayer, FILE_NAME, new SoundEvent(key.identifier(), Optional.empty()));
            } else {
                DialogAPI.play(serverPlayer, FILE_NAME, null);
            }
        }
        return super.finishUsingItem(stack, world, user);
    }
}
