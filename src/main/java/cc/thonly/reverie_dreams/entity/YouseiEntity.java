package cc.thonly.reverie_dreams.entity;

import cc.thonly.reverie_dreams.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.entity.ai.goal.DanmakuGoal;
import cc.thonly.reverie_dreams.entity.ai.goal.UniversalLivingAngerGoal;
import cc.thonly.reverie_dreams.entity.holder.WingHolder;
import cc.thonly.reverie_dreams.entity.npc.NPCEntityImpl;
import cc.thonly.reverie_dreams.entity.variant.YouseiVariant;
import cc.thonly.reverie_dreams.entity.variant.YouseiVariants;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import cc.thonly.reverie_dreams.server.DelayedTask;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Leashable;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector3f;

@Setter
@Getter
public class YouseiEntity extends NPCEntityImpl implements Leashable, FriendlyFaction, VariantData, Yousei {
    private YouseiVariant variant = null;

    public YouseiEntity(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType,
                world,
                YouseiVariants.isEmpty() ? ((YouseiVariant) (YouseiVariants.REGISTRY.getDefaultEntry().isPresent() ? YouseiVariants.REGISTRY.getDefaultEntry().get() : YouseiVariants.BLUE)).getProperty() : YouseiVariants.random().getProperty()
        );
        this.variant = YouseiVariants.getFromProperty(this.getSkin());
    }

    @Override
    protected void initGoals() {
        super.initGoals();

        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new DanmakuGoal(this, (self, target, world) -> {
            ItemStack stack = DanmakuTypes.random(DanmakuTypes.FIREBALL_GLOWY);
            final MinecraftServer server = world.getServer();
            final float[] pitchYaw = MobDanmakuShooter.getPitchYaw(self, target);

            DelayedTask.repeat(server, 1, 0.3f, () -> {
                MobDanmakuShooter.spawn(world, self, stack, pitchYaw[0], pitchYaw[1] - 15.0f, 0.5f, 5.0f, 0.2f);
                MobDanmakuShooter.spawn(world, self, stack, pitchYaw[0], pitchYaw[1], 0.5f, 5.0f, 0.2f);
                MobDanmakuShooter.spawn(world, self, stack, pitchYaw[0], pitchYaw[1] + 15.0f, 0.5f, 5.0f, 0.2f);
            });
        }));
//        this.goalSelector.add(2, new SmartFlyGoal(this, 1.2));

        this.goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 6.0f));
        this.goalSelector.add(4, new LookAtEntityGoal(this, MobEntity.class, 6.0f));
        this.goalSelector.add(5, new LookAroundGoal(this));

        this.goalSelector.add(8, new WanderAroundFarGoal(this, 1.0));

        this.targetSelector.add(1, new RevengeGoal(this).setGroupRevenge());
        this.targetSelector.add(3, new UniversalLivingAngerGoal<>(this, false));
    }

    @Override
    public void tick() {
        this.skin = this.variant != null ? this.variant.getProperty() : YouseiVariants.BLUE.getProperty();
        super.tick();
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        Vec3d vec3d = getVelocity();
        if (!isOnGround() && vec3d.y < 0.0 && !(moveControl.getTargetY() < getY())) {
            setVelocity(vec3d.multiply(1.0, 0.6, 1.0));
        }
    }

    @Override
    public void readCustomData(ReadView view) {
        super.readCustomData(view);
        String youseiVariantId = view.getString("YouseiVariant", YouseiVariants.DEFAULT_ID.toString());
        Identifier variantId = Identifier.of(youseiVariantId);
        this.variant = RegistryManager.YOUSEI_VARIANT.get(variantId);
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
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
    public void setVariantData(Identifier id) {
        this.variant = RegistryManager.YOUSEI_VARIANT.get(id);
        if (this.variant != null) {
            this.skin = this.variant.getProperty();
        }
    }

    @Override
    public Identifier getVariantData() {
        return this.variant.getId();
    }

    @Override
    public boolean cannotDespawn() {
        return false;
    }
}
