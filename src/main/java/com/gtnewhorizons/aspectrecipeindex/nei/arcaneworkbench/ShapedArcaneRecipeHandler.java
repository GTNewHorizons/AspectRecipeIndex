package com.gtnewhorizons.aspectrecipeindex.nei.arcaneworkbench;

import static com.gtnewhorizons.aspectrecipeindex.nei.TemplateThaumHandler.THAUM_OVERLAYS;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.oredict.OreDictionary;

import org.lwjgl.opengl.GL11;

import com.gtnewhorizons.aspectrecipeindex.ModItems;
import com.gtnewhorizons.aspectrecipeindex.client.ARIClient;
import com.gtnewhorizons.aspectrecipeindex.common.items.ItemAspect;
import com.gtnewhorizons.aspectrecipeindex.nei.ResearchInfo;
import com.gtnewhorizons.aspectrecipeindex.nei.TemplateThaumHandler;
import com.gtnewhorizons.aspectrecipeindex.util.ARIConfig;
import com.gtnewhorizons.aspectrecipeindex.util.NEIHelper;
import com.gtnewhorizons.aspectrecipeindex.util.TCUtil;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.guihook.GuiContainerManager;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.ShapedRecipeHandler;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.ShapedArcaneRecipe;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.api.wands.WandCap;
import thaumcraft.api.wands.WandRod;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.items.wands.ItemWandCasting;

public class ShapedArcaneRecipeHandler extends ShapedRecipeHandler {

    private final ARIClient ariClient = ARIClient.getInstance();
    protected ArrayList<AspectList> aspectsAmount = new ArrayList<>();
    public final static int[] XPOS = new int[] { 47, 75, 103 };
    public final static int[] YPOS = new int[] { 33, 60, 87 };

    @Override
    public void loadTransferRects() {
        TCUtil.loadTransferRects(this, 0);
    }

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
            ArcaneWandCachedRecipe wandRec = null;
            if (!(o instanceof ShapedArcaneRecipe tcRecipe)) {
                continue;
            }
            if (tcRecipe.getRecipeOutput().getItem() instanceof ItemWandCasting wand) {
                WandRod rod = wand.getRod(tcRecipe.getRecipeOutput());
                WandCap cap = wand.getCap(tcRecipe.getRecipeOutput());
                boolean shouldShowRecipe = false;
                if (!wand.isSceptre(tcRecipe.getRecipeOutput()) || TCUtil.shouldShowRecipe("SCEPTRE")) {
                    if (TCUtil.shouldShowRecipe(cap.getResearch()) && TCUtil.shouldShowRecipe(rod.getResearch())) {
                        shouldShowRecipe = true;
                    }
                }
                if (rod != null || cap != null) {
                    wandRec = new ArcaneWandCachedRecipe(rod, cap, tcRecipe.getRecipeOutput(), false, shouldShowRecipe);
                }
            }
            ArcaneShapedCachedRecipe recipe = new ArcaneShapedCachedRecipe(
                    tcRecipe,
                    TCUtil.shouldShowRecipe(tcRecipe.getResearch()));
            if (wandRec != null) {
                recipe.prereqs.addAll(wandRec.prereqs);
            }
            if (recipe.isValid()) {
                recipe.computeVisuals();
                this.arecipes.add(recipe);
                this.aspectsAmount.add(tcRecipe.aspects);
            }
        }

        if (ariClient.areWandRecipesDeleted()) return;
        // Also handles staves since they extend WandRod and are stored in the same place
        for (WandRod rod : WandRod.rods.values()) {
            if (ResearchCategories.getResearch(rod.getResearch()) == null) continue;
            for (WandCap cap : WandCap.caps.values()) {
                if (ResearchCategories.getResearch(cap.getResearch()) == null) continue;
                addWandAndScepterRecipe(rod, cap);
            }
        }
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        // Thaumic Bases' Casting Bracelets extend ItemWandCasting but have regular ShapedArcaneRecipes
        if (result.getItem() instanceof ItemWandCasting wand && !result.getUnlocalizedName().equals("tb.bracelet")) {
            WandRod rod = wand.getRod(result);
            WandCap cap = wand.getCap(result);
            boolean shouldShowRecipe = false;
            if (!wand.isSceptre(result) || TCUtil.shouldShowRecipe("SCEPTRE")) {
                if (TCUtil.shouldShowRecipe(cap.getResearch()) && TCUtil.shouldShowRecipe(rod.getResearch())) {
                    shouldShowRecipe = true;
                }
            }

            if (!ARIClient.getInstance().areWandRecipesDeleted()) {
                ArcaneWandCachedRecipe recipe = new ArcaneWandCachedRecipe(
                        rod,
                        cap,
                        result,
                        wand.isSceptre(result),
                        shouldShowRecipe);
                recipe.computeVisuals();
                this.arecipes.add(recipe);
                this.aspectsAmount.add(NEIHelper.getWandAspectsWandCost(result));
            }

            loadShapedRecipesForWands(result, shouldShowRecipe);
        } else {
            for (Object o : ThaumcraftApi.getCraftingRecipes()) {
                if (!(o instanceof ShapedArcaneRecipe shapedArcaneRecipe)) {
                    continue;
                }
                ArcaneShapedCachedRecipe recipe = new ArcaneShapedCachedRecipe(
                        shapedArcaneRecipe,
                        TCUtil.shouldShowRecipe(shapedArcaneRecipe.getResearch()));
                if (!recipe.isValid() || !NEIServerUtils
                        .areStacksSameTypeCraftingWithNBT(shapedArcaneRecipe.getRecipeOutput(), result)) {
                    continue;
                }
                recipe.computeVisuals();
                this.arecipes.add(recipe);
                this.aspectsAmount.add(shapedArcaneRecipe.aspects);
            }
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        for (Object o : ThaumcraftApi.getCraftingRecipes()) {
            if (!(o instanceof ShapedArcaneRecipe tcRecipe)) {
                continue;
            }
            ArcaneShapedCachedRecipe recipe = new ArcaneShapedCachedRecipe(tcRecipe, true);
            if (!recipe.isValid() || !recipe.containsWithNBT(recipe.ingredients, ingredient)
                    || !TCUtil.shouldShowRecipe(tcRecipe.getResearch())) {
                continue;
            }
            recipe.computeVisuals();
            recipe.setIngredientPermutation(recipe.ingredients, ingredient);
            this.arecipes.add(recipe);
            this.aspectsAmount.add(tcRecipe.aspects);
        }
        if (ariClient.areWandRecipesDeleted()) return;
        for (WandRod rod : WandRod.rods.values()) {
            if (ResearchCategories.getResearch(rod.getResearch()) == null) continue;
            if (OreDictionary.itemMatches(rod.getItem(), ingredient, true)) {
                for (WandCap cap : WandCap.caps.values()) {
                    if (ResearchCategories.getResearch(cap.getResearch()) == null) continue;
                    addWandAndScepterRecipe(rod, cap);
                }
                break;
            }
        }
        for (WandCap cap : WandCap.caps.values()) {
            if (ResearchCategories.getResearch(cap.getResearch()) == null) continue;
            if (OreDictionary.itemMatches(cap.getItem(), ingredient, true)) {
                for (WandRod rod : WandRod.rods.values()) {
                    if (ResearchCategories.getResearch(rod.getResearch()) == null) continue;
                    addWandAndScepterRecipe(rod, cap);
                }
                break;
            }
        }
    }

    private void addWandAndScepterRecipe(WandRod rod, WandCap cap) {
        ItemStack wand = createWand(rod, cap);
        addWandRecipe(wand, rod, cap, false);
        // The ItemStack gets copied in the ArcaneWandCachedRecipe's PositionedStack so don't worry about mutation.
        // Yes, this is actually how you set it as a scepter. No, there's no helper method for it like isSceptre.
        wand.setTagInfo("sceptre", new NBTTagByte((byte) 1));
        Items.feather.setDamage(wand, wand.getItemDamage() * 3 / 2);
        addWandRecipe(wand, rod, cap, true);
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

    private void addWandRecipe(ItemStack wandStack, WandRod rod, WandCap cap, boolean sceptre) {
        ArcaneWandCachedRecipe recipe = new ArcaneWandCachedRecipe(
                rod,
                cap,
                wandStack,
                sceptre,
                TCUtil.shouldShowWandRecipe(wandStack));

        recipe.computeVisuals();

        arecipes.add(recipe);
        aspectsAmount.add(NEIHelper.getWandAspectsWandCost(wandStack));
    }

    @Override
    public String getGuiTexture() {
        return "nei:textures/gui/recipebg.png";
    }

    @Override
    public String getOverlayIdentifier() {
        return "thaumcraft.arcane.shaped";
    }

    @Override
    public String getRecipeName() {
        return StatCollector.translateToLocal("aspectrecipeindex.shaped_arcane_crafting.title");
    }

    @SuppressWarnings("unchecked")
    public void loadShapedRecipesForWands(ItemStack wandStack, boolean shouldShowRecipe) {
        if (!(wandStack.getItem() instanceof ItemWandCasting wand)) {
            throw new RuntimeException("This method works only for Thaumcraft Wands! Provided: " + wandStack);
        }

        WandRod rod = wand.getRod(wandStack);
        WandCap cap = wand.getCap(wandStack);
        boolean isSceptre = wand.isSceptre(wandStack);

        ((List<Object>) ThaumcraftApi.getCraftingRecipes()).stream().filter(o -> o instanceof ShapedArcaneRecipe)
                .filter(r -> {
                    ItemStack output = ((ShapedArcaneRecipe) r).output;
                    if (!(output.getItem() instanceof ItemWandCasting)) return false;

                    if (isSceptre != wand.isSceptre(output)) return false;

                    if (output.getItem().getClass() != wandStack.getItem().getClass()) return false;

                    WandRod outputRod = wand.getRod(output);
                    WandCap outputCap = wand.getCap(output);

                    return outputRod.getTag().equals(rod.getTag()) && outputCap.getTag().equals(cap.getTag());
                }).forEach(o -> {
                    ShapedArcaneRecipe arcaneRecipe = (ShapedArcaneRecipe) o;
                    // this needs to be ArcaneShapedCachedRecipe instead of ArcaneWandCachedRecipe
                    // because of modified recipe
                    ArcaneShapedCachedRecipe recipe = new ArcaneShapedCachedRecipe(arcaneRecipe, shouldShowRecipe);
                    ArcaneWandCachedRecipe wandRec = new ArcaneWandCachedRecipe(
                            rod,
                            cap,
                            wandStack,
                            false,
                            shouldShowRecipe);
                    recipe.prereqs.addAll(wandRec.prereqs);
                    recipe.computeVisuals();
                    this.arecipes.add(recipe);
                    this.aspectsAmount.add(arcaneRecipe.aspects);
                });
    }

    @Override
    public void drawBackground(int recipeIndex) {
        CachedRecipe recipe = arecipes.get(recipeIndex);
        boolean shouldShowRecipe = recipe instanceof ArcaneShapedCachedRecipe shapedRecipe
                && shapedRecipe.shouldShowRecipe;
        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GuiDraw.changeTexture(THAUM_OVERLAYS);
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

    /**
     * Changes made here must also be made in ShapelessArcaneRecipeHandler and TemplateThaumHandler!
     *
     * @param recipeIndex The recipeIndex being drawn
     */
    @Override
    public void drawExtras(int recipeIndex) {
        CachedRecipe cRecipe = arecipes.get(recipeIndex);
        if ((cRecipe instanceof ArcaneShapedCachedRecipe cachedRecipe && !cachedRecipe.shouldShowRecipe)) {
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
            if (cRecipe instanceof ArcaneShapedCachedRecipe cachedRecipe) {
                int recipeY = 12;
                for (ResearchInfo r : cachedRecipe.prereqs) {
                    r.onDraw(0, recipeY);
                    recipeY += 13;
                }
            } else if (cRecipe instanceof ArcaneWandCachedRecipe cachedRecipe) {
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
        if (cRecipe instanceof ArcaneShapedCachedRecipe cachedRecipe) {
            for (ResearchInfo r : cachedRecipe.prereqs) {
                Rectangle rect = r.getRect(gui, recipeIndex);
                if (rect.contains(mousePos)) {
                    r.onHover(list);
                }
            }
        }
        return super.handleTooltip(gui, list, recipeIndex);
    }

    private class ArcaneShapedCachedRecipe extends CachedShapedRecipe {

        protected AspectList aspects;
        protected final List<ResearchInfo> prereqs = new ArrayList<>();
        protected final List<PositionedStack> vis = new ArrayList<>();
        protected final boolean shouldShowRecipe;

        protected ArcaneShapedCachedRecipe(int width, int height, Object[] input, ItemStack output,
                boolean shouldShowRecipe) {
            super(width, height, input, output);
            this.result = new PositionedStack(output, TemplateThaumHandler.OUTPUT_X, TemplateThaumHandler.OUTPUT_Y);
            this.shouldShowRecipe = shouldShowRecipe;
        }

        public ArcaneShapedCachedRecipe(ShapedArcaneRecipe recipe, boolean shouldShowRecipe) {
            this(recipe.width, recipe.height, recipe.getInput(), recipe.getRecipeOutput(), shouldShowRecipe);

            this.aspects = recipe.getAspects();

            ResearchItem researchItem = ResearchCategories.getResearch(recipe.getResearch());
            if (researchItem != null && researchItem.key != null) addResearch(researchItem.key);

            addAspects(aspects);
        }

        public boolean isValid() {
            return !ingredients.isEmpty() && result != null;
        }

        @Override
        public List<PositionedStack> getIngredients() {
            if (!shouldShowRecipe) return Collections.emptyList();
            return super.getIngredients();
        }

        @Override
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

        @Override
        public void setIngredientPermutation(Collection<PositionedStack> ingredients, ItemStack ingredient) {
            if (ingredient.getItem() instanceof ItemAspect) return;
            super.setIngredientPermutation(ingredients, ingredient);
        }

        @Override
        public boolean contains(Collection<PositionedStack> ingredients, ItemStack ingredient) {
            if (ingredient.getItem() instanceof ItemAspect) {
                Aspect aspect = ItemAspect.getAspect(ingredient);
                return aspects.aspects.containsKey(aspect);
            }
            return super.contains(ingredients, ingredient);
        }

        protected void addAspects(AspectList aspects) {
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
            return vis;
        }

        protected void addResearch(String name) {
            prereqs.add(
                    new ResearchInfo(
                            ResearchCategories.getResearch(name),
                            ThaumcraftApiHelper.isResearchComplete(TCUtil.getUsername(), name)));
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
                    shouldShowRecipe);

            this.aspects = NEIHelper.getPrimalAspectListFromAmounts(NEIHelper.getWandAspectsWandCost(result));

            if (isScepter) addResearch("SCEPTRE");
            if (!cap.getResearch().isEmpty()) addResearch(cap.getResearch());
            if (!rod.getResearch().isEmpty()) addResearch(rod.getResearch());

            addAspects(aspects);
        }
    }
}
