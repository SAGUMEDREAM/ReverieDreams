package cc.thonly.polymer.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class BasicPolymerBlockItem extends BlockItem implements IBasicPolymerItem {
    public BasicPolymerBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    public InteractionResult useOn(UseOnContext context) {
        InteractionResult x = super.useOn(context);
        if (x == InteractionResult.SUCCESS) {
            Player player = context.getPlayer();
            if (player instanceof ServerPlayer serverPlayer) {
                Direction clickedFace = context.getClickedFace();
                BlockPos offset = context.getClickedPos().offset(clickedFace.getUnitVec3i());
                Vec3 soundPos = Vec3.atCenterOf(offset);
                SoundType blockSoundGroup = this.getBlock().defaultBlockState().getSoundType();
                SoundEvent placeSound = this.getPlaceSound(this.getBlock().defaultBlockState());
                ResourceLocation key = BuiltInRegistries.SOUND_EVENT.getKey(placeSound);
                if (key == null) {
                    return InteractionResult.SUCCESS_SERVER;
                }
                Optional<Holder.Reference<SoundEvent>> optional = BuiltInRegistries.SOUND_EVENT.get(key);
                if (optional.isEmpty()) {
                    return InteractionResult.SUCCESS_SERVER;
                }
                Holder.Reference<SoundEvent> soundEventReference = optional.get();
                serverPlayer.connection.send(new ClientboundSoundPacket(
                        soundEventReference,
                        SoundSource.BLOCKS,
                        soundPos.x,
                        soundPos.y,
                        soundPos.z,
                        (blockSoundGroup.getVolume() + 1.0F) / 2.0F,
                        blockSoundGroup.getPitch() * 0.8F,
                        player.getRandom().nextLong()
                ));
            }
            return InteractionResult.SUCCESS_SERVER;
        } else {
            return x;
        }
    }
}
