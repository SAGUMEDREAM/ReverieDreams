package cc.thonly.reverie_dreams.fabric.datagen;

import com.google.gson.*;
import java.nio.file.*;
import java.util.*;

/** Integer UV geometry: one texel per model unit, timber cabinets and copper/iron mechanisms. */
final class RedstoneDeviceModels {
    private RedstoneDeviceModels() {}
    private static JsonArray array(double... values) { var out = new JsonArray(); for (double v : values) out.add(v); return out; }
    private static JsonObject face(String texture, double... uv) {
        var face = new JsonObject(); face.addProperty("texture", "#" + texture); face.add("uv", array(uv)); return face;
    }
    private static JsonObject root(boolean powered, String name) {
        var model = new JsonObject(); model.addProperty("parent", "minecraft:block/block");
        var textures = new JsonObject();
        textures.addProperty("particle", "minecraft:block/spruce_planks");
        textures.addProperty("wood", "minecraft:block/spruce_planks");
        textures.addProperty("frame", "minecraft:block/stripped_spruce_log");
        textures.addProperty("copper", "minecraft:block/copper_block");
        textures.addProperty("iron", "minecraft:block/iron_block");
        textures.addProperty("dark", "minecraft:block/polished_blackstone");
        textures.addProperty("dial", "minecraft:block/quartz_block_side");
        textures.addProperty("indicator", powered ? "minecraft:block/redstone_block" : "minecraft:block/red_nether_bricks");
        if (name != null) textures.addProperty("rail", "reverie_dreams:block/" + name + (powered ? "_on" : ""));
        model.add("textures", textures); return model;
    }
    private static void box(JsonArray out, String texture, double x, double y, double z, double xx, double yy, double zz) {
        var part = new JsonObject(); part.add("from", array(x,y,z)); part.add("to", array(xx,yy,zz));
        var faces = new JsonObject();
        for (String side : List.of("north","south","east","west","up","down")) {
            JsonObject f = switch (side) {
                case "north", "south" -> face(texture, x, 16-yy, xx, 16-y);
                case "east", "west" -> face(texture, z, 16-yy, zz, 16-y);
                default -> face(texture, x, z, xx, zz);
            };
            if (side.equals("north") && z==0 || side.equals("south") && zz==16 || side.equals("east") && xx==16
                    || side.equals("west") && x==0 || side.equals("up") && yy==16 || side.equals("down") && y==0)
                f.addProperty("cullface", side);
            faces.add(side, f);
        }
        part.add("faces", faces); out.add(part);
    }
    static JsonObject model(String name, boolean powered) {
        var model = root(powered, null); var e = new JsonArray();
        box(e,"wood",2,2,4,14,14,15);
        box(e,"wood",1,2,2,2,14,15); box(e,"wood",14,2,2,15,14,15);
        box(e,"frame",0,0,0,16,2,16); box(e,"frame",0,14,0,16,16,16);
        for (int x : new int[]{0,14}) for (int z : new int[]{0,14}) box(e,"frame",x,2,z,x+2,14,z+2);
        // Copper straps around the cabinet, with separate iron fasteners.
        for (int x : new int[]{0,15}) box(e,"copper",x,7,2,x+1,9,14);
        box(e,"copper",2,7,15,14,9,16);
        for (int x : new int[]{1,14}) for (int y : new int[]{1,14}) box(e,"iron",x,y,0,x+1,y+1,1);
        switch (name) {
            case "remote_server" -> {
                box(e,"dark",4,3,1,12,5,4); // transformer foot
                box(e,"iron",7,5,1,9,13,4);
                for (int y : new int[]{5,7,9,11}) box(e,"copper",5,y,0,11,y+1,3);
                box(e,"iron",3,12,1,13,13,4);
                box(e,"indicator",7,3,0,9,4,1);
            }
            case "remote_client" -> {
                box(e,"dark",4,3,1,12,5,4);
                box(e,"copper",4,7,0,12,8,4);
                box(e,"iron",4,8,0,5,13,3); box(e,"iron",11,8,0,12,13,3);
                box(e,"copper",3,12,0,6,13,4); box(e,"copper",10,12,0,13,13,4);
                box(e,"iron",7,5,1,9,10,4); box(e,"indicator",7,3,0,9,5,1);
            }
            case "signal_delayer" -> {
                box(e,"dark",3,3,3,13,13,5);
                box(e,"dial",4,4,2,12,12,4);
                box(e,"copper",3,5,0,4,11,4); box(e,"copper",12,5,0,13,11,4);
                box(e,"copper",5,3,0,11,4,4); box(e,"copper",5,12,0,11,13,4);
                for (int x : new int[]{4,11}) for (int y : new int[]{4,11}) box(e,"copper",x,y,0,x+1,y+1,4);
                box(e,"dark",7,7,1,8,11,3); box(e,"dark",8,7,1,11,8,3);
                box(e,"indicator",13,3,0,14,4,1);
            }
            default -> {
                box(e,"copper",3,3,0,13,4,5); box(e,"copper",3,12,0,13,13,5);
                box(e,"copper",3,4,0,4,12,5); box(e,"copper",12,4,0,13,12,5);
                box(e,"dark",4,4,3,12,12,5);
                box(e,"iron",5,5,2,11,11,4);
                box(e,"dark",7,7,1,9,9,3);
                for (int y : new int[]{4,7,10}) box(e,"frame",4,y,0,12,y+1,1);
                box(e,"indicator",13,3,0,14,4,1);
            }
        }
        model.add("elements",mesh(e)); return model;
    }
    static JsonObject rail(String name, boolean powered, int slope) {
        var model = root(powered, name); model.addProperty("ambientocclusion",false);
        var e = new JsonArray(); var base = new JsonObject(); base.add("from",array(0,1,0)); base.add("to",array(16,1,16));
        var faces = new JsonObject(); faces.add("up",face("rail",0,0,16,16)); faces.add("down",face("rail",0,16,16,0));
        base.add("faces",faces); e.add(base);
        box(e,"copper",4,1,3,5,2,13); box(e,"copper",11,1,3,12,2,13);
        box(e,"copper",5,1,3,11,2,4); box(e,"copper",5,1,12,11,2,13);
        if (name.equals("rail_controller_block")) {
            box(e,"dark",5,1,5,11,2,11); box(e,"indicator",6,1,6,10,2,10);
        } else {
            for (int z : new int[]{5,9}) {
                box(e,"iron",5,1,z,11,2,z+2); box(e,"indicator",7,1,z,9,2,z+2);
            }
        }
        JsonArray hardware = new JsonArray();
        for (int i=1;i<e.size();i++) hardware.add(e.get(i));
        JsonArray merged = new JsonArray(); merged.add(base);
        for (var item : mesh(hardware)) {
            var part=item.getAsJsonObject();
            // The rail plane already supplies the underside.
            if (part.getAsJsonObject("faces").has("down")) continue;
            merged.add(part);
        }
        e = merged;
        if (slope!=0) for (var item : e) {
            var part=item.getAsJsonObject();
            for (String key:List.of("from","to")) part.getAsJsonArray(key).set(1,new JsonPrimitive(part.getAsJsonArray(key).get(1).getAsDouble()+8));
            var rotation=new JsonObject(); rotation.add("origin",array(8,9,8)); rotation.addProperty("axis","x"); rotation.addProperty("angle",slope*45); rotation.addProperty("rescale",true); part.add("rotation",rotation);
            for (var entry:part.getAsJsonObject("faces").entrySet()) entry.getValue().getAsJsonObject().remove("cullface");
        }
        model.add("elements",e); return model;
    }

    /** Union the integer mechanisms, remove hidden/overlapping faces, merge coplanar texels. */
    private static JsonArray mesh(JsonArray boxes) {
        String[][][] cells = new String[16][16][16];
        for (var item : boxes) {
            var b=item.getAsJsonObject(); var f=b.getAsJsonArray("from"); var t=b.getAsJsonArray("to");
            String material=b.getAsJsonObject("faces").getAsJsonObject("up").get("texture").getAsString().substring(1);
            for(int x=f.get(0).getAsInt();x<t.get(0).getAsInt();x++) for(int y=f.get(1).getAsInt();y<t.get(1).getAsInt();y++)
                for(int z=f.get(2).getAsInt();z<t.get(2).getAsInt();z++) cells[x][y][z]=material;
        }
        var out=new JsonArray();
        for (String side:List.of("north","south","east","west","up","down")) {
            int axis=side.equals("east")||side.equals("west")?0:side.equals("up")||side.equals("down")?1:2;
            int uAxis=axis==0?2:0, vAxis=axis==1?2:1;
            boolean positive=side.equals("east")||side.equals("up")||side.equals("south");
            for(int plane=0;plane<=16;plane++) {
                String[][] grid=new String[16][16];
                int a=positive?plane-1:plane;
                if(a<0||a>=16)continue;
                for(int u=0;u<16;u++)for(int v=0;v<16;v++) {
                    int[] at=new int[3];at[axis]=a;at[uAxis]=u;at[vAxis]=v;
                    String material=cells[at[0]][at[1]][at[2]];if(material==null)continue;
                    at[axis]+=positive?1:-1;
                    if(at[axis]<0||at[axis]>=16||cells[at[0]][at[1]][at[2]]==null) grid[u][v]=material;
                }
                for(int v=0;v<16;v++)for(int u=0;u<16;u++) {
                    String material=grid[u][v];if(material==null)continue;
                    int width=1,height=1;
                    while(u+width<16&&material.equals(grid[u+width][v]))width++;
                    outer:while(v+height<16) {for(int n=0;n<width;n++)if(!material.equals(grid[u+n][v+height]))break outer;height++;}
                    for(int x=0;x<width;x++)for(int y=0;y<height;y++)grid[u+x][v+y]=null;
                    double[] from=new double[3],to=new double[3];from[axis]=to[axis]=plane;
                    from[uAxis]=u;to[uAxis]=u+width;from[vAxis]=v;to[vAxis]=v+height;
                    var part=new JsonObject();part.add("from",array(from));part.add("to",array(to));
                    var surfaces=new JsonObject();var surface=axis==1?face(material,u,v,u+width,v+height):face(material,u,16-v-height,u+width,16-v);
                    if(plane==0||plane==16)surface.addProperty("cullface",side);
                    surfaces.add(side,surface);part.add("faces",surfaces);out.add(part);
                }
            }
        }
        return out;
    }

    public static void main(String[] args) throws Exception {
        Path output = Path.of(args[0]); Files.createDirectories(output);
        var gson = new GsonBuilder().setPrettyPrinting().create();
        for (String name : List.of("remote_server", "remote_client", "signal_delayer", "speaker"))
            for (boolean on : new boolean[]{false, true}) Files.writeString(output.resolve(name + (on ? "_on" : "") + ".json"), gson.toJson(model(name, on)) + "\n");
        for (String name : List.of("rail_controller_block", "signal_rails")) for (boolean on : new boolean[]{false, true})
            for (int slope : new int[]{0, 1, -1}) {
                String suffix = slope == 0 ? "" : slope > 0 ? "_raised_ne" : "_raised_sw";
                Files.writeString(output.resolve(name + (on ? "_on" : "") + suffix + ".json"), gson.toJson(rail(name, on, slope)) + "\n");
            }
    }
}
