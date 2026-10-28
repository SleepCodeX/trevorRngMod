package dev.trevorroll;

import dev.crestwatch.trapper.TrapperCrestHudClient;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.*;
import java.util.function.*;

/** Compact category pages keep every setting reachable at the minimum GUI size. */
public final class TrevorSettingsScreen extends Screen {
    private final Screen parent;
    private final List<net.minecraft.client.gui.components.AbstractWidget> controls = new ArrayList<>();
    private int page;
    private int left, contentWidth, column, top;
    private static final String[] TABS = {"Roll", "Crest", "Looks", "Editor", "Elusive", "Audio"};
    public TrevorSettingsScreen() { this(null); }
    public TrevorSettingsScreen(Screen parent) { super(Component.literal("Trevor Mod settings")); this.parent = parent; }

    @Override protected void init() {
        controls.clear();
        contentWidth = Math.min(440, width - 28); left = (width - contentWidth) / 2;
        column = (contentWidth - 8) / 2; top = 70;
        for (int i = 0; i < TABS.length; i++) {
            final int tab = i;
            var b = button(TABS[i], left + i * (contentWidth / TABS.length), 38,
                contentWidth / TABS.length - 3, () -> { page = tab; rebuildWidgets(); });
            if (i == page) b.active = false;
        }
        var r = TrevorRollClient.settings(); var c = TrapperCrestHudClient.settings();
        switch (page) {
            case 0 -> {
                toggle("Roll", left, 0, () -> r.enabled, v -> r.enabled = v, "Show the hunt reveal when Trevor assigns an animal.");
                toggle("Sound", left + column + 8, 0, () -> r.sound, v -> r.sound = v, "Play rolling clicks and the result sound.");
                slider("Roll duration", 1, 1, 8, r.durationMillis / 1000., v -> r.durationMillis = (int) Math.round(v * 1000), "%.1f seconds", "Lower is faster. Applies to the next roll.");
                slider("Result stays visible", 2, 1, 10, r.resultMillis / 1000., v -> r.resultMillis = (int) Math.round(v * 1000), "%.1f seconds", "How long the winning animal stays on screen.");
                slider("Sound volume", 3, 0, 1, r.volume, v -> r.volume = v, "%.0f%%", "Relative to Minecraft's UI sound volume.");
                toggle("Chat result", left, 4, () -> r.chatAnnouncements, v -> r.chatAnnouncements = v, "Repeat the animal and destination in chat after the reveal.");
                button("Preview roll", left + column + 8, row(4), column, () -> {
                    save(); minecraft.gui.setScreen(new TrevorLayoutScreen(this));
                });
            }
            case 1 -> {
                toggle("Crest HUD", left, 0, () -> c.enabled, v -> c.enabled = v, "Show the active hotbar crest and pelt progress.");
                toggle("Hypixel only", left + column + 8, 0, () -> c.onlyHypixel, v -> c.onlyHypixel = v, "Restrict the live crest HUD to Hypixel.");
                toggle("Missing reminder", left, 1, () -> c.showMissing, v -> c.showMissing = v, "Remind you when no Trapper Crest is in the hotbar.");
                toggle("Yield to roll", left + column + 8, 1, () -> c.hideDuringRoll, v -> c.hideDuringRoll = v, "Hide the crest while a hunt reveal is active.");
                toggle("Animations", left, 2, () -> c.animations, v -> c.animations = v, "Turn off movement and pulsing for a still crest HUD.");
                slider("Crest size", 3, .65, 1.6, c.scale, v -> c.scale = v, "%.0f%%", "Scale text, icons, card and effects together.");
                button("Preview all crest states", left, row(4), contentWidth, () -> { save(); minecraft.gui.setScreen(new TrevorLayoutScreen(this)); });
            }
            case 2 -> {
                slider("Roll size", 0, .5, 2, r.scale, v -> r.scale = v, "%.0f%%", "Oversized panels automatically fit the screen.");
                slider("Crest size", 1, .65, 1.6, c.scale, v -> c.scale = v, "%.0f%%", "Includes every part of the crest card.");
                slider("Roll opacity", 2, .25, 1, r.opacity, v -> r.opacity = v, "%.0f%%", "Keep at least 25% opacity so the panel remains findable.");
                slider("Crest opacity", 3, .25, 1, c.opacity, v -> c.opacity = v, "%.0f%%", "Controls the entire card, including text.");
                toggle("Roll glow", left, 4, () -> r.glow, v -> r.glow = v, "Show soft glow effects on the roll card.");
                toggle("Crest glow", left + column + 8, 4, () -> c.glow, v -> c.glow = v, "Show soft glow effects on the crest card.");
            }
            case 3 -> {
                toggle("Grid", left, 0, () -> r.editorGrid, v -> r.editorGrid = v, "Show a faint placement grid in the layout editor.");
                toggle("Snapping", left + column + 8, 0, () -> r.editorSnap, v -> r.editorSnap = v, "Snap near screen edges, centers and the other panel.");
                slider("Grid spacing", 1, 4, 32, r.gridSize, v -> r.gridSize = (int) Math.round(v), "%.0f px", "Spacing used for grid placement. Shift-drag bypasses snapping.");
                toggle("Lock roll", left, 2, () -> r.rollLocked, v -> r.rollLocked = v, "Prevent accidental movement and resizing in the editor.");
                toggle("Lock crest", left + column + 8, 2, () -> r.crestLocked, v -> r.crestLocked = v, "Prevent accidental movement and resizing in the editor.");
                button("Open layout editor", left, row(3), contentWidth, () -> { save(); minecraft.gui.setScreen(new TrevorLayoutScreen(this)); });
                button("Reset sizes and positions", left, row(4), contentWidth, () -> {
                    r.scale = 1; r.x = .5; r.y = 0; r.customPosition = false;
                    c.scale = 1; c.top = 85; c.rightMargin = 12; save(); rebuildWidgets();
                });
            }
            case 4 -> {
                toggle("Elusive reward", left, 0, () -> r.elusiveReward, v -> r.elusiveReward = v, "Celebrate only ELUSIVE hunts after their roll finishes.");
                toggle("Reward sound", left + column + 8, 0, () -> r.elusiveSound, v -> r.elusiveSound = v, "Play a cute rising chime. The main sound toggle and volume also apply.");
                button("Style: " + r.elusiveStyle.label, left, row(1), contentWidth, () -> {
                    var styles = ElusiveReward.Style.values();
                    r.elusiveStyle = styles[(r.elusiveStyle.ordinal() + 1) % styles.length]; rebuildWidgets();
                });
                slider("Reward duration", 2, 1.5, 10, r.elusiveMillis / 1000., v -> r.elusiveMillis = (int) Math.round(v * 1000), "%.1f seconds", "Independent of roll duration and result display time.");
                slider("Effect intensity", 3, .25, 1, r.elusiveIntensity, v -> r.elusiveIntensity = v, "%.0f%%", "Adjust sparkle density and the cinematic background tint.");
                sliderHalfVolumeAndPreview();
            }
            case 5 -> {
                button("Sound: " + r.elusiveSoundStyle.label,left,row(0),contentWidth,() -> {
                    var modes=RewardSound.values();r.elusiveSoundStyle=modes[(r.elusiveSoundStyle.ordinal()+1)%modes.length];rebuildWidgets();
                });
                button("Play sound preview",left,row(1),contentWidth,TrevorRollClient::previewRewardSound);
                slider("Reward volume",2,0,1,r.elusiveVolume,v -> r.elusiveVolume=v,"%.0f%%","Sound volume for the Elusive celebration.");
                toggle("Reward sound",left,3,()->r.elusiveSound,v->r.elusiveSound=v,"Enable Elusive audio.");
                toggle("Master sound",left+column+8,3,()->r.sound,v->r.sound=v,"Enable all mod sounds.");
            }
        }
        button("Edit layout", left, height - 28, column, () -> { save(); minecraft.gui.setScreen(new TrevorLayoutScreen(this)); });
        button("Done", left + column + 8, height - 28, column, this::onClose);
    }
    private void sliderHalfVolumeAndPreview() {
        var r = TrevorRollClient.settings();
        var b = new AbstractSliderButton(left, row(4), column, 20, Component.empty(), r.elusiveVolume) {
            { updateMessage(); }
            @Override protected void updateMessage() { MenuTheme.slider(this,value); setMessage(Component.literal("Reward volume: " + Math.round(value * 100) + "%")); }
            @Override protected void applyValue() { r.elusiveVolume = value; }
        };
        controls.add(b); addRenderableWidget(b);
        button("Preview reward", left + column + 8, row(4), column, () -> {
            save();
            if (TrevorRollClient.previewElusive() == 1) minecraft.gui.setScreen(null);
        });
    }
    private int row(int index) { return top + index * 24; }
    private Button button(String label, int x, int y, int w, Runnable action) {
        var b = Button.builder(Component.literal(label), ignored -> action.run()).bounds(x, y, w, 20).build();
        controls.add(b); addRenderableWidget(b); return b;
    }
    private void toggle(String label, int x, int row, BooleanSupplier get, Consumer<Boolean> set, String help) {
        var b = Button.builder(Component.literal(label + ": " + (get.getAsBoolean() ? "ON" : "OFF")), button -> {
            set.accept(!get.getAsBoolean()); button.setMessage(Component.literal(label + ": " + (get.getAsBoolean() ? "ON" : "OFF")));
        }).bounds(x, row(row), column, 20).build();
        b.setTooltip(Tooltip.create(Component.literal(help))); MenuTheme.help(b,help); controls.add(b); addRenderableWidget(b);
    }
    private void slider(String name, int row, double min, double max, double initial, DoubleConsumer set, String format, String help) {
        var b = new AbstractSliderButton(left, row(row), contentWidth, 20, Component.empty(), (initial - min) / (max - min)) {
            { updateMessage(); }
            private double actual() { return min + value * (max - min); }
            @Override protected void updateMessage() {
                MenuTheme.slider(this,value);
                double v = actual(); if (format.contains("%%")) v *= 100;
                setMessage(Component.literal(name + ": " + String.format(Locale.ROOT, format, v)));
            }
            @Override protected void applyValue() { set.accept(actual()); }
        };
        b.setTooltip(Tooltip.create(Component.literal(help))); MenuTheme.help(b,help); controls.add(b); addRenderableWidget(b);
    }
    static void save() { TrevorRollClient.settings().save(); TrapperCrestHudClient.settings().save(); }
    @Override public void removed() { save(); }
    @Override public void onClose() { save(); minecraft.gui.setScreen(parent); }
    @Override public boolean isPauseScreen() { return false; }
    @Override public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        var canvas=new MinecraftCanvas(g);
        g.fill(0,0,width,height,0xF70D101B);
        for(int yy=0;yy<height;yy+=2){
            int a=(int)(18*(1-yy/(double)height));g.fill(0,yy,width,yy+2,(a<<24)|0x67528E);
        }
        canvas.glow(left-120,-150,350,310,0x166F50A0);
        canvas.round(left-13,31,contentWidth+26,height-35,13,0x70000000);
        canvas.round(left-12,29,contentWidth+24,height-35,12,0xFF353448);
        canvas.round(left-11,30,contentWidth+22,height-37,11,0xFF171B29);
        canvas.round(left-10,30,contentWidth+20,32,10,0xFF202333);
        canvas.round(left-1,62,contentWidth+2,1,0,0xFF303244);
        canvas.text("TREVOR",left,8,1.1,0xFFECE5FF);
        canvas.text("SETTINGS",left+49,10,.72,0xFF9992AC);
        String desc=switch(page){case 0->"Hunt reveals & timing";case 1->"Crest tracking & reminders";case 2->"Size, opacity & lighting";case 3->"Positioning & alignment";case 4->"Elusive celebrations";default->"Reward sounds & previews";};
        canvas.text(desc,left+contentWidth-canvas.textWidth(desc)*.75,11,.75,0xFFAAA2BA);
        for(var control:controls)MenuTheme.control(g,control,mx,my);
        canvas.round(left,height-48,contentWidth,1,0,0xFF303143);
        String help=MenuTheme.hoveredHelp(controls,mx,my);
        String footer=help.isEmpty()?"SAVED AUTOMATICALLY":help;
        canvas.text(footer,left,height-42,Math.min(.7,(contentWidth-16.)/Math.max(1,canvas.textWidth(footer))),0xFFAAA0BA);
        canvas.ellipse(left+contentWidth-7,height-40,4,4,0xFF8BC5AC);

    }
}
