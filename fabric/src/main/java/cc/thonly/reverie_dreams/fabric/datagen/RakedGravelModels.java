package cc.thonly.reverie_dreams.fabric.datagen;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.decor.RakedGravelBlock;
import cc.thonly.reverie_dreams.block.decor.RakedGravelGeometry;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import static net.minecraft.client.data.models.BlockModelGenerators.plainVariant;

/** Six groove layouts, each with four endpoint combinations, all using vanilla gravel. */
final class RakedGravelModels {
    private RakedGravelModels() { }
    static void generate(BlockModelGenerators generator, Block block) {
        for (var shape : RakedGravelBlock.Shape.values()) for (int connections = 0; connections < 4; connections++) {
            JsonObject model = model(shape,connections);
            generator.modelOutput.accept(ReverieDreams.id("block/" + RakedGravelBlock.modelName(shape,connections)), () -> model);
        }
        generator.blockStateOutput.accept(MultiVariantGenerator.dispatch(block).with(PropertyDispatch.initial(
                RakedGravelBlock.SHAPE,RakedGravelBlock.NORTH,RakedGravelBlock.EAST,RakedGravelBlock.SOUTH,RakedGravelBlock.WEST)
                .generate((shape,n,e,s,w) -> {
                    int connections = (connected(shape.first,n,e,s,w) ? 1 : 0) | (connected(shape.second,n,e,s,w) ? 2 : 0);
                    return plainVariant(ReverieDreams.id("block/" + RakedGravelBlock.modelName(shape,connections)));
                })));
        JsonObject legacy = new JsonObject();
        legacy.addProperty("parent",ReverieDreams.id("block/" + RakedGravelBlock.modelName(RakedGravelBlock.Shape.NORTH_SOUTH,0)).toString());
        generator.modelOutput.accept(ReverieDreams.id("block/raked_gravel"), () -> legacy);
        generator.registerSimpleItemModel(block,ReverieDreams.id("block/raked_gravel"));
    }
    private static boolean connected(Direction direction, boolean n, boolean e, boolean s, boolean w) {
        return switch (direction) { case NORTH -> n; case EAST -> e; case SOUTH -> s; case WEST -> w; default -> false; };
    }
    private static JsonObject model(RakedGravelBlock.Shape shape, int connections) {
        JsonObject model = new JsonObject(); model.addProperty("parent","minecraft:block/block");
        JsonObject textures = new JsonObject();
        textures.addProperty("particle","minecraft:block/gravel");
        textures.addProperty("gravel","minecraft:block/gravel");
        textures.addProperty("groove","minecraft:block/gray_concrete_powder");
        model.add("textures",textures);
        JsonArray elements = new JsonArray();
        JsonObject base = box(0,0,0,16,RakedGravelGeometry.BASE,16,"gravel");
        base.getAsJsonObject("faces").remove("up"); elements.add(base);
        for (var patch : RakedGravelGeometry.patches(shape,connections)) {
            double top = patch.groove() ? RakedGravelGeometry.GROOVE : RakedGravelGeometry.RIDGE;
            JsonObject part = box(patch.x1(),RakedGravelGeometry.BASE,patch.z1(),patch.x2(),top,patch.z2(),patch.groove() ? "groove" : "gravel");
            JsonObject faces = part.getAsJsonObject("faces"); faces.remove("down");
            if (hidden(shape,connections,patch,Direction.NORTH)) faces.remove("north");
            if (hidden(shape,connections,patch,Direction.EAST)) faces.remove("east");
            if (hidden(shape,connections,patch,Direction.SOUTH)) faces.remove("south");
            if (hidden(shape,connections,patch,Direction.WEST)) faces.remove("west");
            elements.add(part);
        }
        model.add("elements",elements); return model;
    }
    private static boolean hidden(RakedGravelBlock.Shape shape, int connections, RakedGravelGeometry.Patch p, Direction side) {
        if (side == Direction.NORTH && p.z1()==0 || side == Direction.SOUTH && p.z2()==16
                || side == Direction.WEST && p.x1()==0 || side == Direction.EAST && p.x2()==16) return false;
        if (p.groove()) return true;
        if (side.getAxis()==Direction.Axis.Z) {
            int z = side==Direction.NORTH ? p.z1()-1 : p.z2();
            for (int x=p.x1();x<p.x2();x++) if (RakedGravelGeometry.groove(shape,connections,x,z)) return false;
        } else {
            int x = side==Direction.WEST ? p.x1()-1 : p.x2();
            for (int z=p.z1();z<p.z2();z++) if (RakedGravelGeometry.groove(shape,connections,x,z)) return false;
        }
        return true;
    }
    private static JsonObject box(double x1,double y1,double z1,double x2,double y2,double z2,String material) {
        JsonObject element = new JsonObject();element.add("from",array(x1,y1,z1));element.add("to",array(x2,y2,z2));
        JsonObject faces = new JsonObject();
        for (String direction : new String[]{"up","down","north","east","south","west"}) {
            JsonObject face = new JsonObject();face.addProperty("texture","#"+(direction.equals("up") ? material : "gravel"));
            face.add("uv",direction.equals("up") || direction.equals("down") ? array(x1,z1,x2,z2)
                    : array(0,16-y2,direction.equals("north") || direction.equals("south") ? x2-x1 : z2-z1,16-y1));
            faces.add(direction,face);
        }
        element.add("faces",faces);return element;
    }
    private static JsonArray array(double... values) { JsonArray array=new JsonArray();for(double v:values)array.add(v);return array; }
}
