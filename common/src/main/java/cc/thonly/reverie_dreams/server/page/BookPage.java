package cc.thonly.reverie_dreams.server.page;

import cc.thonly.reverie_dreams.server.CustomClickActionRegistry;
import cc.thonly.reverie_dreams.server.dialog.*;
import cc.thonly.reverie_dreams.util.LazyFunction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.dialog.action.Action;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

@SuppressWarnings("ALL")
public final class BookPage {
    final Identifier key;
    final RegistryAccess registryAccess;
    final CommonDialogDataBuilder common;
    final ActionButtonBuilder actions;
    @Nullable
    final Identifier prev;

    protected BookPage(Identifier key,
                       RegistryAccess registryAccess,
                       CommonDialogDataBuilder common, ActionButtonBuilder actions,
                       @Nullable Identifier prev) {
        this.key = key;
        this.registryAccess = registryAccess;
        this.common = common;
        this.actions = actions;
        this.prev = prev;
    }

    public void open(ServerPlayer player) {
        DialogEntry dialogEntry = DialogBuilder.builder(builder -> {
            builder.replaceCommonBuilder(this.common);
            builder.replaceActionsBuilder(this.actions);
            builder.common(common -> {
                common.title(Component.translatable(this.titleLangKey()));
                common.setCanCloseWithEscape(true);
                common.setPause(true);
            });
            builder.actions(actions -> {
                ActionBuilder actionBuilder = actions.actionBuilder();
                if (this.prev == null) {
                    CompoundTag compoundTag = new CompoundTag();
                    compoundTag.putString("id", this.key.toString());
                    Action action = actionBuilder.customAll(CustomClickActionRegistry.PAGE_BACK_KEY, compoundTag);
                    actions.addButton(Component.literal("Back"), 180, Optional.of(action));
                } else {
                    CompoundTag compoundTag = new CompoundTag();
                    compoundTag.putString("id", this.prev.toString());
                    Action action = actionBuilder.customAll(CustomClickActionRegistry.PAGE_GOTO_KEY, compoundTag);
                    actions.addButton(Component.literal("Back"), 180, Optional.of(action));
                }
            });
            builder.key(this.key);
        }).get().buildOrThrow();
        dialogEntry.open(player);
    }

    public Identifier key() {
        return this.key;
    }

    public MutableComponent titleKey(Identifier registryKey) {
        return Component.empty().append(Component.translatable(this.titleLangKey()));
    }

    public MutableComponent contentKey(Identifier registryKey) {
        return Component.empty().append(Component.translatable(this.contentLangKey()));
    }

    public String titleLangKey() {
        return this.key.toLanguageKey() + ".title";
    }

    public String contentLangKey() {
        return this.key.toLanguageKey() + ".content";
    }

    public static BookPageBuilder builder(RegistryAccess registryAccess) {
        return new BookPageBuilder(registryAccess);
    }

}
