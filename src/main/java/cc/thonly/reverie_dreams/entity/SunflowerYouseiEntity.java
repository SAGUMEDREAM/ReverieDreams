package cc.thonly.reverie_dreams.entity;

import cc.thonly.mystias_izakaya.item.MIItems;
import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.entity.ai.goal.DanmakuGoal;
import cc.thonly.reverie_dreams.entity.ai.goal.DifferentRevengeGoal;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.skin.SkinType;
import cc.thonly.reverie_dreams.inventory.NPCInventoryImpl;
import cc.thonly.reverie_dreams.server.DelayedTask;
import cc.thonly.reverie_dreams.util.entity.ModelUtil;
import com.mojang.authlib.properties.Property;
import de.tomalbrc.bil.core.model.Model;
import lombok.Getter;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.Leashable;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.function.Supplier;

@Getter
public class SunflowerYouseiEntity extends BaseNPCLikeEntity implements Leashable, FriendlyFaction, Yousei {
    public static final Identifier ID = Touhou.id("yousei_wing");
    public static final Model MODEL = ModelUtil.loadModel(ID);

    public SunflowerYouseiEntity(EntityType<? extends TameableEntity> entityType, World world, SkinType skinType) {
        super(entityType, world, skinType);
        NPCInventoryImpl inventory = this.getInventory();
        inventory.setHead(Items.SUNFLOWER.getDefaultStack());
        inventory.setMainHand(Items.SUNFLOWER.getDefaultStack());
    }

    @Override
    protected void initGoals() {
        super.initGoals();

        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new SitGoal(this));
//        this.goalSelector.add(2, new SmartFlyGoal(this, 1.5));

//        this.goalSelector.add(8, new WanderAroundFarGoal(this, 1.0));

        this.goalSelector.add(3, new DanmakuGoal(this, (self, target, world) -> {
            ItemStack stack = DanmakuTypes.random(DanmakuTypes.BUBBLE);
            float[] pitchYaw = MobDanmakuShooter.getPitchYaw(self, target);
            DelayedTask.repeat(world.getServer(), 2, 0.8f, () -> {
                MobDanmakuShooter.spawn(world, self, stack, pitchYaw[0], pitchYaw[1] - 15.0f, 0.5f, 5.0f, 0.2f);
                MobDanmakuShooter.spawn(world, self, stack, pitchYaw[0], pitchYaw[1], 0.5f, 5.0f, 0.2f);
                MobDanmakuShooter.spawn(world, self, stack, pitchYaw[0], pitchYaw[1] + 15.0f, 0.5f, 5.0f, 0.2f);
            });
        }));
        this.goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 10.0f));
        this.goalSelector.add(4, new LookAtEntityGoal(this, MobEntity.class, 10.0f));
        this.goalSelector.add(5, new LookAroundGoal(this));

        this.targetSelector.add(1, new DifferentRevengeGoal(this).setGroupRevenge());
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        this.targetSelector.add(3, new ActiveTargetGoal<>(this, IronGolemEntity.class, true));
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        super.onDeath(damageSource);
        int i = this.random.nextBetween(1, 9);
        if (i<=3) {
            World world = this.getWorld();
            ItemStack itemStack = new ItemStack(MIItems.MOONFLOWER, this.random.nextBetween(1,2));
            ItemEntity itemEntity = new ItemEntity(world, this.getX(), this.getY(), this.getZ(), itemStack);
            world.spawnEntity(itemEntity);
        }
    }

    @Override
    public Boolean canPickItem() {
        return false;
    }

    @Override
    public String getFactionId() {
        return "mob";
    }

    @Override
    public boolean cannotDespawn() {
        return false;
    }

}
