package cc.thonly.reverie_dreams.item.danmaku;

import cc.thonly.reverie_dreams.data.danmaku.spellcard.LegacySpellcardDefinition;
import cc.thonly.reverie_dreams.data.danmaku.spellcard.LegacySpellcardRuntime;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuDefaults;
import net.minecraft.network.chat.Component;
import cc.thonly.reverie_dreams.data.danmaku.stg.runtime.DanmakuPatternRuntime;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.registry.content.sound.RDSoundEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class SpellcardItem extends Item {
    public SpellcardItem(Properties properties) {
        super(properties);
    }

    /**
     * 按难度创建符卡物品，保留物品组件供玩家再次染色。
     */
    public static ItemStack createCard(Identifier patternId) {
        ItemStack stack = RDItems.SPELLCARD.createStack();
        stack.set(RDDataComponentTypes.DANMAKU_PATTERN.value(), patternId);
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(DanmakuDefaults.difficultyColor(patternId)));
        return stack;
    }

    @Override
    public Component getName(ItemStack stack) {
        Identifier id = stack.get(RDDataComponentTypes.DANMAKU_PATTERN.value());
        return id == null ? super.getName(stack) : DanmakuDefaults.itemName(id);
    }

    private InteractionResult spell(Level level, Player player, InteractionHand hand) {
        if (player == null) {
            return InteractionResult.FAIL;
        }
        ItemCooldowns cooldowns = player.getCooldowns();
        if (level instanceof ServerLevel world) {
            ItemStack itemStack = player.getItemInHand(hand);
            // 耐久耗尽的符卡保留为收藏品：阻止施放但不消失。
            if (itemStack.getDamageValue() >= itemStack.getMaxDamage()) {
                return InteractionResult.FAIL;
            }
            SpellcardCooldown.ensureGroup(itemStack);
            if (cooldowns.isOnCooldown(itemStack)) {
                return InteractionResult.FAIL;
            }

            // 新 STG 引擎：danmaku_pattern 组件指向图案注册表
            Identifier patternId = itemStack.get(RDDataComponentTypes.DANMAKU_PATTERN.value());
            if (patternId != null) {
                DanmakuPatternRuntime runtime = DanmakuPatternRuntime.cast(world, player, patternId);
                if (runtime == null) {
                    return InteractionResult.FAIL;
                }
                cooldowns.addCooldown(itemStack, Math.max(40, runtime.getPattern().timing().duration()));
                damageSpellcard(player, itemStack);
                world.playSound(null, player.getX(), player.getEyeY(), player.getZ(), RDSoundEvents.SPELL_CARD, player.getSoundSource(), 1.0f, 1.0f);
                return InteractionResult.SUCCESS_SERVER;
            }

            // 旧版符卡：保留兼容
            LegacySpellcardDefinition definition = itemStack.get(RDDataComponentTypes.SPELL_CARD_COMPONENT.value());
            if (definition == null) {
                return InteractionResult.PASS;
            }
            LegacySpellcardRuntime.startFromPlayer(definition, world, player);
            cooldowns.addCooldown(itemStack, 40);
            damageSpellcard(player, itemStack);
            world.playSound(null, player.getX(), player.getEyeY(), player.getZ(), RDSoundEvents.SPELL_CARD, player.getSoundSource(), 1.0f, 1.0f);
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }

    private static void damageSpellcard(Player player, ItemStack stack) {
        if (player.hasInfiniteMaterials()) {
            return;
        }
        // 用尽后保留为收藏品：只累加伤害值，不走 hurtAndBreak 的碎裂路径。
        if (stack.getDamageValue() < stack.getMaxDamage()) {
            stack.setDamageValue(stack.getDamageValue() + 1);
        }
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand interactionHand) {
        super.use(level, player, interactionHand);
        return this.spell(level, player, interactionHand);
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        super.useOn(useOnContext);
        return this.spell(useOnContext.getLevel(), useOnContext.getPlayer(), useOnContext.getHand());
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity livingEntity, InteractionHand interactionHand) {
        super.interactLivingEntity(itemStack, player, livingEntity, interactionHand);
        return this.spell(player.level(), player, interactionHand);
    }
}
