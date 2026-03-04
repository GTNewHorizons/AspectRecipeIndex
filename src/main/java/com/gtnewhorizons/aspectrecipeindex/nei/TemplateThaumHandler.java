package com.gtnewhorizons.aspectrecipeindex.nei;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import com.gtnewhorizons.aspectrecipeindex.client.ARIClient;
import com.gtnewhorizons.aspectrecipeindex.common.items.ItemAspect;
import com.gtnewhorizons.aspectrecipeindex.util.ARIConfig;
import com.gtnewhorizons.aspectrecipeindex.util.TCUtil;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.guihook.GuiContainerManager;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.TemplateRecipeHandler;
import thaumcraft.api.aspects.AspectList;

public abstract class TemplateThaumHandler extends TemplateRecipeHandler {

    protected ARIClient ariClient = ARIClient.getInstance();
    protected ArrayList<AspectList> aspects = new ArrayList<>();

    @Override
    public void drawExtras(int recipeIndex) {
        CachedRecipe cRecipe = arecipes.get(recipeIndex);
        if (cRecipe instanceof CachedThaumRecipe cachedRecipe && !cachedRecipe.shouldShowRecipe) {
            String textToDraw = StatCollector.translateToLocal("aspectrecipeindex.research.missing");
            int y = 38;
            for (String text : Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(textToDraw, 162)) {
                GuiDraw.drawStringC(text, 82, y, ariClient.getColor("aspectrecipeindex.gui.textColor"), false);
                y += 11;
            }
        }

        if (ARIConfig.showResearchKey) {
            GuiDraw.drawString(
                    EnumChatFormatting.BOLD + StatCollector.translateToLocal("aspectrecipeindex.research.researchName"),
                    0,
                    13,
                    ariClient.getColor("aspectrecipeindex.gui.textColor"),
                    false);
            if (cRecipe instanceof CachedThaumRecipe cachedRecipe) {
                int recipeY = 23;
                for (ResearchInfo r : cachedRecipe.prereqs) {
                    r.onDraw(0, recipeY);
                    recipeY += 13;
                }
            }
        }

        TCUtil.drawSeeAllRecipesLabel(13);
    }

    @Override
    public List<String> handleTooltip(GuiRecipe<?> gui, List<String> list, int recipeIndex) {
        if (!ARIConfig.showResearchKey || !GuiContainerManager.shouldShowTooltip(gui) || !list.isEmpty()) {
            return super.handleTooltip(gui, list, recipeIndex);
        }
        CachedRecipe cRecipe = arecipes.get(recipeIndex);
        Point mousePos = GuiDraw.getMousePosition();

        if (cRecipe instanceof CachedThaumRecipe cachedRecipe) {
            for (ResearchInfo r : cachedRecipe.prereqs) {
                Rectangle rect = r.getRect(gui, recipeIndex);
                if (rect.contains(mousePos)) {
                    r.onHover(list);
                }
            }
        }
        return super.handleTooltip(gui, list, recipeIndex);
    }

    @Override
    public void loadTransferRects() {
        TCUtil.loadTransferRects(this, 13);
    }

    @Override
    public String getGuiTexture() {
        return "nei:textures/gui/recipebg.png";
    }

    abstract class CachedThaumRecipe extends CachedRecipe {

        protected final List<ResearchInfo> prereqs;
        protected final boolean shouldShowRecipe;
        public List<PositionedStack> ingredients;
        public PositionedStack result;
        protected AspectList aspects;

        CachedThaumRecipe(boolean shouldShowRecipe) {
            this.prereqs = new ArrayList<>();
            this.shouldShowRecipe = shouldShowRecipe;
        }

        protected void setIngredient(Object in) {
            if (in != null && NEIServerUtils.extractRecipeItems(in).length > 0) {
                PositionedStack stack = new PositionedStack(in, 51, 30, false);
                stack.setMaxSize(1);
                this.ingredients = new ArrayList<>(Collections.singletonList(stack));
            }
        }

        @Override
        public void setIngredientPermutation(Collection<PositionedStack> ingredients, ItemStack ingredient) {
            if (ingredient.getItem() instanceof ItemAspect) return;
            super.setIngredientPermutation(ingredients, ingredient);
        }

        protected void setResult(ItemStack out) {
            if (out != null) {
                this.result = new PositionedStack(out, 71, 9, false);
            }
        }

        protected void setAspectList(AspectList aspects) {
            this.aspects = aspects;
        }

        @Override
        public PositionedStack getResult() {
            return this.result;
        }

        public AspectList getAspects() {
            return this.aspects;
        }

        @Override
        public List<PositionedStack> getIngredients() {
            if (!this.shouldShowRecipe) return Collections.emptyList();
            return this.ingredients;
        }

        public boolean isValid() {
            return !this.ingredients.isEmpty() && this.result != null;
        }

        @Override
        public boolean contains(Collection<PositionedStack> ingredients, ItemStack ingredient) {
            if (ingredient.getItem() instanceof ItemAspect) {
                return false;
            }
            return super.contains(ingredients, ingredient);
        }

        public void computeVisuals() {
            for (PositionedStack p : this.ingredients) {
                p.generatePermutations();
            }
        }
    }
}
