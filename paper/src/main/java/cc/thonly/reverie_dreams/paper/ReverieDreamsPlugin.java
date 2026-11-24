package cc.thonly.reverie_dreams.paper;

import cc.thonly.reverie_dreams.paper.registry.InitHolder;
import cc.thonly.reverie_dreams.paper.production.ProductionModule;
import cc.thonly.reverie_dreams.paper.food.FoodConsumptionModule;
import cc.thonly.reverie_dreams.paper.npc.NPCModule;
import cc.thonly.reverie_dreams.paper.command.TouhouCommands;
import cc.thonly.reverie_dreams.paper.kitchen.KitchenModule;
import cc.thonly.reverie_dreams.paper.altar.AltarModule;
import cc.thonly.reverie_dreams.paper.equipment.EquipmentModule;
import cc.thonly.reverie_dreams.paper.danmaku.DanmakuModule;
import cc.thonly.reverie_dreams.paper.tools.ToolsModule;
import cc.thonly.reverie_dreams.paper.farming.FarmingModule;
import cc.thonly.reverie_dreams.paper.danmakucrafting.DanmakuCraftingModule;
import cc.thonly.reverie_dreams.paper.specialitems.SpecialItemsModule;
import cc.thonly.reverie_dreams.paper.storage.StorageSpecialModule;
import cc.thonly.reverie_dreams.paper.specialitems2.SpecialItemsExpansion;
import cc.thonly.reverie_dreams.paper.plants.PlantsModule;
import cc.thonly.reverie_dreams.paper.specialweapons.SpecialWeaponsModule;
import org.bukkit.plugin.java.JavaPlugin;
import cc.thonly.reverie_dreams.paper.worldbridge.CraftEngineWorldBridge;
import org.bukkit.scheduler.BukkitTask;

@SuppressWarnings("LombokGetterMayBeUsed")
public final class ReverieDreamsPlugin extends JavaPlugin {
    private static ReverieDreamsPlugin INSTANCE;
    private DefaultEventBusImpl bus;
    private FoodConsumptionModule food;
    private final NPCModule npc = new NPCModule();
    private final TouhouCommands commands = new TouhouCommands();
    private final EquipmentModule equipment = new EquipmentModule();
    private final ToolsModule tools = new ToolsModule();
    private BukkitTask worldBindings;

    public ReverieDreamsPlugin() {
        INSTANCE = this;
    }

    @Override
    public void onLoad() {
        // Paper loads plugins before enabling them. Register factories before
        // CraftEngine 26.8.2 parses resource configurations during enable.
        InitHolder.initialize();
        ProductionModule.registerTypes();
        KitchenModule.registerTypes();
        AltarModule.registerTypes();
        DanmakuModule.registerTypes();
        DanmakuCraftingModule.registerTypes();
        FarmingModule.registerTypes();
        SpecialItemsModule.registerTypes();
        StorageSpecialModule.registerTypes();
        SpecialItemsExpansion.registerTypes();
        PlantsModule.registerTypes();
        SpecialWeaponsModule.registerTypes();
        if (getServer().getPluginManager().getPlugin("ReverieDreamsWorld") != null) {
            CraftEngineWorldBridge.registerResolver();
        }
    }

    @Override
    public void onEnable() {
        this.bus = new DefaultEventBusImpl(this);
        this.bus.start();
        ProductionModule.enable(this);
        KitchenModule.enable(this);
        food = new FoodConsumptionModule(this);
        food.enable();
        npc.enable(this);
        SpecialItemsModule.enable(this);
        StorageSpecialModule.enable(this);
        DanmakuModule.enable(this);
        DanmakuCraftingModule.enable(this);
        SpecialItemsExpansion.enable(this);
        SpecialWeaponsModule.enable(this);
        FarmingModule.enable(this);
        PlantsModule.enable(this);
        equipment.enable(this);
        tools.enable(this);
        AltarModule.enable(this, npc.interaction()::createRoleCard);
        commands.enable(this);
        if (getServer().getPluginManager().isPluginEnabled("ReverieDreamsWorld")) {
            worldBindings = CraftEngineWorldBridge.connect(this);
        }
    }

    @Override
    public void onDisable() {
        if (worldBindings != null) { worldBindings.cancel(); worldBindings = null; }
        commands.disable();
        AltarModule.disable();
        SpecialItemsExpansion.disable();
        SpecialWeaponsModule.disable();
        PlantsModule.disable();
        FarmingModule.disable();
        DanmakuCraftingModule.disable();
        DanmakuModule.disable();
        equipment.disable();
        tools.disable();
        SpecialItemsModule.disable();
        StorageSpecialModule.disable();
        KitchenModule.disable();
        npc.disable();
        if (food != null) {
            food.disable();
            food = null;
        }
        ProductionModule.disable();
        if (this.bus != null) {
            this.bus.stop();
            this.bus = null;
        }
        INSTANCE = null;
    }

    public DefaultEventBusImpl getBus() {
        return this.bus;
    }

    public FoodConsumptionModule getFood() { return food; }
    public NPCModule getNPC() { return npc; }
    public EquipmentModule getEquipment() { return equipment; }
    public ToolsModule getTools() { return tools; }

    public static ReverieDreamsPlugin getMod() {
        return INSTANCE;
    }

}
