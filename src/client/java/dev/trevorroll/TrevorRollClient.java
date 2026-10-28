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
    private ElusiveReward reward;
    private int rewardNote;
    private long audioPreviewStarted;
    private int audioPreviewNote;
    private RewardSound audioPreviewStyle;
    public static void previewRewardSound(){
        instance.audioPreviewStyle=instance.config.elusiveSoundStyle;
        instance.audioPreviewStarted=now();instance.audioPreviewNote=0;
    }

    @Override public void onInitializeClient() {
        instance = this;
        config = RollConfig.load();
        PeltTracker.INSTANCE.lastCrestMillis=config.lastCrestMillis;
        PeltTracker.INSTANCE.totalCrestMillis=config.totalCrestMillis;
        PeltTracker.INSTANCE.completedCrests=config.completedCrests;
        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            if (overlay || !config.enabled || !onHypixel()) return true;
            var hunt = Hunt.parse(message.getString());
            if (hunt.isEmpty()) return true;
            PeltTracker.INSTANCE.startIfIdle();
            reward = null;
            // Preserve an earlier assignment if two arrive before the first is revealed.
            if (reel != null && !reel.announced && !reel.demo) announce(reel);
            reel = new Reel(hunt.get(), now(), config.durationMillis, false, random, config.resultMillis);
            return false;
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> tick());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> { reel = null; reward = null; audioPreviewStarted=0; });
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT,
            Identifier.fromNamespaceAndPath("trevorroll", "reveal"), this::draw);
        HudElementRegistry.attachElementAfter(VanillaHudElements.CHAT,
            Identifier.fromNamespaceAndPath("trevorroll", "elusive_reward"), this::drawReward);
        registerCommands();
    }

    /** Lets the crest card yield space to the original reveal without changing the reel. */
    public static boolean isRevealActive() {
        return instance != null && (instance.reel != null || instance.reward != null);
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
        PeltTracker.INSTANCE.advance(now(),MC.player!=null&&MC.level!=null);
        if (MC.player == null || MC.level == null) { reward = null; reel = null; return; }
        long time = now();
        if(audioPreviewStarted>0 && audioPreviewNote<audioPreviewStyle.notes()
                && time-audioPreviewStarted>=audioPreviewNote*180L){
            RewardAudio.play(audioPreviewStyle,audioPreviewNote,(float)(config.elusiveVolume*config.volume));
            if(++audioPreviewNote>=audioPreviewStyle.notes())audioPreviewStarted=0;
        }
        if (reward != null) {
            if ((!config.elusiveReward && !previewReward) || reward.expired(time)) { reward = null; previewReward = false; }
            else if (rewardNote < config.elusiveSoundStyle.notes()
                    && time - reward.startedAt() >= rewardNote * 180L) {
                if (config.sound && config.elusiveSound && !MC.gui.hud.isHidden())
                    RewardAudio.play(config.elusiveSoundStyle, rewardNote, (float)(reward.volume()*config.volume));
                rewardNote++;
            }
        }
        if (reel == null) return;
        if (reel.finished(time)) {
            if (!reel.announced) {
                announce(reel);
                sound(1.4f, 0.55f);
                if (ElusiveReward.eligible(reel.hunt.rarity(), config.elusiveReward)) startReward(reel.hunt.location());
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
        if (config.sound) MC.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), pitch, (float) (volume * config.volume)));
    }

    private void announce(Reel roll) {
        roll.announced = true;
        if (!config.chatAnnouncements) return;
        Component result = Component.literal(roll.hunt.rarity().name()).withColor(roll.hunt.rarity().color & 0xFFFFFF);
        MC.gui.hud.getChat().addClientSystemMessage(Component.literal(roll.demo ? "[Trevor Roll DEMO] " : "[Trevor Roll] ")
            .withStyle(ChatFormatting.GOLD).append(result).append(Component.literal(" animal! ").withStyle(ChatFormatting.WHITE))
            .append(Component.literal("Go to " + roll.hunt.location() + " to kill it now!").withStyle(ChatFormatting.GREEN)));
    }

    private void startReward(String location) {
        previewReward = false;
        reward = new ElusiveReward(now(), config.elusiveMillis, config.elusiveStyle,
            config.elusiveIntensity, config.elusiveVolume, location);
        rewardNote = 0;
    }

    public static boolean trackerVisible(){return instance!=null&&instance.config.trackerVisible;}
    public static void observeCrest(dev.crestwatch.trapper.CrestHudState state){
        if(PeltTracker.INSTANCE.observe(state)){
            var c=instance.config;
            c.lastCrestMillis=PeltTracker.INSTANCE.lastCrestMillis;c.totalCrestMillis=PeltTracker.INSTANCE.totalCrestMillis;c.completedCrests=PeltTracker.INSTANCE.completedCrests;c.save();
            if(c.crestCompleteSound&&c.sound)RewardAudio.play(c.crestSoundStyle,0,(float)(c.crestSoundVolume*c.volume));
        }
    }
    public static boolean isElusiveRewardActive() { return instance != null && instance.reward != null; }

    public static int previewElusive() {
        if (MC.player == null || MC.level == null) return 0;
        // Explicit preview also works when automatic rewards are disabled.
        instance.startReward("Overgrown Mushroom Cave");
        instance.previewReward = true;
        return 1;
    }
    private boolean previewReward;

    private void finishPending() {
        reward = null;
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
                .then(literal("preview").executes(ctx -> previewElusive()))
                .then(literal("trackerStart").executes(ctx -> {PeltTracker.INSTANCE.start();feedback("Pelt tracker started");return 1;}))
                .then(literal("trackerStop").executes(ctx -> {PeltTracker.INSTANCE.stop();feedback("Pelt tracker stopped");return 1;}))
                .then(literal("trackerStats").executes(ctx -> {feedback(PeltTracker.INSTANCE.summary()+" | "+PeltTracker.INSTANCE.history());return 1;}))
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
        reward = null;
        reel = new Reel(new Hunt(rarity, "Overgrown Mushroom Cave"), now(), config.durationMillis, true, random, config.resultMillis);
        return 1;
    }

    private void feedback(String text) {
        MC.gui.hud.getChat().addClientSystemMessage(Component.literal(text).withStyle(ChatFormatting.GOLD));
    }

    private void draw(GuiGraphicsExtractor g, DeltaTracker delta) {
        Reel roll = reel;
        if (MC.gui.screen() instanceof TrevorLayoutScreen) return;
        if (MC.player == null || MC.gui.hud.isHidden()) return;
        if (roll != null) HudPainter.draw(new MinecraftCanvas(g, config.opacity, config.glow), roll, now(),
            MC.getWindow().getGuiScaledWidth(), MC.getWindow().getGuiScaledHeight(),
            config.scale, config.x, config.y, config.customPosition);
    }

    private void drawReward(GuiGraphicsExtractor g, DeltaTracker delta) {
        if (reward != null && MC.player != null && !MC.gui.hud.isHidden() && MC.gui.screen() == null)
            ElusiveRewardPainter.draw(new MinecraftCanvas(g), reward, now(),
                MC.getWindow().getGuiScaledWidth(), MC.getWindow().getGuiScaledHeight());
    }
}
