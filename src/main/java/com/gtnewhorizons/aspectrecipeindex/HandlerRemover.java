package com.gtnewhorizons.aspectrecipeindex;

import java.util.ArrayList;
import java.util.Iterator;

import codechicken.nei.event.NEIConfigsLoadedEvent;
import codechicken.nei.recipe.GuiCraftingRecipe;
import codechicken.nei.recipe.GuiUsageRecipe;
import codechicken.nei.recipe.ICraftingHandler;
import codechicken.nei.recipe.IUsageHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class HandlerRemover {

    private static final ArrayList<Class<? extends ICraftingHandler>> recipeHandlersForRemoving = new ArrayList<>();
    private static final ArrayList<Class<? extends IUsageHandler>> usageHandlersForRemoving = new ArrayList<>();

    public static void delayRecipeHandlerRemoving(Class<? extends ICraftingHandler> handlerClass) {
        recipeHandlersForRemoving.add(handlerClass);
    }

    public static void delayUsageHandlerRemoving(Class<? extends IUsageHandler> handlerClass) {
        usageHandlersForRemoving.add(handlerClass);
    }

    @SubscribeEvent
    public void initiateRemoving(NEIConfigsLoadedEvent event) {
        AspectRecipeIndex.LOGGER.info("Initiated handler removing...");
        Iterator<ICraftingHandler> craftingIterator = GuiCraftingRecipe.craftinghandlers.iterator();
        while (craftingIterator.hasNext()) {
            ICraftingHandler next = craftingIterator.next();
            for (Class<? extends ICraftingHandler> craftingHandlerClass : recipeHandlersForRemoving) {
                if (next.getClass() == craftingHandlerClass) {
                    craftingIterator.remove();
                    AspectRecipeIndex.LOGGER.info(
                            "Crafting Recipes: found and removed standard " + craftingHandlerClass.getSimpleName()
                                    + " from Thaumcraft NEI Plugin");
                }
            }
        }

        Iterator<IUsageHandler> usageIterator = GuiUsageRecipe.usagehandlers.iterator();
        while (usageIterator.hasNext()) {
            IUsageHandler next = usageIterator.next();
            for (Class<? extends IUsageHandler> usageHandlerClass : usageHandlersForRemoving) {
                if (next.getClass() == usageHandlerClass) {
                    usageIterator.remove();
                    AspectRecipeIndex.LOGGER.info(
                            "Usage Recipes: found and removed standard " + usageHandlerClass.getSimpleName()
                                    + " from Thaumcraft NEI Plugin");
                }
            }
        }
    }
}
