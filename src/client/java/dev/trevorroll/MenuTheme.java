package dev.trevorroll;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import java.util.*;
/** Native widget input and focus, with a coherent shaded visual layer. */
final class MenuTheme {
    private static final Map<AbstractWidget,Double> VALUES=new WeakHashMap<>();
    static void slider(AbstractWidget b,double value){VALUES.put(b,value);}
    static void control(GuiGraphicsExtractor g,AbstractWidget b,int mx,int my){
        if(!b.visible)return;
        double x=b.getX(),y=b.getY(),w=b.getWidth(),h=b.getHeight();
        boolean hover=mx>=x&&mx<x+w&&my>=y&&my<y+h,focus=b.isFocused();
        var c=new MinecraftCanvas(g);
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
            double s=Math.min(.85,(w-20)/Math.max(1,c.textWidth(label)));
            c.text(label,x+(w-c.textWidth(label)*s)/2,y+4,s,0xFFE3DFF0);
        }else if(label.endsWith(": ON")||label.endsWith(": OFF")){
            boolean on=label.endsWith(": ON");label=label.substring(0,label.lastIndexOf(':'));
            double s=Math.min(.9,(w-43)/Math.max(1,c.textWidth(label)));
            c.text(label,x+9,y+(h-8)/2,s,0xFFDDD9E9);
            c.round(x+w-32,y+6,23,10,5,on?0xFF9074CA:0xFF414457);
            c.ellipse(x+w-(on?19:30),y+7,8,8,on?0xFFF4EDFF:0xFF9A9AAD);
        }else{
            double s=Math.min(.94,(w-20)/Math.max(1,c.textWidth(label)));
            c.text(label,x+(w-c.textWidth(label)*s)/2,y+(h-8*s)/2,s,!b.active?0xFFE0D1FF:0xFFD9D6E5);
        }
    }
}
