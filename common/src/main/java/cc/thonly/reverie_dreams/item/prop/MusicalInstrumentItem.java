package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuTrajectory;
import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import cc.thonly.reverie_dreams.registry.content.NPCWorkModes;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.sound.RDSoundEvents;
import cc.thonly.reverie_dreams.util.nbs.NotaUtils;
import cc.thonly.reverie_dreams.util.advancements.SimpleTriggerFactory;
import cc.thonly.reverie_dreams.util.advancements.SimpleTriggerKeys;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class MusicalInstrumentItem extends Item {
    public static final Codec<NoteBlockInstrument> NOTE_BLOCK_INSTRUMENT_CODEC = StringRepresentable.fromEnum(NoteBlockInstrument::values);

    public MusicalInstrumentItem(Properties settings) {
        super(settings);
    }

    public InteractionResult useByEntity(Level world, LivingEntity user, InteractionHand hand) {
        ItemStack itemByMainSlot = user.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack itemByOffSlot = user.getItemInHand(InteractionHand.OFF_HAND);
        if (itemByOffSlot.getItem() == DanmakuTypes.NOTE.getItemHolder().asItem()) {
            ItemStack baseBullet = itemByOffSlot.copy();
            if (user instanceof Player player) {
                ItemCooldowns itemCooldownManager = player.getCooldowns();
                itemCooldownManager.addCooldown(itemByMainSlot, 20 * 7);
            }

            float pitch = user.getXRot();
            float yaw = user.getYRot();

            final int count = 12;
            final float spread = 20f;

            Random random = ThreadLocalRandom.current();

            for (int i = 0; i < count; i++) {
                float pitchOffset = (float) (random.nextGaussian() * spread * 0.4f);
                float yawOffset = (float) (random.nextGaussian() * spread);

                float speed = 0.7f + random.nextFloat() * 0.2f;

                ItemStack randomStack = DanmakuTypes.random(DanmakuTypes.NOTE).create();
                DanmakuProperties properties = baseBullet.get(RDDataComponentTypes.DANMAKU_PROPERTIES.value());
                if (properties != null) {
                    randomStack.set(RDDataComponentTypes.DANMAKU_PROPERTIES.value(), properties);
                }
                DanmakuTrajectory.spawnByItemStack(
                        (ServerLevel) world,
                        user,
                        user.getX(),
                        user.getY(),
                        user.getZ(),
                        randomStack,
                        pitch + pitchOffset,
                        yaw + yawOffset,
                        0.0f,
                        speed
                );
            }
            user.swing(hand);
            world.playSound(null, user.getX(), user.getY(), user.getZ(), RDSoundEvents.FIRE, SoundSource.NEUTRAL, 1f, 1.0f);
            return InteractionResult.PASS;
        }

        boolean isSneaking = user.isShiftKeyDown();
        ItemStack itemStack = user.getItemInHand(hand);
        List<String> fileNames = NotaUtils.getFileNames();
        if (fileNames.isEmpty()) {
            if (user instanceof ServerPlayer player) {
                player.sendSystemMessage(Component.translatable("item.reverie_dreams.music.no_files"), false);
            }
            return InteractionResult.FAIL;
        }

        String playingMusic = itemStack.get(RDDataComponentTypes.PLAYING_MUSIC.value());
        NoteBlockInstrument noteBlockInstrument = itemStack.getOrDefault(RDDataComponentTypes.NOTE_TYPE.value(), NoteBlockInstrument.PLING);

        if (isSneaking) {
            int index = playingMusic == null ? -1 : fileNames.indexOf(playingMusic);
            index = (index + 1) % fileNames.size();
            String next = fileNames.get(index);
            itemStack.set(RDDataComponentTypes.PLAYING_MUSIC.value(), next);
            if (user instanceof ServerPlayer player) {
                player.sendSystemMessage(Component.translatable("item.reverie_dreams.music.switch_music", next), false);
                if (NotaUtils.isPlaying(player)) {
                    NotaUtils.play(user, next, noteBlockInstrument);
                }
            }
        } else {
            if (playingMusic == null) {
                if (user instanceof ServerPlayer player) {
                    player.sendSystemMessage(Component.translatable("item.reverie_dreams.music.no_music_selected"), false);
                }
            } else {
                NotaUtils.play(user, playingMusic, noteBlockInstrument);
                if (user instanceof ServerPlayer player && ReverieDreams.getServer() != null) {
                    player.sendSystemMessage(Component.translatable("item.reverie_dreams.music.playing_music", playingMusic, noteBlockInstrument.getSerializedName()), false);
                    ReverieDreams.getServer().executeIfPossible(() -> playForMaidEntity(world, player, playingMusic));
                }
                if (user instanceof ServerPlayer player) {
                    SimpleTriggerFactory.create(SimpleTriggerKeys.USE_MUSICAL_INSTRUMENTS).trigger(player);
                }
            }
        }
        user.swing(hand);
        return InteractionResult.SUCCESS_SERVER;
    }

    private void playForMaidEntity(Level world, ServerPlayer player, String playingMusic) {
        AABB box = player.getBoundingBox().inflate(NotaUtils.MAX_DISTANCE);
        List<NPCSimpleEntity> entities = world.getEntitiesOfClass(
                NPCSimpleEntity.class,
                box,
                e -> e.isAlive() && e.isOwnedBy(player) && e.getWorkMode() == NPCWorkModes.PLAYING_MUSIC
        );
        for (NPCSimpleEntity e : entities) {
            ItemStack mainHandStack = e.getMainHandItem();
            ItemStack offHandStack = e.getOffhandItem();
            if (mainHandStack.getItem() instanceof MusicalInstrumentItem) {
                mainHandStack.set(RDDataComponentTypes.PLAYING_MUSIC.value(), playingMusic);
                this.useByEntity(world, e, InteractionHand.MAIN_HAND);
            } else if (offHandStack.getItem() instanceof MusicalInstrumentItem) {
                offHandStack.set(RDDataComponentTypes.PLAYING_MUSIC.value(), playingMusic);
                this.useByEntity(world, e, InteractionHand.OFF_HAND);
            }
        }
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        if (!world.isClientSide()) {
            return this.useByEntity(world, user, hand);
        }
        return InteractionResult.SUCCESS;
    }
}
