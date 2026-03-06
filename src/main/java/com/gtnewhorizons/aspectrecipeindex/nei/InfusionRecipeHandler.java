package com.gtnewhorizons.aspectrecipeindex.nei;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.spongepowered.libraries.com.google.common.base.Stopwatch;

import com.gtnewhorizons.aspectrecipeindex.ModItems;
import com.gtnewhorizons.aspectrecipeindex.common.items.ItemAspect;
import com.gtnewhorizons.aspectrecipeindex.util.ARIConfig;
import com.gtnewhorizons.aspectrecipeindex.util.TCUtil;

import codechicken.lib.gui.GuiDraw;
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

    private final int aspectsPerRow = 7;
    private static final Map<Integer, ArrayList<ItemStack>> runicArmor = new HashMap<>();

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
                    new InfusionCachedRecipe(tcRecipe, shouldShowRecipe);
                }
            }
            loadAllRunicCraftingRecipes();
        } else if (outputId.equals("item")) {
            this.loadCraftingRecipes((ItemStack) results[0]);
        }
    }

    private void loadAllRunicCraftingRecipes() {
        for (int i : runicArmor.keySet()) {
            new RunicShieldCachedRecipe(runicArmor.get(i), i + 1, -1);
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
        loadRunicCraftingRecipe(result);
    }

    private void loadRunicCraftingRecipe(ItemStack result) {
        if (!(result.getItem() instanceof IRunicArmor) || !result.hasTagCompound()) {
            return;
        }
        int bonus = EventHandlerRunic.getHardening(result);
        if (bonus <= 0) {
            return;
        }
        ItemStack copy = result.copy();
        int charge = EventHandlerRunic.getFinalCharge(result);
        copy.stackTagCompound.setByte("RS.HARDEN", (byte) (charge - 1));
        new RunicShieldCachedRecipe(Collections.singletonList(copy), charge, -1);
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        List<InfusionRecipe> tcRecipeList = TCUtil.getInfusionRecipesByInput(ingredient);

        for (InfusionRecipe tcRecipe : tcRecipeList) {
            if (tcRecipe == null || !TCUtil.shouldShowRecipe(tcRecipe.getResearch())) {
                continue; // recipe input is not shown without research
            }
            InfusionCachedRecipe recipe = new InfusionCachedRecipe(tcRecipe, true);
            recipe.setIngredientPermutation(recipe.ingredients, ingredient);
        }
        if (ingredient.getItem() instanceof IRunicArmor) {
            new RunicShieldCachedRecipe(
                    Collections.singletonList(ingredient),
                    EventHandlerRunic.getFinalCharge(ingredient) + 1,
                    -1);
        } else if (isRunicUpgradeIngredient(ingredient)) {
            loadAllRunicCraftingRecipes();
        }
    }

    private static boolean isRunicUpgradeIngredient(ItemStack ingredient) {
        if (ingredient.getItem() instanceof ItemAspect) {
            final Aspect aspect = ItemAspect.getAspect(ingredient);
            return aspect == Aspect.ENERGY || aspect == Aspect.ARMOR || aspect == Aspect.MAGIC;
        }
        return ingredient.getItem() == Items.diamond
                || (ingredient.getItem() == ConfigItems.itemResource && ingredient.getItemDamage() == 14);
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

    public static void init() {
        runicArmor.clear();
        Stopwatch s = Stopwatch.createStarted();

        for (Object obj : Item.itemRegistry) {
            if (!(obj instanceof Item item) || !(item instanceof IRunicArmor runic)) {
                continue;
            }

            if (!item.getHasSubtypes()) {
                addStack(runic, new ItemStack(item));
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
                addStack(runic, stack);
            }
        }
    }

    private static void addStack(IRunicArmor runic, ItemStack stack) {
        int charge = runic.getRunicCharge(stack);
        runicArmor.computeIfAbsent(charge, k -> new ArrayList<>()).add(stack);
    }

    private class InfusionCachedRecipe extends CachedThaumRecipe {

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

        protected InfusionCachedRecipe(int instability, boolean shouldShowRecipe) {
            super(shouldShowRecipe);
            this.instability = instability;
        }

        protected int getInstability() {
            return this.instability;
        }

        protected void setIngredients(InfusionRecipe recipe) {
            EnhancedInfusionRecipe r = InfusionRecipeExt.get().convert(recipe);
            this.ingredients = new ArrayList<>();
            this.ingredients.add(new PositionedStack(r.getCentral().getRepresentativeStacks(), OUTPUT_X, 62));
            addSurroundingItems(r.getComponentsExt());
        }

        protected void addSurroundingItems(List<RecipeIngredient> r) {
            int x = 27;
            int y = -31;
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

        protected void addAspectsToIngredients() {
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

    private class RunicShieldCachedRecipe extends InfusionCachedRecipe {

        public static final String RESEARCH = "RUNICAUGMENTATION";

        public RunicShieldCachedRecipe(List<ItemStack> items, int charge, int permutation) {
            super(5 + charge / 2, TCUtil.shouldShowRecipe(RESEARCH));
            setAspects(charge);
            setIngredients(items, charge, permutation);
            setResult(items, permutation);
            addAspectsToIngredients();
            prereqs.add(
                    new ResearchInfo(
                            ResearchCategories.getResearch(RESEARCH),
                            ThaumcraftApiHelper.isResearchComplete(TCUtil.getUsername(), RESEARCH)));
            addIfValid();
        }

        private void setIngredients(List<ItemStack> items, int charge, int permutation) {
            ingredients = new ArrayList<>();
            PositionedStack center = new PositionedStack(items, OUTPUT_X, 62);
            if (permutation >= 0) {
                center.setPermutationToRender(permutation);
                result.setPermutationToRender(permutation);
            }
            ingredients.add(center);
            List<RecipeIngredient> outer = new ArrayList<>();
            outer.add(RecipeIngredient.item(false, new ItemStack(Items.diamond)));
            for (int i = 0; i < charge; i++) {
                outer.add(RecipeIngredient.item(false, new ItemStack(ConfigItems.itemResource, 1, 14)));
            }
            addSurroundingItems(outer);
        }

        protected void setResult(List<ItemStack> items, int permutation) {
            ItemStack item = items.get(0).copy(); // Output stacks don't show permutations so only use the first one
            int bonus = EventHandlerRunic.getHardening(item) + 1;
            item.setTagInfo("RS.HARDEN", new NBTTagByte((byte) bonus));
            this.result = new PositionedStack(item, OUTPUT_X, OUTPUT_Y, false);
            if (permutation >= 0) result.setPermutationToRender(permutation);
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
