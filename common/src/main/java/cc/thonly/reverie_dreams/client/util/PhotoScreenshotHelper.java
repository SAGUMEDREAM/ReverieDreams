package cc.thonly.reverie_dreams.client.util;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.world.level.material.MapColor;

import java.util.concurrent.CompletableFuture;

public class PhotoScreenshotHelper {
    public static CompletableFuture<NativeImage> getClientImage() {
        Minecraft mc = Minecraft.getInstance();
        RenderTarget target = mc.getMainRenderTarget();

        CompletableFuture<NativeImage> future = new CompletableFuture<>();

        mc.execute(() -> {
            Screenshot.takeScreenshot(target, 1, future::complete);
        });

        return future;
    }

    public static NativeImage resizeImage(NativeImage original) {
        int srcW = original.getWidth();
        int srcH = original.getHeight();

        int size = Math.min(srcW, srcH);

        int startX = (srcW - size) / 2;
        int startY = (srcH - size) / 2;

        NativeImage resized = new NativeImage(128, 128, false);

        for (int y = 0; y < 128; y++) {
            for (int x = 0; x < 128; x++) {

                int srcX = startX + x * size / 128;
                int srcY = startY + y * size / 128;

                int color = original.getPixel(srcX, srcY);
                resized.setPixel(x, y, color);
            }
        }

        return resized;
    }

    public static NativeImage rescaleImage(NativeImage original, int fov) {
        int srcW = original.getWidth();
        int srcH = original.getHeight();

        final double defaultFov = 75.0;

        double fovRad = Math.toRadians(fov);
        double defaultRad = Math.toRadians(defaultFov);

        double scale = Math.tan(fovRad / 2.0) / Math.tan(defaultRad / 2.0);

        scale = Math.max(0.1, Math.min(scale, 3.0));

        int baseSize = Math.min(srcW, srcH);
        int cropSize = (int) (baseSize / scale);

        cropSize = Math.max(1, Math.min(cropSize, baseSize));

        int startX = (srcW - cropSize) / 2;
        int startY = (srcH - cropSize) / 2;

        NativeImage resized = new NativeImage(128, 128, false);

        for (int y = 0; y < 128; y++) {
            for (int x = 0; x < 128; x++) {

                int srcX = startX + x * cropSize / 128;
                int srcY = startY + y * cropSize / 128;

                int color = original.getPixel(srcX, srcY);
                resized.setPixel(x, y, color);
            }
        }

        return resized;
    }

    public static byte[] getImageBytes(NativeImage image) {
        byte[] pixels = new byte[128 * 128];

        for (int y = 0; y < 128; y++) {
            for (int x = 0; x < 128; x++) {

                int argb = image.getPixel(x, y);

                int r = (argb >> 16) & 255;
                int g = (argb >> 8) & 255;
                int b = argb & 255;

                pixels[y * 128 + x] = getClosestColorIndex(r, g, b);
            }
        }

        return pixels;
    }

    public static byte getClosestColorIndex(int r, int g, int b) {
        double bestDistance = Double.MAX_VALUE;
        int bestIndex = 0;

        for (int colorId = 0; colorId < 64; colorId++) {
            MapColor mapColor = MapColor.byId(colorId);
            if (mapColor == MapColor.NONE) continue;

            int base = mapColor.col;

            for (int shade = 0; shade < 4; shade++) {

                int rgb = applyShade(base, shade);

                int cr = (rgb >> 16) & 0xFF;
                int cg = (rgb >> 8) & 0xFF;
                int cb = rgb & 0xFF;

                double dist = colorDistance(r, g, b, cr, cg, cb);

                if (dist < bestDistance) {
                    bestDistance = dist;
                    bestIndex = colorId * 4 + shade;
                }
            }
        }

        return (byte) bestIndex;
    }

    private static double colorDistance(int r1, int g1, int b1, int r2, int g2, int b2) {
        int dr = r1 - r2;
        int dg = g1 - g2;
        int db = b1 - b2;

        return dr * dr * 0.3 + dg * dg * 0.59 + db * db * 0.11;
    }

    private static int applyShade(int baseColor, int shade) {
        int multiplier;

        switch (shade) {
            case 0 -> multiplier = 180;
            case 1 -> multiplier = 220;
            case 2 -> multiplier = 255;
            case 3 -> multiplier = 135;
            default -> multiplier = 255;
        }

        int r = (baseColor >> 16) & 0xFF;
        int g = (baseColor >> 8) & 0xFF;
        int b = baseColor & 0xFF;

        r = r * multiplier / 255;
        g = g * multiplier / 255;
        b = b * multiplier / 255;

        return (r << 16) | (g << 8) | b;
    }
}
