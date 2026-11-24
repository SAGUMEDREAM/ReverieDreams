package cc.thonly.reverie_dreams.workshop;

import cc.thonly.reverie_dreams.block.decor.workshop.WorkshopKind;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Feedback for completed actions only, broadcast once by the server. */
public final class FurnitureSounds {
    public static void play(Level level, BlockPos pos, SoundEvent sound, float volume, float pitch) {
        if (level instanceof ServerLevel && !level.isClientSide())
            level.playSound(null, pos, sound, SoundSource.BLOCKS, volume, pitch);
    }

    public static void transfer(BlockEntity host, WorkshopKind kind, boolean taking) {
        SoundEvent sound = switch (kind) {
            case BOOK_STACK, BOOK_BOX, SCROLL_RACK -> taking
                    ? SoundEvents.CHISELED_BOOKSHELF_PICKUP : SoundEvents.CHISELED_BOOKSHELF_INSERT;
            case HERB_DRYING_TRAY, INGREDIENT_BASKET -> taking
                    ? SoundEvents.BUNDLE_REMOVE_ONE : SoundEvents.BUNDLE_INSERT;
            case FLOWER_VASE, MEDICINE_JAR_RACK, SAKE_CASK_RACK -> SoundEvents.DECORATED_POT_INSERT;
            case SUZURI, YAGEN, STONE_MILL -> SoundEvents.STONE_PLACE;
            default -> taking ? SoundEvents.ITEM_FRAME_REMOVE_ITEM : SoundEvents.ITEM_FRAME_ADD_ITEM;
        };
        play(host.getLevel(), host.getBlockPos(), sound, .35f, taking ? 1.1f : .95f);
    }

    public static void read(Level level, BlockPos pos) {
        play(level, pos, SoundEvents.BOOK_PAGE_TURN, .35f, 1.0f);
    }

    public static void write(BlockEntity host) {
        play(host.getLevel(), host.getBlockPos(), SoundEvents.BOOK_PUT, .4f, 1.1f);
    }

    public static void sort(BlockEntity host) {
        play(host.getLevel(), host.getBlockPos(), SoundEvents.BUNDLE_INSERT, .3f, .85f);
    }

    private FurnitureSounds() {}
}
