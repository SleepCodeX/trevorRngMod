package dev.trevorroll;

/** Hunter, dragon approach, then a dragon-held reveal. */
public final class ElusiveSequence {
    public static final int DEFAULT_MILLIS=8500;
    public static final double DRAGON_START=.38, REVEAL_START=.78, ROAR_PROGRESS=.5;
    private static final int[] SKY={0xFF706050,0xFF241B35,0xFF453344};
    private ElusiveSequence() {}
    public static boolean eligible(Hunt.Rarity rarity,boolean enabled,ElusiveReward.Style style){return enabled&&style==ElusiveReward.Style.JACKPOT&&rarity==Hunt.Rarity.ELUSIVE;}
    public static int duration(Hunt.Rarity rarity,boolean enabled,int normalMillis,int cinematicMillis,ElusiveReward.Style style){
        return eligible(rarity,enabled,style)?Math.clamp(cinematicMillis,5000,14000):normalMillis;
    }
    public static double progress(long elapsed,int duration){return Math.clamp(elapsed/(double)Math.max(1,duration),0,1);}
    public static int stage(double progress){return progress<DRAGON_START?0:progress<REVEAL_START?1:2;}
    public static double local(double progress){
        int stage=stage(progress);
        double start=stage==0?0:stage==1?DRAGON_START:REVEAL_START;
        double end=stage==0?DRAGON_START:stage==1?REVEAL_START:1;
        return Math.clamp((progress-start)/(end-start),0,1);
    }
    public static double smooth(double t){t=Math.clamp(t,0,1);return t*t*(3-2*t);}
    public static int sky(double progress){
        int stage=stage(progress);return MenuMotion.mix(SKY[Math.max(0,stage-1)],SKY[stage],smooth(local(progress)*2));
    }
}
