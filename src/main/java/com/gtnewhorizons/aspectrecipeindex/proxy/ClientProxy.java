package com.gtnewhorizons.aspectrecipeindex.proxy;

import net.minecraftforge.client.MinecraftForgeClient;

import com.gtnewhorizon.gtnhlib.config.ConfigException;
import com.gtnewhorizon.gtnhlib.config.ConfigurationManager;
import com.gtnewhorizons.aspectrecipeindex.ModItems;
import com.gtnewhorizons.aspectrecipeindex.client.ARIClient;
import com.gtnewhorizons.aspectrecipeindex.client.render.ItemAspectRenderer;
import com.gtnewhorizons.aspectrecipeindex.nei.IMCForNEI;
import com.gtnewhorizons.aspectrecipeindex.util.ARIConfig;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@SuppressWarnings("unused")
public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        FMLCommonHandler.instance().bus().register(ARIClient.getInstance());
        ARIClient.getInstance().registerResourceReloadListener();
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
}
