package dev.trevorroll;

import dev.crestwatch.trapper.TrapperCrestHudClient;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.*;
import java.util.function.*;

/** Opaque, responsive control room; native widgets retain input, narration and focus. */
public final class TrevorSettingsScreen extends Screen {
    private final Screen parent;
    private final List<net.minecraft.client.gui.components.AbstractWidget> controls = new ArrayList<>();
    private int page=-1,scroll;
    private boolean draggingScrollbar;
    private final Map<net.minecraft.client.gui.components.AbstractWidget,Integer> baseY=new IdentityHashMap<>();
    private final Set<net.minecraft.client.gui.components.AbstractWidget> fixed=Collections.newSetFromMap(new IdentityHashMap<>());
    private static final String[] TITLES={"Roll","Trapper Crest","Appearance","HUD Layout","Jackpots","Audio Mixer","Pelt Tracker"};
    private static final String[] DESCRIPTIONS={"Hunt reveals, pacing and announcements.","Live crest progress and hotbar reminders.","Sizes, opacity and soft lighting.","Drag, resize and align your HUD elements.","Golden Elusive celebrations and effects.","Mix two sounds, audition and set volumes.","Compact pelts/hour and crest-complete alerts."};
    public void selectModule(int module){page=module;scroll=0;rebuildWidgets();}
    private static final int GAP = 12, CONTROL_HEIGHT = 52;
    private int left, contentWidth, column, top;
    private int frameLeft, frameTop, frameWidth, frameHeight;
    private final Map<AbstractWidget,Integer> modules = new IdentityHashMap<>();
    private final Map<AbstractWidget,Integer> navigation = new IdentityHashMap<>();
    private boolean sidebar;
    public TrevorSettingsScreen() { this(null); }
    public TrevorSettingsScreen(Screen parent) { super(Component.literal("Trevor Mod settings")); this.parent = parent; }

    @Override protected void init() {
        controls.clear();baseY.clear();fixed.clear();modules.clear();navigation.clear();draggingScrollbar=false;
        frameWidth = Math.min(760, width - 24); frameHeight = Math.min(600, height - 24);
        frameLeft = (width - frameWidth) / 2; frameTop = (height - frameHeight) / 2;
        sidebar = frameWidth >= 570 && frameHeight >= 332;
        contentWidth = frameWidth - 40 - (sidebar ? 128 : 0); left = frameLeft + 20 + (sidebar ? 128 : 0);
        column = (contentWidth - GAP) / 2; top = frameTop + 80;
        if(page<0){initDashboard();return;}
        var home=button("Modules",frameLeft+20,frameTop+24,78,28,()->selectModule(-1));fixed.add(home);
        var r = TrevorRollClient.settings(); var c = TrapperCrestHudClient.settings();
        switch (page) {
            case 0 -> {
                toggle("Roll", left, 0, () -> r.enabled, v -> r.enabled = v, "Show the hunt reveal when Trevor assigns an animal.");
                toggle("Sound", left + column + GAP, 0, () -> r.sound, v -> r.sound = v, "Play rolling clicks and the result sound.");
                slider("Roll duration", 1, 1, 8, r.durationMillis / 1000., v -> r.durationMillis = (int) Math.round(v * 1000), "%.1f seconds", "Lower is faster. Applies to the next roll.");
                slider("Result stays visible", 2, 1, 10, r.resultMillis / 1000., v -> r.resultMillis = (int) Math.round(v * 1000), "%.1f seconds", "How long the winning animal stays on screen.");
                slider("Sound volume", 3, 0, 1, r.volume, v -> r.volume = v, "%.0f%%", "Relative to Minecraft's UI sound volume.");
                toggle("Chat result", left, 4, () -> r.chatAnnouncements, v -> r.chatAnnouncements = v, "Repeat the animal and destination in chat after the reveal.");
                button("Preview roll", left + column + GAP, row(4), column, () -> {
                    save(); minecraft.gui.setScreen(new TrevorLayoutScreen(this));
                });
            }
            case 1 -> {
                toggle("Crest HUD", left, 0, () -> c.enabled, v -> c.enabled = v, "Show the active hotbar crest and pelt progress.");
                toggle("Hypixel only", left + column + GAP, 0, () -> c.onlyHypixel, v -> c.onlyHypixel = v, "Restrict the live crest HUD to Hypixel.");
                toggle("Missing reminder", left, 1, () -> c.showMissing, v -> c.showMissing = v, "Remind you when no Trapper Crest is in the hotbar.");
                toggle("Yield to roll", left + column + GAP, 1, () -> c.hideDuringRoll, v -> c.hideDuringRoll = v, "Hide the crest while a hunt reveal is active.");
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
                toggle("Crest glow", left + column + GAP, 4, () -> c.glow, v -> c.glow = v, "Show soft glow effects on the crest card.");
            }
            case 3 -> {
                toggle("Grid", left, 0, () -> r.editorGrid, v -> r.editorGrid = v, "Show a faint placement grid in the layout editor.");
                toggle("Snapping", left + column + GAP, 0, () -> r.editorSnap, v -> r.editorSnap = v, "Snap near screen edges, centers and the other panel.");
                slider("Grid spacing", 1, 4, 32, r.gridSize, v -> r.gridSize = (int) Math.round(v), "%.0f px", "Spacing used for grid placement. Shift-drag bypasses snapping.");
                toggle("Lock roll", left, 2, () -> r.rollLocked, v -> r.rollLocked = v, "Prevent accidental movement and resizing in the editor.");
                toggle("Lock crest", left + column + GAP, 2, () -> r.crestLocked, v -> r.crestLocked = v, "Prevent accidental movement and resizing in the editor.");
                button("Open layout editor", left, row(3), contentWidth, () -> { save(); minecraft.gui.setScreen(new TrevorLayoutScreen(this)); });
                button("Reset sizes and positions", left, row(4), contentWidth, () -> {
                    r.scale = 1; r.x = .5; r.y = 0; r.customPosition = false;
                    c.scale = 1; c.top = 85; c.rightMargin = 12; save(); rebuildWidgets();
                });
            }
            case 4 -> {
                toggle("Elusive reward", left, 0, () -> r.elusiveReward, v -> r.elusiveReward = v, "Celebrate only ELUSIVE hunts after their roll finishes.");
                toggle("Reward sound", left + column + GAP, 0, () -> r.elusiveSound, v -> r.elusiveSound = v, "Play your two chosen Audio Mixer layers together.");
                button("Style: " + r.elusiveStyle.label, left, row(1), contentWidth, () -> {
                    var styles = ElusiveReward.Style.values();
                    r.elusiveStyle = styles[(r.elusiveStyle.ordinal() + 1) % styles.length]; rebuildWidgets();
                });
                slider("Reward duration", 2, 1.5, 10, r.elusiveMillis / 1000., v -> r.elusiveMillis = (int) Math.round(v * 1000), "%.1f seconds", "Independent of roll duration and result display time.");
                slider("Effect intensity", 3, .25, 1, r.elusiveIntensity, v -> r.elusiveIntensity = v, "%.0f%%", "Adjust sparkle density and the cinematic background tint.");
                sliderHalfVolumeAndPreview();
            }
            case 5 -> {
                soundButton("Layer 1",0,r.elusiveSoundStyle,v->r.elusiveSoundStyle=v);
                soundButton("Layer 2",1,r.elusiveSecondSound,v->r.elusiveSecondSound=v);
                slider("Layer 1 volume",2,0,1,r.elusiveVolume,v->r.elusiveVolume=v,"%.0f%%","Volume of the first sound.");
                slider("Layer 2 volume",3,0,1,r.elusiveSecondVolume,v->r.elusiveSecondVolume=v,"%.0f%%","Volume of the second sound, played at the same time.");
                button("Preview both sounds",left,row(4),contentWidth,TrevorRollClient::previewRewardSound);
            }
            case 6 -> {
                toggle("Show stats",left,0,()->r.trackerVisible,v->r.trackerVisible=v,"Show pelts/hour in the crest card.");
                toggle("Crest alert",left+column+GAP,0,()->r.crestCompleteSound,v->r.crestCompleteSound=v,"Play once when a crest reaches 500 pelts.");
                button("Start / reset pelt session",left,row(1),column,()->PeltTracker.INSTANCE.start());
                button("Stop tracker",left+column+GAP,row(1),column,()->PeltTracker.INSTANCE.stop());
                soundButton("Maxed sound 1",2,r.crestSoundStyle,v->r.crestSoundStyle=v);
                soundButton("Maxed sound 2",3,r.crestSecondSound,v->r.crestSecondSound=v);
                button("Preview maxed-crest sounds",left,row(4),contentWidth,()->TrevorRollClient.previewSound(r.crestSoundStyle,r.crestSecondSound,(float)r.crestSoundVolume,(float)r.crestSecondVolume));
            }
        }
        layoutControls();
        initFooter();
        positionRows();
    }
    private void soundButton(String name,int row,RewardSound current,java.util.function.Consumer<RewardSound> set){
        var b=button(name+": "+current.label,left,row(row),contentWidth,()->
            minecraft.gui.setScreen(new SoundPickerScreen(this,current,set)));
        MenuTheme.help(b,"Choose a sound for this layer, or turn it off.");
    }
    private void sliderHalfVolumeAndPreview() {
        var r = TrevorRollClient.settings();
        var b = new AbstractSliderButton(left, row(4), column, 20, Component.empty(), r.elusiveVolume) {
            { updateMessage(); }
            @Override protected void updateMessage() { MenuTheme.slider(this,value); setMessage(Component.literal("Reward volume: " + Math.round(value * 100) + "%")); }
            @Override protected void applyValue() { r.elusiveVolume = value; }
        };
        controls.add(b); addRenderableWidget(b);
        button("Preview reward", left + column + GAP, row(4), column, () -> {
            save();
            if (TrevorRollClient.previewElusive() == 1) minecraft.gui.setScreen(null);
        });
    }
    private int row(int index) { return top + index * (CONTROL_HEIGHT + GAP); }
    private Button button(String label,int x,int y,int w,int h,Runnable action){
        var b=Button.builder(Component.literal(label),ignored->action.run()).bounds(x,y,w,h).build();controls.add(b);addRenderableWidget(b);return b;
    }
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
    /** Stack paired controls at narrow GUI sizes rather than shrinking their text. */
    private void layoutControls() {
        var rows = new TreeMap<Integer,List<AbstractWidget>>();
        for (var b : controls) if (!fixed.contains(b)) rows.computeIfAbsent(b.getY(), ignored -> new ArrayList<>()).add(b);
        int y = top;
        for (var row : rows.values()) {
            row.sort(Comparator.comparingInt(AbstractWidget::getX));
            for (int i = 0; i < row.size(); i++) {
                var b = row.get(i);
                boolean stacked = contentWidth < 390 && row.size() > 1;
                b.setX(stacked || row.size() == 1 ? left : left + i * (column + GAP));
                b.setWidth(stacked || row.size() == 1 ? contentWidth : column);
                b.setHeight(CONTROL_HEIGHT);
                b.setY(y); baseY.put(b, y);
                if (stacked && i < row.size() - 1) y += CONTROL_HEIGHT + GAP;
            }
            y += CONTROL_HEIGHT + GAP;
        }
    }
    private void initFooter() {
        initNavigation();
        int y = frameTop + frameHeight - 42;
        fixed.add(button("Layout studio", left, y, column, 28, () -> {
            save(); minecraft.gui.setScreen(new TrevorLayoutScreen(this));
        }));
        var done = button("Done", left + column + GAP, y, contentWidth - column - GAP, 28, this::onClose);
        fixed.add(done);
    }
    private void initNavigation() {
        if (!sidebar) return;
        int step = Math.min(34, (viewportBottom() - top - 22) / 7);
        for (int i = 0; i < TITLES.length; i++) {
            final int module = i;
            var b = button(TITLES[i], frameLeft + 14, top + 18 + i * step, 116, step - 3, () -> selectModule(module));
            navigation.put(b, i); fixed.add(b);
        }
    }
    private void initDashboard() {
        int columns = contentWidth >= 560 ? 3 : contentWidth >= 330 ? 2 : 1;
        int cardW = (contentWidth - (columns - 1) * GAP) / columns;
        int cardH = Math.min(columns == 1 ? 76 : 94, viewportBottom() - top);
        for (int i = 0; i < TITLES.length; i++) {
            final int module = i;
            int count = Math.min(columns, TITLES.length - (i / columns) * columns);
            int start = left + (contentWidth - count * cardW - (count - 1) * GAP) / 2;
            var b = button(TITLES[i], start + (i % columns) * (cardW + GAP),
                top + (i / columns) * (cardH + GAP), cardW, cardH, () -> selectModule(module));
            baseY.put(b, b.getY()); modules.put(b, i);
        }
        initFooter(); positionRows();
    }
    int viewportTop() { return top; }
    int viewportBottom() { return frameTop + frameHeight - 56; }
    private int maximumScroll() {
        int bottom = top;
        for (var e : baseY.entrySet()) bottom = Math.max(bottom, e.getValue() + e.getKey().getHeight());
        return Math.max(0, bottom - viewportBottom());
    }
    private void positionRows() {
        scroll = Math.clamp(scroll, 0, maximumScroll());
        for (var e : baseY.entrySet()) {
            var b = e.getKey(); b.setY(e.getValue() - scroll);
            b.visible = b.getBottom() > top && b.getY() < viewportBottom();
        }
    }
    private void reveal(AbstractWidget b) {
        if (!baseY.containsKey(b)) return;
        if (b.getY() < top) scroll -= top - b.getY();
        else if (b.getBottom() > viewportBottom()) scroll += b.getBottom() - viewportBottom();
        positionRows();
    }
    @Override public boolean mouseScrolled(double x, double y, double horizontal, double vertical) {
        if (x >= frameLeft && x <= frameLeft + frameWidth && y >= top && y < viewportBottom() && vertical != 0) {
            scroll += (int) Math.round(-vertical * (CONTROL_HEIGHT + GAP)); positionRows(); return true;
        }
        return super.mouseScrolled(x, y, horizontal, vertical);
    }
    @Override public boolean mouseClicked(MouseButtonEvent e, boolean twice) {
        // Scissoring is visual only: never dispatch a masked row to the fixed header/footer.
        if (e.button() == 0 && maximumScroll() > 0 && e.x() >= left + contentWidth + 4
            && e.x() <= frameLeft + frameWidth - 5 && e.y() >= top && e.y() < viewportBottom()) {
            draggingScrollbar = true; scrollToPointer(e.y()); return true;
        }
        boolean outside = e.y() < top || e.y() >= viewportBottom();
        if (outside) for (var b : baseY.keySet()) b.visible = false;
        try { return super.mouseClicked(e, twice); }
        finally { if (outside) positionRows(); }
    }
    private double scrollbarThumb() {
        double track = viewportBottom() - top;
        return Math.max(18, track * track / (track + maximumScroll()));
    }
    private void scrollToPointer(double y) {
        double thumb = scrollbarThumb();
        scroll = (int) Math.round((y - top - thumb / 2) / (viewportBottom() - top - thumb) * maximumScroll());
        positionRows();
    }
    @Override public boolean mouseDragged(MouseButtonEvent e, double dx, double dy) {
        if (draggingScrollbar) { scrollToPointer(e.y()); return true; }
        return super.mouseDragged(e, dx, dy);
    }
    @Override public boolean mouseReleased(MouseButtonEvent e) {
        if (draggingScrollbar && e.button() == 0) { draggingScrollbar = false; return true; }
        return super.mouseReleased(e);
    }
    @Override public boolean keyPressed(KeyEvent e) {
        if (e.key() == 258) { // Include offscreen controls, revealing the newly focused row.
            var choices = controls.stream().filter(b -> b.active).toList();
            if (choices.isEmpty()) return false;
            int at = choices.indexOf(getFocused());
            int next = at < 0 ? ((e.modifiers() & 1) == 0 ? 0 : choices.size() - 1)
                : Math.floorMod(at + ((e.modifiers() & 1) == 0 ? 1 : -1), choices.size());
            var b = choices.get(next); reveal(b); setFocused(b); return true;
        }
        if (e.key() == 266 || e.key() == 267 || e.key() == 268 || e.key() == 269) {
            scroll = switch (e.key()) {
                case 268 -> 0;
                case 269 -> maximumScroll();
                default -> scroll + (e.key() == 266 ? -1 : 1) * Math.max(CONTROL_HEIGHT, viewportBottom() - top - GAP);
            };
            positionRows(); return true;
        }
        return super.keyPressed(e);
    }
    private void description(GuiGraphicsExtractor g,String text,double x,double y,double width,double scale,int color){
        String[] words=text.split(" ");String line="";int n=0;
        for(String word:words){if(MenuFont.width(line+" "+word)*scale>width&&!line.isEmpty()){
            MenuFont.text(g,line,x,y+n*10,scale,color);line=word;if(++n==2)return;
        }else line=line.isEmpty()?word:line+" "+word;}
        if(!line.isEmpty())MenuFont.text(g,line,x,y+n*10,scale,color);
    }
    private static int accent(int module) {
        return module == 4 ? 0xFFE9C784 : module == 6 ? 0xFF83CEBB : 0xFFC0A4EF;
    }
    private void moduleCard(GuiGraphicsExtractor g, MinecraftCanvas c, AbstractWidget b, int module, int mx, int my) {
        int x=b.getX(), y=b.getY(), w=b.getWidth(), h=b.getHeight();
        boolean hover=mx>=x&&mx<b.getRight()&&my>=y&&my<b.getBottom();
        int ink=accent(module);
        c.round(x,y+3,w,h,11,0x44000000);
        c.round(x,y,w,h,11,b.isFocused()?0xFFE3CFFF:hover?ink:0xFF41465B);
        c.round(x+1,y+1,w-2,h-2,10,hover?0xFF303247:0xFF252A3B);
        c.round(x+13,y+13,30,30,9,0xFF38354C);
        ModuleIcons.draw(c,module,x+28,y+28,.72,ink);
        MenuFont.text(g,TITLES[module],x+53,y+20,1.08,0xFFF4F0FC);
        description(g,DESCRIPTIONS[module],x+14,y+(h<86?48:55),w-28,.85,0xFFB9BED0);
        if(h>=90){c.round(x+14,y+h-9,19,2,1,ink);}
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        var c = new MinecraftCanvas(g);
        g.fill(0,0,width,height,0xF5101420);
        c.glow(frameLeft-120,frameTop-140,430,370,0x246F56A6);
        c.round(frameLeft,frameTop+5,frameWidth,frameHeight,16,0x88000000);
        c.round(frameLeft,frameTop,frameWidth,frameHeight,16,0xFF45485F);
        c.round(frameLeft+1,frameTop+1,frameWidth-2,frameHeight-2,15,0xFF191E2B);
        c.round(frameLeft+2,frameTop+2,frameWidth-4,64,14,0xFF222738);
        g.fill(frameLeft+20,top-12,frameLeft+frameWidth-20,top-11,0xFF373D51);
        MenuFont.text(g,"TREVOR  /  CONTROL ROOM",frameLeft+20,frameTop+10,.7,0xFFB29DCE);
        if (page < 0) {
            MenuFont.text(g,"Make it your own.",frameLeft+20,frameTop+27,1.65,0xFFF3EEFF);
            MenuFont.text(g,"Seven modules. One perfect setup.",frameLeft+20,frameTop+48,.88,0xFFB4BDD0);
        } else {
            int titleX=frameLeft+112;
            ModuleIcons.draw(c,page,titleX+8,frameTop+35,.65,accent(page));
            MenuFont.text(g,TITLES[page],titleX+24,frameTop+26,1.45,0xFFF3EEFF);
            MenuFont.text(g,MenuFont.ellipsis(DESCRIPTIONS[page],(frameWidth-150)/.82),
                titleX+24,frameTop+47,.82,0xFFB4BDD0);
        }
        if (sidebar) {
            MenuFont.text(g,"MODULES",frameLeft+23,top+2,.7,0xFF9EABC2);
            g.fill(left-15,top,left-14,viewportBottom(),0xFF303749);
        }
        g.enableScissor(left-1,top,left+contentWidth+1,viewportBottom());
        try {
            for(var b:controls) {
                if(!b.visible||fixed.contains(b))continue;
                if(page<0)moduleCard(g,c,b,modules.get(b),mx,my);
                else MenuTheme.control(g,b,mx,my);
            }
        } finally { g.disableScissor(); }
        // Opaque chrome and clipped body keep rows from ever bleeding under the footer.
        g.fill(frameLeft+20,viewportBottom()+7,frameLeft+frameWidth-20,viewportBottom()+8,0xFF373D51);
        for(var b:fixed) {
            Integer module=navigation.get(b);
            if(module==null){MenuTheme.control(g,b,mx,my);continue;}
            boolean selected=page==module,hover=mx>=b.getX()&&mx<b.getRight()&&my>=b.getY()&&my<b.getBottom();
            if(selected||hover||b.isFocused())c.round(b.getX(),b.getY(),b.getWidth(),b.getHeight(),6,
                b.isFocused()?0xFF57476F:selected?0xFF39344E:0xFF292F40);
            if(selected)c.round(b.getX(),b.getY()+5,2,b.getHeight()-10,1,accent(module));
            ModuleIcons.draw(c,module,b.getX()+15,b.getY()+b.getHeight()/2.,.42,selected?accent(module):0xFF9EA9BE);
            MenuFont.text(g,TITLES[module],b.getX()+28,b.getY()+(b.getHeight()-8)/2.,.9,
                selected?0xFFF0E5FF:0xFFBCC4D5);
        }
        if(maximumScroll()>0) {
            double track=viewportBottom()-top,thumb=scrollbarThumb();
            c.round(left+contentWidth+7,top,3,track,1.5,0xFF32394C);
            c.round(left+contentWidth+7,top+(track-thumb)*scroll/maximumScroll(),3,thumb,1.5,0xFFB99AE6);
        }
    }
}
