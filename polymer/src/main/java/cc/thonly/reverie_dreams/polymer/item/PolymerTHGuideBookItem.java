package cc.thonly.reverie_dreams.polymer.item;

import cc.thonly.reverie_dreams.ReverieDreams;
import eu.pb4.booklet.impl.BookletImplUtil;
import eu.pb4.booklet.impl.BookletOpenState;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import xyz.nucleoid.server.translations.api.LocalizationTarget;

import java.util.function.Consumer;

@SuppressWarnings("NullableProblems")
public class PolymerTHGuideBookItem extends Item {
    public static final Identifier GUIDE_PAGE_ID = ReverieDreams.id("welcome");
    public PolymerTHGuideBookItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BOOK_PAGE_TURN, SoundSource.UI);
            BookletImplUtil.openPage(serverPlayer, GUIDE_PAGE_ID, BookletOpenState.DEFAULT);
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public Component getName(ItemStack stack) {
        var ctx = LocalizationTarget.forPacket();

        var page = BookletImplUtil.getPage(GUIDE_PAGE_ID, ctx != null && ctx.getLanguageCode() != null ? ctx.getLanguageCode() : "en_us");
        if (page != null) {
            return page.info().getExternalTitle();
        }
        return super.getName(stack);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        var ctx = LocalizationTarget.forPacket();

        var page = BookletImplUtil.getPage(GUIDE_PAGE_ID, ctx != null && ctx.getLanguageCode() != null ? ctx.getLanguageCode() : "en_us");
        if (page != null && page.info().description().isPresent()) {
            tooltipAdder.accept(Component.empty().append(page.info().description().orElseThrow()).withStyle(ChatFormatting.GRAY));
        }
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
    }

}
