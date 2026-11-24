package cc.thonly.reverie_dreams.paper.npc;

import cc.thonly.reverie_dreams.paper.util.Ids;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.bukkit.api.CraftEngineBlocks;
import net.momirealms.craftengine.bukkit.block.entity.SimpleStorageBlockEntityController;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.proxy.bukkit.craftbukkit.inventory.CraftInventoryProxy;
import net.momirealms.craftengine.proxy.minecraft.world.ContainerProxy;
import cc.thonly.reverie_dreams.paper.storage.CashBoxController;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Barrel;
import org.bukkit.block.Container;
import org.bukkit.block.Furnace;
import org.bukkit.inventory.Inventory;
import org.bukkit.loot.Lootable;
import org.bukkit.World;
import org.bukkit.block.data.type.Chest.Type;

/** Actual loaded vanilla/CE containers; no snapshot inventories, loot unpacking or cupboard alias. */
final class NPCContainerWorld {
    record Access(Block block,List<Block> parts,Inventory inventory,List<Object> identities,Runnable dirty){
        Access{parts=List.copyOf(parts);identities=List.copyOf(identities);}
        boolean same(Access other){return other!=null&&parts.equals(other.parts)&&identities.equals(other.identities);}
    }
    static long order(Block block){return ((long)block.getX()&0x3ffffffL)<<38|((long)block.getZ()&0x3ffffffL)<<12|((long)block.getY()&0xfffL);}
    static boolean loaded(Block block){return block.getWorld().isChunkLoaded(block.getX()>>4,block.getZ()>>4);}
    static List<Block> positions(World world,NPCWorkSite area){
        var result=new HashSet<Block>();var ce=BukkitAdaptor.adapt(world).storageWorld();
        for(int x=(area.x()-area.radius())>>4;x<=(area.x()+area.radius())>>4;x++)for(int z=(area.z()-area.radius())>>4;z<=(area.z()+area.radius())>>4;z++){
            if(!world.isChunkLoaded(x,z))continue;
            for(var tile:world.getChunkAt(x,z).getTileEntities(false))if(area.contains(tile.getLocation()))result.add(tile.getBlock());
            var chunk=ce.getChunkAtIfLoaded(x,z);if(chunk!=null)for(var entity:List.copyOf(chunk.blockEntities()))if(entity.isValid()&&area.contains(entity.pos.x(),entity.pos.y(),entity.pos.z()))result.add(world.getBlockAt(entity.pos.x(),entity.pos.y(),entity.pos.z()));
        }return result.stream().sorted(Comparator.comparingLong(NPCContainerWorld::order)).toList();
    }
    static boolean usable(Container state){return !state.isLocked()&&(!(state instanceof Lootable loot)||loot.getLootTable()==null)&&(!(state instanceof Chest chest)||!chest.isBlocked());}
    static Access storage(Block block){
        if(!loaded(block))return null;var custom=CraftEngineBlocks.getCustomBlockState(block);
        if(custom!=null){
            String id=custom.owner().value().id().asString();if(!Set.of(Ids.MOD_ID + ":silver_chest",Ids.MOD_ID + ":wooden_box",Ids.MOD_ID + ":cash_box").contains(id))return null;
            var entity=BukkitAdaptor.adapt(block.getWorld()).storageWorld().getBlockEntityAtIfLoaded(new BlockPos(block.getX(),block.getY(),block.getZ()));if(entity==null||!entity.isValid())return null;
            if(id.equals(Ids.MOD_ID + ":cash_box")){var cash=entity.controller.get(CashBoxController.class,0);if(cash==null||!cash.available())return null;return new Access(block,List.of(block),cash.automationInventory(),List.of(entity,cash),cash::markAutomationChanged);}
            var simple=entity.controller.get(SimpleStorageBlockEntityController.class,0);var inventory=simple==null?null:simple.inventory();if(inventory==null||inventory.getSize()!=27)return null;
            return new Access(block,List.of(block),inventory,List.of(entity,simple),()->entity.world.blockEntityChanged(entity.pos));
        }
        var state=block.getState(false);if(!(state instanceof Chest)&&!(state instanceof Barrel)||!(state instanceof Container container)||!usable(container))return null;
        if(state instanceof Chest chest&&block.getBlockData() instanceof org.bukkit.block.data.type.Chest data&&data.getType()!=Type.SINGLE){
            var facing=data.getFacing();BlockFace clockwise=switch(facing){case NORTH->BlockFace.EAST;case EAST->BlockFace.SOUTH;case SOUTH->BlockFace.WEST;case WEST->BlockFace.NORTH;default->BlockFace.SELF;};
            var direction=data.getType()==Type.LEFT?clockwise:clockwise.getOppositeFace();var partner=block.getRelative(direction);
            if(!loaded(partner)||partner.getType()!=block.getType()||!(partner.getState(false) instanceof Chest other)||!usable(other)||!(partner.getBlockData() instanceof org.bukkit.block.data.type.Chest otherData)||otherData.getFacing()!=data.getFacing()||otherData.getType()==data.getType()||otherData.getType()==Type.SINGLE)return null;
            if(order(partner)<order(block))return storage(partner);
            var inventory=chest.getInventory();if(inventory.getSize()!=54)return null;
            return new Access(block,List.of(block,partner),inventory,List.of(chest.getBlockInventory(),other.getBlockInventory()),()->dirty(inventory));
        }
        var inventory=container.getInventory();return new Access(block,List.of(block),inventory,List.of(inventory),()->dirty(inventory));
    }
    static Access furnace(Block block){if(!loaded(block)||CraftEngineBlocks.getCustomBlockState(block)!=null||!(block.getState(false) instanceof Furnace furnace))return null;var inventory=furnace.getInventory();return new Access(block,List.of(block),inventory,List.of(inventory),()->dirty(inventory));}
    static void dirty(Inventory inventory){ContainerProxy.INSTANCE.setChanged(CraftInventoryProxy.INSTANCE.getInventory(inventory));}
}
