package com.gtnewhorizons.aspectrecipeindex;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.gtnewhorizons.aspectrecipeindex.proxy.CommonProxy;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLConstructionEvent;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLMissingMappingsEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(
        modid = AspectRecipeIndex.MODID,
        name = AspectRecipeIndex.NAME,
        version = AspectRecipeIndex.VERSION,
        dependencies = AspectRecipeIndex.DEPENDENCIES,
        acceptableRemoteVersions = "*",
        acceptedMinecraftVersions = "[1.7.10]")
public class AspectRecipeIndex {

    public static final String MODID = "aspectrecipeindex";
    public static final String NAME = "Aspect Recipe Index";
    public static final String VERSION = Tags.VERSION;
    public static final String DEPENDENCIES = "required-after:Thaumcraft;" + "required-after:gtnhmixins;"
            + "required-after:gtnhlib;"
            + "required-after:TC4Tweaks|API|InfusionRecipeLib;"
            + "after:Automagy";

    public static final Logger LOGGER = LogManager.getLogger(NAME);

    @Mod.Instance(value = AspectRecipeIndex.MODID)
    public static AspectRecipeIndex instance;

    @SidedProxy(
            clientSide = "com.gtnewhorizons.aspectrecipeindex.proxy.ClientProxy",
            serverSide = "com.gtnewhorizons.aspectrecipeindex.proxy.ServerProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void construct(FMLConstructionEvent event) {}

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
        ModItems.init();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @Mod.EventHandler
    public static void onMissingMappings(FMLMissingMappingsEvent event) {
        for (FMLMissingMappingsEvent.MissingMapping mapping : event.getAll()) {
            if (mapping.type == GameRegistry.Type.ITEM && mapping.name.equals("thaumcraftneiplugin:Aspect")) {
                mapping.ignore();
            }
        }
    }
}
