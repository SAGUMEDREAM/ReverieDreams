package cc.thonly.reverie_dreams.mixin.entity;

import net.minecraft.entity.Entity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Entity.class)
public abstract class EntityMixin{
    @Shadow public abstract boolean isAlive();

    @Shadow public abstract World getWorld();

    @Shadow public abstract void emitGameEvent(RegistryEntry<GameEvent> event, @Nullable Entity entity);

//    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
//    public void interact(PlayerEntity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
//        Entity entity;
//        if (this.isAlive() && (entity = (Entity) (Object)this) instanceof Leashable) {
//            Leashable leashable = (Leashable)((Object)entity);
//            System.out.println(0);
//            if (leashable.getLeashHolder() == entity) {
//                System.out.println(1);
//                if (!this.getWorld().isClient()) {
//                    System.out.println(2);
//                    if (entity.isInCreativeMode()) {
//                        System.out.println(3);
//                        leashable.detachLeashWithoutDrop();
//                    } else {
//                        System.out.println(4);
//                        leashable.detachLeash();
//                    }
//                    this.emitGameEvent(GameEvent.ENTITY_INTERACT, entity);
//                }
//                cir.setReturnValue(ActionResult.SUCCESS.noIncrementStat());
//            }
//            ItemStack itemStack = entity.getStackInHand(hand);
//            if (itemStack.isOf(ItemTypeTag.LEAD) && leashable.canLeashAttachTo()) {
//                System.out.println(5);
//                if (!this.getWorld().isClient()) {
//                    System.out.println(6);
//                    leashable.attachLeash(entity, true);
//                }
//                itemStack.decrement(1);
//                cir.setReturnValue(ActionResult.SUCCESS);
//            }
//        }
//        cir.setReturnValue(ActionResult.PASS);
//    }
}
