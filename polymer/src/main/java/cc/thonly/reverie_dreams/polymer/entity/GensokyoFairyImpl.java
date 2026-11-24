package cc.thonly.reverie_dreams.polymer.entity;

import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.variant.GensokyoFairyAnimation;
import cc.thonly.reverie_dreams.entity.variant.GensokyoFairySkin;
import cc.thonly.reverie_dreams.entity.variant.GensokyoFairyAppearance;
import cc.thonly.reverie_dreams.fabric.util.AnimationHelper;
import cc.thonly.reverie_dreams.polymer.entity.holder.OverlayLivingEntityHolder;
import cc.thonly.reverie_dreams.polymer.entity.holder.GensokyoFairyHolder;
import cc.thonly.reverie_dreams.entity.variant.GensokyoFairyEntity;
import cc.thonly.reverie_dreams.polymer.entity.bridge.PolymerHolderEntity;
import cc.thonly.reverie_dreams.polymer.entity.bridge.TickHolderEntity;
import cc.thonly.reverie_dreams.polymer.helper.PolymerEntityHelper;
import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.api.AnimatedEntityHolder;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

public abstract class GensokyoFairyImpl<T extends BaseNPCLikeEntity>
        implements AnimatedEntity, PolymerHolderEntity, TickHolderEntity {
    protected final T source;
    private OverlayLivingEntityHolder<T, GensokyoFairyImpl<T>> holder;
    private GensokyoFairySkin skin;
    private GensokyoFairyAppearance appearance;
    private GensokyoFairyAnimation animation;
    private int actionRevision = -1;

    protected GensokyoFairyImpl(T source) {
        this.source = source;
        if (!source.level().isClientSide()) {
            PolymerEntityHelper.addEntityHolderModel(this, source);
        }
    }

    @Override
    public void onCreated() {
        if (this.source.isRemoved() || this.holder != null) {
            return;
        }
        this.createHolder();
        TickHolderEntity.addTickHolder(this);
        this.updateAnimation();
    }

    private void createHolder() {
        this.skin = GensokyoFairySkin.of(this.source.getSkinType().getId());
        this.appearance = GensokyoFairyAppearance.of(this.source);
        this.holder = new GensokyoFairyHolder<>(this.source, this,
                this.appearance != null
                        ? PolymerEntityHelper.getFairyAppearance(this.appearance) : PolymerEntityHelper.getFairyModel(this.skin));
        TickHolderEntity.addElementBind(this.source, this.holder);
        EntityAttachment.ofTicking(this.holder, this.source);
        this.animation = null;
    }

    @Override
    public void onTick() {
        if (this.holder == null) {
            return;
        }
        if (this.skin != GensokyoFairySkin.of(this.source.getSkinType().getId())
                || this.appearance != GensokyoFairyAppearance.of(this.source)) {
            this.holder.destroy();
            this.createHolder();
        }
        this.updateAnimation();
        if (this.source.isDeadOrDying()) this.holder.clearColor();
        else AnimationHelper.updateHurtVariant(this.source, this.holder);
    }

    private void updateAnimation() {
        var actions = ((GensokyoFairyEntity) this.source).fairyActions();
        GensokyoFairyAnimation next = actions.select(this.source);
        if (this.animation == next && (next.looping() || this.actionRevision == actions.revision())) {
            return;
        }
        var animator = this.holder.getAnimator();
        if (this.animation != null) {
            animator.stopAnimation(this.animation.animationName());
        }
        animator.playAnimation(next.animationName());
        this.animation = next;
        this.actionRevision = actions.revision();
    }

    @Override
    public LivingEntity getSource() {
        return this.source;
    }

    @Override
    public AnimatedEntityHolder getHolder() {
        return this.holder;
    }

    @Override
    public EntityType<?> getPolymerEntityType(PacketContext context) {
        return EntityType.BLOCK_DISPLAY;
    }
}
