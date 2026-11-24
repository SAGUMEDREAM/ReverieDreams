package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import cc.thonly.reverie_dreams.item.base.SwordItem;
import cc.thonly.reverie_dreams.registry.content.ItemPropDescriptionConsts;
import cc.thonly.reverie_dreams.registry.tag.RDBlockTags;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;


@SuppressWarnings("resource")
public class Levatin extends SwordItem {
    public static final ToolMaterial LEVATIN = new ToolMaterial(RDBlockTags.EMPTY, 1200, 7.5f, 4.0f, 12, ItemTags.NETHERITE_TOOL_MATERIALS);
    public static final Identifier STAFF_MODEL = Identifier.fromNamespaceAndPath("reverie_dreams", "levatin");
    public static final Identifier FLAME_MODEL = Identifier.fromNamespaceAndPath("reverie_dreams", "levatin_flame");
    public static final int FIRE_TICK = 20;
    public static final int FORM_SWITCH_COOLDOWN = 10;
    public static final int FLAME_DURATION_TICKS = 60 * 20;
    private static final Identifier POWER_BLOCK = Identifier.fromNamespaceAndPath("reverie_dreams", "power_block");
    private static final Identifier POWER = Identifier.fromNamespaceAndPath("reverie_dreams", "power");
    private static final int POWER_COST = 9;
    private static final String FLAME_UNTIL = "reverie_dreams:levatin_flame_until";


    public Levatin(float attackDamage, float attackSpeed, Properties settings) {
        super(LEVATIN, attackDamage, attackSpeed, settings);
    }

    // Vanilla persists and synchronizes ITEM_MODEL; Polymer reads the same component.
    public static boolean isFlameForm(ItemStack stack) {
        return FLAME_MODEL.equals(stack.get(DataComponents.ITEM_MODEL));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        // A vanilla Polymer client can send both block-use and air-use for one click.
        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.SUCCESS_SERVER;
        }

        if (expireFlameForm(stack, level, player)) {
            player.getCooldowns().addCooldown(stack, FORM_SWITCH_COOLDOWN);
            return InteractionResult.SUCCESS_SERVER;
        }
        if (isFlameForm(stack)) {
            fold(stack);
        } else {
            if (!player.isCreative() && !consumeFuel(player)) {
                player.sendOverlayMessage(Component.translatable(ItemPropDescriptionConsts.LEVATIN_TOGGLE_HINT));
                return InteractionResult.SUCCESS_SERVER;
            }
            long expiresAt = level.getGameTime() + FLAME_DURATION_TICKS;
            CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putLong(FLAME_UNTIL, expiresAt));
            stack.set(DataComponents.ITEM_MODEL, FLAME_MODEL);
        }
        player.getCooldowns().addCooldown(stack, FORM_SWITCH_COOLDOWN);
        playFormSound(level, player, isFlameForm(stack));
        player.sendOverlayMessage(formDescription(stack));
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        return player == null ? InteractionResult.PASS : use(context.getLevel(), player, context.getHand());
    }

    public static MutableComponent formDescription(ItemStack stack) {
        return Component.translatable("item.reverie_dreams.levatin.form",
                Component.translatable("item.reverie_dreams.levatin.form." + (isFlameForm(stack) ? "flame" : "staff")));
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        expireFlameForm(stack, level, entity);
    }

    private static boolean consumeFuel(Player player) {
        var inventory = player.getInventory();
        int available = 0;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(item -> item.is(POWER_BLOCK))) {
                return !inventory.removeItem(slot, 1).isEmpty();
            }
            if (stack.is(item -> item.is(POWER))) {
                available += stack.getCount();
            }
        }
        if (available < POWER_COST)
            return false;

        int remaining = POWER_COST;
        for (int slot = 0; remaining > 0 && slot < inventory.getContainerSize(); slot++) {
            if (inventory.getItem(slot).is(item -> item.is(POWER))) {
                remaining -= inventory.removeItem(slot, remaining).getCount();
            }
        }
        return true;
    }

    private static boolean expireFlameForm(ItemStack stack, Level level, Entity holder) {
        if (!isFlameForm(stack) || stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                .copyTag().getLongOr(FLAME_UNTIL, 0) > level.getGameTime()) {
            return false;
        }

        fold(stack);
        playFormSound(level, holder, false);
        if (holder instanceof Player player && (player.getItemInHand(InteractionHand.MAIN_HAND) == stack
                || player.getItemInHand(InteractionHand.OFF_HAND) == stack)) {
            player.sendOverlayMessage(formDescription(stack));
        }
        return true;
    }

    private static void fold(ItemStack stack) {
        stack.set(DataComponents.ITEM_MODEL, STAFF_MODEL);
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.remove(FLAME_UNTIL));
    }

    private static void playFormSound(Level level, Entity holder, boolean flame) {
        level.playSound(null, holder.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS,
                0.85f, flame ? 1.0f : 0.75f);
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!target.level().isClientSide()) {
            if (isFlameForm(stack) && !expireFlameForm(stack, attacker.level(), attacker)
                    && target.getRemainingFireTicks() <= FIRE_TICK) {
                target.setRemainingFireTicks(FIRE_TICK);
            }
            tryBreakEquipments(target);
        }

        super.hurtEnemy(stack, target, attacker);
    }

    private void tryBreakEquipments(LivingEntity target) {
        if (target instanceof Player || target instanceof NPCSimpleEntity)
            return;

        ServerLevel world = (ServerLevel) target.level();
        if (world.getRandom().nextFloat() >= 0.15f) {
            return;
        }
        int i = world.getRandom().nextIntBetweenInclusive(0, EquipmentSlot.values().length - 1);
        breakSlot(target, EquipmentSlot.values()[i]);
    }

    private void breakSlot(LivingEntity target, EquipmentSlot eSlot) {
        ServerLevel world = (ServerLevel) target.level();

        ItemStack stack = target.getItemBySlot(eSlot);
        if (stack.isDamageableItem()) {
            int damage = Math.max(1, stack.getMaxDamage() / 4);
            stack.hurtAndBreak(damage, target, eSlot);
            world.playSound(target, target.blockPosition(), SoundEvents.ITEM_BREAK.value(), SoundSource.PLAYERS, 1, 1);
        }
    }

}
