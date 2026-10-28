package dev.trevorroll;

import dev.crestwatch.trapper.CrestHudState;
import java.util.Locale;

/** Observed lore deltas only; no guessed pelts, offline time or repeat completions. */
public final class PeltTracker {
    public static final PeltTracker INSTANCE=new PeltTracker();
    private boolean running,present,previousActive,completed;
    private int last=-1,slot=-1,earned;
    private long tick=-1,elapsed;
    public void start(){running=true;earned=0;elapsed=0;}
    public void startIfIdle(){if(!running)start();}
    public void stop(){running=false;}
    public boolean running(){return running;}
    public int earned(){return earned;}
    public long elapsed(){return elapsed;}
    public void advance(long now,boolean inWorld){
        boolean active=inWorld&&present&&running;
        if(tick>=0&&active&&previousActive)elapsed+=Math.clamp(now-tick,0,2000);
        tick=now;previousActive=active;
    }
    /** Returns true once per observed transition to a maxed crest. */
    public boolean observe(CrestHudState state){
        if(state.kind()==CrestHudState.Kind.MISSING||state.peltsEarned()<0){present=false;return false;}
        present=true;int count=state.peltsEarned();
        boolean changedSlot=slot>=0&&slot!=state.hotbarSlot();
        boolean replacement=last>=0&&(count<last||(changedSlot&&count!=last));
        if(replacement){completed=false;}
        if(count==0&&(last!=0||replacement)){
            startIfIdle();completed=false;
        }
        if(running&&last>=0&&!replacement&&count>last)earned+=count-last;
        boolean reached=!completed&&last>=0&&!replacement&&last<500&&count>=500;
        if(reached){
            completed=true;
        }
        if(last<0&&count>=500)completed=true;
        last=count;slot=state.hotbarSlot();return reached;
    }
    public String summary(){
        if(!running)return "Tracker stopped";
        return elapsed<10000?"Measuring pelts/hour…":String.format(Locale.ROOT,"%,.0f pelts/hour",earned*3600000./Math.max(1,elapsed));
    }

}
