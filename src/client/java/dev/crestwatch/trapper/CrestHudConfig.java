package dev.crestwatch.trapper;

import net.fabricmc.loader.api.FabricLoader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Keeps existing configuration paths and keys from Trevor Roll 1.2.0. */
public final class CrestHudConfig {
    public boolean enabled = true;
    public boolean onlyHypixel = true;
    public int top = 85;
    public int rightMargin = 12;
    public double scale = 1.0;
    public double opacity = 1;
    public boolean glow = true;
    public boolean animations = true;
    public boolean showMissing = true;
    public boolean hideDuringRoll = true;

    // Resolve lazily after the Fabric client entrypoint starts, never at class-load time.
    // NOTE: this DOES NOT replace a real Loom build: FabricLoader is an interface,
    // and an incorrectly stub-compiled class still causes IncompatibleClassChangeError.
    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("trapper-crest-hud.properties");
    }

    public static CrestHudConfig load() {
        CrestHudConfig result = new CrestHudConfig();
        Properties properties = new Properties();
        Path path = configPath();
        if (Files.exists(path)) {
            try (InputStream in = Files.newInputStream(path)) {
                properties.load(in);
                result.enabled = Boolean.parseBoolean(properties.getProperty("enabled", "true"));
                result.onlyHypixel = Boolean.parseBoolean(properties.getProperty("onlyHypixel", "true"));
                result.top = bounded(properties.getProperty("top", "85"), 85, 0, 10000);
                result.rightMargin = bounded(properties.getProperty("rightMargin", "12"), 12, 0, 10000);
                result.opacity = boundedDouble(properties.getProperty("opacity", "1"), 1, .25, 1);
                result.glow = Boolean.parseBoolean(properties.getProperty("glow", "true"));
                result.animations = Boolean.parseBoolean(properties.getProperty("animations", "true"));
                result.showMissing = Boolean.parseBoolean(properties.getProperty("showMissing", "true"));
                result.hideDuringRoll = Boolean.parseBoolean(properties.getProperty("hideDuringRoll", "true"));
                result.scale = boundedDouble(properties.getProperty("scale", "1.0"), 1.0, .65, 1.60);
            } catch (IOException ignored) { }
        } else result.save();
        return result;
    }

    private static int bounded(String string, int fallback, int low, int high) {
        try { return Math.clamp(Integer.parseInt(string.trim()), low, high); }
        catch (RuntimeException e) { return fallback; }
    }
    private static double boundedDouble(String string, double fallback, double low, double high) {
        try {
            double parsed = Double.parseDouble(string.trim());
            return Double.isFinite(parsed) ? Math.clamp(parsed, low, high) : fallback;
        } catch (RuntimeException e) { return fallback; }
    }

    public void save() {
        try {
            Path path = configPath();
            Files.createDirectories(path.getParent());
            Properties p = new Properties();
            p.setProperty("enabled", Boolean.toString(enabled));
            p.setProperty("onlyHypixel", Boolean.toString(onlyHypixel));
            p.setProperty("top", Integer.toString(top));
            p.setProperty("rightMargin", Integer.toString(rightMargin));
            p.setProperty("scale", Double.toString(scale));
            p.setProperty("opacity", Double.toString(opacity));
            p.setProperty("glow", Boolean.toString(glow));
            p.setProperty("animations", Boolean.toString(animations));
            p.setProperty("showMissing", Boolean.toString(showMissing));
            p.setProperty("hideDuringRoll", Boolean.toString(hideDuringRoll));
            try (OutputStream out = Files.newOutputStream(path)) {
                p.store(out, "Trevor Roll - Trapper Crest floating HUD (updated live by /trevorcrest)");
            }
        } catch (IOException ignored) { }
    }
}
