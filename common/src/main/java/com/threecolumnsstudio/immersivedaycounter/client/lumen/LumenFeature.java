package com.threecolumnsstudio.immersivedaycounter.client.lumen;

import com.mojang.blaze3d.platform.InputConstants;
import com.threecolumnsstudio.immersivedaycounter.ImmersiveDayCounter;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public final class LumenFeature {

    @SuppressWarnings("deprecation")
    public static final KeyMapping.Category CATEGORY =
        KeyMapping.Category.register(ImmersiveDayCounter.id("lumen"));

    public static final KeyMapping KEY = new KeyMapping(
        "key.idc.lumen.toggle",
        InputConstants.Type.KEYSYM,
        InputConstants.KEY_Z,
        CATEGORY
    );

    private static boolean enabled;

    private LumenFeature() {
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static boolean toggle() {
        enabled = !enabled;
        return enabled;
    }

    public static void reset() {
        enabled = false;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options != null) {
            LumenBrightness.restore(minecraft);
        }
    }

    public static void onClientTick(Minecraft minecraft) {
        if (KEY.consumeClick()) {
            boolean toggled = toggle();
            if (toggled) {
                LumenBrightness.ensureActive(minecraft);
            } else {
                LumenBrightness.restore(minecraft);
            }
            LumenFeedback.show(minecraft, toggled);
            return;
        }
        if (enabled) {
            LumenBrightness.ensureActive(minecraft);
        }
    }
}
