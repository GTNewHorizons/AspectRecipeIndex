package com.gtnewhorizons.aspectrecipeindex.nei;

import net.minecraft.nbt.NBTTagCompound;

import com.gtnewhorizons.aspectrecipeindex.nei.arcaneworkbench.ShapedArcaneRecipeHandler;
import com.gtnewhorizons.aspectrecipeindex.nei.arcaneworkbench.ShapelessArcaneRecipeHandler;
import com.gtnewhorizons.aspectrecipeindex.nei.arcaneworkbench.WandRecipeHandler;

import cpw.mods.fml.common.event.FMLInterModComms;

public class IMCForNEI {

    public static void IMCSender() {
        // Arcane Crafting Table
        registerHandlerInfo(ShapedArcaneRecipeHandler.class.getName(), "Thaumcraft:blockTable:15", 138);
        registerHandlerInfo(WandRecipeHandler.class.getName(), "Thaumcraft:WandCasting", 138);
        registerHandlerInfo(ShapelessArcaneRecipeHandler.class.getName(), "Thaumcraft:blockTable:15", 138);
        // Crucible
        registerHandlerInfo(AlchemyRecipeHandler.class.getName(), "Thaumcraft:blockMetalDevice", 136);
        // Runic Matrix
        registerHandlerInfo(InfusionRecipeHandler.class.getName(), "Thaumcraft:blockStoneDevice:2", 152);
        registerHandlerInfo(ItemsContainingAspectHandler.class.getName(), "Thaumcraft:ItemResearchNotes", 147);
        registerHandlerInfo(AspectCombinationHandler.class.getName(), "Thaumcraft:blockTable:2", 43);

        registerCatalystInfo(new ShapedArcaneRecipeHandler().getOverlayIdentifier(), "Thaumcraft:blockTable:15");
        registerCatalystInfo(new WandRecipeHandler().getOverlayIdentifier(), "Thaumcraft:blockTable:15");
        registerCatalystInfo(new ShapelessArcaneRecipeHandler().getOverlayIdentifier(), "Thaumcraft:blockTable:15");
        // Crucible
        registerCatalystInfo(new AlchemyRecipeHandler().getOverlayIdentifier(), "Thaumcraft:blockMetalDevice");
        // TODO Fix item render for Thaumatorium
        // Thaumatorium
        // registerCatalystInfo(new AlchemyRecipeHandler().getOverlayIdentifier(), "Thaumcraft:blockMetalDevice:10");
        registerCatalystInfo(new InfusionRecipeHandler().getOverlayIdentifier(), "Thaumcraft:blockStoneDevice:2");
        registerCatalystInfo(new ItemsContainingAspectHandler().getOverlayIdentifier(), "Thaumcraft:ItemThaumometer");
        // Alchemical Furnace
        registerCatalystInfo(new ItemsContainingAspectHandler().getOverlayIdentifier(), "Thaumcraft:blockStoneDevice");
        // TODO Advanced Alchemical Furnace render item
        // registerCatalystInfo(new ItemsContainingAspectHandler().getOverlayIdentifier(),
        // "Thaumcraft:blockStoneDevice");
        // Research Table TODO add this block to NEI
        registerCatalystInfo(new AspectCombinationHandler().getOverlayIdentifier(), "Thaumcraft:blockTable:2");
    }

    private static void registerHandlerInfo(String name, String stack, int height) {
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

    private static void registerCatalystInfo(String handlerName, String stack) {
        NBTTagCompound aNBT = new NBTTagCompound();
        aNBT.setString("handlerID", handlerName);
        aNBT.setString("itemName", stack);
        aNBT.setInteger("priority", 0);
        FMLInterModComms.sendMessage("NotEnoughItems", "registerCatalystInfo", aNBT);
    }

}
