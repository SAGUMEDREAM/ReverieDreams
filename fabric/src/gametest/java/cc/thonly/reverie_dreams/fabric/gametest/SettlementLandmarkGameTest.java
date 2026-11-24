package cc.thonly.reverie_dreams.fabric.gametest;

import cc.thonly.reverie_dreams.entity.HumanEntity;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.world.settlement.SettlementLandmarkIntegration;
import cc.thonly.reverie_dreams.world.settlement.SettlementPilotData;
import net.fabricmc.fabric.api.gametest.v1.CustomTestMethodInvoker;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

import java.lang.reflect.Method;

public final class SettlementLandmarkGameTest implements CustomTestMethodInvoker {
    @Override
    public void invokeTestMethod(GameTestHelper helper, Method method) throws ReflectiveOperationException {
        method.invoke(this, helper);
    }

    private static final Identifier HUMAN_VILLAGE = Identifier.fromNamespaceAndPath("reverie_dreams", "human_village");
    private static final Identifier UNSUPPORTED_LANDMARK = id("unsupported_landmark");
    @GameTest(setupTicks = 10, maxTicks = 200)
    public void landmarkInstallAndEnrollOnlyAcceptRealGensokyoLandmarks(GameTestHelper helper) {
        prepareFloor(helper, 8);

        ServerLevel overworld = helper.getLevel();
        BlockPos overworldPost = helper.absolutePos(new BlockPos(3, 1, 3));
        BlockPos overworldSecond = helper.absolutePos(new BlockPos(5, 1, 5));
        HumanEntity overworldResident = helper.spawn(RDEntityTypes.HUMAN.get(), new BlockPos(2, 1, 2));

        helper.assertFalse(SettlementLandmarkIntegration.install(overworld, HUMAN_VILLAGE, overworldPost, overworldSecond),
                "a non-Gensokyo level cannot install the human village");
        helper.assertFalse(SettlementLandmarkIntegration.install(overworld, UNSUPPORTED_LANDMARK, overworldPost, overworldSecond),
                "a non-Gensokyo level cannot install an unsupported landmark");
        helper.assertFalse(SettlementLandmarkIntegration.enroll(overworld, HUMAN_VILLAGE, overworldResident, null),
                "a non-Gensokyo level cannot enroll a human village resident");
        helper.assertFalse(SettlementLandmarkIntegration.enroll(overworld, HUMAN_VILLAGE, overworldResident, null, true),
                "a non-Gensokyo level cannot enroll a human village trader");
        helper.assertTrue(SettlementPilotData.getOrCreate(overworld).layout(HUMAN_VILLAGE) == null,
                "the rejected overworld calls leave no human village layout");

        helper.succeed();
    }

    private static void prepareFloor(GameTestHelper helper, int size) {
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
            }
        }
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("reverie_dreams_board_client_test", path);
    }
}
