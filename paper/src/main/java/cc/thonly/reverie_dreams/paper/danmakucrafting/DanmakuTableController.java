package cc.thonly.reverie_dreams.paper.danmakucrafting;

import cc.thonly.reverie_dreams.paper.util.Ids;

import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.block.entity.BlockEntityController;
import net.momirealms.craftengine.libraries.nbt.CompoundTag;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import org.slf4j.LoggerFactory;

/** Source five-slot crafting table or two-slot strength table, with independent CE chunk persistence. */
public final class DanmakuTableController extends BlockEntityController {
    public enum Kind { CRAFTING, STRENGTH }
    public enum Status { SUCCESS, UNAVAILABLE, NO_RECIPE, UNSUPPORTED_OUTPUT, INVENTORY_FULL }
    public record Result(Status status,String recipeId,ItemStack output) {}
    public record Snapshot(Kind kind,List<ItemStack> items,boolean quarantined) {}
    private static final String DATA=Ids.MOD_ID + ":danmaku_table";
    private final Kind kind;
    private ItemStack[] items;
    private boolean loaded,removed;
    private CompoundTag unreadable;
    DanmakuTableController(BlockEntity entity,Kind kind){super(entity);this.kind=kind;items=new ItemStack[kind==Kind.CRAFTING?5:2];}
    public Kind kind(){return kind;}
    public boolean available(){return loaded&&!removed&&unreadable==null&&DanmakuCraftingModule.enabled();}
    public Snapshot snapshot(){thread();return new Snapshot(kind,Arrays.asList(copies(items)),unreadable!=null);}
    public Location location(){return new Location((World)blockEntity.world.world().platformWorld(),blockEntity.pos.x()+.5,blockEntity.pos.y()+.5,blockEntity.pos.z()+.5);}
    public ItemStack item(int slot){thread();slot(slot);return empty(items[slot])?null:items[slot].clone();}
    public ItemStack offer(int slot,ItemStack input){
        thread();slot(slot);if(empty(input))return null;ItemStack remainder=input.clone();
        if(!available()||!accepts(slot,input)||!empty(items[slot])&&!items[slot].isSimilar(input))return remainder;
        int old=empty(items[slot])?0:items[slot].getAmount(),moved=Math.min(input.getAmount(),input.getMaxStackSize()-old);
        if(moved<=0)return remainder;items[slot]=input.clone();items[slot].setAmount(old+moved);remainder.subtract(moved);changed();return remainder.isEmpty()?null:remainder;
    }
    public boolean accepts(int slot,ItemStack item){
        slot(slot);if(kind==Kind.STRENGTH||empty(item))return true;String id=DanmakuCraftingRules.id(item);
        return switch(slot){case 0->id.startsWith("minecraft:")&&id.endsWith("_dye");case 1->id.equals(Ids.MOD_ID + ":danmaku_core");case 2->id.equals(Ids.MOD_ID + ":power");case 3->id.equals(Ids.MOD_ID + ":point");case 4->id.equals(Ids.MOD_ID + ":danmaku_recipe_creator");default->false;};
    }
    public ItemStack take(int slot,int amount){thread();slot(slot);if(!available()||amount<=0||empty(items[slot]))return null;ItemStack result=items[slot].clone();result.setAmount(Math.min(amount,result.getAmount()));items[slot].subtract(result.getAmount());if(items[slot].isEmpty())items[slot]=null;changed();return result;}
    public DanmakuCraftingRules.Plan preview(){thread();if(!available())return null;try{return kind==Kind.CRAFTING?DanmakuCraftingModule.rules().table(copies(items)):DanmakuCraftingModule.rules().strengthen(copies(items));}catch(RuntimeException invalid){return null;}}
    /** The returned output transfers ownership to the caller exactly once; it is not a preview or another stored slot. */
    public Result craft(){return craftTo(null);}
    public Result craftTo(Inventory destination){
        thread();if(!available())return new Result(Status.UNAVAILABLE,"",null);var plan=preview();
        if(plan==null)return new Result(Status.NO_RECIPE,"",null);if(empty(plan.output()))return new Result(Status.UNSUPPORTED_OUTPUT,plan.recipeId(),null);
        if(destination!=null&&!fits(destination,plan.output()))return new Result(Status.INVENTORY_FULL,plan.recipeId(),null);
        ItemStack[] next=copies(items);for(int i=0;i<next.length;i++){next[i].subtract(plan.costs()[i]);if(next[i].isEmpty())next[i]=null;}
        items=next;changed();ItemStack output=plan.output().clone();
        if(destination!=null){var leftovers=destination.addItem(output);if(!leftovers.isEmpty())throw new IllegalStateException("Inventory changed during synchronous danmaku craft");return new Result(Status.SUCCESS,plan.recipeId(),null);}
        return new Result(Status.SUCCESS,plan.recipeId(),output);
    }
    private static boolean fits(Inventory inventory,ItemStack output){int remaining=output.getAmount();for(ItemStack stored:inventory.getStorageContents()){remaining-=empty(stored)?Math.min(output.getMaxStackSize(),inventory.getMaxStackSize()):stored.isSimilar(output)?Math.max(0,Math.min(stored.getMaxStackSize(),inventory.getMaxStackSize())-stored.getAmount()):0;if(remaining<=0)return true;}return false;}
    @Override public void onLoad(){loaded=true;removed=false;DanmakuCraftingModule.loaded(this);}
    @Override public void onUnload(){loaded=false;DanmakuCraftingModule.closeViews(this);DanmakuCraftingModule.unloaded(this);}
    @Override public void onRemove(){if(removed)return;removed=true;loaded=false;DanmakuCraftingModule.closeViews(this);DanmakuCraftingModule.unloaded(this);var drops=items;items=new ItemStack[items.length];for(var item:drops)if(!empty(item))location().getWorld().dropItemNaturally(location(),item);}
    @Override public void saveCustomData(CompoundTag outer){if(unreadable!=null){outer.put(DATA,unreadable.copy());return;}var data=new CompoundTag();data.putInt("schema",1);data.putString("kind",kind.name());for(int i=0;i<items.length;i++)if(!empty(items[i]))data.putByteArray("slot"+i,items[i].serializeAsBytes());outer.put(DATA,data);}
    @Override public void loadCustomData(CompoundTag outer){var data=outer.getCompound(DATA);if(data==null||data.isEmpty())return;try{
        if(data.getInt("schema",-1)!=1||!kind.name().equals(data.getString("kind","")))throw new IllegalArgumentException("Invalid danmaku table schema/kind");var decoded=new ItemStack[items.length];
        for(int i=0;i<items.length;i++){byte[] bytes=data.getByteArray("slot"+i);if(bytes!=null&&bytes.length>0)decoded[i]=ItemStack.deserializeBytes(bytes);if(!empty(decoded[i])&&decoded[i].getAmount()>decoded[i].getMaxStackSize())throw new IllegalArgumentException("Oversized source table slot");}items=decoded;unreadable=null;
        }catch(RuntimeException failure){unreadable=data.copy();LoggerFactory.getLogger(DanmakuTableController.class).error("Danmaku table data at {} preserved in quarantine",blockEntity.pos,failure);}}
    void dirty(){if(blockEntity.world!=null)blockEntity.world.blockEntityChanged(blockEntity.pos);}
    private void changed(){dirty();DanmakuCraftingModule.refresh(this);}
    static void thread(){if(!Bukkit.isPrimaryThread())throw new IllegalStateException("Danmaku table requires the Paper main thread");}
    static boolean empty(ItemStack item){return DanmakuCraftingRules.empty(item);}
    private void slot(int slot){if(slot<0||slot>=items.length)throw new IndexOutOfBoundsException("Danmaku table slot "+slot);}
    private static ItemStack[] copies(ItemStack[] input){return Arrays.stream(input).map(i->empty(i)?null:i.clone()).toArray(ItemStack[]::new);}
}
