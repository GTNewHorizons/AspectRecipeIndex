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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import com.gtnewhorizons.aspectrecipeindex.client.ARIClient;
import com.gtnewhorizons.aspectrecipeindex.common.items.ItemAspect;
import com.gtnewhorizons.aspectrecipeindex.util.ARIConfig;
import com.gtnewhorizons.aspectrecipeindex.util.TCUtil;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.PositionedStack;
import codechicken.nei.guihook.GuiContainerManager;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.TemplateRecipeHandler;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.common.Thaumcraft;

public abstract class TemplateThaumHandler extends TemplateRecipeHandler {

    public static final ResourceLocation THAUM_OVERLAYS = new ResourceLocation(
            Thaumcraft.MODID.toLowerCase(),
            "textures/gui/gui_researchbook_overlay.png");
    public static final int OUTPUT_X = 75;
    public static final int OUTPUT_Y = 5;

    protected ARIClient ariClient = ARIClient.getInstance();
    protected ArrayList<AspectList> aspects = new ArrayList<>();

    @Override
    public void drawBackground(int recipeIndex) {
        CachedThaumRecipe recipe = (CachedThaumRecipe) arecipes.get(recipeIndex);
        GL11.glPushMatrix();
        GuiDraw.changeTexture(THAUM_OVERLAYS);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glScalef(1.75F, 1.75F, 1.0F);
        GL11.glTranslatef(0.3F, 0.4F, 0);
        GuiDraw.drawTexturedModalRect(39, 0, 20, 4, 16, 16); // Result item icon
        GL11.glTranslatef(-0.3F, -0.4F, 0);
        if (recipe.shouldShowRecipe) drawIngredientBackground();
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    /**
     * Use this to draw the backgrounds for the ingredients. This is not drawn if the
     */
    protected void drawIngredientBackground() {}

    /**
     * Changes made here must also be made in ShapedArcaneRecipeHandler and ShapelessArcaneRecipeHandler!
     *
     * @param recipeIndex The recipeIndex being drawn
     */
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
                    2,
                    ariClient.getColor("aspectrecipeindex.gui.textColor"),
                    false);
            if (cRecipe instanceof CachedThaumRecipe cachedRecipe) {
                int recipeY = 12;
                for (ResearchInfo r : cachedRecipe.prereqs) {
                    r.onDraw(0, recipeY);
                    recipeY += 13;
                }
            }
        }

        TCUtil.drawSeeAllRecipesLabel(2);
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
        TCUtil.loadTransferRects(this, 0);
    }

    @Override
    public String getGuiTexture() {
        return "nei:textures/gui/recipebg.png";
    }

    public abstract class CachedThaumRecipe extends CachedRecipe {

        protected final List<ResearchInfo> prereqs = new ArrayList<>();
        protected final boolean shouldShowRecipe;
        protected List<PositionedStack> ingredients = new ArrayList<>();
        protected PositionedStack result;
        protected AspectList aspects;

        protected CachedThaumRecipe(boolean shouldShowRecipe) {
            this.shouldShowRecipe = shouldShowRecipe;
        }

        protected void tryAddResearch(ResearchItem researchItem) {
            if (researchItem != null && researchItem.key != null) {
                prereqs.add(
                        new ResearchInfo(
                                researchItem,
                                ThaumcraftApiHelper.isResearchComplete(TCUtil.getUsername(), researchItem.key)));
            }
        }

        @Override
        public void setIngredientPermutation(Collection<PositionedStack> ingredients, ItemStack ingredient) {
            if (ingredient.getItem() instanceof ItemAspect) return;
            super.setIngredientPermutation(ingredients, ingredient);
        }

        protected void setResult(ItemStack out) {
            if (out != null) {
                this.result = new PositionedStack(out, OUTPUT_X, OUTPUT_Y, false);
            }
        }

        protected void setAspects(AspectList aspects) {
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
                Aspect aspect = ItemAspect.getAspect(ingredient);
                return aspects.aspects.containsKey(aspect);
            }
            return super.contains(ingredients, ingredient);
        }

        @Override
        public boolean containsWithNBT(Collection<PositionedStack> ingredients, ItemStack ingredient) {
            if (ingredient.getItem() instanceof ItemAspect) {
                Aspect aspect = ItemAspect.getAspect(ingredient);
                return aspects.aspects.containsKey(aspect);
            }

            return super.containsWithNBT(ingredients, ingredient);
        }

        public void computeVisuals() {
            for (PositionedStack p : this.ingredients) {
                p.generatePermutations();
            }
        }

        public void addIfValid() {
            if (!isValid()) return;
            computeVisuals();
            arecipes.add(this);
            TemplateThaumHandler.this.aspects.add(aspects);
        }

        public boolean shouldShowRecipe() {
            return shouldShowRecipe;
        }

        public List<ResearchInfo> getPrereqs() {
            return prereqs;
        }
    }
}
