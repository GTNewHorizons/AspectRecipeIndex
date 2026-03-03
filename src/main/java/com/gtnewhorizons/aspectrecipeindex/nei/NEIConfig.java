package com.gtnewhorizons.aspectrecipeindex.nei;

import com.djgiannuzz.thaumcraftneiplugin.nei.recipehandler.ArcaneShapedRecipeHandler;
import com.djgiannuzz.thaumcraftneiplugin.nei.recipehandler.ArcaneShapelessRecipeHandler;
import com.djgiannuzz.thaumcraftneiplugin.nei.recipehandler.AspectRecipeHandler;
import com.djgiannuzz.thaumcraftneiplugin.nei.recipehandler.CrucibleRecipeHandler;
import com.djgiannuzz.thaumcraftneiplugin.nei.recipehandler.InfusionRecipeHandler;

import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;
import com.gtnewhorizons.aspectrecipeindex.HandlerRemover;
import com.gtnewhorizons.aspectrecipeindex.AspectRecipeIndex;
import com.gtnewhorizons.aspectrecipeindex.nei.arcaneworkbench.ArcaneCraftingShapedHandler;
import com.gtnewhorizons.aspectrecipeindex.nei.arcaneworkbench.ArcaneCraftingShapelessHandler;

public class NEIConfig implements IConfigureNEI {

    @Override
    public void loadConfig() {
        HandlerRemover.delayRecipeHandlerRemoving(AspectRecipeHandler.class);
        HandlerRemover.delayRecipeHandlerRemoving(ArcaneShapedRecipeHandler.class);
        HandlerRemover.delayRecipeHandlerRemoving(ArcaneShapelessRecipeHandler.class);
        HandlerRemover.delayRecipeHandlerRemoving(CrucibleRecipeHandler.class);
        HandlerRemover.delayRecipeHandlerRemoving(InfusionRecipeHandler.class);

        HandlerRemover.delayUsageHandlerRemoving(AspectRecipeHandler.class);
        HandlerRemover.delayUsageHandlerRemoving(ArcaneShapedRecipeHandler.class);
        HandlerRemover.delayUsageHandlerRemoving(ArcaneShapelessRecipeHandler.class);
        HandlerRemover.delayUsageHandlerRemoving(CrucibleRecipeHandler.class);
        HandlerRemover.delayUsageHandlerRemoving(InfusionRecipeHandler.class);

        API.registerRecipeHandler(new AspectFromItemStackHandler());
        API.registerRecipeHandler(new AspectCombinationHandler());
        API.registerRecipeHandler(new ArcaneCraftingShapedHandler());
        API.registerRecipeHandler(new ArcaneCraftingShapelessHandler());
        API.registerRecipeHandler(new TCNACrucibleRecipeHandler());
        API.registerRecipeHandler(new TCNAInfusionRecipeHandler());

        API.registerUsageHandler(new AspectCombinationHandler());
        API.registerUsageHandler(new ArcaneCraftingShapedHandler());
        API.registerUsageHandler(new ArcaneCraftingShapelessHandler());
        API.registerUsageHandler(new TCNACrucibleRecipeHandler());
        API.registerUsageHandler(new TCNAInfusionRecipeHandler());

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
