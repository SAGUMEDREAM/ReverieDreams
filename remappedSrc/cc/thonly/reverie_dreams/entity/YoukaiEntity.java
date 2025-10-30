package cc.thonly.reverie_dreams.entity;

import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.skin.SkinType;
import com.mojang.authlib.properties.Property;
import java.util.function.Supplier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;

public class YoukaiEntity extends BaseNPCLikeEntity {
    public YoukaiEntity(EntityType<? extends TamableAnimal> entityType, Level world, SkinType skinType) {
        super(entityType, world, skinType);
    }
}
