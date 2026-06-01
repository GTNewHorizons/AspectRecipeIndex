package com.gtnewhorizons.aspectrecipeindex.proxy;

import net.minecraftforge.client.MinecraftForgeClient;

import com.gtnewhorizon.gtnhlib.config.ConfigException;
import com.gtnewhorizon.gtnhlib.config.ConfigurationManager;
import com.gtnewhorizons.aspectrecipeindex.ModItems;
import com.gtnewhorizons.aspectrecipeindex.client.render.ItemAspectRenderer;
import com.gtnewhorizons.aspectrecipeindex.nei.IMCForNEI;
import com.gtnewhorizons.aspectrecipeindex.util.ARIConfig;

import cpw.mods.fml.common.event.FMLInitializationEvent;

@SuppressWarnings("unused")
public class ClientProxy extends CommonProxy {

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
