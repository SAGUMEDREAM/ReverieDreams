package cc.thonly.reverie_dreams.client.renderer.entity.layer;

import cc.thonly.reverie_dreams.client.renderer.entity.state.NPCAvatarRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.SharedConstants;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwingAnimationType;
import net.minecraft.world.item.component.KineticWeapon;
import org.joml.Matrix4f;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class NPCItemInHandLayerTest {
    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        // Unit tests do not run the data reload that normally binds item components.
        if (!Items.IRON_SPEAR.builtInRegistryHolder().areComponentsBound()) {
            Items.IRON_SPEAR.builtInRegistryHolder().bindComponents(
                    DataComponentMap.builder().set(DataComponents.MAX_STACK_SIZE, 1).build());
        }
    }

    @Test
    void rendersBothHandsWithoutEnteringThePlayerAnimationItemLayer() {
        assertFalse(ItemInHandLayer.class.isAssignableFrom(NPCItemInHandLayer.class),
                "NPCs have no player animation manager and must bypass player-only item mixins");
        for (boolean slim : new boolean[]{false, true}) {
            for (boolean baby : new boolean[]{false, true}) {
                var state = stateWithItems();
                state.isBaby = baby;
                compareWithVanilla(state, slim, 2);
            }
        }
        var empty = new NPCAvatarRenderState();
        compareWithVanilla(empty, false, 0);
    }

    @Test
    void preservesSpearAttackAndItemUseTransformsForEitherDominantArm() {
        for (HumanoidArm arm : HumanoidArm.values()) {
            var state = stateWithItems();
            state.mainArm = arm;
            state.attackArm = arm;
            state.attackTime = 0.45F;
            state.swingAnimationType = SwingAnimationType.STAB;
            compareWithVanilla(state, false, 2);

            state.attackTime = 0.0F;
            state.isUsingItem = true;
            state.useItemHand = InteractionHand.MAIN_HAND;
            state.ticksUsingItem = 12;
            state.rightArmPose = HumanoidModel.ArmPose.SPEAR;
            state.leftArmPose = HumanoidModel.ArmPose.SPEAR;
            var spear = new ItemStack(Items.IRON_SPEAR);
            spear.set(DataComponents.KINETIC_WEAPON, new KineticWeapon(10, 10,
                    Optional.empty(), Optional.empty(), Optional.of(new KineticWeapon.Condition(200, 0, 0)),
                    0.5F, 1.0F, Optional.empty(), Optional.empty()));
            state.rightHandItemStack = spear;
            state.leftHandItemStack = spear;
            compareWithVanilla(state, true, 2);
        }
    }

    @Test
    void preservesSpyglassPlacementAndRestoresHeadRotationAndPoseStack() {
        for (HumanoidArm arm : HumanoidArm.values()) {
            for (InteractionHand hand : InteractionHand.values()) {
                var state = stateWithItems();
                state.mainArm = arm;
                state.isUsingItem = true;
                state.useItemHand = hand;
                state.heldOnHead.newLayer();
                compareWithVanilla(state, false, 2);
            }
        }
    }

    private static NPCAvatarRenderState stateWithItems() {
        var state = new NPCAvatarRenderState();
        state.entityType = EntityType.MANNEQUIN;
        state.outlineColor = 0xFF123456;
        state.rightHandItemState.newLayer();
        state.leftHandItemState.newLayer();
        return state;
    }

    private static void compareWithVanilla(NPCAvatarRenderState state, boolean slim, int expectedDraws) {
        var model = new PlayerModel(LayerDefinition.create(
                PlayerModel.createMesh(CubeDeformation.NONE, slim), 64, 64).bakeRoot(), slim);
        model.rightArm.xRot = 0.35F;
        model.leftArm.zRot = -0.2F;
        model.head.xRot = 2.0F;
        var expected = capture(new PlayerItemInHandLayer<>(() -> model), state);
        var actual = capture(new NPCItemInHandLayer(() -> model), state);
        assertEquals(2.0F, model.head.xRot, "Rendering must restore the head pose");
        assertEquals(expectedDraws, actual.size());
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertTrue(expected.get(i).pose.equals(actual.get(i).pose, 1.0E-6F), "Item transform " + i);
            assertEquals(expected.get(i).parameters, actual.get(i).parameters);
        }
    }

    private static List<Draw> capture(RenderLayer<AvatarRenderState, PlayerModel> layer, AvatarRenderState state) {
        var draws = new ArrayList<Draw>();
        var collector = (SubmitNodeCollector) Proxy.newProxyInstance(
                SubmitNodeCollector.class.getClassLoader(), new Class<?>[]{SubmitNodeCollector.class},
                (proxy, method, args) -> {
                    assertEquals("submitItem", method.getName());
                    draws.add(new Draw(new Matrix4f(((PoseStack) args[0]).last().pose()),
                            List.of(args[1], args[2], args[3], args[4], args[7])));
                    return null;
                });
        var poseStack = new PoseStack();
        poseStack.translate(1.25F, -0.5F, 3.0F);
        var initial = new Matrix4f(poseStack.last().pose());
        layer.submit(poseStack, collector, 0xF000F0, state, 0, 0);
        assertEquals(initial, poseStack.last().pose());
        assertTrue(poseStack.isEmpty(), "Rendering must balance pushPose/popPose");
        return draws;
    }

    private record Draw(Matrix4f pose, List<Object> parameters) {
    }
}
