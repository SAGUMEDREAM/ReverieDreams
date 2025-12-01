package cc.thonly.reverie_dreams.mixin.item;

import cc.thonly.minecraft.api.ItemPostHitCallback;
import cc.thonly.reverie_dreams.data.FoodProperty;
import cc.thonly.reverie_dreams.inf.IPlayerEntity;
import cc.thonly.reverie_dreams.item.base.FoodItem;
import cc.thonly.reverie_dreams.item.base.IngredientItem;
import cc.thonly.reverie_dreams.registry.content.block.RDWoodBlocks;
import cc.thonly.reverie_dreams.registry.tag.RDItemTags;
import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Consumer;

@Mixin(Item.class)
public abstract class ItemMixin implements FeatureElement, ItemLike, FabricItem {
    @Inject(method = "hurtEnemy", at = @At("TAIL"))
    public void postHitCallback(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfo ci) {
        ItemPostHitCallback.EVENT.invoker().postHit(stack, target, attacker);
    }

    @Inject(method = "inventoryTick", at = @At("HEAD"))
    public void inventoryTick(ItemStack itemStack, ServerLevel level, Entity entity, EquipmentSlot slot, CallbackInfo ci) {
        if (entity instanceof IPlayerEntity iPlayerEntity) {
            if (itemStack.is(RDItemTags.SILVER_ITEM)) {
                iPlayerEntity.setNonSleepingTime(0);
            }
        }
    }

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    public void usePaperOnSpiritualStrippedLog(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
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
        if (blockState.is(RDWoodBlocks.SPIRITUAL.strippedLog())) {
            if (level.isClientSide) {
                cir.setReturnValue(InteractionResult.SUCCESS);
                cir.cancel();
            } else {
                level.setBlock(blockPos, RDWoodBlocks.BLESSED_SPIRITUAL_LOG.withPropertiesOf(blockState), Block.UPDATE_KNOWN_SHAPE);
                level.playSound(null, blockPos, SoundEvents.SHEARS_SNIP, SoundSource.BLOCKS, 1.0f, 1.0f);
                itemStack.consume(1, player);
                cir.setReturnValue(InteractionResult.SUCCESS_SERVER);
                cir.cancel();
            }
        }


    }

    @Inject(method = "appendHoverText", at = @At("HEAD"))
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> textConsumer, TooltipFlag type, CallbackInfo ci) {
        Item item = this.asItem();
        if (item instanceof IngredientItem || item instanceof FoodItem) {
            return;
        }
        List<FoodProperty> foodProperties = FoodProperty.getIngredientProperties(item);
        if (!foodProperties.isEmpty()) {
            textConsumer.accept(Component.empty().append(Component.translatable("item.tooltip.food_properties")));
        }
        for (FoodProperty foodProperty : foodProperties) {
            textConsumer.accept(Component.empty().append(FoodProperty.getDisplayPrefix(stack, foodProperty)).append(foodProperty.getTooltip()));
        }
    }
}
