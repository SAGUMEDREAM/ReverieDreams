package cc.thonly.reverie_dreams.entity;

import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.skin.SkinType;
import com.mojang.authlib.properties.Property;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.world.World;

import java.util.function.Supplier;

public class YoukaiEntity extends BaseNPCLikeEntity {
    public YoukaiEntity(EntityType<? extends TameableEntity> entityType, World world, SkinType skinType) {
        super(entityType, world, skinType);
    }
}
