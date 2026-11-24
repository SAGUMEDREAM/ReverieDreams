package cc.thonly.reverie_dreams.paper.storage;

import cc.thonly.reverie_dreams.paper.util.Ids;

import net.momirealms.craftengine.bukkit.world.WorldlyContainerHolder;
import net.momirealms.craftengine.bukkit.world.inventory.BukkitWorldlyStorageContainer;
import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.block.entity.BlockEntityController;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.world.WorldPosition;
import net.momirealms.craftengine.libraries.nbt.CompoundTag;
import net.momirealms.craftengine.proxy.bukkit.craftbukkit.inventory.CraftInventoryProxy;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.util.Direction;
import org.slf4j.LoggerFactory;

/** Native27-slot inventory and all-face unrestricted hopper bridge; coin predicate belongs only to the player GUI. */
public final class CashBoxController extends BlockEntityController {
    public record Snapshot(List<ItemStack> items,boolean quarantined){}
    private static final String DATA=Ids.MOD_ID + ":cash_box";
    private final Inventory storage;
    private boolean loaded,removed;
    private CompoundTag unreadable;
    CashBoxController(BlockEntity entity){super(entity);var holder=new WorldlyContainerHolder(player->{},()->new WorldPosition(blockEntity.world.world(),blockEntity.pos.x()+.5,blockEntity.pos.y()+.5,blockEntity.pos.z()+.5));storage=CraftInventoryProxy.INSTANCE.newInstance(CraftEngine.instance().platform().createContainer(new CashContainer(holder,this)));holder.setInventory(storage);}
    public boolean available(){return loaded&&!removed&&unreadable==null&&StorageSpecialModule.enabled();}
    public Location location(){return new Location((World)blockEntity.world.world().platformWorld(),blockEntity.pos.x()+.5,blockEntity.pos.y()+.5,blockEntity.pos.z()+.5);}
    public Snapshot snapshot(){thread();return new Snapshot(Arrays.stream(storage.getContents()).map(item->empty(item)?null:item.clone()).toList(),unreadable!=null);}
    public ItemStack item(int slot){thread();slot(slot);var item=storage.getItem(slot);return empty(item)?null:item.clone();}
    public Inventory automationInventory(){thread();return storage;}
    /** External native-inventory transactions must mark the CE chunk and refresh coin-only views. */
    public void markAutomationChanged(){thread();if(!available())throw new IllegalStateException("Cashbox unavailable");changed();}
    public static boolean coin(ItemStack item){return !empty(item)&&PlayerFaithService.coinValue(StorageSpecialModule.id(item))>0;}
    public ItemStack offerGui(int slot,ItemStack input){return offer(slot,input,true);}
    public ItemStack offerAutomation(int slot,ItemStack input){return offer(slot,input,false);}
    private ItemStack offer(int slot,ItemStack input,boolean gui){thread();slot(slot);if(empty(input))return null;var remainder=input.clone();if(!available()||gui&&!coin(input))return remainder;var previous=storage.getItem(slot);if(!empty(previous)&&!previous.isSimilar(input))return remainder;
        int old=empty(previous)?0:previous.getAmount(),accepted=Math.min(input.getAmount(),Math.min(input.getMaxStackSize(),storage.getMaxStackSize())-old);if(accepted<=0)return remainder;var merged=input.clone();merged.setAmount(old+accepted);storage.setItem(slot,merged);remainder.subtract(accepted);changed();return remainder.isEmpty()?null:remainder;
    }
    public ItemStack takeGui(int slot,int amount){thread();slot(slot);if(!available()||amount<=0||!coin(storage.getItem(slot)))return null;return take(slot,amount);}
    private ItemStack take(int slot,int amount){var stored=storage.getItem(slot);if(empty(stored))return null;var result=stored.clone();result.setAmount(Math.min(amount,result.getAmount()));var remainder=stored.clone();remainder.subtract(result.getAmount());storage.setItem(slot,remainder.isEmpty()?null:remainder);changed();return result;}
    public int transferToPlayer(int slot,Inventory destination){thread();slot(slot);var original=storage.getItem(slot);if(!available()||!coin(original))return 0;int before=original.getAmount();var left=destination.addItem(original.clone()).values();int moved=before-left.stream().mapToInt(ItemStack::getAmount).sum();if(moved>0)take(slot,moved);return moved;}
    public int transferFromPlayer(Inventory source,int slot){thread();var original=source.getItem(slot);if(!available()||!coin(original))return 0;var remainder=original.clone();
        for(int i=0;i<27&&!empty(remainder);i++)if(!empty(storage.getItem(i))&&storage.getItem(i).isSimilar(remainder))remainder=offerGui(i,remainder);
        for(int i=0;i<27&&!empty(remainder);i++)if(empty(storage.getItem(i)))remainder=offerGui(i,remainder);
        int moved=original.getAmount()-(empty(remainder)?0:remainder.getAmount());if(moved>0)source.setItem(slot,remainder);return moved;
    }
    @Override public void onLoad(){loaded=true;removed=false;StorageSpecialModule.loaded(this);}
    @Override public void onUnload(){loaded=false;StorageSpecialModule.closeViews(this);StorageSpecialModule.unloaded(this);}
    @Override public void onRemove(){if(removed)return;removed=true;loaded=false;var drops=new ArrayList<CupboardController.Entry>();for(int i=0;i<27;i++){var item=storage.getItem(i);if(!empty(item))drops.add(new CupboardController.Entry(i,item.asOne(),item.getAmount()));}StorageSpecialModule.drops().enqueue(location(),drops);storage.clear();StorageSpecialModule.closeViews(this);StorageSpecialModule.unloaded(this);}
    @Override public void saveCustomData(CompoundTag outer){if(unreadable!=null){outer.put(DATA,unreadable.copy());return;}var data=new CompoundTag();data.putInt("schema",1);for(int i=0;i<27;i++){var item=storage.getItem(i);if(!empty(item))data.putByteArray("slot"+i,item.serializeAsBytes());}outer.put(DATA,data);}
    @Override public void loadCustomData(CompoundTag outer){var data=outer.getCompound(DATA);if(data==null||data.isEmpty())return;try{if(data.getInt("schema",-1)!=1)throw new IllegalArgumentException("Unknown cashbox schema");var decoded=new ItemStack[27];for(int i=0;i<27;i++){var bytes=data.getByteArray("slot"+i);if(bytes!=null&&bytes.length>0){decoded[i]=ItemStack.deserializeBytes(bytes);if(!empty(decoded[i])&&decoded[i].getAmount()>Math.min(99,decoded[i].getMaxStackSize()))throw new IllegalArgumentException("Oversized cashbox stack");}}storage.setContents(decoded);unreadable=null;}catch(RuntimeException failure){unreadable=data.copy();LoggerFactory.getLogger(CashBoxController.class).error("Cashbox at {} preserved in quarantine",blockEntity.pos,failure);}}
    void dirty(){if(blockEntity.world!=null)blockEntity.world.blockEntityChanged(blockEntity.pos);}
    private void changed(){dirty();StorageSpecialModule.refresh(this);}
    static boolean empty(ItemStack item){return item==null||item.isEmpty();}
    private static void thread(){PlayerFaithService.thread();}
    private static void slot(int slot){if(slot<0||slot>=27)throw new IndexOutOfBoundsException("Cashbox slot "+slot);}
    private static final class CashContainer extends BukkitWorldlyStorageContainer{
        final CashBoxController owner;CashContainer(WorldlyContainerHolder holder,CashBoxController owner){super(holder,27,true,true);this.owner=owner;setMaxStackSize(99);}
        public boolean canPlaceItemThroughFace(int slot,Item item,Direction direction){return owner.available();}
        public boolean canTakeItemThroughFace(int slot,Item item,Direction direction){return owner.available();}
        public void setChanged(){owner.changed();}
    }
}
