package com.gtnewhorizons.aspectrecipeindex.nei;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import com.gtnewhorizons.aspectrecipeindex.ModItems;
import com.gtnewhorizons.aspectrecipeindex.common.items.ItemAspect;

import codechicken.nei.api.IStackStringifyHandler;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

public class TCAspectStringifyHandler implements IStackStringifyHandler {

    @Override
    public NBTTagCompound convertItemStackToNBT(ItemStack stack, boolean saveStackSize) {
        if (!(stack.getItem() instanceof ItemAspect)) {
            return null;
        }

        AspectList aspectList = ItemAspect.getAspects(stack);
        if (aspectList == null) {
            return null;
        }
        Aspect aspect = aspectList.getAspects()[0];
        NBTTagCompound nbtTag = new NBTTagCompound();
        nbtTag.setString("TCAspect", aspect.getTag());
        nbtTag.setInteger("Count", Math.max(saveStackSize ? stack.stackSize : 1, 1));
        return nbtTag;
    }

    @Override
    public ItemStack convertNBTToItemStack(NBTTagCompound nbtTag) {
        if (!nbtTag.hasKey("TCAspect")) {
            return null;
        }

        Aspect aspect = Aspect.getAspect(nbtTag.getString("TCAspect"));
        int amount = nbtTag.getInteger("Count");
        ItemStack aspectStack = new ItemStack(ModItems.itemAspect, amount, 0);
        ItemAspect.setAspect(aspectStack, aspect);
        return aspectStack;
    }

    @Override
    public FluidStack getFluid(ItemStack stack) {
        return null;
    }
}
