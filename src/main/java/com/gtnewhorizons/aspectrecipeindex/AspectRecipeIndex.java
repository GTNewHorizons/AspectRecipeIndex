package com.gtnewhorizons.aspectrecipeindex;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLConstructionEvent;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import com.gtnewhorizons.aspectrecipeindex.proxy.CommonProxy;

@Mod(
        modid = AspectRecipeIndex.MODID,
        name = AspectRecipeIndex.NAME,
        version = AspectRecipeIndex.VERSION,
        dependencies = AspectRecipeIndex.DEPENDENCIES,
        guiFactory = AspectRecipeIndex.GUI_FACTORY,
        acceptableRemoteVersions = "*",
        acceptedMinecraftVersions = "[1.7.10]")
public class AspectRecipeIndex {

    public static final String MODID = "aspectrecipeindex";
    public static final String NAME = "Aspect Recipe Index";
    public static final String VERSION = Tags.VERSION;
    public static final String DEPENDENCIES = "required-after:Thaumcraft;required-after:thaumcraftneiplugin;required-after:gtnhmixins;after:Automagy";
    public static final String GUI_FACTORY = "com.gtnewhorizons.aspectrecipeindex.client.gui.GuiFactory";

    public static final Logger LOGGER = LogManager.getLogger(NAME);

    @Mod.Instance(value = AspectRecipeIndex.MODID)
    public static AspectRecipeIndex instance;

    @SidedProxy(
            clientSide = "com.gtnewhorizons.aspectrecipeindex.proxy.ClientProxy",
            serverSide = "com.gtnewhorizons.aspectrecipeindex.proxy.ServerProxy")
    public static CommonProxy proxy;

    public static String thaumcraftNEIPluginVersion;

    @Mod.EventHandler
    public void construct(FMLConstructionEvent event) {
        Loader.instance().getModList().stream().filter(mod -> "thaumcraftneiplugin".equals(mod.getModId())).findAny()
                .ifPresent(mod -> {
                    thaumcraftNEIPluginVersion = mod.getMetadata().version;
                    try {
                        // replace @VERSION@ with actual mod version
                        FieldUtils.writeField(mod, "internalVersion", thaumcraftNEIPluginVersion, true);
                    } catch (Exception e) {
                        LOGGER.warn("Failed to set internal version of Thaumcraft NEI Plugin!", e);
                    }
                });
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }
}
