package dev.trevorroll;

import dev.crestwatch.trapper.CrestHudState;
import java.util.Locale;

/** Observed lore deltas only; no guessed pelts, offline time or repeat completions. */
public final class PeltTracker {
    public static final PeltTracker INSTANCE=new PeltTracker();
    private boolean running,present,previousActive,completed;
    private int last=-1,slot=-1,earned;
    private long tick=-1,elapsed,crestStart=-1;
    public long lastCrestMillis,totalCrestMillis;
    public int completedCrests;
    public void start(){running=true;earned=0;elapsed=0;crestStart=last==0?0:-1;}
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
        if(replacement){crestStart=-1;completed=false;}
        if(count==0&&(last!=0||replacement)){
            startIfIdle();crestStart=elapsed;completed=false;
        }
        if(running&&last>=0&&!replacement&&count>last)earned+=count-last;
        boolean reached=!completed&&last>=0&&!replacement&&last<500&&count>=500;
        if(reached){
            completed=true;
            if(crestStart>=0){lastCrestMillis=Math.max(1,elapsed-crestStart);totalCrestMillis+=lastCrestMillis;completedCrests++;crestStart=-1;}
        }
        if(last<0&&count>=500)completed=true;
        last=count;slot=state.hotbarSlot();return reached;
    }
    public String summary(){
        if(!running)return "Tracker stopped";
        String rate=elapsed<10000?"--/h":String.format(Locale.ROOT,"%,.0f/h",earned*3600000./Math.max(1,elapsed));
        String clock=crestStart>=0?"Now "+time(elapsed-crestStart):lastCrestMillis>0?"Last "+time(lastCrestMillis):"Run "+time(elapsed);
        return rate+"  "+clock;
    }
    public String history(){return completedCrests>0?"Last "+time(lastCrestMillis)+"  Avg "+time(totalCrestMillis/completedCrests):"Start a crest at 0 for timings";}
    public static String time(long millis){long seconds=Math.max(0,millis/1000);return String.format(Locale.ROOT,"%d:%02d",seconds/60,seconds%60);}
}
