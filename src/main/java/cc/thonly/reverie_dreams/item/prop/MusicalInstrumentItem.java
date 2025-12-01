package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuTrajectory;
import cc.thonly.reverie_dreams.entity.npc.NPCRoleEntity;
import cc.thonly.reverie_dreams.registry.content.NPCWorkModes;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.sound.SoundEventInit;
import cc.thonly.reverie_dreams.util.TouhouNotaUtils;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.core.BlockPos;
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

                String playingMusic = stack.get(RDDataComponents.PLAYING_MUSIC);
                int index = playingMusic == null ? -1 : fileNames.indexOf(playingMusic);
                index = (index - 1 + fileNames.size()) % fileNames.size(); // 向上翻页

                String previous = fileNames.get(index);
                stack.set(RDDataComponents.PLAYING_MUSIC, previous);
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
        ItemStack itemByMainSlot = user.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack itemByOffSlot = user.getItemInHand(InteractionHand.OFF_HAND);
        if (itemByOffSlot.getItem() == DanmakuTypes.NOTE.getItem()) {
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

                ItemStack randomStack = DanmakuTypes.random(DanmakuTypes.NOTE);
                DanmakuProperties properties = baseBullet.get(RDDataComponents.DANMAKU_PROPERTIES);
                if (properties != null) {
                    randomStack.set(RDDataComponents.DANMAKU_PROPERTIES, properties);
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
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEventInit.FIRE, SoundSource.NEUTRAL, 1f, 1.0f);
            return InteractionResult.PASS;
        }

        boolean isSneaking = user.isShiftKeyDown();
        ItemStack itemStack = user.getItemInHand(hand);
        List<String> fileNames = TouhouNotaUtils.getFileNames();
        if (fileNames.isEmpty()) {
            if (user instanceof ServerPlayer player) {
                player.displayClientMessage(Component.translatable("item.reverie_dreams.music.no_files"), false);
            }
            return InteractionResult.FAIL;
        }

        String playingMusic = itemStack.get(RDDataComponents.PLAYING_MUSIC);
        NoteBlockInstrument noteBlockInstrument = itemStack.getOrDefault(RDDataComponents.NOTE_TYPE, NoteBlockInstrument.PLING);

        if (isSneaking) {
            int index = playingMusic == null ? -1 : fileNames.indexOf(playingMusic);
            index = (index + 1) % fileNames.size();
            String next = fileNames.get(index);
            itemStack.set(RDDataComponents.PLAYING_MUSIC, next);
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
                    ReverieDreams.getServer().executeIfPossible(() -> {
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
                                mainHandStack.set(RDDataComponents.PLAYING_MUSIC, playingMusic);
                                this.useByEntity(world, e, InteractionHand.MAIN_HAND);
                            } else if (offHandStack.getItem() instanceof MusicalInstrumentItem) {
                                offHandStack.set(RDDataComponents.PLAYING_MUSIC, playingMusic);
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
            return this.useByEntity(world, user, hand);
        }
        return super.use(world, user, hand);
    }
}
