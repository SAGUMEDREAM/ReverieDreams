package cc.thonly.reverie_dreams.command.service;

import cc.thonly.reverie_dreams.command.CommandService;
import cc.thonly.reverie_dreams.data.skin.SkinType;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.util.command.PermissionPredicates;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

@Slf4j
public final class CacheSkinsCommandService implements CommandService {
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("start_cached_skins")
                .requires(PermissionPredicates.isGameMasters())
                .executes(context -> {
                    for (SkinType skinType : BuiltInRegistryProviders.SKIN_TYPE_MERGED) {
                        try {
                            if (skinType.getProperty() == null) {
                                throw new NullPointerException();
                            }
                        } catch (Exception e) {
                            log.error("Can't request get skin {}", skinType.getId());
                            context.getSource().sendSuccess(() -> Component.literal("§d× Can't get skin %s Succeed".formatted(skinType.getId())), false);
                            continue;
                        }
                        context.getSource().sendSuccess(() -> Component.literal("§a√ Get skin %s Succeed".formatted(skinType.getId())), false);
                    }
                    context.getSource().sendSuccess(() -> Component.literal("§e Cached skin worker has finished"), false);
                    return 1;
                });
    }
}
