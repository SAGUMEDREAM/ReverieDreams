package cc.thonly.reverie_dreams.item.prop;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.SharedConstants;
import net.minecraft.client.resources.model.cuboid.CuboidModel;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.level.GameType;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TenguShieldTest {
    private static Unsafe unsafe;
    private static TenguShieldItem shield;
    private static Holder<Item> shieldHolder;
    private static CuboidModel idle, blocking;

    @BeforeAll static void bootstrap() throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        var field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        unsafe = (Unsafe) field.get(null);
        shield = (TenguShieldItem) unsafe.allocateInstance(TenguShieldItem.class);
        // Supply vanilla blocking data without initializing every loader-owned mod registry.
        var block = new BlocksAttacks(0.25f, 1, List.of(new BlocksAttacks.DamageReduction(90, Optional.empty(), 0, 1)),
                new BlocksAttacks.ItemDamageFunction(3, 1, 1), Optional.empty(), Optional.empty(), Optional.empty());
        shieldHolder = Holder.direct(shield, DataComponentMap.builder().set(DataComponents.MAX_STACK_SIZE, 1)
                .set(DataComponents.MAX_DAMAGE, 600).set(DataComponents.DAMAGE, 0).set(DataComponents.BLOCKS_ATTACKS, block).build());
        idle = parse("tengu_shield");
        blocking = parse("tengu_shield_blocking");
    }

    @Test void bothHandsUseTheVanillaBlockingLifecycleAndFiveTickDelay() throws Exception {
        for (var hand : InteractionHand.values()) {
            var player = player();
            player.stack = new ItemStack(shieldHolder);
            assertEquals(InteractionResult.CONSUME, shield.use(null, player, hand));
            assertEquals(hand, player.hand);
            player.elapsed(4);
            assertNull(player.getItemBlockingWith());
            player.elapsed(5);
            assertSame(player.stack, player.getItemBlockingWith());
            player.using = false;
            assertNull(player.getItemBlockingWith());
        }
    }

    @Test void removingTheBlockingComponentAlsoRemovesItsAnimationAndUseDuration() throws Exception {
        var player = player();
        player.stack = new ItemStack(shieldHolder);
        assertEquals(ItemUseAnimation.BLOCK, shield.getUseAnimation(player.stack));
        assertEquals(72000, shield.getUseDuration(player.stack, player));
        player.stack.remove(DataComponents.BLOCKS_ATTACKS);
        assertEquals(ItemUseAnimation.NONE, shield.getUseAnimation(player.stack));
        assertEquals(0, shield.getUseDuration(player.stack, player));
        assertEquals(InteractionResult.PASS, shield.use(null, player, InteractionHand.MAIN_HAND));
        assertFalse(player.using);
    }

    @Test void guiAndFixedTransformsKeepTheDiamondWithinTheItemSlot() throws Exception {
        for (var context : List.of(ItemDisplayContext.GUI, ItemDisplayContext.FIXED)) {
            Matrix4f matrix = matrix(idle, context);
            for (var point : vertices()) {
                Vector3f projected = matrix.transformPosition(new Vector3f(point));
                assertTrue(Math.abs(projected.x) < 0.5, context + " clips horizontally");
                assertTrue(Math.abs(projected.y) < 0.5, context + " clips vertically");
            }
        }
    }

    @Test void firstPersonPosesKeepTheShieldAheadOfTheCameraAndTheCrosshairClear() throws Exception {
        for (var model : List.of(idle, blocking)) {
            for (var context : List.of(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, ItemDisplayContext.FIRST_PERSON_LEFT_HAND)) {
                boolean left = context.leftHand();
                Matrix4f matrix = new Matrix4f().translation(left ? -0.56f : 0.56f, -0.52f, -0.72f).mul(matrix(model, context));
                for (var point : vertices()) {
                    Vector3f projected = matrix.transformPosition(new Vector3f(point));
                    assertTrue(projected.z < -0.25, "Shield crosses the camera/near plane");
                }
                assertFalse(intersectsViewRay(matrix), context + " covers the crosshair");
            }
        }
    }

    @Test void leftAndRightHandTransformsMirrorEachOther() {
        for (var model : List.of(idle, blocking)) {
            for (var contexts : List.of(List.of(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, ItemDisplayContext.FIRST_PERSON_LEFT_HAND),
                    List.of(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, ItemDisplayContext.THIRD_PERSON_LEFT_HAND))) {
                var right = matrix(model, contexts.get(0)).transformPosition(new Vector3f(.4f, .6f, .7f));
                var left = matrix(model, contexts.get(1)).transformPosition(new Vector3f(.6f, .6f, .7f));
                assertEquals(-right.x, left.x, 0.000001);
                assertEquals(right.y, left.y, 0.000001);
                assertEquals(right.z, left.z, 0.000001);
            }
        }
    }

    @Test void changingToTheBlockingPoseKeepsTheThirdPersonGripAtTheHand() {
        for (var context : List.of(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, ItemDisplayContext.THIRD_PERSON_LEFT_HAND)) {
            var normalGrip = matrix(idle, context).transformPosition(new Vector3f(.5f, .5f, 8.5f / 16));
            var raisedGrip = matrix(blocking, context).transformPosition(new Vector3f(.5f, .5f, 8.5f / 16));
            assertTrue(normalGrip.distance(raisedGrip) < 1f / 16, "Grip jumps away from the hand");
        }
    }

    @Test void thirdPersonBlockingFacesForwardWithTheVanillaRaisedArmPose() {
        for (var context : List.of(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, ItemDisplayContext.THIRD_PERSON_LEFT_HAND)) {
            boolean left = context.leftHand();
            Matrix4f arm = new Matrix4f().rotateY((left ? 30 : -30) * (float) Math.PI / 180).rotateX(-0.9424779f)
                    .rotateX(-(float) Math.PI / 2).rotateY((float) Math.PI).mul(matrix(blocking, context));
            var facing = arm.transformDirection(new Vector3f(0, 0, -1)).normalize();
            assertTrue(facing.z < -0.95, "Shield faces sideways instead of forward");
        }
    }

    @Test void blockingUsesTheSameGeometryAndInventoryPresentationAsIdle() throws Exception {
        assertEquals(Identifier.parse("reverie_dreams:item/tengu_shield"), blocking.parent());
        assertNull(blocking.geometry());
        assertSame(ItemTransform.NO_TRANSFORM, blocking.transforms().getTransform(ItemDisplayContext.GUI));
        var item = json("assets/reverie_dreams/items/tengu_shield.json").getAsJsonObject("model");
        assertEquals("minecraft:using_item", item.get("property").getAsString());
        assertEquals("reverie_dreams:item/tengu_shield", item.getAsJsonObject("on_false").get("model").getAsString());
        assertEquals("reverie_dreams:item/tengu_shield_blocking", item.getAsJsonObject("on_true").get("model").getAsString());
    }

    private static Matrix4f matrix(CuboidModel model, ItemDisplayContext context) {
        var pose = new PoseStack();
        model.transforms().getTransform(context).apply(context.leftHand(), pose.last());
        return new Matrix4f(pose.last().pose());
    }

    private static List<Vector3f> vertices() throws Exception {
        List<Vector3f> points = new ArrayList<>();
        for (var entry : json("assets/reverie_dreams/models/item/tengu_shield.json").getAsJsonArray("elements")) {
            var e = entry.getAsJsonObject();
            var from = vector(e, "from");
            var to = vector(e, "to");
            Matrix4f rotation = elementRotation(e);
            for (int x = 0; x < 2; x++) for (int y = 0; y < 2; y++) for (int z = 0; z < 2; z++) {
                var point = new Vector3f(x == 0 ? from.x : to.x, y == 0 ? from.y : to.y, z == 0 ? from.z : to.z);
                points.add(rotation.transformPosition(point).div(16));
            }
        }
        return points;
    }

    private static boolean intersectsViewRay(Matrix4f model) throws Exception {
        for (var entry : json("assets/reverie_dreams/models/item/tengu_shield.json").getAsJsonArray("elements")) {
            var e = entry.getAsJsonObject();
            Matrix4f inverse = new Matrix4f(model).scale(1f / 16).mul(elementRotation(e)).invert();
            var origin = inverse.transformPosition(new Vector3f());
            var direction = inverse.transformDirection(new Vector3f(0, 0, -1));
            var from = vector(e, "from"); var to = vector(e, "to");
            float near = 0, far = Float.POSITIVE_INFINITY;
            for (int axis = 0; axis < 3; axis++) {
                float d = direction.get(axis), o = origin.get(axis);
                if (Math.abs(d) < .000001) {
                    if (o < from.get(axis) || o > to.get(axis)) { far = -1; break; }
                } else {
                    float a = (from.get(axis) - o) / d, b = (to.get(axis) - o) / d;
                    near = Math.max(near, Math.min(a, b)); far = Math.min(far, Math.max(a, b));
                }
            }
            if (far >= near) return true;
        }
        return false;
    }

    private static Matrix4f elementRotation(JsonObject element) {
        var transform = new Matrix4f();
        if (element.has("rotation")) {
            var r = element.getAsJsonObject("rotation"); var origin = vector(r, "origin");
            float angle = r.get("angle").getAsFloat() * (float) Math.PI / 180;
            transform.translate(origin);
            switch (r.get("axis").getAsString()) {
                case "x" -> transform.rotateX(angle);
                case "y" -> transform.rotateY(angle);
                case "z" -> transform.rotateZ(angle);
            }
            transform.translate(-origin.x, -origin.y, -origin.z);
        }
        return transform;
    }

    private static Vector3f vector(JsonObject object, String key) {
        var a = object.getAsJsonArray(key);
        return new Vector3f(a.get(0).getAsFloat(), a.get(1).getAsFloat(), a.get(2).getAsFloat());
    }

    private static JsonObject json(String path) throws Exception {
        try (var stream = TenguShieldTest.class.getClassLoader().getResourceAsStream(path)) {
            assertNotNull(stream, path);
            try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) { return JsonParser.parseReader(reader).getAsJsonObject(); }
        }
    }

    private static CuboidModel parse(String name) throws Exception {
        try (var stream = TenguShieldTest.class.getClassLoader().getResourceAsStream("assets/reverie_dreams/models/item/" + name + ".json")) {
            assertNotNull(stream);
            try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) { return CuboidModel.fromStream(reader); }
        }
    }

    private static FixturePlayer player() throws Exception { return (FixturePlayer) unsafe.allocateInstance(FixturePlayer.class); }

    private static class FixturePlayer extends Player {
        ItemStack stack; InteractionHand hand; boolean using;
        private FixturePlayer() { super(null, new GameProfile(UUID.randomUUID(), "tengu-shield-test")); }
        @Override public GameType gameMode() { return GameType.SURVIVAL; }
        @Override public boolean isSpectator() { return false; }
        @Override public ItemStack getItemInHand(InteractionHand hand) { return stack; }
        @Override public void startUsingItem(InteractionHand hand) { this.hand = hand; this.useItem = stack; this.useItemRemaining = 72000; using = true; }
        @Override public boolean isUsingItem() { return using; }
        void elapsed(int ticks) { this.useItemRemaining = 72000 - ticks; }
    }
}
