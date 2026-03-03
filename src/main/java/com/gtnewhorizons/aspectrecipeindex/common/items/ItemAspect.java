package com.gtnewhorizons.aspectrecipeindex.common.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

// TODO this whole class
public class ItemAspect extends Item {

    public ItemAspect() {
        super();
        this.setUnlocalizedName("aspect");
    }

    public static void setAspect(ItemStack stack, Aspect aspect) {
    }

    public static AspectList getAspects(ItemStack ingredient) {
        return new AspectList();
    }
}
