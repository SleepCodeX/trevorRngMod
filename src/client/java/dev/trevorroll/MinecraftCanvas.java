package dev.trevorroll;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/** Smooth, tinted texture corners avoid jagged scanline approximations. */
final class MinecraftCanvas implements HudPainter.Canvas {
    private static final Identifier CIRCLE = Identifier.fromNamespaceAndPath("trevorroll", "textures/gui/circle.png");
    private static final Identifier GLOW = Identifier.fromNamespaceAndPath("trevorroll", "textures/gui/glow.png");
    private final GuiGraphicsExtractor g;
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
