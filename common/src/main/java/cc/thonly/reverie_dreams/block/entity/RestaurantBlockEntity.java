package cc.thonly.reverie_dreams.block.entity;

import cc.thonly.reverie_dreams.block.restaurant.*;
import cc.thonly.reverie_dreams.entity.ai.work.WorkArea;
import cc.thonly.reverie_dreams.registry.content.block.entity.RDBlockEntityTypes;
import cc.thonly.reverie_dreams.restaurant.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Both facilities have persistent identities; binding cannot accidentally target a replacement block.
 */
public final class RestaurantBlockEntity extends BlockEntity {
    private UUID id = UUID.randomUUID(), owner;
    private BlockPos linked;
    private UUID linkedId;
    private int radius = 8;
    private final RestaurantMenu menu = new RestaurantMenu();

    public RestaurantBlockEntity(BlockPos pos, BlockState state) {
        super(RDBlockEntityTypes.RESTAURANT.value(), pos, state);
    }

    public UUID id() {
        return id;
    }

    public UUID owner() {
        return owner;
    }

    public boolean isBell() {
        return getBlockState().getBlock() instanceof MystiaBellBlock;
    }

    public RestaurantMenu menu() {
        return menu;
    }

    public void menuChanged() {
        setChanged();
        var bell = linked();
        if (!isBell() && bell != null && bell.isBell() && bell.open() && !bell.ready()) {
            bell.setOpen(false);
            bell.setChanged();
            setOpen(false);
        }
    }

    public int radius() {
        return radius;
    }

    public UUID linkedId() {
        return linkedId;
    }

    public void radius(int radius) {
        if (!open()) {
            this.radius = Math.clamp(radius, 2, 16);
            setChanged();
        }
    }

    public boolean open() {
        return getBlockState().getValue(RestaurantBlock.OPEN);
    }

    public WorkArea area() {
        return new WorkArea(worldPosition, radius, 8);
    }

    public boolean canEdit(Player player) {
        return !player.isSpectator() && (owner == null || owner.equals(player.getUUID()) || player.isCreative());
    }

    public void claim(Player player) {
        if (owner == null) {
            owner = player.getUUID();
            setChanged();
        }
    }

    public RestaurantBlockEntity linked() {
        if (level == null || linked == null || !level.hasChunkAt(linked))
            return null;
        return level.getBlockEntity(linked) instanceof RestaurantBlockEntity other && other.id.equals(linkedId) && !other.isRemoved() ? other : null;
    }

    public boolean bind(RestaurantBlockEntity book, ServerPlayer player) {
        if (!isBell() || open() || !canEdit(player) || book.isBell() || book.level != level || !book.canEdit(player)
                || !area().contains(book.worldPosition))
            return false;
        if (book.linked != null && !book.linked.equals(worldPosition)
                && (!level.hasChunkAt(book.linked) || book.linked() != null))
            return false;
        var previous = linked();
        if (previous != null) {
            previous.linked = null;
            previous.linkedId = null;
            previous.setOpen(false);
            previous.setChanged();
        }
        linked = book.worldPosition.immutable();
        linkedId = book.id;
        book.linked = worldPosition.immutable();
        book.linkedId = id;
        book.claim(player);
        book.setOpen(false);
        book.setChanged();
        setChanged();
        return true;
    }

    public boolean ready() {
        return openingIssue() == null;
    }

    @Nullable
    public OpeningIssue openingIssue() {
        var book = linked();
        if (!isBell() || book == null || book.isBell() || book.linked() != this)
            return OpeningIssue.MENU;
        if (!area().contains(book.worldPosition))
            return OpeningIssue.RANGE;

        boolean food = book.menu.hasAvailable(false, id -> RestaurantService.valid(id, false));
        boolean drinks = book.menu.hasAvailable(true, id -> RestaurantService.valid(id, true));
        if (!food)
            return drinks ? OpeningIssue.FOOD : OpeningIssue.FOOD_AND_DRINK;
        return drinks ? null : OpeningIssue.DRINK;
    }

    public enum OpeningIssue {
        MENU("missing_menu"),
        RANGE("menu_out_of_range"),
        FOOD("missing_food"),
        DRINK("missing_drink"),
        FOOD_AND_DRINK("missing_food_and_drink");

        private final String key;

        OpeningIssue(String key) {
            this.key = "restaurant.reverie_dreams." + key;
        }

        public Component label() {
            return Component.translatable(key);
        }

        public Component hint() {
            return Component.translatable(key + "_hint");
        }
    }

    public void toggleBusiness(ServerPlayer player) {
        if (!canEdit(player) || !isBell())
            return;
        boolean opening = !open();
        var issue = opening ? openingIssue() : null;
        if (issue != null) {
            player.sendSystemMessage(issue.hint());
            return;
        }
        if (!setOpen(opening))
            return;
        var book = linked();
        if (book != null)
            book.setOpen(opening);
        level.playSound(null, worldPosition, SoundEvents.BELL_BLOCK, SoundSource.BLOCKS, 2, opening ? 1.2f : .75f);
        player.sendSystemMessage(Component.translatable("restaurant.reverie_dreams." + (opening ? "opened" : "closed")));
        setChanged();
    }

    private boolean setOpen(boolean open) {
        return level != null && level.setBlockAndUpdate(worldPosition, getBlockState().setValue(RestaurantBlock.OPEN, open));
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null && !level.isClientSide()) {
            var other = linked();
            if (other != null && id.equals(other.linkedId)) {
                other.linked = null;
                other.linkedId = null;
                other.setOpen(false);
                other.setChanged();
            }
        }
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.putString("RestaurantId", id.toString());
        if (owner != null)
            out.putString("Owner", owner.toString());
        out.putInt("Radius", radius);
        if (linked != null && linkedId != null) {
            out.putLong("LinkedPos", linked.asLong());
            out.putString("LinkedId", linkedId.toString());
        }
        out.store("Foods", Identifier.CODEC.listOf(), menu.entries(false));
        out.store("Drinks", Identifier.CODEC.listOf(), menu.entries(true));
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        id = uuid(in.getStringOr("RestaurantId", ""), UUID.randomUUID());
        owner = uuid(in.getStringOr("Owner", ""), null);
        radius = Math.clamp(in.getIntOr("Radius", 8), 2, 16);
        linked = in.getLong("LinkedPos").map(BlockPos::of).orElse(null);
        linkedId = uuid(in.getStringOr("LinkedId", ""), null);
        menu.load(false, in.read("Foods", Identifier.CODEC.listOf()).orElse(List.of()));
        menu.load(true, in.read("Drinks", Identifier.CODEC.listOf()).orElse(List.of()));
    }

    public static UUID uuid(String value, UUID fallback) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }
}
