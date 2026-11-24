package cc.thonly.reverie_dreams.mixin.entity;

import cc.thonly.reverie_dreams.api.entity.YoukaiFeedingSource;
import cc.thonly.reverie_dreams.entity.ai.YoukaiFeeding;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ItemEntity.class)
public abstract class YoukaiFeedingItemMixin implements YoukaiFeedingSource {
    @Unique
    private @Nullable UUID reverie_dreams$youkaiFeeder;

    @Override
    public @Nullable UUID reverie_dreams$getYoukaiFeeder() {
        return this.reverie_dreams$youkaiFeeder;
    }

    @Override
    public void reverie_dreams$setYoukaiFeeder(@Nullable UUID id) {
        this.reverie_dreams$youkaiFeeder = id;
    }

    @Inject(method = "tryToMerge", at = @At("HEAD"), cancellable = true)
    private void reverie_dreams$separateFeeding(ItemEntity other, CallbackInfo ci) {
        if (!YoukaiFeeding.mayMerge(this.reverie_dreams$youkaiFeeder, YoukaiFeeding.feeder(other)))
            ci.cancel();
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void reverie_dreams$saveFeeding(ValueOutput output, CallbackInfo ci) {
        output.storeNullable("ReverieYoukaiFeeder", UUIDUtil.CODEC, this.reverie_dreams$youkaiFeeder);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void reverie_dreams$loadFeeding(ValueInput input, CallbackInfo ci) {
        this.reverie_dreams$youkaiFeeder = input.read("ReverieYoukaiFeeder", UUIDUtil.CODEC).orElse(null);
    }

    @Inject(method = "restoreFrom", at = @At("TAIL"))
    private void reverie_dreams$transferFeeding(Entity old, CallbackInfo ci) {
        this.reverie_dreams$youkaiFeeder = old instanceof YoukaiFeedingSource source ? source.reverie_dreams$getYoukaiFeeder() : null;
    }
}
