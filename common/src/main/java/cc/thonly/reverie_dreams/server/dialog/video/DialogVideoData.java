package cc.thonly.reverie_dreams.server.dialog.video;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 真彩色 Dialog 视频数据（version 2）。
 *
 * <p>每帧是扁平扫描线上的同色 run 序列：{@code [0xRRGGBB, 像素数]}。
 * 渲染时每个 run 生成一个带 RGB 颜色的 Component，跨行的 run 在文本中插入换行。</p>
 */
public final class DialogVideoData {
    public static final int VERSION = 2;

    private final int width;
    private final int height;
    private final List<Frame> frames;

    private DialogVideoData(int width, int height, List<Frame> frames) {
        this.width = width;
        this.height = height;
        this.frames = frames;
    }

    public int width() {
        return this.width;
    }

    public int height() {
        return this.height;
    }

    public List<Frame> frames() {
        return this.frames;
    }

    public int bodyWidth() {
        return this.width * 16;
    }

    public Component render(Frame frame) {
        MutableComponent root = Component.empty();
        int pixel = 0;
        int[] colors = frame.colors();
        int[] lengths = frame.lengths();
        for (int i = 0; i < colors.length; i++) {
            int length = lengths[i];
            StringBuilder text = new StringBuilder(length * 2 + 4);
            for (int j = 0; j < length; j++) {
                if (pixel > 0 && pixel % this.width == 0) {
                    text.append('\n');
                }
                text.append("■■");
                pixel++;
            }
            root.append(Component.literal(text.toString())
                    .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(colors[i]))));
        }
        return root;
    }

    /**
     * 读取 version 2 视频。顶层是数组时返回 {@code null}，表示旧版字符串格式。
     */
    public static DialogVideoData read(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.BEGIN_ARRAY) {
            return null;
        }
        if (reader.peek() != JsonToken.BEGIN_OBJECT) {
            throw new IOException("Unsupported dialog video root: " + reader.peek());
        }

        int version = 0;
        int width = 0;
        int height = 0;
        List<Frame> frames = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "version" -> version = reader.nextInt();
                case "width" -> width = reader.nextInt();
                case "height" -> height = reader.nextInt();
                case "frames" -> frames = readFrames(reader);
                default -> reader.skipValue();
            }
        }
        reader.endObject();

        if (version < VERSION) {
            throw new IOException("Unsupported dialog video version: " + version);
        }
        if (width <= 0 || height <= 0) {
            throw new IOException("Invalid dialog video size: " + width + "x" + height);
        }
        if (frames == null) {
            throw new IOException("Dialog video has no frames");
        }

        int expected = width * height;
        for (int i = 0; i < frames.size(); i++) {
            Frame frame = frames.get(i);
            if (frame.pixelCount() != expected) {
                throw new IOException("Frame " + i + " has " + frame.pixelCount()
                        + " pixels, expected " + expected);
            }
        }
        return new DialogVideoData(width, height, frames);
    }

    private static List<Frame> readFrames(JsonReader reader) throws IOException {
        List<Frame> frames = new ArrayList<>();
        reader.beginArray();
        while (reader.hasNext()) {
            reader.beginArray();
            IntArrayList colors = new IntArrayList();
            IntArrayList lengths = new IntArrayList();
            int pixelCount = 0;
            while (reader.hasNext()) {
                reader.beginArray();
                int color = reader.nextInt() & 0xFFFFFF;
                int length = reader.nextInt();
                reader.endArray();
                if (length <= 0) {
                    throw new IOException("Run length must be positive: " + length);
                }
                colors.add(color);
                lengths.add(length);
                pixelCount += length;
            }
            reader.endArray();
            frames.add(new Frame(colors.toIntArray(), lengths.toIntArray(), pixelCount));
        }
        reader.endArray();
        return frames;
    }

    public record Frame(int[] colors, int[] lengths, int pixelCount) {
    }
}
