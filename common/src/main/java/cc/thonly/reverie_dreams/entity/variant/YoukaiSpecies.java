package cc.thonly.reverie_dreams.entity.variant;

import cc.thonly.reverie_dreams.ReverieDreams;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.Mob;

/** Species traits are independent of the saved appearance and combat role. */
public enum YoukaiSpecies {
    CAT("cat", .36, 14, 30, 24, .10),
    TANUKI("tanuki", .30, 20, 36, 22, .35),
    FOX("fox", .33, 18, 26, 28, .15);

    public final String path;
    public final double movementSpeed;
    public final int meleeInterval;
    public final int rangedInterval;
    public final double followRange;
    public final double knockbackResistance;

    YoukaiSpecies(String path, double movementSpeed, int meleeInterval, int rangedInterval,
                  double followRange, double knockbackResistance) {
        this.path = path;
        this.movementSpeed = movementSpeed;
        this.meleeInterval = meleeInterval;
        this.rangedInterval = rangedInterval;
        this.followRange = followRange;
        this.knockbackResistance = knockbackResistance;
    }

    public AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 30).add(Attributes.ARMOR, 2)
                .add(Attributes.ATTACK_DAMAGE, 2).add(Attributes.MOVEMENT_SPEED, this.movementSpeed)
                .add(Attributes.FOLLOW_RANGE, this.followRange)
                .add(Attributes.KNOCKBACK_RESISTANCE, this.knockbackResistance);
    }

    public Identifier appearance(int variant) {
        return ReverieDreams.id("youkai_" + this.path + "_female_0" + (Math.clamp(variant, 0, 2) + 1));
    }
}
