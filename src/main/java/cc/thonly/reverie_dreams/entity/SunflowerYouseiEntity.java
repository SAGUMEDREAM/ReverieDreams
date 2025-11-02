package cc.thonly.reverie_dreams.entity;

import cc.thonly.mystias_izakaya.item.MIItems;
import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.entity.ai.goal.DanmakuGoal;
import cc.thonly.reverie_dreams.entity.ai.goal.DifferentRevengeGoal;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.skin.SkinType;
import cc.thonly.reverie_dreams.inventory.NPCInventoryImpl;
import cc.thonly.reverie_dreams.server.DelayedTask;
import cc.thonly.reverie_dreams.util.entity.ModelUtil;
import de.tomalbrc.bil.core.model.Model;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

@Getter
public class SunflowerYouseiEntity extends BaseNPCLikeEntity implements Leashable, FriendlyFaction, Yousei {
    public static final ResourceLocation ID = ReverieDreams.id("yousei_wing");
    public static final Model MODEL = ModelUtil.loadModel(ID);

    public SunflowerYouseiEntity(EntityType<? extends TamableAnimal> entityType, Level world, SkinType skinType) {
        super(entityType, world, skinType);
        NPCInventoryImpl inventory = this.getInventory();
        inventory.setHead(Items.SUNFLOWER.getDefaultInstance());
        inventory.setMainHand(Items.SUNFLOWER.getDefaultInstance());
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
//        this.goalSelector.add(2, new SmartFlyGoal(this, 1.5));

//        this.goalSelector.add(8, new WanderAroundFarGoal(this, 1.0));

        this.goalSelector.addGoal(3, new DanmakuGoal(this, (self, target, world) -> {
            ItemStack stack = DanmakuTypes.random(DanmakuTypes.BUBBLE);
            float[] pitchYaw = MobDanmakuShooter.getPitchYaw(self, target);
            DelayedTask.repeat(world.getServer(), 2, 0.8f, () -> {
                MobDanmakuShooter.spawn(world, self, stack, pitchYaw[0], pitchYaw[1] - 15.0f, 0.5f, 5.0f, 0.2f);
                MobDanmakuShooter.spawn(world, self, stack, pitchYaw[0], pitchYaw[1], 0.5f, 5.0f, 0.2f);
                MobDanmakuShooter.spawn(world, self, stack, pitchYaw[0], pitchYaw[1] + 15.0f, 0.5f, 5.0f, 0.2f);
            });
        }));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 10.0f));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Mob.class, 10.0f));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new DifferentRevengeGoal(this).setGroupRevenge());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        int i = this.random.nextIntBetweenInclusive(1, 9);
        if (i<=3) {
            Level world = this.level();
            ItemStack itemStack = new ItemStack(MIItems.MOONFLOWER, this.random.nextIntBetweenInclusive(1,2));
            ItemEntity itemEntity = new ItemEntity(world, this.getX(), this.getY(), this.getZ(), itemStack);
            world.addFreshEntity(itemEntity);
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
    public boolean requiresCustomPersistence() {
        return false;
    }

}
