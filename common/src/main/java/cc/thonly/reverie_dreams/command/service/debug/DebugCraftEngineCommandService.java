package cc.thonly.reverie_dreams.command.service.debug;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.command.CommandService;
import cc.thonly.reverie_dreams.data.craftengine.BlockDefinitionList;
import cc.thonly.reverie_dreams.data.craftengine.CraftEngineDefinition;
import cc.thonly.reverie_dreams.data.craftengine.CraftEngineProvider;
import cc.thonly.reverie_dreams.data.craftengine.ItemDefinitionList;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
public final class DebugCraftEngineCommandService implements CommandService {
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("debug_generate_craft_engine")
                .executes(context -> {
                    CraftEngineDefinition craftEngineDefinition =
                            CraftEngineProvider.fromNamespace(ReverieDreams.MOD_ID);

                    if (craftEngineDefinition == null) {
                        context.getSource().sendFailure(Component.translatable("command.touhou.craft_engine.generate.failed"));
                        return 0;
                    }

                    List<BlockDefinitionList> blockDefinitions = craftEngineDefinition.getBlockDefinitions();
                    List<ItemDefinitionList> itemDefinitions = craftEngineDefinition.getItemDefinitions();

                    Path path = Path.of("./config/reverie_dreams/craft_engine/");

                    try {
                        Files.createDirectories(path);
                    } catch (Exception e) {
                        log.error("Failed to create CraftEngine config directory", e);

                        context.getSource().sendFailure(Component.translatable("command.touhou.craft_engine.generate.directory_failed"));
                        return 0;
                    }

                    // Items 分片生成
                    CraftEngineProvider.toItemFiles(
                            itemDefinitions,
                            path
                    );

                    // Blocks 继续单文件
                    CraftEngineProvider.toFile(
                            blockDefinitions,
                            path.resolve("blocks.yml")
                    );

                    context.getSource().sendSystemMessage(
                            Component.translatable(
                                    "command.touhou.craft_engine.generate.success"
                            )
                    );

                    return 1;
                });
    }
}
