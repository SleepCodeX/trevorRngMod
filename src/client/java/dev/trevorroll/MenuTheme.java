package dev.trevorroll;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import java.util.*;
/** Native widget input and focus, with a coherent shaded visual layer. */
final class MenuTheme {
    private static final Map<AbstractWidget,Double> VALUES=new WeakHashMap<>();
    private static final Map<AbstractWidget,String> HELP=new WeakHashMap<>();
    private static final Map<AbstractWidget,Motion> MOTION=new WeakHashMap<>();
    private static final Map<AbstractWidget,HelpLayout> LINES=new WeakHashMap<>();
    private static final class Motion { double hover,on; long at=System.nanoTime(); boolean initialized; }
    private record HelpLayout(String text,int width,String[] lines) {}
    private static Motion motion(AbstractWidget b,boolean hover,boolean on) {
        var m=MOTION.computeIfAbsent(b,k->new Motion());long now=System.nanoTime();
        double dt=Math.min(.1,(now-m.at)/1e9);m.at=now;
        if(!m.initialized){m.on=on?1:0;m.initialized=true;}
        boolean animate=TrevorRollClient.settings().menuAnimations;
        m.hover=animate?MenuMotion.approach(m.hover,hover||b.isFocused()?1:0,dt,.075):(hover?1:0);
        m.on=animate?MenuMotion.approach(m.on,on?1:0,dt,.065):(on?1:0);
        return m;
    }
    static void help(AbstractWidget b,String text){HELP.put(b,text);}
    static String hoveredHelp(java.util.List<? extends AbstractWidget> controls,int x,int y){
        for(var b:controls)if(b.visible&&x>=b.getX()&&x<b.getRight()&&y>=b.getY()&&y<b.getBottom())return HELP.getOrDefault(b, "");
        return "";
    }
    static String helpText(AbstractWidget b){return HELP.getOrDefault(b,"");}
    static void slider(AbstractWidget b,double value){VALUES.put(b,value);}
    static void control(GuiGraphicsExtractor g,AbstractWidget b,int mx,int my){
        if(!b.visible)return;
        if(b.getHeight()>=44){studioControl(g,b,mx,my);return;}
        double x=b.getX(),y=b.getY(),w=b.getWidth(),h=b.getHeight();
        boolean hover=mx>=x&&mx<x+w&&my>=y&&my<y+h,focus=b.isFocused();
        var c=new MinecraftCanvas(g,MenuFont.opacity(),true);
        int edge=!b.active?0xFF9B83E9:focus?0xFFE0D3FF:hover?0xFF8272B0:0xFF3A3D53;
        c.round(x,y+2,w,h,6,0x50000000);c.round(x,y,w,h,6,edge);
        c.round(x+1,y+1,w-2,h-2,5,!b.active?0xFF35304F:hover?0xFF313347:0xFF222536);
        c.round(x+3,y+1,w-6,1,0,0x208F86BC);
        String label=b.getMessage().getString();
        Double value=VALUES.get(b);
        if(value!=null){
            double track=w-20,fill=track*Math.clamp(value,0,1);
            c.round(x+10,y+h-5,track,2,1,0xFF121522);
            c.round(x+10,y+h-5,Math.max(1,fill),2,1,0xFFB39AED);
            c.ellipse(x+8+fill,y+h-6,4,4,0xFFE2D4FF);
            int split=label.lastIndexOf(':');
            String name=split<0?label:label.substring(0,split),shown=split<0?"":label.substring(split+1).trim();
            double ss=Math.min(.92,(w-90)/Math.max(1,MenuFont.width(name)));
            MenuFont.text(g,name,x+9,y+5,ss,0xFFE3DFF0);
            double badge=Math.max(42,MenuFont.width(shown)*.8+12);
            c.round(x+w-badge-8,y+4,badge,14,5,0xFF3F3454);
            MenuFont.text(g,shown,x+w-badge-2,y+7,.8,0xFFDCC9F9);
        }else if(label.endsWith(": ON")||label.endsWith(": OFF")){
            boolean on=label.endsWith(": ON");label=label.substring(0,label.lastIndexOf(':'));
            double s=Math.min(.9,(w-43)/Math.max(1,MenuFont.width(label)));
            MenuFont.text(g,label,x+9,y+8,s,0xFFDDD9E9);
            c.round(x+w-32,y+8,23,10,5,on?0xFF9074CA:0xFF414457);
            c.ellipse(x+w-(on?19:30),y+9,8,8,on?0xFFF4EDFF:0xFF9A9AAD);
        }else{
            double s=Math.min(.94,(w-20)/Math.max(1,MenuFont.width(label)));
            MenuFont.text(g,label,x+(w-MenuFont.width(label)*s)/2,y+8,s,!b.active?0xFFE0D1FF:0xFFD9D6E5);
        }
    }
    /** Generous settings rows; compact editor controls keep their original geometry. */
    private static void studioControl(GuiGraphicsExtractor g, AbstractWidget b, int mx, int my) {
        double x=b.getX(), y=b.getY(), w=b.getWidth(), h=b.getHeight();
        boolean hover=mx>=x&&mx<x+w&&my>=y&&my<y+h;
        var c=new MinecraftCanvas(g,MenuFont.opacity(),true);
        var motion=motion(b,hover,b.getMessage().getString().endsWith(": ON"));
        int edge=b.isFocused()?0xFFD9C5FF:MenuMotion.mix(0xFF34414E,0xFF8E79B5,motion.hover);
        c.round(x,y+2,w,h,9,0x50000000);
        c.round(x,y,w,h,9,edge);
        c.round(x+1,y+1,w-2,h-2,8,MenuMotion.mix(0xFF1C252F,0xFF283242,motion.hover));
        String label=b.getMessage().getString(), help=helpText(b);
        Double value=VALUES.get(b);
        if(value!=null){
            int split=label.lastIndexOf(':');
            String name=split<0?label:label.substring(0,split), shown=split<0?"":label.substring(split+1).trim();
            double badge=Math.max(54,MenuFont.width(shown)*.92+18);
            MenuFont.text(g,MenuFont.ellipsis(name,w-badge-38),x+13,y+10,1,0xFFF3EBDD);
            c.round(x+w-badge-12,y+7,badge,18,6,0xFF3F3656);
            MenuFont.text(g,shown,x+w-badge-12+(badge-MenuFont.width(shown)*.92)/2,y+11,.92,0xFFE9D8FF);
            MenuFont.text(g,MenuFont.ellipsis(help,(w-26)/.8),x+13,y+27,.8,0xFFB6BDCF);
            double track=w-8,fill=track*Math.clamp(value,0,1);
            c.round(x+4,y+h-8,track,3,1.5,0xFF111622);
            c.round(x+4,y+h-8,Math.max(1,fill),3,1.5,0xFFBFA1F3);
            c.ellipse(x+.5+fill,y+h-10,7,7,0xFFF0E3FF);
        }else if(label.endsWith(": ON")||label.endsWith(": OFF")){
            boolean on=label.endsWith(": ON"); label=label.substring(0,label.lastIndexOf(':'));
            MenuFont.text(g,MenuFont.ellipsis(label,w-95),x+13,y+11,1,0xFFF3EBDD);
            MenuFont.text(g,on?"On":"Off",x+w-65,y+13,.76,on?0xFFDCCCF6:0xFFA6B1C3);
            c.round(x+w-45,y+9,32,16,8,MenuMotion.mix(0xFF4C5668,0xFF8B6DE2,motion.on));
            c.ellipse(x+w-43+16*motion.on,y+11,12,12,MenuMotion.mix(0xFFA8B0C2,0xFFF8F3FF,motion.on));
            helpLines(g,b,help,x+13,y+29,w-26);
        }else{
            double labelY=help.isEmpty()?y+(h-9)/2:y+11;
            MenuFont.text(g,MenuFont.ellipsis(label,(w-28)/1.02),x+14,labelY,1.02,0xFFF0EAFB);
            if(!help.isEmpty())helpLines(g,b,help,x+14,y+29,w-28);
        }
    }
    private static void helpLines(GuiGraphicsExtractor g,AbstractWidget b,String text,double x,double y,double width){
        var layout=LINES.get(b);
        if(layout==null||layout.width!=(int)width||!layout.text.equals(text)) {
            var lines=new ArrayList<String>();String line="";
            for(String word:text.split(" ")) {
                String next=line.isEmpty()?word:line+" "+word;
                if(MenuFont.width(next)*.8>width&&!line.isEmpty()) {lines.add(line);line=word;} else line=next;
                if(lines.size()==2)break;
            }
            if(lines.size()<2&&!line.isEmpty())lines.add(MenuFont.ellipsis(line,width/.8));
            layout=new HelpLayout(text,(int)width,lines.toArray(String[]::new));LINES.put(b,layout);
        }
        for(int row=0;row<layout.lines.length;row++)MenuFont.text(g,layout.lines[row],x,y+row*10,.8,0xFFB6BDCF);
    }
}
