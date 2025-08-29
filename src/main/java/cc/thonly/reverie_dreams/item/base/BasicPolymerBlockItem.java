//package cc.thonly.reverie_dreams.item.base;
//
//import cc.thonly.reverie_dreams.Touhou;
//import cc.thonly.reverie_dreams.util.IdentifierGetter;
//import com.google.common.collect.ImmutableMap;
//import eu.pb4.polymer.core.api.item.PolymerBlockItem;
//import eu.pb4.polymer.core.api.item.PolymerItem;
//import eu.pb4.polymer.core.api.utils.PolymerClientDecoded;
//import eu.pb4.polymer.core.api.utils.PolymerKeepModel;
//import lombok.Getter;
//import lombok.Setter;
//import net.minecraft.block.Block;
//import net.minecraft.entity.player.PlayerEntity;
//import net.minecraft.item.*;
//import net.minecraft.item.tooltip.TooltipType;
//import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
//import net.minecraft.registry.Registries;
//import net.minecraft.registry.RegistryKey;
//import net.minecraft.registry.RegistryKeys;
//import net.minecraft.server.network.ServerPlayerEntity;
//import net.minecraft.sound.BlockSoundGroup;
//import net.minecraft.sound.SoundCategory;
//import net.minecraft.sound.SoundEvent;
//import net.minecraft.util.ActionResult;
//import net.minecraft.util.Hand;
//import net.minecraft.util.Identifier;
//import net.minecraft.util.math.Vec3d;
//import net.minecraft.world.World;
//import org.jetbrains.annotations.Nullable;
//import xyz.nucleoid.packettweaker.PacketContext;
//
//@Setter
//@Getter
//public class BasicPolymerBlockItem extends PolymerBlockItem implements PolymerItem, PolymerClientDecoded, PolymerKeepModel, IdentifierGetter {
//    final Identifier identifier;
//    final Block block;
//    final Item vanillaItem;
//
//    public BasicPolymerBlockItem(String path, Block block, Settings settings, Item vanillaItem) {
//        this(Touhou.id(path), block, settings, vanillaItem);
//    }
//
//    public BasicPolymerBlockItem(Identifier identifier, Block block, Settings settings, Item vanillaItem) {
//        super(block, settings.useBlockPrefixedTranslationKey().registryKey(RegistryKey.of(RegistryKeys.ITEM, identifier)));
//        this.identifier = identifier;
//        this.block = block;
//        this.vanillaItem = vanillaItem;
//    }
//
//    public ActionResult useOnBlock(ItemUsageContext context) {
//        ActionResult x = super.useOnBlock(context);
//        if (x == ActionResult.SUCCESS) {
//            PlayerEntity player = context.getPlayer();
//            if (player instanceof ServerPlayerEntity serverPlayer) {
//                Vec3d soundPos = Vec3d.ofCenter(context.getBlockPos().offset(context.getSide()));
//                BlockSoundGroup blockSoundGroup = this.getBlock().getDefaultState().getSoundGroup();
//                serverPlayer.networkHandler.sendPacket(new PlaySoundS2CPacket(
//                        Registries.SOUND_EVENT.getEntry(this.getPlaceSound(this.getBlock().getDefaultState())),
//                        SoundCategory.BLOCKS,
//                        soundPos.x,
//                        soundPos.y,
//                        soundPos.z,
//                        (blockSoundGroup.getVolume() + 1.0F) / 2.0F,
//                        blockSoundGroup.getPitch() * 0.8F,
//                        player.getRandom().nextLong()
//                ));
//            }
//            return ActionResult.SUCCESS_SERVER;
//        } else {
//            return x;
//        }
//    }
////    @Override
////    public ActionResult useOnBlock(ItemUsageContext context) {
////        World world = context.getWorld();
////        if (!world.isClient()) {
////            PlayerEntity entity = context.getPlayer();
////            Hand hand = context.getHand();
////            if (entity != null) {
////                ItemPlacementContext itemPlacementContext = new ItemPlacementContext(context);
////                SoundEvent placeSound = this.getPlaceSound(this.block.getPlacementState(itemPlacementContext));
////                if (itemPlacementContext.canPlace()) {
////                    entity.swingHand(hand);
////                    world.playSound(null, context.getBlockPos(), placeSound, SoundCategory.BLOCKS);
////                }
////            }
////        }
////        return super.useOnBlock(context);
////    }
//
//    @Override
//    public Item getPolymerItem(ItemStack itemStack, PacketContext packetContext) {
//        return Items.TRIAL_KEY;
//    }
//
////    @Override
////    public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipType tooltipType, PacketContext context) {
////        ItemStack stack = PolymerItem.super.getPolymerItemStack(itemStack, tooltipType, context);
////        return stack;
////    }
//
//    @Override
//    public @Nullable Identifier getPolymerItemModel(ItemStack stack, PacketContext context) {
//        return this.identifier;
//    }
//
//}
