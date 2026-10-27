package dev.trevorroll.test;

import dev.crestwatch.trapper.CrestHudConfig;
import dev.crestwatch.trapper.CrestHudState;
import dev.crestwatch.trapper.TrapperCrestHudClient;
import dev.trevorroll.TrevorRollClient;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

import java.util.List;

/** Runs against the packaged mod in a real client, with a disposable local world. */
public final class TrevorRollClientGameTest implements FabricClientGameTest {
    @Override public void runTest(ClientGameTestContext context) {
        try { runChecks(context); }
        catch (Exception e) { throw new AssertionError("Client validation failed", e); }
    }

    private void runChecks(ClientGameTestContext context) throws Exception {
        require(FabricLoader.getInstance().getModContainer("minecraft").orElseThrow()
            .getMetadata().getVersion().getFriendlyString().equals("26.2"), "Minecraft version");
        require(FabricLoader.getInstance().getModContainer("fabricloader").orElseThrow()
            .getMetadata().getVersion().getFriendlyString().equals("0.19.5"), "Loader version");
        context.takeScreenshot("startup");
        try (var world = context.worldBuilder().create()) {
            world.getConnection().waitForChunksRender();
            context.setScreen(() -> null);
            context.runOnClient(mc -> {
                var config = config();
                config.onlyHypixel = false;
                config.enabled = true;
                for (int slot = 0; slot < 9; slot++) mc.player.getInventory().setItem(slot, ItemStack.EMPTY);
                // Exercise every hotbar position with real ItemLore components.
                for (int slot = 0; slot < 9; slot++) {
                    mc.player.getInventory().setItem(slot, crest("1.1", 0));
                    require(readHotbar(mc).hotbarSlot() == slot, "Hotbar slot " + slot);
                    require(readHotbar(mc).peltsEarned() == 0, "Zero count");
                    mc.player.getInventory().setItem(slot, ItemStack.EMPTY);
                }
                mc.player.getInventory().setItem(0, crest("1.1", 0));
                mc.player.getInventory().setItem(4, crest("1.25", 258));
                mc.player.getInventory().setItem(8, crest("1.5", 504));
                require(readHotbar(mc).kind() == CrestHudState.Kind.COMMON, "Leftmost crest priority");
                var unchanged = readHotbar(mc);
                require(readHotbar(mc) == unchanged, "Unchanged item components reuse parsed state");
                mc.player.getInventory().setItem(0, new ItemStack(Items.DIAMOND_SWORD));
                require(readHotbar(mc).kind() == CrestHudState.Kind.ALMOST_THERE, "Non-crest items ignored");
                require(readHotbar(mc).peltsEarned() == 258, "Uncommon lore");
                mc.player.getInventory().setItem(4, crest("1.25", 500));
                require(readHotbar(mc).kind() == CrestHudState.Kind.SWITCH_CREST, "500 pelts before rarity update");
                require(readHotbar(mc).hotbarSlot() == 4, "Switch action uses active slot");
                mc.player.getInventory().setItem(4, crest("1.25", 258));
                require(readHotbar(mc).kind() == CrestHudState.Kind.ALMOST_THERE, "Updated lore invalidates cache");
                mc.player.getInventory().setItem(4, ItemStack.EMPTY);
                require(readHotbar(mc).kind() == CrestHudState.Kind.SWITCH_CREST, "Later rare becomes active");
                mc.player.getInventory().setItem(8, ItemStack.EMPTY);
                require(readHotbar(mc).kind() == CrestHudState.Kind.MISSING, "Missing fallback");
                for (int mp : new int[]{3, 5, 8}) {
                    var item = new ItemStack(Items.PAPER);
                    item.set(DataComponents.CUSTOM_NAME, Component.literal("Trapper Crest"));
                    item.set(DataComponents.LORE, new ItemLore(List.of(Component.literal("Magical Power: +" + mp))));
                    mc.player.getInventory().setItem(2, item);
                    var expected = mp == 3 ? CrestHudState.Kind.COMMON
                        : mp == 5 ? CrestHudState.Kind.ALMOST_THERE : CrestHudState.Kind.SWITCH_CREST;
                    require(readHotbar(mc).kind() == expected, "Per-item MP " + mp);
                }
                mc.player.getInventory().setItem(2, ItemStack.EMPTY);
            });
            for (int scale : new int[]{1, 2, 3}) {
                context.getInput().resizeWindow(scale == 1 ? 854 : 1280, scale == 1 ? 480 : 720);
                context.runOnClient(mc -> mc.options.guiScale().set(scale));
                for (String stage : new String[]{"common", "uncommon", "rare", "missing"}) {
                    command(context, "trevorcrest demo " + stage);
                    context.runOnClient(mc -> mc.gui.hud.getChat().clearMessages(false));
                    context.waitTicks(10);
                    context.takeScreenshot("crest-" + stage + "-scale-" + scale);
                }
            }
            command(context, "trevorcrest toggle");
            context.runOnClient(mc -> require(!config().enabled, "Crest hide"));
            command(context, "trevorcrest toggle");
            command(context, "trevorcrest scale 1.2");
            command(context, "trevorcrest top 70");
            command(context, "trevorcrest right 20");
            context.runOnClient(mc -> {
                var saved = CrestHudConfig.load();
                require(saved.enabled && saved.top == 70 && saved.rightMargin == 20
                    && Math.abs(saved.scale - 1.2) < .001, "Saved crest config");
            });
            command(context, "trevorcrest reset");
            command(context, "trevorroll sound");
            command(context, "trevorroll sound");
            command(context, "trevorroll duration 8");
            command(context, "trevorroll toggle");
            command(context, "trevorroll toggle");
            for (String rarity : new String[]{"trackable", "untrackable", "undetected", "endangered", "elusive"}) {
                command(context, "trevorroll test " + rarity);
                context.runOnClient(mc -> {
                    require(TrevorRollClient.isRevealActive(), "Original reel active");
                    mc.gui.hud.getChat().clearMessages(false);
                });
                context.waitTicks(5);
                context.takeScreenshot("original-roll-" + rarity);
                command(context, "trevorroll skip");
                context.runOnClient(mc -> require(!TrevorRollClient.isRevealActive(), "Original skip clears reel"));
            }
            command(context, "trevorroll duration 1");
            command(context, "trevorroll test elusive");
            context.waitTicks(80);
            context.takeScreenshot("original-roll-elusive-complete");
            System.out.println("PASS: real production client startup, hotbar lore/removal, all commands, HUD scales and original reel");
        }
    }

    private static void command(ClientGameTestContext context, String command) throws Exception {
        context.runOnClient(mc -> {
            var dispatcher = ClientCommands.getActiveDispatcher();
            require(dispatcher != null, "Client commands registered");
            require(dispatcher.execute(command, (FabricClientCommandSource) mc.player.connection.getSuggestionsProvider()) == 1,
                "Command failed: " + command);
        });
    }

    private static ItemStack crest(String perk, int count) {
        var item = new ItemStack(Items.PAPER);
        item.set(DataComponents.CUSTOM_NAME, Component.literal("Trapper Crest"));
        item.set(DataComponents.LORE, new ItemLore(List.of(
            Component.literal("Earn x" + perk + " Pelts from tracker quests."),
            Component.literal("§5Collected: §a" + count + " pelts"),
            Component.literal("EPIC ACCESSORY"))));
        return item;
    }

    private static CrestHudState readHotbar(Minecraft mc) throws ReflectiveOperationException {
        var method = TrapperCrestHudClient.class.getDeclaredMethod("readHotbar", Minecraft.class);
        method.setAccessible(true);
        return (CrestHudState) method.invoke(null, mc);
    }

    private static CrestHudConfig config() throws ReflectiveOperationException {
        var field = TrapperCrestHudClient.class.getDeclaredField("config");
        field.setAccessible(true);
        return (CrestHudConfig) field.get(null);
    }

    private static void require(boolean condition, String description) {
        if (!condition) throw new AssertionError(description);
    }
}
