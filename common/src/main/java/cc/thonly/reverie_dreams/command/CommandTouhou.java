package cc.thonly.reverie_dreams.command;

import cc.thonly.reverie_dreams.command.service.AboutCommandService;
import cc.thonly.reverie_dreams.command.service.CacheSkinsCommandService;
import cc.thonly.reverie_dreams.command.service.CastPatternCommandService;
import cc.thonly.reverie_dreams.command.service.DreamGridCommandService;
import cc.thonly.reverie_dreams.command.service.GetPatternCardCommandService;
import cc.thonly.reverie_dreams.command.service.GetSpellcardWithConfigCommandService;
import cc.thonly.reverie_dreams.command.service.HelpCommandService;
import cc.thonly.reverie_dreams.command.service.LoadRootGuidePageCommandService;
import cc.thonly.reverie_dreams.command.service.ParseMidiCommandService;
import cc.thonly.reverie_dreams.command.service.RecipeCommandService;
import cc.thonly.reverie_dreams.command.service.RegistryCommandService;
import cc.thonly.reverie_dreams.command.service.RegistryTagCommandService;
import cc.thonly.reverie_dreams.command.service.ReloadConfigCommandService;
import cc.thonly.reverie_dreams.command.service.SetMusicCommandService;
import cc.thonly.reverie_dreams.command.service.SettlementCommandService;
import cc.thonly.reverie_dreams.command.service.TeleportLandmarkCommandService;
import cc.thonly.reverie_dreams.command.service.TouhouRootCommandService;
import cc.thonly.reverie_dreams.command.service.WithBeveragePropertyCommandService;
import cc.thonly.reverie_dreams.command.service.WithFoodPropertyCommandService;
import cc.thonly.reverie_dreams.command.service.debug.DebugCraftEngineCommandService;
import cc.thonly.reverie_dreams.command.service.debug.DebugLootChestCommandService;
import cc.thonly.reverie_dreams.command.service.debug.DebugRecipeBlockCommandService;
import cc.thonly.reverie_dreams.command.service.debug.DebugRestaurantCommandService;
import cc.thonly.reverie_dreams.command.service.debug.DebugResetItemCooldownCommandService;
import cc.thonly.reverie_dreams.command.service.debug.DebugTestCommandService;
import cc.thonly.reverie_dreams.command.service.settings.SettingsCommandService;
import cc.thonly.reverie_dreams.command.service.video.VideoCommandService;
import cc.thonly.reverie_dreams.util.PlatformContext;
import cc.thonly.reverie_dreams.world.gensokyo.GensokyoFeature;
import cc.thonly.reverie_dreams.world.gensokyo.GensokyoRegenerator;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.architectury.platform.Platform;
import lombok.NoArgsConstructor;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;

@NoArgsConstructor
public class CommandTouhou {

    public LiteralArgumentBuilder<CommandSourceStack> makeInstance(
            CommandDispatcher<CommandSourceStack> dispatcher,
            CommandBuildContext registryAccess
    ) {
        var root = new TouhouRootCommandService().build();
        root.then(new AboutCommandService().build());
        if (PlatformContext.hasPolymer()) {
            root.then(new CacheSkinsCommandService().build());
        }
        root.then(new GetSpellcardWithConfigCommandService().build());
        root.then(new GetPatternCardCommandService().build());
        root.then(new CastPatternCommandService().build());
        root.then(new DreamGridCommandService().build());
        root.then(new HelpCommandService().build());
        root.then(new LoadRootGuidePageCommandService().build());
        root.then(new ParseMidiCommandService().build());
        root.then(new RecipeCommandService().build());
        root.then(new RegistryCommandService().build());
        root.then(new RegistryTagCommandService().build());
        root.then(new ReloadConfigCommandService().build());
        root.then(new SetMusicCommandService().build());
        root.then(new SettingsCommandService().build());
        root.then(new VideoCommandService().build());
        root.then(new WithBeveragePropertyCommandService().build());
        root.then(new WithFoodPropertyCommandService().build());
        if (GensokyoFeature.isEnabled()) {
            root.then(new SettlementCommandService(registryAccess).build());
            root.then(GensokyoRegenerator.command());
            root.then(new TeleportLandmarkCommandService().build());
        }
        if (PlatformContext.isDevMode()) {
            root.then(new DebugLootChestCommandService(registryAccess).build());
            root.then(new DebugRecipeBlockCommandService().build());
            root.then(new DebugResetItemCooldownCommandService().build());
            root.then(new DebugCraftEngineCommandService().build());
            if (PlatformContext.isDevModeByIDEA()) {
                root.then(new DebugTestCommandService().build());
            }
        }
        if (Platform.isDevelopmentEnvironment()) {
            root.then(new DebugRestaurantCommandService().build());
        }

        return root;
    }
}
