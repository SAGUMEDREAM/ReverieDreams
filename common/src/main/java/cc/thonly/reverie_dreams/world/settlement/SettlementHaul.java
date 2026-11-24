package cc.thonly.reverie_dreams.world.settlement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * The single outstanding pilot shipment for a resident. Nonempty cargo is held by the dimension.
 */
public record SettlementHaul(Identifier settlement, Identifier role, Stop source, Stop destination,
                             Item item, int count, ItemStack cargo) {
    static final int MAX_COUNT = 99;
    static final Codec<SettlementHaul> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("settlement").forGetter(SettlementHaul::settlement),
            Identifier.CODEC.fieldOf("role").forGetter(SettlementHaul::role),
            Stop.CODEC.fieldOf("source").forGetter(SettlementHaul::source),
            Stop.CODEC.fieldOf("destination").forGetter(SettlementHaul::destination),
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(SettlementHaul::item),
            Codec.intRange(1, MAX_COUNT).fieldOf("count").forGetter(SettlementHaul::count),
            ItemStack.CODEC.optionalFieldOf("cargo", ItemStack.EMPTY).forGetter(SettlementHaul::cargo)
    ).apply(instance, SettlementHaul::new));

    public record Stop(Identifier site, BlockPos approach, BlockPos chest) {
        private static final Codec<Stop> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("site").forGetter(Stop::site),
                BlockPos.CODEC.fieldOf("approach").forGetter(Stop::approach),
                BlockPos.CODEC.fieldOf("chest").forGetter(Stop::chest)
        ).apply(instance, Stop::new));

        public Stop {
            approach = approach.immutable();
            chest = chest.immutable();
        }

        boolean isAdjacent() {
            return this.approach.getY() == this.chest.getY() && this.approach.distManhattan(this.chest) == 1;
        }
    }

    public SettlementHaul {
        cargo = cargo.copy();
    }

    @Override
    public ItemStack cargo() {
        return this.cargo.copy();
    }

    boolean carrying() {
        return !this.cargo.isEmpty();
    }

    boolean belongsTo(SettlementPilotData.Member member) {
        return member != null && this.settlement.equals(member.settlement()) && this.role.equals(member.role());
    }

    boolean matches(SettlementPilotData.Layout layout) {
        return layout != null && layout.roles().containsKey(this.role)
                && this.source.approach().equals(layout.sites().get(this.source.site()))
                && this.destination.approach().equals(layout.sites().get(this.destination.site()));
    }

    boolean isValid() {
        return !this.source.site().equals(this.destination.site())
                && !this.source.approach().equals(this.destination.approach())
                && !this.source.chest().equals(this.destination.chest())
                && this.source.isAdjacent() && this.destination.isAdjacent()
                && this.count >= 1 && this.count <= MAX_COUNT
                && (!this.carrying() || (this.cargo.is(this.item) && this.cargo.getCount() == this.count
                && this.count <= this.cargo.getMaxStackSize()));
    }

    SettlementHaul withCargo(ItemStack batch) {
        return new SettlementHaul(this.settlement, this.role, this.source, this.destination, this.item, this.count, batch);
    }
}
