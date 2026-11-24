package cc.thonly.reverie_dreams.item.debug;

import cc.thonly.reverie_dreams.component.BattleStickRecorder;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import dev.architectury.event.EventResult;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.UUID;

public class BattleStickItem extends Item {
    public BattleStickItem(Properties settings) {
        super(settings);
    }

    public static EventResult onInteractEntity(Player player, Entity target, InteractionHand hand) {
        if (player.isSpectator() || !(target instanceof LivingEntity living) || !living.isAlive()) {
            return EventResult.pass();
        }
        ItemStack heldStack = player.getItemInHand(hand);
        if (!(heldStack.getItem() instanceof BattleStickItem stick)) {
            return EventResult.pass();
        }

        // Handle the live stack before the mob interacts or vanilla substitutes a creative-mode copy.
        if (!player.level().isClientSide()) {
            stick.interactLivingEntity(heldStack, player, living, hand);
            player.level().gameEvent(GameEvent.ENTITY_INTERACT, living.position(), GameEvent.Context.of(player));
        }
        return EventResult.interruptTrue();
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player user, LivingEntity entity, InteractionHand hand) {
        Level world = user.level();
        if (world.isClientSide())
            return InteractionResult.SUCCESS;
        if (!(user instanceof ServerPlayer serverPlayer))
            return InteractionResult.SUCCESS;

        BattleStickRecorder recorder = stack.getOrDefault(RDDataComponentTypes.BATTLE_STICK_RECORDER.value(), BattleStickRecorder.empty());
        String uuid = entity.getUUID().toString();

        if (recorder.target_0().isEmpty()) {
            // 注意：必须把更新后的记录器写回物品，否则下一次交互读到的还是空记录，
            // 会永远停在“记录第一个目标”这一步。
            stack.set(RDDataComponentTypes.BATTLE_STICK_RECORDER.value(), new BattleStickRecorder(uuid, ""));
            serverPlayer.sendSystemMessage(Component.literal("已记录第一个目标：" + entity.getName().getString()), false);
        } else if (recorder.target_1().isEmpty()) {
            if (recorder.target_0().equals(uuid)) {
                serverPlayer.sendSystemMessage(Component.literal("§c不能把同一个实体设为目标"), false);
                return InteractionResult.SUCCESS_SERVER;
            }

            BattleStickRecorder filled = new BattleStickRecorder(recorder.target_0(), uuid);
            boolean applied = this.apply(filled.target_0(), filled.target_1(), (ServerLevel) world);
            stack.set(RDDataComponentTypes.BATTLE_STICK_RECORDER.value(), BattleStickRecorder.empty());

            if (applied) {
                serverPlayer.sendSystemMessage(Component.literal("§a已记录第二个目标并让双方互相敌对"), false);
            } else {
                serverPlayer.sendSystemMessage(Component.literal("§c目标不在当前世界或已失效，记录已重置"), false);
            }
        } else {
            // 数据异常时重新开始记录
            stack.set(RDDataComponentTypes.BATTLE_STICK_RECORDER.value(), new BattleStickRecorder(uuid, ""));
            serverPlayer.sendSystemMessage(Component.literal("已重新记录第一个目标：" + entity.getName().getString()), false);
        }

        return InteractionResult.SUCCESS_SERVER;
    }

    public boolean apply(String entityUuid0, String entityUuid1, ServerLevel world) {
        LivingEntity target0 = getLivingEntityByUUID(world, entityUuid0);
        LivingEntity target1 = getLivingEntityByUUID(world, entityUuid1);

        if (target0 == null || target1 == null || target0 == target1) {
            return false;
        }
        if (!target0.isAlive() || !target1.isAlive()) {
            return false;
        }

        // 直接写入“最近攻击/被攻击”记录，不造成 0 点伤害，
        // 避免伤害事件、受击动画和无敌帧干扰真正的战斗。
        target0.setLastHurtByMob(target1);
        target1.setLastHurtByMob(target0);
        target0.setLastHurtMob(target1);
        target1.setLastHurtMob(target0);

        if (target0 instanceof Mob mob0) {
            mob0.setTarget(target1);
        }
        if (target1 instanceof Mob mob1) {
            mob1.setTarget(target0);
        }
        return true;
    }

    private LivingEntity getLivingEntityByUUID(ServerLevel world, String uuidStr) {
        if (uuidStr == null || uuidStr.isEmpty()) {
            return null;
        }
        try {
            UUID uuid = UUID.fromString(uuidStr);
            Entity entity = world.getEntity(uuid);
            // 这里的目标可能是玩家/动物/NPC，不一定都是 Mob
            if (entity instanceof LivingEntity living) {
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
