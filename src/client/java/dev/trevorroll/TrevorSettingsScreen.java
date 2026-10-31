package dev.trevorroll;

import dev.crestwatch.trapper.TrapperCrestHudClient;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
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
    private double visibleScroll;
    private long lastFrame = System.nanoTime(), closingAt;
    private double closingOpacity = 1, frameOpacity = 1;
    private boolean smoothWheel;
    private final Map<AbstractWidget,Double> hoverStates = new IdentityHashMap<>();
    private boolean draggingScrollbar, capturingKey;
    private final long openedAt = System.nanoTime();
    private final Map<net.minecraft.client.gui.components.AbstractWidget,Integer> baseY=new IdentityHashMap<>();
    private final Set<net.minecraft.client.gui.components.AbstractWidget> fixed=Collections.newSetFromMap(new IdentityHashMap<>());
    private static final String[] TITLES={"Roll","Trapper Crest","Appearance","HUD Layout","Jackpots","Audio Mixer","Pelt Tracker","Menu & Keys"};
    private static final String[] DESCRIPTIONS={"Hunt reveals, pacing and announcements.","Live crest progress and hotbar reminders.","Sizes, opacity and soft lighting.","Drag, resize and align your HUD elements.","Golden Elusive celebrations and effects.","Mix two sounds, audition and set volumes.","Compact pelts/hour and crest-complete alerts.","Your shortcut, opening motion and advanced graphics."};
    public void selectModule(int module){capturingKey=false;page=module;scroll=0;visibleScroll=0;smoothWheel=false;rebuildWidgets();}
    private static final int GAP = 16, CONTROL_HEIGHT = 60;
    private int left, contentWidth, column, top;
    private int frameLeft, frameTop, frameWidth, frameHeight;
    private final Map<AbstractWidget,Integer> modules = new IdentityHashMap<>();
    private final Map<AbstractWidget,Integer> navigation = new IdentityHashMap<>();
    private boolean sidebar;
    private int filter;
    private String query = "";
    private EditBox searchBox;
    private final Map<AbstractWidget,Integer> filters = new IdentityHashMap<>();
    private static final String[] FILTERS = {"All", "Hunts", "Visuals", "Audio"};
    public TrevorSettingsScreen() { this(null); }
    public TrevorSettingsScreen(Screen parent) { super(Component.literal("Trevor Mod settings")); this.parent = parent; }

    @Override protected void init() {
        hoverStates.clear();controls.clear();baseY.clear();fixed.clear();modules.clear();navigation.clear();filters.clear();searchBox=null;draggingScrollbar=false;
        frameWidth = Math.min(1180, width - (width >= 600 ? 48 : 20));
        frameHeight = Math.min(780, height - (height >= 300 ? 32 : 20));
        frameLeft = (width - frameWidth) / 2; frameTop = (height - frameHeight) / 2;
        sidebar = frameWidth >= 560 && frameHeight >= 300;
        contentWidth = frameWidth - 40 - (sidebar ? 120 : 0); left = frameLeft + 20 + (sidebar ? 120 : 0);
        column = (contentWidth - GAP) / 2; top = frameTop + (height >= 300 ? 108 : 78);
        if(page<0){initDashboard();return;}
        var home=button("Modules",left,frameTop+14,68,24,()->selectModule(-1));fixed.add(home);
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
            case 7 -> {
                var binding = button(capturingKey ? "Press a key... (Esc cancels)" : "Open menu key: " + MenuKeybind.label(),
                    left, row(0), contentWidth, () -> { capturingKey = true; rebuildWidgets(); });
                MenuTheme.help(binding, capturingKey ? "Escape cancels. Delete or Backspace returns to Unbound."
                    : MenuKeybind.conflicts() ? "This key is shared with another control. Choose a free key if needed."
                    : "Unbound by default. Also available in Minecraft Controls.");
                button("Reset key to unbound", left, row(1), contentWidth, () -> {
                    MenuKeybind.clear(); capturingKey = false; rebuildWidgets();
                });
                toggle("Menu fade", left, 2, () -> r.menuAnimations, v -> r.menuAnimations = v,
                    "Smooth opening, closing and scrolling. Off makes menus instant.");
                toggle("Advanced graphics", left + column + GAP, 2, () -> r.advancedMenuGraphics, v -> r.advancedMenuGraphics = v,
                    "Slow 3D icons and richer effects. Off by default for lightweight rendering.");
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
    @Override public void onClose() {
        if (closingAt != 0) return;
        if (!TrevorRollClient.settings().menuAnimations) { minecraft.gui.setScreen(parent); return; }
        closingOpacity = MenuMotion.openingOpacity(System.nanoTime()-openedAt, true);
        closingAt = System.nanoTime();
        capturingKey = false; draggingScrollbar = false;
    }
    @Override public void tick() {
        super.tick();
        if (closingAt != 0 && (!TrevorRollClient.settings().menuAnimations
            || System.nanoTime()-closingAt >= MenuMotion.CLOSE_NANOS)) minecraft.gui.setScreen(parent);
    }
    @Override public boolean isPauseScreen() { return false; }
    /** One setting per row mirrors the reference client and gives each control breathing room. */
    private void layoutControls() {
        var body = controls.stream().filter(b -> !fixed.contains(b))
            .sorted(Comparator.comparingInt(AbstractWidget::getY).thenComparingInt(AbstractWidget::getX)).toList();
        int y = top;
        for (var b : body) {
            b.setX(left); b.setWidth(contentWidth); b.setHeight(CONTROL_HEIGHT);
            b.setY(y); baseY.put(b, y); y += CONTROL_HEIGHT + GAP;
        }
    }
    private void initFooter() {
        initNavigation();
        int y = frameTop + frameHeight - 34;
        fixed.add(button("Layout studio", left, y, column, 24, () -> {
            save(); minecraft.gui.setScreen(new TrevorLayoutScreen(this));
        }));
        fixed.add(button("Done", left + column + GAP, y, contentWidth - column - GAP, 24, this::onClose));
    }
    private void initNavigation() {
        if (!sidebar) return;
        String[] names={"Modules", "Edit HUD", "Menu & Keys", "Appearance", "Audio Mixer"};
        int[] targets={-1,-2,7,2,5};
        for (int i=0;i<names.length;i++) {
            final int target=targets[i];
            int y=frameTop+79+i*31+(i>=3?14:0);
            var b=button(names[i],frameLeft+12,y,110,26,()->{
                if(target==-2){save();minecraft.gui.setScreen(new TrevorLayoutScreen(this));}
                else selectModule(target);
            });
            navigation.put(b,target);fixed.add(b);
        }
    }
    public void selectFilter(int next) {
        filter=Math.clamp(next,0,FILTERS.length-1);scroll=0;visibleScroll=0;smoothWheel=false;
        if(page<0)rebuildWidgets();
    }
    public void setSearch(String text) {
        query=text==null?"":text;
        if(searchBox!=null)searchBox.setValue(query);
        if(page<0)layoutDashboard();
    }
    private boolean matches(int module) {
        boolean category=filter==0 || filter==1&&(module==0||module==1||module==6)
            || filter==2&&(module==2||module==3||module==4) || filter==3&&module==5;
        return category && (TITLES[module]+" "+DESCRIPTIONS[module]).toLowerCase(Locale.ROOT)
            .contains(query.toLowerCase(Locale.ROOT).strip());
    }
    private void initDashboard() {
        for(int i=0;i<TITLES.length;i++) {
            final int module=i;
            var b=button(TITLES[i],left,top,100,90,()->selectModule(module));modules.put(b,i);
        }
        int searchW=Math.min(180,contentWidth/2);
        searchBox=new EditBox(font,left+contentWidth-searchW,frameTop+16,searchW,20,Component.literal("Search modules"));
        searchBox.setBordered(false);searchBox.setTextColor(0xEAE6F2);searchBox.setTextShadow(false);
        searchBox.setHint(Component.literal("Search modules..."));searchBox.setMaxLength(64);searchBox.setValue(query);
        searchBox.setResponder(value->{query=value;scroll=0;visibleScroll=0;smoothWheel=false;layoutDashboard();});
        controls.add(searchBox);fixed.add(searchBox);addRenderableWidget(searchBox);
        int x=left;
        for(int i=0;i<FILTERS.length;i++) {
            final int category=i;int w=i==0?36:i==2?56:49;
            var b=button(FILTERS[i],x,frameTop+(height>=300?68:44),w,height>=300?24:20,()->selectFilter(category));
            filters.put(b,i);fixed.add(b);x+=w+7;
        }
        initFooter();layoutDashboard();
    }
    private void layoutDashboard() {
        baseY.clear();
        var shown=modules.entrySet().stream().filter(e->matches(e.getValue())).sorted(Map.Entry.comparingByValue()).toList();
        for(var e:modules.entrySet()){e.getKey().visible=false;e.getKey().active=matches(e.getValue());}
        int columns=contentWidth>=820?4:contentWidth>=420?3:2;
        int cardW=(contentWidth-(columns-1)*GAP)/columns;
        int cardH=Math.min(height>=300?142:110,viewportBottom()-top);
        for(int i=0;i<shown.size();i++) {
            var b=shown.get(i).getKey();int count=Math.min(columns,shown.size()-(i/columns)*columns);
            int start=left+(contentWidth-count*cardW-(count-1)*GAP)/2;
            b.setX(start+(i%columns)*(cardW+GAP));b.setWidth(cardW);b.setHeight(cardH);
            int y=top+(i/columns)*(cardH+GAP);baseY.put(b,y);
        }
        positionRows();
    }
    int viewportTop() { return top; }
    int viewportBottom() { return frameTop + frameHeight - 44; }
    private int maximumScroll() {
        int bottom = top;
        for (var e : baseY.entrySet()) bottom = Math.max(bottom, e.getValue() + e.getKey().getHeight());
        return Math.max(0, bottom - viewportBottom());
    }
    private void positionRows() {
        scroll = Math.clamp(scroll, 0, maximumScroll());
        if (!smoothWheel || !TrevorRollClient.settings().menuAnimations) visibleScroll=scroll;
        for (var e : baseY.entrySet()) {
            var b = e.getKey(); b.setY(e.getValue() - (int)Math.round(visibleScroll));
            b.visible = b.getBottom() > top && b.getY() < viewportBottom();
            if (getFocused() == b && (b.getY() < top || b.getBottom() > viewportBottom())) setFocused(null);
        }
    }
    private void reveal(AbstractWidget b) {
        if (!baseY.containsKey(b)) return;
        smoothWheel=false;visibleScroll=scroll;positionRows();
        if (b.getY() < top) scroll -= top - b.getY();
        else if (b.getBottom() > viewportBottom()) scroll += b.getBottom() - viewportBottom();
        positionRows();
    }
    @Override public boolean mouseScrolled(double x, double y, double horizontal, double vertical) {
        if (closingAt != 0) return true;
        if (x >= frameLeft && x <= frameLeft + frameWidth && y >= top && y < viewportBottom() && vertical != 0) {
            smoothWheel = Math.abs(vertical) < 10 && Math.abs(vertical) >= .1;
            scroll += (int) Math.round(-vertical * (CONTROL_HEIGHT + GAP)); positionRows(); return true;
        }
        return super.mouseScrolled(x, y, horizontal, vertical);
    }
    @Override public boolean mouseClicked(MouseButtonEvent e, boolean twice) {
        if (closingAt != 0) return true;
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
        smoothWheel=false;
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
        if (closingAt != 0) return true;
        if (capturingKey) {
            if (e.key() == 259 || e.key() == 261) MenuKeybind.clear();
            else if (e.key() != 256) MenuKeybind.bind(e);
            capturingKey = false; rebuildWidgets(); return true;
        }
        if (e.key() == 258) { // Include offscreen controls, revealing the newly focused row.
            var choices = controls.stream().filter(b -> b.active).toList();
            if (choices.isEmpty()) return false;
            int at = choices.indexOf(getFocused());
            int next = at < 0 ? ((e.modifiers() & 1) == 0 ? 0 : choices.size() - 1)
                : Math.floorMod(at + ((e.modifiers() & 1) == 0 ? 1 : -1), choices.size());
            var b = choices.get(next); reveal(b); setFocused(b); return true;
        }
        if (searchBox!=null && searchBox.isFocused()) return super.keyPressed(e);
        if (e.key() == 266 || e.key() == 267 || e.key() == 268 || e.key() == 269) {
            smoothWheel=false;
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
        return module==4||module==6?0xFFE7C081:module==1?0xFFA9C8AE:0xFFC4AFE9;
    }
    private void icon(MinecraftCanvas c,int module,double x,double y,double scale) {
        var r=TrevorRollClient.settings();
        if(r.advancedMenuGraphics)MenuArt.advanced(c,module,x,y,scale,MenuMotion.iconAngle(System.nanoTime()-openedAt,module));
        else ModuleIcons.draw(c,module,x,y,scale,accent(module));
    }
    private void moduleCard(GuiGraphicsExtractor g,MinecraftCanvas c,AbstractWidget b,int module,int mx,int my) {
        int x=b.getX(),y=b.getY(),w=b.getWidth(),h=b.getHeight();
        boolean hover=mx>=x&&mx<b.getRight()&&my>=y&&my<b.getBottom()&&my>=top&&my<viewportBottom();
        double state=hoverStates.getOrDefault(b,0.);
        state=MenuMotion.approach(state,hover||b.isFocused()?1:0,frameSeconds,.09);
        hoverStates.put(b,state);
        int tone=accent(module);
        c.round(x,y+3,w,h,12,0x50000000);
        c.round(x,y,w,h,12,MenuMotion.mix(0xFF37424E,tone,state*.8));
        c.round(x+1,y+1,w-2,h-2,11,MenuMotion.mix(0xFF1D2632,0xFF303448,state));
        c.round(x+10,y+10,22,3,1.5,tone);
        if(h>=112)MenuFont.text(g,"0"+(module+1),x+w-24,y+9,.68,0xFF8896AB);
        double iconY=y+(h>=112?51:(h-26)/2.);
        c.glow(x+w/2.-34,iconY-30,68,60,MenuMotion.mix(0x088E68D6,0x388E68D6,state));
        icon(c,module,x+w/2.,iconY-state*2, h>=112?1.8:1.35);
        if(h>=112)MenuFont.text(g,MenuFont.ellipsis(SUMMARIES[module],(w-18)/.7),
            x+(w-MenuFont.width(MenuFont.ellipsis(SUMMARIES[module],(w-18)/.7))*.7)/2,y+h-48,.7,0xFFA8B4C8);
        int band=MenuMotion.mix(0xFF514779,0xFF7964B5,state);
        c.round(x+1,y+h-30,w-2,29,11,band);
        c.round(x+1,y+h-30,w-2,14,0,band);
        double size=Math.min(1.02,(w-18)/MenuFont.width(TITLES[module]));
        MenuFont.text(g,TITLES[module],x+(w-MenuFont.width(TITLES[module])*size)/2,y+h-20,size,0xFFFFF5E5);
    }
    private static final String[] SUMMARIES={"Every hunt, revealed", "Your next upgrade", "Set the mood", "A place for everything",
        "Celebrate the rare ones", "Find your sound", "Keep the pelts coming", "Made to feel like you"};
    private double frameSeconds;
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta) {
        long now=System.nanoTime();frameSeconds=Math.min(.1,Math.max(0,(now-lastFrame)/1e9));lastFrame=now;
        boolean motion=TrevorRollClient.settings().menuAnimations;
        frameOpacity=closingAt==0?MenuMotion.openingOpacity(now-openedAt,motion)
            :closingOpacity*MenuMotion.closingOpacity(now-closingAt,motion);
        if(smoothWheel) {
            visibleScroll=MenuMotion.approach(visibleScroll,scroll,frameSeconds,.055);
            if(Math.abs(visibleScroll-scroll)<.35){visibleScroll=scroll;smoothWheel=false;}
            positionRows();
        }
        double previousOpacity=MenuFont.opacity();MenuFont.opacity(frameOpacity);
        try {
        var c=new MinecraftCanvas(g,frameOpacity,true);
        c.round(0,0,width,height,0,0xB80D1018);
        c.round(frameLeft,frameTop+5,frameWidth,frameHeight,13,0x88000000);
        c.round(frameLeft,frameTop,frameWidth,frameHeight,13,0xFF3B4353);
        c.round(frameLeft+1,frameTop+1,frameWidth-2,frameHeight-2,12,0xFF111820);
        if(sidebar) {
            c.round(frameLeft+2,frameTop+2,128,frameHeight-4,11,0xFF192130);
            c.round(frameLeft+116,frameTop+2,14,frameHeight-4,0,0xFF192130);
            c.round(frameLeft+130,frameTop+1,1,frameHeight-2,0,0xFF293342);
            MenuArt.logo(c,frameLeft+29,frameTop+29,.66);
            MenuFont.text(g,"TREVOR",frameLeft+47,frameTop+21,1.35,0xFFD3BDF4);
            MenuFont.text(g,"Little hunts. Big moments.",frameLeft+15,frameTop+48,.68,0xFFB7B1BF);
            MenuFont.text(g,"YOUR WORKSHOP",frameLeft+17,frameTop+67,.65,0xFF8996AB);
            MenuFont.text(g,"MAKE IT YOURS",frameLeft+17,frameTop+177,.65,0xFF8996AB);
            MenuFont.text(g,"Trevor Roll  /  1.8.0",frameLeft+15,frameTop+frameHeight-23,.65,0xFF8F9AB0);
        }
        if(page<0) {
            MenuFont.text(g,(contentWidth>=460?"Your hunt. Your way.":"Modules"),left,frameTop+17,1.45,0xFFF3EBDD);
            if(height>=300)MenuFont.text(g,"Eight little modules. A world of possibilities.",left,frameTop+43,.78,0xFFA8B4C8);
        } else {
            icon(c,page,left+87,frameTop+27,.7);
            MenuFont.text(g,TITLES[page],left+104,frameTop+18,1.35,0xFFF3EBDD);
            MenuFont.text(g,MenuFont.ellipsis(DESCRIPTIONS[page],contentWidth/.82),left,frameTop+(height>=300?64:49),.82,0xFFADB5C4);
        }
        g.enableScissor(left-1,top,left+contentWidth+1,viewportBottom());
        try {
            for(var b:controls) {
                if(!b.visible||fixed.contains(b))continue;
                if(page<0)moduleCard(g,c,b,modules.get(b),mx,my);else MenuTheme.control(g,b,mx,my);
            }
            if(page<0&&baseY.isEmpty()) {
                MenuFont.text(g,"No modules found",left+12,top+18,1,0xFFE4DAEE);
                MenuFont.text(g,"Try a different search or category.",left+12,top+35,.8,0xFF9FAABE);
            }
        } finally {g.disableScissor();}
        c.round(left,viewportBottom()+5,contentWidth,1,0,0xFF303949);
        for(var b:fixed) {
            if(b==searchBox) {
                c.round(b.getX()-7,b.getY()-4,b.getWidth()+10,26,6,searchBox.isFocused()?0xFF8971BD:0xFF34404C);
                c.round(b.getX()-6,b.getY()-3,b.getWidth()+8,24,5,0xFF202A34);
                searchBox.setAlpha((float)frameOpacity);
                searchBox.extractWidgetRenderState(g,mx,my,delta);continue;
            }
            Integer category=filters.get(b);
            if(category!=null) {
                boolean selected=category==filter;
                c.round(b.getX(),b.getY(),b.getWidth(),b.getHeight(),6,b.isFocused()?0xFFD7C2FA:selected?0xFF8768E5:0xFF3B4652);
                c.round(b.getX()+1,b.getY()+1,b.getWidth()-2,b.getHeight()-2,5,selected?0xFF6C57C7:0xFF232D37);
                MenuFont.text(g,FILTERS[category],b.getX()+(b.getWidth()-MenuFont.width(FILTERS[category])*.85)/2,
                    b.getY()+(b.getHeight()-8)/2.,.85,0xFFF1E9F5);continue;
            }
            Integer target=navigation.get(b);
            if(target==null){MenuTheme.control(g,b,mx,my);continue;}
            boolean selected=target==-1&&(page<0||page==0||page==1||page==3||page==4||page==6)||target==page;
            boolean hover=mx>=b.getX()&&mx<b.getRight()&&my>=b.getY()&&my<b.getBottom();
            if(selected||hover||b.isFocused())c.round(b.getX(),b.getY(),b.getWidth(),b.getHeight(),6,
                b.isFocused()?0xFF8972BA:selected?0xFF6651C5:0xFF2D3748);
            int icon=target==-2?3:target<0?0:target;
            ModuleIcons.draw(c,icon,b.getX()+14,b.getY()+13,.45,0xFFB8ADD2);
            String label=b.getMessage().getString();
            MenuFont.text(g,label,b.getX()+29,b.getY()+9,.86,selected?0xFFFFF1E2:0xFFC2CAD7);
        }
        if(maximumScroll()>0) {
            double track=viewportBottom()-top,thumb=scrollbarThumb();
            c.round(left+contentWidth+7,top,3,track,1.5,0xFF303A48);
            c.round(left+contentWidth+7,top+(track-thumb)*visibleScroll/maximumScroll(),3,thumb,1.5,0xFF9E85CF);
        }
        } finally { MenuFont.opacity(previousOpacity); if(searchBox!=null)searchBox.setAlpha(1); }
    }
}
