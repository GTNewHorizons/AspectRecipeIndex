package com.gtnewhorizons.aspectrecipeindex.nei;

import net.minecraft.nbt.NBTTagCompound;

import cpw.mods.fml.common.event.FMLInterModComms;

public class IMCForNEI {

    public static void IMCSender() {
        setNBTAndSend(
                "com.gtnewhorizons.aspectrecipeindex.nei.arcaneworkbench.ArcaneCraftingShapedHandler",
                "Thaumcraft:blockTable:15",
                138);
        setNBTAndSend(
                "com.gtnewhorizons.aspectrecipeindex.nei.arcaneworkbench.ArcaneCraftingShapelessHandler",
                "Thaumcraft:blockTable:15",
                138);
        setNBTAndSend(
                "com.gtnewhorizons.aspectrecipeindex.nei.AlchemyRecipeHandler",
                "Thaumcraft:blockMetalDevice",
                136);
        setNBTAndSend(
                "com.gtnewhorizons.aspectrecipeindex.nei.InfusionRecipeHandler",
                "Thaumcraft:blockStoneDevice:2",
                152);
        setNBTAndSend(
                "com.gtnewhorizons.aspectrecipeindex.nei.AspectFromItemStackHandler",
                "Thaumcraft:ItemResearchNotes",
                147);
        setNBTAndSend(
                "com.gtnewhorizons.aspectrecipeindex.nei.AspectCombinationHandler",
                "Thaumcraft:ItemResearchNotes",
                43);
    }

    private static void setNBTAndSend(String name, String stack, int height) {
        NBTTagCompound NBT = new NBTTagCompound();
        NBT.setString("handler", name);
        NBT.setString("modName", "Thaumcraft");
        NBT.setString("modId", "Thaumcraft");
        NBT.setBoolean("modRequired", true);
        NBT.setString("itemName", stack);
        NBT.setInteger("handlerHeight", height);
        NBT.setInteger("maxRecipesPerPage", 2);
        FMLInterModComms.sendMessage("NotEnoughItems", "registerHandlerInfo", NBT);
    }

}
