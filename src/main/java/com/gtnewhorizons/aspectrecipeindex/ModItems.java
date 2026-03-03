package com.gtnewhorizons.aspectrecipeindex;

import net.minecraft.item.Item;

import com.gtnewhorizons.aspectrecipeindex.common.items.ItemAspect;

public class ModItems {

    public static Item itemAspect;

    public static void init() {
        itemAspect = new ItemAspect();
    }
}
