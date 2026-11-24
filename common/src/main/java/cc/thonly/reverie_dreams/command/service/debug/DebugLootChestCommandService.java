package cc.thonly.reverie_dreams.command.service.debug;

import cc.thonly.reverie_dreams.command.CommandService;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceOrIdArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.SeededContainerLoot;
import net.minecraft.world.level.storage.loot.LootTable;

@Slf4j
public final class DebugLootChestCommandService implements CommandService {
    private final HolderLookup.Provider registryAccess;

    public DebugLootChestCommandService(HolderLookup.Provider registryAccess) {
        this.registryAccess = registryAccess;
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("debug_get_loot_with_chest")
                .then(Commands.argument("loot_table", ResourceOrIdArgument.lootTable(CommandBuildContext.simple(registryAccess, FeatureFlagSet.of())))
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            if (!source.isPlayer()) {
                                return 0;
                            }
                            ServerPlayer player = source.getPlayer();
                            assert player != null;
                            try {
                                Holder<LootTable> holder = ResourceOrIdArgument.getLootTable(context, "loot_table");
                                holder.unwrapKey().ifPresent(key -> {
                                    ItemStack itemStack = Items.CHEST.getDefaultInstance();
                                    itemStack.set(DataComponents.CONTAINER_LOOT, new SeededContainerLoot(key, 0L));
                                    boolean success = player.getInventory().add(itemStack);
                                    if (!success) {
                                        player.drop(itemStack, false);
                                    }
                                });
                            } catch (Exception err) {
                                log.error("Error: ", err);
                                return 1;
                            }
                            return 0;
                        }));
    }
}
