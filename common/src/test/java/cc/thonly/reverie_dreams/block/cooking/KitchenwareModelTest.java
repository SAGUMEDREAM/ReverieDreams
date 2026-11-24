package cc.thonly.reverie_dreams.block.cooking;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.SharedConstants;
import net.minecraft.core.Direction;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class KitchenwareModelTest {
    private static final List<String> TYPES=List.of("cooking_pot","grill","frying_pan","steamer","cutting_board");
    private static final List<String> TIERS=List.of("","mystia_","super_","extreme_","nuke_");
    @BeforeAll static void bootstrap(){SharedConstants.tryDetectVersion();Bootstrap.bootStrap();}

    @Test void allModelsHaveDisjointIntegerPartsInsideOneBlock() throws Exception {
        for(String type:TYPES)for(String tier:TIERS){String name=tier+type;BitSet occupied=new BitSet(4096);
            for(var raw:model(name).getAsJsonArray("elements")){
                var part=raw.getAsJsonObject();int[] a=vector(part,"from"),b=vector(part,"to");
                for(int i=0;i<3;i++)assertTrue(a[i]>=0&&a[i]<b[i]&&b[i]<=16,name);
                for(int y=a[1];y<b[1];y++)for(int z=a[2];z<b[2];z++)for(int x=a[0];x<b[0];x++){
                    int cell=x+16*(z+16*y);assertFalse(occupied.get(cell),name+" overlapping part");occupied.set(cell);
                }
            }
            assertFalse(occupied.isEmpty(),name);
        }
    }
    @Test void everyVisibleFaceKeepsOneTexelPerModelUnit() throws Exception {
        for(String type:TYPES)for(String tier:TIERS)for(var raw:model(tier+type).getAsJsonArray("elements")){
            var part=raw.getAsJsonObject();int[] a=vector(part,"from"),b=vector(part,"to");
            for(var entry:part.getAsJsonObject("faces").entrySet()){
                String face=entry.getKey();var uv=entry.getValue().getAsJsonObject().getAsJsonArray("uv");
                int[] axes=face.equals("up")||face.equals("down")?new int[]{0,2}:face.equals("north")||face.equals("south")?new int[]{0,1}:new int[]{2,1};
                for(int i=0;i<2;i++)assertEquals(b[axes[i]]-a[axes[i]],Math.abs(uv.get(i+2).getAsDouble()-uv.get(i).getAsDouble())*4,1e-8,tier+type+" "+face);
                for(var n:uv)assertTrue(n.getAsDouble()>=0&&n.getAsDouble()<=16);
            }
        }
    }
    @Test void cachedOutlineMatchesEveryModelAtAllFourFacings() throws Exception {
        for(String type:TYPES)for(String tier:TIERS)for(Direction direction:List.of(Direction.NORTH,Direction.EAST,Direction.SOUTH,Direction.WEST)){
            int turns=switch(direction){case EAST->1;case SOUTH->2;case WEST->3;default->0;};
            BitSet expected=new BitSet(4096),actual=new BitSet(4096);
            for(var raw:model(tier+type).getAsJsonArray("elements")){
                var part=raw.getAsJsonObject();int[] a=vector(part,"from"),b=vector(part,"to");
                for(int y=a[1];y<b[1];y++)for(int z=a[2];z<b[2];z++)for(int x=a[0];x<b[0];x++){
                    int xx=x,zz=z;for(int i=0;i<turns;i++){int old=xx;xx=15-zz;zz=old;}expected.set(xx+16*(zz+16*y));
                }
            }
            for(var box:KitchenwareShapes.outline(tier+type,direction).toAabbs()){
                for(int y=(int)Math.round(box.minY*16);y<Math.round(box.maxY*16);y++)
                    for(int z=(int)Math.round(box.minZ*16);z<Math.round(box.maxZ*16);z++)
                        for(int x=(int)Math.round(box.minX*16);x<Math.round(box.maxX*16);x++)actual.set(x+16*(z+16*y));
            }
            assertEquals(expected,actual,tier+type+" "+direction);
            assertSame(KitchenwareShapes.outline(tier+type,direction),KitchenwareShapes.outline(tier+type,direction),"outline must be cached");
        }
    }
    @Test void sharedAtlasIsOpaqueAndUsesSmallMaterialPalettes() throws Exception {
        try(var input=getClass().getResourceAsStream("/assets/reverie_dreams/textures/block/kitchenware_atlas.png")){
            assertNotNull(input);var image=ImageIO.read(input);assertEquals(64,image.getWidth());assertEquals(64,image.getHeight());
            for(int ty=0;ty<4;ty++)for(int tx=0;tx<4;tx++){
                var colors=new HashSet<Integer>();for(int y=0;y<16;y++)for(int x=0;x<16;x++){
                    int pixel=image.getRGB(tx*16+x,ty*16+y);assertEquals(255,pixel>>>24);colors.add(pixel);
                }
                assertTrue(colors.size()<=6,"material palette has too many colors");
            }
        }
    }
    private static int[] vector(JsonObject object,String key){
        var values=object.getAsJsonArray(key);int[] result=new int[3];
        for(int i=0;i<3;i++){assertEquals(values.get(i).getAsInt(),values.get(i).getAsDouble(),1e-8);result[i]=values.get(i).getAsInt();}return result;
    }
    private static JsonObject model(String name) throws Exception {
        try(var input=KitchenwareModelTest.class.getResourceAsStream("/assets/reverie_dreams/models/block/"+name+".json")){
            assertNotNull(input,name);return JsonParser.parseReader(new InputStreamReader(input,StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }
}
