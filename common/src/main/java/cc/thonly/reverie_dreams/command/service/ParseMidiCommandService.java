package cc.thonly.reverie_dreams.command.service;

import cc.thonly.reverie_dreams.command.CommandService;
import cc.thonly.reverie_dreams.util.command.PermissionPredicates;
import cc.thonly.reverie_dreams.util.nbs.Midi2Nbs;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.Locale;
import java.util.stream.Stream;

@Slf4j
public final class ParseMidiCommandService implements CommandService {
    private static final Path NOTA_DIR = Path.of("./config/reverie_dreams/nota");

    private static final SuggestionProvider<CommandSourceStack> SUGGESTIONS =
            (context, builder) -> {
                if (!Files.exists(NOTA_DIR) || !Files.isDirectory(NOTA_DIR)) {
                    return builder.buildFuture();
                }

                try (Stream<Path> files = Files.list(NOTA_DIR)) {
                    files.filter(Files::isRegularFile)
                            .filter(ParseMidiCommandService::isMidiFile)
                            .map(path -> path.getFileName().toString())
                            .sorted(String.CASE_INSENSITIVE_ORDER)
                            .forEach(value -> cc.thonly.reverie_dreams.util.command.FilteredSuggestions.file(builder,value));
                } catch (IOException e) {
                    log.error("Failed to list MIDI files", e);
                }

                cc.thonly.reverie_dreams.util.command.FilteredSuggestions.value(builder,"*");
                return builder.buildFuture();
            };

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("parse_nbs_midi")
                .then(Commands.argument("file_name", StringArgumentType.string())
                        .suggests(SUGGESTIONS)
                        .requires(PermissionPredicates.isGameMasters())
                        .executes(ParseMidiCommandService::execute)
                );
    }

    public static int execute(CommandContext<CommandSourceStack> context) {
        String fileName = StringArgumentType.getString(context, "file_name");
        CommandSourceStack source = context.getSource();

        try {
            Files.createDirectories(NOTA_DIR);
            if ("*".equals(fileName)) {
                return convertBatch(source);
            }
            return convertSingle(source, fileName);
        } catch (Exception e) {
            log.error("Failed to import MIDI files", e);
        }

        return 0;
    }

    private static int convertBatch(CommandSourceStack source) throws IOException {
        int total = 0;
        int success = 0;
        int skipped = 0;
        int failed = 0;

        try (Stream<Path> stream = Files.list(NOTA_DIR)) {
            for (Path inputPath : stream
                    .filter(Files::isRegularFile)
                    .filter(ParseMidiCommandService::isMidiFile)
                    .sorted(Comparator.comparing(
                            path -> path.getFileName().toString(),
                            String.CASE_INSENSITIVE_ORDER
                    ))
                    .toList()) {

                total++;

                String inputName = inputPath.getFileName().toString();
                String outputFileName = toNbsName(inputName);
                Path outputPath = NOTA_DIR.resolve(outputFileName);

                if (Files.exists(outputPath)) {
                    skipped++;
                    source.sendSystemMessage(
                            Component.translatable("command.touhou.parse_midi.skip_exists", inputName)
                    );
                    continue;
                }

                try (FileChannel channel = FileChannel.open(
                        inputPath,
                        StandardOpenOption.READ,
                        StandardOpenOption.WRITE
                )) {
                    try (FileLock lock = channel.tryLock()) {
                        if (lock == null) {
                            skipped++;
                            source.sendSystemMessage(
                                    Component.translatable("command.touhou.parse_midi.skip_in_use", inputName)
                            );
                            continue;
                        }

                        try {
                            Midi2Nbs.midi2nbs(inputPath, outputPath, new Midi2Nbs.Options());
                            success++;
                            source.sendSystemMessage(
                                    Component.translatable("command.touhou.parse_midi.success", inputName, outputFileName)
                            );
                        } catch (Exception e) {
                            failed++;
                            log.error("Error parsing MIDI {}", inputPath, e);
                            source.sendFailure(
                                    Component.translatable("command.touhou.parse_midi.failure", inputName)
                            );
                        }
                    }
                } catch (OverlappingFileLockException e) {
                    skipped++;
                    source.sendSystemMessage(
                            Component.translatable("command.touhou.parse_midi.skip_in_use", inputName)
                    );
                } catch (IOException e) {
                    skipped++;
                    log.error("Unable to access MIDI {}", inputPath, e);
                    source.sendFailure(
                            Component.translatable("command.touhou.parse_midi.cannot_access", inputName)
                    );
                }
            }
        }

        source.sendSystemMessage(
                Component.translatable(
                        "command.touhou.parse_midi.finished",
                        total,
                        success,
                        skipped,
                        failed
                )
        );

        return success > 0 ? 1 : 0;
    }

    private static int convertSingle(CommandSourceStack source, String fileName) {
        Path inputPath = NOTA_DIR.resolve(fileName).normalize();

        if (!Files.isRegularFile(inputPath)) {
            source.sendFailure(
                    Component.translatable("command.touhou.parse_midi.file_not_found", fileName)
            );
            return 0;
        }

        String lowerName = fileName.toLowerCase(Locale.ROOT);
        if (!isMidiName(lowerName)) {
            source.sendFailure(
                    Component.translatable("command.touhou.parse_midi.not_midi", fileName)
            );
            return 0;
        }

        String outputFileName = toNbsName(fileName);
        Path outputPath = NOTA_DIR.resolve(outputFileName);

        if (Files.exists(outputPath)) {
            source.sendSystemMessage(
                    Component.translatable("command.touhou.parse_midi.skip_output_exists", outputFileName)
            );
            return 1;
        }

        try (FileChannel channel = FileChannel.open(
                inputPath,
                StandardOpenOption.READ,
                StandardOpenOption.WRITE
        )) {
            try (FileLock lock = channel.tryLock()) {
                if (lock == null) {
                    source.sendFailure(
                            Component.translatable("command.touhou.parse_midi.file_in_use", fileName)
                    );
                    return 0;
                }

                try {
                    Midi2Nbs.midi2nbs(inputPath, outputPath, new Midi2Nbs.Options());
                    source.sendSystemMessage(
                            Component.translatable("command.touhou.parse_midi.saved", outputPath)
                    );
                } catch (Exception e) {
                    log.error("Error parsing MIDI {}", inputPath, e);
                    source.sendFailure(
                            Component.translatable("command.touhou.parse_midi.failure_generic")
                    );
                    return 0;
                }
            }
        } catch (OverlappingFileLockException e) {
            source.sendFailure(
                    Component.translatable("command.touhou.parse_midi.file_in_use", fileName)
            );
            return 0;
        } catch (IOException e) {
            log.error("Unable to access MIDI {}", inputPath, e);
            source.sendFailure(
                    Component.translatable("command.touhou.parse_midi.cannot_access_generic")
            );
            return 0;
        }

        return 1;
    }

    private static boolean isMidiFile(Path path) {
        return isMidiName(path.getFileName().toString().toLowerCase(Locale.ROOT));
    }

    private static boolean isMidiName(String lowerName) {
        return lowerName.endsWith(".mid") || lowerName.endsWith(".midi");
    }

    private static String toNbsName(String inputName) {
        int suffixLength = inputName.toLowerCase(Locale.ROOT).endsWith(".mid") ? 4 : 5;
        return inputName.substring(0, inputName.length() - suffixLength) + ".nbs";
    }
}
