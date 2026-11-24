package cc.thonly.reverie_dreams;

import com.google.gson.Gson;
import me.shedaniel.autoconfig.ConfigData;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReverieDreamsConfigurationTest {
    @Test void validatesWorkBoundsMusicFilesAndExplicitPolicies() throws Exception {
        var config = new ReverieDreamsConfiguration();
        assertFalse(config.npcCustomerResetTimerPerOrder);
        assertEquals(8, config.npcWorkRange);
        config.npcMusicPlaylist = List.of("test.nbs"); config.validatePostLoad();
        config.npcMusicPlaylist = List.of("../test.nbs");
        assertThrows(ConfigData.ValidationException.class, config::validatePostLoad);
        config.npcMusicPlaylist = List.of(); config.npcWorkRange = 17;
        assertThrows(ConfigData.ValidationException.class, config::validatePostLoad);
        config.npcWorkRange = 16; config.npcHoneyProduct = "anything";
        assertThrows(ConfigData.ValidationException.class, config::validatePostLoad);
        config.npcHoneyProduct = "honey"; config.npcCombatTargetPriority = "nearest"; config.validatePostLoad();
    }
    @Test void validatesCustomerTimersWithoutRejectingAnImmediateMealEnd() throws Exception {
        var config = new ReverieDreamsConfiguration();
        config.validatePostLoad();
        config.maxCustomerCooldownTickTime = 0;
        config.maxCustomerTickTime = Integer.MAX_VALUE;
        config.validatePostLoad();
        config.maxCustomerTickTime = 0;
        assertThrows(ConfigData.ValidationException.class, config::validatePostLoad);
        config.maxCustomerTickTime = 1200;
        config.maxCustomerCooldownTickTime = -1;
        assertThrows(ConfigData.ValidationException.class, config::validatePostLoad);
    }

    @Test void legacyNullReportFlagKeepsItsSafePrimitiveDefault() {
        var config = new Gson().fromJson("{\"reportResourceReloadError\":null}", ReverieDreamsConfiguration.class);
        assertTrue(config.reportResourceReloadError);
        assertFalse(config.debugMode);
    }

    @Test void validatesSortingReservesAndCategoryAllowlist() throws Exception {
        var config = new ReverieDreamsConfiguration();
        config.chestSortReserveFood = 0;
        config.chestSortReserveTools = 2304;
        config.chestSortCategoryTags = List.of("minecraft:logs", "c:ores");
        config.validatePostLoad();
        config.chestSortReserveFood = -1;
        assertThrows(ConfigData.ValidationException.class, config::validatePostLoad);
        config.chestSortReserveFood = 16;
        config.chestSortCategoryTags = List.of("Invalid Tag");
        assertThrows(ConfigData.ValidationException.class, config::validatePostLoad);
        config.chestSortCategoryTags = null;
        assertThrows(ConfigData.ValidationException.class, config::validatePostLoad);
    }
}
