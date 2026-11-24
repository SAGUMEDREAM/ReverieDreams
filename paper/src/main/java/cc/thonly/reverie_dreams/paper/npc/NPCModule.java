package cc.thonly.reverie_dreams.paper.npc;

import org.bukkit.plugin.java.JavaPlugin;

/** Paper26.1.2 NPC lifecycle; source FARM/COMBAT cohort plus explicit remaining work modes. */
public final class NPCModule {
    private NPCService service;
    private NPCInteraction interaction;
    private NPCAiService ai;
    private NPCWorkMenu workMenu;
    public void enable(JavaPlugin plugin) {
        if (service != null) throw new IllegalStateException("NPC module already enabled");
        service = new NPCService(plugin);
        service.enable();
        interaction = new NPCInteraction(plugin, service);
        interaction.enable();
        ai = new NPCAiService(plugin, service, interaction::refreshNPC); ai.enable();
        workMenu = new NPCWorkMenu(plugin, service, ai); workMenu.enable();
        interaction.setWorkMenuHandler(workMenu::open);
    }
    public NPCService service() {
        if (service == null) throw new IllegalStateException("NPC module is not enabled");
        return service;
    }
    public NPCInteraction interaction() {
        if (interaction == null) throw new IllegalStateException("NPC module is not enabled");
        return interaction;
    }
    public NPCAiService ai() {
        if (ai == null) throw new IllegalStateException("NPC AI is not enabled");
        return ai;
    }
    public void disable() {
        if (workMenu != null) { workMenu.disable(); workMenu = null; }
        if (interaction != null) interaction.setWorkMenuHandler(null);
        if (ai != null) { ai.disable(); ai = null; }
        if (interaction != null) { interaction.disable(); interaction = null; }
        if (service != null) { service.disable(); service = null; }
    }
}
