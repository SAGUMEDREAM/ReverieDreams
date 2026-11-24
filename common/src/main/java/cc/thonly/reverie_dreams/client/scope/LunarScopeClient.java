package cc.thonly.reverie_dreams.client.scope;

import cc.thonly.reverie_dreams.client.camera.STGCameraController;
import cc.thonly.reverie_dreams.item.weapon.LunarWeaponType;
import cc.thonly.reverie_dreams.item.weapon.WeaponOfTheMoon;
import cc.thonly.reverie_dreams.networking.payload.LunarAimFirePacket;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

/**
 * Client presentation only; never writes FOV options or server weapon state.
 */
public final class LunarScopeClient {
    private static ItemStack tracked = ItemStack.EMPTY;
    private static Object level;
    private static LunarWeaponType type;
    private static float amount;
    private static long lastFrame;

    private LunarScopeClient() {
    }

    private static void reset() {
        tracked = ItemStack.EMPTY;
        level = null;
        type = null;
        amount = 0;
        lastFrame = 0;
    }

    private static boolean canScope(Minecraft mc) {
        return mc.player != null && mc.level != null && mc.getCameraEntity() == mc.player
                && mc.player.isAlive() && !mc.player.isSpectator() && mc.screen == null && !mc.isPaused()
                && mc.options.getCameraType().isFirstPerson() && !STGCameraController.Instance.get().isEnabled();
    }

    public static boolean hidesHeldGun(InteractionHand hand, ItemStack stack) {
        Minecraft mc = Minecraft.getInstance();
        return stack.getItem() instanceof WeaponOfTheMoon && canScope(mc)
                && mc.player.getUsedItemHand() == hand && WeaponOfTheMoon.isAiming(mc.player);
    }

    public static void handleAttackKey() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null || !WeaponOfTheMoon.isAiming(mc.player)
                || !mc.options.keyUse.isDown() || !NetworkManager.canServerReceive(LunarAimFirePacket.PACKET_ID))
            return;

        ItemStack stack = mc.player.getUseItem();
        WeaponOfTheMoon gun = (WeaponOfTheMoon) stack.getItem();
        boolean pressed = mc.options.keyAttack.consumeClick();
        boolean held = gun.getWeaponType().automatic() && mc.options.keyAttack.isDown() && gun.getRounds(stack) > 0;
        // The server's weapon cooldown controls cadence, even when its response arrives late.
        if (pressed || held) {
            NetworkManager.sendToServer(new LunarAimFirePacket(true));
        }
    }

    private static void update() {
        Minecraft mc = Minecraft.getInstance();
        if (!canScope(mc)) {
            reset();
            return;
        }
        if (level != null && level != mc.level) {
            reset();
        }
        level = mc.level;
        ItemStack using = mc.player.getUseItem();
        boolean aiming = WeaponOfTheMoon.isAiming(mc.player);
        if (aiming) {
            if (!ItemStack.isSameItem(tracked, using)) {
                amount = 0;
                lastFrame = 0;
            }
            tracked = using;
            type = ((WeaponOfTheMoon) using.getItem()).getWeaponType();
        } else if (tracked.isEmpty() || (mc.player.getMainHandItem() != tracked && mc.player.getOffhandItem() != tracked)) {
            reset();
            return;
        }
        long now = System.nanoTime();
        double elapsed = lastFrame == 0 ? 1.0 / 60 : (now - lastFrame) / 1_000_000_000.0;
        lastFrame = now;
        amount = LunarScopeStyle.approach(amount, aiming, elapsed);
        if (!aiming && amount < .005f)
            reset();
    }

    public static float modifyFov(float base, float dynamicModifier) {
        update();
        return type == null ? base : LunarScopeStyle.fov(base, dynamicModifier, type, amount);
    }

    public static boolean extractCrosshair(GuiGraphicsExtractor g) {
        update();
        if (type == null || amount < .05f)
            return false;
        LunarScopeOverlay.draw(g::fill, type, g.guiWidth(), g.guiHeight(), amount);
        return true;
    }
}
