package cc.thonly.reverie_dreams.entity.npc;

import cc.thonly.mystias_izakaya.item.base.DrinkItem;
import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.data.ModTags;
import cc.thonly.reverie_dreams.gui.NPCGui;
import cc.thonly.reverie_dreams.interfaces.IItemStack;
import cc.thonly.reverie_dreams.item.ModItems;
import cc.thonly.reverie_dreams.registry.IntrinsicalRegister;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import cc.thonly.reverie_dreams.sound.SoundEventInit;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.UseRemainderComponent;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Slf4j
public class NPCRoleInteractionEvents {
    public static final List<NPCRoleMessage> MESSAGES = new ArrayList<>();

    static {
        registerMessage(Text.translatable("npc.event.send_message.0"));
        registerMessage(Text.translatable("npc.event.send_message.1"));
        registerMessage(Text.translatable("npc.event.send_message.2"));
        registerMessage(Text.translatable("npc.event.send_message.3"));
        registerMessage(Text.translatable("npc.event.send_message.4"));
        registerMessage(Text.translatable("npc.event.send_message.5"));
        registerMessage(Text.translatable("npc.event.send_message.6"));
        registerMessage(Text.translatable("npc.event.send_message.7"));
        registerMessage(Text.translatable("npc.event.send_message.8"));
        registerMessage(Text.translatable("npc.event.send_message.9"));
    }

    public static final NPCRoleInteractionEvent MESSAGE = registerEvent("message", (world, player, stack, hand, entity) -> {
        if (stack.isEmpty() && !player.isSneaking() && entity.isTamed()) {
            if (MESSAGES.isEmpty()) {
                return NPCInteractResult.PASS;
            }
            Random random = Random.create();
            NPCRoleMessage npcRoleMessage = MESSAGES.get(random.nextBetween(0, MESSAGES.size() - 1));
            MutableText message = npcRoleMessage.getMessage(world, player, stack, hand, entity);
            MutableText body = Text.empty();
            body.append(entity.getName());
            body.append(": ");
            body.append(message);
            Optional.ofNullable(npcRoleMessage.getSoundEvent()).ifPresent(
                    (soundEvent) -> {
                        world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), soundEvent, SoundCategory.HOSTILE);
                    }
            );
            player.sendMessage(body, false);
            return NPCInteractResult.SUCCESS;
        }
        return NPCInteractResult.PASS;
    });
    public static final NPCRoleInteractionEvent ON_OPEN_INVENTORY = registerEvent("on_open_inventory", (world, player, stack, hand, entity) -> {
        if (stack.isEmpty() && entity.isAllowOpenInventory(player) && player.isSneaking()) {
            NPCGui npcGui = new NPCGui(player, entity);
            npcGui.open();
            return NPCInteractResult.SUCCESS;
        }
        return NPCInteractResult.PASS;
    });
    public static final NPCRoleInteractionEvent ON_UPGRADED_HEALTH = registerEvent("on_upgraded_health", (world, player, stack, hand, entity) -> {
        if (!entity.isOwner(player)) {
            return NPCInteractResult.PASS;
        }
        if (stack.getItem() == ModItems.UPGRADED_HEALTH) {
            AttributeContainer attributes = entity.getAttributes();
            EntityAttributeInstance max_health = attributes.getCustomInstance(EntityAttributes.MAX_HEALTH);
            float health = entity.getHealth();
            float maxHealth = entity.getMaxHealth();
            if (max_health != null) {
                max_health.setBaseValue(maxHealth + 2);
                entity.setHealth(health + 2);
            }
            player.swingHand(hand);
            player.playSound(SoundEventInit.UP, 1.0f, 1.0f);
            stack.decrementUnlessCreative(1, player);
            return NPCInteractResult.SUCCESS;
        }
        return NPCInteractResult.PASS;
    });
    public static final NPCRoleInteractionEvent ON_TAME = registerEvent("on_tame", (world, player, stack, hand, entity) -> {
        if (entity.npcOwner.isEmpty() && stack.isIn(ModTags.ItemTypeTag.ROLE_TAME_FOOD)) {
            Random random = Random.create();
            float chance = random.nextFloat();
            if (chance <= 0.4) {
                entity.setOwner(player);
                entity.setTamed(true, true);
                world.spawnParticles(ParticleTypes.HEART, entity.getX(), entity.getY() + 1.0, entity.getZ(), 5, 0.5, 0.5, 0.5, 0.1);
            }
            entity.setHealth(entity.getHealth() + 5);
            stack.decrementUnlessCreative(1, player);
            player.swingHand(hand);
            return NPCInteractResult.SUCCESS;
        }
        return NPCInteractResult.PASS;
    });
    public static final NPCRoleInteractionEvent ON_FEED_POTIONS = registerEvent("on_feed_potions", (world, player, stack, hand, entity) -> {
        if (!entity.isOwner(player)) {
            return NPCInteractResult.PASS;
        }
        if (stack.isEmpty()) {
            return NPCInteractResult.PASS;
        }
        if (entity.canFeed() && (stack.getItem() == Items.POTION || stack.getItem() instanceof DrinkItem drinkItem)) {
            UseRemainderComponent useRemainderComponent = stack.get(DataComponentTypes.USE_REMAINDER);
            entity.playSound(SoundEvents.ENTITY_GENERIC_DRINK.value(), 1.0f, 1.0f);
            ItemStack result = stack.finishUsing(world, entity);
            if (!player.isInCreativeMode()) {
                player.setStackInHand(hand, result);
            }
            if (useRemainderComponent != null && !player.isInCreativeMode()) {
                ItemStack itemStack = useRemainderComponent.convert(stack, stack.getCount(), player.isInCreativeMode(), player::giveOrDropStack);
                player.setStackInHand(hand, itemStack);
            }
            player.swingHand(hand);
            return NPCInteractResult.SUCCESS;
        }
        return NPCInteractResult.PASS;
    });
    public static final NPCRoleInteractionEvent ON_FEED_FOOD = registerEvent("_feed_food", (world, player, stack, hand, entity) -> {
        if (!entity.isOwner(player)) {
            return NPCInteractResult.PASS;
        }
        if (stack.isEmpty()) {
            return NPCInteractResult.PASS;
        }
        if ((((IItemStack) (Object) stack).isFood() || stack.isIn(ModTags.ItemTypeTag.ROLE_TAME_FOOD)) && entity.canFeed()) {
            entity.playSound(SoundEvents.ENTITY_GENERIC_EAT.value(), 1.0f, 1.0f);
            ItemStack result = stack.finishUsing(world, entity);
            if (!player.isInCreativeMode()) {
                player.setStackInHand(hand, result);
            }
            player.swingHand(hand);
            return NPCInteractResult.SUCCESS;
        }
        return NPCInteractResult.PASS;
    });
    public static final NPCRoleInteractionEvent SET_OWNER_BY_STICk = registerEvent("set_owner_by_stick", (world, player, stack, hand, entity) -> {
        if (stack.getItem() == ModItems.OWNER_STICK) {
            entity.setOwner(player);
            return NPCInteractResult.SUCCESS;
        }
        return NPCInteractResult.PASS;
    });

    public static void bootstrap(IntrinsicalRegister<NPCRoleInteractionEvent> registry) {

    }

    public static ActionResult emit(ServerWorld world, ServerPlayerEntity player, Hand hand, NPCRoleEntity entity) {
        ItemStack itemStack = player.getStackInHand(hand);
        for (NPCRoleInteractionEvent event : RegistryManager.ROLE_INTERACTION_EVENT) {
            int i = 0;
            NPCInteractResult interact = null;
            try {
                i++;
                interact = event.interact(world, player, itemStack, hand, entity);
                if (interact == NPCInteractResult.PASS) {
                    continue;
                }
                return switch (interact) {
                    case SUCCESS -> ActionResult.SUCCESS_SERVER;
                    case FAIL -> ActionResult.FAIL;
                    default -> throw new IllegalStateException("Unexpected value: " + interact);
                };
            } catch (Exception err) {
                log.error("Role Interaction event {} triggering failed", interact != null ? interact.name() : i, err);
            }
        }
        return ActionResult.CONSUME;
    }

    public static NPCRoleMessage registerMessage(MutableText mutableText) {
        NPCRoleMessage npcRoleMessage = new NPCRoleMessage() {
            @Override
            public @NotNull MutableText getMessage(ServerWorld world, ServerPlayerEntity player, ItemStack stack, Hand hand, BaseNPCLikeEntity entity) {
                return mutableText;
            }

            @Override
            public Identifier getId() {
                return Touhou.id("message/%s".formatted(MESSAGES.size()));
            }
        };
        MESSAGES.add(npcRoleMessage);
        return npcRoleMessage;
    }

    public static NPCRoleMessage registerMessage(NPCRoleMessage message) {
        MESSAGES.add(message);
        return message;
    }

    public static NPCRoleInteractionEvent registerEvent(String name, NPCRoleInteractionEvent.InteractionCallback callback) {
        return registerEvent(Touhou.id(name), callback);
    }

    public static NPCRoleInteractionEvent registerEvent(Identifier eventId, NPCRoleInteractionEvent.InteractionCallback callback) {
        NPCRoleInteractionEvent event = new NPCRoleInteractionEvent(callback);
        return RegistryManager.registerForBuiltin(RegistryManager.ROLE_INTERACTION_EVENT, eventId, event);
    }
}
