package dev.trevorroll;

/** Smooth minted pelt emblems, with a distinct metal/enamel color for every rarity. */
public final class PeltCoins {
    private PeltCoins() {}
    private static final int[] COLORS={0xFFC9CDD5,0xFF83C7A1,0xFF78B4E7,0xFFB181ED,0xFFEFC570};
    private static final String[] NAMES={"Trackable","Untrackable","Undetected","Endangered","Elusive"};
    private static final String[] TIERS={"COMMON","UNCOMMON","RARE","EPIC","LEGENDARY"};
    public static String name(Hunt.Rarity rarity){return NAMES[rarity.ordinal()];}
    public static String tier(Hunt.Rarity rarity){return TIERS[rarity.ordinal()];}
    public static int color(Hunt.Rarity rarity){return COLORS[rarity.ordinal()];}
    public static String tagline(Hunt.Rarity rarity){return switch(rarity){
        case TRACKABLE->"The trail is yours.";
        case UNTRACKABLE->"Follow the wild.";
        case UNDETECTED->"Leave no trail unseen.";
        case ENDANGERED->"A rare trail. Make it count.";
        case ELUSIVE->"Too rare to stand still.";
    };}
    public static void draw(HudPainter.Canvas c,double x,double y,double radius,Hunt.Rarity rarity,double opacity){
        int base=color(rarity),dark=MenuMotion.mix(base,0xFF23232F,.58),light=MenuMotion.mix(base,0xFFFFF6DD,.5);
        double s=radius/14;
        c.ellipse(x-radius,y-radius+2*s,radius*2,radius*2,tint(dark,opacity));
        c.ellipse(x-radius,y-radius,radius*2,radius*2,tint(light,opacity));
        c.ellipse(x-radius+1.4*s,y-radius+1.4*s,(radius-1.4*s)*2,(radius-1.4*s)*2,tint(dark,opacity));
        c.ellipse(x-radius+2.3*s,y-radius+2.3*s,(radius-2.3*s)*2,(radius-2.3*s)*2,tint(base,opacity));
        c.ellipse(x-radius+3.2*s,y-radius+3.2*s,10*s,5*s,tint(light,opacity*.62));
        // Raised hide body and four splayed feet; this is a pelt, rather than a currency letter.
        hide(c,x+.5*s,y+1.1*s,s,tint(dark,opacity));
        hide(c,x-.3*s,y-.3*s,s,tint(light,opacity));
        for(int i=0;i<12;i++){
            double a=i*Math.PI/6;
            c.ellipse(x+Math.cos(a)*(radius-3*s)-.45*s,y+Math.sin(a)*(radius-3*s)-.45*s,.9*s,.9*s,tint(light,opacity*.65));
        }
    }
    private static void hide(HudPainter.Canvas c,double x,double y,double s,int ink){
        c.round(x-2.8*s,y-4.3*s,5.6*s,9*s,1.9*s,ink);
        for(int side=-1;side<=1;side+=2){
            c.round(x+side*3.3*s-1.3*s,y-5.4*s,2.6*s,3.4*s,1*s,ink);
            c.round(x+side*3.3*s-1.3*s,y+2.4*s,2.6*s,3.4*s,1*s,ink);
        }
    }
    private static int tint(int color,double opacity){return color&0xFFFFFF|((int)Math.round((color>>>24)*Math.clamp(opacity,0,1))<<24);}
}
