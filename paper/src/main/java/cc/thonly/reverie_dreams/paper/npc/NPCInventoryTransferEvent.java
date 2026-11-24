package cc.thonly.reverie_dreams.paper.npc;
import java.util.UUID;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
/** Real NPC cargo/world-container transaction; no fictitious hopper/player Inventory is exposed. */
public final class NPCInventoryTransferEvent extends Event implements Cancellable {
 private static final HandlerList HANDLERS=new HandlerList();private final UUID npcId;private final Block block;private final boolean fromNPC;private final ItemStack item;private boolean cancelled;
 public NPCInventoryTransferEvent(UUID npcId,Block block,boolean fromNPC,ItemStack item){this.npcId=npcId;this.block=block;this.fromNPC=fromNPC;this.item=item.clone();}
 public UUID getNPCId(){return npcId;}public Block getBlock(){return block;}public boolean isFromNPC(){return fromNPC;}public ItemStack getItem(){return item.clone();}
 public boolean isCancelled(){return cancelled;}public void setCancelled(boolean value){cancelled=value;}public HandlerList getHandlers(){return HANDLERS;}public static HandlerList getHandlerList(){return HANDLERS;}
}
