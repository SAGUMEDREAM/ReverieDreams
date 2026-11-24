package cc.thonly.reverie_dreams.client.scope;

import cc.thonly.reverie_dreams.item.weapon.WeaponOfTheMoon;
import cc.thonly.reverie_dreams.networking.payload.LunarShotPosePacket;
import cc.thonly.reverie_dreams.networking.payload.LunarReloadPosePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Avatar;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.WeakHashMap;

public final class LunarWeaponPoseClient {
    private static final int SHOT_POSE_TICKS = 5;
    private static final Map<Avatar, Shot> recentShots = new WeakHashMap<>();
    private static final Map<Avatar, Reload> recentReloads = new WeakHashMap<>();

    private LunarWeaponPoseClient() {
    }

    public static void recordShot(LunarShotPosePacket packet) {
        var level = Minecraft.getInstance().level;
        if (level != null && level.getEntity(packet.entityId()) instanceof Avatar avatar) {
            InteractionHand hand = packet.offHand() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            recentShots.put(avatar, new Shot(hand, packet.weaponNumber(), avatar.tickCount));
        }
    }

    @Nullable
    public static InteractionHand getPoseHand(Avatar avatar) {
        if (!avatar.isAlive() || avatar.isSpectator())
            return null;
        if (avatar.isUsingItem()) {
            return avatar.getUseItem().getItem() instanceof WeaponOfTheMoon ? avatar.getUsedItemHand() : null;
        }
        Shot shot = getRecentShot(avatar);
        return shot == null ? null : shot.hand();
    }

    public static float shotKick(Avatar avatar, InteractionHand hand, float partialTick) {
        Shot shot = getRecentShot(avatar);
        if (shot == null || shot.hand() != hand
                || !(avatar.getItemInHand(hand).getItem() instanceof WeaponOfTheMoon weapon))
            return 0;
        var type = weapon.getWeaponType();
        float recovery = type.automatic() ? type.shotInterval() + .65f : SHOT_POSE_TICKS;
        float decay = Mth.clamp(1 - (avatar.tickCount - shot.tick() + partialTick) / recovery, 0, 1);
        return decay * decay * Mth.clamp(type.recoil() / 3, .28f, 1.35f);
    }

    public static void recordReload(LunarReloadPosePacket packet) {
        var player = Minecraft.getInstance().player;
        InteractionHand hand = packet.offHand() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        if (player != null && player.getItemInHand(hand).getItem() instanceof WeaponOfTheMoon weapon
                && weapon.getWeaponType().number() == packet.weaponNumber()) {
            recentReloads.put(player, new Reload(hand, weapon, player.getInventory().getSelectedSlot(), player.tickCount));
        }
    }

    public static float reloadProgress(AbstractClientPlayer player, InteractionHand hand, float partialTick) {
        Reload reload = recentReloads.get(player);
        if (reload == null)
            return 0;
        if (player.getItemInHand(reload.hand()).getItem() != reload.weapon()
                || (reload.hand() == InteractionHand.MAIN_HAND && reload.slot() != player.getInventory().getSelectedSlot())
                || player.tickCount - reload.tick() >= reload.weapon().getWeaponType().reloadTicks()) {
            recentReloads.remove(player);
            return 0;
        }
        return reload.hand() == hand
                ? (player.tickCount - reload.tick() + partialTick) / reload.weapon().getWeaponType().reloadTicks() : 0;
    }

    @Nullable
    private static Shot getRecentShot(Avatar avatar) {
        Shot shot = recentShots.get(avatar);
        if (shot != null && avatar.tickCount - shot.tick() < SHOT_POSE_TICKS
                && avatar.getItemInHand(shot.hand()).getItem() instanceof WeaponOfTheMoon weapon
                && weapon.getWeaponType().number() == shot.weaponNumber())
            return shot;
        return null;
    }

    private record Shot(InteractionHand hand, int weaponNumber, int tick) {
    }

    private record Reload(InteractionHand hand, WeaponOfTheMoon weapon, int slot, int tick) {
    }
}
