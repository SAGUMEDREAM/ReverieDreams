package cc.thonly.reverie_dreams.item.other;

import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import cc.thonly.reverie_dreams.server.BookPageManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

@SuppressWarnings("resource")
public class GuidebookItem extends Item {
    public GuidebookItem(Properties properties) {
        super(properties);
    }

    public GuidebookItem(String namespace, Properties properties) {
        super(properties.component(RDDataComponents.GUIDE_BOOK_NAMESPACE.value(), namespace));
    }

    public GuidebookItem(Identifier pageId, Properties properties) {
        super(properties.component(RDDataComponents.GUIDE_BOOK_PAGE_ID.value(), pageId));
    }

    public void openPageIfExist(ItemStack itemStack, Player iPlayer) {
        if (!(iPlayer instanceof ServerPlayer player)) {
            return;
        }
        String namespace = itemStack.get(RDDataComponents.GUIDE_BOOK_NAMESPACE.value());
        Identifier pageId = itemStack.get(RDDataComponents.GUIDE_BOOK_PAGE_ID.value());
        BookPageManager bookPageManager = BookPageManager.getInstance();
        if (namespace != null) {
            bookPageManager.openRoot(namespace, player);
        } else if (pageId != null) {
            boolean exists = bookPageManager.openIfExists(pageId, player);
            if (!exists) {
                player.sendSystemMessage(Component.literal("§cThe component reverie_dreams:guidebook_page_id was not recognized in the ItemStack."));
            }
        } else {
            player.sendSystemMessage(Component.literal("§cThe component reverie_dreams:guidebook_namespace or reverie_dreams:guidebook_page_id was not recognized in the ItemStack."));
        }
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        this.openPageIfExist(player.getItemInHand(hand), player);
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (context.getPlayer() == null) {
            return InteractionResult.SUCCESS_SERVER;
        }
        this.openPageIfExist(context.getItemInHand(), context.getPlayer());
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity target, InteractionHand type) {
        if (player.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        this.openPageIfExist(player.getItemInHand(type), player);
        return InteractionResult.SUCCESS_SERVER;
    }
}
