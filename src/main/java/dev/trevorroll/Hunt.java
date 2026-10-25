package dev.trevorroll;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

/** Only Trevor's assignment is matched; other messages must remain visible. */
public record Hunt(Rarity rarity, String location) {
    private static final Pattern ASSIGNMENT = Pattern.compile(
        "^\\[NPC] Trevor: (?:✆\\s*)?You can find your " +
        "(TRACKABLE|UNTRACKABLE|UNDETECTED|ENDANGERED|ELUSIVE) animal " +
        "(?:near the|in the|near|in) (.+?)[.!]?$", Pattern.CASE_INSENSITIVE);

    public static Optional<Hunt> parse(String text) {
        String plain = text.replaceAll("(?i)§[0-9a-fk-or]", "")
            .replace('\u00a0', ' ').strip();
        var match = ASSIGNMENT.matcher(plain);
        if (!match.matches()) return Optional.empty();
        String location = match.group(2).strip();
        if (location.isEmpty() || location.equals(".") || location.equals("!")) return Optional.empty();
        return Optional.of(new Hunt(Rarity.valueOf(match.group(1).toUpperCase(Locale.ROOT)), location));
    }

    public enum Rarity {
        TRACKABLE(0xFFF1F3F7), UNTRACKABLE(0xFF55FF55), UNDETECTED(0xFF5599FF),
        ENDANGERED(0xFFAA55FF), ELUSIVE(0xFFFFAA00);

        public final int color;
        Rarity(int color) { this.color = color; }
    }
}
