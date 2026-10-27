package dev.crestwatch.trapper;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses only text already provided by the server in the item's lore.
 * No network calls, external APIs, chat parsing, or estimate-based counters.
 * Designed for Hypixel's post-April-2026 100/500-Pelt Trapper Crest.
 */
public final class CrestLoreParser {
    private CrestLoreParser() {}

    private static final Pattern PELTS_EARNED = Pattern.compile(
        "(?i)\\bpelts?\\s*(?:earned|gained|collected|obtained)\\s*[:：]?\\s*([+\\-−]?[0-9][\\d,]*)"
    );
    // Exact 2026 Hypixel tooltip: "Collected: 258 pelts" (also "Collected: 504 pelts").
    private static final Pattern COLLECTED = Pattern.compile(
        "(?i)\\bcollected\\s*[:：]?\\s*([+\\-−]?[0-9][\\d,]*)\\s*pelts?\\b"
    );
    private static final Pattern EARNED_PELTS = Pattern.compile(
        "(?i)(?<![\\p{L}\\d.,+\\-−])([+\\-−]?[0-9][\\d,]*)\\s*pelts?\\s*(?:earned|gained|collected|obtained)\\b"
    );
    private static final Pattern PROGRESS = Pattern.compile(
        "(?i)(?<![\\p{L}\\d.,+\\-−])([+\\-−]?[0-9][\\d,]*)\\s*/\\s*(?:100|500)\\s*(?:pelts?)?\\b"
    );
    private static final Pattern RARITY = Pattern.compile(
        "(?i)^\\s*(?:\\b(?:SHINY|VERY|RECOMBOBULATED)\\s+)?(COMMON|UNCOMMON|RARE|EPIC|LEGENDARY|MYTHIC)\\s+ACCESSORY\\b"
    );
    private static final Pattern LEGACY_COLOR = Pattern.compile("(?i)§[0-9A-FK-ORX]");
    private static final Pattern MP_PREFIX = Pattern.compile(
        "^(?:magical power|magic power|mp)\\s*[:：]?\\s*\\+?(3|5|8)(?:\\s.*)?$"
    );
    private static final Pattern MP_SUFFIX = Pattern.compile(
        "^(?:(?:provides|grants)\\s+)?\\+?(3|5|8)\\s*(?:magical power|magic power|mp)\\b.*$"
    );

    public static Optional<CrestObservation> parse(String visibleName, List<String> loreLines, int hotbarSlot) {
        if (visibleName == null || !clean(visibleName).contains("trapper crest")) {
            return Optional.empty();
        }
        List<String> lines = (loreLines == null) ? List.of() : loreLines;
        String lore = String.join("\n", lines.stream().map(CrestLoreParser::clean).toList());
        String simplified = lore.replace("×", "x");
        String lower = simplified.toLowerCase(Locale.ROOT);

        int pelts = readEarnedPelts(lower);
        CrestTier stage = stageFromPerkAndProgress(lower, lines);

        // Rare is inherently completed. Pelts may no longer appear in its lore.
        if (stage == CrestTier.RARE && pelts < 0) pelts = 500;
        return Optional.of(new CrestObservation(stage, pelts, hotbarSlot));
    }

    private static String clean(String input) {
        return LEGACY_COLOR.matcher(input.replace('\u00a0', ' '))
            .replaceAll("")
            .replaceAll("\\s+", " ")
            .trim()
            .toLowerCase(Locale.ROOT);
    }

    private static int readEarnedPelts(String lore) {
        Matcher m = COLLECTED.matcher(lore);
        if (m.find()) return parseCount(m.group(1));
        m = PELTS_EARNED.matcher(lore);
        if (m.find()) return parseCount(m.group(1));
        m = EARNED_PELTS.matcher(lore);
        if (m.find()) return parseCount(m.group(1));
        m = PROGRESS.matcher(lore);
        if (m.find()) return parseCount(m.group(1));
        return -1; // Unknown is NOT a fabricated zero.
    }

    private static int parseCount(String digits) {
        try {
            long value = Long.parseLong(digits.replace(",", ""));
            if (value < 0) return -1;
            return (int) Math.min(value, Integer.MAX_VALUE);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static CrestTier stageFromPerkAndProgress(String lore, List<String> rawLines) {
        // Perks reveal the actual progression stage even if the crest was recombobulated.
        if (lore.contains("x1.5") || lore.contains("1.5x") || lore.contains("x 1.5")) return CrestTier.RARE;
        if (lore.contains("x1.25") || lore.contains("1.25x") || lore.contains("x 1.25")) return CrestTier.UNCOMMON;
        if (lore.contains("x1.1") || lore.contains("1.1x") || lore.contains("x 1.1")) return CrestTier.COMMON;

        // Explicit 'collect ... to upgrade' text is more reliable than the rarity label.
        if (lore.matches("(?s).*collect\\s+(?:another\\s+)?500\\s+pelts?\\s+to\\s+upgrade.*")) return CrestTier.UNCOMMON;
        if (lore.matches("(?s).*collect\\s+(?:another\\s+)?100\\s+pelts?\\s+to\\s+upgrade.*")) return CrestTier.COMMON;

        // Fallback for abbreviated tooltips that display only a progress denominator.
        if (lore.matches("(?s).*\\b[\\d,]+\\s*/\\s*500\\b.*")) return CrestTier.UNCOMMON;
        if (lore.matches("(?s).*\\b[\\d,]+\\s*/\\s*100\\b.*")) return CrestTier.COMMON;

        // Per-item MP is useful when progression lore is absent. Explicit perk/upgrade
        // evidence above wins because recombobulation changes rarity and MP.
        for (String line : rawLines) {
            String text = clean(line);
            Matcher mp = MP_PREFIX.matcher(text);
            if (!mp.matches()) mp = MP_SUFFIX.matcher(text);
            if (mp.matches()) return switch (mp.group(1)) {
                case "3" -> CrestTier.COMMON;
                case "5" -> CrestTier.UNCOMMON;
                default -> CrestTier.RARE;
            };
        }

        // Last resort: the rarity in the last-line accessory heading.
        for (int i = rawLines.size() - 1; i >= 0; i--) {
            Matcher m = RARITY.matcher(clean(rawLines.get(i)));
            if (m.find()) {
                return switch (m.group(1).toUpperCase(Locale.ROOT)) {
                    case "COMMON" -> CrestTier.COMMON;
                    case "UNCOMMON" -> CrestTier.UNCOMMON;
                    default -> CrestTier.RARE;
                };
            }
        }
        return CrestTier.COMMON;
    }
}
