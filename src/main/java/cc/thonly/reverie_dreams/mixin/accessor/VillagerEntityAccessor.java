package cc.thonly.reverie_dreams.mixin.accessor;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Villager.class)
public interface VillagerEntityAccessor {
    @Accessor("DATA_VILLAGER_DATA")
    public static EntityDataAccessor<VillagerData> getVillagerData() {
        throw new UnsupportedOperationException();
    }

}
