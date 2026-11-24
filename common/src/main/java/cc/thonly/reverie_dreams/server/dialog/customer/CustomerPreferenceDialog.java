package cc.thonly.reverie_dreams.server.dialog.customer;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.Customer;
import cc.thonly.reverie_dreams.data.CustomerBudget;
import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import cc.thonly.reverie_dreams.entity.npc.container.NPCCustomerContainer;
import cc.thonly.reverie_dreams.api.registry.RegistryEntryTranslatable;
import cc.thonly.reverie_dreams.server.dialog.DialogBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Optional;

/**
 * 顾客偏好只读页。顾客数据、当前订单和预算都在服务端读取，Dialog 不信任客户端 payload。
 */
public final class CustomerPreferenceDialog {
    private static final int BODY_WIDTH = 300;

    private CustomerPreferenceDialog() {
    }

    public static void open(ServerPlayer player, NPCSimpleEntity npc) {
        if (player == null || npc == null || !npc.isAlive()
                || !npc.isOwnedBy(player)
                || npc.distanceToSqr(player) > 64.0 * 64.0) {
            return;
        }

        Customer customer = npc.getRoleType().getCustomer();
        NPCCustomerContainer container = npc.getCustomerContainer();

        DialogBuilder.builder(builder -> {
            builder.key(ReverieDreams.id("dialog/customer_preferences"));
            builder.common(common -> {
                common.title(Component.translatable("dialog.customer.preferences.title", npc.getName()));
                common.setPause(false);
                common.setCanCloseWithEscape(true);
                common.addTextBody(Component.translatable(
                        "dialog.customer.preferences.status",
                        npc.getWorkMode().translationKey()
                ), BODY_WIDTH);
                common.addTextBody(Component.translatable(
                        "dialog.customer.preferences.likes",
                        formatProperties(customer.likes())
                ), BODY_WIDTH);
                common.addTextBody(Component.translatable(
                        "dialog.customer.preferences.dislikes",
                        formatProperties(customer.dislikes())
                ), BODY_WIDTH);
                common.addTextBody(Component.translatable(
                        "dialog.customer.preferences.beverages",
                        formatProperties(customer.beverages())
                ), BODY_WIDTH);

                CustomerBudget budget = customer.budget();
                if (budget.max() == Integer.MAX_VALUE) {
                    common.addTextBody(Component.translatable("dialog.customer.preferences.budget.unlimited"), BODY_WIDTH);
                } else {
                    common.addTextBody(Component.translatable(
                            "dialog.customer.preferences.budget",
                            budget.min(),
                            budget.max()
                    ), BODY_WIDTH);
                }

                if (container.isCustomerMode()) {
                    if (container.getOrderBudget() > 0) {
                        common.addTextBody(Component.translatable(
                                "dialog.customer.preferences.current_budget",
                                container.getOrderBudget()
                        ), BODY_WIDTH);
                    }

                    common.addTextBody(Component.translatable("dialog.customer.preferences.current_order"), BODY_WIDTH);
                    if (container.getComponents().isEmpty()) {
                        common.addTextBody(Component.translatable("dialog.customer.preferences.none"), BODY_WIDTH);
                    } else {
                        for (Component line : container.getComponents()) {
                            common.addTextBody(line, BODY_WIDTH);
                        }
                    }
                } else {
                    common.addTextBody(Component.translatable("dialog.customer.preferences.not_working"), BODY_WIDTH);
                }
            });
            builder.actions(actions -> actions.addButton(
                    Component.translatable("dialog.text.exit"),
                    200,
                    Optional.empty()
            ));
        }).get().buildOrThrow().open(player);
    }

    private static Component formatProperties(List<? extends RegistryEntryTranslatable> properties) {
        if (properties.isEmpty()) {
            return Component.translatable("dialog.customer.preferences.none");
        }
        MutableComponent result = Component.empty();
        for (int i = 0; i < properties.size(); i++) {
            if (i > 0) {
                result.append(Component.literal("、"));
            }
            result.append(Component.translatable(properties.get(i).translateKey()));
        }
        return result;
    }
}
