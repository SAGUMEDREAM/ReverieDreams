package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.entity.npc.NPCRoleEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;

public class CursedDecoyDollItem extends Item {
    public CursedDecoyDollItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        Hand hand = context.getHand();
        World world = context.getWorld();
        BlockPos blockPos = context.getBlockPos().up();
        if (!world.isClient && world instanceof ServerWorld serverWorld && player instanceof ServerPlayerEntity serverPlayer) {
            ItemStack stackInHand = serverPlayer.getStackInHand(hand);
            ArmorStandEntity armorStandEntity = new ArmorStandEntity(serverWorld, blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);
            armorStandEntity.setYaw(player.getYaw());
            armorStandEntity.setPitch(player.getPitch());
            serverWorld.spawnEntity(armorStandEntity);
            List<LivingEntity> list = world.getEntitiesByClass(LivingEntity.class, new Box(blockPos).expand(24), livingEntity -> true);
            for (LivingEntity livingEntity : list) {
                if (livingEntity instanceof PlayerEntity) continue;
                if (livingEntity instanceof NPCRoleEntity role) {
                    LivingEntity attacker = role.getAttacker();
                    LivingEntity target = role.getTarget();
                    if (attacker == player || target == player) {
                        continue;
                    }
                }
                if (livingEntity instanceof MobEntity mob) {
                    if (mob.getTarget() != null) {
                        mob.setTarget(armorStandEntity);
                    }
                }
                if (livingEntity.getAttacker() != null) {
                    livingEntity.setAttacker(armorStandEntity);
                }
            }
            stackInHand.decrementUnlessCreative(1, serverPlayer);
            return ActionResult.SUCCESS_SERVER;
        }
        return ActionResult.SUCCESS;
    }
}
