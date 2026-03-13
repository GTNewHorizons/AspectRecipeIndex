package com.gtnewhorizons.aspectrecipeindex.nei;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import com.gtnewhorizons.aspectrecipeindex.ModItems;
import com.gtnewhorizons.aspectrecipeindex.common.items.ItemAspect;
import com.gtnewhorizons.aspectrecipeindex.util.Util;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.CrucibleRecipe;
import thaumcraft.api.research.ResearchCategories;

public class AlchemyRecipeHandler extends TemplateThaumHandler {

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        if (outputId.equals(this.getOverlayIdentifier())) {
            for (Object o : ThaumcraftApi.getCraftingRecipes()) {
                if (o instanceof CrucibleRecipe recipe) {
                    new AlchemyCachedRecipe(recipe, Util.shouldShowRecipe(recipe.key));
                }
            }
        } else if (outputId.equals("item")) {
            this.loadCraftingRecipes((ItemStack) results[0]);
        }
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        List<CrucibleRecipe> list = new ArrayList<>();
        for (Object o : ThaumcraftApi.getCraftingRecipes()) {
            if (o instanceof CrucibleRecipe recipe
                    && NEIServerUtils.areStacksSameTypeCraftingWithNBT(recipe.getRecipeOutput(), result)) {
                list.add(recipe);
            }
        }
        for (CrucibleRecipe recipe : list) {
            new AlchemyCachedRecipe(recipe, Util.shouldShowRecipe(recipe.key));
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        List<Aspect> inputAspects = Util.getEssentiaFromItem(ingredient);
        for (Object r : ThaumcraftApi.getCraftingRecipes()) {
            if (!(r instanceof CrucibleRecipe recipe) || !Util.shouldShowRecipe(recipe.key)) continue;
            if (recipe.catalystMatches(ingredient)) {
                new AlchemyCachedRecipe(recipe, true);
                continue;
            }
            if (recipe.aspects != null && recipe.aspects.aspects != null && !inputAspects.isEmpty()) {
                for (Aspect a : inputAspects) {
                    if (recipe.aspects.aspects.containsKey(a)) {
                        new AlchemyCachedRecipe(recipe, true);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public String getRecipeName() {
        return StatCollector.translateToLocal("aspectrecipeindex.alchemy.title");
    }

    @Override
    protected void drawIngredientBackground() {
        GuiDraw.drawTexturedModalRect(21, 26, 2, 20, 52, 47); // Crucible outline
        GuiDraw.drawTexturedModalRect(40, 18, 100, 84, 10, 13); // Input item arrow
    }

    @Override
    public String getOverlayIdentifier() {
        return "thaumcraft.alchemy";
    }

    protected class AlchemyCachedRecipe extends CachedThaumRecipe {

        public AlchemyCachedRecipe(CrucibleRecipe recipe, boolean shouldShowRecipe) {
            super(shouldShowRecipe);
            this.setIngredient(recipe.catalyst);
            this.setResult(recipe.getRecipeOutput());
            this.setAspects(recipe.aspects);
            tryAddResearch(ResearchCategories.getResearch(recipe.key));
            this.addAspectsToIngredients(aspects);
            addIfValid();
        }

        protected void setIngredient(Object in) {
            if (in != null && NEIServerUtils.extractRecipeItems(in).length > 0) {
                PositionedStack stack = new PositionedStack(in, 54, 27, false);
                stack.setMaxSize(1);
                this.ingredients.add(stack);
            }
        }

        // Math in these looks a little weird to show similarity to other method above.
        protected void addAspectsToIngredients(AspectList aspects) {
            final int aspectsPerRow = 3;
            int rows = (int) Math.ceil((double) aspects.size() / aspectsPerRow);

            final int xBase = 34;
            final int yBase = 86 - (10 * rows);
            int count = 0;

            for (int row = 0; row < rows; row++) {
                final int columns = Math.min(aspects.size() - row * 3, 3);
                final int offSet = (100 - columns * 20) / 2;
                for (int column = 0; column < columns; column++) {
                    Aspect aspect = aspects.getAspectsSortedAmount()[count++];
                    final int posX = xBase + column * 20 + offSet;
                    final int posY = yBase + row * 20;
                    ItemStack stack = new ItemStack(ModItems.itemAspect, aspects.getAmount(aspect), 1);
                    ItemAspect.setAspect(stack, aspect);
                    this.ingredients.add(new PositionedStack(stack, posX, posY, false));
                }
            }
        }
    }
}
