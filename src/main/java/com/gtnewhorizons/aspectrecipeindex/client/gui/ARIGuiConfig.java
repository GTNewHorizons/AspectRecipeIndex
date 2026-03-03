package com.gtnewhorizons.aspectrecipeindex.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;

import com.gtnewhorizons.aspectrecipeindex.AspectRecipeIndex;
import com.gtnewhorizons.aspectrecipeindex.util.ARIConfig;

import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.IConfigElement;

public class ARIGuiConfig extends GuiConfig {

    public ARIGuiConfig(GuiScreen parentScreen) {
        super(
                parentScreen,
                getConfigElements(),
                AspectRecipeIndex.MODID,
                false,
                false,
                GuiConfig.getAbridgedConfigPath(ARIConfig.config.toString()));
    }

    @SuppressWarnings("rawtypes")
    private static List<IConfigElement> getConfigElements() {
        List<IConfigElement> list = new ArrayList<>();

        for (String category : ARIConfig.CATEGORIES) {
            list.add(new ConfigElement(ARIConfig.config.getCategory(category.toLowerCase(Locale.US))));
        }

        return list;
    }
}
