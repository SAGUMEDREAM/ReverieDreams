package cc.thonly.reverie_dreams.paper.storage;

import cc.thonly.reverie_dreams.paper.util.Ids;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.bukkit.block.behavior.BukkitBlockBehavior;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviors;
import net.momirealms.craftengine.core.block.behavior.EntityBlock;
import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.block.entity.BlockEntityController;
import net.momirealms.craftengine.core.block.property.Property;
import net.momirealms.craftengine.core.entity.player.InteractionResult;
import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.context.BlockPlaceContext;
import net.momirealms.craftengine.core.world.context.UseOnContext;
import net.momirealms.craftengine.libraries.antigrieflib.Flag;
import net.momirealms.craftengine.proxy.bukkit.craftbukkit.inventory.CraftInventoryProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.LevelProxy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.io.IOException;
import java.util.Objects;
import net.momirealms.craftengine.core.block.BlockDefinition;
import net.momirealms.craftengine.core.block.behavior.WorldlyContainerHolder;

/** Cash-box faith and unrestricted native automation plus a genuine36-identity, signed-int cupboard. */
public final class StorageSpecialModule implements Listener {
    private static StorageSpecialModule instance;
    private static boolean registered;
    private static final Set<BlockEntityController> LOADED=Collections.newSetFromMap(new IdentityHashMap<>());
    private static final Map<BlockEntityController,List<View>> VIEWS=new IdentityHashMap<>();
    private static final NamespacedKey FOOD=new NamespacedKey("reverie_dreams","food_properties"),BEVERAGE=new NamespacedKey("reverie_dreams","beverage_properties");
    private final JavaPlugin plugin;
    private final JsonObject domain,foodDomain;
    private final PlayerFaithService faith;
    private final StorageDropService drops;
    private StorageSpecialModule(JavaPlugin plugin){this.plugin=plugin;domain=read(plugin,"migration/storage-special-domain.json");foodDomain=read(plugin,"migration/food-domain.json");faith=new PlayerFaithService(plugin);drops=new StorageDropService(plugin);}
    private static JsonObject read(JavaPlugin plugin,String path){try(var reader=new InputStreamReader(Objects.requireNonNull(plugin.getResource(path)),StandardCharsets.UTF_8)){return JsonParser.parseReader(reader).getAsJsonObject();}catch(IOException failure){throw new IllegalStateException("Cannot read "+path,failure);}}
    public static void registerTypes(){if(registered)return;BlockBehaviors.register(Key.of(Ids.MOD_ID + ":storage_special"),(block,section)->new Behavior(block,section.getBoolean("cash-box",false)));registered=true;}
    public static void enable(JavaPlugin plugin){if(instance!=null)return;instance=new StorageSpecialModule(plugin);Bukkit.getPluginManager().registerEvents(instance,plugin);Bukkit.getPluginManager().registerEvents(instance.faith,plugin);for(var player:Bukkit.getOnlinePlayers())instance.faith.loadSafely(player);instance.drops.enable();}
    public static void disable(){if(instance==null)return;for(var controller:List.copyOf(LOADED)){closeViews(controller);if(controller instanceof CashBoxController cash)cash.dirty();else((CupboardController)controller).dirty();}instance.drops.disable();HandlerList.unregisterAll(instance);HandlerList.unregisterAll(instance.faith);LOADED.clear();VIEWS.clear();instance=null;}
    public static PlayerFaithService faith(){if(instance==null)throw new IllegalStateException("Special storage not enabled");return instance.faith;}
    public static StorageDropService drops(){if(instance==null)throw new IllegalStateException("Special storage not enabled");return instance.drops;}
    public static JsonObject domain(){if(instance==null)throw new IllegalStateException("Special storage not enabled");return instance.domain.deepCopy();}
    static boolean enabled(){return instance!=null;}
    static void loaded(BlockEntityController controller){LOADED.add(controller);}
    static void unloaded(BlockEntityController controller){LOADED.remove(controller);}
    public static BlockEntityController controllerAt(Block block){PlayerFaithService.thread();if(!block.getWorld().isChunkLoaded(block.getX()>>4,block.getZ()>>4))return null;var entity=BukkitAdaptor.adapt(block.getWorld()).storageWorld().getBlockEntityAtIfLoaded(new BlockPos(block.getX(),block.getY(),block.getZ()));if(entity==null)return null;var cash=entity.controller.get(CashBoxController.class,0);return cash!=null?cash:entity.controller.get(CupboardController.class,0);}
    public static String id(ItemStack item){if(CupboardController.empty(item))return "minecraft:air";var id=CraftEngineItems.getCustomItemId(item);return id==null?item.getType().getKey().asString():id.asString();}
    public static boolean acceptsCupboard(ItemStack item){
        if(instance==null||CupboardController.empty(item))return false;
        String id=id(item);for(var value:instance.domain.getAsJsonArray("cupboardAcceptedSourceIds"))if(value.getAsString().equals(id))return true;
        if(hasProperties(item,FOOD,"foodProperties")||hasProperties(item,BEVERAGE,"beverageProperties"))return true;
        var definition=CraftEngineItems.byId(id);if(definition!=null)for(String tag:new String[]{"food","beverage","ingredient"})if(definition.is(Key.of(Ids.MOD_ID + ":"+tag)))return true;
        return false;
    }
    private static boolean hasProperties(ItemStack item,NamespacedKey key,String category){String raw=item.getPersistentDataContainer().get(key,PersistentDataType.STRING);if(raw==null)return false;try{var values=JsonParser.parseString(raw).getAsJsonArray();for(var value:values)if(instance.foodDomain.getAsJsonObject(category).has(value.getAsString()))return true;return false;}catch(RuntimeException malformed){return false;}}
    private static Location location(BlockEntityController controller){return controller instanceof CashBoxController cash?cash.location():((CupboardController)controller).location();}
    private static boolean available(BlockEntityController controller){return controller instanceof CashBoxController cash?cash.available():((CupboardController)controller).available();}
    private static boolean near(Player player,BlockEntityController controller){return available(controller)&&player.getWorld().equals(location(controller).getWorld())&&player.getLocation().distanceSquared(location(controller))<=64;}
    private static void use(Player player,BlockEntityController controller){
        if(!near(player,controller)||!BukkitCraftEngine.instance().antiGriefProvider().test(player,Flag.OPEN_CONTAINER,location(controller)))return;
        if(controller instanceof CashBoxController&&PlayerFaithService.coinValue(id(player.getInventory().getItemInMainHand()))>0){faith().prayMainHand(player,player.getWorld(),ThreadLocalRandom.current());return;}
        var view=new View(controller);VIEWS.computeIfAbsent(controller,ignored->new ArrayList<>()).add(view);view.refresh();player.openInventory(view.inventory);player.playSound(player.getLocation(),controller instanceof CashBoxController?Sound.BLOCK_CHEST_OPEN:Sound.BLOCK_BARREL_OPEN,SoundCategory.BLOCKS,1,1);
    }
    static void refresh(BlockEntityController controller){for(var view:List.copyOf(VIEWS.getOrDefault(controller,List.of())))view.refresh();}
    static void closeViews(BlockEntityController controller){var views=VIEWS.remove(controller);if(views!=null)for(var view:views)for(var player:List.copyOf(view.inventory.getViewers()))player.closeInventory();}
    private void closeLater(Player player,View view){Bukkit.getScheduler().runTask(plugin,()->{if(player.getOpenInventory().getTopInventory()==view.inventory)player.closeInventory();});}
    @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true)public void click(InventoryClickEvent event){
        if(!(event.getView().getTopInventory().getHolder() instanceof View view)||!(event.getWhoClicked() instanceof Player player))return;
        int raw=event.getRawSlot(),size=view.inventory.getSize();var click=event.getClick();
        if(raw<0||raw>=size){
            if(click==ClickType.DOUBLE_CLICK){event.setCancelled(true);return;}
            if(click.isShiftClick()){event.setCancelled(true);if(!near(player,view.controller)){closeLater(player,view);return;}if(!BukkitCraftEngine.instance().antiGriefProvider().test(player,Flag.OPEN_CONTAINER,location(view.controller)))return;if(event.getClickedInventory()!=null&&event.getSlot()>=0){if(view.controller instanceof CashBoxController cash)cash.transferFromPlayer(event.getClickedInventory(),event.getSlot());else((CupboardController)view.controller).transferFrom(event.getClickedInventory(),event.getSlot());}}return;
        }
        event.setCancelled(true);if(!near(player,view.controller)){closeLater(player,view);return;}
        if(!BukkitCraftEngine.instance().antiGriefProvider().test(player,Flag.OPEN_CONTAINER,location(view.controller)))return;
        if(view.controller instanceof CashBoxController cash)cashClick(event,player,cash,raw);else cupboardClick(event,player,(CupboardController)view.controller,raw);
    }
    private void cashClick(InventoryClickEvent event,Player player,CashBoxController cash,int slot){
        var click=event.getClick();if(click.isShiftClick()){cash.transferToPlayer(slot,player.getInventory());return;}
        if(click!=ClickType.LEFT&&click!=ClickType.RIGHT)return;var cursor=event.getCursor();var stored=cash.item(slot);boolean right=click==ClickType.RIGHT;
        if(CupboardController.empty(cursor)){int amount=CupboardController.empty(stored)?0:right?(stored.getAmount()+1)/2:stored.getAmount();var taken=cash.takeGui(slot,amount);if(taken!=null)player.setItemOnCursor(taken);}
        else if(CashBoxController.coin(cursor)&&(CupboardController.empty(stored)||stored.isSimilar(cursor))){var offered=cursor.clone();if(right)offered.setAmount(1);var remainder=cash.offerGui(slot,offered);int moved=offered.getAmount()-(CupboardController.empty(remainder)?0:remainder.getAmount());var next=cursor.clone();next.subtract(moved);player.setItemOnCursor(next.isEmpty()?null:next);}
        else if(CashBoxController.coin(cursor)&&CashBoxController.coin(stored)&&cursor.getAmount()<=cursor.getMaxStackSize()){var old=cash.takeGui(slot,stored.getAmount());cash.offerGui(slot,cursor);player.setItemOnCursor(old);}
    }
    private void cupboardClick(InventoryClickEvent event,Player player,CupboardController cupboard,int slot){
        var cursor=event.getCursor();boolean carrying=!CupboardController.empty(cursor);var click=event.getClick();
        if(carrying){int amount=switch(click){case LEFT,SHIFT_LEFT->cursor.getAmount();case RIGHT->1;case SHIFT_RIGHT->cursor.getAmount()/2;default->0;};if(amount<=0)return;var offered=cursor.clone();offered.setAmount(amount);var remainder=cupboard.offer(offered);int moved=amount-(CupboardController.empty(remainder)?0:remainder.getAmount());var next=cursor.clone();next.subtract(moved);player.setItemOnCursor(next.isEmpty()?null:next);return;}
        switch(click){
            case LEFT,RIGHT->{var item=cupboard.take(slot,1);if(item!=null)player.setItemOnCursor(item);}
            case SHIFT_LEFT->cupboard.transferTo(slot,player.getInventory(),64);
            case SHIFT_RIGHT->cupboard.transferTo(slot,player.getInventory(),32);
            case MIDDLE->{var item=cupboard.take(slot,64);if(item!=null)player.setItemOnCursor(item);}
            case DOUBLE_CLICK->{var item=cupboard.takeSame(slot);if(item!=null)player.setItemOnCursor(item);}
            default->{}
        }
    }
    @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true)public void drag(InventoryDragEvent event){if(event.getView().getTopInventory().getHolder() instanceof View view&&event.getRawSlots().stream().anyMatch(i->i<view.inventory.getSize()))event.setCancelled(true);}
    @EventHandler public void close(InventoryCloseEvent event){if(!(event.getInventory().getHolder() instanceof View view))return;var views=VIEWS.get(view.controller);if(views!=null){views.remove(view);if(views.isEmpty())VIEWS.remove(view.controller);}if(view.controller instanceof CashBoxController cash){cash.dirty();event.getPlayer().getWorld().playSound(event.getPlayer().getLocation(),Sound.BLOCK_CHEST_CLOSE,SoundCategory.BLOCKS,1,1);}else((CupboardController)view.controller).dirty();}
    @EventHandler(ignoreCancelled=true)public void move(PlayerMoveEvent event){if(event.getPlayer().getOpenInventory().getTopInventory().getHolder() instanceof View view&&!near(event.getPlayer(),view.controller))event.getPlayer().closeInventory();}
    private static boolean quarantined(BlockEntityController c){return c instanceof CashBoxController cash?cash.snapshot().quarantined():c instanceof CupboardController cupboard&&cupboard.snapshot().quarantined();}
    @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true)public void breaking(BlockBreakEvent event){var c=controllerAt(event.getBlock());if(c!=null&&quarantined(c))event.setCancelled(true);}
    private static void protect(List<Block> blocks){blocks.removeIf(block->{var c=controllerAt(block);return c!=null&&quarantined(c);});}
    @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true)public void explode(BlockExplodeEvent event){protect(event.blockList());}
    @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true)public void explode(EntityExplodeEvent event){protect(event.blockList());}
    private static final class View implements InventoryHolder{
        final BlockEntityController controller;final Inventory inventory;View(BlockEntityController c){controller=c;boolean cash=c instanceof CashBoxController;inventory=Bukkit.createInventory(this,cash?27:36,Component.translatable("block.reverie_dreams."+(cash?"cash_box":"cupboard")));}
        public Inventory getInventory(){return inventory;}
        void refresh(){inventory.clear();if(controller instanceof CashBoxController cash){for(int i=0;i<27;i++)inventory.setItem(i,cash.item(i));}else{var cupboard=(CupboardController)controller;for(var entry:cupboard.snapshot().entries()){var visual=entry.prototype().asOne();visual.editMeta(meta->meta.displayName(visual.effectiveName().append(Component.text(" (×"+entry.count()+")",NamedTextColor.AQUA))));inventory.setItem(entry.slot(),visual);}}}
    }
    private static final class Behavior extends BukkitBlockBehavior implements EntityBlock,WorldlyContainerHolder{
        final boolean cash;final Property<Direction> facing;int controllerId;
        Behavior(BlockDefinition block,boolean cash){super(block);this.cash=cash;facing=BlockBehaviorFactory.getProperty("facing",block,"facing",Direction.class);}
        public BlockEntityController createBlockEntityController(BlockEntity entity){return cash?new CashBoxController(entity):new CupboardController(entity);}
        public void initControllerId(int id){controllerId=id;}
        public ImmutableBlockState updateStateForPlacement(BlockPlaceContext context,ImmutableBlockState state){return state.with(facing,context.getHorizontalDirection());}
        public InteractionResult useOnBlock(UseOnContext context,ImmutableBlockState state){return interact(context);}
        public InteractionResult useWithoutItem(UseOnContext context,ImmutableBlockState state){return interact(context);}
        private InteractionResult interact(UseOnContext context){if(!enabled()||context.getPlayer()==null)return InteractionResult.PASS;var entity=context.getLevel().storageWorld().getBlockEntityAtIfLoaded(context.getClickedPos());if(entity==null)return InteractionResult.FAIL;BlockEntityController controller=cash?entity.controller.getAt(CashBoxController.class,controllerId):entity.controller.getAt(CupboardController.class,controllerId);if(controller==null)return InteractionResult.FAIL;use((Player)context.getPlayer().platformPlayer(),controller);return InteractionResult.SUCCESS_AND_CANCEL;}
        public Object getContainer(Object block,Object[] args){if(!cash)return null;var world=BukkitAdaptor.adapt(LevelProxy.INSTANCE.getWorld(args[1])).storageWorld();var entity=world.getBlockEntityAtIfLoaded(LocationUtils.fromBlockPos(args[2]));var controller=entity==null?null:entity.controller.getAt(CashBoxController.class,controllerId);return controller==null||!controller.available()?null:CraftInventoryProxy.INSTANCE.getInventory(controller.automationInventory());}
    }
}
