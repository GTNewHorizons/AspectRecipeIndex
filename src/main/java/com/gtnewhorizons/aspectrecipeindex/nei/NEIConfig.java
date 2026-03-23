package com.gtnewhorizons.aspectrecipeindex.nei;

import com.gtnewhorizons.aspectrecipeindex.AspectRecipeIndex;
import com.gtnewhorizons.aspectrecipeindex.nei.arcaneworkbench.ArcaneOverlayHandler;
import com.gtnewhorizons.aspectrecipeindex.nei.arcaneworkbench.ArcaneSlotPositioner;
import com.gtnewhorizons.aspectrecipeindex.nei.arcaneworkbench.ShapedArcaneRecipeHandler;
import com.gtnewhorizons.aspectrecipeindex.nei.arcaneworkbench.ShapelessArcaneRecipeHandler;
import com.gtnewhorizons.aspectrecipeindex.nei.arcaneworkbench.WandRecipeHandler;

import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;
import thaumcraft.client.gui.GuiArcaneWorkbench;

public class NEIConfig implements IConfigureNEI {

    @Override
    public void loadConfig() {

        API.registerRecipeHandler(new ItemsContainingAspectHandler());
        API.registerRecipeHandler(new AspectCombinationHandler());
        API.registerRecipeHandler(new ShapedArcaneRecipeHandler());
        API.registerRecipeHandler(new WandRecipeHandler());
        API.registerRecipeHandler(new ShapelessArcaneRecipeHandler());
        API.registerRecipeHandler(new AlchemyRecipeHandler());
        API.registerRecipeHandler(new InfusionRecipeHandler());

        API.registerUsageHandler(new ItemsContainingAspectHandler());
        API.registerUsageHandler(new AspectCombinationHandler());
        API.registerUsageHandler(new ShapedArcaneRecipeHandler());
        API.registerUsageHandler(new WandRecipeHandler());
        API.registerUsageHandler(new ShapelessArcaneRecipeHandler());
        API.registerUsageHandler(new AlchemyRecipeHandler());
        API.registerUsageHandler(new InfusionRecipeHandler());

        ArcaneSlotPositioner positioner = new ArcaneSlotPositioner();
        API.registerGuiOverlay(GuiArcaneWorkbench.class, ShapedArcaneRecipeHandler.OVERLAY, positioner);
        API.registerGuiOverlay(GuiArcaneWorkbench.class, ShapelessArcaneRecipeHandler.OVERLAY, positioner);
        API.registerGuiOverlay(GuiArcaneWorkbench.class, WandRecipeHandler.OVERLAY, positioner);
        API.registerGuiOverlay(GuiArcaneWorkbench.class, "crafting", positioner);

        ArcaneOverlayHandler handler = new ArcaneOverlayHandler();
        API.registerGuiOverlayHandler(GuiArcaneWorkbench.class, handler, ShapedArcaneRecipeHandler.OVERLAY);
        API.registerGuiOverlayHandler(GuiArcaneWorkbench.class, handler, ShapelessArcaneRecipeHandler.OVERLAY);
        API.registerGuiOverlayHandler(GuiArcaneWorkbench.class, handler, WandRecipeHandler.OVERLAY);
        API.registerGuiOverlayHandler(GuiArcaneWorkbench.class, handler, "crafting");
        try {
            API.registerStackStringifyHandler(new TCAspectStringifyHandler());
        } catch (NoSuchMethodError ignored) {}

        InfusionRecipeHandler.init();
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
