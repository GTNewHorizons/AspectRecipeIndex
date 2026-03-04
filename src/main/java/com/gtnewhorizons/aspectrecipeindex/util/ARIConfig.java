package com.gtnewhorizons.aspectrecipeindex.util;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

import com.gtnewhorizons.aspectrecipeindex.AspectRecipeIndex;

public class ARIConfig {

    public static Configuration config;

    public static final String GENERAL = "general";

    public static final String[] CATEGORIES = new String[] { GENERAL };

    private static final String LANG_PREFIX = AspectRecipeIndex.MODID + ".config.";

    public static boolean showLockedRecipes;
    public static boolean showInstabilityNumber;
    public static boolean showResearchKey;
    public static boolean showUndiscoveredAspects;

    public static void init(File file) {
        config = new Configuration(file);
        syncConfig();
    }

    public static void syncConfig() {
        config.setCategoryComment(GENERAL, "General config");
        config.setCategoryLanguageKey(GENERAL, LANG_PREFIX + GENERAL);

        showLockedRecipes = config
                .get(GENERAL, "showLockedRecipes", false, "Show recipes even if the research is not completed")
                .setLanguageKey(LANG_PREFIX + GENERAL + ".showLockedRecipes").getBoolean();

        showInstabilityNumber = config
                .get(GENERAL, "showInstabilityNumber", true, "Show the number of instability of infusion")
                .setLanguageKey(LANG_PREFIX + GENERAL + ".showInstabilityNumber").getBoolean();

        showResearchKey = config.get(GENERAL, "showResearchKey", true, "Show research key")
                .setLanguageKey(LANG_PREFIX + GENERAL + ".showResearchKey").getBoolean();

        showUndiscoveredAspects = config
                .get(GENERAL, "showUndiscoveredAspects", false, "Show icons and names of undiscovered aspects in NEI")
                .setLanguageKey(LANG_PREFIX + GENERAL + ".showUndiscoveredAspects").getBoolean();

        if (config.hasChanged()) {
            config.save();
        }
    }
}
