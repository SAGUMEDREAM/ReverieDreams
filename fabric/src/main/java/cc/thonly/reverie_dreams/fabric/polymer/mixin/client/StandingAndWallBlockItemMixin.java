package cc.thonly.reverie_dreams.fabric.polymer.mixin.client;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.util.PlatformContext;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Method;

@SuppressWarnings("unused")
@Pseudo
@Slf4j
@Mixin(BlockItem.class)
public class StandingAndWallBlockItemMixin {
    @Unique
    private static Class<?> reverie_dreams$_polymerItemUtils;
    @Unique
    private static Method reverie_dreams$_getPolymerIdentifier;
    @Unique
    private static boolean reverie_dreams$_init = false;
    @Unique
    private static boolean reverie_dreams$_loggedError = false;

    @Unique
    private static void reverie_dreams$initPolymer() {
        if (reverie_dreams$_init) return;
        reverie_dreams$_init = true;

        try {
            Class<?> clazz = Class.forName("eu.pb4.polymer.core.api.item.PolymerItemUtils");
            reverie_dreams$_polymerItemUtils = clazz;
            reverie_dreams$_getPolymerIdentifier =
                    clazz.getDeclaredMethod("getPolymerIdentifier", ItemStack.class);
        } catch (Exception e) {
            log.error("Failed to init Polymer reflection", e);
        }
    }

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    public void cancelPolymerItemPlace(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (!PlatformContext.hasPolymer()) {
            return;
        }

        reverie_dreams$initPolymer();

        if (reverie_dreams$_getPolymerIdentifier == null) {
            return;
        }

        ItemStack itemStack = context.getItemInHand();
        Level level = context.getLevel();

        Identifier polymerIdentifier = null;
        try {
            polymerIdentifier = (Identifier) reverie_dreams$_getPolymerIdentifier.invoke(null, itemStack);
        } catch (Exception e) {
            if (!reverie_dreams$_loggedError) {
                log.error("Failed to invoke Polymer API", e);
                reverie_dreams$_loggedError = true;
            }
        }

        if (polymerIdentifier == null) {
            return;
        }

        if (itemStack.getItem() instanceof StandingAndWallBlockItem
                && polymerIdentifier.getNamespace().equals(ReverieDreams.MOD_ID)) {

            if (level.isClientSide()) {
                cir.setReturnValue(InteractionResult.FAIL);
            }
        }
    }
}
