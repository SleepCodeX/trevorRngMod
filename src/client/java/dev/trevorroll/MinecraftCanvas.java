package dev.trevorroll;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2f;

/** Smooth, tinted texture corners avoid jagged scanline approximations. */
final class MinecraftCanvas implements HudPainter.Canvas {
    @Override public boolean extremeReward(double seconds,double progress,double fade,String location){
        ElusiveCinematicHud.drawFrame(g,1,seconds,mc.getWindow().getGuiScaledWidth(),mc.getWindow().getGuiScaledHeight(),fade*opacity,location);
        return true;
    }
    private static final Identifier CIRCLE = Identifier.fromNamespaceAndPath("trevorroll", "textures/gui/circle.png");
    private static final Identifier[] COINS = java.util.stream.IntStream.range(0,4)
        .mapToObj(i -> Identifier.fromNamespaceAndPath("trevorroll", "textures/gui/coin/coin_"+i+".png")).toArray(Identifier[]::new);
    @Override public boolean medallion(double x,double y,double scale,double yaw,double roll,double opacity) {
        int frame = Math.floorMod((int)Math.round(yaw / (Math.PI*2) * 64),64);
        push(x,y,96*scale/512);
        try {
            g.pose().rotate((float)roll);
            int cell=frame%16;
            g.blit(RenderPipelines.GUI_TEXTURED,COINS[frame/16],-256,-256,(cell%4)*512,(cell/4)*512,
                512,512,512,512,2048,2048,((int)Math.round(opacity*255)<<24)|0xFFFFFF);
        } finally { pop(); }
        return true;
    }
    private static final Identifier GLOW = Identifier.fromNamespaceAndPath("trevorroll", "textures/gui/glow.png");
    private final GuiGraphicsExtractor g;
    private Matrix3x2f artTransform;
    private net.minecraft.client.gui.navigation.ScreenRectangle modelClip;
    private final Minecraft mc = Minecraft.getInstance();
    private final double opacity;
    private final boolean glows;
    private final int fontMode;
    public MinecraftCanvas(GuiGraphicsExtractor graphics) { this(graphics, 1, true); }
    public MinecraftCanvas(GuiGraphicsExtractor graphics, double opacity, boolean glows) {
        this(graphics,opacity,glows,false);
    }
    public MinecraftCanvas(GuiGraphicsExtractor graphics,double opacity,boolean glows,boolean polishedFont){
        this(graphics,opacity,glows,polishedFont?1:0);
    }
    private MinecraftCanvas(GuiGraphicsExtractor graphics,double opacity,boolean glows,int fontMode){
        this.g=graphics;this.opacity=opacity;this.glows=glows;this.fontMode=fontMode;
    }
    static MinecraftCanvas reward(GuiGraphicsExtractor graphics){return new MinecraftCanvas(graphics,1,true,2);}
    private int alpha(int color) {
        return (color & 0xFFFFFF) | ((int) Math.round((color >>> 24) * opacity) << 24);
    }

    @Override public void push(double x, double y, double scale) {
        g.pose().pushMatrix();
        g.pose().translate((float) x, (float) y);
        g.pose().scale((float) scale, (float) scale);
    }
    /** Push an orthographic artwork plane without allocating a matrix per face. */
    void affine(double x, double y, double xx, double xy, double yx, double yy) {
        g.pose().pushMatrix();
        if (artTransform == null) artTransform = new Matrix3x2f();
        artTransform.set((float)xx, (float)xy, (float)yx, (float)yy, (float)x, (float)y);
        g.pose().mul(artTransform);
    }
    /** Packed pixel sprites already have a transform; emit their runs directly. */
    void pixelRect(int x, int y, int width, int height, int colour) {
        colour = alpha(colour);
        if (width > 0 && height > 0 && (colour >>> 24) != 0) g.fill(x, y, x + width, y + height, colour);
    }
    private static final Identifier MENU_ART = Identifier.fromNamespaceAndPath("trevorroll", "textures/gui/menu_art.png");
    void menuSprite(int cell, int size, double x, double y, double scale) {
        push(x - size * scale / 2, y - size * scale / 2, scale);
        try { g.blit(RenderPipelines.GUI_TEXTURED, MENU_ART, 0, 0, cell * 32, 0,
            size, size, size, size, 288, 32, alpha(0xFFFFFFFF)); }
        finally { pop(); }
    }
    void model(int module,double x,double y,double scale,double angle) {
        if(opacity<=0)return;
        int diameter=(int)Math.ceil(42*scale);
        submitModel(module,x-diameter/2.,y-diameter/2.,diameter,diameter,scale,angle,0,0,opacity,2);
    }
    void cinematic(double x,double y,double width,double height,double scale,double seconds,double progress,double fade) {
        submitModel(8,x,y,(int)width,(int)height,scale,0,seconds,progress,fade*opacity,1);
    }
    private void submitModel(int model,double x,double y,int w,int h,double scale,double angle,
                             double seconds,double progress,double alpha,int samples) {
        if(alpha<=0||w<=0||h<=0)return;
        var pose=new Matrix3x2f(g.pose()).translate((float)x,(float)y).scale(1f/samples);
        var bounds=new net.minecraft.client.gui.navigation.ScreenRectangle(0,0,w*samples,h*samples).transformMaxBounds(pose);
        if(modelClip!=null){bounds=bounds.intersection(modelClip);if(bounds==null)return;}
        var state=new SolidModelState(model,angle,seconds,progress,(float)alpha,0,0,w*samples,h*samples,
            (float)(scale*samples),pose,modelClip,bounds);
        ((dev.trevorroll.mixin.GuiGraphicsAccess)(Object)g).trevorroll$renderState().addPicturesInPictureState(state);
    }
    private static final Identifier PELT_COINS=Identifier.fromNamespaceAndPath("trevorroll","textures/gui/pelt_coins.png");
    @Override public boolean peltCoin(double x,double y,double radius,Hunt.Rarity rarity,double fade){
        push(x-radius,y-radius,radius/128);
        try{g.blit(RenderPipelines.GUI_TEXTURED,PELT_COINS,0,0,rarity.ordinal()*256,0,256,256,256,256,1280,256,alpha(((int)Math.round(fade*255)<<24)|0xFFFFFF));}
        finally{pop();}return true;
    }
    @Override public void horizontalFade(double x,double y,double w,double h,int color,boolean reverse){
        g.pose().pushMatrix();
        try{
            g.pose().translate((float)(reverse?x+w:x),(float)(reverse?y:y+h));
            g.pose().rotate((float)(reverse?Math.PI/2:-Math.PI/2));
            g.fillGradient(0,0,(int)h,(int)w,alpha(color),color&0xFFFFFF);
        }finally{g.pose().popMatrix();}
    }
    @Override public void pop() { g.pose().popMatrix(); }
    @Override public double textWidth(String text) {
        return fontMode==2?(text.startsWith("ELUSIVE")?CinematicFont.titleWidth(text):CinematicFont.bodyWidth(text))
            :fontMode==1?MenuFont.width(text):mc.font.width(text);
    }
    @Override public void text(String text, double x, double y, double size, int color) {
        color = alpha(color);
        if ((color >>> 24) == 0) return;
        if(fontMode==2){
            if(text.startsWith("ELUSIVE"))CinematicFont.title(g,text,x,y,size,color);
            else CinematicFont.body(g,text,x,y,size,color);
            return;
        }
        if(fontMode==1){MenuFont.text(g,text,x,y,size,color);return;}
        push(x, y, size);
        try { g.text(mc.font, text, 0, 0, color, false); }
        finally { pop(); }
    }
    @Override public void clip(double x, double y, double w, double h) {
        modelClip=new net.minecraft.client.gui.navigation.ScreenRectangle((int)x,(int)y,(int)w,(int)h).transformMaxBounds(g.pose());
        g.enableScissor((int) x, (int) y, (int) (x + w), (int) (y + h));
    }
    @Override public void unclip() { modelClip=null;g.disableScissor(); }

    @Override public void round(double x, double y, double w, double h, double radius, int color) {
        color = alpha(color);
        if (w <= 0 || h <= 0 || (color >>> 24) == 0) return;
        int iw = (int) Math.ceil(w), ih = (int) Math.ceil(h);
        int r = Math.min(Math.min(iw, ih) / 2, (int) Math.round(radius));
        push(x, y, 1);
        g.pose().scale((float) (w / iw), (float) (h / ih));
        try {
            if (r == 0) { g.fill(0, 0, iw, ih, color); return; }
            g.fill(r, 0, iw - r, ih, color);
            g.fill(0, r, r, ih - r, color);
            g.fill(iw - r, r, iw, ih - r, color);
            corner(0, 0, 0, 0, r, color);
            corner(iw - r, 0, 64, 0, r, color);
            corner(0, ih - r, 0, 64, r, color);
            corner(iw - r, ih - r, 64, 64, r, color);
        } finally { pop(); }
    }
    private void corner(int x, int y, int u, int v, int r, int color) {
        g.blit(RenderPipelines.GUI_TEXTURED, CIRCLE, x, y, u, v, r, r, 64, 64, 128, 128, color);
    }
    @Override public void ellipse(double x, double y, double w, double h, int color) { image(CIRCLE, x, y, w, h, color); }
    @Override public void glow(double x, double y, double w, double h, int color) { if (glows) image(GLOW, x, y, w, h, color); }
    private void image(Identifier id, double x, double y, double w, double h, int color) {
        color = alpha(color);
        if ((color >>> 24) == 0) return;
        push(x, y, 1);
        g.pose().scale((float) (w / 128), (float) (h / 128));
        try { g.blit(RenderPipelines.GUI_TEXTURED, id, 0, 0, 0, 0, 128, 128, 128, 128, color); }
        finally { pop(); }
    }
}
