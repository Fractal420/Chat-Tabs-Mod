package com.example.tibiachat.integration;

import com.example.tibiachat.gui.TibiaChatConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * Registered under the "modmenu" entrypoint in fabric.mod.json. Only loaded
 * when ModMenu is installed and asks for it - the mod builds and runs fine
 * without ModMenu present (see the modCompileOnly dependency in build.gradle).
 */
public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return TibiaChatConfigScreen::new;
    }
}
