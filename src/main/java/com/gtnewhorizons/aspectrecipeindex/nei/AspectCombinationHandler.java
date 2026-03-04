package com.gtnewhorizons.aspectrecipeindex.nei;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import org.apache.commons.lang3.ArrayUtils;

import com.gtnewhorizons.aspectrecipeindex.ModItems;
import com.gtnewhorizons.aspectrecipeindex.client.ARIClient;
import com.gtnewhorizons.aspectrecipeindex.client.DrawUtils;
import com.gtnewhorizons.aspectrecipeindex.common.items.ItemAspect;
import com.gtnewhorizons.aspectrecipeindex.util.TCUtil;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.PositionedStack;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.Thaumcraft;

public class AspectCombinationHandler extends TemplateThaumHandler {

    @Override
    public String getRecipeName() {
        return StatCollector.translateToLocal("aspectrecipeindex.aspect_combination.title");
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        if (result.getItem() instanceof ItemAspect) {
            Aspect aspect = ItemAspect.getAspect(result);
            if (Thaumcraft.proxy.playerKnowledge.hasDiscoveredAspect(TCUtil.username, aspect)) {
                new AspectCombinationRecipe(result);
            }
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
    public void drawBackground(int recipe) {
        AspectCombinationRecipe cachedRecipe = (AspectCombinationRecipe) arecipes.get(recipe);
        if (cachedRecipe.getIngredients().isEmpty()) {
            int startY = 25;
            GuiDraw.drawStringC(
                    StatCollector.translateToLocal("tc.aspect.primal"),
                    ARIClient.NEI_GUI_WIDTH / 2,
                    startY,
                    ariClient.getColor("aspectrecipeindex.gui.textColor"),
                    false);
        } else {
            int spaceX = 16;
            int startX = ARIClient.NEI_GUI_WIDTH / 2 - (16 + (16 + spaceX) * 2) / 2;
            int startY = 6;
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
    public void drawForeground(int recipe) {}

    private class AspectCombinationRecipe extends CachedRecipe {

        private final List<PositionedStack> ingredients = new ArrayList<>();
        private final PositionedStack result;

        public AspectCombinationRecipe(ItemStack aspectStack) {
            arecipes.add(this);

            int startY = 0;

            Aspect aspect = ItemAspect.getAspect(aspectStack);
            aspectStack = new ItemStack(ModItems.itemAspect);
            ItemAspect.setAspect(aspectStack, aspect);

            if (aspect.isPrimal()) {
                this.result = new PositionedStack(aspectStack, ARIClient.NEI_GUI_WIDTH / 2 - 16 / 2, startY + 6);
            } else {
                int spaceX = 16;
                int startX = ARIClient.NEI_GUI_WIDTH / 2 - (16 + (16 + spaceX) * 2) / 2;

                this.result = new PositionedStack(aspectStack, startX, startY + 6);

                Aspect[] components = aspect.getComponents();

                ItemStack firstIngred = new ItemStack(ModItems.itemAspect);
                ItemAspect.setAspect(firstIngred, components[0]);
                ItemStack secondIngred = new ItemStack(ModItems.itemAspect);
                ItemAspect.setAspect(secondIngred, components[1]);

                ingredients.add(new PositionedStack(firstIngred, startX + (spaceX + 16), startY + 6));
                ingredients.add(new PositionedStack(secondIngred, startX + (spaceX + 16) * 2, startY + 6));
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
