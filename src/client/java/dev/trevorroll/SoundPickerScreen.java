package dev.trevorroll;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.*;
import java.util.function.Consumer;

/** Direct sound selection with audition; no long cycling list. */
public final class SoundPickerScreen extends Screen {
    private final Screen parent;
    private final Consumer<RewardSound> apply;
    private final List<Button> controls=new ArrayList<>();
    private RewardSound selected;
    public SoundPickerScreen(Screen parent,RewardSound selected,Consumer<RewardSound> apply){
        super(Component.literal("Choose a sound"));this.parent=parent;this.selected=selected;this.apply=apply;
    }
    @Override protected void init(){
        controls.clear();int w=Math.min(450,width-28),x=(width-w)/2,col=w/3,i=0;
        for(var sound:RewardSound.values()){
            if(sound==RewardSound.DRAGON_LEVEL)continue;
            final var choice=sound;
            var b=Button.builder(Component.literal(sound.label),ignored->{selected=choice;rebuildWidgets();})
                .bounds(x+(i%3)*col,43+(i/3)*24,col-4,20).build();
            b.active=sound!=selected;controls.add(b);addRenderableWidget(b);i++;
        }
        add("Preview",x,height-28,w/3-4,()->TrevorRollClient.previewSound(selected,RewardSound.NONE,.7f,0));
        add("Use sound",x+w/3,height-28,w/3-4,()->{apply.accept(selected);onClose();});
        add("Back",x+2*w/3,height-28,w/3-4,this::onClose);
    }
    private void add(String text,int x,int y,int w,Runnable action){var b=Button.builder(Component.literal(text),v->action.run()).bounds(x,y,w,20).build();controls.add(b);addRenderableWidget(b);}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float dt){
        g.fill(0,0,width,height,0xFA101321);var c=new MinecraftCanvas(g);
        MenuFont.text(g,"CHOOSE A SOUND",(width-Math.min(450,width-28))/2,15,1,0xFFE3D8F7);
        for(var b:controls)MenuTheme.control(g,b,mx,my);
    }
    @Override public void onClose(){minecraft.gui.setScreen(parent);}
    @Override public boolean isPauseScreen(){return false;}
}
