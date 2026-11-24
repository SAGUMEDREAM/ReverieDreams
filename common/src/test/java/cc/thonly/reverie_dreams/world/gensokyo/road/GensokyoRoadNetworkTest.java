package cc.thonly.reverie_dreams.world.gensokyo.road;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.Base64;
import java.util.zip.GZIPOutputStream;
import static org.junit.jupiter.api.Assertions.*;

class GensokyoRoadNetworkTest {
    private JsonObject encoded(int width, int depth, int ox, int oz, int[][] entries) throws Exception {
        var bytes = new ByteArrayOutputStream();
        try (var gzip = new GZIPOutputStream(bytes); var out = new DataOutputStream(gzip)) {
            out.writeInt(0x52445231);out.writeInt(width);out.writeInt(depth);out.writeInt(ox);out.writeInt(oz);out.writeInt(entries.length);
            for (var e : entries) { out.writeInt(e[0]);out.writeShort(e[1]);out.writeShort(e[2]);out.writeShort(e[3]);out.writeByte(e[4]); }
        }
        var root = new JsonObject();root.addProperty("version",1);root.addProperty("encoding","gzip-base64-rdr1");
        root.addProperty("columns",entries.length);root.addProperty("data",Base64.getEncoder().encodeToString(bytes.toByteArray()));return root;
    }
    @Test void oldMapsHaveNoReservation() { assertSame(GensokyoRoadNetwork.EMPTY,GensokyoRoadNetwork.decode(null,32,16,0,0,0,64)); }
    @Test void exactVerticalBoundsAndNeighbouringChunk() throws Exception {
        var net=GensokyoRoadNetwork.decode(encoded(32,16,0,0,new int[][]{{15,20,21,25,1},{16,22,25,27,4}}),32,16,0,0,0,64);
        assertEquals(2,net.size());assertEquals(20,net.pavementY(15,0));assertEquals(22,net.pavementY(16,0));
        assertTrue(net.contains(15,19,0));assertTrue(net.contains(15,25,0));assertFalse(net.contains(15,18,0));assertFalse(net.contains(15,26,0));
        assertFalse(net.contains(14,21,0));assertFalse(net.contains(15,21,1));assertEquals(Short.MIN_VALUE,net.pavementY(32,0));
    }
    @Test void negativeOriginUsesFloorChunkCoordinates() throws Exception {
        var net=GensokyoRoadNetwork.decode(encoded(32,16,-17,-1,new int[][]{{0,20,21,25,1},{17,20,21,25,1}}),32,16,-17,-1,0,64);
        assertTrue(net.contains(-17,21,-1));assertTrue(net.contains(0,21,-1));assertFalse(net.contains(-16,21,-1));
    }
    @Test void dimensionsAndOriginCannotSilentlyDrift() throws Exception {
        var data=encoded(32,16,0,0,new int[][]{{0,20,21,25,1}});
        assertThrows(IllegalArgumentException.class,()->GensokyoRoadNetwork.decode(data,16,32,0,0,0,64));
        assertThrows(IllegalArgumentException.class,()->GensokyoRoadNetwork.decode(data,32,16,1,0,0,64));
    }
    @Test void badDuplicateAndOutOfRangeColumnsFailFast() throws Exception {
        for (var rows : new int[][][]{ {{0,20,21,25,1},{0,20,21,25,1}}, {{512,20,21,25,1}}, {{-1,20,21,25,1}}, {{0,20,20,25,1}}, {{0,20,21,64,1}}, {{0,20,21,25,5}} }) {
            var data=encoded(32,16,0,0,rows);
            assertThrows(IllegalArgumentException.class,()->GensokyoRoadNetwork.decode(data,32,16,0,0,0,64));
        }
    }
    @Test void mismatchingDeclaredCountIsRejected() throws Exception {
        var data=encoded(32,16,0,0,new int[][]{{0,20,21,25,1}});data.addProperty("columns",2);
        assertThrows(IllegalArgumentException.class,()->GensokyoRoadNetwork.decode(data,32,16,0,0,0,64));
    }
}
