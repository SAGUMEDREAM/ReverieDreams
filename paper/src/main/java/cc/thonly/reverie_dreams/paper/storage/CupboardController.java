package cc.thonly.reverie_dreams.paper.storage;

import cc.thonly.reverie_dreams.paper.util.Ids;

import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.block.entity.BlockEntityController;
import net.momirealms.craftengine.libraries.nbt.CompoundTag;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.LoggerFactory;

/** Thirty-six independent complete component identities, each with a real positive signed-int count. */
public final class CupboardController extends BlockEntityController {
    public record Entry(int slot,ItemStack prototype,int count){}
    public record Snapshot(List<Entry> entries,boolean quarantined,long totalUnits){}
    private static final String DATA=Ids.MOD_ID + ":cupboard";
    private ItemStack[] prototypes=new ItemStack[36];
    private int[] counts=new int[36];
    private boolean loaded,removed;
    private CompoundTag unreadable;
    CupboardController(BlockEntity entity){super(entity);}
    public boolean available(){return loaded&&!removed&&unreadable==null&&StorageSpecialModule.enabled();}
    public Location location(){return new Location((World)blockEntity.world.world().platformWorld(),blockEntity.pos.x()+.5,blockEntity.pos.y()+.5,blockEntity.pos.z()+.5);}
    public Snapshot snapshot(){thread();var entries=new ArrayList<Entry>();long total=0;for(int i=0;i<36;i++)if(counts[i]>0&&prototypes[i]!=null){entries.add(new Entry(i,prototypes[i].asOne(),counts[i]));total+=counts[i];}return new Snapshot(List.copyOf(entries),unreadable!=null,total);}
    public Entry entry(int slot){thread();slot(slot);return counts[slot]<=0?null:new Entry(slot,prototypes[slot].asOne(),counts[slot]);}
    /** Returns only the actual unaccepted remainder. No input stack is mutated by this method. */
    public ItemStack offer(ItemStack input){
        thread();if(empty(input))return null;var remainder=input.clone();if(!available()||!StorageSpecialModule.acceptsCupboard(input))return remainder;
        int selected=-1;for(int i=0;i<36;i++)if(counts[i]>0&&prototypes[i].isSimilar(input)){selected=i;break;}
        if(selected<0)for(int i=0;i<36;i++)if(counts[i]<=0){selected=i;break;}
        if(selected<0)return remainder;int accepted=(int)Math.min((long)input.getAmount(),Integer.MAX_VALUE-(long)counts[selected]);if(accepted<=0)return remainder;
        prototypes[selected]=input.asOne();counts[selected]+=accepted;remainder.subtract(accepted);changed();return remainder.isEmpty()?null:remainder;
    }
    /** Clamp before debit, so a middle click on an unstackable food never deletes the other63 units. */
    public ItemStack take(int slot,int requested){thread();slot(slot);if(!available()||requested<=0||counts[slot]<=0)return null;
        int moved=Math.min(Math.min(requested,counts[slot]),prototypes[slot].getMaxStackSize());var result=prototypes[slot].clone();result.setAmount(moved);debit(slot,moved);return result;
    }
    public ItemStack takeSame(int slot){thread();slot(slot);if(!available()||counts[slot]<=0)return null;var target=prototypes[slot].asOne();int remaining=target.getMaxStackSize(),moved=0;
        for(int i=0;i<36&&remaining>0;i++)if(counts[i]>0&&prototypes[i].isSimilar(target)){int count=Math.min(remaining,counts[i]);counts[i]-=count;remaining-=count;moved+=count;if(counts[i]==0)prototypes[i]=null;}
        if(moved<=0)return null;target.setAmount(moved);changed();return target;
    }
    public int transferTo(int slot,Inventory destination,int requested){thread();slot(slot);if(!available()||requested<=0||counts[slot]<=0)return 0;
        int offered=Math.min(Math.min(requested,counts[slot]),prototypes[slot].getMaxStackSize());var moving=prototypes[slot].clone();moving.setAmount(offered);
        int left=destination.addItem(moving).values().stream().mapToInt(ItemStack::getAmount).sum(),moved=offered-left;if(moved>0)debit(slot,moved);return moved;
    }
    public int transferFrom(Inventory source,int slot){thread();var item=source.getItem(slot);if(empty(item))return 0;var remainder=offer(item);int moved=item.getAmount()-(empty(remainder)?0:remainder.getAmount());if(moved>0)source.setItem(slot,remainder);return moved;}
    private void debit(int slot,int count){counts[slot]-=count;if(counts[slot]==0)prototypes[slot]=null;changed();}
    @Override public void onLoad(){loaded=true;removed=false;StorageSpecialModule.loaded(this);}
    @Override public void onUnload(){loaded=false;StorageSpecialModule.closeViews(this);StorageSpecialModule.unloaded(this);}
    @Override public void onRemove(){if(removed)return;var drops=snapshot().entries();removed=true;loaded=false;StorageSpecialModule.drops().enqueue(location(),drops);prototypes=new ItemStack[36];counts=new int[36];StorageSpecialModule.closeViews(this);StorageSpecialModule.unloaded(this);}
    @Override public void saveCustomData(CompoundTag outer){if(unreadable!=null){outer.put(DATA,unreadable.copy());return;}var data=new CompoundTag();data.putInt("schema",1);for(int i=0;i<36;i++)if(counts[i]>0){data.putByteArray("item"+i,prototypes[i].asOne().serializeAsBytes());data.putInt("count"+i,counts[i]);}outer.put(DATA,data);}
    @Override public void loadCustomData(CompoundTag outer){var data=outer.getCompound(DATA);if(data==null||data.isEmpty())return;try{
        if(data.getInt("schema",-1)!=1)throw new IllegalArgumentException("Unknown cupboard schema");var decoded=new ItemStack[36];var amounts=new int[36];
        for(int i=0;i<36;i++){byte[] bytes=data.getByteArray("item"+i);int count=data.getInt("count"+i,0);if(count<0)throw new IllegalArgumentException("Negative cupboard count");if(count>0){if(bytes==null||bytes.length==0)throw new IllegalArgumentException("Missing cupboard prototype");decoded[i]=ItemStack.deserializeBytes(bytes);if(empty(decoded[i]))throw new IllegalArgumentException("Empty cupboard prototype");decoded[i]=decoded[i].asOne();amounts[i]=count;}}
        prototypes=decoded;counts=amounts;unreadable=null;
        }catch(RuntimeException failure){unreadable=data.copy();LoggerFactory.getLogger(CupboardController.class).error("Cupboard at {} preserved in quarantine",blockEntity.pos,failure);}}
    void dirty(){if(blockEntity.world!=null)blockEntity.world.blockEntityChanged(blockEntity.pos);}
    private void changed(){dirty();StorageSpecialModule.refresh(this);}
    static void thread(){PlayerFaithService.thread();}
    static boolean empty(ItemStack item){return item==null||item.isEmpty();}
    private static void slot(int slot){if(slot<0||slot>=36)throw new IndexOutOfBoundsException("Cupboard slot "+slot);}
}
