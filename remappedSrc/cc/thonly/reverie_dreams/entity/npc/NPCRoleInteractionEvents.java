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
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.UseRemainder;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Slf4j
public class NPCRoleInteractionEvents {
    public static final List<NPCRoleMessage> MESSAGES = new ArrayList<>();

    static {
        registerMessage(Component.translatable("npc.event.send_message.0"));
        registerMessage(Component.translatable("npc.event.send_message.1"));
        registerMessage(Component.translatable("npc.event.send_message.2"));
        registerMessage(Component.translatable("npc.event.send_message.3"));
        registerMessage(Component.translatable("npc.event.send_message.4"));
        registerMessage(Component.translatable("npc.event.send_message.5"));
        registerMessage(Component.translatable("npc.event.send_message.6"));
        registerMessage(Component.translatable("npc.event.send_message.7"));
        registerMessage(Component.translatable("npc.event.send_message.8"));
        registerMessage(Component.translatable("npc.event.send_message.9"));
    }

    public static final NPCRoleInteractionEvent MESSAGE = registerEvent("message", (world, player, stack, hand, entity) -> {
        if (stack.isEmpty() && !player.isShiftKeyDown() && entity.isTame()) {
            if (MESSAGES.isEmpty()) {
                return NPCInteractResult.PASS;
            }
            RandomSource random = RandomSource.create();
            NPCRoleMessage npcRoleMessage = MESSAGES.get(random.nextIntBetweenInclusive(0, MESSAGES.size() - 1));
            MutableComponent message = npcRoleMessage.getMessage(world, player, stack, hand, entity);
            MutableComponent body = Component.empty();
            body.append(entity.getName());
            body.append(": ");
            body.append(message);
            Optional.ofNullable(npcRoleMessage.getSoundEvent()).ifPresent(
                    (soundEvent) -> {
                        world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), soundEvent, SoundSource.HOSTILE);
                    }
            );
            player.displayClientMessage(body, false);
            return NPCInteractResult.SUCCESS;
        }
        return NPCInteractResult.PASS;
    });
    public static final NPCRoleInteractionEvent ON_OPEN_INVENTORY = registerEvent("on_open_inventory", (world, player, stack, hand, entity) -> {
        if (stack.isEmpty() && entity.isAllowOpenInventory(player) && player.isShiftKeyDown()) {
            NPCGui npcGui = new NPCGui(player, entity);
            npcGui.open();
            return NPCInteractResult.SUCCESS;
        }
        return NPCInteractResult.PASS;
    });
    public static final NPCRoleInteractionEvent ON_UPGRADED_HEALTH = registerEvent("on_upgraded_health", (world, player, stack, hand, entity) -> {
        if (!entity.isOwnedBy(player)) {
            return NPCInteractResult.PASS;
        }
        if (stack.getItem() == ModItems.UPGRADED_HEALTH) {
            AttributeMap attributes = entity.getAttributes();
            AttributeInstance max_health = attributes.getInstance(Attributes.MAX_HEALTH);
            float health = entity.getHealth();
            float maxHealth = entity.getMaxHealth();
            if (max_health != null) {
                max_health.setBaseValue(maxHealth + 2);
                entity.setHealth(health + 2);
            }
            player.swing(hand);
            player.playSound(SoundEventInit.UP, 1.0f, 1.0f);
            stack.consume(1, player);
            return NPCInteractResult.SUCCESS;
        }
        return NPCInteractResult.PASS;
    });
    public static final NPCRoleInteractionEvent ON_TAME = registerEvent("on_tame", (world, player, stack, hand, entity) -> {
        if (entity.npcOwner.isEmpty() && stack.is(ModTags.ItemTypeTag.ROLE_TAME_FOOD)) {
            RandomSource random = RandomSource.create();
            float chance = random.nextFloat();
            if (chance <= 0.4) {
                entity.setOwner(player);
                entity.setTame(true, true);
                world.sendParticles(ParticleTypes.HEART, entity.getX(), entity.getY() + 1.0, entity.getZ(), 5, 0.5, 0.5, 0.5, 0.1);
            }
            entity.setHealth(entity.getHealth() + 5);
            stack.consume(1, player);
            player.swing(hand);
            return NPCInteractResult.SUCCESS;
        }
        return NPCInteractResult.PASS;
    });
    public static final NPCRoleInteractionEvent ON_FEED_POTIONS = registerEvent("on_feed_potions", (world, player, stack, hand, entity) -> {
        if (!entity.isOwnedBy(player)) {
            return NPCInteractResult.PASS;
        }
        if (stack.isEmpty()) {
            return NPCInteractResult.PASS;
        }
        if (entity.canFeed() && (stack.getItem() == Items.POTION || stack.getItem() instanceof DrinkItem drinkItem)) {
            UseRemainder useRemainderComponent = stack.get(DataComponents.USE_REMAINDER);
            entity.playSound(SoundEvents.GENERIC_DRINK.value(), 1.0f, 1.0f);
            ItemStack result = stack.finishUsingItem(world, entity);
            if (!player.hasInfiniteMaterials()) {
                player.setItemInHand(hand, result);
            }
            if (useRemainderComponent != null && !player.hasInfiniteMaterials()) {
                ItemStack itemStack = useRemainderComponent.convertIntoRemainder(stack, stack.getCount(), player.hasInfiniteMaterials(), player::handleExtraItemsCreatedOnUse);
                player.setItemInHand(hand, itemStack);
            }
            player.swing(hand);
            return NPCInteractResult.SUCCESS;
        }
        return NPCInteractResult.PASS;
    });
    public static final NPCRoleInteractionEvent ON_FEED_FOOD = registerEvent("_feed_food", (world, player, stack, hand, entity) -> {
        if (!entity.isOwnedBy(player)) {
            return NPCInteractResult.PASS;
        }
        if (stack.isEmpty()) {
            return NPCInteractResult.PASS;
        }
        if ((((IItemStack) (Object) stack).isFood() || stack.is(ModTags.ItemTypeTag.ROLE_TAME_FOOD)) && entity.canFeed()) {
            entity.playSound(SoundEvents.GENERIC_EAT.value(), 1.0f, 1.0f);
            ItemStack result = stack.finishUsingItem(world, entity);
            if (!player.hasInfiniteMaterials()) {
                player.setItemInHand(hand, result);
            }
            player.swing(hand);
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

    public static InteractionResult emit(ServerLevel world, ServerPlayer player, InteractionHand hand, NPCRoleEntity entity) {
        ItemStack itemStack = player.getItemInHand(hand);
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
                    case SUCCESS -> InteractionResult.SUCCESS_SERVER;
                    case FAIL -> InteractionResult.FAIL;
                    default -> throw new IllegalStateException("Unexpected value: " + interact);
                };
            } catch (Exception err) {
                log.error("Role Interaction event {} triggering failed", interact != null ? interact.name() : i, err);
            }
        }
        return InteractionResult.CONSUME;
    }

    public static NPCRoleMessage registerMessage(MutableComponent mutableText) {
        NPCRoleMessage npcRoleMessage = new NPCRoleMessage() {
            @Override
            public @NotNull MutableComponent getMessage(ServerLevel world, ServerPlayer player, ItemStack stack, InteractionHand hand, BaseNPCLikeEntity entity) {
                return mutableText;
            }

            @Override
            public ResourceLocation getId() {
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

    public static NPCRoleInteractionEvent registerEvent(ResourceLocation eventId, NPCRoleInteractionEvent.InteractionCallback callback) {
        NPCRoleInteractionEvent event = new NPCRoleInteractionEvent(callback);
        return RegistryManager.registerForBuiltin(RegistryManager.ROLE_INTERACTION_EVENT, eventId, event);
    }
}
