package cc.thonly.reverie_dreams.util.math;

import lombok.Getter;

@Getter
public abstract class BasicGraphics {
    protected double x;
    protected double y;

    public record Point(double x, double y) {
    }
}
