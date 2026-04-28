package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.entity.villager.FumoSeller;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FumoLicenseItem extends Item {
    public FumoLicenseItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player user, LivingEntity entity, InteractionHand hand) {
        Level world = user.level();
        if (world.isClientSide()) return InteractionResult.SUCCESS;

        if (entity instanceof Villager villager) {
            BlockPos blockPos = entity.blockPosition();
            Vec3 pos = villager.position();
            Component name = villager.getName();
            boolean hasCN = villager.hasCustomName();
            villager.discard();
            FumoSeller sellerVillager = new FumoSeller(villager.getVillagerData(), world);
            sellerVillager.setPosRaw(pos.x(), pos.y(), pos.z());
            if (hasCN) {
                sellerVillager.setCustomName(name);
            }
            world.addFreshEntity(sellerVillager);
            world.playSound(null, blockPos, SoundEvents.ANVIL_FALL, SoundSource.PLAYERS);

            stack.consume(1, user);
            user.swing(hand);

            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.FAIL;
    }

//    @Override
//    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
//        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
//        textConsumer.accept(Text.translatable("item.tooltip.use.villager"));
//    }
}
