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
    public static boolean showUndiscoveredAspectNames;
    public static boolean showUndiscoveredAspectRecipes;

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
                .get(GENERAL, "showInstabilityNumber", true, "Show the number of instability for infusion recipes")
                .setLanguageKey(LANG_PREFIX + GENERAL + ".showInstabilityNumber").getBoolean();

        showResearchKey = config.get(GENERAL, "showResearchKey", true, "Show research key")
                .setLanguageKey(LANG_PREFIX + GENERAL + ".showResearchKey").getBoolean();

        showUndiscoveredAspectNames = config
                .get(
                        GENERAL,
                        "showUndiscoveredAspectNames",
                        false,
                        "Show names of undiscovered aspects when hovered in NEI")
                .setLanguageKey(LANG_PREFIX + GENERAL + ".showUndiscoveredAspectNames").getBoolean();

        showUndiscoveredAspectRecipes = config
                .get(
                        GENERAL,
                        "showUndiscoveredAspectRecipes",
                        false,
                        "Show combination recipes of undiscovered aspects in NEI")
                .setLanguageKey(LANG_PREFIX + GENERAL + ".showUndiscoveredAspectRecipes").getBoolean();

        if (config.hasChanged()) {
            config.save();
        }
    }
}
