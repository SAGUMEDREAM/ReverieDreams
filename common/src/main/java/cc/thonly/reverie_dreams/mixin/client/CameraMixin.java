package cc.thonly.reverie_dreams.mixin.client;

import cc.thonly.reverie_dreams.client.camera.STGCameraController;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow
    protected abstract void setPosition(double x, double y, double z);

    @Shadow
    protected abstract void setRotation(float yRot, float xRot);


    @Inject(
            method = "alignWithEntity",
            at = @At("TAIL")
    )
    private void reverie_dreams$stgCamera(
            float partialTicks,
            CallbackInfo ci
    ) {
        STGCameraController controller = STGCameraController.Instance.get();
        if (!controller.isEnabled()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Entity player = minecraft.player;
        if (player == null) {
            return;
        }

        Vec3 pos = player.getPosition(partialTicks);
        float yaw = player.getViewYRot(partialTicks);
        double yawRad = Math.toRadians(yaw);

        double offsetX = -Math.sin(yawRad) * controller.getDistance();
        double offsetZ = Math.cos(yawRad) * controller.getDistance();

        this.setPosition(
                pos.x + offsetX,
                pos.y + controller.getHeight(),
                pos.z + offsetZ
        );

        this.setRotation(
                yaw,
                65.0F
        );

    }
}