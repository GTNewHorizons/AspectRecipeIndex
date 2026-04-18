package com.gtnewhorizons.aspectrecipeindex.nei;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.glease.tc4tweak.api.infusionrecipe.EnhancedInfusionRecipe;
import net.glease.tc4tweak.api.infusionrecipe.InfusionRecipeExt;
import net.glease.tc4tweak.api.infusionrecipe.RecipeIngredient;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import com.gtnewhorizons.aspectrecipeindex.AspectRecipeIndex;
import com.gtnewhorizons.aspectrecipeindex.ModItems;
import com.gtnewhorizons.aspectrecipeindex.common.items.ItemAspect;
import com.gtnewhorizons.aspectrecipeindex.util.ARIConfig;
import com.gtnewhorizons.aspectrecipeindex.util.Util;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import thaumcraft.api.IRunicArmor;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.InfusionRecipe;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.lib.events.EventHandlerRunic;

public class InfusionRecipeHandler extends TemplateThaumHandler {

    public static final String OVERLAY = "thaumcraft.infusion";
    private final int aspectsPerRow = 7;
    private static final Map<Integer, ArrayList<ItemStack>> runicArmor = new TreeMap<>();

    @Override
    public String getRecipeName() {
        return StatCollector.translateToLocal("aspectrecipeindex.infusion.title");
    }

    @Override
    public String getOverlayIdentifier() {
        return OVERLAY;
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
                if (o instanceof InfusionRecipe recipe && recipe.getRecipeInput() != null && validOutput(recipe)) {
                    final boolean shouldShowRecipe = Util.shouldShowRecipe(recipe.getResearch());
                    new InfusionCachedRecipe(recipe, shouldShowRecipe);
                }
            }
            loadAllRunicCraftingRecipes();
        } else if (outputId.equals("item")) {
            this.loadCraftingRecipes((ItemStack) results[0]);
        }
    }

    private void loadAllRunicCraftingRecipes() {
        for (int i : runicArmor.keySet()) {
            new RunicShieldCachedRecipe(runicArmor.get(i), i + 1);
        }
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        for (Object o : ThaumcraftApi.getCraftingRecipes()) {
            if (o instanceof InfusionRecipe recipe && recipe.getRecipeOutput() instanceof ItemStack output
                    && NEIServerUtils.areStacksSameTypeCraftingWithNBT(output, result)) {
                final boolean shouldShowRecipe = Util.shouldShowRecipe(recipe.getResearch());
                new InfusionCachedRecipe(recipe, shouldShowRecipe);
            }
        }
        loadRunicUpgradeRecipe(result);
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        List<InfusionRecipe> recipeList = getInfusionRecipesByInput(ingredient);

        for (InfusionRecipe recipe : recipeList) {
            if (Util.shouldShowRecipe(recipe.getResearch())) {
                InfusionCachedRecipe r = new InfusionCachedRecipe(recipe, true);
                r.setIngredientPermutation(r.ingredients, ingredient);
            }
        }
        if (ingredient.getItem() instanceof IRunicArmor) {
            new RunicShieldCachedRecipe(
                    Collections.singletonList(ingredient),
                    EventHandlerRunic.getFinalCharge(ingredient) + 1);
        } else if (isRunicUpgradeIngredient(ingredient)) {
            loadAllRunicCraftingRecipes();
        }
    }

    @Override
    public String getGuiTexture() {
        return super.getGuiTexture();
    }

    @Override
    protected void drawIngredientBackground() {
        GL11.glTranslatef(-0.5F, -1F, 0);
        GuiDraw.drawTexturedModalRect(27, 21, 207, 78, 42, 42); // Runic matrix drawing
        GL11.glTranslatef(0.5F, 1F, 0);
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
            color = Util.getColor("aspectrecipeindex.gui.instabilityColor" + colorIndex);
        } else {
            text = StatCollector.translateToLocal("tc.inst." + Math.min(5, recipe.getInstability() / 2));
            color = Util.getColor("aspectrecipeindex.gui.instabilityColorOff");
        }
        GuiDraw.drawString(text, 83 - GuiDraw.fontRenderer.getStringWidth(text) / 2, 120, color, false);
    }

    public static void init() {
        runicArmor.clear();
        for (Object obj : Item.itemRegistry) {
            if (!(obj instanceof Item item) || !(item instanceof IRunicArmor runic)) {
                continue;
            }

            if (!item.getHasSubtypes()) {
                addRunicItem(runic, new ItemStack(item));
                continue;
            }

            List<ItemStack> subItems = new ArrayList<>();
            try {
                item.getSubItems(item, item.getCreativeTab(), subItems);
            } catch (Exception ignored) {}

            if (subItems.isEmpty()) {
                subItems.add(new ItemStack(item));
            }

            for (ItemStack stack : subItems) {
                addRunicItem(runic, stack);
            }
        }
    }

    private void loadRunicUpgradeRecipe(ItemStack result) {
        if (!(result.getItem() instanceof IRunicArmor) || !result.hasTagCompound()
                || EventHandlerRunic.getHardening(result) <= 0) {
            return;
        }
        ItemStack copy = result.copy();
        int charge = EventHandlerRunic.getFinalCharge(result);
        copy.stackTagCompound.setByte("RS.HARDEN", (byte) (charge - 1));
        new RunicShieldCachedRecipe(Collections.singletonList(copy), charge);
    }

    private static boolean isRunicUpgradeIngredient(ItemStack ingredient) {
        if (ingredient.getItem() == Items.diamond
                || (ingredient.getItem() == ConfigItems.itemResource && ingredient.getItemDamage() == 14)) {
            return true;
        }
        List<Aspect> inputAspects = Util.getEssentiaFromItem(ingredient);
        if (!inputAspects.isEmpty()) {
            for (Aspect a : inputAspects) {
                if (a == Aspect.ENERGY || a == Aspect.ARMOR || a == Aspect.MAGIC) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void addRunicItem(IRunicArmor runic, ItemStack stack) {
        int charge = runic.getRunicCharge(stack);
        runicArmor.computeIfAbsent(charge, k -> new ArrayList<>()).add(stack);
    }

    private static List<InfusionRecipe> getInfusionRecipesByInput(ItemStack input) {
        final ArrayList<InfusionRecipe> list = new ArrayList<>();

        List<Aspect> inputAspects = Util.getEssentiaFromItem(input);
        for (Object r : ThaumcraftApi.getCraftingRecipes()) {
            if (!(r instanceof InfusionRecipe raw)) continue;

            if (raw.getRecipeOutput() == null) continue;
            Object[] comps = raw.getComponents();
            if (comps == null) continue; // avoid null array stream

            // keep bad recipe from killing the scan
            EnhancedInfusionRecipe recipe;
            try {
                recipe = InfusionRecipeExt.get().convert(raw);
            } catch (RuntimeException e) {
                continue;
            }
            if (recipe == null || recipe.getCentral() == null || !validOutput(recipe)) continue;

            if (recipe.getCentral().matches(input) || outerInputsContainIngredient(input, recipe)) {
                list.add(recipe);
                continue;
            }
            if (recipe.getAspects() != null && recipe.getAspects().aspects != null && !inputAspects.isEmpty()) {
                for (Aspect a : inputAspects) {
                    if (recipe.getAspects().aspects.containsKey(a)) {
                        list.add(recipe);
                        break;
                    }
                }
            }
        }
        return list;
    }

    private static boolean outerInputsContainIngredient(ItemStack input, EnhancedInfusionRecipe recipe) {
        List<RecipeIngredient> components = recipe.getComponentsExt();
        if (components != null && !components.isEmpty()) {
            for (RecipeIngredient c : components) {
                try {
                    if (c != null && c.matches(input)) return true;
                } catch (Throwable ignored) {}
            }
        }
        return false;
    }

    // TODO Figure out if anything else is valid
    private static boolean validOutput(InfusionRecipe recipe) {
        Object o = recipe.getRecipeOutput();
        if (o instanceof ItemStack stack && stack.getItem() != null) return true;
        if (o instanceof Object[]arr && arr.length >= 2 && arr[0] instanceof String) return true;
        AspectRecipeIndex.LOGGER.warn("Invalid output for infusion recipe, please report to Aspect Recipe Index:");
        AspectRecipeIndex.LOGGER.warn("Research: {}", recipe.getResearch());
        AspectRecipeIndex.LOGGER.warn("Center Item: {}", recipe.getRecipeInput());
        AspectRecipeIndex.LOGGER.warn("Output: {}", o);
        return false;
    }

    protected class InfusionCachedRecipe extends CachedThaumRecipe {

        private final int instability;

        public InfusionCachedRecipe(InfusionRecipe recipe, boolean shouldShowRecipe) {
            super(shouldShowRecipe);
            setIngredients(recipe);
            setResult(recipe);
            setAspects(recipe.getAspects());
            instability = recipe.getInstability();
            addAspectsToIngredients();
            tryAddResearch(ResearchCategories.getResearch(recipe.getResearch()));
            addIfValid();
        }

        @Override
        public void setIngredientPermutation(Collection<PositionedStack> ingredients, ItemStack ingredient) {
            super.setIngredientPermutation(ingredients, ingredient);
            if (result.item.getItemDamage() == Short.MAX_VALUE && ingredient.getItem() == result.item.getItem()) {
                Items.feather.setDamage(result.item, ingredient.getItemDamage());
            }
        }

        protected InfusionCachedRecipe(int instability, boolean shouldShowRecipe) {
            super(shouldShowRecipe);
            this.instability = instability;
        }

        protected int getInstability() {
            return this.instability;
        }

        protected void setIngredients(InfusionRecipe recipe) {
            EnhancedInfusionRecipe r = InfusionRecipeExt.get().convert(recipe);
            this.ingredients.clear();
            this.ingredients.add(new PositionedStack(r.getCentral().getRepresentativeStacks(), OUTPUT_X, 63));
            addSurroundingItems(r.getComponentsExt());
        }

        protected void addSurroundingItems(List<RecipeIngredient> r) {
            int x = 27;
            int y = -31 + itemOffset();
            int le = r.size();
            ArrayList<Point> coords = new ArrayList<>();
            float pieSlice = 360f / le;
            float currentRot = -90.0F;

            int total;
            int sx;
            int sy;
            for (total = 0; total < le; ++total) {
                sx = (int) (MathHelper.cos((float) (currentRot / 180.0F * Math.PI)) * 40.0F) - 8;
                sy = (int) (MathHelper.sin((float) (currentRot / 180.0F * Math.PI)) * 40.0F) - 8;
                currentRot += pieSlice;
                coords.add(new Point(sx, sy));
            }

            total = 0;
            sx = x + 56;
            sy = y + 102;

            for (RecipeIngredient ingredient : r) {
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
        public PositionedStack getResult() {
            if (result.item.getItemDamage() == Short.MAX_VALUE) {
                Items.feather.setDamage(result.item, 0);
            }
            return this.result;
        }

        protected void addAspectsToIngredients() {
            int rows = (int) Math.ceil((double) aspects.size() / aspectsPerRow);
            final int baseX = 35;
            final int baseY = 130 + itemOffset();
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

        protected int itemOffset() {
            return 0;
        }
    }

    private class RunicShieldCachedRecipe extends InfusionCachedRecipe {

        public static final String RESEARCH = "RUNICAUGMENTATION";

        public RunicShieldCachedRecipe(List<ItemStack> items, int charge) {
            super(5 + charge / 2, Util.shouldShowRecipe(RESEARCH));
            setAspects(charge);
            setIngredients(items, charge);
            setResult(items.get(0).copy()); // Output stacks don't show permutations so only use the first one
            addAspectsToIngredients();
            prereqs.add(
                    new ResearchInfo(
                            ResearchCategories.getResearch(RESEARCH),
                            ThaumcraftApiHelper.isResearchComplete(Util.getUsername(), RESEARCH)));
            addIfValid();
        }

        private void setIngredients(List<ItemStack> items, int charge) {
            ingredients.clear();
            ingredients.add(new PositionedStack(items, OUTPUT_X, 63));
            List<RecipeIngredient> outer = new ArrayList<>();
            outer.add(RecipeIngredient.item(false, new ItemStack(Items.diamond)));
            for (int i = 0; i < charge; i++) {
                outer.add(RecipeIngredient.item(false, new ItemStack(ConfigItems.itemResource, 1, 14)));
            }
            addSurroundingItems(outer);
        }

        @Override
        protected void setResult(ItemStack item) {
            int bonus = EventHandlerRunic.getHardening(item) + 1;
            item.setTagInfo("RS.HARDEN", new NBTTagByte((byte) bonus));
            this.result = new PositionedStack(item, OUTPUT_X, OUTPUT_Y, false);
        }

        public void setAspects(int charge) {
            aspects = new AspectList();
            int cost = 8 << charge; // 8 * 2^charge
            aspects.add(Aspect.ENERGY, cost * 2);
            aspects.add(Aspect.ARMOR, cost);
            aspects.add(Aspect.MAGIC, cost);
        }

    }
}
