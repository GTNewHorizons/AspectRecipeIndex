package com.gtnewhorizons.aspectrecipeindex.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraftforge.client.MinecraftForgeClient;

import com.gtnewhorizon.gtnhlib.config.ConfigException;
import com.gtnewhorizon.gtnhlib.config.ConfigurationManager;
import com.gtnewhorizons.aspectrecipeindex.AspectRecipeIndex;
import com.gtnewhorizons.aspectrecipeindex.ModItems;
import com.gtnewhorizons.aspectrecipeindex.client.render.ItemAspectRenderer;
import com.gtnewhorizons.aspectrecipeindex.nei.IMCForNEI;
import com.gtnewhorizons.aspectrecipeindex.nei.arcaneworkbench.WandRecipeHandler;
import com.gtnewhorizons.aspectrecipeindex.util.ARIConfig;
import com.gtnewhorizons.aspectrecipeindex.util.Util;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.common.lib.crafting.ArcaneSceptreRecipe;
import thaumcraft.common.lib.crafting.ArcaneWandRecipe;

@SuppressWarnings("unused")
public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        FMLCommonHandler.instance().bus().register(new EventHandler());
        if (Minecraft.getMinecraft().getResourceManager() instanceof IReloadableResourceManager manager) {
            manager.registerReloadListener((IResourceManager m) -> Util.updateColorOverride());
        }
        super.preInit(event);
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        IMCForNEI.IMCSender();
        MinecraftForgeClient.registerItemRenderer(ModItems.itemAspect, new ItemAspectRenderer());
        try {
            ConfigurationManager.registerConfig(ARIConfig.class);
        } catch (ConfigException e) {
            throw new RuntimeException(e);
        }
    }

    public static class EventHandler {

        @SubscribeEvent
        public void onPlayerEntered(FMLNetworkEvent.ClientConnectedToServerEvent event) {
            for (Object craftingRecipe : ThaumcraftApi.getCraftingRecipes()) {
                if (craftingRecipe instanceof ArcaneWandRecipe || craftingRecipe instanceof ArcaneSceptreRecipe) {
                    return;
                }
            }
            WandRecipeHandler.wandRecipesDeleted = true;

            AspectRecipeIndex.LOGGER.info(
                    "Another mod has removed ArcaneWandRecipe and ArcaneSceptreRecipe. WandRecipeHandler will now find recipes for every ItemWandCasting made in a ShapedArcaneRecipe.");
        }
    }
}
