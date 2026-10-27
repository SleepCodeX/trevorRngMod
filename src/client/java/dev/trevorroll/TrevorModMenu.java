package dev.trevorroll;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/** Optional entrypoint: only loaded by Mod Menu when it is installed. */
public final class TrevorModMenu implements ModMenuApi {
    @Override public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return TrevorSettingsScreen::new;
    }
}
