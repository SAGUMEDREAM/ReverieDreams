package cc.thonly.reverie_dreams.paper.specialitems2;

import cc.thonly.reverie_dreams.paper.util.Ids;

import cc.thonly.reverie_dreams.paper.storage.CupboardController;
import cc.thonly.reverie_dreams.paper.storage.StorageSpecialModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.Objects;

/** Source bearer cheque: signature limits deposits, redemption is allowed to any bearer. Coin value is1/10/100. */
public final class ChequeService {
    public static final NamespacedKey AMOUNT=new NamespacedKey("reverie_dreams","cheque_amount"),OWNER=new NamespacedKey("reverie_dreams","cheque_player_id"),NAME=new NamespacedKey("reverie_dreams","cheque_name");
    public enum Status { DEPOSITED, REDEEMED, NOT_CHEQUE, FOREIGN_SIGNATURE, INSUFFICIENT_COINS, EMPTY, OVERFLOW, BUSY }
    public record Snapshot(int amount,String playerId,String displayNameJson){}
    public record Result(Status status,int value,int gold,int silver,int copper){}
    private final Set<UUID> inFlight=new HashSet<>();
    public Snapshot inspect(ItemStack item){SpecialItemsExpansion.thread();var pdc=item.getPersistentDataContainer();return new Snapshot(pdc.getOrDefault(AMOUNT,PersistentDataType.INTEGER,0),pdc.getOrDefault(OWNER,PersistentDataType.STRING,""),pdc.getOrDefault(NAME,PersistentDataType.STRING,""));}
    public static int denomination(String id){return MoneyRules.denomination(id);}
    public long inventoryValue(Player player){SpecialItemsExpansion.thread();long total=0;for(var item:player.getInventory().getContents())if(item!=null&&!item.isEmpty())total+=(long)denomination(SpecialItemsExpansion.id(item))*item.getAmount();return total;}
    public Result use(Player player,EquipmentSlot hand,boolean deposit){SpecialItemsExpansion.thread();if(!SpecialItemsExpansion.id(player.getInventory().getItem(hand)).equals(Ids.MOD_ID + ":cheque"))return result(Status.NOT_CHEQUE,0);
        if(!inFlight.add(player.getUniqueId()))return result(Status.BUSY,0);try{return deposit?deposit(player,hand):redeem(player,hand);}finally{inFlight.remove(player.getUniqueId());}
    }
    private Result deposit(Player player,EquipmentSlot hand){var original=player.getInventory().getItem(hand);var state=inspect(original);boolean creative=player.getGameMode()==GameMode.CREATIVE;
        if(!creative&&!state.playerId.isEmpty()&&!state.playerId.equals(player.getName()))return result(Status.FOREIGN_SIGNATURE,0);long cash=inventoryValue(player);if(cash<10)return result(Status.INSUFFICIENT_COINS,0);
        var amount=MoneyRules.add(state.amount,cash);if(amount.isEmpty())return result(Status.OVERFLOW,0);int updated=amount.getAsInt();
        var next=cloneContents(player);var cheque=original.clone();cheque.editPersistentDataContainer(data->{data.set(AMOUNT,PersistentDataType.INTEGER,(int)updated);data.set(OWNER,PersistentDataType.STRING,player.getName());data.set(NAME,PersistentDataType.STRING,GsonComponentSerializer.gson().serialize(player.displayName()));});
        refreshTooltip(cheque);if(!creative)for(int i=0;i<next.length;i++)if(next[i]!=null&&denomination(SpecialItemsExpansion.id(next[i]))>0)next[i]=null;next[handIndex(player,hand)]=cheque;player.getInventory().setContents(next);player.sendMessage(Component.text("§e+"+cash));return result(Status.DEPOSITED,(int)cash);
    }
    private Result redeem(Player player,EquipmentSlot hand){var original=player.getInventory().getItem(hand);var state=inspect(original);if(state.amount<=0)return result(Status.EMPTY,0);
        var coins=MoneyRules.calculate(state.amount);int gold=coins.gold(),silver=coins.silver(),copper=coins.copper();var next=cloneContents(player);var cleared=original.clone();cleared.editPersistentDataContainer(pdc->{pdc.remove(AMOUNT);pdc.remove(OWNER);pdc.remove(NAME);});refreshTooltip(cleared);next[handIndex(player,hand)]=cleared;
        var overflow=new ArrayList<CupboardController.Entry>();int index=0;for(var coin:List.of(new Coin("gold_coin",gold),new Coin("silver_coin",silver),new Coin("copper_coin",copper))){if(coin.count<=0)continue;var definition=Objects.requireNonNull(CraftEngineItems.byId(Ids.MOD_ID + ":"+coin.id),"Missing real coin");var prototype=definition.buildBukkitItem().asOne();int left=pack(player,next,prototype,coin.count);if(left>0)overflow.add(new CupboardController.Entry(index++,prototype,left));}
        // Reserve the cheque before ItemSpawnEvent can re-enter. Native array commit raises no player inventory event.
        // A rejected persistent carrier spawn happens before any point/coin is emitted; restore the cheque on that failure.
        var drops=overflow.isEmpty()?null:StorageSpecialModule.drops();player.getInventory().setItem(hand,cleared);
        try{if(drops!=null)drops.enqueue(player.getLocation(),overflow);}catch(RuntimeException failure){player.getInventory().setItem(hand,original);throw failure;}
        player.getInventory().setContents(next);return new Result(Status.REDEEMED,state.amount,gold,silver,copper);
    }
    private static int pack(Player player,ItemStack[] contents,ItemStack prototype,int count){int remaining=count,max=prototype.getMaxStackSize();
        int selected=player.getInventory().getHeldItemSlot();remaining=merge(contents,selected,prototype,max,remaining);if(contents.length>40)remaining=merge(contents,40,prototype,max,remaining);
        for(int i=0;i<36&&remaining>0;i++)remaining=merge(contents,i,prototype,max,remaining);
        for(int i=0;i<36&&remaining>0;i++)if(contents[i]==null||contents[i].isEmpty()){int moved=Math.min(max,remaining);contents[i]=prototype.clone();contents[i].setAmount(moved);remaining-=moved;}return remaining;
    }
    private static int merge(ItemStack[] contents,int slot,ItemStack prototype,int max,int remaining){var item=contents[slot];if(remaining>0&&item!=null&&item.isSimilar(prototype)&&item.getAmount()<max){int count=Math.min(remaining,max-item.getAmount());item.add(count);remaining-=count;}return remaining;}
    private static ItemStack[] cloneContents(Player player){var contents=player.getInventory().getContents();for(int i=0;i<contents.length;i++)if(contents[i]!=null)contents[i]=contents[i].clone();return contents;}
    private static int handIndex(Player player,EquipmentSlot hand){return hand==EquipmentSlot.OFF_HAND?40:player.getInventory().getHeldItemSlot();}
    public void refreshTooltip(ItemStack cheque){var state=inspect(cheque);var lore=new ArrayList<Component>();if(cheque.lore()!=null)for(var line:cheque.lore())if(!generated(line))lore.add(line);
        if(!state.displayNameJson.isEmpty())lore.add(Component.translatable("item.tooltip.cheque.owner",GsonComponentSerializer.gson().deserialize(state.displayNameJson)).decoration(TextDecoration.ITALIC,false));lore.add(Component.translatable("item.tooltip.cheque.amount",Component.text(state.amount)).decoration(TextDecoration.ITALIC,false));cheque.lore(lore);
    }
    private static boolean generated(Component line){if(line instanceof TranslatableComponent translated&&(translated.key().equals("item.tooltip.cheque.owner")||translated.key().equals("item.tooltip.cheque.amount")))return true;return line.children().stream().anyMatch(ChequeService::generated);}
    private record Coin(String id,int count){}
    private static Result result(Status status,int value){return new Result(status,value,0,0,0);}
}
