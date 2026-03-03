package com.gtnewhorizons.aspectrecipeindex.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class DrawUtils {

    public static void drawXYCenteredString(String text, int centerX, int centerY, int color, boolean shadow) {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        int stringWidth = fontRenderer.getStringWidth(text);
        fontRenderer.drawString(text, centerX - stringWidth / 2, centerY - fontRenderer.FONT_HEIGHT / 2, color, shadow);
    }
}
