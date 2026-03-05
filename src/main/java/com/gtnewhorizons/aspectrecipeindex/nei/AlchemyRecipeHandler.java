package com.gtnewhorizons.aspectrecipeindex.nei;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import com.gtnewhorizons.aspectrecipeindex.ModItems;
import com.gtnewhorizons.aspectrecipeindex.common.items.ItemAspect;
import com.gtnewhorizons.aspectrecipeindex.util.TCUtil;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.CrucibleRecipe;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchItem;

public class AlchemyRecipeHandler extends TemplateThaumHandler {

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        if (outputId.equals(this.getOverlayIdentifier())) {
            for (Object o : ThaumcraftApi.getCraftingRecipes()) {
                if (o instanceof CrucibleRecipe tcRecipe) {
                    boolean shouldShowRecipe = TCUtil.shouldShowRecipe(tcRecipe.key);
                    AlchemyCachedRecipe recipe = new AlchemyCachedRecipe(tcRecipe, shouldShowRecipe);
                    if (recipe.isValid()) {
                        recipe.computeVisuals();
                        this.arecipes.add(recipe);
                    }
                }
            }
        } else if (outputId.equals("item")) {
            this.loadCraftingRecipes((ItemStack) results[0]);
        }
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        for (CrucibleRecipe tcRecipe : TCUtil.getCrucibleRecipes(result)) {
            boolean shouldShowRecipe = TCUtil.shouldShowRecipe(tcRecipe.key);
            AlchemyCachedRecipe recipe = new AlchemyCachedRecipe(tcRecipe, shouldShowRecipe);
            recipe.computeVisuals();
            this.arecipes.add(recipe);
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        List<CrucibleRecipe> tcRecipeList = TCUtil.getCrucibleRecipesByInput(ingredient);

        for (CrucibleRecipe tcRecipe : tcRecipeList) {
            if (tcRecipe == null || !TCUtil.shouldShowRecipe(tcRecipe.key)) {
                continue; // recipe input is not shown without research
            }
            AlchemyCachedRecipe recipe = new AlchemyCachedRecipe(tcRecipe, true);
            recipe.computeVisuals();
            recipe.setIngredientPermutation(recipe.ingredients, ingredient);
            this.arecipes.add(recipe);
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

    private class AlchemyCachedRecipe extends CachedThaumRecipe {

        public AlchemyCachedRecipe(CrucibleRecipe recipe, boolean shouldShowRecipe) {
            super(shouldShowRecipe);
            this.setIngredient(recipe.catalyst);
            this.setResult(recipe.getRecipeOutput());
            this.setAspects(recipe.aspects);
            ResearchItem researchItem = ResearchCategories.getResearch(recipe.key);
            if (researchItem != null && researchItem.key != null) {
                prereqs.add(
                        new ResearchInfo(
                                researchItem,
                                ThaumcraftApiHelper.isResearchComplete(TCUtil.getUsername(), researchItem.key)));
            }
            this.addAspectsToIngredients(aspects);
        }

        protected void setIngredient(Object in) {
            if (in != null && NEIServerUtils.extractRecipeItems(in).length > 0) {
                PositionedStack stack = new PositionedStack(in, 54, 27, false);
                stack.setMaxSize(1);
                this.ingredients = new ArrayList<>(Collections.singletonList(stack));
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
