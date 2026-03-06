package com.gtnewhorizons.aspectrecipeindex.nei.arcaneworkbench;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import com.gtnewhorizons.aspectrecipeindex.ModItems;
import com.gtnewhorizons.aspectrecipeindex.common.items.ItemAspect;
import com.gtnewhorizons.aspectrecipeindex.nei.ResearchInfo;
import com.gtnewhorizons.aspectrecipeindex.nei.TemplateThaumHandler;
import com.gtnewhorizons.aspectrecipeindex.util.TCUtil;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.ShapedArcaneRecipe;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.common.items.wands.ItemWandCasting;

public class ShapedArcaneRecipeHandler extends TemplateThaumHandler {

    public final static int[] XPOS = new int[] { 47, 75, 103 };
    public final static int[] YPOS = new int[] { 33, 60, 87 };

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        if (outputId.equals("item")) {
            super.loadCraftingRecipes(outputId, results);
            return;
        }
        if (!outputId.equals(this.getOverlayIdentifier())) {
            return;
        }
        for (Object o : ThaumcraftApi.getCraftingRecipes()) {
            if (o instanceof ShapedArcaneRecipe recipe && !(recipe.output.getItem() instanceof ItemWandCasting)) {
                new ArcaneShapedCachedRecipe(recipe, TCUtil.shouldShowRecipe(recipe.getResearch()));
            }
        }
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        // Thaumic Bases' Casting Bracelets extend ItemWandCasting but have regular ShapedArcaneRecipes
        if (result.getItem() instanceof ItemWandCasting && !result.getUnlocalizedName().equals("tb.bracelet")) {
            return;
        }
        for (Object o : ThaumcraftApi.getCraftingRecipes()) {
            if (o instanceof ShapedArcaneRecipe recipe
                    && NEIServerUtils.areStacksSameTypeCraftingWithNBT(recipe.getRecipeOutput(), result)) {
                new ArcaneShapedCachedRecipe(recipe, TCUtil.shouldShowRecipe(recipe.getResearch()));
            }
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        for (Object o : ThaumcraftApi.getCraftingRecipes()) {
            if (!(o instanceof ShapedArcaneRecipe recipe)
                    || recipe.getRecipeOutput().getItem() instanceof ItemWandCasting) {
                continue;
            }
            ArcaneShapedCachedRecipe r = new ArcaneShapedCachedRecipe(recipe, true) {

                @Override
                public boolean isValid() {
                    return super.isValid() && containsWithNBT(ingredients, ingredient)
                            && TCUtil.shouldShowRecipe(recipe.getResearch());
                }
            };
            r.setIngredientPermutation(r.getIngredients(), ingredient);
        }
    }

    @Override
    public String getOverlayIdentifier() {
        return "thaumcraft.arcane.shaped";
    }

    @Override
    public String getRecipeName() {
        return StatCollector.translateToLocal("aspectrecipeindex.shaped_arcane_crafting.title");
    }

    @Override
    public void drawIngredientBackground() {
        GuiDraw.drawTexturedModalRect(20, 13, 112, 15, 52, 52); // Crafting grid
        GL11.glPushMatrix();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.4F);
        GL11.glScalef(1.25F, 1.25F, 1.0F);
        GuiDraw.drawTexturedModalRect(0, 49, 68, 76, 12, 12); // Wand icon
        GL11.glPopMatrix();
    }

    class ArcaneShapedCachedRecipe extends CachedThaumRecipe {

        protected final List<PositionedStack> vis = new ArrayList<>();

        protected ArcaneShapedCachedRecipe(int width, int height, Object[] input, ItemStack output,
                boolean shouldShowRecipe, AspectList aspects) {
            super(shouldShowRecipe);
            setAspects(aspects);
            setIngredients(width, height, input);
            this.result = new PositionedStack(output, OUTPUT_X, OUTPUT_Y);
            addAspects();
            addIfValid();
        }

        public ArcaneShapedCachedRecipe(ShapedArcaneRecipe recipe, boolean shouldShowRecipe) {
            this(
                    recipe.width,
                    recipe.height,
                    recipe.getInput(),
                    recipe.getRecipeOutput(),
                    shouldShowRecipe,
                    recipe.aspects);
            ResearchItem researchItem = ResearchCategories.getResearch(recipe.getResearch());
            if (researchItem != null) addResearch(researchItem.key);
        }

        public void setIngredients(int width, int height, Object[] items) {
            if (items == null || items.length == 0) return;

            for (int x = 0; x < width; ++x) {
                for (int y = 0; y < height; ++y) {
                    Object object = items[y * width + x];

                    if (!(object instanceof ItemStack) && !(object instanceof ItemStack[])
                            && !(object instanceof String)
                            && !(object instanceof List) || (object instanceof List && ((List<?>) object).isEmpty()))
                        continue;

                    PositionedStack stack = new PositionedStack(object, XPOS[x], YPOS[y], object instanceof ItemStack);

                    stack.setMaxSize(1);
                    ingredients.add(stack);
                }
            }
        }

        protected void addAspects() {
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

                vis.add(new PositionedStack(stack, posX, baseY, false));
            }
        }

        @Override
        public List<PositionedStack> getOtherStacks() {
            return this.shouldShowRecipe ? vis : Collections.emptyList();
        }

        protected void addResearch(String name) {
            if (name.isEmpty()) return;
            prereqs.add(
                    new ResearchInfo(
                            ResearchCategories.getResearch(name),
                            ThaumcraftApiHelper.isResearchComplete(TCUtil.getUsername(), name)));
        }
    }
}
