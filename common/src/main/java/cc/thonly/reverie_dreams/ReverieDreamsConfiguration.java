package cc.thonly.reverie_dreams;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.resources.Identifier;

@SuppressWarnings("SpellCheckingInspection")
@Config(name = ReverieDreams.MOD_ID)
public class ReverieDreamsConfiguration implements ConfigData {
    // AutoConfig groups fields before it filters hidden entries.
    @ConfigEntry.Category("gameplay")
    @ConfigEntry.Gui.Excluded
    public int configVersion = 1;

    /** Server-side policy; manual structure commands remain available when automatic upgrades are off. */
    @ConfigEntry.Gui.Excluded
    public boolean gensokyoStructureAutoUpgrade = false;
    @ConfigEntry.Gui.Excluded
    public int gensokyoStructureUpdateBudgetMs = 2;
    @ConfigEntry.Gui.Excluded
    public int gensokyoStructureUpdateMaxChunks = 1024;
    @ConfigEntry.Gui.Excluded
    public int gensokyoStructureUnloadTimeoutTicks = 1200;
    @ConfigEntry.Gui.Excluded
    public java.util.List<String> gensokyoStructureUpdateExcluded = new java.util.ArrayList<>();

    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Category("gameplay")
    @ConfigEntry.Gui.Tooltip
    public boolean enableTimeStoppingMultiplayer = true;

    @ConfigEntry.Category("gameplay")
    @ConfigEntry.Gui.Tooltip
    public int maxUpgradedHealthValue = 1024;

    @ConfigEntry.Category("spawning")
    @ConfigEntry.Gui.Tooltip
    public boolean enableYouseiSpawn = true;

    @ConfigEntry.Category("spawning")
    @ConfigEntry.Gui.Tooltip
    public boolean enableGhostSpawn = false;

    @ConfigEntry.Category("spawning")
    @ConfigEntry.Gui.Tooltip
    public boolean enableAquaticSpawn = true;

    @ConfigEntry.Category("spawning")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 0, max = 16)
    public int aquaticLocalPopulationLimit = 6;

    @ConfigEntry.Category("npc_logistics")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 1, max = 16)
    public int npcWorkRange = 8;

    @ConfigEntry.Category("npc_logistics")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 1, max = 8)
    public int npcWorkVerticalRange = 8;

    @ConfigEntry.Category("npc_logistics")
    @ConfigEntry.Gui.Tooltip
    public boolean npcWorkAutoLogistics = true;

    @ConfigEntry.Category("npc_logistics")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 1, max = 18)
    public int npcWorkUnloadThreshold = 14;

    @ConfigEntry.Category("npc_logistics")
    @ConfigEntry.Gui.Tooltip
    public boolean chestSortClaimEmptyChests = true;

    @ConfigEntry.Category("npc_logistics")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 0, max = 2304)
    public int chestSortReserveFood = 16;

    @ConfigEntry.Category("npc_logistics")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 0, max = 2304)
    public int chestSortReserveTools = 1;

    @ConfigEntry.Category("npc_logistics")
    @ConfigEntry.Gui.Tooltip
    public List<String> chestSortCategoryTags = new ArrayList<>(List.of(
            "minecraft:logs", "minecraft:planks", "minecraft:wool", "minecraft:saplings",
            "minecraft:coals", "c:ores", "c:ingots", "c:nuggets", "c:gems"));

    @ConfigEntry.Category("npc_logistics")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 2, max = 128)
    public int npcBreedMaxAnimals = 32;

    @ConfigEntry.Category("npc_logistics")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 0, max = 64)
    public int npcBreedFoodReserve = 2;

    @ConfigEntry.Category("npc_logistics")
    @ConfigEntry.Gui.Tooltip
    public String npcHoneyProduct = "auto";

    @ConfigEntry.Category("npc_logistics")
    @ConfigEntry.Gui.Tooltip
    public boolean npcHoneyRequireSmoke = true;

    @ConfigEntry.Category("npc_logistics")
    @ConfigEntry.Gui.Tooltip
    public List<String> npcMusicPlaylist = new ArrayList<>();

    @ConfigEntry.Category("npc_logistics")
    @ConfigEntry.Gui.Tooltip
    public boolean npcMusicLoop = true;

    @ConfigEntry.Category("npc_logistics")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
    public int npcMusicVolume = 100;

    @ConfigEntry.Category("npc_logistics")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 8, max = 128)
    public int npcMusicRange = 64;

    @ConfigEntry.Category("npc_logistics")
    @ConfigEntry.Gui.Tooltip
    public boolean npcCombatPatrol = true;

    @ConfigEntry.Category("npc_logistics")
    @ConfigEntry.Gui.Tooltip
    public String npcCombatTargetPriority = "owner_threat";

    @ConfigEntry.Category("restaurant")
    public boolean enableManualCustomerMode = false;

    @ConfigEntry.Category("restaurant")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 1, max = 4096)
    public int maxCustomerTickTime = 20 * 60;

    @ConfigEntry.Category("restaurant")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 0, max = 4096)
    public int maxCustomerCooldownTickTime = 20 * 20;

    @ConfigEntry.Category("restaurant")
    @ConfigEntry.Gui.Excluded
    @Deprecated
    public boolean npcCustomerResetTimerPerOrder = false;

    @ConfigEntry.Category("ai")
    @ConfigEntry.Gui.Tooltip
    public boolean enableAIReplacesGeneralChat = false;

    @ConfigEntry.Category("ai")
    @ConfigEntry.Gui.Tooltip
    public String apiUrl = "https://api.deepseek.com/v1/chat/completions";

    @ConfigEntry.Category("ai")
    @ConfigEntry.Gui.Tooltip
    public String apiKey = "";

    @ConfigEntry.Category("ai")
    @ConfigEntry.Gui.Tooltip
    public String aiProxyUrl = "";

    @ConfigEntry.Category("ai")
    @ConfigEntry.Gui.Tooltip
    public String model = "deepseek-chat";

    @ConfigEntry.Category("ai")
    @ConfigEntry.Gui.Tooltip
    public String chatType = "deepseek";

    @ConfigEntry.Category("ai")
    @ConfigEntry.Gui.Tooltip
    public int maxChatHistory = 40;

    @ConfigEntry.Category("ai")
    @ConfigEntry.Gui.Tooltip
    public int maxTokens = 512;

    @ConfigEntry.Category("ai")
    @ConfigEntry.Gui.Tooltip
    public double temperature = 0.7;

    @ConfigEntry.Category("ai")
    @ConfigEntry.Gui.Tooltip
    public int aiDailyRequests = 200;

    @ConfigEntry.Category("ai")
    @ConfigEntry.Gui.Tooltip
    public int aiPlayerDailyRequests = 40;

    @ConfigEntry.Category("ai")
    @ConfigEntry.Gui.Tooltip
    public int aiBackgroundDailyRequests = 20;

    @ConfigEntry.Category("ai")
    @ConfigEntry.Gui.Tooltip
    public int aiDailyTokenBudget = 500000;

    @ConfigEntry.Category("ai")
    @ConfigEntry.Gui.Tooltip
    public int aiPlayerDailyTokenBudget = 100000;

    @ConfigEntry.Category("advanced")
    @ConfigEntry.Gui.Tooltip
    public boolean checkUpdate = true;

    @ConfigEntry.Category("advanced")
    @ConfigEntry.Gui.Tooltip
    public boolean debugMode = false;

    /** Optional startup settings. An omitted value follows the loader's development environment. */
    @ConfigEntry.Category("advanced")
    @ConfigEntry.Gui.Excluded
    public Boolean enableVanillaWoolAliases;

    @ConfigEntry.Category("advanced")
    @ConfigEntry.Gui.Excluded
    public Boolean enableVanillaPoplarAliases;

    @ConfigEntry.Category("advanced")
    @ConfigEntry.Gui.Tooltip
    public boolean reportResourceReloadError = true;

    /** Changes the writer after restart; existing Zstd chunks remain readable when disabled. */
    @ConfigEntry.Category("advanced")
    @ConfigEntry.Gui.Tooltip
    public boolean enableGensokyoChunkZstd = false;

    @ConfigEntry.Category("advanced")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 1, max = 19)
    public int gensokyoChunkZstdLevel = 8;

    @ConfigEntry.Category("advanced")
    @ConfigEntry.Gui.Tooltip
    public boolean enableDanmakuGlow = true;

    @Override
    public void validatePostLoad() throws ValidationException {
        if (npcWorkRange < 1 || npcWorkRange > 16 || npcWorkVerticalRange < 1 || npcWorkVerticalRange > 8
                || npcWorkUnloadThreshold < 1 || npcWorkUnloadThreshold > 18 || npcBreedMaxAnimals < 2 || npcBreedMaxAnimals > 128
                || npcBreedFoodReserve < 0 || npcBreedFoodReserve > 64 || npcMusicVolume < 0 || npcMusicVolume > 100
                || npcMusicRange < 8 || npcMusicRange > 128) throw new ValidationException("NPC work configuration is outside its supported range");
        if (!Set.of("auto", "honey", "honeycomb").contains(npcHoneyProduct == null ? "" : npcHoneyProduct)
                || !Set.of("nearest", "owner_threat", "npc_threat").contains(npcCombatTargetPriority == null ? "" : npcCombatTargetPriority))
            throw new ValidationException("Unknown NPC work policy");
        if (npcMusicPlaylist == null || npcMusicPlaylist.size() > 64 || npcMusicPlaylist.stream().anyMatch(s -> s == null
                || s.contains("/") || s.contains("\\") || s.contains("..") || !s.toLowerCase(Locale.ROOT).endsWith(".nbs")))
            throw new ValidationException("NPC music playlist must contain at most 64 local NBS filenames");
        if (this.maxCustomerTickTime < 1) {
            throw new ValidationException("maxCustomerTickTime must be at least 1 tick");
        }
        if (this.maxCustomerCooldownTickTime < 0) {
            throw new ValidationException("maxCustomerCooldownTickTime must be at least 0 ticks");
        }
        if (this.chestSortReserveFood < 0 || this.chestSortReserveFood > 2304
                || this.chestSortReserveTools < 0 || this.chestSortReserveTools > 2304) {
            throw new ValidationException("Chest sorting reserves must be between 0 and 2304");
        }
        if (this.chestSortCategoryTags == null || this.chestSortCategoryTags.size() > 64
                || this.chestSortCategoryTags.stream().anyMatch(tag -> tag == null
                || Identifier.tryParse(tag) == null)) {
            throw new ValidationException("chestSortCategoryTags must contain at most 64 valid tag identifiers");
        }
    }
}
