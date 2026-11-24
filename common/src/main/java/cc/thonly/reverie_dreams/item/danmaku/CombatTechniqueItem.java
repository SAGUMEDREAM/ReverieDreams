package cc.thonly.reverie_dreams.item.danmaku;

import cc.thonly.reverie_dreams.data.danmaku.stg.runtime.DanmakuPatternRuntime;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuDefaults;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.registry.content.danmaku.soku.SokuMoves;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * A repeatable ordinary move using the shared pattern executor and per-stack cooldown.
 */
public final class CombatTechniqueItem extends Item {
    public CombatTechniqueItem(Properties properties) {
        super(properties);
    }

    public static ItemStack createTechnique(Identifier id) {
        var move = SokuMoves.find(id);
        if (move == null) {
            throw new IllegalArgumentException("Unknown combat technique: " + id);
        }
        ItemStack stack = RDItems.COMBAT_TECHNIQUE.createStack();
        stack.set(RDDataComponentTypes.DANMAKU_PATTERN.value(), id);
        stack.set(DataComponents.LORE, new ItemLore(List.of(
                Component.translatable(move.characterKey()),
                Component.literal(move.game().toUpperCase(java.util.Locale.ROOT) + " · " + move.action()))));
        return stack;
    }

    @Override
    public Component getName(ItemStack stack) {
        var move = SokuMoves.find(stack.get(RDDataComponentTypes.DANMAKU_PATTERN.value()));
        return move == null ? super.getName(stack) : Component.translatableWithFallback(
                "item.reverie_dreams.combat_technique.named", "%s · %s",
                Component.translatable(move.characterKey()), DanmakuDefaults.displayName(move.id()));
    }

    private InteractionResult cast(Level level, Player player, InteractionHand hand) {
        if (player == null) {
            return InteractionResult.FAIL;
        }
        ItemStack stack = player.getItemInHand(hand);
        Identifier id = stack.get(RDDataComponentTypes.DANMAKU_PATTERN.value());
        if (SokuMoves.find(id) == null) {
            return InteractionResult.PASS;
        }
        if (!(level instanceof ServerLevel world)) {
            return InteractionResult.SUCCESS;
        }
        SpellcardCooldown.ensureGroup(stack);
        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }
        DanmakuPatternRuntime runtime = DanmakuPatternRuntime.castFromFacing(world, player, id);
        if (runtime == null) {
            return InteractionResult.FAIL;
        }
        player.getCooldowns().addCooldown(stack, Math.max(1, runtime.getPattern().timing().duration()));
        world.playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP,
                player.getSoundSource(), 0.6f, 1f);
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        return this.cast(level, player, hand);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return this.cast(context.getLevel(), context.getPlayer(), context.getHand());
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        return this.cast(player.level(), player, hand);
    }
}
