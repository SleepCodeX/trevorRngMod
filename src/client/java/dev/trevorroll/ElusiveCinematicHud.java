package dev.trevorroll;

import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Five-stage, Elusive-only HUD with a lit 3D diorama and no input interception. */
final class ElusiveCinematicHud {
    private ElusiveCinematicHud() {}
    private static final String[] TITLES={"A HUNTER'S CALL","FOLLOW THE TRAIL","SOMETHING SHIFTS","THE END AWAKENS","ELUSIVE!"};
    static void draw(GuiGraphicsExtractor g,Reel reel,long now,int width,int height,double opacity){
        double progress=ElusiveSequence.progress(now-reel.startedAt,reel.durationMillis);
        drawFrame(g,progress,(now-reel.startedAt)/1000.,width,height,opacity,reel.hunt.location());
    }
    static void drawFrame(GuiGraphicsExtractor g,double progress,double seconds,int width,int height,double opacity,String location){
        if(width<100||height<80)return;
        int stage=ElusiveSequence.stage(progress);double local=ElusiveSequence.local(progress);
        double fade=ElusiveSequence.smooth(seconds/.35)*opacity;
        double previous=MenuFont.opacity();MenuFont.opacity(fade);
        try {
            var c=new MinecraftCanvas(g,fade,true);
            int sky=ElusiveSequence.sky(progress);
            c.round(0,0,width,height,0,0xB0181726);
            g.fillGradient(0,0,width,height,((int)(fade*145)<<24)|(sky&0xFFFFFF),(int)(fade*215)<<24|0x0B0C19);
            c.glow(width*.11,height*.1,width*.78,height*.75,stage<2?0x6874B58C:stage<4?0x887E41BC:0x99E5B66F);
            double x=width*.04,y=height*.12,w=width*.92,h=height*.72,scale=Math.min(w/58,h/40);
            double transition=ElusiveSequence.smooth(local/.16);
            if(stage>0&&transition<1)c.cinematic(x,y,w,h,scale,seconds,(stage-.001)/5,1-transition);
            c.cinematic(x,y,w,h,scale,seconds,progress,stage==0?1:transition);
            // Moving light streaks lead the eye toward the approaching model.
            if(stage>0)for(int i=0;i<22;i++){
                double angle=i*Math.PI*2/22+seconds*.08;
                double travel=((seconds*(stage==3?1.9:1.1)+i*.173)%1);
                double radius=(.12+travel*.42)*Math.min(width,height);
                g.pose().pushMatrix();g.pose().translate(width/2f,height*.47f);g.pose().rotate((float)angle);
                c.round(radius,-.55,6+travel*20,1.1,.5,stage<3?0x88F4E4A6:stage==3?0x99C9A0F4:0xCCFFE9A9);
                g.pose().popMatrix();
            }
            String heading=TITLES[stage];double textScale=stage==4?2.1:1.2;
            MenuFont.text(g,heading,(width-MenuFont.width(heading)*textScale)/2,height*.1,textScale,stage==4?0xFFFFDF94:0xFFF1EBDD);
            String sub=stage==4?location:"Trevor's elusive trail";
            MenuFont.text(g,sub,(width-MenuFont.width(sub)*.85)/2,height*.88,.85,0xFFE0CEB5);
            for(int i=0;i<5;i++)c.round(width/2.-44+i*19,height*.94,12,3,1.5,i<=stage?0xFFE6C188:0xFF4E485B);
        } finally {MenuFont.opacity(previous);}
    }
}
