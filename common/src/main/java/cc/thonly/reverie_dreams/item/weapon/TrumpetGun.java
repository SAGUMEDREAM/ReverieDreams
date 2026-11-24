package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.reverie_dreams.data.danmaku.DanmakuTrajectory;
import cc.thonly.reverie_dreams.entity.misc.DanmakuEntity;
import cc.thonly.reverie_dreams.item.base.IDanmakuItem;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.server.DelayedTask;
import cc.thonly.reverie_dreams.registry.content.sound.RDSoundEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.function.BooleanSupplier;

public class TrumpetGun extends Item implements IDanmakuItem {

    public TrumpetGun(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        if (!world.isClientSide() && user instanceof ServerPlayer player) {
            Item ammunition = RDItems.POWER.asItem();
            if (!player.hasInfiniteMaterials() && findAmmunition(player.getInventory(), ammunition).isEmpty()) {
                return InteractionResult.FAIL;
            }
            ServerLevel serverWorld = (ServerLevel) world;
            float pitch = user.getXRot();
            float yaw = user.getYRot();
            boolean[] fired = {false};
            DelayedTask.repeat(world.getServer(), 3, 2, () -> {
                if (!player.isAlive() || player.isRemoved() || player.level() != serverWorld || player.getItemInHand(hand) != stack) {
                    return;
                }
                boolean shot = tryFireShot(player.getInventory(), ammunition, player.hasInfiniteMaterials(), () -> {
                    DanmakuEntity entity = DanmakuTrajectory.spawnByItemStack(serverWorld, user, user.getX(), user.getEyeY(), user.getZ(), DanmakuTypes.random(DanmakuTypes.BULLET).create(), pitch, yaw, 0.0f, 1.5f);
                    if (entity == null) {
                        return false;
                    }
                    entity.setDanmakuProperties(entity.getDanmakuProperties().withSpeed(2.3f));
                    return true;
                });
                if (!shot) {
                    return;
                }
                if (!fired[0]) {
                    fired[0] = true;
                    if (!player.hasInfiniteMaterials()) {
                        stack.hurtWithoutBreaking(1, player);
                    }
                }
                world.playSound(null, user.getOnPos(), RDSoundEvents.FIRE.value(), SoundSource.HOSTILE, 1.0f, 1.0f);
            });
            player.getCooldowns().addCooldown(stack, 10);
            return InteractionResult.SUCCESS_SERVER;
        }
        return super.use(world, user, hand);
    }

    private static ItemStack findAmmunition(Container inventory, Item ammunition) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(ammunition) && !stack.isEmpty()) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    // Each delayed shot checks current stock, so a partial burst needs no upfront reload.
    static boolean tryFireShot(Container inventory, Item ammunition, boolean infinite, BooleanSupplier spawnShot) {
        ItemStack ammo = infinite ? ItemStack.EMPTY : findAmmunition(inventory, ammunition);
        if ((!infinite && ammo.isEmpty()) || !spawnShot.getAsBoolean()) {
            return false;
        }
        if (!infinite) {
            ammo.shrink(1);
            inventory.setChanged();
        }
        return true;
    }

}
