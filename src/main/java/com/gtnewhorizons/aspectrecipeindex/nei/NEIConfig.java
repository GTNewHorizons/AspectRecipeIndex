package com.gtnewhorizons.aspectrecipeindex.nei;

import com.gtnewhorizons.aspectrecipeindex.AspectRecipeIndex;
import com.gtnewhorizons.aspectrecipeindex.nei.arcaneworkbench.ShapedArcaneRecipeHandler;
import com.gtnewhorizons.aspectrecipeindex.nei.arcaneworkbench.ShapelessArcaneRecipeHandler;

import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;

public class NEIConfig implements IConfigureNEI {

    @Override
    public void loadConfig() {

        API.registerRecipeHandler(new ItemsContainingAspectHandler());
        API.registerRecipeHandler(new AspectCombinationHandler());
        API.registerRecipeHandler(new ShapedArcaneRecipeHandler());
        API.registerRecipeHandler(new ShapelessArcaneRecipeHandler());
        API.registerRecipeHandler(new AlchemyRecipeHandler());
        API.registerRecipeHandler(new InfusionRecipeHandler());

        API.registerUsageHandler(new ItemsContainingAspectHandler());
        API.registerUsageHandler(new AspectCombinationHandler());
        API.registerUsageHandler(new ShapedArcaneRecipeHandler());
        API.registerUsageHandler(new ShapelessArcaneRecipeHandler());
        API.registerUsageHandler(new AlchemyRecipeHandler());
        API.registerUsageHandler(new InfusionRecipeHandler());

        try {
            API.registerStackStringifyHandler(new TCAspectStringifyHandler());
        } catch (NoSuchMethodError ignored) {}
    }

    @Override
    public String getName() {
        return AspectRecipeIndex.NAME;
    }

    @Override
    public String getVersion() {
        return AspectRecipeIndex.VERSION;
    }
}
