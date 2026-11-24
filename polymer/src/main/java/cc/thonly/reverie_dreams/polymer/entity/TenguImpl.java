package cc.thonly.reverie_dreams.polymer.entity;

import cc.thonly.reverie_dreams.entity.Tengu;
import cc.thonly.reverie_dreams.entity.variant.TenguAppearance;
import cc.thonly.reverie_dreams.entity.variant.TenguEquipment;
import cc.thonly.reverie_dreams.ReverieDreams;
import net.minecraft.core.component.DataComponents;
import cc.thonly.reverie_dreams.fabric.util.AnimationHelper;
import cc.thonly.reverie_dreams.polymer.entity.holder.EquippedHumanoidHolder;
import cc.thonly.reverie_dreams.polymer.entity.bridge.PolymerHolderEntity;
import cc.thonly.reverie_dreams.polymer.entity.bridge.TickHolderEntity;
import cc.thonly.reverie_dreams.polymer.helper.PolymerEntityHelper;
import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.api.AnimatedEntityHolder;
import de.tomalbrc.bil.core.model.Model;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import org.joml.Matrix4f;

public final class TenguImpl implements AnimatedEntity,PolymerHolderEntity,TickHolderEntity {
    private final Tengu source;
    private EquippedHumanoidHolder<Tengu,TenguImpl> holder;
    private TenguAppearance appearance;
    private Model model;
    private String animation;
    private long revision=-1;
    private int hurt;
    private boolean walkingLegs;
    public TenguImpl(Tengu source){this.source=source;if(!source.level().isClientSide())PolymerEntityHelper.addEntityHolderModel(this,source);}
    @Override public void onCreated(){
        if(this.source.isRemoved()||this.holder!=null)return;this.createHolder();TickHolderEntity.addTickHolder(this);this.updateAnimation();
    }
    private void createHolder(){
        this.appearance=this.source.appearance();
        this.model=PolymerEntityHelper.getTenguModel(this.appearance);
        this.holder=new EquippedHumanoidHolder<>(this.source,this,this.model){
            @Override protected ItemStack handRenderStack(ItemStack stack,HumanoidArm arm){
                if(!TenguEquipment.customShield(stack))return super.handRenderStack(stack,arm);
                var copy=stack.copy();copy.set(DataComponents.ITEM_MODEL,ReverieDreams.id("tengu_equipment/shield"+(TenguEquipment.usingShield(source,arm)?"_blocking":"")));return copy;
            }
            @Override protected Matrix4f transformHand(Matrix4f transform,ItemStack stack,HumanoidArm arm){
                if(TenguEquipment.customShield(stack))return TenguEquipment.shieldHandTransform(transform,arm,TenguEquipment.usingShield(source,arm));
                super.transformHand(transform,stack,arm);
                if(stack.getItem() instanceof ShieldItem){
                    transform.translate(0,.125F,arm==HumanoidArm.RIGHT?-.25F:.25F);
                    if(arm==HumanoidArm.LEFT)transform.rotateY((float)Math.PI);
                }
                return transform;
            }
        };
        TickHolderEntity.addElementBind(this.source,this.holder);EntityAttachment.ofTicking(this.holder,this.source);this.animation=null;this.walkingLegs=false;
    }
    @Override public void onTick(){
        if(this.holder==null)return;
        if(this.appearance!=this.source.appearance()){this.holder.destroy();this.createHolder();}
        this.updateAnimation();AnimationHelper.updateHurtVariant(this.source,this.holder);
    }
    private void updateAnimation() {
        var animator = this.holder.getAnimator();
        String legClip = Tengu.WALKING_LEGS_CLIP;
        boolean legs = this.source.hasWalkingLegAnimation() && this.model.animations().containsKey(legClip);
        if (legs != this.walkingLegs) {
            if (legs) {
                animator.playAnimation(legClip, 10);
            } else {
                animator.stopAnimation(legClip);
            }
            this.walkingLegs = legs;
        }
        if (legs) animator.setAnimationFrame(legClip, (int)(this.source.walkingLegPhase(1) * 20));

        String requested = this.source.modelAnimation();
        String next = this.availableAnimation(requested);
        boolean repeat = this.source.restartsModelAnimation(requested, this.revision, this.hurt);
        this.revision = this.source.actionRevision();
        this.hurt = this.source.hurtTime;
        String clip = "animation." + next;
        // BIL samples during its own tick; paused animations neither update nor affect bones.
        double time = this.source.modelAnimationTime(next, next.equals("walk") ? 1 : 0);
        if (!next.equals(this.animation) || repeat) {
            if (this.animation != null) animator.stopAnimation("animation." + this.animation);
            animator.playAnimation(clip);
            this.animation = next;
        }
        if (time >= 0) {
            int lastFrame = Math.max(0, this.model.animations().get(clip).duration() - 1);
            // Use the completed motion sample, but keep one-shots on their server tick.
            int frame = (int)(time * 20 + 1e-6);
            animator.setAnimationFrame(clip, Math.clamp(frame, 0, lastFrame));
        }
    }

    private String availableAnimation(String requested) {
        if (this.model.animations().containsKey("animation." + requested)) return requested;
        return switch (requested) {
            case "takeoff", "glide" -> "fly";
            case "land" -> "idle";
            case "photo_air" -> "photo";
            default -> "idle";
        };
    }
    @Override public Tengu getSource(){return this.source;}
    @Override public AnimatedEntityHolder getHolder(){return this.holder;}
    @Override public EntityType<?> getPolymerEntityType(PacketContext context){return EntityType.BLOCK_DISPLAY;}
}
