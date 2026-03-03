package com.gtnewhorizons.aspectrecipeindex.proxy;

import com.gtnewhorizons.aspectrecipeindex.client.ARIClient;
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
        ARIConfig.init(event.getSuggestedConfigurationFile());
        FMLCommonHandler.instance().bus().register(ARIClient.getInstance());
        ARIClient.getInstance().registerResourceReloadListener();
        super.preInit(event);
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        IMCForNEI.IMCSender();
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
    }
}
