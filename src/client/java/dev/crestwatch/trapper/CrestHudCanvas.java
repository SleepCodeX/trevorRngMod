package dev.crestwatch.trapper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/**
 * Reuses the ORIGINAL Trevor Roll circle + glow textures for feathered corners.
 * This duplicates only the small render adapter; original Trevor Roll classes are unchanged.
 */
public final class CrestHudCanvas implements CrestHudPainter.Canvas {
    private static final Identifier CIRCLE = Identifier.fromNamespaceAndPath("trevorroll", "textures/gui/circle.png");
    private static final Identifier GLOW = Identifier.fromNamespaceAndPath("trevorroll", "textures/gui/glow.png");
    private final GuiGraphicsExtractor g;
    private final Minecraft mc = Minecraft.getInstance();

    private final double opacity;
    private final boolean glows;
    public CrestHudCanvas(GuiGraphicsExtractor graphics) { this(graphics, 1, true); }
    public CrestHudCanvas(GuiGraphicsExtractor graphics, double opacity, boolean glows) {
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
    @Override public double textWidth(String str) { return mc.font.width(str); }
    @Override public void text(String value, double x, double y, double scale, int color) {
        color = alpha(color);
        if ((color >>> 24) == 0) return;
        push(x, y, scale);
        try { g.text(mc.font, value, 0, 0, color, false); }
        finally { pop(); }
    }
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
    @Override public void ellipse(double x, double y, double w, double h, int color) {
        sprite(CIRCLE, x, y, w, h, color);
    }
    @Override public void glow(double x, double y, double w, double h, int color) {
        if (glows) sprite(GLOW, x, y, w, h, color);
    }
    private void sprite(Identifier id, double x, double y, double w, double h, int color) {
        color = alpha(color);
        if ((color >>> 24) == 0 || w <= 0 || h <= 0) return;
        push(x, y, 1);
        g.pose().scale((float) (w / 128), (float) (h / 128));
        try { g.blit(RenderPipelines.GUI_TEXTURED, id, 0, 0, 0, 0, 128, 128, 128, 128, color); }
        finally { pop(); }
    }
}
