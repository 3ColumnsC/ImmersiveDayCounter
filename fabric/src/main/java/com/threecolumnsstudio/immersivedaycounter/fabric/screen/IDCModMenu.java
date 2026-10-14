package com.threecolumnsstudio.immersivedaycounter.fabric.screen;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.threecolumnsstudio.immersivedaycounter.client.screen.IDCConfigScreen;

public final class IDCModMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return IDCConfigScreen::new;
    }
}
