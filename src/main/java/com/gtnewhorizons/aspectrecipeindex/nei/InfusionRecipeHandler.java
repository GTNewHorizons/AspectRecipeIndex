package com.gtnewhorizons.aspectrecipeindex.nei;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.glease.tc4tweak.api.infusionrecipe.EnhancedInfusionRecipe;
import net.glease.tc4tweak.api.infusionrecipe.InfusionRecipeExt;
import net.glease.tc4tweak.api.infusionrecipe.RecipeIngredient;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import com.gtnewhorizons.aspectrecipeindex.ModItems;
import com.gtnewhorizons.aspectrecipeindex.common.items.ItemAspect;
import com.gtnewhorizons.aspectrecipeindex.util.ARIConfig;
import com.gtnewhorizons.aspectrecipeindex.util.TCUtil;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.PositionedStack;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.InfusionRecipe;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchItem;

public class InfusionRecipeHandler extends TemplateThaumHandler {

    private final int aspectsPerRow = 7;

    @Override
    public String getRecipeName() {
        return StatCollector.translateToLocal("aspectrecipeindex.infusion.title");
    }

    @Override
    public String getOverlayIdentifier() {
        return "thaumcraft.infusion";
    }

    @Override
    public int getRecipeHeight(int recipe) {
        final AspectList aspects = this.aspects.get(recipe);
        final int rows = (int) Math.ceil((double) aspects.size() / aspectsPerRow);
        return 152 + (rows - 1) * 20;
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        if (outputId.equals(this.getOverlayIdentifier())) {
            for (Object o : ThaumcraftApi.getCraftingRecipes()) {
                if (o instanceof InfusionRecipe tcRecipe) {
                    if (tcRecipe.getRecipeInput() == null
                            || TCUtil.getAssociatedItemStack(tcRecipe.getRecipeOutput()) == null) {
                        continue;
                    }
                    final boolean shouldShowRecipe = TCUtil.shouldShowRecipe(tcRecipe.getResearch());
                    InfusionCachedRecipe recipe = new InfusionCachedRecipe(tcRecipe, shouldShowRecipe);
                    if (recipe.isValid()) {
                        recipe.computeVisuals();
                        this.arecipes.add(recipe);
                        this.aspects.add(recipe.aspects);
                    }
                }
            }
        } else if (outputId.equals("item")) {
            this.loadCraftingRecipes((ItemStack) results[0]);
        }
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        for (InfusionRecipe tcRecipe : TCUtil.getInfusionRecipes(result)) {
            final boolean shouldShowRecipe = TCUtil.shouldShowRecipe(tcRecipe.getResearch());
            InfusionCachedRecipe recipe = new InfusionCachedRecipe(tcRecipe, shouldShowRecipe);
            recipe.computeVisuals();
            this.arecipes.add(recipe);
            this.aspects.add(recipe.aspects);
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        List<InfusionRecipe> tcRecipeList = TCUtil.getInfusionRecipesByInput(ingredient);

        for (InfusionRecipe tcRecipe : tcRecipeList) {
            if (tcRecipe == null || !TCUtil.shouldShowRecipe(tcRecipe.getResearch())) {
                continue; // recipe input is not shown without research
            }
            InfusionCachedRecipe recipe = new InfusionCachedRecipe(tcRecipe, true);
            recipe.computeVisuals();
            recipe.setIngredientPermutation(recipe.ingredients, ingredient);
            this.arecipes.add(recipe);
            this.aspects.add(recipe.aspects);
        }
    }

    @Override
    public String getGuiTexture() {
        return super.getGuiTexture();
    }

    @Override
    protected void drawIngredientBackground() {
        GL11.glTranslatef(-0.5F, -0.4F, 0);
        GuiDraw.drawTexturedModalRect(27, 20, 207, 78, 42, 42); // Runic matrix drawing
        GL11.glTranslatef(0.5F, 0.4F, 0);
    }

    @Override
    public void drawExtras(int recipeIndex) {
        super.drawExtras(recipeIndex);
        drawInstability(recipeIndex);
    }

    public void drawInstability(int recipeIndex) {
        InfusionCachedRecipe recipe = (InfusionCachedRecipe) this.arecipes.get(recipeIndex);
        if (!recipe.shouldShowRecipe) return;

        String text;
        int color;
        if (ARIConfig.showInstabilityNumber) {
            text = StatCollector.translateToLocal("tc.inst") + recipe.getInstability();
            int colorIndex = Math.min(5, recipe.getInstability() / 2);
            color = ariClient.getColor("aspectrecipeindex.gui.instabilityColor" + colorIndex);
        } else {
            text = StatCollector.translateToLocal("tc.inst." + Math.min(5, recipe.getInstability() / 2));
            color = ariClient.getColor("aspectrecipeindex.gui.instabilityColorOff");
        }
        GuiDraw.drawString(text, 83 - GuiDraw.fontRenderer.getStringWidth(text) / 2, 120, color, false);
    }

    private class InfusionCachedRecipe extends CachedThaumRecipe {

        private final int instability;

        public InfusionCachedRecipe(InfusionRecipe recipe, boolean shouldShowRecipe) {
            super(shouldShowRecipe);
            this.setIngredients(recipe);
            this.setResult(recipe);
            this.setAspects(recipe.getAspects());
            this.instability = recipe.getInstability();
            this.addAspectsToIngredients(this.aspects);
            ResearchItem researchItem = ResearchCategories.getResearch(recipe.getResearch());
            if (researchItem != null && researchItem.key != null) {
                prereqs.add(
                        new ResearchInfo(
                                researchItem,
                                ThaumcraftApiHelper.isResearchComplete(TCUtil.getUsername(), researchItem.key)));
            }
        }

        protected int getInstability() {
            return this.instability;
        }

        protected void setIngredients(InfusionRecipe recipeLegacy) {
            EnhancedInfusionRecipe r = InfusionRecipeExt.get().convert(recipeLegacy);
            this.ingredients = new ArrayList<>();
            this.ingredients.add(new PositionedStack(r.getCentral().getRepresentativeStacks(), 75, 62));
            int x = 27;
            int y = -31;
            int le = r.getComponentsExt().size();
            ArrayList<Point> coords = new ArrayList<>();
            float pieSlice = 360f / le;
            float currentRot = -90.0F;

            int total;
            int sx;
            int sy;
            for (total = 0; total < le; ++total) {
                sx = (int) (MathHelper.cos(currentRot / 180.0F * 3.141593F) * 40.0F) - 8;
                sy = (int) (MathHelper.sin(currentRot / 180.0F * 3.141593F) * 40.0F) - 8;
                currentRot += pieSlice;
                coords.add(new Point(sx, sy));
            }

            total = 0;
            sx = x + 56;
            sy = y + 102;

            for (RecipeIngredient ingredient : r.getComponentsExt()) {
                int vx = sx + coords.get(total).x;
                int vy = sy + coords.get(total).y;
                this.ingredients.add(new PositionedStack(ingredient.getRepresentativeStacks(), vx, vy));
                ++total;
            }
        }

        protected void setResult(InfusionRecipe recipe) {
            ItemStack res;
            if (recipe.getRecipeOutput() instanceof ItemStack stack) {
                res = stack;
            } else {
                // Used for adding faceplates to thaumium fortress helms and maybe other things I don't know
                res = recipe.getRecipeInput().copy();
                Object[] obj = (Object[]) recipe.getRecipeOutput();
                NBTBase tag = (NBTBase) obj[1];
                res.setTagInfo((String) obj[0], tag);
            }

            this.setResult(res);
        }

        @Override
        public void setIngredientPermutation(Collection<PositionedStack> ingredients, ItemStack ingredient) {
            if (ingredient.getItem() instanceof ItemAspect) return;
            super.setIngredientPermutation(ingredients, ingredient);
        }

        @Override
        public boolean contains(Collection<PositionedStack> ingredients, ItemStack ingredient) {
            if (ingredient.getItem() instanceof ItemAspect) {
                return false;
            }
            return super.contains(ingredients, ingredient);
        }

        protected void addAspectsToIngredients(AspectList aspects) {
            int rows = (int) Math.ceil((double) aspects.size() / aspectsPerRow);
            final int baseX = 35;
            final int baseY = 130;
            int count = 0;
            for (int row = 0; row < rows; row++) {
                int reversedRow = -row + rows - 1;
                // distribute evenly
                int columns = (aspects.size() + reversedRow) / rows;
                int xOffset = (100 - columns * 20) / 2;
                for (int column = 0; column < columns; column++) {
                    Aspect aspect = aspects.getAspectsSortedAmount()[count++];
                    int posX = baseX + column * 20 + xOffset;
                    int posY = baseY + row * 20;
                    ItemStack stack = new ItemStack(ModItems.itemAspect, aspects.getAmount(aspect), 1);
                    ItemAspect.setAspect(stack, aspect);
                    this.ingredients.add(new PositionedStack(stack, posX, posY, false));
                }
            }
        }
    }
}
