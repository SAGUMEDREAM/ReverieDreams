package cc.thonly.reverie_dreams.polymer.entity;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.entity.aquatic.AquaticSpecies;
import cc.thonly.reverie_dreams.entity.aquatic.CuisineAquaticEntity;
import cc.thonly.reverie_dreams.fabric.util.ModelUtil;
import cc.thonly.reverie_dreams.fabric.util.AnimationHelper;
import cc.thonly.reverie_dreams.polymer.entity.holder.OverlayLivingEntityHolder;
import cc.thonly.reverie_dreams.polymer.entity.bridge.PolymerHolderEntity;
import cc.thonly.reverie_dreams.polymer.entity.bridge.TickHolderEntity;
import cc.thonly.reverie_dreams.polymer.helper.PolymerEntityHelper;
import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.api.AnimatedEntityHolder;
import de.tomalbrc.bil.core.model.Model;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.world.entity.EntityType;

import java.util.EnumMap;
import java.util.Map;

public final class CuisineAquaticImpl implements AnimatedEntity, PolymerHolderEntity, TickHolderEntity {
    private static final Map<AquaticSpecies, Model> MODELS = loadModels();
    private final CuisineAquaticEntity entity;
    private OverlayLivingEntityHolder<CuisineAquaticEntity, CuisineAquaticImpl> holder;
    private String currentAnimation;

    /** Register every bone model before Polymer builds its first resource pack. */
    public static void initialize() {}

    public CuisineAquaticImpl(CuisineAquaticEntity entity) {
        this.entity = entity;
        if (!entity.level().isClientSide()) PolymerEntityHelper.addEntityHolderModel(this, entity);
    }

    private static Map<AquaticSpecies, Model> loadModels() {
        Map<AquaticSpecies, Model> models = new EnumMap<>(AquaticSpecies.class);
        for (AquaticSpecies species : AquaticSpecies.values()) {
            models.put(species, ModelUtil.loadBBModel(ReverieDreams.id("aquatic/" + species.key())));
        }
        return models;
    }

    @Override
    public void onCreated() {
        if (this.entity.isRemoved() || this.holder != null) return;
        this.holder = new OverlayLivingEntityHolder<>(this.entity, this, MODELS.get(this.entity.species()));
        TickHolderEntity.addTickHolder(this);
        TickHolderEntity.addElementBind(this.entity, this.holder);
        EntityAttachment.ofTicking(this.holder, this.entity);
        updateAnimation();
    }

    @Override
    public void onTick() {
        if (this.holder != null && this.entity.tickCount % 2 == 0) {
            updateAnimation();
            if (this.entity.isDeadOrDying()) this.holder.clearColor();
            else AnimationHelper.updateHurtVariant(this.entity, this.holder);
        }
    }

    private void updateAnimation() {
        String animation = this.entity.animationName();
        if (!animation.equals(this.currentAnimation)) {
            if (this.currentAnimation != null) this.holder.getAnimator().stopAnimation(this.currentAnimation);
            this.holder.getAnimator().playAnimation(animation);
            this.currentAnimation = animation;
        }
    }

    @Override public CuisineAquaticEntity getSource() { return this.entity; }
    @Override public AnimatedEntityHolder getHolder() { return this.holder; }
    @Override public EntityType<?> getPolymerEntityType(PacketContext context) { return EntityType.BLOCK_DISPLAY; }
}
