package com.gtnewhorizons.aspectrecipeindex.nei;

import com.gtnewhorizons.aspectrecipeindex.AspectRecipeIndex;
import com.gtnewhorizons.aspectrecipeindex.nei.arcaneworkbench.ArcaneCraftingShapedHandler;
import com.gtnewhorizons.aspectrecipeindex.nei.arcaneworkbench.ArcaneCraftingShapelessHandler;

import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;

public class NEIConfig implements IConfigureNEI {

    @Override
    public void loadConfig() {

        API.registerRecipeHandler(new AspectFromItemStackHandler());
        API.registerRecipeHandler(new AspectCombinationHandler());
        API.registerRecipeHandler(new ArcaneCraftingShapedHandler());
        API.registerRecipeHandler(new ArcaneCraftingShapelessHandler());
        API.registerRecipeHandler(new AlchemyRecipeHandler());
        API.registerRecipeHandler(new InfusionRecipeHandler());

        API.registerUsageHandler(new AspectCombinationHandler());
        API.registerUsageHandler(new ArcaneCraftingShapedHandler());
        API.registerUsageHandler(new ArcaneCraftingShapelessHandler());
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
