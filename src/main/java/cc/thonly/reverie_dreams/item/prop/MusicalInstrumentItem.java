package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.component.ModDataComponentTypes;
import cc.thonly.reverie_dreams.entity.npc.NPCRoleEntity;
import cc.thonly.reverie_dreams.entity.npc.NPCWorkModes;
import cc.thonly.reverie_dreams.util.TouhouNotaUtils;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.phys.AABB;
import java.util.List;

public class MusicalInstrumentItem extends Item {
    public static final Codec<NoteBlockInstrument> NOTE_BLOCK_INSTRUMENT_CODEC = StringRepresentable.fromEnum(NoteBlockInstrument::values);
    public static final BlockPos NONE = new BlockPos(0, 0, 0);
    public static final AttackBlockCallback BLOCK_CALLBACK = (player, world, hand, blockPos, direction) -> {
        if (!world.isClientSide() && world instanceof ServerLevel serverWorld) {
            ItemStack mainStack = player.getMainHandItem();
            ItemStack offStack = player.getOffhandItem();
            ItemStack stack = null;

            if (mainStack.getItem() instanceof MusicalInstrumentItem) {
                stack = mainStack;
            } else if (offStack.getItem() instanceof MusicalInstrumentItem) {
                stack = offStack;
            }

            if (stack != null && !player.isSpectator() && player.isShiftKeyDown()) {
                List<String> fileNames = TouhouNotaUtils.getFileNames();
                if (fileNames.isEmpty()) {
                    player.displayClientMessage(Component.translatable("item.reverie_dreams.music.no_files"), false);
                    return InteractionResult.SUCCESS;
                }

                String playingMusic = stack.getOrDefault(ModDataComponentTypes.PLAYING_MUSIC, null);
                int index = playingMusic == null ? -1 : fileNames.indexOf(playingMusic);
                index = (index - 1 + fileNames.size()) % fileNames.size(); // 向上翻页

                String previous = fileNames.get(index);
                stack.set(ModDataComponentTypes.PLAYING_MUSIC, previous);
                player.displayClientMessage(Component.translatable("item.reverie_dreams.music.switch_music", previous), false);
                player.swing(hand);

                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    };

    static {
        AttackBlockCallback.EVENT.register(BLOCK_CALLBACK);
        AttackEntityCallback.EVENT.register((player, world, hand, entity, result) -> BLOCK_CALLBACK.interact(player, world, hand, NONE, player.getMotionDirection()));
    }

    public MusicalInstrumentItem(Properties settings) {
        super(settings);
    }

    public InteractionResult useByEntity(Level world, LivingEntity user, InteractionHand hand) {
        boolean isSneaking = user.isShiftKeyDown();
        ItemStack itemStack = user.getItemInHand(hand);
        List<String> fileNames = TouhouNotaUtils.getFileNames();
        if (fileNames.isEmpty()) {
            if (user instanceof ServerPlayer player) {
                player.displayClientMessage(Component.translatable("item.reverie_dreams.music.no_files"), false);
            }
            return InteractionResult.FAIL;
        }

        String playingMusic = itemStack.getOrDefault(ModDataComponentTypes.PLAYING_MUSIC, null);
        NoteBlockInstrument noteBlockInstrument = itemStack.getOrDefault(ModDataComponentTypes.NOTE_TYPE, NoteBlockInstrument.PLING);

        if (isSneaking) {
            int index = playingMusic == null ? -1 : fileNames.indexOf(playingMusic);
            index = (index + 1) % fileNames.size();
            String next = fileNames.get(index);
            itemStack.set(ModDataComponentTypes.PLAYING_MUSIC, next);
            if (user instanceof ServerPlayer player) {
                player.displayClientMessage(Component.translatable("item.reverie_dreams.music.switch_music", next), false);
            }
        } else {
            if (playingMusic == null) {
                if (user instanceof ServerPlayer player) {
                    player.displayClientMessage(Component.translatable("item.reverie_dreams.music.no_music_selected"), false);
                }
           } else {
                TouhouNotaUtils.play(user, playingMusic, noteBlockInstrument);
                if (user instanceof ServerPlayer player && ReverieDreams.getServer() != null) {
                    player.displayClientMessage(Component.translatable("item.reverie_dreams.music.playing_music", playingMusic, noteBlockInstrument.getSerializedName()), false);
                    ReverieDreams.getServer().executeIfPossible(()-> {
                        AABB box = player.getBoundingBox().inflate(TouhouNotaUtils.MAX_DISTANCE);
                        List<NPCRoleEntity> entities = world.getEntitiesOfClass(
                                NPCRoleEntity.class,
                                box,
                                e -> e.isAlive() && e.isOwnedBy(player) && e.getWorkMode() == NPCWorkModes.PLAYING_MUSIC
                        );
                        for (NPCRoleEntity e : entities) {
                            ItemStack mainHandStack = e.getMainHandItem();
                            ItemStack offHandStack = e.getOffhandItem();
                            if (mainHandStack.getItem() instanceof MusicalInstrumentItem) {
                                mainHandStack.set(ModDataComponentTypes.PLAYING_MUSIC, playingMusic);
                                this.useByEntity(world, e, InteractionHand.MAIN_HAND);
                            } else if (offHandStack.getItem() instanceof MusicalInstrumentItem) {
                                offHandStack.set(ModDataComponentTypes.PLAYING_MUSIC, playingMusic);
                                this.useByEntity(world, e, InteractionHand.OFF_HAND);
                            }
                        }
                    });
                }
            }
        }
        user.swing(hand);
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        if (!world.isClientSide) {
            return this.useByEntity(world, user , hand);
        }
        return super.use(world, user, hand);
    }
}
