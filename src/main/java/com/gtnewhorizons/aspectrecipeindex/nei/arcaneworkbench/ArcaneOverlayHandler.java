package com.gtnewhorizons.aspectrecipeindex.nei.arcaneworkbench;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;

import com.gtnewhorizons.aspectrecipeindex.common.items.ItemAspect;

import codechicken.nei.FastTransferManager;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.DefaultOverlayHandler;
import codechicken.nei.recipe.IRecipeHandler;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.items.wands.ItemWandCasting;

public class ArcaneOverlayHandler extends DefaultOverlayHandler {

    @Override
    protected Set<Slot> getCraftMatrixSlots(GuiContainer gui, IRecipeHandler handler) {
        final Set<Slot> ingredSlots = new HashSet<>();
        for (int i = 2; i < 11; i++) {
            ingredSlots.add(gui.inventorySlots.getSlot(i));
        }
        return ingredSlots;
    }

    @Override
    public Slot[][] mapIngredSlots(GuiContainer gui, List<PositionedStack> ingredients) {
        Slot[][] recipeSlotList = new Slot[ingredients.size()][];

        for (int i = 0; i < ingredients.size(); i++) {
            PositionedStack stack = ingredients.get(i);

            int slotIndex = ArcaneSlotPositioner.getSlotIndex(stack);

            Slot slot = gui.inventorySlots.inventorySlots.get(2 + slotIndex);
            recipeSlotList[i] = new Slot[] { slot };
        }

        return recipeSlotList;
    }

    @Override
    public int transferRecipe(GuiContainer gui, IRecipeHandler handler, int recipeIndex, int multiplier) {
        Slot slot = gui.inventorySlots.inventorySlots.get(1);

        AspectList cost = getVisCost(handler, recipeIndex);
        if (!isValidWand(slot, cost)) {
            tryReplaceWand(gui, slot, cost);
        }
        return super.transferRecipe(gui, handler, recipeIndex, multiplier);
    }

    private static AspectList getVisCost(IRecipeHandler handler, int recipeIndex) {
        List<PositionedStack> otherStacks = handler.getOtherStacks(recipeIndex);
        AspectList aspects = new AspectList();
        for (PositionedStack stack : otherStacks) {
            if (stack.item.getItem() instanceof ItemAspect)
                aspects.add(ItemAspect.getAspect(stack.item), stack.item.stackSize * 100);
        }
        return aspects;
    }

    private void tryReplaceWand(GuiContainer gui, Slot slot, AspectList vis) {
        for (int i = 11; i < gui.inventorySlots.inventorySlots.size(); i++) {
            Slot s = gui.inventorySlots.inventorySlots.get(i);
            if (isValidWand(s, vis)) {
                FastTransferManager.clickSlot(gui, slot.slotNumber, 0, 1);
                if (slot.getHasStack()) return;
                FastTransferManager.clickSlot(gui, i, 0, 1);
                return;
            }
        }
    }

    private boolean isValidWand(Slot slot, AspectList vis) {
        return slot.getHasStack() && slot.getStack().getItem() instanceof ItemWandCasting wand
                && wand.consumeAllVis(slot.getStack(), Minecraft.getMinecraft().thePlayer, vis, false, true);
    }
}
