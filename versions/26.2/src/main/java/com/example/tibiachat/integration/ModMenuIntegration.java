package com.example.tibiachat.integration;

import com.example.tibiachat.gui.TibiaChatConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return TibiaChatConfigScreen::new;
    }
}
