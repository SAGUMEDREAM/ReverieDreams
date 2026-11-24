package cc.thonly.reverie_dreams.server.dialog.video;

import com.google.gson.stream.JsonReader;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DialogVideoDataTest {

    @Test
    void readsRleFrames() throws IOException {
        String json = """
                {"version":2,"width":2,"height":2,"fps":20,
                 "frames":[[[16711680,2],[65280,2]]]}
                """;
        DialogVideoData data = DialogVideoData.read(new JsonReader(new StringReader(json)));

        assertEquals(2, data.width());
        assertEquals(2, data.height());
        assertEquals(1, data.frames().size());

        DialogVideoData.Frame frame = data.frames().getFirst();
        assertEquals(2, frame.colors().length);
        assertEquals(0xFF0000, frame.colors()[0]);
        assertEquals(0x00FF00, frame.colors()[1]);
        assertEquals(2, frame.lengths()[0]);
        assertEquals(4, frame.pixelCount());
    }

    @Test
    void rendersRunsWithRgbStyleAndRowBreak() throws IOException {
        String json = """
                {"version":2,"width":2,"height":2,
                 "frames":[[[16711680,2],[65280,2]]]}
                """;
        DialogVideoData data = DialogVideoData.read(new JsonReader(new StringReader(json)));
        Component body = data.render(data.frames().getFirst());

        assertEquals("■■■■\n■■■■", body.getString());
        List<Component> runs = body.getSiblings();
        assertEquals(2, runs.size());
        assertEquals(0xFF0000, runs.get(0).getStyle().getColor().getValue());
        assertEquals(0x00FF00, runs.get(1).getStyle().getColor().getValue());
    }

    @Test
    void legacyArrayIsNotV2() throws IOException {
        assertNull(DialogVideoData.read(new JsonReader(new StringReader("[[\"§0■\"]]"))));
    }

    @Test
    void rejectsWrongPixelCount() {
        String json = """
                {"version":2,"width":2,"height":2,"frames":[[[0,3]]]}
                """;
        assertThrows(IOException.class,
                () -> DialogVideoData.read(new JsonReader(new StringReader(json))));
    }
}
