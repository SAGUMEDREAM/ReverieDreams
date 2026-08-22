package cc.thonly.reverie_dreams.entity.npc;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.entity.type.ChatAIEntity;
import cc.thonly.reverie_dreams.api.item.IItemStack;
import cc.thonly.reverie_dreams.data.Customer;
import cc.thonly.reverie_dreams.data.npc.NPCRoleType;
import cc.thonly.reverie_dreams.data.npc.NPCLikeInteractionEvent;
import cc.thonly.reverie_dreams.data.npc.RoleType;
import cc.thonly.reverie_dreams.data.skin.SkinType;
import cc.thonly.reverie_dreams.entity.npc.container.FavorabilityContainer;
import cc.thonly.reverie_dreams.entity.npc.container.NPCCustomerContainer;
import cc.thonly.reverie_dreams.entity.npc.container.NPCFoodDataContainer;
import cc.thonly.reverie_dreams.gui.entity.NPCGui;
import cc.thonly.reverie_dreams.item.base.CustomSkinSelectorItem;
import cc.thonly.reverie_dreams.item.prop.SoulCardItem;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.content.NPCStates;
import cc.thonly.reverie_dreams.registry.content.NPCWorkModes;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.registry.content.skin.SkinTypes;
import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import cc.thonly.reverie_dreams.registry.tag.RDItemTags;
import cc.thonly.reverie_dreams.server.CustomClickActionRegistry;
import cc.thonly.reverie_dreams.server.PlayerSettings;
import cc.thonly.reverie_dreams.server.dialog.DialogBuilder;
import cc.thonly.reverie_dreams.server.dialog.DialogEntry;
import cc.thonly.reverie_dreams.sound.RDSoundEvents;
import cc.thonly.reverie_dreams.util.advancements.SimpleTriggerFactory;
import cc.thonly.reverie_dreams.util.advancements.SimpleTriggerKeys;
import cc.thonly.reverie_dreams.util.sound.SoundEventPlayUtils;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.server.dialog.body.PlainMessage;
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
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.UseRemainder;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SuppressWarnings({"UnusedReturnValue", "unused", "rawtypes", "resource"})
@Slf4j
public class NPCLikeInteractionEvents {
    public static final String KEY_DEFAULT_SEND_MESSAGE_0 = "npc.event.send_message.0";
    public static final String KEY_DEFAULT_SEND_MESSAGE_1 = "npc.event.send_message.1";
    public static final String KEY_DEFAULT_SEND_MESSAGE_2 = "npc.event.send_message.2";
    public static final String KEY_DEFAULT_SEND_MESSAGE_3 = "npc.event.send_message.3";
    public static final String KEY_DEFAULT_SEND_MESSAGE_4 = "npc.event.send_message.4";
    public static final String KEY_DEFAULT_SEND_MESSAGE_5 = "npc.event.send_message.5";
    public static final String KEY_DEFAULT_SEND_MESSAGE_6 = "npc.event.send_message.6";
    public static final String KEY_DEFAULT_SEND_MESSAGE_7 = "npc.event.send_message.7";
    public static final String KEY_DEFAULT_SEND_MESSAGE_8 = "npc.event.send_message.8";
    public static final String KEY_DEFAULT_SEND_MESSAGE_9 = "npc.event.send_message.9";

    public static final NPCLikeInteractionEvent CUSTOMER = registerCompanionEvent("customer", (world, player, stack, hand, entity) -> {
        if (entity instanceof NPCSimpleEntity npc && npc.isTame()) {
            NPCCustomerContainer container = npc.getCustomerContainer();
            return container.triggerInteraction(player, hand, stack);
        }
        return NPCInteractResult.PASS;
    });
    public static final NPCLikeInteractionEvent MESSAGE = registerCompanionEvent("message", (world, player, stack, hand, entity) -> {
        if (stack.isEmpty() && !player.isShiftKeyDown() && entity.isTame()) {
            return handleChat(world, player, stack, hand, entity);
        }
        return NPCInteractResult.PASS;
    });
    public static final NPCLikeInteractionEvent ON_OPEN_INVENTORY = registerCompanionEvent("on_open_inventory", (world, player, stack, hand, entity) -> {
        if (stack.isEmpty() && entity instanceof NPCSimpleEntity npc && entity.isAllowOpenInventory(player) && player.isShiftKeyDown()) {
            NPCGui npcGui = new NPCGui(player, npc);
            npcGui.open();
            return NPCInteractResult.SUCCESS;
        }
        return NPCInteractResult.PASS;
    });
    public static final NPCLikeInteractionEvent ON_USE_SOUL_CARD = registerCompanionEvent("on_use_soul_card", (world, player, stack, hand, entity) -> {
        if (!entity.isOwnedBy(player)) {
            return NPCInteractResult.PASS;
        }
        if (stack.getItem() == RDItems.SOUL_CARD.asItem()) {
            if (stack.has(RDDataComponentTypes.ROLE_FOLLOWER_ARCHIVE.value())) {
                return NPCInteractResult.PASS;
            }
            MutableComponent mutableComponent = Component.empty();
            mutableComponent.append(stack.getItemName()).append("(").append(entity.getName()).append(")");
            stack.set(DataComponents.ITEM_NAME, mutableComponent);
            stack.set(RDDataComponentTypes.ROLE_FOLLOWER_ARCHIVE.value(), entity.toArchiveComponent());
            player.swing(hand);
            entity.discard();
            world.playSound(null, player.getX(), player.getY(), player.getZ(), SoulCardItem.SOUND, player.getSoundSource(), 2.0f, 1.0f);
            return NPCInteractResult.SUCCESS;
        }
        return NPCInteractResult.PASS;
    });
    public static final NPCLikeInteractionEvent ON_UPGRADED_HEALTH = registerCompanionEvent("on_upgraded_health", (world, player, stack, hand, entity) -> {
        if (!entity.isOwnedBy(player)) {
            return NPCInteractResult.PASS;
        }
        if (stack.getItem() == RDItems.UPGRADED_HEALTH.asItem()) {
            AttributeMap attributes = entity.getAttributes();
            AttributeInstance max_health = attributes.getInstance(Attributes.MAX_HEALTH);
            float health = entity.getHealth();
            float maxHealth = entity.getMaxHealth();
            if (max_health != null) {
                max_health.setBaseValue(maxHealth + 2);
                entity.setHealth(health + 2);
            }
            player.swing(hand);
            SoundEventPlayUtils.playSound(player, RDSoundEvents.UP.value(), SoundSource.NEUTRAL, 1.0f, 1.0f);
            stack.consume(1, player);
            return NPCInteractResult.SUCCESS;
        }
        return NPCInteractResult.PASS;
    });
    public static final NPCLikeInteractionEvent ON_TAME = registerCompanionEvent("on_tame", (world, player, stack, hand, entity) -> {
        if (entity.npcOwner.isEmpty() && stack.is(RDItemTags.ROLE_TAME_FOOD)) {
            RandomSource random = RandomSource.create();
            float chance = random.nextFloat();
            if (chance <= 0.4) {
                entity.setOwner(player);
                entity.setTame(true, true);
                world.sendParticles(ParticleTypes.HEART, entity.getX(), entity.getY() + 1.0, entity.getZ(), 5, 0.5, 0.5, 0.5, 0.1);
                SimpleTriggerFactory.create(SimpleTriggerKeys.MAKING_FRIEND).trigger(player);
            }
            entity.setHealth(entity.getHealth() + 5);
            stack.consume(1, player);
            player.swing(hand);

            return NPCInteractResult.SUCCESS;
        }
        return NPCInteractResult.PASS;
    });
    public static final NPCLikeInteractionEvent ON_FEED_POTIONS = registerCompanionEvent("on_feed_potions", (world, player, stack, hand, entity) -> {
        if (!entity.isOwnedBy(player)) {
            return NPCInteractResult.PASS;
        }
        if (stack.isEmpty()) {
            return NPCInteractResult.PASS;
        }
        if (entity.canFeed() && entity instanceof NPCSimpleEntity npc && (stack.getItem() == Items.POTION || stack.has(RDDataComponentTypes.DRINK_ITEM_TYPE.value()))) {
            UseRemainder useRemainderComponent = stack.get(DataComponents.USE_REMAINDER);
            npc.playSound(SoundEvents.GENERIC_DRINK.value(), 1.0f, 1.0f);
            ItemStack result = stack.finishUsingItem(world, entity);
            if (!player.hasInfiniteMaterials()) {
                player.setItemInHand(hand, result);
            }
            if (useRemainderComponent != null && !player.hasInfiniteMaterials()) {
                ItemStack itemStack = useRemainderComponent.convertIntoRemainder(stack, stack.getCount(), player.hasInfiniteMaterials(), player::handleExtraItemsCreatedOnUse);
                player.setItemInHand(hand, itemStack);
            }
            FavorabilityContainer favorabilityContainer = npc.getFavorabilityContainer();
            favorabilityContainer.add(player.getUUID(), npc.getRandom().nextInt(1, 9));
            player.swing(hand);
            return NPCInteractResult.SUCCESS;
        }
        return NPCInteractResult.PASS;
    });
    public static final NPCLikeInteractionEvent ON_FEED_FOOD = registerCompanionEvent("on_feed_food", (world, player, stack, hand, entity) -> {
        if (!entity.isOwnedBy(player)) {
            return NPCInteractResult.PASS;
        }
        if (stack.isEmpty()) {
            return NPCInteractResult.PASS;
        }
        if (!(entity instanceof NPCSimpleEntity npc)) {
            return NPCInteractResult.PASS;
        }
        if ((((IItemStack) (Object) stack).reverie_dreams$isFood() || stack.is(RDItemTags.ROLE_TAME_FOOD)) && entity.canFeed()) {
            SoundEventPlayUtils.playSound(player, SoundEvents.GENERIC_EAT.value(), SoundSource.PLAYERS, 1.0f, 1.0f);
            FoodProperties food = stack.get(DataComponents.FOOD);
            NPCFoodDataContainer foodData = npc.getFoodData();
            if (food != null) {
                foodData.eat(food);
            }
            ItemStack result = stack.finishUsingItem(world, entity);
            if (!player.hasInfiniteMaterials()) {
                player.setItemInHand(hand, result);
            }
            player.swing(hand);
            return NPCInteractResult.SUCCESS;
        }
        return NPCInteractResult.PASS;
    });
    public static final NPCLikeInteractionEvent ON_SELECT_CUSTOM_SKIN = registerCompanionEvent("on_select_custom_skin", (world, player, stack, hand, entity) -> {
        Level level = player.level();
        if (level.isClientSide()) {
            return NPCInteractResult.SUCCESS;
        }
        if (!(stack.getItem() instanceof CustomSkinSelectorItem)) {
            return NPCInteractResult.PASS;
        }
        if (!(entity instanceof NPCSimpleEntity npc)) {
            return NPCInteractResult.PASS;
        }
        if ((!npc.isOwner(player)) && (!player.isCreative())) {
            return NPCInteractResult.PASS;
        }
        DialogEntry dialogEntry = DialogBuilder.builder(builder -> {
            builder.key(ReverieDreams.id("custom_skin_selector"));
            builder.common(commons -> {
                commons.title(Component.translatable("gui.reverie_dreams.custom_skin_selector.title"));
                {
                    Identifier id = SkinType.RECOVERY;
                    Item icon = Items.STICK;
                    MutableComponent component = Component.empty();
                    CompoundTag tag = new CompoundTag();
                    tag.putString("skin_id", id.toString());
                    tag.putString("dim_key", npc.level().dimension().identifier().toString());
                    tag.putInt("target_id", npc.getId());
                    component.append(
                            Component.translatable(npc.getRoleType().translateKey())
                                     .withStyle(
                                             Style.EMPTY.withClickEvent(new ClickEvent.Custom(CustomClickActionRegistry.MODIFY_CUSTOM_SKIN_KEY, Optional.of(tag)))
                                     )
                    );
                    commons.addItemBody(new ItemStackTemplate(icon), Optional.of(
                            new PlainMessage(component, 200))
                    );
                }
                for (var customType : SkinTypes.getCustomTypes()) {
                    Identifier id = customType.getId();
                    Item icon = customType.getIcon();
                    MutableComponent component = Component.empty();
                    CompoundTag tag = new CompoundTag();
                    tag.putString("skin_id", id.toString());
                    tag.putString("dim_key", npc.level().dimension().identifier().toString());
                    tag.putInt("target_id", npc.getId());
                    component.append(
                            Component.translatable(customType.getDescriptionId())
                                     .withStyle(
                                             Style.EMPTY.withClickEvent(new ClickEvent.Custom(CustomClickActionRegistry.MODIFY_CUSTOM_SKIN_KEY, Optional.of(tag)))
                                     )
                    );
                    commons.addItemBody(new ItemStackTemplate(icon), Optional.of(
                            new PlainMessage(component, 200))
                    );
                }
            });
            builder.actions(actions -> {
                actions.addButton(Component.translatable("gui.reverie_dreams.close"), 180, Optional.empty());
            });
        }).get().buildOrThrow();
        dialogEntry.open(player);
        return NPCInteractResult.SUCCESS;
    });
    public static final NPCLikeInteractionEvent SET_OWNER_BY_STICk = registerCompanionEvent("set_owner_by_stick", (world, player, stack, hand, entity) -> {
        if (stack.is(RDItems.OWNER_STICK)) {
            entity.setOwner(player);
            return NPCInteractResult.SUCCESS;
        }
        return NPCInteractResult.PASS;
    });

    public static void bootstrap(RegistryProvider<NPCLikeInteractionEvent> registry) {

    }

    public static InteractionResult emit(ServerLevel world, ServerPlayer player, InteractionHand hand, NPCSimpleEntity entity) {
        ItemStack itemStack = player.getItemInHand(hand);
        for (NPCLikeInteractionEvent event : BuiltInRegistryProviders.NPCLIKE_INTERACTION_EVENT) {
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
        return InteractionResult.PASS;
    }

    private static NPCInteractResult handleChat(ServerLevel world, ServerPlayer player, ItemStack stack, InteractionHand hand, BaseNPCLikeEntity entity) {
        if (ReverieDreams.config().enableAIReplacesGeneralChat) {
            return handleAIChat(world, player, stack, hand, entity);
        }
        return handleGenericChat(world, player, stack, hand, entity);
    }

    private static NPCInteractResult handleAIChat(ServerLevel world, ServerPlayer player, ItemStack stack, InteractionHand hand, BaseNPCLikeEntity entity) {
        PlayerSettings playerSettings = PlayerSettings.get(player);
        Object disableChatAi = playerSettings.get(PlayerSettings.DISABLE_CHAT_AI);
        if (disableChatAi instanceof Boolean bool && bool) {
            return NPCInteractResult.PASS;
        }
        if (ReverieDreams.config().apiKey.isEmpty() || ReverieDreams.config().apiUrl.isEmpty()) {
            return handleGenericChat(world, player, stack, hand, entity);
        }
        if (entity instanceof ChatAIEntity chatAIEntity) {
            chatAIEntity.openChatAIGUI(player);
            return NPCInteractResult.SUCCESS;
        }
        return handleGenericChat(world, player, stack, hand, entity);
    }

    private static NPCInteractResult handleGenericChat(ServerLevel world, ServerPlayer player, ItemStack stack, InteractionHand hand, BaseNPCLikeEntity entity) {
        if (entity.getWorkMode().equals(NPCWorkModes.CUSTOMER) && entity.getNpcState().equals(NPCStates.WORKING)) {
            return NPCInteractResult.PASS;
        }
        RandomSource random = RandomSource.create();
        if (entity instanceof NPCSimpleEntity roleEntity) {
            RoleType roleType = roleEntity.getRoleType();
            Customer customer = roleType.getCustomer();
            List<Component> chats = customer.chats();
            Component component = chats.get(random.nextIntBetweenInclusive(0, chats.size() - 1));
            MutableComponent body = Component.empty();
            body.append(entity.getName());
            body.append(": ");
            body.append(component);
            SoundEventPlayUtils.playUISound(player, SoundEvents.NOTE_BLOCK_PLING.value(), 1.0f, 5.0f);
            player.sendSystemMessage(body, false);
        }
        return NPCInteractResult.SUCCESS;
    }

    public static NPCLikeInteractionEvent registerCompanionEvent(String name, NPCLikeInteractionEvent.InteractionCallback callback) {
        return registerEvent(ReverieDreams.id(name), (world, player, stack, hand, entity) -> {
            if (entity instanceof NPCCompanionEntity npc) {
                return callback.onInteract(world, player, stack, hand, npc);
            }
            return NPCInteractResult.PASS;
        });
    }

    public static NPCLikeInteractionEvent registerSimpleEvent(String name, NPCLikeInteractionEvent.InteractionCallback callback) {
        return registerEvent(ReverieDreams.id(name), callback);
    }

    public static NPCLikeInteractionEvent registerEvent(Identifier eventId, NPCLikeInteractionEvent.InteractionCallback callback) {
        NPCLikeInteractionEvent event = new NPCLikeInteractionEvent(callback);
        return BuiltInRegistryProviders.registerForBuiltin(BuiltInRegistryProviders.NPCLIKE_INTERACTION_EVENT, eventId, event);
    }
}
