package cc.thonly.reverie_dreams.fabric.gametest;

import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import cc.thonly.reverie_dreams.gui.entity.NPCAgentGui;
import cc.thonly.reverie_dreams.gui.entity.NPCGui;
import cc.thonly.reverie_dreams.gui.entity.NPCStateGui;
import cc.thonly.reverie_dreams.gui.entity.NPCWorkGui;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiFunction;

@SuppressWarnings("UnstableApiUsage")
public final class NPCUiClientGameTest implements FabricClientGameTest {
    private static final int GUI_SCALE = 2;
    private static final int CONTAINER_WIDTH = 176;
    private static final int CONTAINER_HEIGHT = 222;

    @Override
    public void runTest(ClientGameTestContext context) {
        TestSingleplayerContext world = context.worldBuilder().create();
        try {
            context.getInput().resizeWindow(1280, 720);
            var reload = context.computeOnClient(client -> {
                client.options.guiScale().set(GUI_SCALE);
                client.options.renderDistance().set(4);
                client.options.hideGui = false;
                client.options.pauseOnLostFocus = false;
                client.getLanguageManager().setSelected("zh_cn");
                client.options.languageCode = "zh_cn";
                client.resizeGui();
                return client.reloadResourcePacks();
            });
            context.waitFor(client -> reload.isDone() && client.getOverlay() == null, 1200);
            reload.join();
            UUID npcId = world.getServer().computeOnServer(server -> {
                ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
                var level = server.overworld();
                var npc = Objects.requireNonNull(RDEntityTypes.NPC_SIMPLE_ENTITY.value().create(level, EntitySpawnReason.COMMAND));
                npc.setPos(player.getX(), player.getY(), player.getZ() + 2);
                npc.setNoAi(true);
                npc.setOwner(player);
                npc.setRoleType(BuiltInRegistryProviders.NPC_ROLE_TYPE.stream().findFirst().orElseThrow());
                npc.setCustomName(Component.literal("测试 NPC"));
                level.addFreshEntity(npc);
                return npc.getUUID();
            });
            context.waitTicks(110);

            openAndCapture(context, world, npcId, NPCGui::new, "npc-overview", "\ub000", false);
            openAndCapture(context, world, npcId, NPCWorkGui::new, "npc-work", "\ue004", true);
            openAndCapture(context, world, npcId, NPCStateGui::new, "npc-state", "\ue005", true);
            openAndCapture(context, world, npcId, NPCAgentGui::new, "npc-agent", "\ue006", false);
        } catch (IOException failure) {
            throw new AssertionError("Unable to save NPC UI screenshot", failure);
        } finally {
            closeWorld(context, world);
        }
        System.out.println("NPC_UI_CLIENT_GAMETEST_PASS: four themed pages and both 54-slot mode grids rendered");
    }

    private static void closeWorld(ClientGameTestContext context, TestSingleplayerContext world) {
        var server = world.getServer().computeOnServer(current -> current);
        try {
            context.runOnClient(client -> {
                // Keep Minecraft.disconnect from blocking on the game test's paused server thread.
                var serverField = Minecraft.class.getDeclaredField("singleplayerServer");
                serverField.setAccessible(true);
                serverField.set(client, null);
                var saving = Component.translatable("menu.savingLevel");
                client.level.disconnect(saving);
                client.disconnect(new GenericMessageScreen(saving), false);
            });
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError("Unable to detach the NPC UI test world", failure);
        }
        context.waitFor(client -> client.level == null, 1200);
        if (!server.isShutdown()) {
            world.getServer().runOnServer(current -> current.halt(false));
        }
        context.waitFor(client -> server.isShutdown(), 1200);
        context.setScreen(TitleScreen::new);
    }

    private static void openAndCapture(ClientGameTestContext context, TestSingleplayerContext world, UUID npcId,
                                       BiFunction<ServerPlayer, NPCSimpleEntity, SimpleGui> factory,
                                       String name, String glyph, boolean fullGrid) throws IOException {
        world.getServer().runOnServer(server -> {
            var player = server.getPlayerList().getPlayers().getFirst();
            var npc = (NPCSimpleEntity) Objects.requireNonNull(server.overworld().getEntity(npcId));
            if (!factory.apply(player, npc).open()) {
                throw new AssertionError(name + " could not open");
            }
        });
        context.waitFor(client -> client.screen instanceof ContainerScreen screen
                && screen.getTitle().getString().contains(glyph), 200);
        context.getInput().setCursorPos(20, 20);
        context.waitTicks(8);

        Path screenshot = context.takeScreenshot(name);
        String output = System.getProperty("reverie.npcUiClientTest.output");
        if (output != null) {
            Path screenshots = Files.createDirectories(Path.of(output, "screenshots"));
            Files.copy(screenshot, screenshots.resolve(name + ".png"), StandardCopyOption.REPLACE_EXISTING);
        }
        var image = ImageIO.read(screenshot.toFile());
        int left = (image.getWidth() / GUI_SCALE - CONTAINER_WIDTH) / 2 * GUI_SCALE;
        int top = (image.getHeight() / GUI_SCALE - CONTAINER_HEIGHT) / 2 * GUI_SCALE;
        int header = image.getRGB(left + 140 * GUI_SCALE, top + 10 * GUI_SCALE);
        int red = (header >> 16) & 255;
        int blue = header & 255;
        if (blue - red < 40) {
            throw new AssertionError(name + " theme is missing from the container header");
        }
        if (fullGrid) {
            assertFullGrid(image, left, top, name);
        }
    }

    private static void assertFullGrid(BufferedImage image, int left, int top, String name) {
        int firstX = left + 7 * GUI_SCALE;
        int firstY = top + 17 * GUI_SCALE;
        int farEdge = 17 * GUI_SCALE;
        int pitch = 18 * GUI_SCALE;
        int leadingColor = image.getRGB(firstX, firstY);
        int trailingColor = image.getRGB(firstX + farEdge, firstY + farEdge);
        if (leadingColor == trailingColor) {
            throw new AssertionError(name + " lacks a recessed frame in the first slot");
        }
        for (int row = 0; row < 6; row++) {
            for (int column = 0; column < 9; column++) {
                int x = firstX + column * pitch;
                int y = firstY + row * pitch;
                if (image.getRGB(x, y) != leadingColor || image.getRGB(x + farEdge, y + farEdge) != trailingColor) {
                    throw new AssertionError(name + " is missing the slot frame at index " + (row * 9 + column));
                }
            }
        }
    }
}
