package cc.thonly.reverie_dreams;

import cc.thonly.reverie_dreams.entity.Goblin;
import cc.thonly.reverie_dreams.item.AncientIronEquipment;
import cc.thonly.reverie_dreams.world.gensokyo.adventure.*;
import cc.thonly.reverie_dreams.world.gensokyo.gen.GensokyoChunkGenerator;
import com.google.gson.*;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.phys.*;
import java.nio.file.*;
import java.util.*;

/** Actual native entities, anvil processing, authored containers, persistence and rescue. */
@SuppressWarnings("UnstableApiUsage")
public final class ScarletAdventureClientTest implements FabricClientGameTest {
    private final Path out=Path.of(System.getProperty("reverie.building.evidence"));
    private final ResourceKey<Level> dimension=ResourceKey.create(Registries.DIMENSION,Identifier.parse("reverie_dreams:gensokyo"));
    private final List<String> checks=new ArrayList<>();
    private TestSingleplayerContext world;
    private JsonObject layout;
    private void check(boolean yes,String label){if(!yes)throw new AssertionError(label);checks.add(label);}
    private ServerLevel level(net.minecraft.server.MinecraftServer server){return Objects.requireNonNull(server.getLevel(dimension));}
    private BlockPos pos(JsonArray a){return new BlockPos(a.get(0).getAsInt()+4810,a.get(1).getAsInt()+1004,a.get(2).getAsInt()+7046);}
    private ScarletAdventureState state(ServerLevel level){return level.getDataStorage().computeIfAbsent(ScarletAdventureState.TYPE);}
    private void command(String command){world.getServer().runCommand(command);}
    private void advance(ServerLevel level,long amount){((net.minecraft.world.level.storage.ServerLevelData)level.getServer().overworld().getLevelData()).setGameTime(level.getGameTime()+amount);((GensokyoChunkGenerator)level.getChunkSource().getGenerator()).authoredStructures(level).tickAdventure();}
    private UUID binding(ServerLevel level,String id){String value=state(level).get("mob:"+id);return value.isEmpty()?null:UUID.fromString(value);}
    private ArmorStand firstStand(ServerLevel level){var a=layout.getAsJsonArray("armor_stands").get(0).getAsJsonArray();Vec3 p=new Vec3(a.get(0).getAsDouble()+4810,a.get(1).getAsDouble()+1004,a.get(2).getAsDouble()+7046);return level.getEntitiesOfClass(ArmorStand.class,new AABB(p,p).inflate(.1)).getFirst();}

    @Override public void runTest(ClientGameTestContext context){
        Throwable failure=null;Map<String,Object> metrics=new LinkedHashMap<>();
        try {
            Files.createDirectories(out.resolve("screenshots"));
            layout=JsonParser.parseString(Files.readString(Path.of("D:/Gradle/ReverieDreams/common/src/generated/resources/data/reverie_dreams/gensokyo_adventure/scarlet_devil_mansion.json"))).getAsJsonObject();
            world=context.worldBuilder().create();context.getInput().resizeWindow(1440,900);
            context.runOnClient(mc->{mc.options.hideGui=true;mc.options.renderDistance().set(4);mc.options.simulationDistance().set(3);mc.options.pauseOnLostFocus=false;mc.options.broadcastOptions();});
            command("difficulty normal");command("gamemode spectator @p");command("time set noon");command("weather clear");
            command("execute in reverie_dreams:gensokyo run tp @p 4990 1080 7144 90 0");
            command("execute in reverie_dreams:gensokyo run forceload add 4784 7024 5055 7263");
            boolean ready=false;
            for(int attempt=0;attempt<300&&!ready;attempt++){
                boolean[] value={false};world.getServer().runOnServer(server->{var l=level(server);value[0]=layout.getAsJsonArray("treasures").asList().stream().allMatch(v->state(l).has("box:"+v.getAsJsonObject().get("id").getAsString()));});
                ready=value[0];if(!ready)context.waitTicks(10);
            }
            check(ready,"all authored treasure points finish initialization after support chunks");
            boolean displaysReady=false;
            for(int attempt=0;attempt<120&&!displaysReady;attempt++){
                boolean[] value={true};world.getServer().runOnServer(server->{var l=level(server);for(var v:layout.getAsJsonArray("armor_stands")){var a=v.getAsJsonArray();Vec3 p=new Vec3(a.get(0).getAsDouble()+4810,a.get(1).getAsDouble()+1004,a.get(2).getAsDouble()+7046);if(l.getEntitiesOfClass(ArmorStand.class,new AABB(p,p).inflate(.1)).size()!=1)value[0]=false;}});
                displaysReady=value[0];if(!displaysReady)context.waitTicks(5);
            }
            if(!displaysReady)world.getServer().runOnServer(server->{var l=level(server);System.out.println("SCARLET_STAND_DIAGNOSTIC "+l.getEntitiesOfClass(ArmorStand.class,new AABB(4810,1004,7046,5031,1169,7243)).stream().map(e->e.position().toString()).toList());});
            check(displaysReady,"all 34 authored displays finish their separate decoration pass");
            world.getServer().runOnServer(server->{var l=level(server);var player=server.getPlayerList().getPlayers().getFirst();
                Map<String,Item> vanilla=Map.of("helmet",Items.IRON_HELMET,"chestplate",Items.IRON_CHESTPLATE,"leggings",Items.IRON_LEGGINGS,"boots",Items.IRON_BOOTS,"sword",Items.IRON_SWORD,"pickaxe",Items.IRON_PICKAXE,"axe",Items.IRON_AXE,"shovel",Items.IRON_SHOVEL,"hoe",Items.IRON_HOE,"spear",Items.IRON_SPEAR);
                check(AncientIronEquipment.ITEMS.size()==10,"ten independent ancient iron item registrations");
                for(var entry:AncientIronEquipment.ITEMS.entrySet()){
                    ItemStack stack=entry.getValue().getDefaultInstance(),iron=vanilla.get(entry.getKey()).getDefaultInstance();
                    check(stack.getMaxDamage()==iron.getMaxDamage(),entry.getKey()+" has iron maximum durability");
                    check(Objects.equals(stack.get(DataComponents.ATTRIBUTE_MODIFIERS),iron.get(DataComponents.ATTRIBUTE_MODIFIERS)),entry.getKey()+" has exact iron combat attributes");
                    check(Objects.equals(stack.get(DataComponents.ENCHANTABLE),iron.get(DataComponents.ENCHANTABLE)),entry.getKey()+" has iron enchantability");
                    check(stack.get(DataComponents.DAMAGE_RESISTANT)==null,entry.getKey()+" does not inherit netherite fire resistance");
                    stack.setDamageValue(stack.getMaxDamage()/3);AnvilMenu menu=new AnvilMenu(27,player.getInventory());menu.getSlot(0).set(stack);menu.getSlot(1).set(new ItemStack(Items.IRON_INGOT));menu.createResult();
                    ItemStack repaired=menu.getSlot(2).getItem();check(!repaired.isEmpty()&&repaired.getItem()==entry.getValue()&&repaired.getDamageValue()<stack.getDamageValue(),entry.getKey()+" actually repairs with an iron ingot in an anvil");
                }
                int stands=0,pieces=0;
                for(var v:layout.getAsJsonArray("armor_stands")){
                    var a=v.getAsJsonArray();Vec3 p=new Vec3(a.get(0).getAsDouble()+4810,a.get(1).getAsDouble()+1004,a.get(2).getAsDouble()+7046);
                    var found=l.getEntitiesOfClass(ArmorStand.class,new AABB(p,p).inflate(.1));check(found.size()==1,"one source stand at "+p);var stand=found.getFirst();check(stand.showArms(),"source stand keeps its arms");stands++;
                    for(var slot:new EquipmentSlot[]{EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET}){var s=stand.getItemBySlot(slot);double remaining=(s.getMaxDamage()-s.getDamageValue())/(double)s.getMaxDamage();check(AncientIronEquipment.ITEMS.containsValue(s.getItem())&&remaining>=.70&&remaining<=.88,"source display "+slot+" is ancient iron with 70-88 percent durability");pieces++;}
                }
                metrics.put("armorStands",stands);metrics.put("authoredArmorPieces",pieces);
                ArmorStand legacy=firstStand(l);legacy.removeTag("rd_ancient_iron_v1");legacy.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.NETHERITE_SWORD));legacy.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(Items.NETHERITE_PICKAXE));
                ArmorStand other=EntityType.ARMOR_STAND.create(l,EntitySpawnReason.COMMAND);other.snapTo(4911.5,1056,7144.5,0,0);other.setNoGravity(true);other.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.NETHERITE_SWORD));l.addFreshEntity(other);advance(l,21);
                check(legacy.getMainHandItem().is(AncientIronEquipment.SWORD)&&legacy.getOffhandItem().is(AncientIronEquipment.PICKAXE),"authored armed legacy stand converts BOTH hands");
                for(var s:List.of(legacy.getMainHandItem(),legacy.getOffhandItem()))check(s.getDamageValue()>=30&&s.getDamageValue()<=75,"converted hand tool retains 70-88 percent iron durability");
                check(other.getMainHandItem().is(Items.NETHERITE_SWORD),"unrelated player armor stand inside the mansion is unchanged");other.discard();
                for(var v:layout.getAsJsonArray("treasures")){
                    var q=v.getAsJsonObject();var be=(RandomizableContainerBlockEntity)l.getBlockEntity(pos(q.getAsJsonArray("pos")));
                    check(be.getLootTable()!=null&&be.getLootTable().equals(ScarletChestLoot.key(q.get("tier").getAsString())),"correct themed loot tier "+q.get("id"));
                    check(!be.isEmpty(),"treasure rolls nonempty contents "+q.get("id"));
                    for(int slot=0;slot<be.getContainerSize();slot++){check(!BuiltInRegistries.ITEM.getKey(be.getItem(slot).getItem()).getPath().startsWith("netherite_"),"treasure contains no netherite progression skip");be.setItem(slot,ItemStack.EMPTY);}be.setChanged();
                }
            });
            command("gamemode creative @p");
            world.getServer().runOnServer(server->{var l=level(server);var player=server.getPlayerList().getPlayers().getFirst();
                for(var v:layout.getAsJsonArray("encounters")){var q=v.getAsJsonObject();BlockPos p=pos(q.getAsJsonArray("pos"));player.snapTo(p.getX()+20,p.getY(),p.getZ()+.5,0,0);advance(l,21);}
                int cells=0,maids=0,friendly=0;
                for(var v:layout.getAsJsonArray("encounters")){var q=v.getAsJsonObject();String id=q.get("id").getAsString();UUID uuid=binding(l,id);check(uuid!=null,"encounter owns an actual entity "+id);var mob=(Mob)l.getEntity(uuid);check(mob!=null&&mob.isAlive(),"encounter entity actually exists "+id);
                    if(mob instanceof Goblin goblin){cells++;check(!goblin.getMainHandItem().is(Items.NETHERITE_PICKAXE)&&!goblin.getMainHandItem().is(Items.DIAMOND_PICKAXE),"prisoner has restrained equipment");}
                    else maids++;
                    if(q.get("friendly").getAsBoolean()){friendly++;check(mob.entityTags().contains(ScarletAdventure.FRIENDLY)&&mob.isCustomNameVisible(),"rescuable prisoner is visibly identified");mob.setTarget(player);check(mob.getTarget()==null&&!mob.canAttack(player),"rescuable prisoner does not attack a player");}
                }
                long expectedMaids=layout.getAsJsonArray("encounters").asList().stream().filter(v->v.getAsJsonObject().get("kind").getAsString().equals("maid")).count();
                check(cells==197&&friendly==49,"all 197 prison points include 49 actually friendly prisoners");check(maids==expectedMaids,"all authored maid guard points can activate");metrics.put("prisoners",cells);metrics.put("rescuablePrisoners",friendly);metrics.put("maidGuards",maids);
            });
            JsonObject rescued=layout.getAsJsonArray("encounters").asList().stream().map(JsonElement::getAsJsonObject).filter(q->q.get("friendly").getAsBoolean()).findFirst().orElseThrow();
            BlockPos rescuePos=pos(rescued.getAsJsonArray("pos"));var door=pos(rescued.getAsJsonArray("doors").get(0).getAsJsonArray());
            command("execute in reverie_dreams:gensokyo run tp @p "+(door.getX()+.5)+" "+door.getY()+" "+(door.getZ()+3.5)+" 180 10");
            world.getServer().runOnServer(server->{var l=level(server);l.destroyBlock(door,false);});
            boolean freed=false;for(int attempt=0;attempt<120&&!freed;attempt++){boolean[] value={false};world.getServer().runOnServer(server->value[0]=state(level(server)).has("retired:"+rescued.get("id").getAsString()));freed=value[0];if(!freed)context.waitTicks(5);}
            check(freed,"opening a real cell lets a friendly goblin path out and retire its spawn");
            Files.copy(context.takeScreenshot("scarlet-prison"),out.resolve("screenshots/prison-native.png"),StandardCopyOption.REPLACE_EXISTING);
            JsonObject enemy=layout.getAsJsonArray("encounters").get(1).getAsJsonObject();String enemyId=enemy.get("id").getAsString();
            world.getServer().runOnServer(server->{var l=level(server);var player=server.getPlayerList().getPlayers().getFirst();BlockPos p=pos(enemy.getAsJsonArray("pos"));player.snapTo(p.getX()+20,p.getY(),p.getZ()+.5,0,0);
                var mob=(LivingEntity)l.getEntity(binding(l,enemyId));mob.setHealth(0);mob.die(l.damageSources().genericKill());check(binding(l,enemyId)==null,"actual mob death releases only its own encounter binding");
                advance(l,20*60*20);check(binding(l,enemyId)==null,"camping at a cleared encounter does not refill it");
                player.snapTo(8200,1100,5000,0,0);advance(l,21);advance(l,20*60*18+21);player.snapTo(p.getX()+20,p.getY(),p.getZ()+.5,0,0);advance(l,21);
                check(binding(l,enemyId)!=null&&l.getEntity(binding(l,enemyId)).isAlive(),"leaving for the full cooldown allows one replacement");
                check(state(l).has("retired:"+rescued.get("id").getAsString())&&binding(l,rescued.get("id").getAsString())==null,"rescued prisoner is never restocked");
            });
            command("touhou gensokyo repair reverie_dreams:scarlet_devil_mansion");context.waitTicks(200);
            world.getServer().runOnServer(server->{var l=level(server);for(var v:layout.getAsJsonArray("treasures")){var q=v.getAsJsonObject();var be=(RandomizableContainerBlockEntity)l.getBlockEntity(pos(q.getAsJsonArray("pos")));check(be!=null&&be.getLootTable()==null&&be.isEmpty(),"repair cannot refill an exhausted treasure "+q.get("id"));}});
            command("effect give @p minecraft:night_vision infinite 0 true");command("execute in reverie_dreams:gensokyo run tp @p 4887 1057 7206 135 10");context.waitTicks(40);
            Files.copy(context.takeScreenshot("ancient-iron-display"),out.resolve("screenshots/ancient-iron-display-native.png"),StandardCopyOption.REPLACE_EXISTING);
            Map<String,String> bindings=new HashMap<>();world.getServer().runOnServer(server->{var l=level(server);for(var v:layout.getAsJsonArray("encounters")){String id=v.getAsJsonObject().get("id").getAsString();String uuid=state(l).get("mob:"+id);if(!uuid.isEmpty())bindings.put(id,uuid);}});
            var save=world.getWorldSave();world.close();world=null;world=save.open();context.waitTicks(200);
            world.getServer().runOnServer(server->{var l=level(server);for(var e:bindings.entrySet()){check(state(l).get("mob:"+e.getKey()).equals(e.getValue()),"encounter ownership survives a full save/reopen "+e.getKey());check(l.getEntity(UUID.fromString(e.getValue()))!=null,"saved encounter entity reloads "+e.getKey());}
                for(var v:layout.getAsJsonArray("treasures")){var q=v.getAsJsonObject();var be=(RandomizableContainerBlockEntity)l.getBlockEntity(pos(q.getAsJsonArray("pos")));check(be!=null&&be.getLootTable()==null&&be.isEmpty(),"shared treasure remains exhausted after reopen "+q.get("id"));}
                check(state(l).has("retired:"+rescued.get("id").getAsString()),"rescue completion survives save/reopen");
            });metrics.put("savedEncounterBindings",bindings.size());
        }catch(Throwable error){failure=error;error.printStackTrace();}
        finally{if(world!=null)try{world.close();}catch(Throwable error){if(failure==null)failure=error;else failure.addSuppressed(error);}world=null;
            try{Files.writeString(out.resolve("adventure-results.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("status",failure==null?"PASS":"FAIL","failure",failure==null?"":failure.toString(),"checks",checks,"metrics",metrics,"polymer","not involved; not run")));}catch(Exception error){throw new RuntimeException(error);}}
        if(failure!=null)throw new AssertionError("Scarlet adventure validation failed",failure);
        System.out.println("SCARLET_ADVENTURE_NATIVE_PASS checks="+checks.size());
    }
}
