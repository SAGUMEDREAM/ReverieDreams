package cc.thonly.reverie_dreams.server;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ArmorAttributeManager {
    public static final ArmorAttributeManager INSTANCE = new ArmorAttributeManager();
    private MinecraftServer server;
    private final List<Entry> entries = new ArrayList<>();

    private ArmorAttributeManager() {
    }

    public static List<Entry> register(ItemStackAction action, Item... items) {
        List<Entry> entries = INSTANCE.entries;
        List<Entry> entryList = new ArrayList<>();
        for (Item item : items) {
            Entry entry = new Entry((itemStack) -> itemStack.getItem() == item, action);
            entryList.add(entry);
            entries.add(entry);
        }
        return entryList;
    }

    public static Entry register(ItemStackAction action, Predicate<ItemStack> predicate) {
        List<Entry> entries = INSTANCE.entries;
        var entry = new Entry(predicate, action);
        entries.add(entry);
        return entry;
    }

    public static boolean unregister(Entry entry) {
        return INSTANCE.entries.remove(entry);
    }

    public void onTick() {
        PlayerList playerManager = this.server.getPlayerList();
        for (ServerPlayer player : playerManager.getPlayers()) {
            if (player.hasDisconnected()) {
                continue;
            }
            ItemStack[] itemStacks = {
                    player.getItemBySlot(EquipmentSlot.HEAD),
                    player.getItemBySlot(EquipmentSlot.CHEST),
                    player.getItemBySlot(EquipmentSlot.LEGS),
                    player.getItemBySlot(EquipmentSlot.FEET),
            };
            for (ItemStack itemStack : itemStacks) {
                for (Entry entry : this.entries) {
                    Predicate<ItemStack> predicate = entry.predicate();
                    if (predicate.test(itemStack)) {
                        entry.action.apply(player, itemStack);
                    }
                }
            }
        }
    }


    public static synchronized void tick(MinecraftServer server) {
        if (server == null) {
            return;
        }
        INSTANCE.server = server;
        INSTANCE.onTick();
    }

    public record Entry(Predicate<ItemStack> predicate, ItemStackAction action) {

    }

    public interface ItemStackAction {
        void apply(LivingEntity entity, ItemStack itemStack);
    }
}
