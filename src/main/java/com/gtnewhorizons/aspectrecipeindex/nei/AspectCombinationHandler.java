package com.gtnewhorizons.aspectrecipeindex.nei;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.opengl.GL11;

import com.gtnewhorizons.aspectrecipeindex.ModItems;
import com.gtnewhorizons.aspectrecipeindex.client.ARIClient;
import com.gtnewhorizons.aspectrecipeindex.client.DrawUtils;
import com.gtnewhorizons.aspectrecipeindex.common.items.ItemAspect;
import com.gtnewhorizons.aspectrecipeindex.util.TCUtil;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.PositionedStack;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.research.ResearchCategories;

public class AspectCombinationHandler extends TemplateThaumHandler {

    @Override
    public String getRecipeName() {
        return StatCollector.translateToLocal("aspectrecipeindex.aspect_combination.title");
    }

    @Override
    public String getOverlayIdentifier() {
        return "thaumcraft.aspects";
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        if (result.getItem() instanceof ItemAspect) {
            Aspect aspect = ItemAspect.getAspect(result);
            if (TCUtil.shouldShowAspect(aspect)) {
                new AspectCombinationRecipe(result);
            }
        }
    }

    @Override
    public void loadCraftingRecipes(String inputId, Object... results) {
        if (inputId.equals("item")) loadCraftingRecipes((ItemStack) results[0]);
        if (!inputId.equals(this.getOverlayIdentifier())) return;
        for (Aspect aspect : Aspect.aspects.values()) {
            if (!TCUtil.shouldShowAspect(aspect)) continue;
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

        if (!TCUtil.shouldShowAspect(aspect)) {
            return;
        }
        for (Aspect compoundAspect : Aspect.getCompoundAspects()) {
            if (ArrayUtils.contains(compoundAspect.getComponents(), aspect)
                    && TCUtil.shouldShowAspect(compoundAspect)) {
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
            int startY = 25;
            GuiDraw.drawStringC(
                    StatCollector.translateToLocal("tc.aspect.primal"),
                    ARIClient.NEI_GUI_WIDTH / 2,
                    startY,
                    ariClient.getColor("aspectrecipeindex.gui.textColor"),
                    false);
        } else {
            GL11.glTranslatef(-32F, 8, 0F);
            super.drawBackground(recipe);
            GL11.glTranslatef(32F, -8F, 0F);
            int spaceX = 16;
            int startX = ARIClient.NEI_GUI_WIDTH / 2 - (16 + (16 + spaceX) * 2) / 2;
            int startY = 13;
            DrawUtils.drawXYCenteredString(
                    "=",
                    startX + 24,
                    startY + 8,
                    ariClient.getColor("aspectrecipeindex.gui.textColor"),
                    false);
            DrawUtils.drawXYCenteredString(
                    "+",
                    startX + 56,
                    startY + 8,
                    ariClient.getColor("aspectrecipeindex.gui.textColor"),
                    false);
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

    private class AspectCombinationRecipe extends CachedThaumRecipe {

        private final List<PositionedStack> ingredients = new ArrayList<>();
        private final PositionedStack result;

        public AspectCombinationRecipe(ItemStack aspectStack) {
            super(true);
            arecipes.add(this);
            prereqs.add(new ResearchInfo(ResearchCategories.getResearch("ASPECTS"), true));

            Aspect aspect = ItemAspect.getAspect(aspectStack);
            aspectStack = new ItemStack(ModItems.itemAspect);
            ItemAspect.setAspect(aspectStack, aspect);

            if (aspect.isPrimal()) {
                this.result = new PositionedStack(aspectStack, ARIClient.NEI_GUI_WIDTH / 2 - 16 / 2, 5);
            } else {
                int spaceX = 16;
                int startX = ARIClient.NEI_GUI_WIDTH / 2 - (16 + (16 + spaceX) * 2) / 2;

                this.result = new PositionedStack(aspectStack, startX, 13);

                Aspect[] components = aspect.getComponents();

                ItemStack firstIngred = new ItemStack(ModItems.itemAspect);
                ItemAspect.setAspect(firstIngred, components[0]);
                ItemStack secondIngred = new ItemStack(ModItems.itemAspect);
                ItemAspect.setAspect(secondIngred, components[1]);

                ingredients.add(new PositionedStack(firstIngred, startX + (spaceX + 16), 13));
                ingredients.add(new PositionedStack(secondIngred, startX + (spaceX + 16) * 2, 13));
            }
        }

        @Override
        public PositionedStack getResult() {
            return result;
        }

        @Override
        public List<PositionedStack> getIngredients() {
            return ingredients;
        }
    }
}
