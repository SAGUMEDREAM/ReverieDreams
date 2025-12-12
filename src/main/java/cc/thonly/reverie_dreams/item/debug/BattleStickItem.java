package cc.thonly.reverie_dreams.item.debug;

import cc.thonly.reverie_dreams.component.BattleStickRecorder;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class BattleStickItem extends Item {
    public BattleStickItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player user, LivingEntity entity, InteractionHand hand) {
        Level world = user.level();
        if (world.isClientSide()) return InteractionResult.SUCCESS;

        BattleStickRecorder recorder = stack.getOrDefault(RDDataComponents.BATTLE_STICK_RECORDER, new BattleStickRecorder("", ""));

        String uuid = entity.getUUID().toString();
        if (recorder.getTarget_0().isEmpty()) {
            recorder.setTarget_0(uuid);
            user.displayClientMessage(Component.literal("已记录第一个目标：" + entity.getName().getString()), false);
        } else if (recorder.getTarget_1().isEmpty()) {
            recorder.setTarget_1(uuid);
            user.displayClientMessage(Component.literal("已记录第二个目标：" + entity.getName().getString()), false);
            this.apply(recorder.getTarget_0(), recorder.getTarget_1(), (ServerLevel) world);

            recorder.setTarget_0("");
            recorder.setTarget_1("");
        }

        stack.set(RDDataComponents.BATTLE_STICK_RECORDER, recorder);
        user.swing(hand);
        return InteractionResult.SUCCESS_SERVER;
    }

    public void apply(String entityUuid0, String entityUuid1, ServerLevel world) {
        LivingEntity target0 = getLivingEntityByUUID(world, entityUuid0);
        LivingEntity target1 = getLivingEntityByUUID(world, entityUuid1);

        if (target0 != null && target1 != null) {
            target0.hurtServer(world, target1.damageSources().generic(), 0);
            target1.hurtServer(world, target0.damageSources().generic(), 0);
            if (target0 instanceof Mob mob0) {
                mob0.setTarget(target1);
            }
            if (target1 instanceof Mob mob1) {
                mob1.setTarget(target0);
            }
        }
    }

    private LivingEntity getLivingEntityByUUID(ServerLevel world, String uuidStr) {
        try {
            UUID uuid = UUID.fromString(uuidStr);
            Entity entity = world.getEntity(uuid);
            if (entity instanceof Mob living) {
                return living;
            }
        } catch (IllegalArgumentException ignored) {
        }
        return null;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
