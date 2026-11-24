package cc.thonly.reverie_dreams.fabric.gametest;

import cc.thonly.reverie_dreams.data.npc.NPCState;
import cc.thonly.reverie_dreams.data.npc.NPCWorkMode;
import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import cc.thonly.reverie_dreams.gui.entity.NPCAgentGui;
import cc.thonly.reverie_dreams.gui.entity.NPCGui;
import cc.thonly.reverie_dreams.gui.entity.NPCStateGui;
import cc.thonly.reverie_dreams.gui.entity.NPCWorkGui;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.content.NPCStates;
import cc.thonly.reverie_dreams.registry.content.NPCWorkModes;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.stream.IntStream;

public final class NPCUiGameTest {
    private static final int CHEST_MENU_SIZE = 54;
    private static final int TOTAL_MENU_SIZE = 90;

    @GameTest(setupTicks = 1, maxTicks = 100)
    public void npcBlueUiPagesOpenWithExpectedGlyphs(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.snapTo(helper.absolutePos(new BlockPos(2, 1, 2)).getCenter());
        NPCSimpleEntity npc = helper.spawn(RDEntityTypes.NPC_SIMPLE_ENTITY.value(), new BlockPos(2, 1, 4));
        npc.setOwner(player);
        npc.setRoleType(BuiltInRegistryProviders.NPC_ROLE_TYPE.stream().findFirst().orElseThrow());

        var workModes = BuiltInRegistryProviders.NPC_WORK_MODE;
        var states = BuiltInRegistryProviders.NPC_STATE;
        int firstWorkSlot = Math.toIntExact(workModes.stream().filter(NPCWorkModes::canAssign).count());
        int firstStateSlot = states.size();
        List<Identifier> testModes = IntStream.range(0, 3)
                .mapToObj(index -> Identifier.fromNamespaceAndPath("reverie_dreams_test", "work_mode_" + index))
                .toList();
        List<Identifier> testStates = IntStream.range(0, 4)
                .mapToObj(index -> Identifier.fromNamespaceAndPath("reverie_dreams_test", "state_" + index))
                .toList();

        try {
            for (Identifier id : testModes) {
                NPCWorkModes.register(id, new NPCWorkMode(id.getPath(), Items.AMETHYST_SHARD));
            }
            for (Identifier id : testStates) {
                NPCStates.register(id, new NPCState(id.getPath()).withIcon(Items.ECHO_SHARD));
            }
            assertPage(helper, player, new NPCGui(player, npc), "\ub000", "overview");
            assertPage(helper, player, new NPCWorkGui(player, npc), "\ue004", "work");
            helper.assertTrue(player.containerMenu.getSlot(firstWorkSlot + testModes.size() - 1).getItem().is(Items.AMETHYST_SHARD),
                    "additional work registrations occupy later container slots");
            assertPage(helper, player, new NPCStateGui(player, npc), "\ue005", "state");
            helper.assertTrue(player.containerMenu.getSlot(firstStateSlot + testStates.size() - 1).getItem().is(Items.ECHO_SHARD),
                    "additional state registrations occupy later container slots");
            assertPage(helper, player, new NPCAgentGui(player, npc), "\ue006", "agent");
        } finally {
            player.closeContainer();
            npc.remove(Entity.RemovalReason.DISCARDED);
            for (Identifier id : testModes.reversed()) {
                workModes.unregister(workModes.createKey(id));
            }
            for (Identifier id : testStates.reversed()) {
                states.unregister(states.createKey(id));
            }
        }
        helper.succeed();
    }

    private static void assertPage(GameTestHelper helper, ServerPlayer player, SimpleGui gui, String glyph, String page) {
        helper.assertTrue(gui.open(), page + " page opens");
        AbstractContainerMenu menu = player.containerMenu;
        helper.assertTrue(menu != player.inventoryMenu, page + " page replaces the player inventory menu");
        helper.assertTrue(gui.getVirtualSize() == CHEST_MENU_SIZE, page + " page keeps a 9x6 virtual container size");
        helper.assertTrue(menu.slots.size() == TOTAL_MENU_SIZE, page + " page includes the player inventory below the 9x6 panel");
        helper.assertTrue(gui.getTitle().getString().contains(glyph), page + " page uses the expected background glyph");
    }
}
