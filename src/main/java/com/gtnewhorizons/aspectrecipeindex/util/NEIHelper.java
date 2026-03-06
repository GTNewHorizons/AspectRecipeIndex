package com.gtnewhorizons.aspectrecipeindex.util;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.wands.WandCap;
import thaumcraft.api.wands.WandRod;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.items.wands.ItemWandCasting;

public class NEIHelper {

    public static AspectList getWandAspectsWandCost(ItemStack item) {
        AspectList costs = new AspectList();
        if (!(item.getItem() instanceof ItemWandCasting wand)) return costs;
        int cost = wand.getRod(item).getCraftCost() * wand.getCap(item).getCraftCost();
        if (wand.isSceptre(item)) cost = cost * 3 / 2; // *= 1.5
        for (Aspect aspect : Aspect.getPrimalAspects()) {
            costs.add(aspect, cost);
        }
        return costs;
    }

    // TODO Figure out what this is supposed to do and if it's still needed
    public static ItemStack getAssociatedItemStack(Object o) {
        return new ItemStack(Blocks.stone);
    }

    public static ItemStack[] buildScepterInput(WandRod rod, WandCap cap) {
        return new ItemStack[] { null, cap.getItem(), new ItemStack(ConfigItems.itemResource, 1, 15), null,
                rod.getItem(), cap.getItem(), cap.getItem(), null, null };
    }

    public static ItemStack[] buildWandInput(WandRod rod, WandCap cap) {
        return new ItemStack[] { null, null, cap.getItem(), null, rod.getItem(), null, cap.getItem(), null, null };
    }
}
