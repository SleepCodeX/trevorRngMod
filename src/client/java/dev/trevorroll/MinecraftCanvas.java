package dev.trevorroll;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2f;

/** Smooth, tinted texture corners avoid jagged scanline approximations. */
final class MinecraftCanvas implements HudPainter.Canvas {
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
    private final Minecraft mc = Minecraft.getInstance();
    private final double opacity;
    private final boolean glows;
    public MinecraftCanvas(GuiGraphicsExtractor graphics) { this(graphics, 1, true); }
    public MinecraftCanvas(GuiGraphicsExtractor graphics, double opacity, boolean glows) {
        this.g = graphics; this.opacity = opacity; this.glows = glows;
    }
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
    @Override public void pop() { g.pose().popMatrix(); }
    @Override public double textWidth(String text) { return mc.font.width(text); }
    @Override public void text(String text, double x, double y, double size, int color) {
        color = alpha(color);
        if ((color >>> 24) == 0) return;
        push(x, y, size);
        try { g.text(mc.font, text, 0, 0, color, false); }
        finally { pop(); }
    }
    @Override public void clip(double x, double y, double w, double h) {
        g.enableScissor((int) x, (int) y, (int) (x + w), (int) (y + h));
    }
    @Override public void unclip() { g.disableScissor(); }

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
