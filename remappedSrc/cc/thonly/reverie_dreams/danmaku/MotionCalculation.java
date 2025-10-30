package cc.thonly.reverie_dreams.danmaku;

@FunctionalInterface
public interface MotionCalculation {
    double get(Integer age, Integer component);
}
