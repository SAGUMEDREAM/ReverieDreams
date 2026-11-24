package cc.thonly.reverie_dreams.world.settlement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/**
 * A finite, explicitly registered offer. Remaining stock is stock minus completed uses.
 */
public record SettlementTrade(Identifier settlement, Identifier role, Identifier site, BlockPos position,
                              ItemStack payment, ItemStack result, int stock, int uses, Optional<Stockroom> stockroom) {
    public record Stockroom(BlockPos goods, BlockPos till) {
        private static final Codec<Stockroom> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockPos.CODEC.fieldOf("goods").forGetter(Stockroom::goods),
                BlockPos.CODEC.fieldOf("till").forGetter(Stockroom::till)
        ).apply(instance, Stockroom::new));

        public Stockroom {
            goods = goods.immutable();
            till = till.immutable();
        }

        boolean isValid(BlockPos post) {
            return !goods.equals(till) && goods.getY() == post.getY() && till.getY() == post.getY()
                    && goods.distManhattan(post) == 1 && till.distManhattan(post) == 1;
        }
    }

    static final int MAX_STOCK = 64;
    static final Codec<SettlementTrade> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("settlement").forGetter(SettlementTrade::settlement),
            Identifier.CODEC.fieldOf("role").forGetter(SettlementTrade::role),
            Identifier.CODEC.fieldOf("site").forGetter(SettlementTrade::site),
            BlockPos.CODEC.fieldOf("position").forGetter(SettlementTrade::position),
            ItemStack.CODEC.fieldOf("payment").forGetter(SettlementTrade::payment),
            ItemStack.CODEC.fieldOf("result").forGetter(SettlementTrade::result),
            Codec.intRange(1, MAX_STOCK).fieldOf("stock").forGetter(SettlementTrade::stock),
            Codec.intRange(0, MAX_STOCK).fieldOf("uses").forGetter(SettlementTrade::uses),
            Stockroom.CODEC.optionalFieldOf("stockroom").forGetter(SettlementTrade::stockroom)
    ).apply(instance, SettlementTrade::new));

    public SettlementTrade {
        position = position.immutable();
        payment = payment.copy();
        result = result.copy();
    }

    public SettlementTrade(Identifier settlement, Identifier role, Identifier site, BlockPos position,
                           ItemStack payment, ItemStack result, int stock, int uses) {
        this(settlement, role, site, position, payment, result, stock, uses, Optional.empty());
    }

    @Override
    public ItemStack payment() {
        return this.payment.copy();
    }

    @Override
    public ItemStack result() {
        return this.result.copy();
    }

    boolean isValid() {
        return !this.payment.isEmpty() && !this.result.isEmpty()
                && this.payment.getCount() <= this.payment.getMaxStackSize()
                && this.payment.getComponentsPatch().isEmpty()
                && this.result.getCount() <= this.result.getMaxStackSize()
                && this.stock >= 1 && this.stock <= MAX_STOCK && this.uses >= 0 && this.uses <= this.stock
                && this.stockroom.map(room -> room.isValid(this.position) && this.stock == 1 && this.uses == 0).orElse(true);
    }

    boolean matches(SettlementPilotData.Member member, SettlementPilotData.Layout layout) {
        return member != null && layout != null && this.settlement.equals(member.settlement())
                && this.role.equals(member.role()) && this.position.equals(layout.sites().get(this.site));
    }

    SettlementTrade withUses(int count) {
        return new SettlementTrade(this.settlement, this.role, this.site, this.position,
                this.payment, this.result, this.stock, count, this.stockroom);
    }
}
