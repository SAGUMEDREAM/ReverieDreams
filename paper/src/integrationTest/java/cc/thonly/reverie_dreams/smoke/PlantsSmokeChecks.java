package cc.thonly.reverie_dreams.smoke;

import cc.thonly.reverie_dreams.paper.plants.PlantsModule;
import net.momirealms.craftengine.bukkit.api.CraftEngineBlocks;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.BiConsumer;
import cc.thonly.reverie_dreams.paper.specialitems2.SpecialItemsExpansion;
import net.momirealms.craftengine.core.block.property.Property;

/** Exercises native sapling generation using the source configured tree, not a replacement tree API. */
final class PlantsSmokeChecks {
    private static final String[] FAMILIES = {"spiritual","lemon","ginkgo","peach","iroha_maple","itaya_maple","hauchiwa_maple"};
    private PlantsSmokeChecks() { }
    static boolean plant(String id) {
        return id.endsWith("_leaves") || id.endsWith("_sapling") || id.endsWith("_leaf_litter")
                || id.equals("reverie_dreams:udumbara_flower") || id.equals("reverie_dreams:tremella");
    }
    static void verify(BiConsumer<Boolean,String> check, World world, boolean restart) throws Exception {
        var definitions = PlantsModule.validateLoadedDefinitions();
        check.accept(definitions.size()==22 && definitions.values().stream().mapToInt(Integer::intValue).sum()==596,
                "all 22 source plants and 596 native states round-trip with seven real configured trees");
        var file = Path.of("probe-plants.yml").toFile(); var saved = YamlConfiguration.loadConfiguration(file);
        for (int i=0;i<FAMILIES.length;i++) {
            int x=40+i*24,z=40; String family=FAMILIES[i], log=family.equals("ginkgo")?"lemon":family;
            for (int cx=(x-8)>>4;cx<=(x+8)>>4;cx++) for (int cz=(z-8)>>4;cz<=(z+8)>>4;cz++) world.getChunkAt(cx,cz);
            Block sapling=world.getBlockAt(x,60,z);
            if (!restart) {
                for (int dx=-2;dx<=2;dx++) for (int dz=-2;dz<=2;dz++) world.getBlockAt(x+dx,59,z+dz).setType(Material.GRASS_BLOCK,false);
                place(sapling,family+"_sapling");
                for (int tries=0;tries<128 && id(sapling).endsWith("_sapling");tries++) sapling.applyBoneMeal(BlockFace.UP);
                int leaves=0,logs=0;Block firstLog=null;
                for (int dx=-8;dx<=8;dx++) for (int dy=0;dy<=24;dy++) for (int dz=-8;dz<=8;dz++) {
                    var block=world.getBlockAt(x+dx,60+dy,z+dz); String name=id(block);
                    if (name.equals("reverie_dreams:"+family+"_leaves") || name.equals("reverie_dreams:"+family+"_fruit_leaves")) leaves++;
                    if (name.equals("reverie_dreams:"+log+"_log")) { logs++; if(firstLog==null)firstLog=block; }
                }
                check.accept(logs>0 && !id(sapling).endsWith("_sapling"),
                        "native bone meal grows source trunk: "+family+", logs="+logs+", base="+id(sapling));
                check.accept(leaves>0,"native source tree contains real custom leaf states: "+family);
                saved.set(family+".id", id(firstLog));saved.set(family+".x",firstLog.getX());
                saved.set(family+".y",firstLog.getY());saved.set(family+".z",firstLog.getZ());
            } else check.accept(id(world.getBlockAt(saved.getInt(family+".x"),saved.getInt(family+".y"),saved.getInt(family+".z")))
                    .equals(saved.getString(family+".id")),"generated source trunk survives restart: "+family);
        }
        if (!restart) saved.save(file);

        for (String family : new String[]{"lemon","ginkgo","peach"}) {
            Block fruit=world.getBlockAt(12,285,12); place(fruit,family+"_fruit_leaves");
            check.accept(PlantsModule.fruitAge(fruit)==3 && PlantsModule.harvestFruit(fruit,null,3)
                            && PlantsModule.fruitAge(fruit)==1 && !PlantsModule.harvestFruit(fruit,null,3),
                    "fruit leaves harvest three source products exactly once and reset to age one: "+family);
            int drops=0;String product=PlantsModule.fruitProducts().get("reverie_dreams:"+family+"_fruit_leaves");
            for (var entity:world.getNearbyEntities(fruit.getLocation(),2,18,2)) if(entity instanceof Item item
                    && SpecialItemsExpansion.id(item.getItemStack()).equals(product)) {
                drops+=item.getItemStack().getAmount();item.remove();
            }
            check.accept(drops==3,"fruit harvest emits exact native custom product count: "+family);
            check.accept(!PlantsModule.tryRandomTick(fruit,8,0) && PlantsModule.tryRandomTick(fruit,15,0)
                            && PlantsModule.fruitAge(fruit)==2 && PlantsModule.boneMealFruit(fruit)
                            && PlantsModule.fruitAge(fruit)==3,
                    "fruit light gate, natural growth and bone meal match source stages: "+family);
            CraftEngineBlocks.remove(fruit,null,false,false,true);
        }
        Block flower=world.getBlockAt(12,285,12);flower.getRelative(BlockFace.DOWN).setType(Material.GRASS_BLOCK,false);
        place(flower,"udumbara_flower");check.accept(PlantsModule.duplicateUdumbara(flower),"source udumbara bone-meal path duplicates the actual flower");
        int flowers=0;
        for(var entity:world.getNearbyEntities(flower.getLocation(),2,2,2))if(entity instanceof Item item
                && SpecialItemsExpansion.id(item.getItemStack()).equals("reverie_dreams:udumbara_flower")) {
            flowers+=item.getItemStack().getAmount();item.remove();
        }
        check.accept(flowers==1,"udumbara produces exactly one real source item");
        CraftEngineBlocks.remove(flower,null,false,false,true);flower.getRelative(BlockFace.DOWN).setType(Material.AIR,false);
        place(world.getBlockAt(11,295,12),"spiritual_log");
        for (int x=12;x<=13;x++) {
            var leaf=CraftEngineBlocks.byId(Key.of("reverie_dreams:spiritual_leaves")).defaultState();
            Property<Boolean> persistent=leaf.getProperty("persistent");
            if(!CraftEngineBlocks.place(world.getBlockAt(x,295,12).getLocation(),leaf.with(persistent,false),false))
                throw new AssertionError("Native distance leaf fixture refused");
        }
    }
    static void complete(BiConsumer<Boolean,String> check,World world) {
        for(int x=12;x<=13;x++) {
            var leaf=CraftEngineBlocks.getCustomBlockState(world.getBlockAt(x,295,12));
            Property<Integer> distance=leaf==null?null:leaf.getProperty("distance");
            check.accept(leaf!=null && leaf.get(distance)==x-11,"native CE leaves propagate source log distance: "+(x-11));
        }
        for(int x=11;x<=13;x++) CraftEngineBlocks.remove(world.getBlockAt(x,295,12),null,false,false,true);
    }
    private static void place(Block block,String path) {
        if(!CraftEngineBlocks.place(block.getLocation(),CraftEngineBlocks.byId(Key.of("reverie_dreams:"+path)).defaultState(),false))
            throw new AssertionError("Plant place failed: "+path);
    }
    private static String id(Block block) {
        var state=CraftEngineBlocks.getCustomBlockState(block);return state==null?"":state.owner().value().id().asString();
    }
}
