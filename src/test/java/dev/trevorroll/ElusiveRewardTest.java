package dev.trevorroll;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;
import javax.imageio.*;
import javax.imageio.metadata.*;
import javax.imageio.stream.ImageOutputStream;

/** Real painter geometry checks plus a reproducible comparison GIF from the same renderer. */
public final class ElusiveRewardTest {
    private static int checks;
    private static void check(boolean ok, String detail) { checks++; if (!ok) throw new AssertionError(detail); }
    private static final class Probe implements HudPainter.Canvas {
        int depth, calls;
        @Override public void push(double x, double y, double scale) { check(Double.isFinite(x+y+scale) && scale > 0, "finite transform"); depth++; }
        @Override public void pop() { check(--depth >= 0, "balanced transforms"); }
        private void geometry(double... values) { for (double v : values) check(Double.isFinite(v), "finite reward geometry"); calls++; }
        @Override public void round(double x,double y,double w,double h,double r,int c) { geometry(x,y,w,h,r); }
        @Override public void ellipse(double x,double y,double w,double h,int c) { geometry(x,y,w,h); }
        @Override public void glow(double x,double y,double w,double h,int c) { geometry(x,y,w,h); }
        @Override public void text(String s,double x,double y,double z,int c) { geometry(x,y,z); }
        @Override public double textWidth(String s) { return s.length()*5.5; }
        @Override public void clip(double x,double y,double w,double h) {}
        @Override public void unclip() {}
    }
    public static void main(String[] args) throws Exception {
        for (var rarity : Hunt.Rarity.values()) {
            check(ElusiveReward.eligible(rarity, true) == (rarity == Hunt.Rarity.ELUSIVE), "only elusive eligible");
            check(!ElusiveReward.eligible(rarity, false), "toggle disables all automatic rewards");
        }
        for (var style : ElusiveReward.Style.values()) {
            var reward = new ElusiveReward(1000, 3500, style, .8, .7, "Overgrown Mushroom Cave");
            check(!reward.expired(4499) && reward.expired(4500), "duration boundary");
            for (int width : new int[]{320,854,1920}) for (int height : new int[]{180,480,1080}) {
                for (int age : new int[]{0,100,400,1000,2100,3400,3500}) {
                    var c = new Probe(); ElusiveRewardPainter.draw(c,reward,1000+age,width,height);
                    check(c.depth == 0, "pose always restored");
                    check(c.calls < 12000, "bounded draw work");
                    if (age == 3500) check(c.calls == 0, "expired reward draws nothing");
                }
            }
        }
        var safe = new ElusiveReward(0,-1,null,Double.NaN,Double.POSITIVE_INFINITY,null);
        check(safe.durationMillis()==1500 && safe.style()==ElusiveReward.Style.EXTRA && safe.intensity()==.8, "invalid settings use safe bounds");
        preview();
        System.out.println("PASS: " + checks + " Elusive-only reward, timing and painter assertions; comparison GIF rendered");
    }
    private static final class AwtCanvas implements HudPainter.Canvas {
        Graphics2D g; final Deque<Graphics2D> stack = new ArrayDeque<>();
        AwtCanvas(Graphics2D g) { this.g=g; }
        @Override public void push(double x,double y,double s) { stack.push(g);g=(Graphics2D)g.create();g.translate(x,y);g.scale(s,s); }
        @Override public void pop() { g.dispose();g=stack.pop(); }
        @Override public void round(double x,double y,double w,double h,double r,int color) {g.setColor(new Color(color,true));g.fill(new RoundRectangle2D.Double(x,y,w,h,r*2,r*2));}
        @Override public void ellipse(double x,double y,double w,double h,int color) {g.setColor(new Color(color,true));g.fill(new Ellipse2D.Double(x,y,w,h));}
        @Override public void glow(double x,double y,double w,double h,int color) {
            for(int i=10;i>0;i--) { int a=(int)((color>>>24)*.07*(1-i/11.));ellipse(x+(1-i/10.)*w/2,y+(1-i/10.)*h/2,w*i/10.,h*i/10.,(color&0xFFFFFF)|(a<<24)); }
        }
        private Font font() { return new Font(Font.MONOSPACED,Font.BOLD,9); }
        @Override public void text(String s,double x,double y,double scale,int c) { push(x,y,scale);g.setFont(font());g.setColor(new Color(c,true));g.drawString(s,0,8);pop(); }
        @Override public double textWidth(String s) { return g.getFontMetrics(font()).stringWidth(s); }
        @Override public void clip(double x,double y,double w,double h) {g.clip(new Rectangle2D.Double(x,y,w,h));}
        @Override public void unclip() {g.setClip(null);}
    }
    private static void preview() throws Exception {
        Path out=Files.createDirectories(Path.of("build/preview"));
        var writer=ImageIO.getImageWritersByFormatName("gif").next();
        try(ImageOutputStream stream=ImageIO.createImageOutputStream(out.resolve("elusive-styles.gif").toFile())) {
            writer.setOutput(stream);writer.prepareWriteSequence(null);
            for(int frame=0;frame<70;frame++) {
                BufferedImage image=new BufferedImage(960,540,BufferedImage.TYPE_INT_RGB);
                var g=image.createGraphics();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(new Color(15,17,30));g.fillRect(0,0,960,540);
                for(var style:ElusiveReward.Style.values()) {
                    int x=(style.ordinal()%2)*480, y=(style.ordinal()/2)*270;
                    var cg=(Graphics2D)g.create(x,y,480,270);
                    cg.setPaint(new GradientPaint(0,0,new Color(28,33,51),0,270,new Color(12,15,25)));cg.fillRect(0,0,480,270);
                    ElusiveRewardPainter.draw(new AwtCanvas(cg),new ElusiveReward(0,3500,style,.8,.7,"Overgrown Mushroom Cave"),frame*50L,480,270);
                    cg.setFont(new Font(Font.MONOSPACED,Font.BOLD,11));cg.setColor(new Color(207,188,240));cg.drawString(style.label.toUpperCase(Locale.ROOT),12,16);
                    cg.setColor(new Color(63,58,82));cg.drawRect(0,0,479,269);cg.dispose();
                }
                g.dispose();
                if(frame==20) ImageIO.write(image,"png",out.resolve("elusive-styles.png").toFile());
                var meta=writer.getDefaultImageMetadata(new ImageTypeSpecifier(image),writer.getDefaultWriteParam());
                String format=meta.getNativeMetadataFormatName();var root=(IIOMetadataNode)meta.getAsTree(format);
                var gce=(IIOMetadataNode)root.getElementsByTagName("GraphicControlExtension").item(0);
                gce.setAttribute("delayTime","5");gce.setAttribute("disposalMethod","none");gce.setAttribute("userInputFlag","FALSE");gce.setAttribute("transparentColorFlag","FALSE");gce.setAttribute("transparentColorIndex","0");
                if(frame==0) {var apps=new IIOMetadataNode("ApplicationExtensions");var app=new IIOMetadataNode("ApplicationExtension");app.setAttribute("applicationID","NETSCAPE");app.setAttribute("authenticationCode","2.0");app.setUserObject(new byte[]{1,0,0});apps.appendChild(app);root.appendChild(apps);}
                meta.setFromTree(format,root);writer.writeToSequence(new IIOImage(image,null,meta),writer.getDefaultWriteParam());
            }
            writer.endWriteSequence();
        } finally {writer.dispose();}
    }
}
