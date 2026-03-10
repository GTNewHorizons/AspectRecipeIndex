package com.gtnewhorizons.aspectrecipeindex.nei;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.gtnewhorizons.aspectrecipeindex.ModItems;
import com.gtnewhorizons.aspectrecipeindex.common.items.ItemAspect;

import codechicken.nei.api.IStackStringifyHandler;
import thaumcraft.api.aspects.Aspect;

public class TCAspectStringifyHandler implements IStackStringifyHandler {

    @Override
    public NBTTagCompound convertItemStackToNBT(ItemStack stack, boolean saveStackSize) {
        if (!(stack.getItem() instanceof ItemAspect)) {
            return null;
        }

        Aspect aspect = ItemAspect.getAspect(stack);
        if (aspect == null) {
            return null;
        }
        NBTTagCompound nbtTag = new NBTTagCompound();
        nbtTag.setString("TCAspect", aspect.getTag());
        nbtTag.setInteger("Count", Math.max(saveStackSize ? stack.stackSize : 1, 1));
        return nbtTag;
    }

    @Override
    public ItemStack convertNBTToItemStack(NBTTagCompound nbtTag) {
        if (nbtTag == null || !nbtTag.hasKey("TCAspect")) {
            return null;
        }

        ItemStack aspectStack = new ItemStack(ModItems.itemAspect, nbtTag.getInteger("Count"));
        ItemAspect.setAspect(aspectStack, Aspect.getAspect(nbtTag.getString("TCAspect")));
        return aspectStack;
    }

}
