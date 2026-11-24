package cc.thonly.reverie_dreams.command.service;

import cc.thonly.reverie_dreams.command.CommandService;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public final class TouhouRootCommandService implements CommandService {
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("touhou").executes(context -> {
            MutableComponent text = Component.translatable("command.touhou.suggest_help");
            context.getSource().sendSuccess(() -> text.setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), false);
            return 1;
        });
    }
}
