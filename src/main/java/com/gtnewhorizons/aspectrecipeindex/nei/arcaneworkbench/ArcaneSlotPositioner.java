package com.gtnewhorizons.aspectrecipeindex.nei.arcaneworkbench;

import java.util.ArrayList;

import codechicken.nei.PositionedStack;
import codechicken.nei.api.IStackPositioner;

public class ArcaneSlotPositioner implements IStackPositioner {

    @Override
    public ArrayList<PositionedStack> positionStacks(final ArrayList<PositionedStack> stacks) {
        for (PositionedStack stack : stacks) {
            if (stack != null) {
                int i = getSlotIndex(stack);
                stack.relx = 40 + i % 3 * 24;
                stack.rely = 40 + i / 3 * 24;
            }
        }
        return stacks;
    }

    public static int getSlotIndex(PositionedStack stack) {
        int row, col;

        if ((stack.relx - ShapedArcaneRecipeHandler.XPOS[0]) % 28 == 0 // Arcane recipes
                && (stack.rely - ShapedArcaneRecipeHandler.YPOS[0]) % 27 == 0) {
            row = (stack.relx - ShapedArcaneRecipeHandler.XPOS[0]) / 28;
            col = (stack.rely - ShapedArcaneRecipeHandler.YPOS[0]) / 27;
        } else { // Vanilla recipes
            row = (stack.relx - 25) / 18;
            col = (stack.rely - 6) / 18;
        }

        return col * 3 + row;
    }
}
