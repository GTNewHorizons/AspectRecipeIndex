package com.gtnewhorizons.aspectrecipeindex.proxy;

import net.minecraftforge.common.MinecraftForge;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import com.gtnewhorizons.aspectrecipeindex.HandlerRemover;
import com.gtnewhorizons.aspectrecipeindex.client.TCNAClient;
import com.gtnewhorizons.aspectrecipeindex.nei.IMCForNEI;
import com.gtnewhorizons.aspectrecipeindex.util.TCNAConfig;

@SuppressWarnings("unused")
public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        TCNAConfig.init(event.getSuggestedConfigurationFile());
        FMLCommonHandler.instance().bus().register(TCNAClient.getInstance());
        TCNAClient.getInstance().registerResourceReloadListener();
        MinecraftForge.EVENT_BUS.register(new HandlerRemover());
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
