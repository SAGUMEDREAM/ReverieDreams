package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.reverie_dreams.danmaku.DanmakuTrajectory;
import cc.thonly.reverie_dreams.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.data.ModTags;
import cc.thonly.reverie_dreams.entity.misc.DanmakuEntity;
import cc.thonly.reverie_dreams.item.base.SwordItem;
import cc.thonly.reverie_dreams.registry.ItemColor;
import cc.thonly.reverie_dreams.sound.SoundEventInit;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class HakureiCane extends SwordItem {
    public static final ToolMaterial HAKUREI_CANE = new ToolMaterial(ModTags.BlockTypeTag.EMPTY, 250, 4.0f, 3.5f, 5, ItemTags.IRON_TOOL_MATERIALS);

    public HakureiCane(float attackDamage, float attackSpeed, Settings settings) {
        super(HAKUREI_CANE, attackDamage, attackSpeed, settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient() && user instanceof ServerPlayerEntity player) {
            ServerWorld serverWorld = (ServerWorld) world;
            float pitch = user.getPitch();
            float yaw = user.getYaw();

            DanmakuEntity center = DanmakuTrajectory.spawnByItemStack(serverWorld, user, user.getX(), user.getY(), user.getZ(), DanmakuTypes.withColor(DanmakuTypes.AMULET, ItemColor.RED), pitch, yaw, 1.4f, 0f, 0.0f, 0.8f);
            center.playSound(SoundEventInit.FIRE, 1.0f, 1.0f);

            ItemCooldownManager itemCooldownManager = player.getItemCooldownManager();
            itemCooldownManager.set(stack, 20 * 1);
            if (!player.isInCreativeMode()) {
                stack.damage(1, player);
            }
            return ActionResult.SUCCESS_SERVER;
        }
        return super.use(world, user, hand);
    }
}
