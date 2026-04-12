package com.gtnewhorizons.aspectrecipeindex.nei.arcaneworkbench;

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
import com.gtnewhorizons.tcwands.api.GTTier;
import com.gtnewhorizons.tcwands.api.TCWandAPI;
import com.gtnewhorizons.tcwands.api.wrappers.AbstractWandWrapper;
import com.gtnewhorizons.tcwands.api.wrappers.CapWrapper;
import com.gtnewhorizons.tcwands.api.wrappers.SceptreWrapper;

import cpw.mods.fml.common.Loader;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.ShapedArcaneRecipe;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.wands.WandCap;
import thaumcraft.api.wands.WandRod;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.items.wands.ItemWandCasting;

public class WandRecipeHandler extends ShapedArcaneRecipeHandler {

    public static final String OVERLAY = "thaumcraft.wands";
    public static final String SCEPTRE = "SCEPTRE";
    public static final String ROD_WOOD = "ROD_wood";
    public static final String CAP_IRON = "CAP_iron";

    private static final Predicate<String> VALID_RESEARCH = WandRecipeHandler::validResearch;
    private static final Predicate<String> VISIBLE_RESEARCH = WandRecipeHandler::show;
    private static final boolean GTNH_WAND_RECIPES = Loader.isModLoaded("gtnhtcwands");

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        if (outputId.equals("item")) {
            super.loadCraftingRecipes(outputId, results);
            return;
        }
        if (!outputId.equals(this.getOverlayIdentifier())) {
            return;
        }
        if (GTNH_WAND_RECIPES) {
            for (AbstractWandWrapper wand : TCWandAPI.getWandWrappers()) {
                if (!validResearch(wand.getResearchName())) continue;
                for (CapWrapper cap : TCWandAPI.getCaps()) {
                    if (!validResearch(cap.getResearch())) continue;
                    ShapedArcaneRecipe recipe = wand.getRecipe(cap);
                    new GTNHWandCachedRecipe(recipe, wand, cap, shouldShowWandRecipe(recipe.getRecipeOutput()));
                }
            }
        } else {
            forEachRodCap((rod, cap) -> generateRecipes(rod, cap, VALID_RESEARCH), VALID_RESEARCH);
        }
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        if (!(result.getItem() instanceof ItemWandCasting wand)) {
            return;
        }
        WandRod rod = wand.getRod(result);
        WandCap cap = wand.getCap(result);
        boolean scepter = wand.isSceptre(result);
        if (!validResearch(cap.getResearch()) || !validResearch(rod.getResearch())) {
            return;
        }
        boolean shouldShowRecipe = (!scepter || Util.shouldShowRecipe(SCEPTRE)) && show(cap.getResearch())
                && show(rod.getResearch());
        if (GTNH_WAND_RECIPES) {
            AbstractWandWrapper wandWrapper = TCWandAPI.getWrapperForRod(rod, scepter);
            CapWrapper capWrapper = TCWandAPI.getWrapperForCap(cap);
            new GTNHWandCachedRecipe(wandWrapper.getRecipe(capWrapper), wandWrapper, capWrapper, shouldShowRecipe);
            return;
        }
        new ArcaneWandCachedRecipe(rod, cap, result, wand.isSceptre(result), shouldShowRecipe);
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        if (GTNH_WAND_RECIPES) {
            loadGTNHUsageRecipes(ingredient);
            return;
        }
        if (ingredient.getItem() == ConfigItems.itemResource && ingredient.getItemDamage() == 15 && show(SCEPTRE)) {
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
        return OVERLAY;
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

    /**
     * Turns the passed wand into a scepter. Adds the "sceptre" nbt tag and adjusts the metadata accordingly.
     */
    public static void makeScepter(ItemStack wand) {
        wand.setTagInfo("sceptre", new NBTTagByte((byte) 1));
        Items.feather.setDamage(wand, wand.getItemDamage() * 3 / 2);
    }

    public static boolean validResearch(String research) {
        return research.equals(ROD_WOOD) || research.equals(CAP_IRON)
                || ResearchCategories.getResearch(research) != null;
    }

    /**
     * @return whether a recipe that requires this research is valid and should be shown to the player.
     */
    public static boolean show(String research) {
        return validResearch(research) && Util.shouldShowRecipe(research);
    }

    /**
     * @return a new wand with from the provided rod and cap with the correct metadata.
     */
    public static ItemStack createWand(WandRod rod, WandCap cap) {
        ItemStack stack = new ItemStack(ConfigItems.itemWandCasting);
        ItemWandCasting wand = (ItemWandCasting) stack.getItem();
        assert wand != null;
        wand.setRod(stack, rod);
        wand.setCap(stack, cap);
        // Wand metadata is based on the crafting cost before vis discounts
        Items.feather.setDamage(stack, getWandVisCost(stack).getAmount(Aspect.AIR));
        return stack;
    }

    public void loadGTNHUsageRecipes(ItemStack component) {
        usagesForGTNHPrimalCharms(component);
        usagesForGTNHRods(component);
        usagesForGTNHCaps(component);
        usagesForGTNHScrewsAndConductors(component);
        usagesForGTNHVis(component);
    }

    private void usagesForGTNHPrimalCharms(ItemStack component) {
        if (!(component.getItem() == ConfigItems.itemResource) || component.getItemDamage() != 15 || !show(SCEPTRE)) {
            return;
        }
        for (AbstractWandWrapper wand : TCWandAPI.getWandWrappers()) {
            if (!(wand instanceof SceptreWrapper && show(wand.getResearchName()))) continue;
            for (CapWrapper cap : TCWandAPI.getCaps()) {
                if (!show(cap.getResearch())) continue;
                ShapedArcaneRecipe recipe = wand.getRecipe(cap);
                new GTNHWandCachedRecipe(recipe, wand, cap, shouldShowWandRecipe(recipe.getRecipeOutput()));
            }
        }
    }

    private void usagesForGTNHRods(ItemStack component) {
        AbstractWandWrapper wand = TCWandAPI.getWrapperForRod(component, false);
        if (wand == null || !show(wand.getResearchName())) {
            return;
        }
        AbstractWandWrapper scepter = TCWandAPI.getWrapperForRod(component, true);
        for (CapWrapper cap : TCWandAPI.getCaps()) {
            if (!show(cap.getResearch())) continue;
            ShapedArcaneRecipe recipe = wand.getRecipe(cap);
            new GTNHWandCachedRecipe(recipe, wand, cap, shouldShowWandRecipe(recipe.getRecipeOutput()));
            if (scepter == null || !show(SCEPTRE)) continue;
            recipe = scepter.getRecipe(cap);
            new GTNHWandCachedRecipe(recipe, scepter, cap, shouldShowWandRecipe(recipe.getRecipeOutput()));
        }
    }

    private void usagesForGTNHCaps(ItemStack component) {
        CapWrapper cap = TCWandAPI.getWrapperForCap(component);
        if (cap == null || !show(cap.getResearch())) {
            return;
        }
        for (AbstractWandWrapper wand : TCWandAPI.getWandWrappers()) {
            if ((!show(wand.getResearchName()) || wand instanceof SceptreWrapper) && !show(SCEPTRE)) continue;
            ShapedArcaneRecipe recipe = wand.getRecipe(cap);
            new GTNHWandCachedRecipe(recipe, wand, cap, shouldShowWandRecipe(recipe.getRecipeOutput()));
        }
    }

    private void usagesForGTNHVis(ItemStack component) {
        if (!(component.getItem() instanceof ItemAspect) || !ItemAspect.getAspect(component).isPrimal()) return;
        for (AbstractWandWrapper wand : TCWandAPI.getWandWrappers()) {
            if ((!show(wand.getResearchName()) || wand instanceof SceptreWrapper) && !show(SCEPTRE)) continue;
            for (CapWrapper cap : TCWandAPI.getCaps()) {
                if (!show(cap.getResearch())) continue;
                ShapedArcaneRecipe recipe = wand.getRecipe(cap);
                new GTNHWandCachedRecipe(recipe, wand, cap, shouldShowWandRecipe(recipe.getRecipeOutput()));
            }
        }
    }

    private void usagesForGTNHScrewsAndConductors(ItemStack component) {
        for (GTTier value : GTTier.values()) {
            if (!isScrewOrConductor(component, value)) continue;
            for (AbstractWandWrapper wand : TCWandAPI.getWandWrappers()) {
                if (wand.getDetails().tier() != value || !show(wand.getResearchName())
                        || (wand instanceof SceptreWrapper) && !show(SCEPTRE))
                    continue;
                for (CapWrapper cap : TCWandAPI.getCaps()) {
                    if (!show(cap.getResearch())) continue;
                    ShapedArcaneRecipe recipe = wand.getRecipe(cap);
                    new GTNHWandCachedRecipe(recipe, wand, cap, shouldShowWandRecipe(recipe.getRecipeOutput()));
                }
            }
            return;
        }
    }

    private static boolean isScrewOrConductor(ItemStack component, GTTier value) {
        int screwID = OreDictionary.getOreID("screw" + value.getMaterial().mName);
        for (int oreID : OreDictionary.getOreIDs(component)) {
            if (oreID == screwID) return true;
        }
        return OreDictionary.itemMatches(component, value.getConductor(), true);
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

    protected class GTNHWandCachedRecipe extends ArcaneShapedCachedRecipe {

        public GTNHWandCachedRecipe(ShapedArcaneRecipe recipe, AbstractWandWrapper wandWrapper, CapWrapper capWrapper,
                boolean shouldShowRecipe) {
            super(recipe, shouldShowRecipe);
            if (wandWrapper instanceof SceptreWrapper) addResearch(SCEPTRE);
            addResearch(capWrapper.getResearch());
            addResearch(wandWrapper.getResearchName());
        }
    }
}
