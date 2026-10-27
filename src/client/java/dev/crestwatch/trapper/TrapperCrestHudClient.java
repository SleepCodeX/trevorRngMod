package dev.crestwatch.trapper;

import dev.trevorroll.TrevorRollClient;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.*;

/**
 * Client-side only: scans the nine hotbar slots and renders an independently configurable
 * floating crest widget. Preserves Trevor Roll's existing hunt reveal and commands.
 */
public final class TrapperCrestHudClient implements ClientModInitializer {
    private static final String MOD_ID = "trevorroll";
    private static final Minecraft MC = Minecraft.getInstance();
    private static final CrestHudAnimator MOTION = new CrestHudAnimator();
    private static CrestHudConfig config;
    private static CrestHudState previewState;
    private static long previewExpires;
    private static final ItemStack[] CACHED_STACKS = new ItemStack[9];
    private static final CrestObservation[] CACHED_CRESTS = new CrestObservation[9];
    private static final List<CrestObservation> HOTBAR_CRESTS = Arrays.asList(CACHED_CRESTS);
    private static CrestHudState hotbarState = CrestHudState.fromHotbar(List.of());

    @Override public void onInitializeClient() {
        config = CrestHudConfig.load();
        ClientTickEvents.END_CLIENT_TICK.register(TrapperCrestHudClient::tick);
        HudElementRegistry.attachElementBefore(
            VanillaHudElements.CHAT,
            Identifier.fromNamespaceAndPath(MOD_ID, "crest_status"),
            TrapperCrestHudClient::render
        );
        registerCommands();
    }

    public static CrestHudConfig settings() { return config; }

    private static void tick(Minecraft mc) {
        if (mc.player == null || mc.level == null) {
            Arrays.fill(CACHED_STACKS, null);
            Arrays.fill(CACHED_CRESTS, null);
            hotbarState = CrestHudState.fromHotbar(List.of());
        } else if (config.enabled && (!config.onlyHypixel || isHypixel(mc))) {
            readHotbar(mc);
        }
    }

    private static void render(GuiGraphicsExtractor graphics, DeltaTracker tick) {
        if (MC.player == null || MC.level == null || MC.gui.hud.isHidden() || !config.enabled) return;
        if ((config.hideDuringRoll && TrevorRollClient.isRevealActive()) || MC.gui.screen() instanceof dev.trevorroll.TrevorLayoutScreen) return;
        long now = System.nanoTime() / 1_000_000L;
        boolean preview = previewState != null && now < previewExpires;
        if (!preview && config.onlyHypixel && !isHypixel(MC)) return;
        CrestHudState state = preview ? previewState : hotbarState;
        if (!config.showMissing && state.kind() == CrestHudState.Kind.MISSING) return;
        CrestHudFrame frame = config.animations ? MOTION.sample(state, now)
            : new CrestHudFrame(state.ratio(), 0, 0, 1, 0, 0);
        CrestHudPainter.draw(new CrestHudCanvas(graphics, config.opacity, config.glow), state, frame,
            graphics.guiWidth(), graphics.guiHeight(), config.top, config.rightMargin, config.scale);
        if (!preview) previewState = null;
    }

    private static CrestHudState readHotbar(Minecraft mc) {
        boolean changed = false;
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = mc.player.getInventory().getItem(slot);
            if (CACHED_STACKS[slot] != null && ItemStack.isSameItemSameComponents(stack, CACHED_STACKS[slot])) continue;
            changed = true;
            CACHED_STACKS[slot] = stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
            CACHED_CRESTS[slot] = null;
            if (stack.isEmpty()) continue;
            String name = stack.getHoverName().getString();
            if (!name.toLowerCase(Locale.ROOT).contains("trapper crest")) continue;
            ItemLore lore = stack.get(DataComponents.LORE);
            List<String> lines = new ArrayList<>();
            if (lore != null) for (Component line : lore.lines()) lines.add(line.getString());
            CACHED_CRESTS[slot] = CrestLoreParser.parse(name, lines, slot).orElse(null);
        }
        if (changed) hotbarState = CrestHudState.fromHotbar(HOTBAR_CRESTS);
        return hotbarState;
    }

    private static boolean isHypixel(Minecraft mc) {
        var server = mc.getCurrentServer();
        if (server == null || server.ip == null || mc.hasSingleplayerServer()) return false;
        String host = server.ip.toLowerCase(Locale.ROOT).split(":", 2)[0];
        return host.equals("hypixel.net") || host.endsWith(".hypixel.net");
    }

    private static void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registry) -> dispatcher.register(
            literal("trevorcrest")
                .executes(ctx -> {
                    feedback("Crest HUD: /trevorcrest demo <common|uncommon|rare|missing>, toggle, scale <0.65-1.6>, top <pixels>, right <pixels>, reset");
                    return 1;
                })
                .then(literal("config").executes(ctx -> { TrevorRollClient.openSettings(); return 1; }))
                .then(literal("toggle").executes(ctx -> {
                    config.enabled = !config.enabled; config.save();
                    feedback("Crest HUD " + (config.enabled ? "enabled" : "disabled"));
                    return 1;
                }))
                .then(literal("demo")
                    .then(literal("common").executes(ctx -> preview(CrestHudState.Kind.COMMON, 42, 100)))
                    .then(literal("uncommon").executes(ctx -> preview(CrestHudState.Kind.ALMOST_THERE, 258, 500)))
                    .then(literal("rare").executes(ctx -> preview(CrestHudState.Kind.SWITCH_CREST, 504, 500)))
                    .then(literal("missing").executes(ctx -> preview(CrestHudState.Kind.MISSING, -1, 100))))
                .then(literal("scale").then(argument("size", FloatArgumentType.floatArg(.65f, 1.6f)).executes(ctx -> {
                    config.scale = FloatArgumentType.getFloat(ctx, "size"); config.save();
                    feedback("Crest HUD scale set to " + String.format(Locale.ROOT, "%.2f", config.scale)); return 1;
                })))
                .then(literal("top").then(argument("pixels", IntegerArgumentType.integer(0, 2000)).executes(ctx -> {
                    config.top = IntegerArgumentType.getInteger(ctx, "pixels"); config.save();
                    feedback("Crest HUD top set to " + config.top); return 1;
                })))
                .then(literal("right").then(argument("pixels", IntegerArgumentType.integer(0, 1000)).executes(ctx -> {
                    config.rightMargin = IntegerArgumentType.getInteger(ctx, "pixels"); config.save();
                    feedback("Crest HUD right margin set to " + config.rightMargin); return 1;
                })))
                .then(literal("reset").executes(ctx -> {
                    config.scale = 1.0; config.top = 85; config.rightMargin = 12; config.enabled = true;
                    config.save(); feedback("Crest HUD appearance reset to defaults"); return 1;
                }))
        ));
    }

    private static int preview(CrestHudState.Kind kind, int count, int goal) {
        previewState = new CrestHudState(kind, count, goal, kind == CrestHudState.Kind.MISSING ? -1 : 4);
        previewExpires = System.nanoTime() / 1_000_000L + 14_000;
        config.enabled = true;
        feedback("Crest HUD preview: " + previewState.badge() + " (14 seconds)");
        return 1;
    }

    private static void feedback(String message) {
        if (MC.player != null) MC.gui.hud.getChat().addClientSystemMessage(Component.literal("[Trevor Roll] " + message));
    }
}
