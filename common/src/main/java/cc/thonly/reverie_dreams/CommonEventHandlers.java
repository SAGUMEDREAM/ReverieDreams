package cc.thonly.reverie_dreams;

import cc.thonly.reverie_dreams.item.prop.MusicalInstrumentItem;
import cc.thonly.reverie_dreams.item.prop.TenguCameraItem;
import cc.thonly.reverie_dreams.registry.content.RDEnchantments;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import cc.thonly.reverie_dreams.registry.content.effect.RDStatusEffects;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDIngredientItems;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.registry.tag.RDDamageTypeTags;
import cc.thonly.reverie_dreams.registry.tag.RDItemTags;
import cc.thonly.reverie_dreams.sound.RDSoundEvents;
import cc.thonly.reverie_dreams.util.NotaUtils;
import cc.thonly.reverie_dreams.util.entity.EntityUtil;
import cc.thonly.reverie_dreams.util.item.ItemUtils;
import cc.thonly.reverie_dreams.util.sound.SoundEventPlayUtils;
import dev.architectury.event.EventResult;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings({"resource", "SameReturnValue", "deprecation", "JavaExistingMethodCanBeUsed"})
public class CommonEventHandlers {
    // 银质物品对亡灵伤害
    public static EventResult onModifyingLivingEntityDamageByUndeadSilverDamage(LivingEntity entity, DamageSource damageSource, float damageAmount) {
        Entity directEntity = damageSource.getDirectEntity();
        if (!(directEntity instanceof LivingEntity attacker)) {
            return EventResult.pass();
        }
        if (attacker.level().isClientSide()) {
            return EventResult.pass();
        }
        ItemStack itemInHand = attacker.getItemInHand(InteractionHand.MAIN_HAND);
        if (!itemInHand.has(RDDataComponents.SILVER_ITEM.value())) {
            return EventResult.pass();
        }
        EntityUtil.hurt((ServerLevel) entity.level(), entity, damageSource, 2);
        return EventResult.pass();
    }

    // 银质物品对亡灵伤害
    public static boolean onPostHitBySilverWeapon(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        MinecraftServer server = target.level().getServer();
        if (server != null && target.level() instanceof ServerLevel serverWorld && stack.has(RDDataComponents.SILVER_ITEM.value())) {
            RegistryAccess.Frozen registryAccess = server.registryAccess();
            Registry<EntityType<?>> entityTypes = registryAccess.lookupOrThrow(Registries.ENTITY_TYPE);
            DamageSources damageSources = attacker.damageSources();
            for (Holder<EntityType<?>> iterateEntry : entityTypes.getTagOrEmpty(EntityTypeTags.UNDEAD)) {
                EntityType<?> value = iterateEntry.value();
                if (target.getType() == value) {
                    target.lastHurt = 0;
                    target.hurtServer(serverWorld, damageSources.magic(), 2);
                    target.lastHurt = 0;
                    break;
                }
            }

        }
        return true;
    }

    // 月伤附魔攻击效果
    public static boolean onPostHitByMoonEnchantment(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        Level world = target.level();
        if (world.isClientSide()) {
            return true;
        }
        ServerLevel level = (ServerLevel) target.level();
        RegistryAccess registryAccess = level.registryAccess();
        long overworldClockTime = level.getOverworldClockTime();
        long timeOfDay = overworldClockTime % 24000;
        boolean isNight = timeOfDay >= 13000 && timeOfDay <= 23000;
        if (!isNight) {
            return true;
        }
        if (!stack.isEmpty()) {
            Registry<Enchantment> enchantments = registryAccess.lookupOrThrow(Registries.ENCHANTMENT);
            Holder.Reference<Enchantment> moonDamage = enchantments.getOrThrow(RDEnchantments.MOON_DAMAGE);
            int itemEnchantmentLevel = EnchantmentHelper.getItemEnchantmentLevel(moonDamage, stack);
            if (itemEnchantmentLevel != 0) {
                var invulnerableTime = target.invulnerableTime;
                var hurtTime = target.hurtTime;
                var lastHurt = target.lastHurt;
                target.invulnerableTime = 0;
                target.hurtTime = 0;
                target.lastHurt = 0;
                target.hurtServer(level, attacker.damageSources().magic(), itemEnchantmentLevel);
                target.invulnerableTime = invulnerableTime;
                target.hurtTime = hurtTime;
                target.lastHurt = lastHurt;
            }
        }
        return true;
    }

    public static boolean onPostByFrozenEnchantment(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        Level world = target.level();
        if (world.isClientSide()) {
            return true;
        }
        ServerLevel level = (ServerLevel) target.level();
        RegistryAccess registryAccess = level.registryAccess();
        if (!stack.isEmpty()) {
            Registry<Enchantment> enchantments = registryAccess.lookupOrThrow(Registries.ENCHANTMENT);
            Holder.Reference<Enchantment> frozen = enchantments.getOrThrow(RDEnchantments.FROZEN);
            int itemEnchantmentLevel = EnchantmentHelper.getItemEnchantmentLevel(frozen, stack);
            boolean iceWeapon = stack.is(RDItemTags.MAGIC_ICE_TOOL_MATERIALS);
            if (iceWeapon) {
                itemEnchantmentLevel += 1;
            }
            if (itemEnchantmentLevel != 0) {
                var invulnerableTime = target.invulnerableTime;
                var hurtTime = target.hurtTime;
                var lastHurt = target.lastHurt;
                target.invulnerableTime = 0;
                target.hurtTime = 0;
                target.lastHurt = 0;
                target.setTicksFrozen(40 * itemEnchantmentLevel);
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40 * itemEnchantmentLevel, 0, false, false));
//                    target.hurtServer((ServerLevel) target.level(), attacker.damageSources().freeze(), itemEnchantmentLevel);
                target.invulnerableTime = invulnerableTime;
                target.hurtTime = hurtTime;
                target.lastHurt = lastHurt;
            }
        }
        return true;
    }

    // 冲刺附魔
    public static boolean onPostByChargeEnchantment(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        Level world = target.level();
        if (world.isClientSide()) {
            return true;
        }
        ServerLevel level = (ServerLevel) target.level();
        RegistryAccess registryAccess = level.registryAccess();
        if (!stack.isEmpty()) {
            Registry<Enchantment> enchantments = registryAccess.lookupOrThrow(Registries.ENCHANTMENT);
            Holder.Reference<Enchantment> charge = enchantments.getOrThrow(RDEnchantments.CHARGE);
            int itemEnchantmentLevel = EnchantmentHelper.getItemEnchantmentLevel(charge, stack);
            float attackStrength = attacker instanceof Player player
                    ? player.getAttackStrengthScale(0.5f)
                    : 1.0f;
            if (attackStrength < 0.9f) {
                return true;
            }
            if (stack.is(RDItems.ROKANKEN)) {
                return true;
            }
            if (itemEnchantmentLevel != 0) {
                var look = attacker.getLookAngle();

                double forwardStrength = 0.34 + 0.2 * itemEnchantmentLevel;
                double yBoost = 0.05 + 0.015 * itemEnchantmentLevel;

                attacker.push(
                        look.x * forwardStrength,
                        yBoost,
                        look.z * forwardStrength
                );

                attacker.hurtMarked = true;
                attacker.fallDistance = 0;
                SoundEventPlayUtils.playSound(level, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.NEUTRAL);
            }
        }
        return true;
    }

    // 银制品秒杀鬼魂
    public static boolean onPostHitByInstantKillGhost(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        MinecraftServer server = target.level().getServer();
        if (server != null && target.level() instanceof ServerLevel serverWorld && target.getType() == RDEntityTypes.GHOST && RDItems.ROKANKEN.is(stack.getItem().builtInRegistryHolder())) {
            DamageSources damageSources = attacker.damageSources();
            target.invulnerableTime = 0;
            target.hurtTime = 0;
            target.lastHurt = 0;
            target.hurtServer(serverWorld, damageSources.magic(), Integer.MAX_VALUE);
        }
        return true;
    }

    // 骨粉作用于睡莲
    public static InteractionResult onItemUsingByLilyPad(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            ItemStack stack = player.getItemInHand(hand);
            BlockPos pos = hitResult.getBlockPos();
            BlockState state = level.getBlockState(pos);
            Block block = state.getBlock();

            if (block instanceof LeavesBlock && state.getValue(LeavesBlock.WATERLOGGED)) {
                if (stack.getItem() == Items.LILY_PAD) {
                    stack.consume(1, player);

                    if (!player.hasInfiniteMaterials()) {
                        player.addItem(new ItemStack(RDIngredientItems.DEW.asItem(), 1));
                    }

                    player.swing(hand);

                    return InteractionResult.SUCCESS_SERVER;
                }
            }
        }

        return InteractionResult.PASS;
    }

    public static InteractionResult onAttackingBlockChangeCameraFov(Player player, Level world, InteractionHand hand, BlockPos pos, Direction direction) {
        ItemStack stack = ItemUtils.getHandItem(player, itemStack -> itemStack.getItem() instanceof TenguCameraItem);
        if (stack.isEmpty()) {
            return InteractionResult.PASS;
        }
        if (!player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (!world.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            int fov = stack.getOrDefault(RDDataComponents.FOV.value(), 75);
            int newFov = fov - 1;
            if (newFov < 30)
                newFov = 30;
            if (newFov > 110)
                newFov = 110;

            stack.set(RDDataComponents.FOV.value(), newFov);

            serverPlayer.sendSystemMessage(
                    Component.literal("§aFOV: " + newFov),
                    true
            );
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }

    // 改变乐器播放的曲子
    public static InteractionResult onChangingMusicalInstrumentMusic(Player player, Level world, InteractionHand hand, BlockPos blockPos, Direction direction) {
        if (!world.isClientSide() && world instanceof ServerLevel serverWorld && player instanceof ServerPlayer serverPlayer) {
            ItemStack stack = ItemUtils.getHandItem(player, itemStack -> itemStack.getItem() instanceof MusicalInstrumentItem);

            if (!stack.isEmpty() && !serverPlayer.isSpectator() && serverPlayer.isShiftKeyDown()) {
                List<String> fileNames = NotaUtils.getFileNames();
                if (fileNames.isEmpty()) {
                    serverPlayer.sendSystemMessage(Component.translatable("item.reverie_dreams.music.no_files"), false);
                    return InteractionResult.SUCCESS_SERVER;
                }

                String playingMusic = stack.get(RDDataComponents.PLAYING_MUSIC.value());
                int index = playingMusic == null ? -1 : fileNames.indexOf(playingMusic);
                index = (index - 1 + fileNames.size()) % fileNames.size(); // 向上翻页

                String previous = fileNames.get(index);
                stack.set(RDDataComponents.PLAYING_MUSIC.value(), previous);
                serverPlayer.sendSystemMessage(Component.translatable("item.reverie_dreams.music.switch_music", previous), false);
                if (NotaUtils.isPlaying(serverPlayer)) {
                    NoteBlockInstrument noteBlockInstrument = stack.getOrDefault(RDDataComponents.NOTE_TYPE.value(), NoteBlockInstrument.PLING);
                    NotaUtils.play(serverPlayer, previous, noteBlockInstrument);
                }
                serverPlayer.swing(hand);

                return InteractionResult.SUCCESS_SERVER;
            }
        }
        return InteractionResult.PASS;
    }

    public static EventResult onChangingMusicalInstrumentMusic(Player player, Level level, Entity entity, InteractionHand hand, @Nullable EntityHitResult result) {
        onChangingMusicalInstrumentMusic(player, player.level(), InteractionHand.MAIN_HAND, BlockPos.ZERO, player.getMotionDirection());
        return EventResult.pass();
    }

    public static EventResult onChangingMusicalInstrumentMusic(Player player, Entity target) {
        onChangingMusicalInstrumentMusic(player, player.level(), InteractionHand.MAIN_HAND, BlockPos.ZERO, player.getMotionDirection());
        return EventResult.pass();
    }

    public static EventResult onLivingEntityDeathByElixirOfLife(LivingEntity entity, DamageSource damageSource) {
        return !entity.hasEffect(RDStatusEffects.ELIXIR_OF_LIFE) ? EventResult.interruptFalse() : EventResult.pass();
    }

    public static EventResult onLivingEntityDeathByDanmaku(LivingEntity entity, DamageSource damageSource) {
        if (damageSource.is(RDDamageTypeTags.DANMAKU_HIT)) {
//            System.out.println("biu");
            SoundEventPlayUtils.playSound(entity.level(), entity.getX(), entity.getY(), entity.getZ(), RDSoundEvents.BIU.value(), SoundSource.NEUTRAL);
        }
        return EventResult.pass();
    }

}
