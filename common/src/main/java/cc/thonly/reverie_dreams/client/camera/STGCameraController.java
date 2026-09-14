package cc.thonly.reverie_dreams.client.camera;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public final class STGCameraController {

    private boolean enabled = false;
    /**
     * Camera 高度。
     */
    private double height = 8.0D;
    /**
     * Camera 与玩家的水平距离。
     */
    private double distance = -3.0D;

    private STGCameraController() {
    }

    public void toggle() {
        this.enabled = !this.enabled;
    }

    public enum Instance {
        INSTANCE();

        private final STGCameraController controller =
                new STGCameraController();

        public static STGCameraController get() {
            return INSTANCE.controller;
        }
    }
}