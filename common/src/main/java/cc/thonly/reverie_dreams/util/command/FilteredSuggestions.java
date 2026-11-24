package cc.thonly.reverie_dreams.util.command;

import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.resources.Identifier;

import java.util.Locale;

/**
 * Filtering belongs to each provider; SuggestionsBuilder itself does not filter candidates.
 */
public final class FilteredSuggestions {
    private FilteredSuggestions() {
    }

    public static void resource(SuggestionsBuilder builder, Identifier id) {
        String query = builder.getRemainingLowerCase();
        boolean matches = query.contains(":")
                ? SharedSuggestionProvider.matchesSubStr(query, id.toString())
                : SharedSuggestionProvider.matchesSubStr(query, id.getPath())
                || SharedSuggestionProvider.matchesSubStr(query, id.getNamespace());
        if (matches)
            builder.suggest(id.toString());
    }

    public static void value(SuggestionsBuilder builder, String value) {
        if (SharedSuggestionProvider.matchesSubStr(builder.getRemainingLowerCase(), value.toLowerCase(Locale.ROOT)))
            builder.suggest(value);
    }

    public static void file(SuggestionsBuilder builder, String file) {
        String remaining = builder.getRemaining();
        boolean quoted = remaining.startsWith("\"");
        String prefix = quoted ? remaining.substring(1) : remaining;
        if (prefix.endsWith("\"") && quoted)
            prefix = prefix.substring(0, prefix.length() - 1);
        prefix = prefix.replace("\\\"", "\"").replace("\\\\", "\\");
        if (!file.toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase(Locale.ROOT)))
            return;
        builder.suggest("\"" + file.replace("\\", "\\\\").replace("\"", "\\\"") + "\"");
    }
}
