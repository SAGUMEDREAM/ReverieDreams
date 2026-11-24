package cc.thonly.reverie_dreams.client.scope;

import cc.thonly.reverie_dreams.item.weapon.LunarWeaponType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.List;

public final class LunarFirstPersonRenderer {
    private static final float MODEL_UNIT = 1 / 16f;
    private static final float MAGAZINE_DROP = .18f;
    private static final float MAGAZINE_PULL_BACK = .02f;

    // Points on each gun's item model: the rear grip, fore-end and exposed magazine.
    // All five gun models share the first-person display transform (Y 180°, Y offset 6.5).
    private record GunPose(float scale, float height, float depth,
                           float gripY, float gripZ, float supportY, float supportZ,
                           float magazineY, float magazineZ) {
        static GunPose forWeapon(LunarWeaponType type) {
            return switch (type) {
                case RIFLE -> RIFLE_POSE;
                case PISTOL -> PISTOL_POSE;
                case SHOTGUN -> SHOTGUN_POSE;
                case SMG -> SMG_POSE;
                case SNIPER -> SNIPER_POSE;
            };
        }

        Vector3f point(float y, float z) {
            return new Vector3f(0, (y - 1.5f) * scale * MODEL_UNIT, (8 - z) * scale * MODEL_UNIT);
        }
    }

    private static final GunPose RIFLE_POSE = new GunPose(.73f, -.48f, -.92f, 3.6f, 2.9f, 4.6f, 14.2f, 1.0f, 9.3f);
    private static final GunPose PISTOL_POSE = new GunPose(.84f, -.45f, -.83f, 2.9f, 4.1f, 0, 0, .15f, 2.8f);
    private static final GunPose SHOTGUN_POSE = new GunPose(.70f, -.48f, -.98f, 3.4f, 2.2f, 3.9f, 15.2f, 1.1f, 8.5f);
    private static final GunPose SMG_POSE = new GunPose(.75f, -.49f, -.91f, 3.2f, 2.2f, 2.8f, 11.6f, -1.9f, 7.8f);
    private static final GunPose SNIPER_POSE = new GunPose(.66f, -.49f, -1.02f, 3.4f, 2.4f, 5.2f, 13.8f, 2.4f, 8.5f);

    private LunarFirstPersonRenderer() {}

    public static void render(ItemInHandRenderer items, ItemModelResolver models, EntityRenderDispatcher entities,
                              AbstractClientPlayer player,
                              ItemStack gun, LunarWeaponType type, HumanoidArm arm, boolean freeHand,
                              float equip, float kick, float reload, PoseStack poses,
                              SubmitNodeCollector collector, int light) {
        GunPose shape = GunPose.forWeapon(type);
        float side = side(arm);
        float tilt = smooth(0, .25f, reload) * (1 - smooth(.65f, 1, reload));
        float magazineTravel = freeHand ? smooth(.35f, .49f, reload) - smooth(.53f, .67f, reload) : 0;
        Vector3f grip = shape.point(shape.gripY, shape.gripZ);
        poses.pushPose();
        try {
            // Bring the magwell into the lower part of the view while the support hand works.
            poses.translate(side * (.28f - tilt * .04f), shape.height - equip * .54f + tilt * .085f,
                    shape.depth + kick * .065f - tilt * .045f);
            poses.mulPose(Axis.YP.rotationDegrees(side * (13 + tilt * 8)));
            // Recoil and reload rotate around the rear grip. Every contact follows the same pose.
            poses.translate(grip.x, grip.y, grip.z);
            poses.mulPose(Axis.XP.rotationDegrees(3 + kick * 6 - tilt * 11));
            poses.mulPose(Axis.ZP.rotationDegrees(-side * tilt * 28));
            poses.translate(-grip.x, -grip.y, -grip.z);

            if (!player.isInvisible()) {
                float palmOffset = player.getSkin().model() == PlayerModelType.SLIM ? .055f : .07f;
                grip.add(side * palmOffset, -.046f, .045f);
                renderArm(entities, player, arm, grip, -68 + tilt * 8, side * 15, poses, collector, light);
                if (freeHand && (type != LunarWeaponType.PISTOL || reload > 0)) {
                    renderSupport(entities, player, shape, type, arm.getOpposite(), palmOffset, reload, magazineTravel,
                            poses, collector, light);
                }
            }

            poses.pushPose();
            try {
                poses.scale(shape.scale, shape.scale, shape.scale);
                ItemDisplayContext context = arm == HumanoidArm.RIGHT ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                        : ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
                if (magazineTravel > 0) {
                    renderReloadModel(models, player, gun, type, context, magazineTravel / shape.scale, poses, collector, light);
                } else {
                    items.renderItem(player, gun, context, poses, collector, light);
                }
            } finally {
                poses.popPose();
            }
        } finally {
            poses.popPose();
        }
    }

    private static void renderSupport(EntityRenderDispatcher entities, AbstractClientPlayer player, GunPose shape,
                                      LunarWeaponType type, HumanoidArm arm, float palmOffset, float reload,
                                      float magazineTravel, PoseStack poses, SubmitNodeCollector collector, int light) {
        float side = side(arm);
        // Follow the underside on the way to the magwell, then go back to the same fore-end.
        float reach = smooth(.08f, .35f, reload) * (1 - smooth(.67f, .94f, reload));
        Vector3f magazine = shape.point(shape.magazineY, shape.magazineZ)
                .add(side * (palmOffset + .015f), -.035f, .032f);
        Vector3f foreEnd = type == LunarWeaponType.PISTOL
                ? new Vector3f(magazine).add(side * .19f, -.38f, .17f)
                : shape.point(shape.supportY, shape.supportZ).add(side * (palmOffset + .012f), -.076f, .028f);
        foreEnd.lerp(magazine, reach);
        foreEnd.y -= .34f * reach * (1 - reach);
        foreEnd.y -= MAGAZINE_DROP * magazineTravel;
        foreEnd.z += MAGAZINE_PULL_BACK * magazineTravel;
        renderArm(entities, player, arm, foreEnd, Mth.lerp(reach, -50, -35),
                side * Mth.lerp(reach, 42, 30), poses, collector, light);
    }

    private static void renderReloadModel(ItemModelResolver models, AbstractClientPlayer player, ItemStack gun,
                                          LunarWeaponType type, ItemDisplayContext context, float travel,
                                          PoseStack poses, SubmitNodeCollector collector, int light) {
        ReloadModel model = new ReloadModel();
        models.updateForTopItem(model, gun, context, player.level(), player, player.getId() + context.ordinal());
        model.moveMagazine(MagazineBounds.forWeapon(type), travel);
        model.submit(poses, collector, light, OverlayTexture.NO_OVERLAY, 0);
    }

    // The installed magazine is baked into each gun. Move its original faces in the local render state
    // so the render never shows a second magazine beside the one still mounted on the gun.
    private static final class ReloadModel extends ItemStackRenderState {
        private LayerRenderState layer;
        private int layerCount;

        @Override
        public LayerRenderState newLayer() {
            layerCount++;
            layer = super.newLayer();
            return layer;
        }

        void moveMagazine(MagazineBounds bounds, float travel) {
            if (layerCount != 1) return;
            List<BakedQuad> quads = layer.prepareQuadList();
            int[] magazineFaces = new int[bounds.faces];
            int count = 0;
            for (int i = 0; i < quads.size(); i++) {
                if (bounds.contains(quads.get(i))) {
                    if (count == magazineFaces.length) return;
                    magazineFaces[count++] = i;
                }
            }
            // Leave unfamiliar resource-pack geometry intact.
            if (count != magazineFaces.length) return;
            Vector3f offset = new Vector3f(0, -MAGAZINE_DROP * travel, -MAGAZINE_PULL_BACK * travel);
            for (int index : magazineFaces) {
                BakedQuad face = quads.get(index);
                quads.set(index, new BakedQuad(
                        offset.add(face.position0(), new Vector3f()), offset.add(face.position1(), new Vector3f()),
                        offset.add(face.position2(), new Vector3f()), offset.add(face.position3(), new Vector3f()),
                        face.packedUV0(), face.packedUV1(), face.packedUV2(), face.packedUV3(),
                        face.direction(), face.materialInfo()));
            }
        }
    }

    private record MagazineBounds(float minX, float minY, float minZ, float maxX, float maxY, float maxZ,
                                  int faces, boolean clearTriggerGuard) {
        static MagazineBounds forWeapon(LunarWeaponType type) {
            return switch (type) {
                case RIFLE -> RIFLE_MAGAZINE;
                case PISTOL -> PISTOL_MAGAZINE;
                case SHOTGUN -> SHOTGUN_MAGAZINE;
                case SMG -> SMG_MAGAZINE;
                case SNIPER -> SNIPER_MAGAZINE;
            };
        }

        boolean contains(BakedQuad face) {
            for (int i = 0; i < BakedQuad.VERTEX_COUNT; i++) {
                Vector3fc p = face.position(i);
                float x = p.x() * 16;
                float y = p.y() * 16;
                float z = p.z() * 16;
                if (x < minX || x > maxX || y < minY || y > maxY || z < minZ || z > maxZ
                        || (clearTriggerGuard && z <= 6.01f && y >= -1.95f)) {
                    return false;
                }
            }
            return true;
        }
    }

    private static final MagazineBounds RIFLE_MAGAZINE = new MagazineBounds(7.49f, .23f, 8.13f, 8.51f, 4.40f, 10.02f, 6, false);
    private static final MagazineBounds PISTOL_MAGAZINE = new MagazineBounds(6.74f, -.11f, 1.24f, 9.26f, .51f, 4.26f, 6, false);
    private static final MagazineBounds SHOTGUN_MAGAZINE = new MagazineBounds(6.34f, .49f, 6.99f, 9.66f, 5.01f, 10.01f, 30, false);
    private static final MagazineBounds SMG_MAGAZINE = new MagazineBounds(6.74f, -2.51f, 5.99f, 9.26f, 5.01f, 9.51f, 36, true);
    private static final MagazineBounds SNIPER_MAGAZINE = new MagazineBounds(6.34f, 2.24f, 6.99f, 9.66f, 5.01f, 10.01f, 30, false);

    private static void renderArm(EntityRenderDispatcher entities, AbstractClientPlayer player, HumanoidArm arm,
                                  Vector3f palm, float pitch, float roll, PoseStack poses,
                                  SubmitNodeCollector collector, int light) {
        poses.pushPose();
        try {
            float side = side(arm);
            PlayerSkin skin = player.getSkin();
            float palmCenterX = skin.model() == PlayerModelType.SLIM ? .5f : 1;
            poses.translate(palm.x, palm.y, palm.z);
            poses.mulPose(Axis.XP.rotationDegrees(pitch));
            poses.mulPose(Axis.ZP.rotationDegrees(roll));
            // The avatar entry points render a shoulder-relative cuboid with a small built-in roll.
            // Recenter on the palm while keeping those entry points available to YSM replacements.
            poses.translate(side * palmCenterX * MODEL_UNIT, -8.4f * MODEL_UNIT, 0);
            poses.mulPose(Axis.ZP.rotation(-side * .1f));
            poses.translate(side * 5 * MODEL_UNIT, -2 * MODEL_UNIT, 0);

            var renderer = entities.getPlayerRenderer(player);
            var texture = skin.body().texturePath();
            if (arm == HumanoidArm.RIGHT) {
                renderer.renderRightHand(poses, collector, light, texture, player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE));
            } else {
                renderer.renderLeftHand(poses, collector, light, texture, player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE));
            }
        } finally {
            poses.popPose();
        }
    }

    private static float side(HumanoidArm arm) {
        return arm == HumanoidArm.RIGHT ? 1 : -1;
    }

    private static float smooth(float from, float to, float progress) {
        float t = Mth.clamp((progress - from) / (to - from), 0, 1);
        return t * t * (3 - 2 * t);
    }
}
