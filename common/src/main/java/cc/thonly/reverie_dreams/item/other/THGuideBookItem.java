package cc.thonly.reverie_dreams.item.other;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

import java.net.URI;

public class THGuideBookItem extends Item {
    public static final URI GUIDE_URI = URI.create("https://reverie-dreams-docs.thonly.cc/");
    public THGuideBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            Component message = Component.literal("§6📖 Guidebook§r\n")
                    .append(Component.literal("This version has no compatible Guidebook API.\n"))
                    .append(Component.literal("Please click the link below to open the documentation.\n\n"))
                    .append(Component.literal("👉 Open Wiki")
                            .setStyle(Style.EMPTY.withClickEvent(
                                    new ClickEvent.OpenUrl(GUIDE_URI)
                            ))
                    );

            serverPlayer.sendSystemMessage(message);

            return InteractionResult.SUCCESS_SERVER;
        }

        return InteractionResult.SUCCESS;
    }
}
