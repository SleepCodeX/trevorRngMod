package dev.crestwatch.trapper;

import java.util.List;

public final class CrestHudStateTest {
    private static int checks = 0;
    private static void eq(Object actual, Object expected) {
        checks++;
        if (!java.util.Objects.equals(actual, expected)) {
            throw new AssertionError("Expected " + expected + " but got " + actual);
        }
    }

    public static void main(String[] args) {
        var common = CrestLoreParser.parse("Trapper Crest", List.of(
            "Earns x1.1 Pelts from tracker quests.",
            "Pelts Earned: 37/100",
            "Collect 100 Pelts to upgrade this accessory to UNCOMMON.",
            "COMMON ACCESSORY"
        ), 0).orElseThrow();
        eq(common.tier(), CrestTier.COMMON);
        eq(common.peltsEarned(), 37);
        eq(CrestHudState.fromHotbar(List.of(common)).goal(), 100);

        var uncommon = CrestLoreParser.parse("Trapper Crest", List.of(
            "Earns x1.25 Pelts from tracker quests.", "Pelts Earned: 347/500",
            "Collect 500 Pelts to upgrade this accessory to RARE.", "UNCOMMON ACCESSORY"
        ), 5).orElseThrow();
        eq(uncommon.tier(), CrestTier.UNCOMMON);
        eq(uncommon.peltsEarned(), 347);
        eq(CrestHudState.fromHotbar(List.of(uncommon)).kind(), CrestHudState.Kind.ALMOST_THERE);
        eq(CrestHudState.fromHotbar(List.of(uncommon)).remaining(), 153);

        var rare = CrestLoreParser.parse("Trapper Crest", List.of(
            "Earns x1.5 Pelts from tracker quests.", "Pelts Earned: 500",
            "Increases the odds of finding ENDANGERED or ELUSIVE mobs by 10%.", "RARE ACCESSORY"
        ), 8).orElseThrow();
        eq(rare.tier(), CrestTier.RARE);
        eq(rare.peltsEarned(), 500);
        eq(CrestHudState.fromHotbar(List.of(uncommon, rare)).kind(), CrestHudState.Kind.ALMOST_THERE);
        eq(CrestHudState.fromHotbar(List.of(uncommon)).kind(), CrestHudState.Kind.ALMOST_THERE);
        eq(CrestHudState.fromHotbar(List.of()).kind(), CrestHudState.Kind.MISSING);

        // Even a recombobulated Common/Uncommon uses PERK STAGE rather than display rarity.
        var recombed = CrestLoreParser.parse("Trapper Crest", List.of("Earns x1.1 Pelts from tracker quests.",
            "Pelts Earned: 42", "UNCOMMON ACCESSORY"), 1).orElseThrow();
        eq(recombed.tier(), CrestTier.COMMON);

        var missingCount = CrestLoreParser.parse("Trapper Crest", List.of("Earns x1.25 Pelts from tracker quests."), 2).orElseThrow();
        eq(missingCount.peltsEarned(), -1);
        eq(CrestHudState.fromHotbar(List.of(missingCount)).ratio(), 0.0);

        // Literal tooltip wording from the user's screenshots, including color and the text "Collected: N pelts".
        var screenshotCommon = CrestLoreParser.parse("Trapper Crest", List.of(
            "Earn x1.1 Pelts from tracker quests.",
            "Collect 100 Pelts to upgrade this accessory to UNCOMMON.",
            "Collected: 0 pelts", "COMMON ACCESSORY"
        ), 4).orElseThrow();
        eq(screenshotCommon.tier(), CrestTier.COMMON);
        eq(screenshotCommon.peltsEarned(), 0);
        var screenshotUncommon = CrestLoreParser.parse("Trapper Crest", List.of(
            "Earn x1.25 Pelts from tracker quests.",
            "Collect 500 Pelts to upgrade this accessory to RARE.",
            "§5Collected: §a258 pelts", "UNCOMMON ACCESSORY"
        ), 5).orElseThrow();
        eq(screenshotUncommon.tier(), CrestTier.UNCOMMON);
        eq(screenshotUncommon.peltsEarned(), 258);
        var screenshotRare = CrestLoreParser.parse("Trapper Crest", List.of(
            "Earn x1.5 Pelts from tracker quests.",
            "Grants +10% chance to find ENDANGERED and ELUSIVE mobs.",
            "Collected: 504 pelts", "RARE ACCESSORY"
        ), 6).orElseThrow();
        eq(screenshotRare.tier(), CrestTier.RARE);
        eq(screenshotRare.peltsEarned(), 504);
        eq(CrestHudState.fromHotbar(List.of(screenshotRare, screenshotCommon)).kind(), CrestHudState.Kind.COMMON);
        eq(CrestHudState.fromHotbar(List.of(screenshotCommon)).kind(), CrestHudState.Kind.COMMON);
        eq(CrestHudState.fromHotbar(List.of()).kind(), CrestHudState.Kind.MISSING);

        var rareNoCount = CrestLoreParser.parse("Trapper Crest", List.of("Earns x1.5 Pelts from tracker quests."), 7).orElseThrow();
        eq(rareNoCount.peltsEarned(), 500);
        eq(CrestLoreParser.parse("Scorpion Foil", List.of("Pelts Earned: 500"), 0).isEmpty(), true);
        eq(CrestLoreParser.parse("Trapper Crest", List.of("§6Pelts Earned: §e1,234/500", "RARE ACCESSORY"), 0)
            .orElseThrow().peltsEarned(), 1234);
        // Negative values and missing counts must remain unknown; never invent zero progress.
        var negative = CrestLoreParser.parse("§7Trapper Crest", List.of(
            "Earn x1.25 Pelts from tracker quests.", "Collected: -45 pelts"
        ), 3).orElseThrow();
        eq(negative.peltsEarned(), -1);
        eq(CrestHudState.fromHotbar(List.of(negative)).peltsEarned(), -1);

        var commaCount = CrestLoreParser.parse("Trapper Crest", List.of(
            "Earn x1.25 Pelts from tracker quests.", "§eCollected: 1,234 pelts",
            "§dEPIC ACCESSORY"
        ), 7).orElseThrow();
        eq(commaCount.tier(), CrestTier.UNCOMMON); // Recombobulation does not promote perk.
        eq(commaCount.peltsEarned(), 1234);

        var commonOtherSlot = CrestLoreParser.parse("Trapper Crest", List.of(
            "Earn x1.1 Pelts from tracker quests.", "Collected: 92 pelts"
        ), 1).orElseThrow();
        var mixed = List.of(commonOtherSlot, screenshotUncommon, screenshotRare);
        eq(CrestHudState.fromHotbar(mixed).kind(), CrestHudState.Kind.COMMON);
        eq(CrestHudState.fromHotbar(mixed).hotbarSlot(), 1);
        // Input order and later completed crests cannot override the leftmost crest.
        eq(CrestHudState.fromHotbar(List.of(screenshotRare, screenshotUncommon, commonOtherSlot)).hotbarSlot(), 1);
        eq(CrestHudState.fromHotbar(List.of(commonOtherSlot, screenshotUncommon)).kind(), CrestHudState.Kind.COMMON);
        eq(CrestHudState.fromHotbar(List.of(commonOtherSlot)).kind(), CrestHudState.Kind.COMMON);
        eq(CrestHudState.fromHotbar(List.of()).message(), "Move a Trapper Crest to your hotbar!");
        var firstCompleted = new CrestObservation(CrestTier.UNCOMMON, 500, 0);
        eq(CrestHudState.fromHotbar(List.of(screenshotUncommon, firstCompleted)).kind(), CrestHudState.Kind.SWITCH_CREST);
        eq(CrestHudState.fromHotbar(List.of(screenshotUncommon, firstCompleted)).hotbarSlot(), 0);
        // Removing the active completed crest immediately selects the next unfinished one.
        eq(CrestHudState.fromHotbar(List.of(screenshotUncommon)).kind(), CrestHudState.Kind.ALMOST_THERE);
        var overflow = new CrestObservation(CrestTier.UNCOMMON, 504, 0);
        eq(CrestHudState.fromHotbar(List.of(overflow)).kind(), CrestHudState.Kind.SWITCH_CREST);
        eq(CrestHudState.fromHotbar(java.util.Arrays.asList(null, screenshotRare)).hotbarSlot(), 6);
        for (int slot = 0; slot < 9; slot++) {
            var active = new CrestObservation(CrestTier.COMMON, slot, slot);
            eq(CrestHudState.fromHotbar(List.of(active)).hotbarSlot(), slot);
        }
        for (String text : List.of("Magical Power: +3", "Magic Power: 3", "3 MP", "Provides +3 Magical Power")) {
            eq(CrestLoreParser.parse("Trapper Crest", List.of(text, "Collected: 0 pelts"), 0)
                .orElseThrow().tier(), CrestTier.COMMON);
        }
        var mp5 = CrestLoreParser.parse("Trapper Crest", List.of("§eMP: §a+5", "Collected: 258 pelts"), 2).orElseThrow();
        eq(mp5.tier(), CrestTier.UNCOMMON);
        eq(mp5.peltsEarned(), 258);
        var mp8 = CrestLoreParser.parse("Trapper Crest", List.of("8 Magical Power"), 1).orElseThrow();
        eq(mp8.tier(), CrestTier.RARE);
        eq(CrestHudState.fromHotbar(List.of(mp5, mp8)).kind(), CrestHudState.Kind.SWITCH_CREST);
        eq(CrestLoreParser.parse("Trapper Crest", List.of("Earn x1.25 Pelts from tracker quests.",
            "Magical Power: +8", "Collected: 258 pelts"), 0).orElseThrow().tier(), CrestTier.UNCOMMON);
        for (String invalid : List.of("MP: -8", "MP: 8.5", "Total Magical Power: 8", "80 MP")) {
            eq(CrestLoreParser.parse("Trapper Crest", List.of(invalid), 0).orElseThrow().tier(), CrestTier.COMMON);
        }
        for (String negativeProgress : List.of("Collected: -45/500 pelts", "-45 pelts earned",
                "Pelts Earned: −45/500", "Collected: -1,234 pelts")) {
            eq(CrestLoreParser.parse("Trapper Crest", List.of(negativeProgress), 0)
                .orElseThrow().peltsEarned(), -1);
        }
        System.out.println("PASS: " + checks + " Trapper Crest HUD parsing/state assertions");
    }
}
