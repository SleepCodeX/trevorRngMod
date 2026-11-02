package dev.trevorroll;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import javax.imageio.ImageIO;

/** Reproducible source-art bake. Runtime draws each 256px coin as a single quad. */
public final class PeltCoinAtlas implements HudPainter.Canvas {
    private final Graphics2D g;
    private PeltCoinAtlas(Graphics2D g){this.g=g;}
    public static void main(String[] args)throws Exception{
        var image=new BufferedImage(1280,256,BufferedImage.TYPE_INT_ARGB);var g=image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        for(var rarity:Hunt.Rarity.values())PeltCoins.draw(new PeltCoinAtlas(g),rarity.ordinal()*256+128,122,112,rarity,1);
        g.dispose();Path target=Path.of(args.length>0?args[0]:"build/preview/pelt-coins.png");Files.createDirectories(target.getParent());ImageIO.write(image,"png",target.toFile());
    }
    public void round(double x,double y,double w,double h,double r,int ink){g.setPaint(new GradientPaint((float)x,(float)y,new Color(MenuMotion.mix(ink,0xFFFFFFFF,.12),true),(float)(x+w),(float)(y+h),new Color(MenuMotion.mix(ink,0xFF131824,.16),true)));g.fill(new RoundRectangle2D.Double(x,y,w,h,2*r,2*r));}
    public void ellipse(double x,double y,double w,double h,int ink){
        if(w>100)g.setPaint(new LinearGradientPaint((float)x,(float)y,(float)(x+w*.7),(float)(y+h),
            new float[]{0,.35f,.5f,.72f,1},new Color[]{new Color(MenuMotion.mix(ink,0xFFFFFFFF,.26),true),new Color(ink,true),new Color(MenuMotion.mix(ink,0xFF29303E,.22),true),new Color(MenuMotion.mix(ink,0xFFFFFFFF,.1),true),new Color(MenuMotion.mix(ink,0xFF202633,.3),true)}));
        else g.setColor(new Color(ink,true));g.fill(new Ellipse2D.Double(x,y,w,h));}
    public void push(double x,double y,double s){}public void pop(){}public void glow(double x,double y,double w,double h,int c){}
    public void text(String s,double x,double y,double z,int c){}public double textWidth(String s){return 0;}public void clip(double x,double y,double w,double h){}public void unclip(){}
}
