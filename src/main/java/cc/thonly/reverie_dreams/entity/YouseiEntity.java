package cc.thonly.reverie_dreams.entity;

import cc.thonly.reverie_dreams.entity.ai.goal.DanmakuGoal;
import cc.thonly.reverie_dreams.entity.ai.goal.UniversalLivingAngerGoal;
import cc.thonly.reverie_dreams.entity.interfaces.DanmakuShooter;
import cc.thonly.reverie_dreams.entity.interfaces.FriendlyFaction;
import cc.thonly.reverie_dreams.entity.interfaces.VariantData;
import cc.thonly.reverie_dreams.entity.interfaces.Yousei;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.variant.YouseiVariant;
import cc.thonly.reverie_dreams.entity.variant.YouseiVariants;
import cc.thonly.reverie_dreams.registry.RegistryHandlers;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.server.DelayedTask;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

@Setter
@Getter
public class YouseiEntity extends BaseNPCLikeEntity implements Leashable, FriendlyFaction, VariantData, Yousei {
    private YouseiVariant variant = null;

    public YouseiEntity(EntityType<? extends TamableAnimal> entityType, Level world) {
        super(entityType,
                world,
                (
                        YouseiVariants.isEmpty()
                                ? (YouseiVariants.REGISTRY.getAny().isPresent() ? YouseiVariants.REGISTRY.getAny().get().value().getSkinType() : YouseiVariants.BLUE.getSkinType())
                                : Objects.requireNonNull(YouseiVariants.random()).getSkinType()
                )
       );
        this.xpReward = 5;
        this.variant = YouseiVariants.getFromProperty(this.getSkin());
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new DanmakuGoal(this, (self, target, world) -> {
            ItemStack stack = DanmakuTypes.random(DanmakuTypes.FIREBALL_GLOWY);
            final MinecraftServer server = world.getServer();
            final float[] pitchYaw = DanmakuShooter.getPitchYaw(self, target);

            DelayedTask.repeat(server, 1, 0.3f, () -> {
                DanmakuShooter.spawn(world, self, stack, pitchYaw[0], pitchYaw[1] - 15.0f, 0.5f, 5.0f, 0.2f);
                DanmakuShooter.spawn(world, self, stack, pitchYaw[0], pitchYaw[1], 0.5f, 5.0f, 0.2f);
                DanmakuShooter.spawn(world, self, stack, pitchYaw[0], pitchYaw[1] + 15.0f, 0.5f, 5.0f, 0.2f);
            });
        }));
//        this.goalSelector.add(2, new SmartFlyGoal(this, 1.2));

        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Mob.class, 6.0f));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 1.0));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(3, new UniversalLivingAngerGoal<>(this, false));
    }

    @Override
    public void tick() {
        this.skinType = this.variant != null ? this.variant.getSkinType() : YouseiVariants.BLUE.getSkinType();
        super.tick();
        this.onEntityTick();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        Vec3 vec3d = getDeltaMovement();
        if (!onGround() && vec3d.y < 0.0 && !(moveControl.getWantedY() < getY())) {
            setDeltaMovement(vec3d.multiply(1.0, 0.6, 1.0));
        }
    }

    @Override
    public void readAdditionalSaveData(ValueInput view) {
        super.readAdditionalSaveData(view);
        String youseiVariantId = view.getStringOr("YouseiVariant", YouseiVariants.DEFAULT_ID.toString());
        ResourceLocation variantId = ResourceLocation.parse(youseiVariantId);
        this.variant = RegistryHandlers.YOUSEI_VARIANT.getValue(variantId);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput view) {
        super.addAdditionalSaveData(view);
        view.putString("YouseiVariant", this.variant.getId().toString());
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
    public void setVariantData(ResourceLocation id) {
        this.variant = RegistryHandlers.YOUSEI_VARIANT.getValue(id);
        if (this.variant != null) {
            this.skinType = this.variant.getSkinType();
        }
    }

    @Override
    public ResourceLocation getVariantData() {
        return this.variant.getId();
    }

    @Override
    public boolean requiresCustomPersistence() {
        return false;
    }
}
