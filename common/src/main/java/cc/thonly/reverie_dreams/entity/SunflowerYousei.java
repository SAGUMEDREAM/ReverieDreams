package cc.thonly.reverie_dreams.entity;

import cc.thonly.reverie_dreams.api.entity.type.DanmakuShooter;
import cc.thonly.reverie_dreams.api.entity.type.FriendlyFaction;
import cc.thonly.reverie_dreams.api.entity.type.YouseiType;
import cc.thonly.reverie_dreams.data.skin.SkinType;
import cc.thonly.reverie_dreams.entity.ai.goal.DanmakuGoal;
import cc.thonly.reverie_dreams.entity.ai.goal.DifferentRevengeGoal;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.npc.KeepInventoryTypes;
import cc.thonly.reverie_dreams.inventory.NPCInventoryImpl;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDIngredientItems;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.server.DelayedTask;
import cc.thonly.reverie_dreams.util.entity.EntityHelper;
import lombok.Getter;
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
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

@Getter
public class SunflowerYousei extends BaseNPCLikeEntity implements Leashable, FriendlyFaction, YouseiType {
    public SunflowerYousei(EntityType<? extends TamableAnimal> entityType, Level world, SkinType skinType) {
        super(entityType, world, skinType);
        this.xpReward = 5;
        NPCInventoryImpl inventory = this.getInventory();
        inventory.setHead(Items.SUNFLOWER.getDefaultInstance());
        inventory.setMainHand(RDItems.SUNFLOWER.asItem().getDefaultInstance());
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
//        this.goalSelector.add(2, new SmartFlyGoal(this, 1.5));

//        this.goalSelector.add(8, new WanderAroundFarGoal(this, 1.0));

        this.goalSelector.addGoal(3, new DanmakuGoal(this, (self, target, world) -> {
            ItemStack stack = DanmakuTypes.random(DanmakuTypes.BUBBLE).create();
            float[] pitchYaw = DanmakuShooter.getPitchYaw(self, target);
            DelayedTask.repeat(world.getServer(), 2, 0.8f, () -> {
                DanmakuShooter.spawn(world, self, stack, pitchYaw[0], pitchYaw[1] - 15.0f, 0.5f, 5.0f, 0.2f);
                DanmakuShooter.spawn(world, self, stack, pitchYaw[0], pitchYaw[1], 0.5f, 5.0f, 0.2f);
                DanmakuShooter.spawn(world, self, stack, pitchYaw[0], pitchYaw[1] + 15.0f, 0.5f, 5.0f, 0.2f);
                DanmakuShooter.soundDefault(this);
            });
        }));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 10.0f));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Mob.class, 10.0f));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new DifferentRevengeGoal(this).setGroupRevenge());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));

        EntityHelper.registerHostilityAllRabbit(this, this.targetSelector);
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        int i = this.random.nextIntBetweenInclusive(1, 9);
        if (i<=3) {
            Level world = this.level();
            ItemStack itemStack = new ItemStack(RDIngredientItems.MOONFLOWER.asItem(), this.random.nextIntBetweenInclusive(1,2));
            ItemEntity itemEntity = new ItemEntity(world, this.getX(), this.getY(), this.getZ(), itemStack);
            world.addFreshEntity(itemEntity);
        }
    }

    @Override
    public KeepInventoryTypes getKeepInventoryType() {
        return KeepInventoryTypes.ONLY_ARMOR;
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
