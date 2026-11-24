package cc.thonly.reverie_dreams.paper.danmakucrafting;

import cc.thonly.reverie_dreams.paper.util.Ids;

import io.papermc.paper.event.entity.EntityEquipmentChangedEvent;
import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.bukkit.block.behavior.BukkitBlockBehavior;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviors;
import net.momirealms.craftengine.core.block.behavior.EntityBlock;
import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.block.entity.BlockEntityController;
import net.momirealms.craftengine.core.entity.player.InteractionHand;
import net.momirealms.craftengine.core.entity.player.InteractionResult;
import net.momirealms.craftengine.core.item.behavior.ItemBehavior;
import net.momirealms.craftengine.core.item.behavior.ItemBehaviors;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.context.UseOnContext;
import net.momirealms.craftengine.libraries.antigrieflib.Flag;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mannequin;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.event.world.EntitiesUnloadEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;
import java.util.Locale;
import net.momirealms.craftengine.core.block.BlockDefinition;
import net.momirealms.craftengine.core.world.World;

/** Independent production/strength stations and real creator UI. No preview item is ever a freely movable output. */
public final class DanmakuCraftingModule implements Listener {
    private static final Set<DanmakuTableController> LOADED=Collections.newSetFromMap(new IdentityHashMap<>());
    private static final Map<DanmakuTableController,List<TableView>> VIEWS=new IdentityHashMap<>();
    private static final Set<String> TEMPLATE_LOOT=Set.of("minecraft:chests/abandoned_mineshaft","minecraft:chests/village/village_weaponsmith","minecraft:chests/end_city_treasure");
    private static final int[] CRAFT_SLOTS={0,2,4,6,8},STRENGTH_SLOTS={2,3};
    private static boolean registered;
    private static DanmakuCraftingModule instance;
    private final JavaPlugin plugin;
    private final DanmakuCraftingRules rules=new DanmakuCraftingRules();
    private final Map<UUID,LivingEntity> featherHolders=new LinkedHashMap<>();
    private final DanmakuNativeBridge nativeBridge;
    private BukkitTask ticker;
    private DanmakuCraftingModule(JavaPlugin plugin){this.plugin=plugin;nativeBridge=new DanmakuNativeBridge(rules);}
    public static void registerTypes(){if(registered)return;
        BlockBehaviors.register(Key.of(Ids.MOD_ID + ":danmaku_table"),(block,section)->new Behavior(block,DanmakuTableController.Kind.valueOf(section.getString("kind","CRAFTING"))));
        ItemBehaviors.register(Key.of(Ids.MOD_ID + ":danmaku_shape_creator"),(pack,path,id,section)->new CreatorBehavior());registered=true;
    }
    public static void enable(JavaPlugin plugin){if(instance!=null)return;instance=new DanmakuCraftingModule(plugin);Bukkit.getPluginManager().registerEvents(instance,plugin);Bukkit.getPluginManager().registerEvents(instance.nativeBridge,plugin);
        for(var world:Bukkit.getWorlds())for(var entity:world.getLivingEntities())instance.track(entity);
        instance.ticker=Bukkit.getScheduler().runTaskTimer(plugin,instance::tick,1,1);
    }
    public static void disable(){if(instance==null)return;for(var controller:List.copyOf(LOADED)){closeViews(controller);controller.dirty();}
        for(var player:Bukkit.getOnlinePlayers())if(player.getOpenInventory().getTopInventory().getHolder() instanceof ShapeView)player.closeInventory();
        if(instance.ticker!=null)instance.ticker.cancel();HandlerList.unregisterAll(instance);HandlerList.unregisterAll(instance.nativeBridge);instance=null;LOADED.clear();VIEWS.clear();
    }
    public static DanmakuCraftingRules rules(){if(instance==null)throw new IllegalStateException("Danmaku crafting not enabled");return instance.rules;}
    static boolean enabled(){return instance!=null;}
    static void loaded(DanmakuTableController controller){LOADED.add(controller);}
    static void unloaded(DanmakuTableController controller){LOADED.remove(controller);}
    public static DanmakuTableController controllerAt(Block block){DanmakuTableController.thread();if(!block.getWorld().isChunkLoaded(block.getX()>>4,block.getZ()>>4))return null;
        var entity=BukkitAdaptor.adapt(block.getWorld()).storageWorld().getBlockEntityAtIfLoaded(new BlockPos(block.getX(),block.getY(),block.getZ()));return entity==null?null:entity.controller.get(DanmakuTableController.class,0);
    }
    private static boolean near(Player player,DanmakuTableController controller){return controller.available()&&player.getWorld().equals(controller.location().getWorld())&&player.getLocation().distanceSquared(controller.location())<=64;}
    private static void open(Player player,DanmakuTableController controller){if(!near(player,controller)||!BukkitCraftEngine.instance().antiGriefProvider().test(player,Flag.OPEN_CONTAINER,controller.location()))return;
        var view=new TableView(controller);VIEWS.computeIfAbsent(controller,ignored->new ArrayList<>()).add(view);view.refresh();player.openInventory(view.inventory);
    }
    static void refresh(DanmakuTableController controller){for(var view:List.copyOf(VIEWS.getOrDefault(controller,List.of())))view.refresh();}
    static void closeViews(DanmakuTableController controller){var views=VIEWS.remove(controller);if(views!=null)for(var view:views)for(var viewer:List.copyOf(view.inventory.getViewers()))viewer.closeInventory();}
    private void closeLater(Player player,Inventory inventory){Bukkit.getScheduler().runTask(plugin,()->{if(player.getOpenInventory().getTopInventory()==inventory)player.closeInventory();});}
    private void openCreator(Player player,EquipmentSlot hand){ItemStack held=player.getInventory().getItem(hand);if(!DanmakuCraftingRules.id(held).equals(Ids.MOD_ID + ":danmaku_recipe_creator"))return;player.openInventory(new ShapeView(held,hand).inventory);}
    @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true)public void click(InventoryClickEvent event){
        var holder=event.getView().getTopInventory().getHolder();if(!(holder instanceof TableView)&&!(holder instanceof ShapeView))return;
        int size=event.getView().getTopInventory().getSize(),raw=event.getRawSlot();boolean ordinary=event.getClick()==ClickType.LEFT||event.getClick()==ClickType.RIGHT;
        if(raw<0||raw>=size){if(holder instanceof ShapeView||!ordinary)event.setCancelled(true);return;}event.setCancelled(true);
        if(!ordinary||!(event.getWhoClicked() instanceof Player player))return;
        if(holder instanceof ShapeView view){
            if(raw%9>=1&&raw%9<=6){int y=raw/9,x=raw%9-1;view.matrix[y][x]=!view.matrix[y][x];view.refresh();return;}
            if(raw==44){ItemStack current=player.getInventory().getItem(view.hand);
                if(current==null||!current.equals(view.original)){player.sendActionBar(Component.translatable("message.reverie_dreams.danmaku_creator.changed"));closeLater(player,view.inventory);return;}
                ItemStack result=rules.draw(current,view.matrix);if(result==null)player.sendActionBar(Component.translatable("item.action.click.shape_recipe.fail"));else{player.getInventory().setItem(view.hand,result);player.sendActionBar(Component.translatable("item.action.click.shape_recipe.success"));}
                closeLater(player,view.inventory);
            }else if(raw==53)closeLater(player,view.inventory);return;
        }
        var view=(TableView)holder;if(!near(player,view.controller)){closeLater(player,view.inventory);return;}
        if(!BukkitCraftEngine.instance().antiGriefProvider().test(player,Flag.OPEN_CONTAINER,view.controller.location()))return;
        if(raw==view.resultSlot){var result=view.controller.craftTo(player.getInventory());if(result.status()==DanmakuTableController.Status.SUCCESS)player.playSound(player.getLocation(),Sound.BLOCK_ANVIL_USE,SoundCategory.BLOCKS,1,1);else player.sendActionBar(Component.translatable("message.reverie_dreams.danmaku_table."+result.status().name().toLowerCase(Locale.ROOT)));return;}
        int slot=-1;for(int i=0;i<view.slots.length;i++)if(view.slots[i]==raw)slot=i;if(slot<0)return;
        ItemStack stored=view.controller.item(slot),cursor=event.getCursor();boolean right=event.getClick()==ClickType.RIGHT;
        if(DanmakuTableController.empty(cursor)){int count=DanmakuTableController.empty(stored)?0:right?(stored.getAmount()+1)/2:stored.getAmount();var removed=view.controller.take(slot,count);if(removed!=null)player.setItemOnCursor(removed);}
        else if(DanmakuTableController.empty(stored)||stored.isSimilar(cursor)){var offered=cursor.clone();if(right)offered.setAmount(1);var remainder=view.controller.offer(slot,offered);var next=cursor.clone();next.subtract(offered.getAmount()-(DanmakuTableController.empty(remainder)?0:remainder.getAmount()));player.setItemOnCursor(next.isEmpty()?null:next);}
        else if(view.controller.accepts(slot,cursor)&&cursor.getAmount()<=cursor.getMaxStackSize()){var removed=view.controller.take(slot,stored.getAmount());view.controller.offer(slot,cursor);player.setItemOnCursor(removed);}
    }
    @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true)public void drag(InventoryDragEvent event){var holder=event.getView().getTopInventory().getHolder();if((holder instanceof TableView||holder instanceof ShapeView)&&event.getRawSlots().stream().anyMatch(i->i<event.getView().getTopInventory().getSize()))event.setCancelled(true);}
    @EventHandler public void close(InventoryCloseEvent event){if(event.getInventory().getHolder() instanceof TableView view){var views=VIEWS.get(view.controller);if(views!=null){views.remove(view);if(views.isEmpty())VIEWS.remove(view.controller);}view.controller.dirty();}}
    @EventHandler(ignoreCancelled=true)public void move(PlayerMoveEvent event){if(event.getPlayer().getOpenInventory().getTopInventory().getHolder() instanceof TableView view&&!near(event.getPlayer(),view.controller))event.getPlayer().closeInventory();}
    @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true)public void breakBlock(BlockBreakEvent event){var controller=controllerAt(event.getBlock());if(controller!=null&&controller.snapshot().quarantined())event.setCancelled(true);}
    private static void protect(List<Block> blocks){blocks.removeIf(block->{var controller=controllerAt(block);return controller!=null&&controller.snapshot().quarantined();});}
    @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true)public void explode(BlockExplodeEvent event){protect(event.blockList());}
    @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true)public void explode(EntityExplodeEvent event){protect(event.blockList());}
    /** Native chest pool: 40% chance, one uniform choice because all eleven source weights are six. */
    public List<ItemStack> templateLoot(RandomGenerator random){if(random.nextFloat()>=.4f)return List.of();var names=rules.templates();var item=rules.createTemplate(names.get(random.nextInt(names.size())));return item==null?List.of():List.of(item);}
    public List<ItemStack> monsterLoot(RandomGenerator random){if(random.nextInt(100)>=45)return List.of();ItemStack power=DanmakuCraftingRules.create(Ids.MOD_ID + ":power"),point=DanmakuCraftingRules.create(Ids.MOD_ID + ":point");if(power==null||point==null)return List.of();power.setAmount(random.nextInt(4)+1);point.setAmount(random.nextInt(4)+1);return List.of(power,point);}
    public static DanmakuCraftingModule service(){if(instance==null)throw new IllegalStateException("Danmaku crafting not enabled");return instance;}
    @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true)public void loot(LootGenerateEvent event){if(TEMPLATE_LOOT.contains(event.getLootTable().getKey().asString()))event.getLoot().addAll(templateLoot(ThreadLocalRandom.current()));}
    @EventHandler public void death(EntityDeathEvent event){featherHolders.remove(event.getEntity().getUniqueId());if(event.getEntity() instanceof Monster)event.getDrops().addAll(monsterLoot(ThreadLocalRandom.current()));}
    private boolean visual(LivingEntity entity){return entity instanceof Mannequin&&entity.getPersistentDataContainer().has(new NamespacedKey(plugin,"npc/carrier"),PersistentDataType.STRING);}
    private boolean holdsFeather(LivingEntity entity){var equipment=entity.getEquipment();return equipment!=null&&(DanmakuCraftingRules.id(equipment.getItemInMainHand()).equals(Ids.MOD_ID + ":speed_feather")||DanmakuCraftingRules.id(equipment.getItemInOffHand()).equals(Ids.MOD_ID + ":speed_feather"));}
    private void track(LivingEntity entity){if(entity instanceof Player||visual(entity))return;if(holdsFeather(entity))featherHolders.put(entity.getUniqueId(),entity);else featherHolders.remove(entity.getUniqueId());}
    public void applyFeatherTick(LivingEntity entity){if(entity.isValid()&&!entity.isDead()&&!visual(entity)&&holdsFeather(entity)&&!entity.hasPotionEffect(PotionEffectType.SPEED))entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,1,0,false,false,true));}
    private void tick(){for(var player:Bukkit.getOnlinePlayers())applyFeatherTick(player);for(var iterator=featherHolders.values().iterator();iterator.hasNext();){var entity=iterator.next();if(!entity.isValid()||entity.isDead()){iterator.remove();continue;}applyFeatherTick(entity);}}
    @EventHandler public void equipment(EntityEquipmentChangedEvent event){track(event.getEntity());}
    @EventHandler public void load(EntitiesLoadEvent event){for(var entity:event.getEntities())if(entity instanceof LivingEntity living)track(living);}
    @EventHandler public void unload(EntitiesUnloadEvent event){for(var entity:event.getEntities())featherHolders.remove(entity.getUniqueId());}
    private static final class TableView implements InventoryHolder{
        final DanmakuTableController controller;final int[] slots;final int resultSlot;final Inventory inventory;
        TableView(DanmakuTableController controller){this.controller=controller;boolean craft=controller.kind()==DanmakuTableController.Kind.CRAFTING;slots=craft?CRAFT_SLOTS:STRENGTH_SLOTS;resultSlot=craft?22:6;inventory=Bukkit.createInventory(this,craft?27:9,Component.translatable("block.reverie_dreams."+(craft?"danmaku_crafting_table":"strength_table")));}
        public Inventory getInventory(){return inventory;}
        void refresh(){inventory.clear();for(int i=0;i<slots.length;i++)inventory.setItem(slots[i],controller.item(i));var plan=controller.preview();if(plan!=null&&plan.output()!=null)inventory.setItem(resultSlot,plan.output());}
    }
    private static final class ShapeView implements InventoryHolder{
        final Inventory inventory;final ItemStack original;final EquipmentSlot hand;boolean[][] matrix=new boolean[6][6];
        ShapeView(ItemStack source,EquipmentSlot hand){original=source.clone();this.hand=hand;var old=rules().shapeFor(source);if(old!=null)matrix=old;inventory=Bukkit.createInventory(this,54,source.effectiveName());refresh();}
        public Inventory getInventory(){return inventory;}
        void refresh(){inventory.clear();for(int y=0;y<6;y++)for(int x=0;x<6;x++){var item=new ItemStack(matrix[y][x]?Material.LIME_STAINED_GLASS_PANE:Material.GRAY_STAINED_GLASS_PANE);String coordinate=(x+1)+","+(y+1);item.editMeta(meta->meta.displayName(Component.text(coordinate)));inventory.setItem(y*9+x+1,item);}var save=new ItemStack(Material.EMERALD);save.editMeta(meta->meta.displayName(Component.translatable("message.reverie_dreams.danmaku_creator.save")));inventory.setItem(44,save);var cancel=new ItemStack(Material.BARRIER);cancel.editMeta(meta->meta.displayName(Component.translatable("message.reverie_dreams.danmaku_creator.cancel")));inventory.setItem(53,cancel);}
    }
    private static final class Behavior extends BukkitBlockBehavior implements EntityBlock{
        final DanmakuTableController.Kind kind;int id;Behavior(BlockDefinition block,DanmakuTableController.Kind kind){super(block);this.kind=kind;}
        public BlockEntityController createBlockEntityController(BlockEntity entity){return new DanmakuTableController(entity,kind);}
        public void initControllerId(int id){this.id=id;}
        public InteractionResult useOnBlock(UseOnContext context,ImmutableBlockState state){return use(context);}
        public InteractionResult useWithoutItem(UseOnContext context,ImmutableBlockState state){return use(context);}
        private InteractionResult use(UseOnContext context){if(!enabled()||context.getPlayer()==null)return InteractionResult.PASS;var entity=context.getLevel().storageWorld().getBlockEntityAtIfLoaded(context.getClickedPos());var controller=entity==null?null:entity.controller.getAt(DanmakuTableController.class,id);if(controller==null)return InteractionResult.FAIL;open((Player)context.getPlayer().platformPlayer(),controller);return InteractionResult.SUCCESS_AND_CANCEL;}
    }
    private static final class CreatorBehavior extends ItemBehavior{
        public InteractionResult use(World world,net.momirealms.craftengine.core.entity.player.Player player,InteractionHand hand){if(!enabled()||player==null)return InteractionResult.PASS;instance.openCreator((Player)player.platformPlayer(),hand==InteractionHand.MAIN_HAND?EquipmentSlot.HAND:EquipmentSlot.OFF_HAND);return InteractionResult.SUCCESS_AND_CANCEL;}
        public InteractionResult useOnBlock(UseOnContext context){return use(context.getLevel(),context.getPlayer(),context.getHand());}
    }
}
