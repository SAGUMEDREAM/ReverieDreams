package cc.thonly.reverie_dreams.util.command;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.permissions.Permissions;

import java.util.function.Predicate;

public interface PermissionPredicates {

    static Predicate<CommandSourceStack> all() {
        return ctx -> true;
    }

    static Predicate<CommandSourceStack> isModerator() {
        return ctx -> ctx.permissions().hasPermission(Permissions.COMMANDS_MODERATOR);
    }

    static Predicate<CommandSourceStack> isAdmin() {
        return ctx -> ctx.permissions().hasPermission(Permissions.COMMANDS_ADMIN);
    }

    static Predicate<CommandSourceStack> isGameMasters() {
        return ctx -> ctx.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
    }

    static Predicate<CommandSourceStack> isOwner() {
        return ctx -> ctx.permissions().hasPermission(Permissions.COMMANDS_OWNER);
    }
}
