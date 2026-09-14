package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.reverie_dreams.data.danmaku.DanmakuTrajectory;
import cc.thonly.reverie_dreams.entity.misc.DanmakuEntity;
import cc.thonly.reverie_dreams.item.base.SwordItem;
import cc.thonly.reverie_dreams.registry.content.ItemColor;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.registry.tag.RDBlockTags;
import cc.thonly.reverie_dreams.server.DelayedTask;
import cc.thonly.reverie_dreams.sound.RDSoundEvents;
import cc.thonly.reverie_dreams.util.sound.SoundEventPlayUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.function.Supplier;

public class HakureiCane extends SwordItem {
    public static final ToolMaterial HAKUREI_CANE = new ToolMaterial(RDBlockTags.EMPTY, 250, 4.0f, 3.5f, 5, ItemTags.IRON_TOOL_MATERIALS);

    public HakureiCane(float attackDamage, float attackSpeed, Properties settings) {
        super(HAKUREI_CANE, attackDamage, attackSpeed, settings);
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        if (!world.isClientSide() && user instanceof ServerPlayer player) {
            ServerLevel serverWorld = (ServerLevel) world;
            float pitch = user.getXRot();
            float yaw = user.getYRot();

            Vec3 look = user.getLookAngle();
            Vec3 right = look.cross(new Vec3(0, 1, 0)).normalize();

            double offset = 1;
            Runnable factory = () -> {
                spawnDanmaku(serverWorld, user, 0, look, pitch, yaw);
                spawnDanmaku(serverWorld, user, -offset, right, pitch, yaw + 1);
                spawnDanmaku(serverWorld, user, offset, right, pitch, yaw - 1);
                SoundEventPlayUtils.playSound(user, RDSoundEvents.FIRE.value(), SoundSource.NEUTRAL, 1.0f, 1.0f);
            };
            factory.run();
            DelayedTask.createFromSecond(serverWorld.getServer(), 0.2f, ()->{
                factory.run();
                DelayedTask.createFromSecond(serverWorld.getServer(), 0.3f, factory);
            });

            ItemCooldowns itemCooldownManager = player.getCooldowns();
            itemCooldownManager.addCooldown(stack, 5);
            if (!player.hasInfiniteMaterials()) {
                stack.hurtWithoutBreaking(1, player);
            }
            return InteractionResult.SUCCESS_SERVER;
        }
        return super.use(world, user, hand);
    }

    private void spawnDanmaku(ServerLevel world, Player user, double sideOffset, Vec3 right, float pitch, float yaw) {
        Vec3 pos = user.position().add(right.scale(sideOffset));

        DanmakuEntity danmaku = DanmakuTrajectory.spawnByItemStack(
                world,
                user,
                pos.x, pos.y + 1.5, pos.z,
                DanmakuTypes.withColor(DanmakuTypes.AMULET, ItemColor.RED).create(),
                pitch, yaw,
                0.0f, 0.8f
        );

        danmaku.setDanmakuProperties(
                danmaku.getDanmakuProperties().withSpeed(3.4f)
        );
    }
}
