package com.gtnewhorizons.aspectrecipeindex.util;

import com.gtnewhorizon.gtnhlib.config.Config;

@Config(modid = "aspectrecipeindex")
public class ARIConfig {

    @Config.Comment("Show recipes even if the research is not completed")
    @Config.DefaultBoolean(false)
    public static boolean showLockedRecipes;

    @Config.Comment("Show the instability of infusion recipes as a number")
    @Config.DefaultBoolean(true)
    public static boolean showInstabilityNumber;

    @Config.Comment("Show the required research for recipes on all handlers")
    @Config.DefaultBoolean(true)
    public static boolean showResearchKey;

    @Config.Comment("Show names of undiscovered aspects when hovered in NEI")
    @Config.DefaultBoolean(false)
    public static boolean showUndiscoveredAspectNames;

    @Config.Comment("Show combination recipes of undiscovered aspects in NEI")
    @Config.DefaultBoolean(false)
    public static boolean showUndiscoveredAspectRecipes;
}
