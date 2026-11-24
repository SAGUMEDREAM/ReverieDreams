package cc.thonly.reverie_dreams.mixin.entity;

import cc.thonly.reverie_dreams.api.entity.AnimalEntityActionController;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.UseRemainder;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import cc.thonly.reverie_dreams.entity.npc.work.NPCProduction;

import java.util.UUID;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Animal.class)
public abstract class AnimalEntityMixin extends AgeableMob implements AnimalEntityActionController, NPCProduction.AnimalSource {
    @Unique
    private NPCProduction.Source reverie_dreams$producer;

    @Override
    public NPCProduction.Source reverie_dreams$productionSource() {
        return reverie_dreams$producer;
    }

    @Override
    public void reverie_dreams$productionSource(NPCProduction.Source source) {
        reverie_dreams$producer = source;
    }

    @Inject(method = "setInLove", at = @At("HEAD"))
    private void reverie_dreams$playerFed(Player player, CallbackInfo ci) {
        reverie_dreams$producer = null;
    }

    @Inject(method = "finalizeSpawnChildFromBreeding", at = @At("HEAD"))
    private void reverie_dreams$offspring(ServerLevel level, Animal partner, AgeableMob offspring,
                                          CallbackInfo ci) {
        NPCProduction.breeding((Animal) (Object) this, partner, offspring);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void reverie_dreams$saveProducer(ValueOutput out, CallbackInfo ci) {
        if (reverie_dreams$producer != null) {
            out.putString("NPCProduction.NPC", reverie_dreams$producer.npc().toString());
            out.putString("NPCProduction.Task", reverie_dreams$producer.task().toString());
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void reverie_dreams$loadProducer(ValueInput in, CallbackInfo ci) {
        reverie_dreams$producer = null;
        try {
            String npc = in.getStringOr("NPCProduction.NPC", "");
            if (!npc.isEmpty())
                reverie_dreams$producer = new NPCProduction.Source(
                        UUID.fromString(npc), UUID.fromString(in.getStringOr("NPCProduction.Task", "")));
        } catch (IllegalArgumentException invalid) {
            reverie_dreams$producer = null;
        }
    }

    @Shadow
    private int inLove;

    protected AnimalEntityMixin(EntityType<? extends AgeableMob> entityType, Level world) {
        super(entityType, world);
    }

    @Unique
    @Override
    public void reverie_dreams$eatStackFood(LivingEntity livingEntity, InteractionHand hand, ItemStack stack) {
        int i = stack.getCount();
        UseRemainder useRemainderComponent = stack.get(DataComponents.USE_REMAINDER);
        stack.shrink(1);
        if (useRemainderComponent != null) {
            ItemStack itemStack = useRemainderComponent.convertIntoRemainder(stack, i, false, livingEntity::handleExtraItemsCreatedOnUse);
            livingEntity.setItemInHand(hand, itemStack);
        }
    }

    @Unique
    @Override
    public void reverie_dreams$loveEntity(@Nullable LivingEntity entity) {
        this.inLove = 600;
        this.level().broadcastEntityEvent(this, EntityEvent.IN_LOVE_HEARTS);
    }
}
