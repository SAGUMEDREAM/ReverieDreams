package cc.thonly.reverie_dreams.paper.plants;

/** Pure rules taken from FruitLeavesBlock; independent of CE allocation and world state. */
public final class PlantRules {
    private PlantRules() { }
    public static final int MATURE_AGE = 3;
    public static final int HARVEST_AGE = 1;
    public static float moisture(int[] farmlandMoisture, boolean sameOnX, boolean sameOnZ, boolean sameDiagonal) {
        if (farmlandMoisture.length != 9) throw new IllegalArgumentException("Expected the nine cells below the fruit leaf");
        float factor = 1.0f;
        for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) {
            int value = farmlandMoisture[(x + 1) * 3 + z + 1];
            if (value < -1 || value > 7) throw new IllegalArgumentException("Expected -1 for non-farmland or moisture0..7");
            float bonus = value < 0 ? 0.0f : value > 0 ? 3.0f : 1.0f;
            if (x != 0 || z != 0) bonus /= 4.0f;
            factor += bonus;
        }
        if (sameOnX && sameOnZ || sameDiagonal) factor /= 2.0f;
        return factor;
    }
    public static int growthDenominator(float moisture) {
        if (!Float.isFinite(moisture) || moisture <= 0) throw new IllegalArgumentException("Invalid moisture");
        return (int) (25.0f / moisture) + 1;
    }
    public static boolean grows(int age, int brightness, int draw, int denominator) {
        checkAge(age);
        if (draw < 0 || draw >= denominator) throw new IllegalArgumentException("Random draw outside source range");
        return brightness >= 9 && age < MATURE_AGE && draw == 0;
    }
    public static int boneMealAge(int age) { checkAge(age); return Math.min(MATURE_AGE, age + 1); }
    public static void checkAge(int age) {
        if (age < 0 || age > MATURE_AGE) throw new IllegalArgumentException("Fruit age outside0..3");
    }
}
