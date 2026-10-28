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
    private int page=-1,scroll;
    private final Map<net.minecraft.client.gui.components.AbstractWidget,Integer> baseY=new IdentityHashMap<>();
    private final Set<net.minecraft.client.gui.components.AbstractWidget> fixed=Collections.newSetFromMap(new IdentityHashMap<>());
    private static final String[] TITLES={"Roll","Trapper Crest","Appearance","HUD Layout","Jackpots","Audio Mixer","Pelt Tracker"};
    private static final String[] DESCRIPTIONS={"Hunt reveals, pacing and announcements.","Live crest progress and hotbar reminders.","Sizes, opacity and soft lighting.","Drag, resize and align your HUD elements.","Golden Elusive celebrations and effects.","Mix two sounds, audition and set volumes.","Compact pelts/hour and crest-complete alerts."};
    public void selectModule(int module){page=module;scroll=0;rebuildWidgets();}
    private int left, contentWidth, column, top;
    private static final String[] TABS = {"Roll", "Crest", "Looks", "Editor", "Elusive", "Audio", "Tracker"};
    public TrevorSettingsScreen() { this(null); }
    public TrevorSettingsScreen(Screen parent) { super(Component.literal("Trevor Mod settings")); this.parent = parent; }

    @Override protected void init() {
        controls.clear();baseY.clear();fixed.clear();
        contentWidth=Math.min(540,width-36);left=(width-contentWidth)/2;column=(contentWidth-10)/2;top=70;
        if(page<0){initDashboard();return;}
        var home=button("Modules",left,16,79,22,()->selectModule(-1));fixed.add(home);
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
                soundButton("Layer 1",0,r.elusiveSoundStyle,v->r.elusiveSoundStyle=v);
                soundButton("Layer 2",1,r.elusiveSecondSound,v->r.elusiveSecondSound=v);
                slider("Layer 1 volume",2,0,1,r.elusiveVolume,v->r.elusiveVolume=v,"%.0f%%","Volume of the first sound.");
                slider("Layer 2 volume",3,0,1,r.elusiveSecondVolume,v->r.elusiveSecondVolume=v,"%.0f%%","Volume of the second sound, played at the same time.");
                button("Preview both sounds",left,row(4),contentWidth,TrevorRollClient::previewRewardSound);
            }
            case 6 -> {
                toggle("Show stats",left,0,()->r.trackerVisible,v->r.trackerVisible=v,"Show pelts/hour in the crest card.");
                toggle("Crest alert",left+column+8,0,()->r.crestCompleteSound,v->r.crestCompleteSound=v,"Play once when a crest reaches 500 pelts.");
                button("Start / reset pelt session",left,row(1),column,()->PeltTracker.INSTANCE.start());
                button("Stop tracker",left+column+8,row(1),column,()->PeltTracker.INSTANCE.stop());
                soundButton("Maxed sound 1",2,r.crestSoundStyle,v->r.crestSoundStyle=v);
                soundButton("Maxed sound 2",3,r.crestSecondSound,v->r.crestSecondSound=v);
                button("Preview maxed-crest sounds",left,row(4),contentWidth,()->TrevorRollClient.previewSound(r.crestSoundStyle,r.crestSecondSound,(float)r.crestSoundVolume,(float)r.crestSecondVolume));
            }
        }
        for(var b:controls)if(!fixed.contains(b)){baseY.put(b,b.getY());b.setHeight(36);}
        fixed.add(button("Open layout studio",left,height-32,column,()->{save();minecraft.gui.setScreen(new TrevorLayoutScreen(this));}));
        fixed.add(button("Done",left+column+10,height-32,column,this::onClose));
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
        button("Preview reward", left + column + 8, row(4), column, () -> {
            save();
            if (TrevorRollClient.previewElusive() == 1) minecraft.gui.setScreen(null);
        });
    }
    private int row(int index) { return top + index * 44; }
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
    private void initDashboard(){
        int columns=width<350?2:3,cardW=(contentWidth-(columns-1)*10)/columns;
        int cardH=56;
        for(int i=0;i<TITLES.length;i++){
            final int module=i;var b=button(TITLES[i],left+(i%columns)*(cardW+10),70+(i/columns)*(cardH+9),cardW,cardH,()->selectModule(module));
            baseY.put(b,b.getY());
        }
        fixed.add(button("Done",left+contentWidth-90,height-32,90,this::onClose));positionRows();
    }
    private int viewportBottom(){return height-45;}
    private int maximumScroll(){int bottom=76;for(var e:baseY.entrySet())bottom=Math.max(bottom,e.getValue()+e.getKey().getHeight());return Math.max(0,bottom-viewportBottom());}
    private void positionRows(){scroll=Math.clamp(scroll,0,maximumScroll());for(var e:baseY.entrySet()){
        var b=e.getKey();b.setY(e.getValue()-scroll);b.visible=b.getY()>=70&&b.getBottom()<=viewportBottom();
    }}
    @Override public boolean mouseScrolled(double x,double y,double horizontal,double vertical){
        if(y>=65&&y<viewportBottom()){scroll+=(int)(-vertical*44);positionRows();return true;}return super.mouseScrolled(x,y,horizontal,vertical);
    }
    private void description(GuiGraphicsExtractor g,String text,double x,double y,double width,double scale,int color){
        String[] words=text.split(" ");String line="";int n=0;
        for(String word:words){if(MenuFont.width(line+" "+word)*scale>width&&!line.isEmpty()){
            MenuFont.text(g,line,x,y+n*10,scale,color);line=word;if(++n==2)return;
        }else line=line.isEmpty()?word:line+" "+word;}
        if(!line.isEmpty())MenuFont.text(g,line,x,y+n*10,scale,color);
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){
        var c=new MinecraftCanvas(g);g.fill(0,0,width,height,0xE8131420);
        c.glow(left-130,-160,390,350,0x236C4D99);
        c.round(left-11,9,contentWidth+22,height-17,14,0x80000000);
        c.round(left-10,8,contentWidth+20,height-20,13,0xFF393748);
        c.round(left-9,9,contentWidth+18,height-22,12,0xFF202131);
        c.round(left-8,10,contentWidth+16,49,11,0xFF27283A);
        if(page<0){
            MenuFont.text(g,"Modules",left+3,18,1.6,0xFFF0E9FF);
            MenuFont.text(g,"Make Trevor feel like yours.",left+4,39,.85,0xFFA7A0B9);
        }else{
            ModuleIcons.draw(c,page,left+100,29,.7,0xFFBEA5F4);
            MenuFont.text(g,TITLES[page],left+115,18,1.4,0xFFF0E9FF);
            MenuFont.text(g,MenuFont.ellipsis(DESCRIPTIONS[page],(contentWidth-118)/.75),left+115,37,.75,0xFFA8A0BA);
        }
        g.enableScissor(left-3,65,left+contentWidth+3,viewportBottom()+1);
        try{
            for(var b:controls){if(!b.visible||fixed.contains(b))continue;
                if(page<0){
                    int i=controls.indexOf(b);boolean hover=mx>=b.getX()&&mx<b.getRight()&&my>=b.getY()&&my<b.getBottom();
                    c.round(b.getX(),b.getY()+2,b.getWidth(),b.getHeight(),10,0x45000000);
                    c.round(b.getX(),b.getY(),b.getWidth(),b.getHeight(),10,hover?0xFF9E84CB:0xFF414053);
                    c.round(b.getX()+1,b.getY()+1,b.getWidth()-2,b.getHeight()-2,9,hover?0xFF353047:0xFF292939);
                    c.round(b.getX()+10,b.getY()+9,23,23,8,0xFF393149);
                    ModuleIcons.draw(c,i,b.getX()+21.5,b.getY()+20.5,.62,i==4?0xFFFFD583:0xFFC7AFF1);
                    MenuFont.text(g,TITLES[i],b.getX()+40,b.getY()+12,.94,0xFFE8DFF5);
                    description(g,DESCRIPTIONS[i],b.getX()+10,b.getY()+36,b.getWidth()-20,.75,0xFFAAA1BB);
                }else{
                    MenuTheme.control(g,b,mx,my);
                    String help=MenuTheme.helpText(b);
                    if(!help.isEmpty())MenuFont.text(g,MenuFont.ellipsis(help,(b.getWidth()-20)/.67),b.getX()+9,b.getY()+18,.67,0xFF9990AA);
                }
            }
        }finally{g.disableScissor();}
        for(var b:fixed)MenuTheme.control(g,b,mx,my);
        if(maximumScroll()>0){double track=viewportBottom()-70,thumb=Math.max(18,track*track/(track+maximumScroll()));
            c.round(left+contentWidth+4,70,2,track,1,0xFF393748);
            c.round(left+contentWidth+4,70+(track-thumb)*scroll/Math.max(1,maximumScroll()),2,thumb,1,0xFFB59BE4);
            MenuFont.text(g,"Scroll for more",left, height-22,.7,0xFF978CA8);
        }
    }
}
