package com.gtnewhorizons.aspectrecipeindex.nei.arcaneworkbench;

import java.util.List;
import java.util.Objects;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.util.StatCollector;
import net.minecraftforge.oredict.OreDictionary;

import com.gtnewhorizons.aspectrecipeindex.client.ARIClient;
import com.gtnewhorizons.aspectrecipeindex.common.items.ItemAspect;
import com.gtnewhorizons.aspectrecipeindex.util.NEIHelper;
import com.gtnewhorizons.aspectrecipeindex.util.TCUtil;

import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.crafting.ShapedArcaneRecipe;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.wands.WandCap;
import thaumcraft.api.wands.WandRod;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.items.ItemResource;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumcraft.common.lib.research.ResearchManager;

public class WandRecipeHandler extends ShapedArcaneRecipeHandler {

    public static final String SCEPTRE = "SCEPTRE";

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        if (outputId.equals("item")) {
            super.loadCraftingRecipes(outputId, results);
            return;
        }
        if (!outputId.equals(this.getOverlayIdentifier())) {
            return;
        }
        if (!ariClient.areWandRecipesDeleted()) {
            loadAllWandRecipes();
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
                boolean shouldShowRecipe = (!wand.isSceptre(recipe.getRecipeOutput())
                        || TCUtil.shouldShowRecipe("SCEPTRE")) && TCUtil.shouldShowRecipe(cap.getResearch())
                        && TCUtil.shouldShowRecipe(rod.getResearch());
                new ArcaneWandCachedRecipe(rod, cap, recipe.getRecipeOutput(), false, shouldShowRecipe);
            }
        }
    }

    /**
     * Also handles staves since they extend WandRod and are stored in the same place
     */
    private void loadAllWandRecipes() {
        for (WandRod rod : WandRod.rods.values()) {
            if (invalidResearch(rod.getResearch())) continue;
            for (WandCap cap : WandCap.caps.values()) {
                if (invalidResearch(cap.getResearch())) continue;
                addWandAndScepterRecipe(rod, cap);
            }
        }
    }

    private void loadAllScepterRecipes() {
        for (WandRod rod : WandRod.rods.values()) {
            if (invalidResearch(rod.getResearch())) continue;
            for (WandCap cap : WandCap.caps.values()) {
                if (invalidResearch(cap.getResearch())) continue;
                addScepterRecipe(createWand(rod, cap), rod, cap);
            }
        }
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        // Thaumic Bases' Casting Bracelets extend ItemWandCasting but have regular ShapedArcaneRecipes
        if (!(result.getItem() instanceof ItemWandCasting wand) || result.getUnlocalizedName().equals("tb.bracelet")) {
            return;
        }
        WandRod rod = wand.getRod(result);
        WandCap cap = wand.getCap(result);
        boolean shouldShowRecipe = false;
        if (!wand.isSceptre(result) || TCUtil.shouldShowRecipe("SCEPTRE")) {
            if (TCUtil.shouldShowRecipe(cap.getResearch()) && TCUtil.shouldShowRecipe(rod.getResearch())) {
                shouldShowRecipe = true;
            }
        }

        if (!ARIClient.getInstance().areWandRecipesDeleted()) {
            new ArcaneWandCachedRecipe(rod, cap, result, wand.isSceptre(result), shouldShowRecipe);
        } else {
            loadShapedCraftingRecipesForWands(result, wand, shouldShowRecipe);
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        if (ariClient.areWandRecipesDeleted()) {
            loadWandUsageRecipesForIngredient(ingredient);
            return;
        }
        if (ingredient.getItem() instanceof ItemResource && ingredient.getItemDamage() == 15
                && ResearchManager.isResearchComplete(TCUtil.getUsername(), SCEPTRE)) {
            loadAllScepterRecipes();
            return;
        } else if (ingredient.getItem() instanceof ItemAspect && ItemAspect.getAspect(ingredient).isPrimal()) {
            loadAllWandRecipes();
            return;
        }
        for (WandRod rod : WandRod.rods.values()) {
            if (invalidResearch(rod.getResearch())) continue;
            if (OreDictionary.itemMatches(rod.getItem(), ingredient, true)) {
                for (WandCap cap : WandCap.caps.values()) {
                    if (invalidResearch(cap.getResearch())) continue;
                    addWandAndScepterRecipe(rod, cap);
                }
                break;
            }
        }
        for (WandCap cap : WandCap.caps.values()) {
            if (invalidResearch(cap.getResearch())) continue;
            if (OreDictionary.itemMatches(cap.getItem(), ingredient, true)) {
                for (WandRod rod : WandRod.rods.values()) {
                    if (invalidResearch(rod.getResearch())) continue;
                    addWandAndScepterRecipe(rod, cap);
                }
                break;
            }
        }
    }

    private static boolean invalidResearch(String research) {
        return !research.equals("CAP_iron") && !research.equals("ROD_wood")
            && (ResearchCategories.getResearch(research) == null
            || !ResearchManager.isResearchComplete(TCUtil.getUsername(), research));
    }

    private void addWandAndScepterRecipe(WandRod rod, WandCap cap) {
        ItemStack wand = createWand(rod, cap);
        new ArcaneWandCachedRecipe(rod, cap, wand, false, TCUtil.shouldShowWandRecipe(wand));
        if (ResearchManager.isResearchComplete(TCUtil.getUsername(), SCEPTRE)) addScepterRecipe(wand, rod, cap);
    }

    private void addScepterRecipe(ItemStack wand, WandRod rod, WandCap cap) {
        // The ItemStack gets copied in the ArcaneWandCachedRecipe's PositionedStack so don't worry about mutation.
        // Yes, this is actually how you set it as a scepter. No, there's no helper method for it like isSceptre.
        wand.setTagInfo("sceptre", new NBTTagByte((byte) 1));
        Items.feather.setDamage(wand, wand.getItemDamage() * 3 / 2);
        new ArcaneWandCachedRecipe(rod, cap, wand, true, TCUtil.shouldShowWandRecipe(wand));
    }

    private ItemStack createWand(WandRod rod, WandCap cap) {
        ItemStack stack = new ItemStack(ConfigItems.itemWandCasting);
        ItemWandCasting wand = (ItemWandCasting) stack.getItem();
        assert wand != null;
        wand.setRod(stack, rod);
        wand.setCap(stack, cap);
        // Wand metadata is based on the crafting cost before vis discounts
        Items.feather.setDamage(stack, NEIHelper.getWandAspectsWandCost(stack).getAmount(Aspect.AIR));
        return stack;
    }

    @Override
    public String getOverlayIdentifier() {
        return "thaumcraft.wands";
    }

    @Override
    public String getRecipeName() {
        return StatCollector.translateToLocal("aspectrecipeindex.wand_crafting.title");
    }

    @SuppressWarnings("unchecked")
    public void loadShapedCraftingRecipesForWands(ItemStack wandStack, ItemWandCasting wand, boolean shouldShowRecipe) {
        WandRod rod = wand.getRod(wandStack);
        WandCap cap = wand.getCap(wandStack);
        boolean isSceptre = wand.isSceptre(wandStack);

        for (Object o : (List<Object>) ThaumcraftApi.getCraftingRecipes()) {
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
            new ArcaneShapedCachedRecipe(recipe, shouldShowRecipe);
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
                            && TCUtil.shouldShowWandRecipe(output);
                }
            };
        }
    }

    private class ArcaneWandCachedRecipe extends ArcaneShapedCachedRecipe {

        public ArcaneWandCachedRecipe(WandRod rod, WandCap cap, ItemStack result, boolean isScepter,
                boolean shouldShowRecipe) {
            super(
                    3,
                    3,
                    isScepter ? NEIHelper.buildScepterInput(rod, cap) : NEIHelper.buildWandInput(rod, cap),
                    result,
                    shouldShowRecipe,
                    NEIHelper.getWandAspectsWandCost(result));

            if (isScepter) addResearch(SCEPTRE);
            addResearch(cap.getResearch());
            addResearch(rod.getResearch());
        }
    }
}
