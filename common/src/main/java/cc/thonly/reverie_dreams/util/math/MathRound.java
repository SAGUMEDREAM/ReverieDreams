package cc.thonly.reverie_dreams.util.math;

import lombok.Getter;

@Getter
public class MathRound extends BasicGraphics {
    private final double radius;

    public MathRound(double radius) {
        this.radius = radius;
    }

    public Point next(double range) {
        double circumference = 2 * Math.PI * this.radius;
        double theta = (range / circumference) * 2 * Math.PI;
        double nx = this.radius * Math.cos(theta);
        double ny = this.radius * Math.sin(theta);
        this.x = nx;
        this.y = ny;
        return new Point(nx, ny);
    }

}
