package com.gtnewhorizons.aspectrecipeindex.nei;

import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.opengl.GL11;

import com.gtnewhorizons.aspectrecipeindex.ModItems;
import com.gtnewhorizons.aspectrecipeindex.common.items.ItemAspect;
import com.gtnewhorizons.aspectrecipeindex.util.Util;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.PositionedStack;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.research.ResearchCategories;

public class AspectCombinationHandler extends TemplateThaumHandler {

    public static final String OVERLAY = "thaumcraft.aspects";

    @Override
    public String getRecipeName() {
        return StatCollector.translateToLocal("aspectrecipeindex.aspect_combination.title");
    }

    @Override
    public String getOverlayIdentifier() {
        return OVERLAY;
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        if (result.getItem() instanceof ItemAspect && Util.shouldShowAspect(ItemAspect.getAspect(result))) {
            new AspectCombinationRecipe(result);
        }
    }

    @Override
    public void loadCraftingRecipes(String inputId, Object... results) {
        if (inputId.equals("item")) loadCraftingRecipes((ItemStack) results[0]);
        if (!inputId.equals(this.getOverlayIdentifier())) return;
        for (Aspect aspect : Aspect.aspects.values()) {
            if (!Util.shouldShowAspect(aspect)) continue;
            ItemStack result = new ItemStack(ModItems.itemAspect);
            ItemAspect.setAspect(result, aspect);
            new AspectCombinationRecipe(result);
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        if (!(ingredient.getItem() instanceof ItemAspect)) {
            return;
        }
        Aspect aspect = ItemAspect.getAspect(ingredient);

        if (!Util.shouldShowAspect(aspect)) {
            return;
        }
        for (Aspect compoundAspect : Aspect.getCompoundAspects()) {
            if (ArrayUtils.contains(compoundAspect.getComponents(), aspect) && Util.shouldShowAspect(compoundAspect)) {
                ItemStack result = new ItemStack(ModItems.itemAspect);
                ItemAspect.setAspect(result, compoundAspect);
                new AspectCombinationRecipe(result);
            }
        }
    }

    @Override
    public void loadUsageRecipes(String inputId, Object... ingredients) {
        if (inputId.equals("item")) loadUsageRecipes((ItemStack) ingredients[0]);
        else if (inputId.equals(this.getOverlayIdentifier())) loadCraftingRecipes(inputId, ingredients);
    }

    @Override
    public void drawBackground(int recipe) {
        AspectCombinationRecipe cachedRecipe = (AspectCombinationRecipe) arecipes.get(recipe);
        if (cachedRecipe.getIngredients().isEmpty()) {
            super.drawBackground(recipe);
            GuiDraw.drawStringC(
                    StatCollector.translateToLocal("tc.aspect.primal"),
                    84,
                    25,
                    Util.getColor("aspectrecipeindex.gui.textColor"),
                    false);
        } else {
            GL11.glTranslatef(-33F, 8, 0F);
            super.drawBackground(recipe);
            GL11.glTranslatef(33F, -8F, 0F);
            GuiDraw.drawStringC("=", 68, 17, Util.getColor("aspectrecipeindex.gui.textColor"), false);
            GuiDraw.drawStringC("+", 100, 17, Util.getColor("aspectrecipeindex.gui.textColor"), false);
        }
    }

    @Override
    public void drawForeground(int recipe) {
        drawExtras(recipe);
    }

    @Override
    public void drawExtras(int recipe) {
        // Only show these for the first result to limit clutter
        if (recipe == 0) {
            super.drawExtras(recipe);
        }
    }

    protected class AspectCombinationRecipe extends CachedThaumRecipe {

        public AspectCombinationRecipe(ItemStack aspectStack) {
            super(true);
            arecipes.add(this);
            prereqs.add(new ResearchInfo(ResearchCategories.getResearch("ASPECTS"), true));

            Aspect aspect = ItemAspect.getAspect(aspectStack);
            aspectStack = new ItemStack(ModItems.itemAspect);
            ItemAspect.setAspect(aspectStack, aspect);

            if (aspect.isPrimal()) {
                this.result = new PositionedStack(aspectStack, TemplateThaumHandler.OUTPUT_X, 5);
            } else {
                int spaceX = 33;

                this.result = new PositionedStack(aspectStack, TemplateThaumHandler.OUTPUT_X - spaceX, 13);

                Aspect[] components = aspect.getComponents();

                ItemStack firstIngred = new ItemStack(ModItems.itemAspect);
                ItemAspect.setAspect(firstIngred, components[0]);
                ItemStack secondIngred = new ItemStack(ModItems.itemAspect);
                ItemAspect.setAspect(secondIngred, components[1]);

                ingredients.add(new PositionedStack(firstIngred, TemplateThaumHandler.OUTPUT_X, 13));
                ingredients.add(new PositionedStack(secondIngred, TemplateThaumHandler.OUTPUT_X + spaceX, 13));
            }
        }
    }
}
