package cc.thonly.reverie_dreams.entity.misc;


import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.registry.content.item.RDEntityHolderItems;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Setter
@Getter
@ToString
@SuppressWarnings("unchecked")
public class KnifeEntity extends DanmakuEntity {

    public KnifeEntity(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public KnifeEntity(Level level) {
        super(level);
    }

    public KnifeEntity(@NotNull LivingEntity owner, Level level, ItemStack item) {
        super(owner, level, item);
    }

    public KnifeEntity(double x, double y, double z, Level level, ItemStack item) {
        super(x, y, z, level, item);
    }

    @Override
    public ItemStack getItemStack() {
        return RDEntityHolderItems.KNIFE_DISPLAY.createStack();
    }

    @Override
    public ItemStack getItem() {
        return RDEntityHolderItems.KNIFE_DISPLAY.createStack();
    }

    @Override
    public Item getDefaultItem() {
        return RDEntityHolderItems.KNIFE_DISPLAY.asItem();
    }
}
