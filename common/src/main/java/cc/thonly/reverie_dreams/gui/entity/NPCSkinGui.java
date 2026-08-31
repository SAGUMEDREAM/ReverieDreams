package cc.thonly.reverie_dreams.gui.entity;

import cc.thonly.reverie_dreams.data.npc.NPCRoleType;
import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import cc.thonly.reverie_dreams.entity.npc.NPCSimpleRedirectEntity;
import cc.thonly.reverie_dreams.item.base.RoleCard;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.server.CustomClickActionRegistry;
import cc.thonly.reverie_dreams.server.SessionManager;
import cc.thonly.reverie_dreams.util.math.ModMth;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.dialog.*;
import net.minecraft.server.dialog.action.StaticAction;
import net.minecraft.server.dialog.body.PlainMessage;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings({"unchecked", "resource"})
public class NPCSkinGui {
    public static final List<NPCRoleType> OTHER_ROLES = new ArrayList<>();

    public static void registerOtherRole(NPCRoleType roleType) {
        if (roleType == null || OTHER_ROLES.contains(roleType)) {
            return;
        }

        OTHER_ROLES.add(roleType);
    }

    public static void open(ServerPlayer player, NPCSimpleEntity npc) {
        UUID sessionUid = UUID.randomUUID();
        String session = sessionUid.toString();

        Dialog selectMenu = createSelectMenu(session, npc);

        SessionManager.startSession(
                player.getUUID(),
                sessionUid
        );

        player.openDialog(Holder.direct(selectMenu));
    }

    public static void handleAction(
            ServerPlayer player,
            ServerboundCustomClickActionPacket packet
    ) {
        Optional<Tag> payload = packet.payload();

        if (payload.isEmpty()) {
            return;
        }

        Tag element = payload.get();

        if (!(element instanceof CompoundTag compound)) {
            return;
        }

        Optional<String> siOptional =
                compound.getString("session_id");

        Optional<String> nrtOptional =
                compound.getString("npc_role_type_id");

        Optional<String> tiOptional =
                compound.getString("target_id");

        if (siOptional.isEmpty()
                || nrtOptional.isEmpty()
                || tiOptional.isEmpty()) {
            return;
        }

        String sessionId = siOptional.get();
        String npcRoleTypeId = nrtOptional.get();
        String targetId = tiOptional.get();

        if (SessionManager.getSession(player.getUUID()) == null) {
            return;
        }

        Identifier npcRoleTypeIdentifier = Identifier.tryParse(npcRoleTypeId);
        if (npcRoleTypeIdentifier == null) {
            return;
        }

        ServerLevel level = player.level();
        Entity entity = level.getEntity(UUID.fromString(targetId));
        if (!(entity instanceof NPCSimpleEntity npc)) {
            return;
        }
        NPCRoleType roleType = BuiltInRegistryProviders.NPC_ROLE_TYPE.getValue(npcRoleTypeIdentifier);
        if (roleType == null) {
            return;
        }
        npc.setRoleType(roleType);
        npc.setSkinType(roleType.getSkinType());
        npc.setCustomName(Component.translatable(roleType.translateKey()));
        SessionManager.clear(player.getUUID());
    }

    public static Dialog createSelectMenu(
            String sessionUid,
            NPCSimpleEntity npc
    ) {
        MultiActionDialog dialog = new MultiActionDialog(
                new CommonDialogData(
                        Component.translatable(
                                "item.reverie_dreams.role_card.full"
                        ),
                        Optional.empty(),
                        true,
                        false,
                        DialogAction.CLOSE,
                        new ArrayList<>(
                                List.of(
                                        new PlainMessage(
                                                Component.translatable(
                                                        "dialog.message.select"
                                                ),
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

            MultiActionDialog subDialog = createRoleCardDialog(roleCard, roleTypes, sessionUid, npc);

            dialog.actions().add(
                    new ActionButton(
                            new CommonButtonData(
                                    Component.translatable(
                                            roleCard.translationKey()
                                    ),
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

        if (!OTHER_ROLES.isEmpty()) {
            MultiActionDialog otherDialog =
                    createOtherDialog(
                            sessionUid,
                            npc
                    );

            dialog.actions().add(
                    new ActionButton(
                            new CommonButtonData(
                                    Component.translatable(
                                            "dialog.text.other"
                                    ),
                                    180
                            ),
                            Optional.of(
                                    new StaticAction(
                                            new ClickEvent.ShowDialog(
                                                    Holder.direct(otherDialog)
                                            )
                                    )
                            )
                    )
            );
        }

        dialog.actions().add(
                new ActionButton(
                        new CommonButtonData(
                                Component.translatable(
                                        "dialog.text.exit"
                                ),
                                200
                        ),
                        Optional.empty()
                )
        );

        return dialog;
    }

    private static MultiActionDialog createRoleCardDialog(
            RoleCard roleCard,
            List<NPCRoleType> roleTypes,
            String sessionUid,
            NPCSimpleEntity npc
    ) {
        MultiActionDialog dialog = new MultiActionDialog(
                new CommonDialogData(
                        Component.translatable(
                                roleCard.translationKey()
                        ),
                        Optional.empty(),
                        true,
                        false,
                        DialogAction.CLOSE,
                        new ArrayList<>(
                                List.of(
                                        new PlainMessage(
                                                Component.translatable(
                                                        "dialog.message.select"
                                                ),
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
            ActionButton button =
                    createSubDialog(
                            roleType,
                            sessionUid,
                            npc
                    );

            if (button != null) {
                dialog.actions().add(button);
            }
        }

        dialog.actions().add(createExitButton());

        return dialog;
    }

    private static MultiActionDialog createOtherDialog(
            String sessionUid,
            NPCSimpleEntity npc
    ) {
        MultiActionDialog dialog = new MultiActionDialog(
                new CommonDialogData(
                        Component.translatable(
                                "dialog.text.other"
                        ),
                        Optional.empty(),
                        true,
                        false,
                        DialogAction.CLOSE,
                        new ArrayList<>(
                                List.of(
                                        new PlainMessage(
                                                Component.translatable(
                                                        "dialog.message.select"
                                                ),
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

        for (NPCRoleType roleType : OTHER_ROLES) {
            ActionButton button =
                    createSubDialog(
                            roleType,
                            sessionUid,
                            npc
                    );

            if (button != null) {
                dialog.actions().add(button);
            }
        }

        dialog.actions().add(createExitButton());

        return dialog;
    }

    private static ActionButton createExitButton() {
        return new ActionButton(
                new CommonButtonData(
                        Component.translatable(
                                "dialog.text.exit"
                        ),
                        200
                ),
                Optional.empty()
        );
    }

    public static ActionButton createSubDialog(
            NPCRoleType roleType,
            String sessionUid,
            NPCSimpleEntity npc
    ) {
        Identifier roleId =
                BuiltInRegistryProviders.NPC_ROLE_TYPE.getKey(
                        roleType
                );

        if (roleId == null) {
            return null;
        }

        EntityType<NPCSimpleRedirectEntity> entityType =
                (EntityType<NPCSimpleRedirectEntity>) (Object)
                        roleType.get().value();

        CompoundTag element =
                createTag(
                        roleType,
                        sessionUid,
                        npc
                );

        return new ActionButton(
                new CommonButtonData(
                        Component.translatable(
                                entityType.getDescriptionId()
                        ),
                        180
                ),
                Optional.of(
                        new StaticAction(
                                new ClickEvent.Custom(
                                        CustomClickActionRegistry.ROLE_CHANGE_SKIN_KEY,
                                        Optional.of(element)
                                )
                        )
                )
        );
    }

    public static CompoundTag createTag(
            NPCRoleType roleType,
            String sessionUid,
            NPCSimpleEntity npc
    ) {
        CompoundTag element = new CompoundTag();

        element.putString(
                "session_id",
                sessionUid
        );

        element.putString(
                "npc_role_type_id",
                roleType.getId().toString()
        );

        element.putString(
                "target_id",
                npc.getUUID().toString()
        );

        return element;
    }
}