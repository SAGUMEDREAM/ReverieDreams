package cc.thonly.reverie_dreams.item.prop.debug;

import cc.thonly.reverie_dreams.component.BattleStickRecorder;
import cc.thonly.reverie_dreams.component.ModDataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.UUID;

public class BattleStickItem extends Item {
    public BattleStickItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        World world = user.getWorld();
        if (world.isClient()) return ActionResult.SUCCESS;

        BattleStickRecorder recorder = stack.getOrDefault(ModDataComponentTypes.BATTLE_STICK_RECORDER, new BattleStickRecorder("", ""));

        String uuid = entity.getUuid().toString();
        if (recorder.getTarget_0().isEmpty()) {
            recorder.setTarget_0(uuid);
            user.sendMessage(Text.literal("已记录第一个目标：" + entity.getName().getString()), false);
        } else if (recorder.getTarget_1().isEmpty()) {
            recorder.setTarget_1(uuid);
            user.sendMessage(Text.literal("已记录第二个目标：" + entity.getName().getString()), false);
            this.apply(recorder.getTarget_0(), recorder.getTarget_1(), (ServerWorld) world);

            recorder.setTarget_0("");
            recorder.setTarget_1("");
        }

        stack.set(ModDataComponentTypes.BATTLE_STICK_RECORDER, recorder);
        user.swingHand(hand);
        return ActionResult.SUCCESS_SERVER;
    }

    public void apply(String entityUuid0, String entityUuid1, ServerWorld world) {
        LivingEntity target0 = getLivingEntityByUUID(world, entityUuid0);
        LivingEntity target1 = getLivingEntityByUUID(world, entityUuid1);

        if (target0 != null && target1 != null) {
            target0.damage(world, target1.getDamageSources().generic(), 0);
            target1.damage(world, target0.getDamageSources().generic(), 0);
            if (target0 instanceof MobEntity mob0) {
                mob0.setTarget(target1);
            }
            if (target1 instanceof MobEntity mob1) {
                mob1.setTarget(target0);
            }
        }
    }

    private LivingEntity getLivingEntityByUUID(ServerWorld world, String uuidStr) {
        try {
            UUID uuid = UUID.fromString(uuidStr);
            Entity entity = world.getEntity(uuid);
            if (entity instanceof MobEntity living) {
                return living;
            }
        } catch (IllegalArgumentException ignored) {
        }
        return null;
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }
}
