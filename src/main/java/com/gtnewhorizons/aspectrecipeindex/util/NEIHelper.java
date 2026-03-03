package com.gtnewhorizons.aspectrecipeindex.util;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.wands.WandCap;
import thaumcraft.api.wands.WandRod;

// TODO this whole class
public class NEIHelper {

    public static AspectList getWandAspectsWandCost(ItemStack result) {
        return new AspectList();
    }

    public static ItemStack getAssociatedItemStack(Object o) {
        return new ItemStack(Blocks.stone);
    }

    public static Object[] buildScepterInput(WandRod rod, WandCap cap) {
        return new Object[] {};
    }

    public static Object[] buildWandInput(WandRod rod, WandCap cap) {
        return new Object[] {};
    }

    public static AspectList getPrimalAspectListFromAmounts(AspectList wandAspectsWandCost) {
        return new AspectList();
    }
}
