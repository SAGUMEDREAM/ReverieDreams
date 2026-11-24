package cc.thonly.reverie_dreams.api.level;

import net.minecraft.world.level.Level;

public interface WorldExtension {

    static WorldExtension get(Level level) {
        return (WorldExtension) level;
    }

}
