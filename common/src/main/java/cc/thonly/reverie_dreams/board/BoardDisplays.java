package cc.thonly.reverie_dreams.board;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.decor.BoardGameBlock;
import cc.thonly.reverie_dreams.block.entity.BoardGameBlockEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.*;

import java.util.*;

/**
 * Identical local transforms and model IDs for native rendering and Polymer display elements.
 */
public final class BoardDisplays {
    public record Part(ItemStack stack, double x, double y, double z, float scale, float yaw) {
    }

    private BoardDisplays() {
    }

    public static ItemStack model(String path) {
        var stack = new ItemStack(Items.PAPER);
        stack.set(DataComponents.ITEM_MODEL, ReverieDreams.id(path));
        return stack;
    }

    public static List<Part> parts(BoardGameBlockEntity host) {
        return host.displayParts();
    }

    public static List<Part> buildParts(BoardGameBlockEntity host) {
        if (host.game() == null)
            return List.of();
        var board = host.game().position;
        List<Part> parts = new ArrayList<>();
        float yaw = switch (host.facing()) {
            case EAST -> 90;
            case SOUTH -> 180;
            case WEST -> 270;
            default -> 0;
        };
        double base = host.surface();
        if (!(host.getBlockState().getBlock() instanceof BoardGameBlock))
            parts.add(new Part(model("board/" + board.kind.id), .5, base + .5, .5, 1, yaw));
        double cell = 12.0 / 16 / Math.max(board.kind.width - 1, board.kind.height - 1);
        if (board.kind == BoardKind.CHESS)
            cell = 12.0 / 16 / 8;
        float scale = (float) (cell * .82);
        double angle = Math.toRadians(yaw), sin = Math.sin(angle), cos = Math.cos(angle);
        for (int square = 0; square < board.size(); square++)
            if (board.at(square) != 0) {
                double x = board.kind == BoardKind.CHESS ? (square % 8 - 3.5) * cell : (square % board.kind.width - (board.kind.width - 1) * .5) * cell;
                double z = board.kind == BoardKind.CHESS ? (square / 8 - 3.5) * cell : (square / board.kind.width - (board.kind.height - 1) * .5) * cell;
                String model = "board/piece/" + (board.kind == BoardKind.GO ? "go" : board.kind.name().toLowerCase(Locale.ROOT)) + "_" + (board.at(square) > 0 ? "first" : "second") + "_" + Math.abs(board.at(square));
                parts.add(new Part(model(model), .5 + x * cos - z * sin, base + 1.05 / 16 + scale * .5, .5 + x * sin + z * cos, scale, yaw + (board.at(square) < 0 ? 180 : 0)));
            }
        return parts;
    }
}
