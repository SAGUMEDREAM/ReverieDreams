package cc.thonly.reverie_dreams.entity.npc;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.entity.type.ChatAIEntity;
import cc.thonly.reverie_dreams.api.item.ItemStackHelper;
import cc.thonly.reverie_dreams.data.npc.NPCRoleInteractionEvent;
import cc.thonly.reverie_dreams.data.skin.SkinType;
import cc.thonly.reverie_dreams.gui.entity.NPCGui;
import cc.thonly.reverie_dreams.item.base.CustomSkinSelectorItem;
import cc.thonly.reverie_dreams.registry.RegistryImpls;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.registry.content.skin.SkinTypes;
import cc.thonly.reverie_dreams.registry.impl.RegistryImpl;
import cc.thonly.reverie_dreams.registry.tag.RDItemTags;
import cc.thonly.reverie_dreams.server.CustomClickActionRegistry;
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

@SuppressWarnings({"UnusedReturnValue", "unused"})
@Slf4j
public class NPCRoleInteractionEvents {
    public static final List<NPCRoleMessage> MESSAGES = new ArrayList<>();

    static {
        registerGeneralMessage(Component.translatable("npc.event.send_message.0"));
        registerGeneralMessage(Component.translatable("npc.event.send_message.1"));
        registerGeneralMessage(Component.translatable("npc.event.send_message.2"));
        registerGeneralMessage(Component.translatable("npc.event.send_message.3"));
        registerGeneralMessage(Component.translatable("npc.event.send_message.4"));
        registerGeneralMessage(Component.translatable("npc.event.send_message.5"));
        registerGeneralMessage(Component.translatable("npc.event.send_message.6"));
        registerGeneralMessage(Component.translatable("npc.event.send_message.7"));
        registerGeneralMessage(Component.translatable("npc.event.send_message.8"));
        registerGeneralMessage(Component.translatable("npc.event.send_message.9"));
    }

    public static final NPCRoleInteractionEvent MESSAGE = registerEvent("message", (world, player, stack, hand, entity) -> {
        if (stack.isEmpty() && !player.isShiftKeyDown() && entity.isTame()) {
            return handleChat(world, player, stack, hand, entity);
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
    public static final NPCRoleInteractionEvent ON_TAME = registerEvent("on_tame", (world, player, stack, hand, entity) -> {
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
    public static final NPCRoleInteractionEvent ON_FEED_POTIONS = registerEvent("on_feed_potions", (world, player, stack, hand, entity) -> {
        if (!entity.isOwnedBy(player)) {
            return NPCInteractResult.PASS;
        }
        if (stack.isEmpty()) {
            return NPCInteractResult.PASS;
        }
        if (entity.canFeed() && (stack.getItem() == Items.POTION || stack.has(RDDataComponents.DRINK_ITEM_TYPE.value()))) {
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
    public static final NPCRoleInteractionEvent ON_FEED_FOOD = registerEvent("on_feed_food", (world, player, stack, hand, entity) -> {
        if (!entity.isOwnedBy(player)) {
            return NPCInteractResult.PASS;
        }
        if (stack.isEmpty()) {
            return NPCInteractResult.PASS;
        }
        if ((((ItemStackHelper) (Object) stack).reverie_dreams$isFood() || stack.is(RDItemTags.ROLE_TAME_FOOD)) && entity.canFeed()) {
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
    public static final NPCRoleInteractionEvent ON_SELECT_CUSTOM_SKIN = registerEvent("on_select_custom_skin", (world, player, stack, hand, entity) -> {
        Level level = player.level();
        if (level.isClientSide()) {
            return NPCInteractResult.SUCCESS;
        }
        if (!(stack.getItem() instanceof CustomSkinSelectorItem)) {
            return NPCInteractResult.PASS;
        }
        if (!(entity instanceof NPCRoleEntity npcRoleEntity)) {
            return NPCInteractResult.PASS;
        }
        if ((!npcRoleEntity.isOwner(player)) && (!player.isCreative())) {
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
                    tag.putString("dim_key", npcRoleEntity.level().dimension().identifier().toString());
                    tag.putInt("target_id", npcRoleEntity.getId());
                    component.append(
                            Component.translatable(npcRoleEntity.getRoleType().translateKey())
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
                    tag.putString("dim_key", npcRoleEntity.level().dimension().identifier().toString());
                    tag.putInt("target_id", npcRoleEntity.getId());
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
    public static final NPCRoleInteractionEvent SET_OWNER_BY_STICk = registerEvent("set_owner_by_stick", (world, player, stack, hand, entity) -> {
        if (stack.getItem() == RDItems.OWNER_STICK.asItem()) {
            entity.setOwner(player);
            return NPCInteractResult.SUCCESS;
        }
        return NPCInteractResult.PASS;
    });

    public static void bootstrap(RegistryImpl<NPCRoleInteractionEvent> registry) {

    }

    public static InteractionResult emit(ServerLevel world, ServerPlayer player, InteractionHand hand, NPCRoleEntity entity) {
        ItemStack itemStack = player.getItemInHand(hand);
        for (NPCRoleInteractionEvent event : RegistryImpls.ROLE_INTERACTION_EVENT) {
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

    public static NPCRoleMessage registerGeneralMessage(MutableComponent mutableText) {
        NPCRoleMessage npcRoleMessage = new NPCRoleMessage() {
            @Override
            public @NotNull MutableComponent getMessage(ServerLevel world, ServerPlayer player, ItemStack stack, InteractionHand hand, BaseNPCLikeEntity entity) {
                return mutableText;
            }

            @Override
            public Identifier getId() {
                return ReverieDreams.id("message/%s".formatted(MESSAGES.size()));
            }
        };
        MESSAGES.add(npcRoleMessage);
        return npcRoleMessage;
    }

    private static NPCInteractResult handleChat(ServerLevel world, ServerPlayer player, ItemStack stack, InteractionHand hand, BaseNPCLikeEntity entity) {
        if (ReverieDreams.config().enableAIReplacesGeneralChat) {
            return handleAIChat(world, player, stack, hand, entity);
        }
        return handleGenericChat(world, player, stack, hand, entity);
    }

    private static NPCInteractResult handleAIChat(ServerLevel world, ServerPlayer player, ItemStack stack, InteractionHand hand, BaseNPCLikeEntity entity) {
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
        player.sendSystemMessage(body, false);
        return NPCInteractResult.SUCCESS;
    }

    public static NPCRoleMessage registerGeneralMessage(NPCRoleMessage message) {
        MESSAGES.add(message);
        return message;
    }

    public static NPCRoleInteractionEvent registerEvent(String name, NPCRoleInteractionEvent.InteractionCallback callback) {
        return registerEvent(ReverieDreams.id(name), callback);
    }

    public static NPCRoleInteractionEvent registerEvent(Identifier eventId, NPCRoleInteractionEvent.InteractionCallback callback) {
        NPCRoleInteractionEvent event = new NPCRoleInteractionEvent(callback);
        return RegistryImpls.registerForBuiltin(RegistryImpls.ROLE_INTERACTION_EVENT, eventId, event);
    }
}
