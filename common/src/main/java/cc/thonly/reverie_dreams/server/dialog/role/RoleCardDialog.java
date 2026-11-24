package cc.thonly.reverie_dreams.server.dialog.role;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.npc.NPCRoleType;
import cc.thonly.reverie_dreams.item.base.RoleCard;
import cc.thonly.reverie_dreams.item.template.RoleCardItem;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.server.CustomClickActionRegistry;
import cc.thonly.reverie_dreams.server.dialog.DialogBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.server.dialog.DialogAction;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class RoleCardDialog {
    private static final int WIDTH = 300;

    private RoleCardDialog() { }

    public static Dialog selection(String session, RoleCardItem.UsingData data) {
        return menu(session, data.getItemStack().getHoverName(), choices(data.getRoleList()), true, false);
    }

    public static Dialog groups(String session) {
        List<Choice> choices = new ArrayList<>();
        for (RoleCard card : BuiltInRegistryProviders.ROLE_CARD) {
            Identifier id = BuiltInRegistryProviders.ROLE_CARD.getKey(card);
            if (id != null && !card.isEmpty()) {
                choices.add(new Choice(Component.translatable(card.translationKey()), "menu", Optional.of(id)));
            }
        }
        return menu(session, Component.translatable("item.reverie_dreams.role_card.full"), choices, false, false);
    }

    // Called only after RoleCards has checked the session owner, world and distance.
    public static void navigate(ServerPlayer player, String session, RoleCardItem.UsingData data, CompoundTag tag) {
        if (!data.isFreeChoice()) {
            return;
        }
        String group = tag.getString("group_id").orElse("");
        if (group.isEmpty()) {
            player.openDialog(Holder.direct(groups(session)));
            return;
        }
        Identifier id = Identifier.tryParse(group);
        if (id == null) {
            return;
        }
        RoleCard card = BuiltInRegistryProviders.ROLE_CARD.get(id).map(Holder::value).orElse(null);
        if (card == null) {
            return;
        }
        List<NPCRoleType> allowed = card.stream()
                .filter(role -> data.getId2Role().containsKey(role.getId())).toList();
        player.openDialog(Holder.direct(menu(session, Component.translatable(card.translationKey()), choices(allowed), false, true)));
    }

    private static List<Choice> choices(List<NPCRoleType> roles) {
        List<Choice> choices = new ArrayList<>();
        for (NPCRoleType role : roles) {
            if (role.getId() != null) {
                choices.add(new Choice(Component.translatable(role.translateKey()), role.getId().toString(), Optional.empty()));
            }
        }
        return choices;
    }

    static Dialog menu(String session, Component title, List<Choice> choices, boolean random, boolean back) {
        return DialogBuilder.builder(builder -> {
            builder.key(ReverieDreams.id("role_card/selection"));
            builder.common(common -> {
                common.title(title).setPause(false).setCanCloseWithEscape(true).setAfterAction(DialogAction.CLOSE);
                common.addTextBody(Component.translatable("dialog.message.select"), WIDTH);
                for (Choice choice : choices) {
                    common.addTextBody(link(session, choice, ChatFormatting.AQUA), WIDTH);
                }
                if (random) {
                    common.addTextBody(link(session, new Choice(Component.translatable("dialog.text.random"), "random", Optional.empty()), ChatFormatting.GOLD), WIDTH);
                }
                if (back) {
                    common.addTextBody(link(session, new Choice(Component.translatable("dialog.text.back"), "menu", Optional.empty()), ChatFormatting.GRAY), WIDTH);
                }
                common.addTextBody(link(session, new Choice(Component.translatable("dialog.text.exit"), "close", Optional.empty()), ChatFormatting.GRAY), WIDTH);
            });
        }).get().buildTextOnlyOrThrow().get().value();
    }

    private static Component link(String session, Choice choice, ChatFormatting color) {
        CompoundTag tag = new CompoundTag();
        tag.putString("session_id", session);
        tag.putString("entity_id", choice.target());
        choice.group().ifPresent(id -> tag.putString("group_id", id.toString()));
        return Component.literal("› ").append(choice.label().copy()).withStyle(style -> style
                .withColor(color).withUnderlined(true)
                .withClickEvent(new ClickEvent.Custom(CustomClickActionRegistry.ROLE_SUMMON_KEY, Optional.of(tag))));
    }

    record Choice(Component label, String target, Optional<Identifier> group) { }
}
