package cc.thonly.reverie_dreams.paper.danmaku;

import cc.thonly.reverie_dreams.paper.util.Ids;

import net.momirealms.craftengine.core.entity.player.InteractionHand;
import net.momirealms.craftengine.core.entity.player.InteractionResult;
import net.momirealms.craftengine.core.item.behavior.ItemBehavior;
import net.momirealms.craftengine.core.item.behavior.ItemBehaviors;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.world.World;
import net.momirealms.craftengine.core.world.context.InteractEntityContext;
import net.momirealms.craftengine.core.world.context.UseOnContext;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;

/** Own factory: never delegates to the repository's old empty DanmakuItemBehaviour.shoot(). */
public final class DanmakuModule {
    private static DanmakuService service;
    private static boolean registered;
    private DanmakuModule() {}
    public static void registerTypes() {
        if(registered)return;
        ItemBehaviors.register(Key.of(Ids.MOD_ID + ":ordinary_danmaku"),(pack,path,id,section)->new Behavior());registered=true;
    }
    public static void enable(JavaPlugin plugin) {if(service!=null)return;service=new DanmakuService(plugin);service.enable();}
    public static void disable() {if(service!=null){service.disable();service=null;}}
    public static DanmakuService service() {if(service==null)throw new IllegalStateException("Danmaku is not enabled");return service;}
    private static EquipmentSlot slot(InteractionHand hand){return hand==InteractionHand.MAIN_HAND?EquipmentSlot.HAND:EquipmentSlot.OFF_HAND;}
    private static final class Behavior extends ItemBehavior {
        @Override public InteractionResult use(World world,net.momirealms.craftengine.core.entity.player.Player player,InteractionHand hand) {
            if(service==null||player==null)return InteractionResult.PASS;
            return service.fire((Player)player.platformPlayer(),slot(hand)).status()==DanmakuService.FireStatus.FIRED?InteractionResult.SUCCESS_AND_CANCEL:InteractionResult.FAIL;
        }
        @Override public InteractionResult useOnBlock(UseOnContext context) {
            if(service==null||context.getPlayer()==null)return InteractionResult.PASS;
            service.fire((Player)context.getPlayer().platformPlayer(),slot(context.getHand()));return InteractionResult.SUCCESS_AND_CANCEL;
        }
        @Override public InteractionResult useOnEntity(InteractEntityContext context) {
            if(service==null||context.getPlayer()==null)return InteractionResult.PASS;
            service.fire((Player)context.getPlayer().platformPlayer(),slot(context.getHand()));return InteractionResult.SUCCESS_AND_CANCEL;
        }
    }
}
