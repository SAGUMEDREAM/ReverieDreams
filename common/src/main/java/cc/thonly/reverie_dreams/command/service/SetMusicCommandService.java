package cc.thonly.reverie_dreams.command.service;

import cc.thonly.reverie_dreams.block.entity.MusicBlockEntity;
import cc.thonly.reverie_dreams.command.CommandService;
import cc.thonly.reverie_dreams.item.prop.MusicalInstrumentItem;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.util.entity.PlayerHelper;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.stream.Stream;

@Slf4j
public final class SetMusicCommandService implements CommandService {
    private static final Path NOTA_DIR = Path.of("./config/reverie_dreams/nota");

    private static final SuggestionProvider<CommandSourceStack> SUGGESTIONS =
            (context, builder) -> {
                if (!Files.exists(NOTA_DIR) || !Files.isDirectory(NOTA_DIR)) {
                    return builder.buildFuture();
                }

                try (Stream<Path> files = Files.list(NOTA_DIR)) {
                    files.filter(Files::isRegularFile)
                            .filter(path -> {
                                String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
                                return name.endsWith(".nbs");
                            })
                            .map(path -> path.getFileName().toString())
                            .sorted(String.CASE_INSENSITIVE_ORDER)
                            .forEach(value -> cc.thonly.reverie_dreams.util.command.FilteredSuggestions.file(builder,value));

                } catch (IOException e) {
                    log.error("Error: ", e);
                }

                return builder.buildFuture();
            };

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("set_music")
                .then(Commands.argument("file_name", StringArgumentType.string())
                        .suggests(SUGGESTIONS)
                        .executes(this::execute)
                );
    }

    private int execute(CommandContext<CommandSourceStack> context) {
        String fileName = StringArgumentType.getString(context, "file_name");
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();

        if (player == null) {
            return 0;
        }

        ServerLevel level = player.level();

        if (!Files.exists(NOTA_DIR.resolve(fileName))) {
            player.sendSystemMessage(
                    Component.translatable("command.touhou.set_music.file_not_exists", fileName)
            );
            return 0;
        }

        BlockHitResult hitResult = PlayerHelper.getPlayerLookBlock(player, 64.0);

        if (hitResult.getType() != HitResult.Type.BLOCK) {
            ItemStack mainHandItem = player.getItemInHand(InteractionHand.MAIN_HAND);
            ItemStack offHandItem = player.getItemInHand(InteractionHand.OFF_HAND);

            if (mainHandItem.getItem() instanceof MusicalInstrumentItem) {
                mainHandItem.set(
                        RDDataComponentTypes.PLAYING_MUSIC.value(),
                        fileName
                );

                player.sendSystemMessage(
                        Component.translatable("command.touhou.set_music.success_item", fileName)
                );
                return 1;

            } else if (offHandItem.getItem() instanceof MusicalInstrumentItem) {
                offHandItem.set(
                        RDDataComponentTypes.PLAYING_MUSIC.value(),
                        fileName
                );

                player.sendSystemMessage(
                        Component.translatable("command.touhou.set_music.success_item", fileName)
                );
                return 1;
            }

            player.sendSystemMessage(
                    Component.translatable("command.touhou.set_music.no_target")
            );
            return 0;
        }

        BlockPos pos = hitResult.getBlockPos();
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (!(blockEntity instanceof MusicBlockEntity musicBlockEntity)) {
            player.sendSystemMessage(
                    Component.translatable("command.touhou.set_music.not_music_block")
            );
            return 0;
        }

        musicBlockEntity.setSelect(fileName);
        musicBlockEntity.setChanged();
        musicBlockEntity.play();

        player.sendSystemMessage(
                Component.translatable("command.touhou.set_music.success_block", fileName)
        );

        return 1;
    }
}
