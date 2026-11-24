package cc.thonly.reverie_dreams.entity.ai.work;

import cc.thonly.reverie_dreams.entity.ai.goal.work.NPCHoneycombHarvestingGoal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HoneySupplyTest {
    @BeforeAll static void bootstrap() { WorkTestItems.bootstrap(); }
    @Test void automaticSupplyAcceptsShearsWhenInputStorageHasNoBottles() {
        assertTrue(NPCHoneycombHarvestingGoal.acceptsTool("auto", false, new ItemStack(Items.SHEARS)));
        assertTrue(NPCHoneycombHarvestingGoal.acceptsTool("auto", false, new ItemStack(Items.GLASS_BOTTLE)));
        assertFalse(NPCHoneycombHarvestingGoal.acceptsTool("auto", false, new ItemStack(Items.DIAMOND)));
    }
    @Test void availableShearsWinWhileExplicitPreferencesNeverSubstituteProducts() {
        assertFalse(NPCHoneycombHarvestingGoal.acceptsTool("auto", true, new ItemStack(Items.GLASS_BOTTLE)));
        assertFalse(NPCHoneycombHarvestingGoal.acceptsTool("honeycomb", false, new ItemStack(Items.GLASS_BOTTLE)));
        assertFalse(NPCHoneycombHarvestingGoal.acceptsTool("honey", true, new ItemStack(Items.SHEARS)));
        assertTrue(NPCHoneycombHarvestingGoal.acceptsTool("honey", true, new ItemStack(Items.GLASS_BOTTLE)));
    }
}
