package cc.thonly.reverie_dreams.paper.command;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.CookingRecipe;
import org.bukkit.inventory.StonecuttingRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.bukkit.command.CommandSender;

/** Paper command entry and read-only browser for the currently registered native recipes. */
@SuppressWarnings("UnstableApiUsage")
public final class TouhouCommands implements Listener {
    private JavaPlugin plugin;
    public void enable(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event ->
                event.registrar().register(Commands.literal("touhou")
                        .executes(context -> help(context.getSource().getSender()))
                        .then(Commands.literal("help").executes(context -> help(context.getSource().getSender())))
                        .then(Commands.literal("about").executes(context -> {
                            context.getSource().getSender().sendMessage(Component.text("ReverieDreams — Paper + CraftEngine " + plugin.getPluginMeta().getVersion(), NamedTextColor.AQUA));
                            return 1;
                        }))
                        .then(Commands.literal("recipe").executes(context -> {
                            if (context.getSource().getSender() instanceof Player player) openList(player, 0);
                            else context.getSource().getSender().sendMessage(Component.text("请在游戏中打开配方浏览器。"));
                            return 1;
                        })).build(), "ReverieDreams commands"));
    }

    public void disable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof View) player.closeInventory();
        }
        HandlerList.unregisterAll(this);
    }

    private int help(CommandSender sender) {
        sender.sendMessage(Component.text("/touhou help · /touhou about · /touhou recipe", NamedTextColor.AQUA));
        return 1;
    }

    private List<Recipe> recipes() {
        List<Recipe> recipes = new ArrayList<>();
        Bukkit.recipeIterator().forEachRemaining(recipe -> {
            if (recipe instanceof Keyed keyed && keyed.getKey().getNamespace().equals("reverie_dreams")) recipes.add(recipe);
        });
        recipes.sort(Comparator.comparing(recipe -> ((Keyed) recipe).getKey().asString()));
        return List.copyOf(recipes);
    }

    private void openList(Player player, int requestedPage) {
        List<Recipe> all = recipes();
        int page = Math.max(0, Math.min(requestedPage, Math.max(0, (all.size() - 1) / 45)));
        View view = new View(all, page, false, 54, "ReverieDreams · 原生配方 " + (page + 1));
        for (int i = 0; i < 45 && page * 45 + i < all.size(); i++) {
            Recipe recipe = all.get(page * 45 + i);
            ItemStack icon = recipe.getResult().clone();
            icon.editMeta(meta -> meta.lore(List.of(Component.text(((Keyed) recipe).getKey().asString(), NamedTextColor.GRAY))));
            view.inventory.setItem(i, icon);
        }
        if (page > 0) view.inventory.setItem(45, button(Material.ARROW, "上一页"));
        if ((page + 1) * 45 < all.size()) view.inventory.setItem(53, button(Material.ARROW, "下一页"));
        view.inventory.setItem(49, button(Material.BOOK, all.size() + " 条已加载原生配方"));
        player.openInventory(view.inventory);
    }

    private void openRecipe(Player player, View parent, int index) {
        Recipe recipe = parent.recipes.get(index);
        View detail = new View(parent.recipes, parent.page, true, 45, ((Keyed) recipe).getKey().getKey());
        if (recipe instanceof ShapedRecipe shaped) {
            String[] shape = shaped.getShape();
            var choices = shaped.getChoiceMap();
            for (int row = 0; row < shape.length; row++) for (int col = 0; col < shape[row].length(); col++) {
                char key = shape[row].charAt(col);
                if (key != ' ') detail.inventory.setItem(10 + row * 9 + col, choice(choices.get(key)));
            }
        } else if (recipe instanceof ShapelessRecipe shapeless) {
            int slot = 0;
            for (RecipeChoice input : shapeless.getChoiceList()) {
                detail.inventory.setItem(10 + slot / 3 * 9 + slot % 3, choice(input));
                slot++;
            }
        } else if (recipe instanceof CookingRecipe<?> cooking) {
            detail.inventory.setItem(19, choice(cooking.getInputChoice()));
            detail.inventory.setItem(22, button(Material.CLOCK, cooking.getCookingTime() + " tick · " + cooking.getExperience() + " XP"));
        } else if (recipe instanceof StonecuttingRecipe stonecutting) {
            detail.inventory.setItem(19, choice(stonecutting.getInputChoice()));
        }
        detail.inventory.setItem(25, recipe.getResult().clone());
        detail.inventory.setItem(40, button(Material.ARROW, "返回配方列表"));
        player.openInventory(detail.inventory);
    }

    private static ItemStack choice(RecipeChoice choice) {
        if (choice instanceof RecipeChoice.ExactChoice exact) return exact.getChoices().getFirst().clone();
        if (choice instanceof RecipeChoice.MaterialChoice materials) return new ItemStack(materials.getChoices().getFirst());
        return button(Material.BARRIER, "此配方的输入需要专门的浏览界面");
    }

    private static ItemStack button(Material material, String text) {
        var item = new ItemStack(material);
        item.editMeta(meta -> meta.itemName(Component.text(text)));
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof View view)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player) || !event.isLeftClick()) return;
        int slot = event.getRawSlot();
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!player.isOnline() || player.getOpenInventory().getTopInventory().getHolder() != view) return;
            if (view.detail) {
                if (slot == 40) openList(player, view.page);
            } else if (slot == 45 && view.page > 0) openList(player, view.page - 1);
            else if (slot == 53 && (view.page + 1) * 45 < view.recipes.size()) openList(player, view.page + 1);
            else if (slot >= 0 && slot < 45 && view.page * 45 + slot < view.recipes.size()) openRecipe(player, view, view.page * 45 + slot);
        });
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof View) event.setCancelled(true);
    }

    private static final class View implements InventoryHolder {
        private final List<Recipe> recipes;
        private final int page;
        private final boolean detail;
        private final Inventory inventory;
        private View(List<Recipe> recipes, int page, boolean detail, int size, String title) {
            this.recipes = recipes; this.page = page; this.detail = detail;
            inventory = Bukkit.createInventory(this, size, Component.text(title));
        }
        @Override public Inventory getInventory() { return inventory; }
    }
}
