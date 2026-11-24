package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * A replaceable magazine for the weapon of the moon. Every magazine carries a
 * number of rounds in its {@link RDDataComponentTypes#MAGAZINE_ROUNDS}
 * component; loading a magazine into the weapon moves those rounds over.
 */
public class MagazineItem extends Item {
    /**
     * Legacy API: the type-one rifle's capacity. Other magazines use {@link #capacity()}.
     */
    public static final int CAPACITY = 30;
    private final LunarWeaponType weaponType;

    public MagazineItem(Properties properties) {
        this(properties, LunarWeaponType.RIFLE);
    }

    public MagazineItem(Properties properties, LunarWeaponType weaponType) {
        super(properties);
        this.weaponType = weaponType;
    }

    public LunarWeaponType getWeaponType() {
        return weaponType;
    }

    public int capacity() {
        return weaponType.capacity();
    }

    public static int capacity(ItemStack stack) {
        return stack.getItem() instanceof MagazineItem magazine ? magazine.capacity() : CAPACITY;
    }

    public static int getRounds(ItemStack stack) {
        return Mth.clamp(stack.getOrDefault(RDDataComponentTypes.MAGAZINE_ROUNDS.get(), 0), 0, capacity(stack));
    }

    public static void setRounds(ItemStack stack, int rounds) {
        stack.set(RDDataComponentTypes.MAGAZINE_ROUNDS.get(), Mth.clamp(rounds, 0, capacity(stack)));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getRounds(stack) < capacity();
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0f * getRounds(stack) / capacity());
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xA9C9FF;
    }
}
