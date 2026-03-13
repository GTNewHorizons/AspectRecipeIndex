package com.gtnewhorizons.aspectrecipeindex.nei.arcaneworkbench;

import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import com.gtnewhorizons.aspectrecipeindex.util.Util;

import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.crafting.ShapelessArcaneRecipe;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchItem;

public class ShapelessArcaneRecipeHandler extends ShapedArcaneRecipeHandler {

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        if (outputId.equals("item")) {
            this.loadCraftingRecipes((ItemStack) results[0]);
            return;
        }
        if (!outputId.equals(this.getOverlayIdentifier())) {
            return;
        }
        for (Object o : ThaumcraftApi.getCraftingRecipes()) {
            if (o instanceof ShapelessArcaneRecipe recipe) {
                new ArcaneShapelessCachedRecipe(recipe, Util.shouldShowRecipe(recipe.getResearch()));
            }
        }
    }

    @Override
    public String getOverlayIdentifier() {
        return "thaumcraft.arcane.shapeless";
    }

    @Override
    public String getRecipeName() {
        return StatCollector.translateToLocal("aspectrecipeindex.shapeless_arcane_crafting.title");
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        for (Object o : ThaumcraftApi.getCraftingRecipes()) {
            if (o instanceof ShapelessArcaneRecipe recipe
                    && NEIServerUtils.areStacksSameTypeCraftingWithNBT(recipe.getRecipeOutput(), result)) {
                new ArcaneShapelessCachedRecipe(recipe, Util.shouldShowRecipe(recipe.getResearch()));
            }
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        for (Object o : ThaumcraftApi.getCraftingRecipes()) {
            if (o instanceof ShapelessArcaneRecipe recipe) {
                ArcaneShapelessCachedRecipe r = new ArcaneShapelessCachedRecipe(recipe, true) {

                    @Override
                    public boolean isValid() {
                        return super.isValid() && containsWithNBT(ingredients, ingredient)
                                && Util.shouldShowRecipe(recipe.getResearch());
                    }
                };
                r.setIngredientPermutation(r.getIngredients(), ingredient);
            }
        }
    }

    protected class ArcaneShapelessCachedRecipe extends ArcaneShapedCachedRecipe {

        public ArcaneShapelessCachedRecipe(ShapelessArcaneRecipe recipe, boolean shouldShowRecipe) {
            super(
                    Math.max(recipe.getInput().size(), 3),
                    recipe.getInput().size() / 3 + 1,
                    recipe.getInput().toArray(),
                    recipe.getRecipeOutput(),
                    shouldShowRecipe,
                    recipe.aspects);
            ResearchItem researchItem = ResearchCategories.getResearch(recipe.getResearch());
            if (researchItem != null) addResearch(researchItem.key);
        }

        @Override
        public void setIngredients(int width, int height, Object[] items) {
            if (items == null || items.length == 0) {
                return;
            }
            for (int x = 0; x < items.length; ++x) {
                if (items[x] == null || NEIServerUtils.extractRecipeItems(items[x]).length == 0) {
                    continue;
                }
                PositionedStack stack = new PositionedStack(
                        items[x],
                        ShapedArcaneRecipeHandler.XPOS[x % 3],
                        ShapedArcaneRecipeHandler.YPOS[x / 3],
                        items[x] instanceof ItemStack);
                stack.setMaxSize(1);
                this.ingredients.add(stack);
            }
        }
    }
}
