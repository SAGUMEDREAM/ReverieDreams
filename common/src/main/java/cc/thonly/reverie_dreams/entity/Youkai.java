package cc.thonly.reverie_dreams.entity;

import cc.thonly.reverie_dreams.data.skin.SkinType;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;

public class Youkai extends BaseNPCLikeEntity {
    public Youkai(EntityType<? extends TamableAnimal> entityType, Level world, SkinType skinType) {
        super(entityType, world, skinType);
    }
}
