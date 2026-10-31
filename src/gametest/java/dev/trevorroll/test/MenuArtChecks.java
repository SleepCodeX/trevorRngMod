package dev.trevorroll;

import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.Component;
import org.lwjgl.opengl.GL11;

/** Real renderer smoke checks, including singular-looking edge-on prism angles. */
public final class MenuArtChecks {
    private MenuArtChecks() {}
    private record State(Screen screen, int windowWidth, int windowHeight, int guiScale,
                         boolean advanced, boolean animations) {}

    public static void run(ClientGameTestContext ctx) {
        State original = snapshot(ctx);
        try {
            ctx.getInput().resizeWindow(1280, 720);
            ctx.runOnClient(mc -> mc.options.guiScale().set(2));
            double[] angles = {0, Math.PI / 4, Math.PI / 2, Math.PI, 3 * Math.PI / 2};
            for (int frame = 0; frame < angles.length; frame++) {
                final double angle = angles[frame];
                ctx.setScreen(() -> new ArtScreen(angle));
                ctx.waitTicks(3);
                ctx.runOnClient(mc -> {
                    ArtScreen screen = (ArtScreen) mc.gui.screen();
                    require(screen.renderedFrames > 0, "All artwork reaches the real render path");
                    require(GL11.glGetError() == GL11.GL_NO_ERROR, "No OpenGL error at angle " + angle);
                    double x = screen.probe.getX() + screen.probe.getWidth() / 2.;
                    double y = screen.probe.getY() + screen.probe.getHeight() / 2.;
                    MouseButtonEvent click = new MouseButtonEvent(x, y, new MouseButtonInfo(0, 0));
                    require(screen.mouseClicked(click, false), "Artwork does not intercept button input");
                    screen.mouseReleased(click);
                    screen.setFocused(screen.probe);
                    require(screen.keyPressed(new KeyEvent(257, 0, 0)), "Native keyboard activation remains available");
                    require(screen.activations == 2, "Mouse and keyboard activate exactly once each");
                });
                ctx.takeScreenshot("menu-art-angle-" + frame);
            }
            System.out.println("PASS: all eight pixel/3D icons render at five angles, including edge-on; native input and GL state remain valid");
        } finally { restore(ctx, original); }
    }

    /** Twelve untouched screenshots of the actual animated dashboard, not a mockup. */
    public static void captureDashboardRotation(ClientGameTestContext ctx) {
        State original = snapshot(ctx);
        try {
            ctx.getInput().resizeWindow(1280, 720);
            ctx.runOnClient(mc -> {
                mc.options.guiScale().set(2);
                TrevorRollClient.settings().advancedMenuGraphics = true;
                TrevorRollClient.settings().menuAnimations = false;
                mc.gui.setScreen(new TrevorSettingsScreen());
            });
            ctx.waitTicks(5);
            for (int frame = 0; frame < 12; frame++) {
                if (frame > 0) ctx.waitTicks(40);
                ctx.runOnClient(mc -> {
                    require(mc.gui.screen() instanceof TrevorSettingsScreen, "Rotation capture stays in the real settings screen");
                    require(GL11.glGetError() == GL11.GL_NO_ERROR, "No OpenGL error during dashboard rotation");
                });
                ctx.takeScreenshot("settings-advanced-rotation-" + String.format(java.util.Locale.ROOT, "%02d", frame));
            }
        } finally { restore(ctx, original); }
    }

    private static State snapshot(ClientGameTestContext ctx) {
        return ctx.computeOnClient(mc -> new State(mc.gui.screen(), mc.getWindow().getWidth(),
            mc.getWindow().getHeight(), mc.options.guiScale().get(),
            TrevorRollClient.settings().advancedMenuGraphics, TrevorRollClient.settings().menuAnimations));
    }
    private static void restore(ClientGameTestContext ctx, State state) {
        ctx.getInput().resizeWindow(state.windowWidth, state.windowHeight);
        ctx.runOnClient(mc -> {
            mc.options.guiScale().set(state.guiScale);
            TrevorRollClient.settings().advancedMenuGraphics = state.advanced;
            TrevorRollClient.settings().menuAnimations = state.animations;
            mc.gui.setScreen(state.screen);
            // SettingsScreen.removed() persists; restore the original values on disk too.
            TrevorSettingsScreen.save();
        });
        ctx.waitTick();
    }
    private static void require(boolean ok, String description) {
        if (!ok) throw new AssertionError(description);
    }

    private static final class ArtScreen extends Screen {
        private final double angle;
        private final String angleLabel;
        private Button probe;
        private int renderedFrames, activations;
        ArtScreen(double angle) {
            super(Component.literal("Trevor original artwork validation"));
            this.angle = angle;
            angleLabel = "DEPTH / " + Math.round(Math.toDegrees(angle)) + " DEGREES";
        }
        @Override protected void init() {
            probe = addRenderableWidget(Button.builder(Component.literal("Input check"), ignored -> activations++)
                .bounds(width / 2 - 48, height - 35, 96, 24).build());
        }
        @Override public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float dt) {
            g.fill(0, 0, width, height, 0xFF191E2B);
            var c = new MinecraftCanvas(g);
            MenuArt.logo(c, 36, 35, 1);
            MenuFont.text(g, "TREVOR / ORIGINAL PIXEL ART", 62, 23, 1.2, 0xFFF2E6C9);
            MenuFont.text(g, "Eight hand-authored sprites. Optional shaded depth.", 62, 41, .9, 0xFFB8A8CC);
            MenuFont.text(g, "STATIC / BASELINE", 24, 76, .85, 0xFFBFCDBF);
            MenuFont.text(g, angleLabel, 24, 179, .85, 0xFFE5C98F);
            double spacing = (width - 48.) / 8;
            for (int module = 0; module < 8; module++) {
                double x = 24 + (module + .5) * spacing;
                c.round(x - 29, 95, 58, 66, 5, 0xFF292D3C);
                c.round(x - 29, 198, 58, 77, 5, 0xFF292D3C);
                MenuArt.module(c, module, x, 124, 1.55);
                MenuArt.advanced(c, module, x, 232, 1.7, angle);
                MenuFont.text(g, Integer.toString(module + 1), x - 2, 149, .8, 0xFFB8A8CC);
                MenuFont.text(g, Integer.toString(module + 1), x - 2, 262, .8, 0xFFB8A8CC);
            }
            MenuTheme.control(g, probe, mx, my);
            renderedFrames++;
        }
        @Override public boolean isPauseScreen() { return false; }
    }
}
