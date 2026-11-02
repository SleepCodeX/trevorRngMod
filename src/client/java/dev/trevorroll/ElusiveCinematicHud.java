package dev.trevorroll;

import net.minecraft.client.gui.GuiGraphicsExtractor;

/** A cowboy greeting flows into a dragon-held Elusive reveal, with no token finale. */
final class ElusiveCinematicHud {
    private ElusiveCinematicHud() {}
    static void draw(GuiGraphicsExtractor g,Reel reel,long now,int width,int height,double opacity){
        drawFrame(g,ElusiveSequence.progress(now-reel.startedAt,reel.durationMillis),(now-reel.startedAt)/1000.,width,height,opacity,reel.hunt.location());
    }
    static void drawFrame(GuiGraphicsExtractor g,double progress,double seconds,int width,int height,double opacity,String location){
        if(width<100||height<80)return;
        int stage=ElusiveSequence.stage(progress);double local=ElusiveSequence.local(progress);
        double fade=ElusiveSequence.smooth(seconds/.35)*opacity;
        var c=new MinecraftCanvas(g,fade,true);
        int sky=ElusiveSequence.sky(progress);
        c.round(0,0,width,height,0,0xB0161721);
        g.fillGradient(0,0,width,height,alpha(sky,fade*.6),alpha(0xFF0D0E19,fade*.88));
        c.glow(width*.13,height*.12,width*.74,height*.74,stage==0?0x667C976E:stage==1?0x887E56B4:0x99D7AB6B);
        double x=width*.04,w=width*.92,h=height*.96,scale=Math.min(w/58,height*.72/40);
        double transition=stage==1?ElusiveSequence.smooth(local/.14):1;
        if(stage==1&&transition<1)c.cinematic(x,0,w,h,scale,seconds,ElusiveSequence.DRAGON_START-.001,1-transition);
        c.cinematic(x,0,w,h,scale,seconds,progress,transition);
        if(stage>0)for(int i=0;i<18;i++){
            double angle=i*Math.PI*2/18+seconds*.055,travel=(seconds*.8+i*.173)%1;
            double radius=(.16+travel*.42)*Math.min(width,height);
            g.pose().pushMatrix();g.pose().translate(width/2f,height*.5f);g.pose().rotate((float)angle);
            c.round(radius,-.45,5+travel*16,.9,.45,stage==1?0x77BDA1D9:0x99E6CA91);
            g.pose().popMatrix();
        }
        String heading=stage==0?"THE HUNTER":"ELUSIVE";
        double headingScale=Math.min(stage==0?2.0:3.15,width*.76/CinematicFont.titleWidth(heading));
        double hx=(width-CinematicFont.titleWidth(heading)*headingScale)/2,hy=height*.055;
        CinematicFont.title(g,heading,hx,hy+1.3,headingScale,alpha(0xFF362323,fade*.8));
        CinematicFont.title(g,heading,hx,hy,headingScale,alpha(stage==0?0xFFE3CAA0:0xFFFFDF96,fade));
        if(stage==0){
            String caption="TREVOR  /  THE ELUSIVE TRAIL";double fs=Math.min(.78,width*.8/CinematicFont.bodyWidth(caption));
            CinematicFont.body(g,caption,(width-CinematicFont.bodyWidth(caption)*fs)/2,height*.91,fs,alpha(0xFFE1C7A2,fade));
        }else if(stage==2&&!location.isBlank()){
            double fs=Math.min(.95,width*.8/CinematicFont.bodyWidth(location));
            CinematicFont.body(g,location,(width-CinematicFont.bodyWidth(location)*fs)/2,height*.91,fs,alpha(0xFFE6D9C1,fade));
        }
    }
    private static int alpha(int color,double amount){return(color&0xFFFFFF)|((int)Math.round((color>>>24)*Math.clamp(amount,0,1))<<24);}
}
