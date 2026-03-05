package com.gtnewhorizons.aspectrecipeindex.nei.arcaneworkbench;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import com.gtnewhorizons.aspectrecipeindex.common.items.ItemAspect;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.DefaultOverlayHandler;
import codechicken.nei.recipe.IRecipeHandler;

public class ArcaneOverlayHandler extends DefaultOverlayHandler {

    public ArcaneOverlayHandler() {
        super();
        this.offsetx = 15;
        this.offsety = 15;
    }

    @Override
    protected Set<Slot> getCraftMatrixSlots(GuiContainer gui, IRecipeHandler handler) {
        final Set<Slot> ingredSlots = new HashSet<>();
        for (int i = 2; i < 11; i++) {
            ingredSlots.add(gui.inventorySlots.getSlot(i));
        }
        return ingredSlots;
    }

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

    public DistributedIngred findIngred(List<DistributedIngred> ingredStacks, ItemStack pstack) {
        return pstack.getItem() instanceof ItemAspect ? null : super.findIngred(ingredStacks, pstack);
    }
}
