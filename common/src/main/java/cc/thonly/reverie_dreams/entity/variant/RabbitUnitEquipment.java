package cc.thonly.reverie_dreams.entity.variant;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.entity.RabbitUnit;
import cc.thonly.reverie_dreams.item.weapon.LunarWeaponType;
import cc.thonly.reverie_dreams.item.weapon.WeaponOfTheMoon;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShieldItem;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import java.util.Locale;

/** Shared native/Polymer attachment space and mechanical phase for the soldier's real equipment. */
public final class RabbitUnitEquipment {
    public static final float MOUNT_SCALE = .65f;
    public static Vector3f handAnchorOffset() { return new Vector3f(.022349476443f, -.0789840607904f, 2.3359351340998f); }
    public enum Part { BASE, MAGAZINE, BOLT }

    public record Frame(LunarWeaponType right, LunarWeaponType left, RabbitUnitAction action,
                        float reload, float elapsedTicks, HumanoidArm mainArm) {
        public LunarWeaponType type(HumanoidArm arm) { return arm == HumanoidArm.RIGHT ? right : left; }
        public boolean reloading(HumanoidArm arm) { return arm == mainArm && action == RabbitUnitAction.RELOAD; }
        public boolean shooting(HumanoidArm arm) { return arm == mainArm && action == RabbitUnitAction.SHOOT; }
    }

    private RabbitUnitEquipment() {}

    public static LunarWeaponType type(ItemStack stack) {
        return stack.getItem() instanceof WeaponOfTheMoon gun ? gun.getWeaponType() : null;
    }

    public static ItemStack stackInArm(RabbitUnit actor, HumanoidArm arm) {
        return actor.getItemInHand(actor.getMainArm() == arm ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
    }

    public static Frame capture(RabbitUnit actor, float partialTick) {
        float duration = Math.max(1, actor.combatActionDuration());
        return new Frame(type(stackInArm(actor, HumanoidArm.RIGHT)), type(stackInArm(actor, HumanoidArm.LEFT)),
                actor.modelAction(), Mth.clamp(actor.reloadProgress() + partialTick / duration, 0, 1),
                actor.combatActionElapsedTicks() + partialTick, actor.getMainArm());
    }

    /** A render-only vanilla carrier, never an entity equipment replacement or registered item. */
    public static ItemStack carrier(ItemStack source, Part part) {
        LunarWeaponType type = type(source);
        if (type == null) return source;
        var result = new ItemStack(Items.PAPER);
        result.applyComponents(source.getComponentsPatch());
        result.set(DataComponents.ITEM_MODEL, ReverieDreams.id("rabbit_unit_equipment/type_" + type.number() + "_" + part.name().toLowerCase(Locale.ROOT)));
        result.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, source.hasFoil());
        return result;
    }

    /** Item displays cannot evaluate a living entity's using_item predicate, so select its vanilla branch explicitly. */
    public static ItemStack shieldCarrier(RabbitUnit actor, HumanoidArm arm, ItemStack source) {
        if (!(source.getItem() instanceof ShieldItem)) return source;
        boolean blocking = blockingShield(actor, arm);
        var copy = source.copy();
        copy.set(DataComponents.ITEM_MODEL, ReverieDreams.id("rabbit_unit_equipment/shield" + (blocking ? "_blocking" : "")));
        return copy;
    }

    public static boolean blockingShield(RabbitUnit actor, HumanoidArm arm) {
        return actor.isBlocking() && actor.getUsedItemHand()
                == (actor.getMainArm() == arm ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
    }

    /** Vanilla shield handle center (0,0,2) is anchored to the soldier's palm at (0,-.25,0), in model pixels. */
    public static Vector3f shieldHandAnchorOffset(HumanoidArm arm, boolean blocking) {
        if (!blocking) return new Vector3f(0, arm == HumanoidArm.LEFT ? -7.25f : .75f, 2);
        return arm == HumanoidArm.LEFT ? new Vector3f(-.033355086300f, -2.388980338775f, 1.325271839791f)
                : new Vector3f(.465720321114f, 3.607600519094f, .543892020109f);
    }

    /** Match the vanilla special-model display, with the idle left plate facing away from the body. */
    public static Matrix4f shieldHandTransform(Matrix4f matrix, HumanoidArm arm, boolean blocking) {
        matrix.translate(shieldHandAnchorOffset(arm, blocking).div(16))
                .rotateX(-Mth.HALF_PI).translate(0, .125f, -.0625f)
                .translate(0, .125f, arm == HumanoidArm.RIGHT ? -.25f : .25f);
        // Guard IK was authored with this rotation; an idle left shield must not inherit it.
        if (arm == HumanoidArm.LEFT && blocking) matrix.rotateY(Mth.PI);
        return matrix;
    }

    /** Align every original slanted grip center with the original rifle's solved hand anchor. Units are model pixels. */
    public static Vector3f gripCorrection(LunarWeaponType type) {
        float c = Mth.cos(22.5f * Mth.DEG_TO_RAD), s = Mth.sin(22.5f * Mth.DEG_TO_RAD);
        var rifle = new Vector3f(8, 1.25f + 2.5f*c - s, 1 + 2.5f*s + c);
        var grip = switch (type) {
            case RIFLE -> new Vector3f(rifle);
            case PISTOL -> new Vector3f(8, 5.5f - 2.75f*c, 4 - 2.75f*s);
            case SHOTGUN, SMG -> new Vector3f(8, 5 - 2.5f*c, 1 - 2.5f*s);
            case SNIPER -> new Vector3f(8, 5 - 2.5f*c, 1.25f - 2.5f*s);
        };
        return rifle.sub(grip);
    }

    public static Vector3f supportPoint(LunarWeaponType type) {
        return (switch (type) {
            case RIFLE -> new Vector3f(8, 5.25f, 10.5f);
            case PISTOL -> new Vector3f(8, 3.8f, 4);
            case SHOTGUN -> new Vector3f(8, 4.8f, 13);
            case SMG -> new Vector3f(8, 3.5f, 11.5f);
            case SNIPER -> new Vector3f(8, 5.2f, 11.5f);
        }).add(gripCorrection(type));
    }

    /** Rear butt pad center, or grip center for the stockless pistol. In the same corrected mesh space as supportPoint. */
    public static Vector3f stockPoint(LunarWeaponType type) {
        return (switch (type) {
            case RIFLE -> new Vector3f(8, 6, -5.5f);
            case PISTOL -> new Vector3f(8, 2.75f, 4);
            case SHOTGUN -> new Vector3f(8, 6, -6);
            case SMG -> new Vector3f(8, 5.5f, -6);
            case SNIPER -> new Vector3f(8, 6, -9);
        }).add(gripCorrection(type));
    }

    public static boolean visible(Frame frame, HumanoidArm arm, Part part) {
        return frame.type(arm) != null && !(part == Part.MAGAZINE && frame.reloading(arm)
                && frame.reload() >= .30f && frame.reload() < .44f);
    }

    /** Append exactly the same hand, original third-person display and mechanical transforms on both backends. */
    public static Matrix4f transform(Matrix4f matrix, Frame frame, HumanoidArm arm, Part part) {
        var type = frame.type(arm);
        if (type == null) return matrix;
        boolean left = arm == HumanoidArm.LEFT;
        matrix.translate(handAnchorOffset().div(16)).rotateX(-Mth.HALF_PI).translate(0, .125f, -.0625f);
        // Original lunar-arsenal third-person display. Item carriers use NONE so this is applied only here.
        matrix.translate(0, 3f/16, -2f/16)
                .rotateXYZ(-Mth.PI, (left ? -1 : 1) * 1.63f * Mth.DEG_TO_RAD,
                        (left ? -1 : 1) * 178.75f * Mth.DEG_TO_RAD).scale(.85f * MOUNT_SCALE);
        var correction = gripCorrection(type).div(16);
        matrix.translate(correction);
        if (part == Part.MAGAZINE && frame.reloading(arm)) {
            float p = frame.reload();
            float withdrawn = p < .30f ? smooth((p - .08f) / .22f) : 1 - smooth((p - .44f) / .31f);
            float travel = switch (type) { case RIFLE -> 6; case PISTOL -> 6; case SHOTGUN -> 6; case SMG -> 9; case SNIPER -> 4.5f; };
            float angle = type == LunarWeaponType.PISTOL ? 22.5f : type == LunarWeaponType.RIFLE ? -5 : 0;
            matrix.translate(0, -withdrawn * travel * Mth.cos(angle * Mth.DEG_TO_RAD) / 16,
                    -withdrawn * travel * Mth.sin(angle * Mth.DEG_TO_RAD) / 16);
        }
        if (part == Part.BOLT) {
            float pull = 0, lift = 0;
            if (frame.reloading(arm)) {
                float p = frame.reload();
                pull = smooth((p - .78f) / .08f) * (1 - smooth((p - .89f) / .09f));
                lift = smooth((p - .75f) / .05f) * (1 - smooth((p - .94f) / .05f));
            } else if (frame.shooting(arm)) {
                float t = frame.elapsedTicks();
                float start = type == LunarWeaponType.SHOTGUN || type == LunarWeaponType.SNIPER ? 3 : 0;
                float length = start == 3 ? 10 : type == LunarWeaponType.SMG ? Math.min(3, type.shotInterval() * 2) : 3;
                pull = t <= start || t >= start + length ? 0 : Mth.sin(Mth.PI * ((t - start) / length));
                lift = pull;
            }
            float travel = switch (type) { case RIFLE -> 1.8f; case PISTOL -> 2; case SHOTGUN -> 3.5f; case SMG -> 2; case SNIPER -> 3; };
            matrix.translate(0, 0, -pull * travel / 16);
            if (type == LunarWeaponType.SNIPER) matrix.translate(2.5f/16, -1.5f/16, -3.5f/16)
                    .rotateZ(-lift * 55 * Mth.DEG_TO_RAD).translate(-2.5f/16, 1.5f/16, 3.5f/16);
        }
        return matrix;
    }

    private static float smooth(float v) {
        v = Mth.clamp(v, 0, 1);
        return v * v * (3 - 2 * v);
    }
}
