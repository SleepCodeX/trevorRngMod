package dev.trevorroll;

import com.mojang.brigadier.arguments.FloatArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import java.util.Locale;
import java.util.Random;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.*;

public final class TrevorRollClient implements ClientModInitializer {
    private static final Minecraft MC = Minecraft.getInstance();
    private static TrevorRollClient instance;
    private final Random random = new Random();
    private RollConfig config;
    private Reel reel;

    @Override public void onInitializeClient() {
        instance = this;
        config = RollConfig.load();
        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            if (overlay || !config.enabled || !onHypixel()) return true;
            var hunt = Hunt.parse(message.getString());
            if (hunt.isEmpty()) return true;
            // Preserve an earlier assignment if two arrive before the first is revealed.
            if (reel != null && !reel.announced && !reel.demo) announce(reel);
            reel = new Reel(hunt.get(), now(), config.durationMillis, false, random);
            return false;
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> tick());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> reel = null);
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT,
            Identifier.fromNamespaceAndPath("trevorroll", "reveal"), this::draw);
        registerCommands();
    }

    /** Lets the crest card yield space to the original reveal without changing the reel. */
    public static boolean isRevealActive() {
        return instance != null && instance.reel != null;
    }

    private static long now() { return System.nanoTime() / 1_000_000; }

    private boolean onHypixel() {
        var server = MC.getCurrentServer();
        if (server == null || MC.hasSingleplayerServer()) return false;
        String host = server.ip.toLowerCase(Locale.ROOT).split(":", 2)[0];
        return host.equals("hypixel.net") || host.endsWith(".hypixel.net");
    }

    private void tick() {
        if (openSettingsNextTick) {
            openSettingsNextTick = false;
            MC.gui.setScreen(new TrevorSettingsScreen());
        }
        if (reel == null || MC.player == null) return;
        long time = now();
        if (reel.finished(time)) {
            if (!reel.announced) {
                announce(reel);
                sound(1.4f, 0.55f);
            }
            if (reel.expired(time)) reel = null;
        } else {
            int card = (int) Math.floor(reel.position(time) + 0.5);
            if (card != reel.lastClick) {
                reel.lastClick = card;
                sound(1.85f, 0.18f);
            }
        }
    }

    private void sound(float pitch, float volume) {
        if (config.sound) MC.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), pitch, volume));
    }

    private void announce(Reel roll) {
        roll.announced = true;
        Component result = Component.literal(roll.hunt.rarity().name()).withColor(roll.hunt.rarity().color & 0xFFFFFF);
        MC.gui.hud.getChat().addClientSystemMessage(Component.literal(roll.demo ? "[Trevor Roll DEMO] " : "[Trevor Roll] ")
            .withStyle(ChatFormatting.GOLD).append(result).append(Component.literal(" animal! ").withStyle(ChatFormatting.WHITE))
            .append(Component.literal("Go to " + roll.hunt.location() + " to kill it now!").withStyle(ChatFormatting.GREEN)));
    }

    private void finishPending() {
        if (reel != null && !reel.announced && !reel.demo && MC.player != null) announce(reel);
        reel = null;
    }

    static RollConfig settings() { return instance.config; }

    public static void openSettings() {
        // Commands run while the chat screen is still closing; defer until next tick.
        instance.openSettingsNextTick = true;
    }
    private boolean openSettingsNextTick;

    private void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registry) -> dispatcher.register(
            literal("trevormod")
                .executes(ctx -> { feedback("Trevor Mod: /trevormod settings"); return 1; })
                .then(literal("settings").executes(ctx -> { openSettings(); return 1; }))
        ));
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registry) -> dispatcher.register(
            literal("trevorroll")
                .executes(ctx -> {
                    feedback("All settings: /trevormod settings. Trevor Roll: /trevorroll config, test [rarity], toggle, sound, duration <1-8>, skip");
                    return 1;
                })
                .then(literal("config").executes(ctx -> { openSettings(); return 1; }))
                .then(literal("test").executes(ctx -> demo(Hunt.Rarity.values()[random.nextInt(5)]))
                    .then(literal("trackable").executes(ctx -> demo(Hunt.Rarity.TRACKABLE)))
                    .then(literal("untrackable").executes(ctx -> demo(Hunt.Rarity.UNTRACKABLE)))
                    .then(literal("undetected").executes(ctx -> demo(Hunt.Rarity.UNDETECTED)))
                    .then(literal("endangered").executes(ctx -> demo(Hunt.Rarity.ENDANGERED)))
                    .then(literal("elusive").executes(ctx -> demo(Hunt.Rarity.ELUSIVE))))
                .then(literal("toggle").executes(ctx -> {
                    finishPending(); config.enabled = !config.enabled; config.save();
                    feedback("Trevor Roll " + (config.enabled ? "enabled" : "disabled")); return 1;
                }))
                .then(literal("sound").executes(ctx -> {
                    config.sound = !config.sound; config.save();
                    feedback("Trevor Roll sound " + (config.sound ? "on" : "off")); return 1;
                }))
                .then(literal("duration").then(argument("seconds", FloatArgumentType.floatArg(1, 8)).executes(ctx -> {
                    config.durationMillis = Math.round(FloatArgumentType.getFloat(ctx, "seconds") * 1000);
                    config.save(); feedback("Roll duration: " + config.durationMillis / 1000f + " seconds"); return 1;
                })))
                .then(literal("skip").executes(ctx -> { finishPending(); return 1; }))
        ));
    }

    private int demo(Hunt.Rarity rarity) {
        if (reel != null && !reel.demo) {
            feedback("Finish the current hunt reveal first, or use /trevorroll skip."); return 0;
        }
        reel = new Reel(new Hunt(rarity, "Overgrown Mushroom Cave"), now(), config.durationMillis, true, random);
        return 1;
    }

    private void feedback(String text) {
        MC.gui.hud.getChat().addClientSystemMessage(Component.literal(text).withStyle(ChatFormatting.GOLD));
    }

    private void draw(GuiGraphicsExtractor g, DeltaTracker delta) {
        Reel roll = reel;
        if (MC.gui.screen() instanceof TrevorLayoutScreen) return;
        if (roll == null || MC.player == null || MC.gui.hud.isHidden()) return;
        HudPainter.draw(new MinecraftCanvas(g), roll, now(),
            MC.getWindow().getGuiScaledWidth(), MC.getWindow().getGuiScaledHeight(),
            config.scale, config.x, config.y, config.customPosition);
    }
}
