package cc.thonly.reverie_dreams.mixin.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 让船（以及竹筏等所有 {@link AbstractBoat} 子类）自动爬上一级水坡。
 *
 * <p>原版船的漂浮只看「船底那一层」：{@code checkInWater()} 只有在船底所在的那一格压到水面时
 * 才会写入 {@code waterLevel}。因此船头若先探进高一级的水里、船体还留在下面，就会被
 * {@code isUnderwater()} 判成 {@code UNDER_WATER}，随后 {@code floatBoat()} 让它持续下沉——
 * 于是「想爬坡」表现为「沉下去」。</p>
 *
 * <p>这里在 {@code tick()} 末尾以「船头前沿」（船体半宽再外扩 0.06 格）为观测点，
 * 看它所在水柱的水面：比船底高出 0.35~1.6 格（典型一级水梯）时，把船对齐到该水柱的漂浮高度
 * （{@code 水面 - 船高 + 0.101}，与原版落水对齐公式一致），并向前微调让船底那层落进新水柱。</p>
 *
 * <p>要点：</p>
 * <ul>
 *   <li>观测点在船头，所以船体还没真正探进上一级水柱时就会提前抬升；</li>
 *   <li>前移量从 0.4 / 0.25 / 0.1 / 0 依次试探并逐个做碰撞检测，绝不会把宽船体推过下一级台阶
 *       （旧版固定推 0.5 格，船靠到台阶面较近时会被下一级台阶卡住 → 抬升被跳过 → 沉船，
 *       这正是「有概率沉船」的来源）；</li>
 *   <li>船一旦已经被淹（本柱水面就在船顶上），同一套判定会把它重新托回水面，属于自愈兜底。</li>
 * </ul>
 *
 * <p>判定只看水面高度、不要求台阶是实心方块，因此「整块台阶 + 一层水」「更深的台阶水柱」
 * 「纯水流落差」都能爬；平静湖面（船头处水面落差≈0）不会误触发。</p>
 */
@Mixin(AbstractBoat.class)
public abstract class AbstractBoatMixin {

    /**
     * 抬升所需的最小高度差（格）。同层漂浮时该值为略小于 0 的负数，不会误触发。
     */
    @Unique
    private static final double RD_MIN_RISE = 0.35D;
    /**
     * 一次抬升允许的最大高度差（格），超过视为「不止一级」不处理。
     */
    @Unique
    private static final double RD_MAX_RISE = 1.6D;
    /**
     * 船头前沿的额外外扩距离，避免「船体正好贴在台阶面上」时前沿落在新水柱边界之外。
     */
    @Unique
    private static final double RD_FRONT_BIAS = 0.06D;
    /**
     * 从船底所在层向上搜索水面的层数。
     */
    @Unique
    private static final int RD_SCAN_LAYERS = 3;

    @Inject(method = "tick", at = @At("TAIL"))
    private void reverie_dreams$climbWaterSlope(CallbackInfo ci) {
        AbstractBoat boat = (AbstractBoat) (Object) this;
        Level level = boat.level();
        AABB box = boat.getBoundingBox();

        // 船所在柱子必须真的有水（船底那层可能是实心地板，所以从船底层往上找水面）
        double currentTop = reverie_dreams$columnWaterTop(
                level, Mth.floor(boat.getX()), Mth.floor(box.minY), Mth.floor(boat.getZ())
        );
        if (currentTop == Double.NEGATIVE_INFINITY) {
            return;
        }

        double dirX;
        double dirZ;
        Vec3 motion = boat.getDeltaMovement();
        double horizontalSqr = motion.x * motion.x + motion.z * motion.z;
        if (horizontalSqr > 4.0E-4D) {
            double inv = 1.0D / Math.sqrt(horizontalSqr);
            dirX = motion.x * inv;
            dirZ = motion.z * inv;
        } else {
            // 被台阶挡停时水平速度会被碰撞清零，此时回退到船头朝向（无人空船不主动爬）
            if (boat.getControllingPassenger() == null) {
                return;
            }
            float yaw = boat.getYRot() * ((float) Math.PI / 180.0F);
            dirX = -Mth.sin(yaw);
            dirZ = Mth.cos(yaw);
        }

        double lead = boat.getBbWidth() * 0.5D + RD_FRONT_BIAS;
        int baseY = Mth.floor(box.minY);
        double frontTop = reverie_dreams$columnWaterTop(
                level,
                Mth.floor(boat.getX() + dirX * lead),
                baseY,
                Mth.floor(boat.getZ() + dirZ * lead)
        );
        if (frontTop == Double.NEGATIVE_INFINITY) {
            return;
        }

        double targetY = frontTop - boat.getBbHeight() + 0.101D;
        double rise = targetY - box.minY;
        if (rise < RD_MIN_RISE || rise > RD_MAX_RISE) {
            return;
        }

        // 前移量逐个试探：第一个既能不卡方块、又能让船底落进新水柱的即采用
        double[] advances = {0.4D, 0.25D, 0.1D, 0.0D};
        for (double advance : advances) {
            if (reverie_dreams$tryClimb(level, boat, box, dirX, dirZ, advance, targetY, frontTop)) {
                return;
            }
        }
    }

    /**
     * 尝试把船对齐到 {@code targetY} 并前移 {@code advance}：目标位置必须不卡方块、
     * 且船底那一层确实压住新水柱的水面。成功则落位并清零竖直速度，返回 true。
     */
    @Unique
    private static boolean reverie_dreams$tryClimb(
            Level level,
            AbstractBoat boat,
            AABB box,
            double dirX,
            double dirZ,
            double advance,
            double targetY,
            double frontTop
    ) {
        double targetX = boat.getX() + dirX * advance;
        double targetZ = boat.getZ() + dirZ * advance;
        AABB targetBox = box.move(targetX - boat.getX(), targetY - box.minY, targetZ - boat.getZ());
        if (!level.noCollision(boat, targetBox)) {
            return false;
        }
        if (reverie_dreams$bottomWaterTop(level, targetBox) < frontTop - 0.05D) {
            return false;
        }
        boat.setPos(targetX, targetY, targetZ);
        Vec3 remaining = boat.getDeltaMovement();
        boat.setDeltaMovement(remaining.x, 0.0D, remaining.z);
        return true;
    }

    /**
     * 「船底那一层」（与原版 {@code checkInWater()} 同层）里最高的水面高度；没水时返回 -∞。
     */
    @Unique
    private static double reverie_dreams$bottomWaterTop(Level level, AABB box) {
        int minX = Mth.floor(box.minX);
        int maxX = Mth.ceil(box.maxX);
        int y = Mth.floor(box.minY);
        int minZ = Mth.floor(box.minZ);
        int maxZ = Mth.ceil(box.maxZ);
        double top = Double.NEGATIVE_INFINITY;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                pos.set(x, y, z);
                FluidState fluid = level.getFluidState(pos);
                if (fluid.is(FluidTags.WATER)) {
                    top = Math.max(top, y + fluid.getHeight(level, pos));
                }
            }
        }
        return top;
    }

    /**
     * 指定柱子从 {@code fromY} 起向上若干层内最高的水面高度；没水时返回 -∞。
     */
    @Unique
    private static double reverie_dreams$columnWaterTop(Level level, int x, int fromY, int z) {
        double top = Double.NEGATIVE_INFINITY;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = fromY; y <= fromY + RD_SCAN_LAYERS; y++) {
            pos.set(x, y, z);
            FluidState fluid = level.getFluidState(pos);
            if (fluid.is(FluidTags.WATER)) {
                top = Math.max(top, y + fluid.getHeight(level, pos));
            }
        }
        return top;
    }
}