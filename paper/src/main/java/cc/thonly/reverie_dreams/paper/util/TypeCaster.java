package cc.thonly.reverie_dreams.paper.util;

import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.bukkit.entity.BukkitEntity;
import net.momirealms.craftengine.bukkit.item.BukkitItem;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.bukkit.world.BukkitExistingBlock;
import net.momirealms.craftengine.bukkit.world.BukkitWorld;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TypeCaster {
    public static BukkitServerPlayer getCraftEnginePlayer(Player player) {
        return BukkitAdaptor.adapt(player);
    }

    public static BukkitEntity getCraftEngineEntity(Entity entity) {
        return BukkitAdaptor.adapt(entity);
    }

    public static BukkitExistingBlock getCraftEngineBlock(Block block) {
        return BukkitAdaptor.adapt(block);
    }

    public static BukkitItem getCraftEngineItem(ItemStack itemStack) {
        return BukkitAdaptor.adapt(itemStack);
    }

    public static BukkitWorld getCraftEngineWorld(org.bukkit.World world) {
        return BukkitAdaptor.adapt(world);
    }
}
