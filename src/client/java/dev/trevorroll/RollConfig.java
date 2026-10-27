package dev.trevorroll;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.LoggerFactory;
import java.nio.file.Files;
import java.nio.file.Path;

final class RollConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("trevor-roll.json");
    boolean enabled = true;
    boolean sound = true;
    int durationMillis = 3000;
    double scale = 1.0;
    double x = .5;
    double y = 0;
    boolean customPosition = false;

    static RollConfig load() {
        try {
            if (Files.exists(PATH)) {
                RollConfig config = GSON.fromJson(Files.readString(PATH), RollConfig.class);
                if (config != null) {
                    config.durationMillis = Math.clamp(config.durationMillis, 1000, 8000);
                    config.scale = HudLayout.finiteClamp(config.scale, .5, 2, 1);
                    config.x = HudLayout.finiteClamp(config.x, 0, 1, .5);
                    config.y = HudLayout.finiteClamp(config.y, 0, 1, 0);
                    return config;
                }
            }
        } catch (Exception e) {
            LoggerFactory.getLogger("trevorroll").warn("Could not read Trevor Roll config; using defaults", e);
        }
        return new RollConfig();
    }

    void save() {
        try {
            Files.createDirectories(PATH.getParent());
            Files.writeString(PATH, GSON.toJson(this));
        } catch (Exception e) {
            LoggerFactory.getLogger("trevorroll").warn("Could not save Trevor Roll config", e);
        }
    }
}
