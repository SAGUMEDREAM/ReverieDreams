package cc.thonly.reverie_dreams.paper.specialweapons;

import cc.thonly.reverie_dreams.paper.util.Ids;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.core.entity.player.InteractionHand;
import net.momirealms.craftengine.core.entity.player.InteractionResult;
import net.momirealms.craftengine.core.item.behavior.ItemBehavior;
import net.momirealms.craftengine.core.item.behavior.ItemBehaviors;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.world.context.InteractEntityContext;
import net.momirealms.craftengine.core.world.context.UseOnContext;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.Objects;
import net.momirealms.craftengine.core.world.World;

/** Third cohort: four actual source registrations and source-specific ordinary-bullet factory requests. */
public final class SpecialWeaponsModule {
    private static SpecialWeaponsModule instance;
    private static boolean registered;
    private final JsonObject domain;
    private final MagazineService magazines=new MagazineService();
    private final WeaponRepairBridge repairs=new WeaponRepairBridge();
    private final GunsService guns;
    private SpecialWeaponsModule(JavaPlugin plugin){try(var reader=new InputStreamReader(Objects.requireNonNull(plugin.getResource("migration/specialweapons-domain.json")),StandardCharsets.UTF_8)){domain=JsonParser.parseReader(reader).getAsJsonObject();}catch(IOException failure){throw new IllegalStateException(failure);}guns=new GunsService(plugin,magazines,domain);}
    public static void registerTypes(){if(registered)return;ItemBehaviors.register(Key.of(Ids.MOD_ID + ":special_weapon"),(pack,path,id,section)->new Behavior());registered=true;}
    public static void enable(JavaPlugin plugin){if(instance!=null)return;instance=new SpecialWeaponsModule(plugin);Bukkit.getPluginManager().registerEvents(instance.repairs,plugin);instance.guns.enable();}
    public static void disable(){if(instance==null)return;instance.guns.disable();HandlerList.unregisterAll(instance.repairs);instance=null;}
    public static SpecialWeaponsModule service(){if(instance==null)throw new IllegalStateException("Special weapons not enabled");return instance;}
    public JsonObject domain(){return domain.deepCopy();}
    public MagazineService magazines(){return magazines;}
    public GunsService guns(){return guns;}
    public WeaponRepairBridge repairs(){return repairs;}
    public ItemStack create(String id){thread();if(!id.contains(":"))id=Ids.MOD_ID + ":"+id;if(!domain.getAsJsonObject("implemented").has(id))return null;var definition=CraftEngineItems.byId(id);if(definition==null)return null;var item=definition.buildBukkitItem();if(LunarWeaponType.weapon(item)!=null)item=guns.prepare(item);var magazine=LunarWeaponType.magazine(item);if(magazine!=null)magazines.setRounds(item,magazine.capacity());magazines.refreshTooltip(item);return item;}
    public GunsService.UseResult use(Player player,EquipmentSlot hand){return use(player,hand,null);}
    public GunsService.UseResult use(Player player,EquipmentSlot hand,LivingEntity target){thread();var item=player.getInventory().getItem(hand);String id=id(item);
        LunarWeaponType lunar=LunarWeaponType.weapon(item);
        if(lunar!=null)return guns.useLunar(player,hand,lunar,target!=null);
        if(!domain.getAsJsonObject("implemented").has(id)||id.endsWith(":magazine"))return new GunsService.UseResult(GunsService.Status.PASS,0,false);if(player.hasCooldown(item))return new GunsService.UseResult(GunsService.Status.COOLDOWN,0,true);
        if(id.endsWith(":trumpet_gun")){guns.trumpet(player);player.setCooldown(item,10);WeaponDurability.withoutBreaking(player,hand);return new GunsService.UseResult(GunsService.Status.CAST,0,false);}
        if(id.endsWith(":sunflower")){long before=guns.launchedCount();guns.sunflower(player,target);player.setCooldown(item,50);if(player.getGameMode()!=GameMode.CREATIVE)player.damageItemStack(hand,1);return new GunsService.UseResult(GunsService.Status.CAST,(int)(guns.launchedCount()-before),false);}
        return new GunsService.UseResult(GunsService.Status.PASS,0,false);
    }
    public static String id(ItemStack item){if(item==null||item.isEmpty())return "minecraft:air";var custom=CraftEngineItems.getCustomItemId(item);return custom==null?item.getType().getKey().asString():custom.asString();}
    static void thread(){if(!Bukkit.isPrimaryThread())throw new IllegalStateException("Weapons require the Paper main thread");}
    private static EquipmentSlot slot(InteractionHand hand){return hand==InteractionHand.MAIN_HAND?EquipmentSlot.HAND:EquipmentSlot.OFF_HAND;}
    private static InteractionResult adapt(GunsService.UseResult result){if(result.sourceReturnsFail())return InteractionResult.FAIL;return result.status()==GunsService.Status.PASS?InteractionResult.PASS:InteractionResult.SUCCESS_AND_CANCEL;}
    private static final class Behavior extends ItemBehavior{
        @Override public InteractionResult use(World world,net.momirealms.craftengine.core.entity.player.Player player,InteractionHand hand){return instance==null||player==null?InteractionResult.PASS:adapt(instance.use((Player)player.platformPlayer(),slot(hand)));}
        @Override public InteractionResult useOnBlock(UseOnContext c){return instance==null||c.getPlayer()==null?InteractionResult.PASS:adapt(instance.use((Player)c.getPlayer().platformPlayer(),slot(c.getHand())));}
        @Override public InteractionResult useOnEntity(InteractEntityContext c){if(instance==null||c.getPlayer()==null||c.getTarget()==null||!(c.getTarget().platformEntity() instanceof LivingEntity target))return InteractionResult.PASS;ItemStack held=((Player)c.getPlayer().platformPlayer()).getInventory().getItem(slot(c.getHand()));String id=id(held);if(LunarWeaponType.weapon(held)==null&&!id.equals(Ids.MOD_ID + ":sunflower"))return InteractionResult.PASS;return adapt(instance.use((Player)c.getPlayer().platformPlayer(),slot(c.getHand()),target));}
    }
}
