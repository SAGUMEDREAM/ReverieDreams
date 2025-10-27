package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.component.ModDataComponentTypes;
import cc.thonly.reverie_dreams.entity.npc.NPCRoleEntity;
import cc.thonly.reverie_dreams.entity.npc.NPCWorkModes;
import cc.thonly.reverie_dreams.util.TouhouNotaUtils;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;

public class MusicalInstrumentItem extends Item {
    public static final Codec<NoteBlockInstrument> NOTE_BLOCK_INSTRUMENT_CODEC = StringIdentifiable.createCodec(NoteBlockInstrument::values);
    public static final BlockPos NONE = new BlockPos(0, 0, 0);
    public static final AttackBlockCallback BLOCK_CALLBACK = (player, world, hand, blockPos, direction) -> {
        if (!world.isClient() && world instanceof ServerWorld serverWorld) {
            ItemStack mainStack = player.getMainHandStack();
            ItemStack offStack = player.getOffHandStack();
            ItemStack stack = null;

            if (mainStack.getItem() instanceof MusicalInstrumentItem) {
                stack = mainStack;
            } else if (offStack.getItem() instanceof MusicalInstrumentItem) {
                stack = offStack;
            }

            if (stack != null && !player.isSpectator() && player.isSneaking()) {
                List<String> fileNames = TouhouNotaUtils.getFileNames();
                if (fileNames.isEmpty()) {
                    player.sendMessage(Text.translatable("item.reverie_dreams.music.no_files"), false);
                    return ActionResult.SUCCESS;
                }

                String playingMusic = stack.getOrDefault(ModDataComponentTypes.PLAYING_MUSIC, null);
                int index = playingMusic == null ? -1 : fileNames.indexOf(playingMusic);
                index = (index - 1 + fileNames.size()) % fileNames.size(); // 向上翻页

                String previous = fileNames.get(index);
                stack.set(ModDataComponentTypes.PLAYING_MUSIC, previous);
                player.sendMessage(Text.translatable("item.reverie_dreams.music.switch_music", previous), false);
                player.swingHand(hand);

                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.PASS;
    };

    static {
        AttackBlockCallback.EVENT.register(BLOCK_CALLBACK);
        AttackEntityCallback.EVENT.register((player, world, hand, entity, result) -> BLOCK_CALLBACK.interact(player, world, hand, NONE, player.getMovementDirection()));
    }

    public MusicalInstrumentItem(Settings settings) {
        super(settings);
    }

    public ActionResult useByEntity(World world, LivingEntity user, Hand hand) {
        boolean isSneaking = user.isSneaking();
        ItemStack itemStack = user.getStackInHand(hand);
        List<String> fileNames = TouhouNotaUtils.getFileNames();
        if (fileNames.isEmpty()) {
            if (user instanceof ServerPlayerEntity player) {
                player.sendMessage(Text.translatable("item.reverie_dreams.music.no_files"), false);
            }
            return ActionResult.FAIL;
        }

        String playingMusic = itemStack.getOrDefault(ModDataComponentTypes.PLAYING_MUSIC, null);
        NoteBlockInstrument noteBlockInstrument = itemStack.getOrDefault(ModDataComponentTypes.NOTE_TYPE, NoteBlockInstrument.PLING);

        if (isSneaking) {
            int index = playingMusic == null ? -1 : fileNames.indexOf(playingMusic);
            index = (index + 1) % fileNames.size();
            String next = fileNames.get(index);
            itemStack.set(ModDataComponentTypes.PLAYING_MUSIC, next);
            if (user instanceof ServerPlayerEntity player) {
                player.sendMessage(Text.translatable("item.reverie_dreams.music.switch_music", next), false);
            }
        } else {
            if (playingMusic == null) {
                if (user instanceof ServerPlayerEntity player) {
                    player.sendMessage(Text.translatable("item.reverie_dreams.music.no_music_selected"), false);
                }
           } else {
                TouhouNotaUtils.play(user, playingMusic, noteBlockInstrument);
                if (user instanceof ServerPlayerEntity player && Touhou.getServer() != null) {
                    player.sendMessage(Text.translatable("item.reverie_dreams.music.playing_music", playingMusic, noteBlockInstrument.asString()), false);
                    Touhou.getServer().executeSync(()-> {
                        Box box = player.getBoundingBox().expand(TouhouNotaUtils.MAX_DISTANCE);
                        List<NPCRoleEntity> entities = world.getEntitiesByClass(
                                NPCRoleEntity.class,
                                box,
                                e -> e.isAlive() && e.isOwner(player) && e.getWorkMode() == NPCWorkModes.PLAYING_MUSIC
                        );
                        for (NPCRoleEntity e : entities) {
                            ItemStack mainHandStack = e.getMainHandStack();
                            ItemStack offHandStack = e.getOffHandStack();
                            if (mainHandStack.getItem() instanceof MusicalInstrumentItem) {
                                mainHandStack.set(ModDataComponentTypes.PLAYING_MUSIC, playingMusic);
                                this.useByEntity(world, e, Hand.MAIN_HAND);
                            } else if (offHandStack.getItem() instanceof MusicalInstrumentItem) {
                                offHandStack.set(ModDataComponentTypes.PLAYING_MUSIC, playingMusic);
                                this.useByEntity(world, e, Hand.OFF_HAND);
                            }
                        }
                    });
                }
            }
        }
        user.swingHand(hand);
        return ActionResult.SUCCESS_SERVER;
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient) {
            return this.useByEntity(world, user , hand);
        }
        return super.use(world, user, hand);
    }
}
