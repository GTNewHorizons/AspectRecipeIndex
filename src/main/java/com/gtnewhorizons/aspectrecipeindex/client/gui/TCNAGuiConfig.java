package com.gtnewhorizons.aspectrecipeindex.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;

import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.IConfigElement;
import com.gtnewhorizons.aspectrecipeindex.AspectRecipeIndex;
import com.gtnewhorizons.aspectrecipeindex.util.TCNAConfig;

public class TCNAGuiConfig extends GuiConfig {

    public TCNAGuiConfig(GuiScreen parentScreen) {
        super(
                parentScreen,
                getConfigElements(),
                AspectRecipeIndex.MODID,
                false,
                false,
                GuiConfig.getAbridgedConfigPath(TCNAConfig.config.toString()));
    }

    @SuppressWarnings("rawtypes")
    private static List<IConfigElement> getConfigElements() {
        List<IConfigElement> list = new ArrayList<>();

        for (String category : TCNAConfig.CATEGORIES) {
            list.add(new ConfigElement(TCNAConfig.config.getCategory(category.toLowerCase(Locale.US))));
        }

        return list;
    }
}
