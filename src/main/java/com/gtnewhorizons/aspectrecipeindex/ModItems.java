package com.gtnewhorizons.aspectrecipeindex;

import net.minecraft.item.Item;

import com.gtnewhorizons.aspectrecipeindex.common.items.ItemAspect;

import cpw.mods.fml.common.registry.GameRegistry;

public class ModItems {

    public static Item itemAspect;

    public static void init() {
        itemAspect = new ItemAspect();
        GameRegistry.registerItem(itemAspect, "aspect");
    }
}
