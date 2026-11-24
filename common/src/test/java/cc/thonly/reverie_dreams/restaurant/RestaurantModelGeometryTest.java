package cc.thonly.reverie_dreams.restaurant;

import cc.thonly.reverie_dreams.block.restaurant.MenuBookBlock;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.AABB;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.IdentityHashMap;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class RestaurantModelGeometryTest {
    private static final double EPSILON = 1e-8;
    private static MenuBookBlock book;

    @BeforeAll static void bootstrap() throws Exception {
        SharedConstants.tryDetectVersion(); Bootstrap.bootStrap();
        var holders=MappedRegistry.class.getDeclaredField("unregisteredIntrusiveHolders");
        var frozen=MappedRegistry.class.getDeclaredField("frozen");holders.setAccessible(true);frozen.setAccessible(true);
        Object previous=holders.get(BuiltInRegistries.BLOCK);boolean wasFrozen=frozen.getBoolean(BuiltInRegistries.BLOCK);
        try {
            frozen.setBoolean(BuiltInRegistries.BLOCK,false);holders.set(BuiltInRegistries.BLOCK,new IdentityHashMap<>());
            book=new MenuBookBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK,Identifier.parse("test:menu_book_geometry"))));
        } finally { holders.set(BuiltInRegistries.BLOCK,previous);frozen.setBoolean(BuiltInRegistries.BLOCK,wasFrozen); }
    }

    @Test void restaurantPartsMeetWithoutIntersectingInEitherOpenState() throws Exception {
        for(String name:List.of("mystia_bell","mystia_bell_open","menu_book","menu_book_open")) {
            JsonArray parts=model(name).getAsJsonArray("elements");
            for(int i=0;i<parts.size();i++) for(int j=i+1;j<parts.size();j++)
                assertFalse(intersects(parts.get(i).getAsJsonObject(),parts.get(j).getAsJsonObject()),name+" parts "+i+" / "+j);
        }
    }

    @Test void everyMenuVertexFitsItsSelectionAndCollisionAfterFacingRotation() throws Exception {
        for(boolean open:List.of(false,true)) for(Direction facing:Direction.Plane.HORIZONTAL) {
            var state=book.defaultBlockState().setValue(MenuBookBlock.OPEN,open).setValue(MenuBookBlock.FACING,facing);
            AABB selection=state.getShape(null,BlockPos.ZERO).bounds();
            AABB collision=state.getCollisionShape(null,BlockPos.ZERO).bounds();
            double angle=Math.toRadians(switch(facing){case EAST->90;case SOUTH->180;case WEST->270;default->0;});
            for(var element:model(open?"menu_book_open":"menu_book").getAsJsonArray("elements")) {
                JsonObject part=element.getAsJsonObject();double[] lo=vector(part.getAsJsonArray("from")),hi=vector(part.getAsJsonArray("to"));
                double[][] polygon=polygon(part);
                for(double[] xy:polygon) for(double z:new double[]{lo[2],hi[2]}) {
                    double x=8+Math.cos(angle)*(xy[0]-8)-Math.sin(angle)*(z-8);
                    double rotatedZ=8+Math.sin(angle)*(xy[0]-8)+Math.cos(angle)*(z-8);
                    for(AABB bounds:List.of(selection,collision)) assertTrue(contains(bounds,x/16,xy[1]/16,rotatedZ/16),facing+" open="+open+" "+part.get("name"));
                }
            }
        }
    }

    private static boolean contains(AABB box,double x,double y,double z) {
        return x>=box.minX-EPSILON&&x<=box.maxX+EPSILON&&y>=box.minY-EPSILON&&y<=box.maxY+EPSILON&&z>=box.minZ-EPSILON&&z<=box.maxZ+EPSILON;
    }
    private static JsonObject model(String name) throws Exception {
        try(var input=RestaurantModelGeometryTest.class.getResourceAsStream("/assets/reverie_dreams/models/block/"+name+".json")) {
            assertNotNull(input,name);return JsonParser.parseReader(new InputStreamReader(input,StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }
    private static double[] vector(JsonArray array) { return new double[]{array.get(0).getAsDouble(),array.get(1).getAsDouble(),array.get(2).getAsDouble()}; }
    private static double[][] polygon(JsonObject part) {
        double[] lo=vector(part.getAsJsonArray("from")),hi=vector(part.getAsJsonArray("to"));
        double[][] points={{lo[0],lo[1]},{hi[0],lo[1]},{hi[0],hi[1]},{lo[0],hi[1]}};
        if(part.has("rotation")) {
            JsonObject rotation=part.getAsJsonObject("rotation");assertEquals("z",rotation.get("axis").getAsString());
            double angle=Math.toRadians(rotation.get("angle").getAsDouble()),c=Math.cos(angle),s=Math.sin(angle);
            double[] origin=vector(rotation.getAsJsonArray("origin"));
            for(double[] point:points) {double x=point[0]-origin[0],y=point[1]-origin[1];point[0]=origin[0]+c*x-s*y;point[1]=origin[1]+s*x+c*y;}
        }
        return points;
    }
    private static boolean intersects(JsonObject a,JsonObject b) {
        double[] al=vector(a.getAsJsonArray("from")),ah=vector(a.getAsJsonArray("to")),bl=vector(b.getAsJsonArray("from")),bh=vector(b.getAsJsonArray("to"));
        if(Math.min(ah[2],bh[2])-Math.max(al[2],bl[2])<=EPSILON)return false;
        double[][] pa=polygon(a),pb=polygon(b);
        // Separating-axis test: touching cover/page interfaces have zero volume.
        for(double[][] polygon:new double[][][]{pa,pb}) for(int i=0;i<4;i++) {
            double[] p=polygon[i],q=polygon[(i+1)%4];double nx=-(q[1]-p[1]),ny=q[0]-p[0],length=Math.hypot(nx,ny);nx/=length;ny/=length;
            double amin=Double.POSITIVE_INFINITY,amax=Double.NEGATIVE_INFINITY,bmin=Double.POSITIVE_INFINITY,bmax=Double.NEGATIVE_INFINITY;
            for(double[] v:pa){double d=v[0]*nx+v[1]*ny;amin=Math.min(amin,d);amax=Math.max(amax,d);}
            for(double[] v:pb){double d=v[0]*nx+v[1]*ny;bmin=Math.min(bmin,d);bmax=Math.max(bmax,d);}
            if(Math.min(amax,bmax)-Math.max(amin,bmin)<=EPSILON)return false;
        }
        return true;
    }
}
