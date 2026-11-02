package dev.trevorroll;

/** Five deterministic stages. Only the server-assigned Elusive rarity can select this path. */
public final class ElusiveSequence {
    public static final int DEFAULT_MILLIS=8500;
    private static final int[] SKY={0xFF496B67,0xFF9D9C66,0xFF40375E,0xFF1A122E,0xFFBE8546};
    private ElusiveSequence() {}
    public static boolean eligible(Hunt.Rarity rarity,boolean enabled,ElusiveReward.Style style){return enabled&&style==ElusiveReward.Style.JACKPOT&&rarity==Hunt.Rarity.ELUSIVE;}
    public static int duration(Hunt.Rarity rarity,boolean enabled,int normalMillis,int cinematicMillis,ElusiveReward.Style style){
        return eligible(rarity,enabled,style)?Math.clamp(cinematicMillis,5000,14000):normalMillis;
    }
    public static double progress(long elapsed,int duration){return Math.clamp(elapsed/(double)Math.max(1,duration),0,1);}
    public static int stage(double progress){return Math.clamp((int)(Math.clamp(progress,0,1)*5),0,4);}
    public static double local(double progress){return Math.clamp(progress*5-stage(progress),0,1);}
    public static double smooth(double t){t=Math.clamp(t,0,1);return t*t*(3-2*t);}
    public static int sky(double progress){
        int stage=stage(progress);return MenuMotion.mix(SKY[Math.max(0,stage-1)],SKY[stage],smooth(local(progress)*2));
    }
}
