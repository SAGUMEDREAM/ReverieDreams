package cc.thonly.reverie_dreams.paper.specialitems2;

import cc.thonly.reverie_dreams.paper.util.Ids;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.core.entity.player.InteractionHand;
import net.momirealms.craftengine.core.entity.player.InteractionResult;
import net.momirealms.craftengine.core.item.behavior.ItemBehavior;
import net.momirealms.craftengine.core.item.behavior.ItemBehaviors;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.world.context.UseOnContext;
import org.bukkit.Bukkit;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import net.momirealms.craftengine.core.world.World;
import org.bukkit.configuration.file.YamlConfiguration;

/** Four additional source registrations; independent namespace/factory and domain leave the frozen first12 unchanged. */
public final class SpecialItemsExpansion {
    public enum Status { SUCCESS, LIMIT, PASS, COOLDOWN, NOT_SNEAKING, CHUNKS_NOT_LOADED, REFUSED }
    public record Result(Status status,int affected){}
    private static SpecialItemsExpansion instance;
    private static boolean registered;
    private final JavaPlugin plugin;
    private final JsonObject domain;
    private final HealthUpgradeService health;
    private final TreasureRodService rod;
    private final ChequeService cheques=new ChequeService();
    private final BombService bombs=new BombService();
    private BukkitTask task;
    private SpecialItemsExpansion(JavaPlugin plugin){this.plugin=plugin;try(var reader=new InputStreamReader(Objects.requireNonNull(plugin.getResource("migration/specialitems2-domain.json")),StandardCharsets.UTF_8)){domain=JsonParser.parseReader(reader).getAsJsonObject();}catch(IOException failure){throw new IllegalStateException(failure);}
        if(!new File(plugin.getDataFolder(),"specialitems2.yml").isFile())plugin.saveResource("specialitems2.yml",false);var config=YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(),"specialitems2.yml"));health=new HealthUpgradeService(plugin,config.getInt("max-upgraded-health-value",1024));rod=new TreasureRodService(domain);
    }
    public static void registerTypes(){if(registered)return;ItemBehaviors.register(Key.of(Ids.MOD_ID + ":special_item_expansion"),(pack,path,id,section)->new Behavior());registered=true;}
    public static void enable(JavaPlugin plugin){if(instance!=null)return;instance=new SpecialItemsExpansion(plugin);Bukkit.getPluginManager().registerEvents(instance.health,plugin);Bukkit.getPluginManager().registerEvents(instance.rod,plugin);instance.task=Bukkit.getScheduler().runTaskTimer(plugin,()->{instance.health.tick();instance.rod.tick();},1,1);}
    public static void disable(){if(instance==null)return;if(instance.task!=null)instance.task.cancel();instance.rod.disable();HandlerList.unregisterAll(instance.health);HandlerList.unregisterAll(instance.rod);instance=null;}
    public static SpecialItemsExpansion service(){if(instance==null)throw new IllegalStateException("Special expansion is not enabled");return instance;}
    public JsonObject domain(){return domain.deepCopy();}
    public HealthUpgradeService health(){return health;}
    public TreasureRodService rod(){return rod;}
    public ChequeService cheques(){return cheques;}
    public BombService bombs(){return bombs;}
    public ItemStack create(String id){thread();if(!id.contains(":"))id=Ids.MOD_ID + ":"+id;if(!domain.getAsJsonObject("implemented").has(id))return null;var item=CraftEngineItems.byId(id);return item==null?null:item.buildBukkitItem();}
    public Result use(Player player,EquipmentSlot hand){thread();var item=player.getInventory().getItem(hand);String id=id(item);if(!domain.getAsJsonObject("implemented").has(id))return new Result(Status.PASS,0);if(player.hasCooldown(item))return new Result(Status.COOLDOWN,0);
        return switch(id){
            case Ids.MOD_ID + ":upgraded_health"->{boolean success=health.use(player,hand);yield new Result(success?Status.SUCCESS:Status.LIMIT,success?1:0);}
            case Ids.MOD_ID + ":cheque"->{var result=cheques.use(player,hand,player.isSneaking());yield new Result(result.status()==ChequeService.Status.DEPOSITED||result.status()==ChequeService.Status.REDEEMED?Status.SUCCESS:Status.REFUSED,result.value());}
            case Ids.MOD_ID + ":bomb"->{var result=bombs.use(player,hand,ThreadLocalRandom.current());yield new Result(Status.SUCCESS,result.cancelledBullets()+result.otherEntities());}
            case Ids.MOD_ID + ":treasure_hunting_rod"->{if(!player.isSneaking())yield new Result(Status.NOT_SNEAKING,0);OreScanRules.Scan scan;try{scan=rod.inspect(player.getLocation());}catch(IllegalStateException incomplete){yield new Result(Status.CHUNKS_NOT_LOADED,0);}player.swingHand(hand);rod.display(player,scan);ExpansionDurability.damage(player,hand);player.setCooldown(item,35);yield new Result(Status.SUCCESS,scan.ores().size());}
            default->new Result(Status.PASS,0);
        };
    }
    public static String id(ItemStack item){if(item==null||item.isEmpty())return "minecraft:air";var custom=CraftEngineItems.getCustomItemId(item);return custom==null?item.getType().getKey().asString():custom.asString();}
    static void thread(){if(!Bukkit.isPrimaryThread())throw new IllegalStateException("Special items require the Paper main thread");}
    private static EquipmentSlot slot(InteractionHand hand){return hand==InteractionHand.MAIN_HAND?EquipmentSlot.HAND:EquipmentSlot.OFF_HAND;}
    private static InteractionResult result(Result result){return result.status==Status.PASS||result.status==Status.NOT_SNEAKING?InteractionResult.PASS:result.status==Status.SUCCESS||result.status==Status.LIMIT?InteractionResult.SUCCESS_AND_CANCEL:InteractionResult.FAIL;}
    private static final class Behavior extends ItemBehavior{
        @Override public InteractionResult use(World world,net.momirealms.craftengine.core.entity.player.Player player,InteractionHand hand){return instance==null||player==null?InteractionResult.PASS:result(instance.use((Player)player.platformPlayer(),slot(hand)));}
        @Override public InteractionResult useOnBlock(UseOnContext context){return instance==null||context.getPlayer()==null?InteractionResult.PASS:result(instance.use((Player)context.getPlayer().platformPlayer(),slot(context.getHand())));}
    }
}
