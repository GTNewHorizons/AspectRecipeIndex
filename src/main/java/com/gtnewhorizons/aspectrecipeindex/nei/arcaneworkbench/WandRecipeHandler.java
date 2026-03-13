package com.gtnewhorizons.aspectrecipeindex.nei.arcaneworkbench;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.util.StatCollector;
import net.minecraftforge.oredict.OreDictionary;

import com.gtnewhorizons.aspectrecipeindex.common.items.ItemAspect;
import com.gtnewhorizons.aspectrecipeindex.util.ARIConfig;
import com.gtnewhorizons.aspectrecipeindex.util.Util;

import codechicken.nei.NEIServerUtils;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.ShapedArcaneRecipe;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.wands.WandCap;
import thaumcraft.api.wands.WandRod;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.items.ItemResource;
import thaumcraft.common.items.wands.ItemWandCasting;

public class WandRecipeHandler extends ShapedArcaneRecipeHandler {

    public static final String SCEPTRE = "SCEPTRE";
    public static final String ROD_WOOD = "ROD_wood";
    public static final String CAP_IRON = "CAP_iron";

    private static final Predicate<String> VALID_RESEARCH = WandRecipeHandler::validResearch;
    private static final Predicate<String> VISIBLE_RESEARCH = WandRecipeHandler::show;
    public static boolean wandRecipesDeleted = false;

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        if (outputId.equals("item")) {
            super.loadCraftingRecipes(outputId, results);
            return;
        }
        if (!outputId.equals(this.getOverlayIdentifier())) {
            return;
        }
        if (!wandRecipesDeleted) {
            forEachRodCap((rod, cap) -> generateRecipes(rod, cap, VALID_RESEARCH), VALID_RESEARCH);
            for (Object o : ThaumcraftApi.getCraftingRecipes()) {
                if (o instanceof ShapedArcaneRecipe recipe
                        && recipe.getRecipeOutput().getItem() instanceof ItemWandCasting) {
                    new ArcaneShapedCachedRecipe(recipe, Util.shouldShowRecipe(recipe.getResearch()));
                }
            }
            return;
        }
        for (Object o : ThaumcraftApi.getCraftingRecipes()) {
            if (!(o instanceof ShapedArcaneRecipe recipe
                    && recipe.getRecipeOutput().getItem() instanceof ItemWandCasting wand)) {
                continue;
            }
            WandRod rod = wand.getRod(recipe.getRecipeOutput());
            WandCap cap = wand.getCap(recipe.getRecipeOutput());
            if (rod != null && cap != null) {
                final boolean shouldShowRecipe = (!wand.isSceptre(recipe.getRecipeOutput())
                        || Util.shouldShowRecipe(SCEPTRE)) && show(cap.getResearch()) && show(rod.getResearch());
                new ArcaneShapedCachedRecipe(
                        3,
                        3,
                        recipe.input,
                        recipe.getRecipeOutput(),
                        shouldShowRecipe,
                        recipe.aspects);
            }
        }
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        if (!(result.getItem() instanceof ItemWandCasting wand)) {
            return;
        }
        WandRod rod = wand.getRod(result);
        WandCap cap = wand.getCap(result);
        if (wandRecipesDeleted) {
            loadShapedCraftingRecipesForWands(result, wand);
            return;
        }
        // Find items like Casting Bracelets from Thaumic Bases
        if (result.getItem().getClass() != ItemWandCasting.class) {
            for (Object o : ThaumcraftApi.getCraftingRecipes()) {
                if (o instanceof ShapedArcaneRecipe recipe
                        && NEIServerUtils.areStacksSameTypeCraftingWithNBT(recipe.getRecipeOutput(), result)) {
                    new ArcaneShapedCachedRecipe(recipe, Util.shouldShowRecipe(recipe.getResearch()));
                    return;
                }
            }
        }
        if (!validResearch(cap.getResearch()) || !validResearch(rod.getResearch())) {
            return;
        }
        new ArcaneWandCachedRecipe(
                rod,
                cap,
                result,
                wand.isSceptre(result),
                (!wand.isSceptre(result) || Util.shouldShowRecipe(SCEPTRE)) && show(cap.getResearch())
                        && show(rod.getResearch()));
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        if (wandRecipesDeleted) {
            loadWandUsageRecipesForIngredient(ingredient);
            return;
        }
        if (ingredient.getItem() instanceof ItemResource && ingredient.getItemDamage() == 15 && show(SCEPTRE)) {
            forEachRodCap((rod, cap) -> {
                if (validResearch(rod.getResearch()) && validResearch(cap.getResearch())) {
                    generateScepterRecipe(createWand(rod, cap), rod, cap);
                }
            }, VALID_RESEARCH);
            return;
        }
        if (ingredient.getItem() instanceof ItemAspect && ItemAspect.getAspect(ingredient).isPrimal()) {
            forEachRodCap((rod, cap) -> generateRecipes(rod, cap, VISIBLE_RESEARCH), VISIBLE_RESEARCH);
            return;
        }
        forEachRodCap((rod, cap) -> {
            boolean rodMatch = OreDictionary.itemMatches(rod.getItem(), ingredient, true);
            boolean capMatch = OreDictionary.itemMatches(cap.getItem(), ingredient, true);
            if (rodMatch || capMatch) generateRecipes(rod, cap, VISIBLE_RESEARCH);
        }, VISIBLE_RESEARCH);
    }

    @Override
    public String getOverlayIdentifier() {
        return "thaumcraft.wands";
    }

    @Override
    public String getRecipeName() {
        return StatCollector.translateToLocal("aspectrecipeindex.wand_crafting.title");
    }

    private void generateRecipes(WandRod rod, WandCap cap, Predicate<String> researchCheck) {
        ItemStack wand = createWand(rod, cap);
        addRecipe(wand, rod, cap, false);
        if (researchCheck.test(SCEPTRE)) {
            generateScepterRecipe(wand.copy(), rod, cap);
        }
    }

    private void generateScepterRecipe(ItemStack wand, WandRod rod, WandCap cap) {
        makeScepter(wand);
        addRecipe(wand, rod, cap, true);
    }

    private void addRecipe(ItemStack result, WandRod rod, WandCap cap, boolean isScepter) {
        new ArcaneWandCachedRecipe(rod, cap, result, isScepter, shouldShowWandRecipe(result));
    }

    private void forEachRodCap(BiConsumer<WandRod, WandCap> action, Predicate<String> researchCheck) {
        for (WandRod rod : WandRod.rods.values()) {
            if (!researchCheck.test(rod.getResearch())) continue;
            for (WandCap cap : WandCap.caps.values()) {
                if (!researchCheck.test(cap.getResearch())) continue;
                action.accept(rod, cap);
            }
        }
    }

    private void makeScepter(ItemStack wand) {
        wand.setTagInfo("sceptre", new NBTTagByte((byte) 1));
        Items.feather.setDamage(wand, wand.getItemDamage() * 3 / 2);
    }

    private static boolean validResearch(String research) {
        return research.equals(ROD_WOOD) || research.equals(CAP_IRON)
                || ResearchCategories.getResearch(research) != null;
    }

    private static boolean show(String research) {
        return validResearch(research) && Util.shouldShowRecipe(research);
    }

    private ItemStack createWand(WandRod rod, WandCap cap) {
        ItemStack stack = new ItemStack(ConfigItems.itemWandCasting);
        ItemWandCasting wand = (ItemWandCasting) stack.getItem();
        assert wand != null;
        wand.setRod(stack, rod);
        wand.setCap(stack, cap);
        // Wand metadata is based on the crafting cost before vis discounts
        Items.feather.setDamage(stack, getWandVisCost(stack).getAmount(Aspect.AIR));
        return stack;
    }

    public void loadShapedCraftingRecipesForWands(ItemStack wandStack, ItemWandCasting wand) {
        WandRod rod = wand.getRod(wandStack);
        WandCap cap = wand.getCap(wandStack);
        boolean isSceptre = wand.isSceptre(wandStack);

        for (Object o : ThaumcraftApi.getCraftingRecipes()) {
            if (!(o instanceof ShapedArcaneRecipe recipe)) continue;

            ItemStack output = recipe.output;
            if (!(output.getItem() instanceof ItemWandCasting) || isSceptre != wand.isSceptre(output)
                    || output.getItem().getClass() != Objects.requireNonNull(wandStack.getItem()).getClass()) {
                continue;
            }

            WandRod outputRod = wand.getRod(output);
            WandCap outputCap = wand.getCap(output);

            if (!outputRod.getTag().equals(rod.getTag()) || !outputCap.getTag().equals(cap.getTag())) continue;

            // this needs to be ArcaneShapedCachedRecipe instead of ArcaneWandCachedRecipe because of modified recipe
            new ArcaneShapedCachedRecipe(recipe, Util.shouldShowRecipe(recipe.research));
        }
    }

    public void loadWandUsageRecipesForIngredient(ItemStack component) {
        for (Object o : ThaumcraftApi.getCraftingRecipes()) {
            if (!(o instanceof ShapedArcaneRecipe recipe)) {
                continue;
            }
            ItemStack output = recipe.output;
            if (!(output.getItem() instanceof ItemWandCasting)) continue;
            new ArcaneShapedCachedRecipe(recipe, true) {

                @Override
                public boolean isValid() {
                    return super.isValid()
                            && (containsWithNBT(ingredients, component) || (component.getItem() instanceof ItemAspect
                                    && ItemAspect.getAspect(component).isPrimal()))
                            && shouldShowWandRecipe(output);
                }
            };
        }
    }

    public static AspectList getWandVisCost(ItemStack item) {
        AspectList costs = new AspectList();
        if (!(item.getItem() instanceof ItemWandCasting wand)) return costs;
        int cost = wand.getRod(item).getCraftCost() * wand.getCap(item).getCraftCost();
        if (wand.isSceptre(item)) {
            cost = cost * 3 / 2; // *= 1.5
        } else
            if (wand.getRod(item).getResearch().equals(ROD_WOOD) && wand.getCap(item).getResearch().equals(CAP_IRON)) {
                return costs; // Stick + iron cap wand (not scepter) costs no vis
            }
        for (Aspect aspect : Aspect.getPrimalAspects()) {
            costs.add(aspect, cost);
        }
        return costs;
    }

    public static boolean shouldShowWandRecipe(ItemStack item) {
        if (item == null || !(item.getItem() instanceof ItemWandCasting wand)) return false;
        return ARIConfig.showLockedRecipes || (ThaumcraftApiHelper
                .isResearchComplete(Util.getUsername(), wand.getRod(item).getResearch())
                && ThaumcraftApiHelper.isResearchComplete(Util.getUsername(), wand.getCap(item).getResearch())
                && (ThaumcraftApiHelper.isResearchComplete(Util.getUsername(), "SCEPTRE") || !wand.isSceptre(item)));
    }

    protected class ArcaneWandCachedRecipe extends ArcaneShapedCachedRecipe {

        public ArcaneWandCachedRecipe(WandRod rod, WandCap cap, ItemStack result, boolean isScepter,
                boolean shouldShowRecipe) {
            super(
                    3,
                    3,
                    isScepter ? buildScepterInput(rod, cap) : buildWandInput(rod, cap),
                    result,
                    shouldShowRecipe,
                    getWandVisCost(result));

            if (isScepter) addResearch(SCEPTRE);
            addResearch(cap.getResearch());
            addResearch(rod.getResearch());
        }

        public static ItemStack[] buildScepterInput(WandRod rod, WandCap cap) {
            return new ItemStack[] { null, cap.getItem(), new ItemStack(ConfigItems.itemResource, 1, 15), null,
                    rod.getItem(), cap.getItem(), cap.getItem(), null, null };
        }

        public static ItemStack[] buildWandInput(WandRod rod, WandCap cap) {
            return new ItemStack[] { null, null, cap.getItem(), null, rod.getItem(), null, cap.getItem(), null, null };
        }
    }
}
