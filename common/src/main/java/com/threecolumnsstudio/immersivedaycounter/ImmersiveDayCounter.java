package com.threecolumnsstudio.immersivedaycounter;

import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ImmersiveDayCounter {

    public static final String MOD_ID = "idc";
    public static final String MOD_NAME = "Immersive Day Counter";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    private ImmersiveDayCounter() {
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
