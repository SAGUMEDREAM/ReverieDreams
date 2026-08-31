package cc.thonly.reverie_dreams.mixin.item;

import cc.thonly.reverie_dreams.CommonEventHandlers;
import cc.thonly.reverie_dreams.api.entity.PlayerEntityDataModifier;
import cc.thonly.reverie_dreams.data.BeverageProperty;
import cc.thonly.reverie_dreams.data.FoodProperty;
import cc.thonly.reverie_dreams.registry.content.BeverageProperties;
import cc.thonly.reverie_dreams.registry.content.FoodProperties;
import cc.thonly.reverie_dreams.registry.content.RDEnchantments;
import cc.thonly.reverie_dreams.registry.content.advancements.RDCriteriaTriggers;
import cc.thonly.reverie_dreams.registry.content.block.RDWoodBlocks;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.registry.tag.RDItemTags;
import cc.thonly.reverie_dreams.util.advancements.SimpleTriggerFactory;
import cc.thonly.reverie_dreams.util.advancements.SimpleTriggerKeys;
import cc.thonly.reverie_dreams.util.entity.EntityHelper;
import cc.thonly.reverie_dreams.util.item.ItemUtils;
import com.google.common.base.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.List;

@Mixin(Item.class)
public abstract class ItemMixin implements FeatureElement, ItemLike {

    @Shadow
    public abstract InteractionResult use(Level level, Player player, InteractionHand hand);

    @Inject(method = "<init>", at = @At("RETURN"))
    public void reverie_dreams$onInit(Item.Properties properties, CallbackInfo ci) {

    }

    @Inject(method = "finishUsingItem", at = @At("HEAD"), cancellable = true)
    public void reverie_dreams$finishUsingItem(ItemStack itemStack, Level level, LivingEntity livingEntity, CallbackInfoReturnable<ItemStack> cir) {
        if (!level.isClientSide() && itemStack.isEnchanted()) {
            RegistryAccess registryAccess = level.registryAccess();
            Registry<Enchantment> enchantmentAccess = registryAccess.lookupOrThrow(Registries.ENCHANTMENT);
            Holder.Reference<Enchantment> powerful = enchantmentAccess.getOrThrow(RDEnchantments.POWERFUL);
            ItemEnchantments enchantments = itemStack.getEnchantments();
            int enchantLevel = enchantments.getLevel(powerful);
            if (enchantLevel >= 1) {
                livingEntity.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 20 * 20));
                livingEntity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 20));
                if (livingEntity instanceof ServerPlayer serverPlayer) {
                    SimpleTriggerFactory.create(SimpleTriggerKeys.EAT_PEACH).trigger(serverPlayer);
                }
            }
        }
    }

    @Inject(method = "finishUsingItem", at = @At("HEAD"), cancellable = true)
    public void reverie_dreams$finishEatFood(ItemStack itemStack, Level level, LivingEntity entity, CallbackInfoReturnable<ItemStack> cir) {
        if (level.isClientSide()) {
            return;
        }
        CommonEventHandlers.onFinishUseItem(itemStack, level, entity, cir);
    }

    @Inject(method = "use", at = @At("RETURN"))
    public void reverie_dreams$advancementUseItem(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (!level.isClientSide() && !itemStack.isEmpty() && !Objects.equal(cir.getReturnValue(), InteractionResult.FAIL)) {
            RDCriteriaTriggers.USE_ITEM.value().trigger((ServerPlayer) player, itemStack);
        }
    }

    @Inject(method = "inventoryTick", at = @At("HEAD"))
    public void reverie_dreams$inventoryTick(ItemStack itemStack, ServerLevel level, Entity entity, EquipmentSlot slot, CallbackInfo ci) {
        if (entity instanceof PlayerEntityDataModifier playerEntityDataModifier) {
            if (itemStack.is(RDItemTags.SILVER_ITEM)) {
                playerEntityDataModifier.reverie_dreams$setNonSleepingTime(0);
            }
        }
        ItemUtils.updateItemStackTag(itemStack);
    }

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    public void reverie_dreams$usePaperOnSpiritualStrippedLog(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        Player player = context.getPlayer();
        InteractionHand hand = context.getHand();
        Level level = context.getLevel();
        if (player == null) {
            return;
        }
        ItemStack itemStack = player.getItemInHand(hand);
        if (!itemStack.is(Items.PAPER)) {
            return;
        }

        BlockPos blockPos = context.getClickedPos();
        BlockState blockState = level.getBlockState(blockPos);
        if (blockState.is(RDWoodBlocks.SPIRITUAL_BUNDLE.strippedLog())) {
            if (level.isClientSide()) {
                cir.setReturnValue(InteractionResult.SUCCESS);
                cir.cancel();
            } else {
                level.setBlock(blockPos, RDWoodBlocks.BLESSED_SPIRITUAL_LOG.value().withPropertiesOf(blockState), Block.UPDATE_KNOWN_SHAPE);
                level.playSound(null, blockPos, SoundEvents.SHEARS_SNIP, SoundSource.BLOCKS, 1.0f, 1.0f);
                itemStack.consume(1, player);
                cir.setReturnValue(InteractionResult.SUCCESS_SERVER);
                cir.cancel();
            }
        }
    }
}
