package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.entity.npc.NPCRoleEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class CursedDecoyDollItem extends Item {
    public CursedDecoyDollItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        InteractionHand hand = context.getHand();
        Level world = context.getLevel();
        BlockPos blockPos = context.getClickedPos().above();
        if (!world.isClientSide && world instanceof ServerLevel serverWorld && player instanceof ServerPlayer serverPlayer) {
            ItemStack stackInHand = serverPlayer.getItemInHand(hand);
            ArmorStand armorStandEntity = new ArmorStand(serverWorld, blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);
            armorStandEntity.setYRot(player.getYRot());
            armorStandEntity.setXRot(player.getXRot());
            serverWorld.addFreshEntity(armorStandEntity);
            List<LivingEntity> list = world.getEntitiesOfClass(LivingEntity.class, new AABB(blockPos).inflate(24), livingEntity -> true);
            for (LivingEntity livingEntity : list) {
                if (livingEntity instanceof Player) continue;
                if (livingEntity instanceof NPCRoleEntity role) {
                    LivingEntity attacker = role.getLastHurtByMob();
                    LivingEntity target = role.getTarget();
                    if (attacker == player || target == player) {
                        continue;
                    }
                }
                if (livingEntity instanceof Mob mob) {
                    if (mob.getTarget() != null) {
                        mob.setTarget(armorStandEntity);
                    }
                }
                if (livingEntity.getLastHurtByMob() != null) {
                    livingEntity.setLastHurtByMob(armorStandEntity);
                }
            }
            stackInHand.consume(1, serverPlayer);
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }
}
