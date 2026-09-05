package cc.thonly.reverie_dreams.item.template;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.proxy.SafeClientAccess;
import cc.thonly.reverie_dreams.data.npc.NPCRoleType;
import cc.thonly.reverie_dreams.entity.npc.NPCSimpleRedirectEntity;
import cc.thonly.reverie_dreams.item.base.RoleCard;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.content.RoleCards;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.server.CustomClickActionRegistry;
import cc.thonly.reverie_dreams.util.math.ModMth;
import cc.thonly.reverie_dreams.world.RDBuiltInGameRules;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dialog.*;
import net.minecraft.server.dialog.action.StaticAction;
import net.minecraft.server.dialog.body.PlainMessage;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;

import java.util.*;

@SuppressWarnings({"DuplicatedCode", "unchecked"})
@Getter
public class RoleCardItem extends Item {
    public static final BiMap<String, UsingData> USING_DATA_MAP = HashBiMap.create();
    public static final SoundEvent SOUND = SoundEvents.BUCKET_FILL;

    public RoleCardItem(Properties settings) {
        super(settings);
    }

    public Optional<RoleCard> getRoleCardComponent(ItemStack itemStack) {
        Identifier identifier = itemStack.get(RDDataComponentTypes.ROLE_CARD_ID.value());
        if (identifier == null) {
            return Optional.empty();
        }
        RoleCard roleCard = BuiltInRegistryProviders.ROLE_CARD.getValue(identifier);
        if (roleCard == null) {
            return Optional.empty();
        }
        return Optional.of(roleCard);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        if (!world.isClientSide() && context.getLevel() instanceof ServerLevel serverWorld && context.getPlayer() instanceof ServerPlayer player) {
            InteractionHand hand = context.getHand();
            ItemStack itemStack = context.getItemInHand();
            GameRules gameRules = serverWorld.getGameRules();

            UUID uuid = UUID.randomUUID();
            boolean freeChoice = gameRules.get(RDBuiltInGameRules.FREE_CHOICE_OF_ROLE.value());
            Optional<RoleCard> roleCardWrapper = this.getRoleCardComponent(itemStack);
            if (freeChoice && roleCardWrapper.isEmpty()) {
                UsingData data = new UsingData(player, serverWorld, context.getClickedPos().above(), itemStack, RoleCards.PROTAGONIST_GROUP);

                USING_DATA_MAP.put(uuid.toString(), data);
                Dialog selectMenu = getAllSelectMenu(uuid.toString());
                player.openDialog(Holder.direct(selectMenu));

                player.swing(hand);
                return InteractionResult.SUCCESS_SERVER;
            }
            if (roleCardWrapper.isPresent()) {
                RoleCard roleCard = roleCardWrapper.get();
                UsingData data = new UsingData(player, serverWorld, context.getClickedPos().above(), itemStack, roleCard);

                USING_DATA_MAP.put(uuid.toString(), data);
                Dialog selectMenu = getSelectMenu(uuid.toString(), data);
                player.openDialog(Holder.direct(selectMenu));
                player.swing(hand);
                return InteractionResult.SUCCESS_SERVER;
            }
        }
        return super.useOn(context);
    }

    @Override
    public Component getName(ItemStack itemStack) {
        Component defaultName = super.getName(itemStack);
        MinecraftServer server = ReverieDreams.getServer();
        if (server != null) {
            ServerLevel overworld = server.overworld();
            GameRules gameRules = overworld.getGameRules();
            if (gameRules.get(RDBuiltInGameRules.FREE_CHOICE_OF_ROLE.value())) {
                return Component.translatable("item.reverie_dreams.role_card.full");
            }
        }
        return defaultName;
    }

    @SuppressWarnings("unchecked")
    public static Dialog getSelectMenu(String uuid, UsingData data) {
        MultiActionDialog dialog = new MultiActionDialog(
                new CommonDialogData(
                        data.getItemStack().getHoverName(),
                        Optional.empty(),
                        true, false,
                        DialogAction.CLOSE,
                        new ArrayList<>(
                                List.of(
                                        new PlainMessage(Component.translatable("dialog.message.select"), 200)
                                )
                        ),
                        new ArrayList<>()
                ),
                new ArrayList<>(List.of()),
                Optional.empty(),
                1
        );
        for (Map.Entry<Identifier, NPCRoleType> entry : data.id2Role.entrySet()) {
            Identifier identifier = entry.getKey();
            NPCRoleType role = entry.getValue();
            EntityType<NPCSimpleRedirectEntity> entityType = (EntityType<NPCSimpleRedirectEntity>) (Object) role.get().value();
            CompoundTag element = new CompoundTag();
            element.putString("session_id", uuid);
            element.putString("entity_id", identifier.toString());
            dialog.actions().add(new ActionButton(
                    new CommonButtonData(Component.empty().append(Component.translatable(entityType.getDescriptionId())), 180),
                    Optional.of(new StaticAction(new ClickEvent.Custom(CustomClickActionRegistry.ROLE_SUMMON_KEY, Optional.of(element))))
            ));
        }
        CompoundTag element = new CompoundTag();
        element.putString("session_id", uuid);
        element.putString("entity_id", "random");
        dialog.actions().add(new ActionButton(
                new CommonButtonData(Component.empty().append(Component.translatable("dialog.text.random")), 180),
                Optional.of(new StaticAction(new ClickEvent.Custom(CustomClickActionRegistry.ROLE_SUMMON_KEY, Optional.of(element))))
        ));
        dialog.actions().add(new ActionButton(
                new CommonButtonData(Component.empty().append(Component.translatable("dialog.text.exit")), 200),
                Optional.empty()
        ));
        return dialog;
    }

    public static Dialog getAllSelectMenu(String uuid) {
        MultiActionDialog dialog = new MultiActionDialog(
                new CommonDialogData(
                        Component.translatable("item.reverie_dreams.role_card.full"),
                        Optional.empty(),
                        true,
                        false,
                        DialogAction.CLOSE,
                        new ArrayList<>(
                                List.of(
                                        new PlainMessage(
                                                Component.translatable("dialog.message.select"),
                                                200
                                        )
                                )
                        ),
                        new ArrayList<>()
                ),
                new ArrayList<>(),
                Optional.empty(),
                1
        );

        for (RoleCard roleCard : BuiltInRegistryProviders.ROLE_CARD) {
            Identifier roleCardId =
                    BuiltInRegistryProviders.ROLE_CARD.getKey(roleCard);

            if (roleCardId == null) {
                continue;
            }

            List<NPCRoleType> roleTypes =
                    ModMth.toList(roleCard.stream());

            if (roleTypes.isEmpty()) {
                continue;
            }

            MultiActionDialog subDialog = new MultiActionDialog(
                    new CommonDialogData(
                            Component.translatable(roleCard.translationKey()),
                            Optional.empty(),
                            true,
                            false,
                            DialogAction.CLOSE,
                            new ArrayList<>(
                                    List.of(
                                            new PlainMessage(
                                                    Component.translatable("dialog.message.select"),
                                                    200
                                            )
                                    )
                            ),
                            new ArrayList<>()
                    ),
                    new ArrayList<>(),
                    Optional.empty(),
                    1
            );

            for (NPCRoleType roleType : roleTypes) {
                Identifier roleId =
                        BuiltInRegistryProviders.NPC_ROLE_TYPE.getKey(roleType);

                if (roleId == null) {
                    continue;
                }

                EntityType<NPCSimpleRedirectEntity> entityType =
                        (EntityType<NPCSimpleRedirectEntity>) (Object) roleType.get().value();

                CompoundTag element = new CompoundTag();
                element.putString("session_id", uuid);
                element.putString("entity_id", roleId.toString());

                subDialog.actions().add(
                        new ActionButton(
                                new CommonButtonData(
                                        Component.translatable(
                                                entityType.getDescriptionId()
                                        ),
                                        180
                                ),
                                Optional.of(
                                        new StaticAction(
                                                new ClickEvent.Custom(
                                                        CustomClickActionRegistry.ROLE_SUMMON_KEY,
                                                        Optional.of(element)
                                                )
                                        )
                                )
                        )
                );
            }

            subDialog.actions().add(
                    new ActionButton(
                            new CommonButtonData(
                                    Component.translatable("dialog.text.exit"),
                                    200
                            ),
                            Optional.empty()
                    )
            );

            dialog.actions().add(
                    new ActionButton(
                            new CommonButtonData(
                                    Component.translatable(roleCard.translationKey()),
                                    180
                            ),
                            Optional.of(
                                    new StaticAction(
                                            new ClickEvent.ShowDialog(
                                                    Holder.direct(subDialog)
                                            )
                                    )
                            )
                    )
            );
        }

        dialog.actions().add(
                new ActionButton(
                        new CommonButtonData(
                                Component.translatable("dialog.text.exit"),
                                200
                        ),
                        Optional.empty()
                )
        );

        return dialog;
    }

    @Getter
    public static class UsingData {
        private final ServerPlayer player;
        private final ServerLevel world;
        private final ItemStack itemStack;
        private final BlockPos blockPos;
        private final RoleCard roleCard;
        private final Map<Identifier, NPCRoleType> id2Role = new Object2ObjectOpenHashMap<>();
        private final List<NPCRoleType> roleList = new ArrayList<>();

        public UsingData(ServerPlayer player, ServerLevel world, BlockPos blockPos, ItemStack itemStack, RoleCard roleCard) {
            this.player = player;
            this.world = world;
            this.itemStack = itemStack;
            this.blockPos = blockPos;
            this.roleCard = roleCard;
            this.roleList.addAll(this.roleCard.stream().toList());
            for (NPCRoleType npcRole : this.roleList) {
                this.id2Role.put(npcRole.getId(), npcRole);
            }
        }
    }
}
