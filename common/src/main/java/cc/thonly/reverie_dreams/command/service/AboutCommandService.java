package cc.thonly.reverie_dreams.command.service;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.command.CommandService;
import cc.thonly.reverie_dreams.util.ImageToTextScanner;
import cc.thonly.reverie_dreams.util.PlatformContext;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public final class AboutCommandService implements CommandService {
    private static final String[] INFO_KEYS = new String[]{
            "command.touhou.about.line1",
            "command.touhou.about.line2",
            "command.touhou.about.line3",
            "command.touhou.about.line4",
            "command.touhou.about.line5",
            "command.touhou.about.line6",
            "command.touhou.about.title",
            "command.touhou.about.version",
            "command.touhou.about.author",
            "command.touhou.about.line10",
            "command.touhou.about.line11"
    };

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("about").executes(context -> {
            Class<?> clazz = ReverieDreams.class;
            ImageToTextScanner instance = ImageToTextScanner.createInstance(clazz);
            String path = ImageToTextScanner.ofNamespace(ReverieDreams.MOD_ID, "icon_about.png");
            BufferedImage iconBuffer = instance.loadImageFromJar(path);
            List<Component> iconText = instance.renderImageToText(iconBuffer, 16, 16);

            List<Component> rightTexts = new ArrayList<>();
            for (String key : INFO_KEYS) {
                if (key.equals("command.touhou.about.version")) {
                    rightTexts.add(Component.translatable(key, PlatformContext.VERSION.get()));
                } else {
                    rightTexts.add(Component.translatable(key));
                }
            }

            while (rightTexts.size() < iconText.size()) {
                rightTexts.add(Component.literal(""));
            }

            for (int i = 0; i < iconText.size(); i++) {
                Component left = iconText.get(i);
                Component right = rightTexts.get(i).copy().withStyle(ChatFormatting.WHITE);
                context.getSource().sendSuccess(() -> Component.empty().append(left).append(Component.literal("  ")).append(right), false);
            }

            return 1;
        });
    }
}
