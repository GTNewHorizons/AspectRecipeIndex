package com.gtnewhorizons.aspectrecipeindex.client.research;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import org.lwjgl.input.Keyboard;

import com.gtnewhorizons.aspectrecipeindex.util.ARIConfig;

import codechicken.nei.KeyManager;
import codechicken.nei.NEIClientUtils;

public class ResearchIdDisplay {

    public static String copyKeyName() {
        String name = KeyManager.getKeyName("copy.identifier", NEIClientUtils.CTRL_HASH);
        return name == null ? "" : name;
    }

    public static List<String> lines(String id) {
        if (!ARIConfig.showResearchId || id == null || id.isEmpty()) return List.of();
        var lines = new ArrayList<String>(2);
        lines.add(
                EnumChatFormatting.DARK_GRAY
                        + StatCollector.translateToLocalFormatted("aspectrecipeindex.research.id", id));
        String key = copyKeyName();
        if (!key.isEmpty()) lines.add(
                EnumChatFormatting.GRAY
                        + StatCollector.translateToLocalFormatted("aspectrecipeindex.research.copy_id", key));
        return lines;
    }

    public static boolean copy(String id) {
        if (id == null || id.isEmpty() || !KeyManager.isHashDown("copy.identifier", NEIClientUtils.CTRL_HASH))
            return false;
        GuiScreen.setClipboardString(id);
        return true;
    }

    public static boolean copyKey(String id, int keyCode) {
        return !Keyboard.isRepeatEvent() && keyCode == KeyManager.getKeyCode("copy.identifier") && copy(id);
    }

    public static boolean copyMouse(String id, int button) {
        return button - 100 == KeyManager.getKeyCode("copy.identifier")
                && KeyManager.handleMouseHotkey(button, () -> copy(id));
    }
}
