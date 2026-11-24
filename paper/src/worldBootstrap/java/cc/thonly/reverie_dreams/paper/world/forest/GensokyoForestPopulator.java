package cc.thonly.reverie_dreams.paper.world.forest;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.keys.tags.BlockTypeTagKeys;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.HeightMap;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
/** OPTIONAL POST-native adaptation. Exact source shapes/density; order and feature seed differ explicitly. */
public final class GensokyoForestPopulator extends BlockPopulator {
 public record Sample(String profile,int x,int y,int z,String nativeState){}
 private final AtomicReference<Sample> lastSample=new AtomicReference<>();
 public Sample lastGeneratedSample(){return lastSample.get();}
 @FunctionalInterface public interface SourceBiome {String at(long worldSeed,int x,int y,int z);}
 private final boolean enabled;private final Set<GensokyoForestType> types;private final SourceBiome biomes;
 private final Map<String,BlockData> states;private final Set<String> substrate,replaceable;
 private final GensokyoForestNoise noise=new GensokyoForestNoise();
 private final AtomicLong successfulTrees=new AtomicLong();
 public GensokyoForestPopulator(boolean enabled,Set<GensokyoForestType> types,SourceBiome biomes,Function<String,BlockData> verifiedParser){
  this.enabled=enabled;this.types=Set.copyOf(types);this.biomes=Objects.requireNonNull(biomes);
  var bindings=new HashMap<String,BlockData>();if(enabled)for(var state:requiredStates(types))bindings.put(state,Objects.requireNonNull(verifiedParser.apply(state),"Unbound forest state "+state).clone());this.states=Map.copyOf(bindings);
  if(enabled){var registry=RegistryAccess.registryAccess().getRegistry(RegistryKey.BLOCK);substrate=registry.getTagValues(BlockTypeTagKeys.SUBSTRATE_OVERWORLD).stream().map(b->b.getKey().asString()).collect(Collectors.toUnmodifiableSet());replaceable=registry.getTagValues(BlockTypeTagKeys.REPLACEABLE_BY_TREES).stream().map(b->b.getKey().asString()).collect(Collectors.toUnmodifiableSet());}else{substrate=Set.of();replaceable=Set.of();}
 }
 public boolean enabled(){return enabled;}
 /** Bounded aggregate only; counts full Planner.place successes, never stores chunk history. */
 public long generatedTrees(){return successfulTrees.get();}
 public String limitation(){return enabled?"Source tree geometry/density enabled AFTER native decoration, with a separate deterministic feature seed; vegetation order differs from original":"Source custom tree layer is disabled (source-forest-trees=false); native biome trees were deliberately removed without a vanilla substitute";}
 public static Set<String> requiredStates(Set<GensokyoForestType> types){var result=new HashSet<String>();for(var type:types)for(var entry:type.mix()){String material=material(entry.profile().settings().species());for(var axis:GensokyoTreeShape.Axis.values())result.add(material+"_log[axis="+axis.name().toLowerCase(Locale.ROOT)+"]");for(int distance=1;distance<=6;distance++)result.add(material+"_leaves[distance="+distance+",persistent=false]");}return Set.copyOf(result);}
 private static String material(GensokyoTreeShape.Species species){return switch(species){case OAK->"minecraft:oak";case DARK_OAK->"minecraft:dark_oak";case BIRCH->"minecraft:birch";case SPRUCE->"minecraft:spruce";case CHERRY->"minecraft:cherry";case IROHA_MAPLE->"reverie_dreams:iroha_maple";case ITAYA_MAPLE->"reverie_dreams:itaya_maple";case HAUCHIWA_MAPLE->"reverie_dreams:hauchiwa_maple";};}
 private static String kind(BlockData data){var value=data.getAsString();int i=value.indexOf('[');return i<0?value:value.substring(0,i);}
 @Override public void populate(WorldInfo info,Random ignored,int chunkX,int chunkZ,LimitedRegion region){
  if(!enabled)return;if(info.getMinHeight()!=0||info.getMaxHeight()!=1536)throw new IllegalStateException("Original forest requires exact1536-height world");
  var tiles=new HashSet<GensokyoForestPlanner.Point>();for(var tile:region.getTileEntities())tiles.add(new GensokyoForestPlanner.Point(tile.getX(),tile.getY(),tile.getZ()));
  final Sample[] currentSample={null};
  var access=new GensokyoForestPlanner.Region(){
   public int minY(){return info.getMinHeight();}public int maxY(){return info.getMaxHeight();}public boolean canWrite(int x,int y,int z){return region.isInRegion(x,y,z);}
   public boolean substrate(int x,int y,int z){return GensokyoForestPopulator.this.substrate.contains(kind(region.getBlockData(x,y,z)));}
   public boolean validTreePosition(int x,int y,int z){var data=region.getBlockData(x,y,z);return data.getMaterial().isAir()||replaceable.contains(kind(data));}
   public boolean fluid(int x,int y,int z){var data=region.getBlockData(x,y,z);return data.getMaterial()==Material.WATER||data.getMaterial()==Material.LAVA||data.getMaterial()==Material.BUBBLE_COLUMN||data instanceof Waterlogged wet&&wet.isWaterlogged();}
   public boolean blockEntity(int x,int y,int z){return tiles.contains(new GensokyoForestPlanner.Point(x,y,z));}
   public void write(GensokyoTreeProfile profile,GensokyoForestPlanner.Write write){var p=write.point();String material=material(profile.settings().species());String state=write.log()?material+"_log[axis="+write.axis().name().toLowerCase(Locale.ROOT)+"]":material+"_leaves[distance="+write.leafDistance()+",persistent=false]";region.setBlockData(p.x(),p.y(),p.z(),states.get(state));if(write.log()&&currentSample[0]==null)currentSample[0]=new Sample(profile.name(),p.x(),p.y(),p.z(),states.get(state).getAsString());}
  };
  for(var type:types.stream().sorted().toList()){
   var random=new GensokyoForestRandom(0);long seed=random.decorationSeed(info.getSeed(),chunkX<<4,chunkZ<<4);random.adaptationSeed(seed,"reverie_dreams:"+type.biomePath());int attempts=noise.attempts(type,chunkX<<4,chunkZ<<4);
   // Paper74 CraftRegionAccessor returns WorldGenLevel.getHeight directly: already first-free.
   for(int i=0;i<attempts;i++){int x=(chunkX<<4)+random.nextInt(16),z=(chunkZ<<4)+random.nextInt(16),y=region.getHighestBlockYAt(x,z,HeightMap.OCEAN_FLOOR_WG);if(y<info.getMinHeight()||y>=info.getMaxHeight()||!biomes.at(info.getSeed(),x,y,z).equals("reverie_dreams:"+type.biomePath()))continue;var profile=GensokyoForestPlanner.select(type,random);long shapeSeed=random.nextLong();currentSample[0]=null;if(GensokyoForestPlanner.place(access,profile,shapeSeed,x,y,z)){successfulTrees.incrementAndGet();lastSample.set(currentSample[0]);}}
  }
 }
}
