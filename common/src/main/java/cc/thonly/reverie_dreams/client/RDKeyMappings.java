package cc.thonly.reverie_dreams.client;

import cc.thonly.reverie_dreams.client.camera.STGCameraController;
import cc.thonly.reverie_dreams.util.PlatformContext;
import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

@SuppressWarnings("ConstantValue")
public class RDKeyMappings {
    // STG 相机按键暂时封存：仅开发模式（IDE/开发构建）注册，正式环境不暴露；做 Boss 时沿用此入口恢复。
    @Nullable
    public static final Holder<KeyMapping> STG_CAMERA = PlatformContext.isDevModeByIDEA()
            ? registerKey(() -> new KeyMapping("reverie_dreams.key.stg_camera", InputConstants.Type.KEYSYM, InputConstants.KEY_V, KeyMapping.Category.GAMEPLAY))
            : null;

    public static void initialize() {
        if (STG_CAMERA == null) {
            return;
        }
        Holder<KeyMapping> stgCamera = STG_CAMERA;
        ClientTickEvent.CLIENT_POST.register(mc -> {
            LocalPlayer player = mc.player;
            if (player == null) {
                return;
            }
            STGCameraController controller = STGCameraController.Instance.get();
            while (stgCamera.value().consumeClick()) {
                controller.toggle();
                if (controller.isEnabled()) {
                    mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
                } else {
                    mc.options.setCameraType(CameraType.FIRST_PERSON);
                }
            }
            if (controller.isEnabled()) {
                player.setXRot(0);
                player.xRotO = 0;
            }
        });
    }

    public static Holder<KeyMapping> registerKey(Supplier<KeyMapping> factory) {
        KeyMapping keyMapping = factory.get();
        KeyMappingRegistry.register(keyMapping);
        return Holder.direct(keyMapping);
    }
}
