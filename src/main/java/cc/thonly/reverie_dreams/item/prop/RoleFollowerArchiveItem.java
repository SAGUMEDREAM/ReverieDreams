package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.component.ModDataComponentTypes;
import cc.thonly.reverie_dreams.component.RoleFollowerArchive;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;

import java.util.function.Consumer;

public class RoleFollowerArchiveItem extends Item {
    public static final SoundEvent SOUND = SoundEvents.ITEM_BUCKET_FILL;

    public RoleFollowerArchiveItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World contextWorld = context.getWorld();
        PlayerEntity player = context.getPlayer();
        if (!contextWorld.isClient() && contextWorld instanceof ServerWorld world && player != null) {
            ItemStack stack = context.getStack();
            RoleFollowerArchive followerArchive = stack.get(ModDataComponentTypes.ROLE_FOLLOWER_ARCHIVE);
            if (followerArchive == null) {
                return ActionResult.PASS;
            }
            boolean canRespawn = stack.getOrDefault(ModDataComponentTypes.ROLE_CAN_RESPAWN, false);
            if (!canRespawn) {
                return ActionResult.PASS;
            }
            followerArchive.respawn(world, context.getBlockPos().up(), world.getRegistryManager());
            world.playSound(null, player.getX(), player.getY(), player.getZ(), SOUND, player.getSoundCategory(), 2.0f, 1.0f);
            stack.decrementUnlessCreative(1, player);
            player.swingHand(context.getHand());
            return ActionResult.SUCCESS_SERVER;
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
//        RoleFollowerArchive archive = stack.get(ModDataComponentTypes.ROLE_FOLLOWER_ARCHIVE);
//        if (archive != null) {
//            String name = archive.getNameJson();
//            MutableText main = Text.empty();
//            Text mutableText = TextUtil.decode(name).orElse(Text.empty());
//            main.append("Name: ");
//            main.append(mutableText);
//            textConsumer.accept(main);
//        }
    }
}
