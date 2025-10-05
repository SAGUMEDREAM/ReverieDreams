package cc.thonly.reverie_dreams.mixin.client;

import com.google.gson.Gson;
import com.mojang.serialization.JsonOps;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.SetTradeOffersS2CPacket;
import net.minecraft.predicate.component.ComponentMapPredicate;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.TradedItem;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Optional;

@Mixin(SetTradeOffersS2CPacket.class)
public class SetTradeOffersS2CPacketMixin {
    @Mutable
    @Shadow
    @Final
    private TradeOfferList offers;

    @Shadow @Final private int syncId;

    @Shadow @Final private int levelProgress;

    @Shadow @Final private int experience;

    @Shadow @Final private boolean leveled;

    @Shadow @Final private boolean refreshable;

//    @Unique private TradeOfferList polymerifyList;

//    @Inject(method = "<init>*", at = @At("TAIL"))
//    private void modifyOffersArg(int syncId, TradeOfferList offers, int levelProgress, int experience, boolean leveled, boolean refreshable, CallbackInfo ci) {
//        TradeOfferList origin = this.offers.copy();
//        TradeOfferList polymerifyList = new TradeOfferList();
//        for (TradeOffer offer : origin) {
//            TradeOffer originOffer = offer.copy();
//            Optional<TradedItem> firstBuyItem = Optional.of(originOffer.getFirstBuyItem());
//            Optional<TradedItem> secondBuyItem = originOffer.getSecondBuyItem();
//            TradedItem polymerifyFirstBuyTradedItem = polymerifyTradeItem(firstBuyItem);
//            TradedItem polymerifySecondBuyTradedItem = polymerifyTradeItem(secondBuyItem);
//            var polymerifyOffer = new TradeOffer(
//                    polymerifyFirstBuyTradedItem,
//                    polymerifySecondBuyTradedItem == null ? Optional.empty() : Optional.of(polymerifySecondBuyTradedItem),
//                    originOffer.getSellItem(),
//                    originOffer.getUses(),
//                    originOffer.getMaxUses(),
//                    originOffer.shouldRewardPlayerExperience(),
//                    originOffer.getSpecialPrice(),
//                    originOffer.getDemandBonus(),
//                    originOffer.getPriceMultiplier(),
//                    originOffer.getMerchantExperience()
//            );
//            polymerifyList.add(polymerifyOffer);
//        }
//        this.offers = polymerifyList;
//        this.polymerifyList = polymerifyList;
//        System.out.println(new Gson().toJson(TradeOfferList.CODEC.encodeStart(JsonOps.INSTANCE, this.offers).getOrThrow()));
//    }
//
//    @Inject(method = "write", at = @At("HEAD"), cancellable = true)
//    private void onWrite(RegistryByteBuf buf, CallbackInfo ci) {
//        buf.writeSyncId(this.syncId);
//        TradeOfferList.PACKET_CODEC.encode(buf, this.polymerifyList);
//        buf.writeVarInt(this.levelProgress);
//        buf.writeVarInt(this.experience);
//        buf.writeBoolean(this.leveled);
//        buf.writeBoolean(this.refreshable);
//        ci.cancel();
//    }
//
//    @Unique
//    private TradedItem polymerifyTradeItem(Optional<TradedItem> optionalTradedItem) {
//        if (optionalTradedItem.isEmpty()) {
//            return null;
//        }
//        TradedItem tradedItem = optionalTradedItem.get();
//        ItemStack itemStack = tradedItem.itemStack();
//        ItemStack polymerItemStack = PolymerItemUtils.getPolymerItemStack(itemStack, PacketContext.create());
//        return new TradedItem(polymerItemStack.getItem().getRegistryEntry(), polymerItemStack.getCount(), ComponentMapPredicate.of(polymerItemStack.components));
//    }
}
