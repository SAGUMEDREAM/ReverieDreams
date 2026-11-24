package cc.thonly.reverie_dreams.client.renderer.danmaku;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Map;

/**
 * Resolves only the known flat orb models. A new baked model identity invalidates the cache on reload.
 */
public final class OrbSpriteCache {
    private static final int TEST_DYE = 0x5ABCEF;
    private static final int WHITE = 0xFFFFFFFF;
    private static final Identifier GENERATED = Identifier.withDefaultNamespace("models/item/generated.json");
    private final Map<Item, Entry> entries = Map.of(
            DanmakuTypes.BALL.getItemHolder().asItem(), new Entry("ball"),
            DanmakuTypes.MEDIUM_ORB.getItemHolder().asItem(), new Entry("medium_orb"),
            DanmakuTypes.HIGHLIGHT_ORB.getItemHolder().asItem(), new Entry("highlight_orb"),
            DanmakuTypes.LIGHT_ORB.getItemHolder().asItem(), new Entry("light_orb")
    );

    @Nullable
    public Sprite resolve(ItemStack stack, ItemModelResolver resolver) {
        Entry entry = entries.get(stack.getItem());
        if (entry == null || !entry.id.equals(stack.get(DataComponents.ITEM_MODEL)) || stack.hasFoil()) {
            return null;
        }
        Minecraft client = Minecraft.getInstance();
        ItemModel model = client.getModelManager().getItemModel(entry.id);
        if (entry.baked != model) {
            entry.baked = model;
            entry.plain = null;
            entry.dyed = null;
            if (!entry.isOverridden(client.getResourceManager())) {
                ItemStack sample = stack.getItem().getDefaultInstance();
                sample.remove(DataComponents.DYED_COLOR);
                entry.plain = capture(resolver, sample, entry.id, false);
                sample.set(DataComponents.DYED_COLOR, new DyedItemColor(TEST_DYE));
                entry.dyed = capture(resolver, sample, entry.id, true);
            }
        }
        return stack.has(DataComponents.DYED_COLOR) ? entry.dyed : entry.plain;
    }

    @Nullable
    private static Sprite capture(ItemModelResolver resolver, ItemStack sample, Identifier id, boolean dyed) {
        CapturedItem output = new CapturedItem();
        resolver.updateForTopItem(output, sample, ItemDisplayContext.FIXED, null, null, 0);
        if (output.layerCount != 1 || output.special || output.isAnimated()) {
            return null;
        }
        var layer = output.layer;
        var tints = layer.tintLayers();
        if (dyed ? tints.size() != 2 || tints.getInt(0) != WHITE || tints.getInt(1) != (0xFF000000 | TEST_DYE)
                : !tints.isEmpty()) {
            return null;
        }
        PoseStack local = new PoseStack();
        output.itemTransform.apply(false, local.last());
        local.last().mulPose(output.localTransform);
        var faces = new ArrayList<Face>(dyed ? 2 : 1);
        for (var quad : layer.prepareQuadList()) {
            var material = quad.materialInfo();
            if (material.itemRenderType() != Sheets.cutoutItemSheet() || material.lightEmission() != 0) {
                // Keep any translucent item in Minecraft's original sorting and compositing path.
                return null;
            }
            Vector3f normal = local.last().transformNormal(quad.direction().getUnitVec3f(), new Vector3f());
            // The existing renderer turns fixed-context models 180 degrees about the camera's up axis.
            if (normal.z() > -0.999f) {
                continue;
            }
            int tint = material.tintIndex();
            boolean overlay = dyed && tint == 1;
            Identifier expected = id.withPath("item/" + id.getPath() + (overlay ? "_overlay" : ""));
            if ((tint != 0 && tint != -1 && !overlay) || !material.sprite().contents().name().equals(expected)) {
                return null;
            }
            float[] vertices = new float[4 * Face.STRIDE];
            for (int i = 0; i < 4; i++) {
                Vector3f point = local.last().pose().transformPosition(quad.position(i), new Vector3f());
                int vertex = i * Face.STRIDE;
                vertices[vertex] = point.x;
                vertices[vertex + 1] = point.y;
                vertices[vertex + 2] = point.z;
                vertices[vertex + 3] = UVPair.unpackU(quad.packedUV(i));
                vertices[vertex + 4] = UVPair.unpackV(quad.packedUV(i));
            }
            faces.add(new Face(vertices, overlay));
        }
        if (faces.size() != (dyed ? 2 : 1) || faces.getFirst().dyed || (dyed && !faces.getLast().dyed)) {
            return null;
        }
        return new Sprite(faces.toArray(Face[]::new));
    }

    public record Sprite(Face[] faces) {
    }

    public record Face(float[] vertices, boolean dyed) {
        static final int STRIDE = 5;
    }

    private static final class Entry {
        final Identifier id;
        ItemModel baked;
        Sprite plain;
        Sprite dyed;

        Entry(String path) {
            this.id = ReverieDreams.id("danmaku/" + path);
        }

        boolean isOverridden(ResourceManager resources) {
            return resources.getResourceStack(id.withPath("items/" + id.getPath() + ".json")).size() != 1
                    || resources.getResourceStack(id.withPath("models/item/" + id.getPath() + ".json")).size() != 1
                    || resources.getResourceStack(id.withPath("models/item/" + id.getPath() + "_dyed.json")).size() != 1
                    || resources.getResourceStack(GENERATED).size() > 1;
        }
    }

    private static final class CapturedItem extends ItemStackRenderState {
        private final LayerRenderState layer = new LayerRenderState() {
            @Override
            public void setItemTransform(ItemTransform transform) {
                CapturedItem.this.itemTransform = transform;
            }

            @Override
            public void setLocalTransform(Matrix4fc transform) {
                CapturedItem.this.localTransform = transform;
            }

            @Override
            public <T> void setupSpecialModel(net.minecraft.client.renderer.special.SpecialModelRenderer<T> renderer, @Nullable T argument) {
                CapturedItem.this.special = true;
            }
        };
        private ItemTransform itemTransform = ItemTransform.NO_TRANSFORM;
        private Matrix4fc localTransform = new Matrix4f();
        private int layerCount;
        private boolean special;

        @Override
        public LayerRenderState newLayer() {
            layerCount++;
            return layer;
        }
    }
}
