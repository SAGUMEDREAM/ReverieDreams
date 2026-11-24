package cc.thonly.reverie_dreams;

import cc.thonly.reverie_dreams.item.AncientIronEquipment;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.*;
import java.nio.file.*;

/** Small daylight comparison, separate from the already-passed adventure integration test. */
@SuppressWarnings("UnstableApiUsage")
public final class ScarletEquipmentVisualTest implements FabricClientGameTest {
    @Override public void runTest(ClientGameTestContext context){
        Path out=Path.of(System.getProperty("reverie.building.evidence"));
        try(var world=context.worldBuilder().create()){
            context.getInput().resizeWindow(1440,900);
            context.runOnClient(mc->{mc.options.hideGui=true;mc.options.renderDistance().set(4);mc.options.pauseOnLostFocus=false;mc.options.broadcastOptions();});
            var server=world.getServer();server.runCommand("difficulty peaceful");server.runCommand("gamemode creative @p");server.runCommand("time set noon");server.runCommand("weather clear");
            server.runCommand("tp @p .5 200 -5.5 0 0");server.runCommand("forceload add -16 -16 16 16");context.waitTicks(100);
            server.runCommand("fill -8 199 -8 8 199 8 minecraft:stone");
            server.runOnServer(s->{var level=s.overworld();var player=s.getPlayerList().getPlayers().getFirst();
                for(boolean ancient:new boolean[]{false,true}){
                    ArmorStand stand=EntityType.ARMOR_STAND.create(level,EntitySpawnReason.COMMAND);stand.snapTo(ancient?2.5:-1.5,200,.5,180,0);stand.setNoGravity(true);stand.setShowArms(true);
                    stand.setItemSlot(EquipmentSlot.HEAD,new ItemStack(ancient?AncientIronEquipment.HELMET:Items.NETHERITE_HELMET));
                    stand.setItemSlot(EquipmentSlot.CHEST,new ItemStack(ancient?AncientIronEquipment.CHESTPLATE:Items.NETHERITE_CHESTPLATE));
                    stand.setItemSlot(EquipmentSlot.LEGS,new ItemStack(ancient?AncientIronEquipment.LEGGINGS:Items.NETHERITE_LEGGINGS));
                    stand.setItemSlot(EquipmentSlot.FEET,new ItemStack(ancient?AncientIronEquipment.BOOTS:Items.NETHERITE_BOOTS));
                    stand.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(ancient?AncientIronEquipment.SPEAR:Items.NETHERITE_SPEAR));
                    stand.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(ancient?AncientIronEquipment.SWORD:Items.NETHERITE_SWORD));level.addFreshEntity(stand);
                }
                player.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(AncientIronEquipment.SPEAR));player.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(AncientIronEquipment.PICKAXE));
            });server.runCommand("tp @p .5 200 -5.5 0 0");context.waitTicks(40);
            Files.copy(context.takeScreenshot("ancient-iron-front"),out.resolve("screenshots/equipment-front-native.png"),StandardCopyOption.REPLACE_EXISTING);
            server.runCommand("tp @p .5 200 6.5 180 0");context.waitTicks(30);
            Files.copy(context.takeScreenshot("ancient-iron-back"),out.resolve("screenshots/equipment-back-native.png"),StandardCopyOption.REPLACE_EXISTING);
            server.runCommand("tp @p 6.5 200 3.5 115 0");context.waitTicks(30);
            context.runOnClient(mc->mc.options.hideGui=false);
            Files.copy(context.takeScreenshot("ancient-iron-hand"),out.resolve("screenshots/equipment-hand-native.png"),StandardCopyOption.REPLACE_EXISTING);
            Files.writeString(out.resolve("visual-results.json"),"{\"status\":\"PASS\",\"comparison\":\"front: ancient iron left and netherite right; back: netherite left and ancient iron right\",\"views\":[\"front\",\"back\",\"first person spear and pickaxe\"],\"artAcceptance\":\"pending actual human review\",\"polymer\":\"not involved; not run\"}");
        }catch(Exception error){throw new RuntimeException(error);}
    }
}
