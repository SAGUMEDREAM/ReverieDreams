package cc.thonly.reverie_dreams.entity;

import cc.thonly.polymer.PolymerEntityHelper;
import cc.thonly.polymer.entity.PolymerHolderEntity;
import cc.thonly.polymer.entity.TickHolderEntity;
import cc.thonly.polymer.entity.bil.OverlayEntityHolder;
import cc.thonly.polymer.entity.bil.OverlayLivingEntityHolder;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.util.entity.AnimationHelper;
import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.api.AnimatedEntityHolder;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ScarecrowEntity extends Mob implements AnimatedEntity, PolymerHolderEntity, TickHolderEntity {
    private OverlayEntityHolder<ScarecrowEntity, AnimatedEntity> holder;
    public int attackCnt = 0;

    public ScarecrowEntity(Level level) {
        super(RDEntityTypes.SCARECROW, level);
        PolymerEntityHelper.addEntityHolderModel(this);
    }

    public ScarecrowEntity(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
        PolymerEntityHelper.addEntityHolderModel(this);
    }

    public static AttributeSupplier.Builder createLivingAttributes() {
        AttributeSupplier.Builder attributes = Mob.createLivingAttributes();
        return attributes.add(Attributes.KNOCKBACK_RESISTANCE, 1.0D).add(Attributes.FOLLOW_RANGE);
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        if (this.level() instanceof ServerLevel serverLevel) {
            ItemEntity itemEntity = new ItemEntity(serverLevel, this.getX(), this.getY(), this.getZ(), new ItemStack(RDItems.SCARECROW));
            serverLevel.addFreshEntity(itemEntity);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel world, DamageSource source, float amount) {
        Entity attacker = source.getEntity();

        if (attacker == null) {
            return super.hurtServer(world, source, amount);
        }

        if (attacker instanceof ServerPlayer player) {
            String formattedAmount = String.format("%.1f", amount);
            MutableComponent component = Component.empty()
                    .append(this.getName())
                    .append(Component.literal(": §%s%s%s".formatted(
                            amount == 0 ? "e" : amount > 0 ? "c" : "a",
                            amount == 0 ? "" : amount > 0 ? "-" : "+",
                            formattedAmount
                    )));
            player.sendSystemMessage(component);

            this.hurtTime = 5;
            this.hurtDuration = 5;

            ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (attacker.isShiftKeyDown() && mainHand.isEmpty()) {
                if (this.attackCnt < 2) {
                    this.attackCnt++;
                    return false;
                }
                ItemEntity itemEntity = new ItemEntity(world, this.getX(), this.getY(), this.getZ(), new ItemStack(RDItems.SCARECROW));
                world.addFreshEntity(itemEntity);
                this.discard();
                return false;
            }
            world.playSound(null, this.blockPosition(), SoundEvents.GENERIC_HURT, SoundSource.PLAYERS, 1.0F, 1.0F);

        }

        return false;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        Level world = this.level();
        if (world.isClientSide || !(world instanceof ServerLevel serverWorld) || !(player instanceof ServerPlayer serverPlayer)) {
            return super.mobInteract(player, hand);
        }
        if (player.isShiftKeyDown() && player.getItemInHand(hand).isEmpty()) {
            double dx = player.getX() - this.getX();
            double dz = player.getZ() - this.getZ();
            float yaw = (float) (Math.atan2(dz, dx) * (180 / Math.PI)) - 90f;

            this.setYRot(yaw);
            this.setYBodyRot(yaw);
            this.yHeadRot = yaw;
            this.yHeadRotO = yaw;
            this.yBodyRotO = yaw;
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void push(double d, double e, double f) {

    }

    @Override
    protected void pushEntities() {

    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Override
    public void onCreated() {
        this.holder = new OverlayLivingEntityHolder<>(this, this, PolymerEntityHelper.SCARECROW_MODEL);
        TickHolderEntity.addTickHolder(this);
        TickHolderEntity.addElementBind(this, this.holder);
        EntityAttachment.ofTicking(this.holder, this);
    }
    @Override
    public void onTick() {
        if (this.holder == null) {
            return;
        }
        if (this.tickCount % 2 == 0) {
            AnimationHelper.updateWalkAnimation(this, this.holder);
            AnimationHelper.updateHurtVariant(this, this.holder);
        }
    }
    @Override
    public LivingEntity getEntity() {
        return this;
    }

    @Override
    public AnimatedEntityHolder getHolder() {
        return this.holder;
    }
}
