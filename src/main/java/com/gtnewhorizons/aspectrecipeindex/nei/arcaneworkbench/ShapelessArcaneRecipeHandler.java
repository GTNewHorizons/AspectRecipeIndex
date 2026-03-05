package com.gtnewhorizons.aspectrecipeindex.nei.arcaneworkbench;

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

import org.lwjgl.opengl.GL11;

import com.gtnewhorizons.aspectrecipeindex.ModItems;
import com.gtnewhorizons.aspectrecipeindex.client.ARIClient;
import com.gtnewhorizons.aspectrecipeindex.common.items.ItemAspect;
import com.gtnewhorizons.aspectrecipeindex.nei.ResearchInfo;
import com.gtnewhorizons.aspectrecipeindex.nei.TemplateThaumHandler;
import com.gtnewhorizons.aspectrecipeindex.util.ARIConfig;
import com.gtnewhorizons.aspectrecipeindex.util.TCUtil;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.guihook.GuiContainerManager;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.ShapelessRecipeHandler;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.ShapelessArcaneRecipe;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.client.lib.UtilsFX;

public class ShapelessArcaneRecipeHandler extends ShapelessRecipeHandler {

    protected ARIClient ariClient = ARIClient.getInstance();
    protected ArrayList<AspectList> aspectsAmount = new ArrayList<>();

    @Override
    public void loadTransferRects() {
        TCUtil.loadTransferRects(this, 7);
    }

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
            if (!(o instanceof ShapelessArcaneRecipe tcRecipe)) {
                continue;
            }
            boolean shouldShowRecipe = TCUtil.shouldShowRecipe(tcRecipe.getResearch());
            ArcaneShapelessCachedRecipe recipe = new ArcaneShapelessCachedRecipe(tcRecipe, shouldShowRecipe);
            if (recipe.isValid()) {
                this.arecipes.add(recipe);
                this.aspectsAmount.add(tcRecipe.aspects);
            }
        }
    }

    @Override
    public String getGuiTexture() {
        return "nei:textures/gui/recipebg.png";
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
            if (o instanceof ShapelessArcaneRecipe tcRecipe) {
                boolean shouldShowRecipe = TCUtil.shouldShowRecipe(tcRecipe.getResearch());
                ArcaneShapelessCachedRecipe recipe = new ArcaneShapelessCachedRecipe(tcRecipe, shouldShowRecipe);
                if (recipe.isValid()
                        && NEIServerUtils.areStacksSameTypeCraftingWithNBT(tcRecipe.getRecipeOutput(), result)) {
                    this.arecipes.add(recipe);
                    this.aspectsAmount.add(tcRecipe.aspects);
                }
            }
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        for (Object o : ThaumcraftApi.getCraftingRecipes()) {
            if (o instanceof ShapelessArcaneRecipe tcRecipe) {
                ArcaneShapelessCachedRecipe recipe = new ArcaneShapelessCachedRecipe(tcRecipe, true);
                if (recipe.isValid() && recipe.containsWithNBT(recipe.ingredients, ingredient)
                        && TCUtil.shouldShowRecipe(tcRecipe.getResearch())) {
                    recipe.setIngredientPermutation(recipe.ingredients, ingredient);
                    this.arecipes.add(recipe);
                    this.aspectsAmount.add(tcRecipe.aspects);
                }
            }
        }
    }

    @Override
    public void drawBackground(int recipeIndex) {
        boolean shouldShowRecipe = ((ArcaneShapelessCachedRecipe) arecipes.get(recipeIndex)).shouldShowRecipe;
        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GuiDraw.changeTexture(TemplateThaumHandler.THAUM_OVERLAYS);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glScalef(1.75F, 1.75F, 1.0F);
        GL11.glTranslatef(0.3F, 0.4F, 0);
        GuiDraw.drawTexturedModalRect(39, 0, 20, 4, 16, 16); // Result item icon
        GL11.glTranslatef(-0.3F, -0.4F, 0);
        if (shouldShowRecipe) {
            GuiDraw.drawTexturedModalRect(20, 13, 112, 15, 52, 52);
        }
        GL11.glPopMatrix();
        if (shouldShowRecipe) {
            GL11.glPushMatrix();
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.4F);
            GL11.glTranslatef(4F, 111F, 0.0F);
            GL11.glScalef(2.0F, 2.0F, 1.0F);
            GuiDraw.drawTexturedModalRect(0, 0, 68, 76, 12, 12);
            GL11.glPopMatrix();
        }
        GL11.glPopAttrib();
    }

    public void drawAspects(int recipe) {
        AspectList aspects = this.aspectsAmount.get(recipe);

        int baseX = 36;
        int baseY = 115;
        int count = 0;
        int columns = aspects.size();
        int xOffset = (100 - columns * 20) / 2;

        for (int column = 0; column < columns; column++) {
            Aspect aspect = aspects.getAspectsSortedAmount()[count++];
            int posX = baseX + column * 18 + xOffset;
            UtilsFX.drawTag(posX, baseY, aspect, 0, 0, GuiDraw.gui.getZLevel());
        }
    }

    /**
     * Changes made here must also be made in ShapedArcaneRecipeHandler and TemplateThaumHandler!
     *
     * @param recipeIndex The recipeIndex being drawn
     */
    @Override
    public void drawExtras(int recipeIndex) {
        CachedRecipe cRecipe = arecipes.get(recipeIndex);
        if (cRecipe instanceof ArcaneShapelessCachedRecipe cachedRecipe && !cachedRecipe.shouldShowRecipe) {
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
                    2,
                    ariClient.getColor("aspectrecipeindex.gui.textColor"),
                    false);
            if (cRecipe instanceof ArcaneShapelessCachedRecipe cachedRecipe) {
                int recipeY = 12;
                for (ResearchInfo r : cachedRecipe.prereqs) {
                    r.onDraw(0, recipeY);
                    recipeY += 13;
                }
            }
        }

        TCUtil.drawSeeAllRecipesLabel(2);
    }

    private boolean isValidInput(Object input) {
        return NEIServerUtils.extractRecipeItems(input).length != 0;
    }

    @Override
    public List<String> handleTooltip(GuiRecipe<?> gui, List<String> list, int recipeIndex) {
        if (ARIConfig.showResearchKey && GuiContainerManager.shouldShowTooltip(gui) && list.isEmpty()) {
            CachedRecipe cRecipe = arecipes.get(recipeIndex);
            Point mousePos = GuiDraw.getMousePosition();

            if (cRecipe instanceof ArcaneShapelessCachedRecipe cachedRecipe) {
                for (ResearchInfo r : cachedRecipe.prereqs) {
                    Rectangle rect = r.getRect(gui, recipeIndex);
                    if (rect.contains(mousePos)) {
                        r.onHover(list);
                    }
                }
            }
        }
        return super.handleTooltip(gui, list, recipeIndex);
    }

    private class ArcaneShapelessCachedRecipe extends CachedShapelessRecipe {

        private final AspectList aspects;
        protected Object[] overlay;
        protected final List<ResearchInfo> prereqs;
        private final boolean shouldShowRecipe;

        public ArcaneShapelessCachedRecipe(ShapelessArcaneRecipe recipe, boolean shouldShowRecipe) {
            super(recipe.getInput(), recipe.getRecipeOutput());
            this.result = new PositionedStack(
                    recipe.getRecipeOutput(),
                    TemplateThaumHandler.OUTPUT_X,
                    TemplateThaumHandler.OUTPUT_Y);
            this.overlay = recipe.getInput().toArray();
            this.aspects = recipe.getAspects();
            this.shouldShowRecipe = shouldShowRecipe;
            ResearchItem researchItem = ResearchCategories.getResearch(recipe.getResearch());
            this.prereqs = new ArrayList<>();
            if (researchItem != null && researchItem.key != null) {
                prereqs.add(
                        new ResearchInfo(
                                researchItem,
                                ThaumcraftApiHelper.isResearchComplete(TCUtil.getUsername(), researchItem.key)));
            }
            this.addAspectsToIngredients(aspects);
        }

        public boolean isValid() {
            return !this.ingredients.isEmpty() && this.result != null;
        }

        @Override
        public void setIngredients(List<?> items) {
            if (items == null || items.isEmpty()) {
                return;
            }
            for (int x = 0; x < items.size(); ++x) {
                if (items.get(x) == null || !isValidInput(items.get(x))) {
                    continue;
                }
                PositionedStack stack = new PositionedStack(
                        items.get(x),
                        ShapedArcaneRecipeHandler.XPOS[x % 3],
                        ShapedArcaneRecipeHandler.YPOS[x / 3],
                        items.get(x) instanceof ItemStack);
                stack.setMaxSize(1);
                this.ingredients.add(stack);
            }
        }

        @Override
        public List<PositionedStack> getIngredients() {
            if (!this.shouldShowRecipe) return Collections.emptyList();
            return super.getIngredients();
        }

        @Override
        public void setIngredientPermutation(Collection<PositionedStack> ingredients, ItemStack ingredient) {
            if (ingredient.getItem() instanceof ItemAspect) return;
            super.setIngredientPermutation(ingredients, ingredient);
        }

        @Override
        public boolean contains(Collection<PositionedStack> ingredients, ItemStack ingredient) {
            if (ingredient.getItem() instanceof ItemAspect) {
                Aspect aspect = ItemAspect.getAspect(ingredient);
                return this.aspects.aspects.containsKey(aspect);
            }
            return super.contains(ingredients, ingredient);
        }

        protected void addAspectsToIngredients(AspectList aspects) {
            final int baseY = 115;
            final int spacing = 19;

            Aspect[] sorted = aspects.getAspectsSortedAmount();
            int columns = sorted.length;

            int startX = 35 + (100 - columns * spacing) / 2;

            for (int i = 0; i < columns; i++) {
                Aspect aspect = sorted[i];
                int posX = startX + i * spacing;

                ItemStack stack = new ItemStack(ModItems.itemAspect, aspects.getAmount(aspect), 1);

                ItemAspect.setAspect(stack, aspect);
                ingredients.add(new PositionedStack(stack, posX, baseY, false));
            }
        }
    }
}
