package dev.trevorroll;

import dev.crestwatch.trapper.TrapperCrestHudClient;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.Locale;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;

/** Keyboard-accessible settings; changes persist when leaving the screen. */
public final class TrevorSettingsScreen extends Screen {
    private final Screen parent;
    public TrevorSettingsScreen() { this(null); }
    public TrevorSettingsScreen(Screen parent) {
        super(Component.literal("Trevor Roll settings")); this.parent = parent;
    }

    @Override protected void init() {
        var roll = TrevorRollClient.settings();
        var crest = TrapperCrestHudClient.settings();
        int x = width / 2 - 150, y = Math.max(28, (height - 212) / 2);
        addRenderableWidget(Button.builder(label("Roll", roll.enabled), b -> {
            roll.enabled = !roll.enabled; b.setMessage(label("Roll", roll.enabled));
        }).bounds(x, y, 145, 20).build());
        addRenderableWidget(Button.builder(label("Sound", roll.sound), b -> {
            roll.sound = !roll.sound; b.setMessage(label("Sound", roll.sound));
        }).bounds(x + 155, y, 145, 20).build());
        slider(x, y + 26, 1, 8, roll.durationMillis / 1000.0,
            v -> roll.durationMillis = (int) Math.round(v * 1000),
            v -> String.format(Locale.ROOT, "Roll duration: %.1fs (lower = faster)", v));
        slider(x, y + 52, .5, 2, roll.scale, v -> roll.scale = v,
            v -> String.format(Locale.ROOT, "Roll size: %.0f%%", v * 100));
        slider(x, y + 78, .65, 1.6, crest.scale, v -> crest.scale = v,
            v -> String.format(Locale.ROOT, "Crest size: %.0f%%", v * 100));
        addRenderableWidget(Button.builder(label("Crest HUD", crest.enabled), b -> {
            crest.enabled = !crest.enabled; b.setMessage(label("Crest HUD", crest.enabled));
        }).bounds(x, y + 104, 145, 20).build());
        addRenderableWidget(Button.builder(label("Hypixel only", crest.onlyHypixel), b -> {
            crest.onlyHypixel = !crest.onlyHypixel; b.setMessage(label("Hypixel only", crest.onlyHypixel));
        }).bounds(x + 155, y + 104, 145, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Edit HUD layout / live preview"), b -> {
            save(); minecraft.gui.setScreen(new TrevorLayoutScreen(this));
        }).bounds(x, y + 130, 300, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Reset sizes and positions"), b -> {
            roll.scale = 1; roll.x = .5; roll.y = 0; roll.customPosition = false;
            crest.scale = 1; crest.top = 85; crest.rightMargin = 12;
            save(); rebuildWidgets();
        }).bounds(x, y + 156, 300, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
            .bounds(x, y + 182, 300, 20).build());
    }
    private static Component label(String name, boolean value) {
        return Component.literal(name + ": " + (value ? "ON" : "OFF"));
    }
    private void slider(int x, int y, double min, double max, double initial,
                        DoubleConsumer setter, DoubleFunction<String> label) {
        addRenderableWidget(new AbstractSliderButton(x, y, 300, 20, Component.empty(), (initial - min) / (max - min)) {
            { updateMessage(); }
            @Override protected void updateMessage() { setMessage(Component.literal(label.apply(min + value * (max - min)))); }
            @Override protected void applyValue() { setter.accept(min + value * (max - min)); }
        });
    }
    static void save() {
        TrevorRollClient.settings().save();
        TrapperCrestHudClient.settings().save();
    }
    @Override public void removed() { save(); }
    @Override public void onClose() { save(); minecraft.gui.setScreen(parent); }
    @Override public boolean isPauseScreen() { return false; }
    @Override public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractRenderState(g, mouseX, mouseY, delta);
        g.text(font, title, width / 2 - font.width(title) / 2, 10, 0xFFF6F2FF, true);
    }
}
