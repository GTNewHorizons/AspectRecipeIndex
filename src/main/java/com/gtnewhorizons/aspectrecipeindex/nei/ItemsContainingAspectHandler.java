package com.gtnewhorizons.aspectrecipeindex.nei;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import com.gtnewhorizons.aspectrecipeindex.AspectRecipeIndex;
import com.gtnewhorizons.aspectrecipeindex.ModItems;
import com.gtnewhorizons.aspectrecipeindex.client.ARIClient;
import com.gtnewhorizons.aspectrecipeindex.client.ThaumcraftHooks;
import com.gtnewhorizons.aspectrecipeindex.common.items.ItemAspect;
import com.gtnewhorizons.aspectrecipeindex.util.TCUtil;

import codechicken.lib.gui.GuiDraw;
import codechicken.lib.gui.GuiDraw.ITooltipLineHandler;
import codechicken.nei.ItemsTooltipLineHandler;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.GuiRecipe;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.client.gui.GuiResearchRecipe;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.lib.crafting.ThaumcraftCraftingManager;
import thaumcraft.common.lib.research.ScanManager;

public class ItemsContainingAspectHandler extends TemplateThaumHandler {

    private static final ResourceLocation BACKGROUND = new ResourceLocation(
            AspectRecipeIndex.MODID,
            "textures/gui/itemstack_background.png");
    private static final int STACKS_OVERLAY_WIDTH = 163;
    private static final int STACKS_OVERLAY_HEIGHT = 74;
    private static final int STACKS_OVERLAY_START_X = ARIClient.NEI_GUI_WIDTH / 2 - STACKS_OVERLAY_WIDTH / 2;
    private static final int STACKS_OVERLAY_START_Y = ARIClient.NEI_GUI_HEIGHT - STACKS_OVERLAY_HEIGHT;
    private int ticks;

    private ITooltipLineHandler aspectsTooltipLineHandler = null;
    private ItemStack aspectsTooltipStack = null;

    @Override
    public String getRecipeName() {
        return StatCollector.translateToLocal("aspectrecipeindex.items_containing_aspect.title");
    }

    @Override
    public String getOverlayIdentifier() {
        return "thaumcraft.items_containing_aspect";
    }

    protected AspectList getAspectsForItemStack(ItemStack stack) {
        final int hash = ScanManager.generateItemHash(stack.getItem(), stack.getItemDamage());
        final List<String> list = Thaumcraft.proxy.getScannedObjects().get(TCUtil.username);

        if (list != null && (list.contains("@" + hash) || list.contains("#" + hash))) {
            final AspectList tags = ThaumcraftCraftingManager.getObjectTags(stack);
            return ThaumcraftCraftingManager.getBonusTags(stack, tags);
        }

        return null;
    }

    @Override
    public List<String> handleItemTooltip(GuiRecipe<?> gui, ItemStack stack, List<String> currenttip, int recipe) {
        if (stack == null) {
            this.aspectsTooltipStack = null;
            this.aspectsTooltipLineHandler = null;
        } else if (aspectsTooltipStack == null || !ItemStack.areItemStacksEqual(aspectsTooltipStack, stack)) {
            final AspectList tags = getAspectsForItemStack(stack);
            this.aspectsTooltipStack = stack;
            this.aspectsTooltipLineHandler = null;

            if (tags != null && tags.size() > 0) {
                final List<ItemStack> inputs = new ArrayList<>();

                for (Aspect aspect : tags.getAspectsSortedAmount()) {
                    final ItemStack aspectStack = new ItemStack(ModItems.itemAspect);
                    ItemAspect.setAspect(aspectStack, aspect);
                    aspectStack.stackSize = tags.getAmount(aspect);
                    inputs.add(aspectStack);
                }

                this.aspectsTooltipLineHandler = new ItemsTooltipLineHandler(
                        StatCollector.translateToLocal("aspectrecipeindex.items_containing_aspect.aspect_list"),
                        inputs,
                        true,
                        Integer.MAX_VALUE);
            }
        }

        if (this.aspectsTooltipLineHandler != null) {
            currenttip.add(GuiDraw.TOOLTIP_HANDLER + GuiDraw.getTipLineId(this.aspectsTooltipLineHandler));
        }

        return currenttip;
    }

    @Override
    public void loadCraftingRecipes(ItemStack ingredient) {
        if (ingredient.getItem() instanceof ItemAspect) {
            Aspect aspect = ItemAspect.getAspect(ingredient);

            if (TCUtil.shouldShowAspect(aspect)) {
                final List<ItemStack> containingItemStacks = findContainingItemStacks(aspect);
                if (!containingItemStacks.isEmpty()) {
                    new AspectCachedRecipe(aspect, containingItemStacks);
                }
            }
        }
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        if (outputId.equals(this.getOverlayIdentifier())) {
            for (Aspect aspect : Aspect.aspects.values()) {
                if (TCUtil.shouldShowAspect(aspect)) {
                    continue;
                }
                final List<ItemStack> containingItemStacks = findContainingItemStacks(aspect);
                new AspectCachedRecipe(aspect, containingItemStacks);
            }
        } else if (outputId.equals("item")) {
            this.loadCraftingRecipes((ItemStack) results[0]);
        }
    }

    @Override
    public void drawBackground(int recipe) {
        super.drawBackground(recipe);

        if (!ThaumcraftHooks.isDataLoaded()) {
            GuiDraw.drawString(
                    I18n.format(
                            "aspectrecipeindex.items_containing_aspect.still_load",
                            ThaumcraftHooks.getItemsLoaded(),
                            ThaumcraftHooks.getTotalToLoad()),
                    2,
                    32,
                    ariClient.getColor("aspectrecipeindex.gui.loadingTextColor"),
                    true);
        }
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        GuiDraw.changeTexture(BACKGROUND);
        GuiDraw.drawTexturedModalRect(
                STACKS_OVERLAY_START_X,
                STACKS_OVERLAY_START_Y,
                0,
                0,
                STACKS_OVERLAY_WIDTH,
                STACKS_OVERLAY_HEIGHT);
    }

    @Override
    public void drawForeground(int recipe) {
        drawExtras(recipe);
    }

    @Override
    public void onUpdate() {
        if (ThaumcraftHooks.isDataLoaded()) {
            return;
        }
        if (ticks < -1) {
            ticks = -1;
        }

        if (ticks % 200 == 0 && !arecipes.isEmpty() && arecipes.get(0) instanceof AspectCachedRecipe first) {
            final List<ItemStack> fullList = findContainingItemStacks(first.aspect);
            first.initStackList(fullList);
        }

        ticks++;
    }

    private List<ItemStack> findContainingItemStacks(Aspect aspect) {
        ArrayList<ItemStack> stacks = new ArrayList<>();
        List<String> list = Thaumcraft.proxy.getScannedObjects().get(TCUtil.username);

        if (list != null) {
            for (String itemStackCache : list) { // every string represents cache of itemstack, like @12921929129
                try {
                    itemStackCache = itemStackCache.substring(1); // here we get rid of @

                    ItemStack is = GuiResearchRecipe.getFromCache(Integer.parseInt(itemStackCache));
                    if (is == null) continue;

                    AspectList tags = ThaumcraftCraftingManager.getObjectTags(is);
                    tags = ThaumcraftCraftingManager.getBonusTags(is, tags);

                    if (tags.size() <= 0) continue;

                    ItemStack is2 = is.copy();
                    is2.stackSize = tags.getAmount(aspect);
                    if (is2.stackSize <= 0) continue;

                    stacks.add(is2);
                } catch (NumberFormatException ignored) {}
            }
        }

        stacks.sort(Comparator.<ItemStack>comparingInt(itemStack -> itemStack.stackSize).reversed());

        return stacks;
    }

    private class AspectCachedRecipe extends CachedThaumRecipe {

        private static final int STACKS_COUNT = 36;
        private final Aspect aspect;
        private final int start;
        private ItemStack[] localPageStacks;
        private List<PositionedStack> ingredients = null;
        private final PositionedStack result;
        private AspectCachedRecipe next = null;

        public AspectCachedRecipe(Aspect aspect, List<ItemStack> fullItemStackList) {
            this(aspect, fullItemStackList, 0);
        }

        private AspectCachedRecipe(Aspect aspect, List<ItemStack> fullItemStackList, int start) {
            super(true);
            this.start = start;
            this.aspect = aspect;

            final ItemStack aspectStack = new ItemStack(ModItems.itemAspect);
            ItemAspect.setAspect(aspectStack, aspect);
            this.result = new PositionedStack(aspectStack, ARIClient.NEI_GUI_WIDTH / 2 - 16 / 2, 5);
            prereqs.add(new ResearchInfo(ResearchCategories.getResearch("ASPECTS"), true));

            arecipes.add(this);
            initStackList(fullItemStackList);
        }

        private void initStackList(List<ItemStack> fullItemStackList) {
            this.localPageStacks = getItemsInInterval(fullItemStackList);
            this.ingredients = null;

            if (next != null) {
                next.initStackList(fullItemStackList);
            } else if (start + STACKS_COUNT < fullItemStackList.size()) {
                next = new AspectCachedRecipe(aspect, fullItemStackList, start + STACKS_COUNT);
            }
        }

        @Override
        public PositionedStack getResult() {
            return result;
        }

        @Override
        public List<PositionedStack> getIngredients() {
            if (ingredients == null) {
                ingredients = new ArrayList<>(localPageStacks.length);
                for (int i = 0; i < localPageStacks.length; i++) {
                    int x = STACKS_OVERLAY_START_X + 1 /* small offset from top like in vanilla */ + i % 9 * (16 + 2);
                    int y = STACKS_OVERLAY_START_Y + 1 /* small offset from top like in vanilla */ + i / 9 * (16 + 2);
                    ItemStack stack = localPageStacks[i];
                    ingredients.add(new PositionedStack(stack, x, y));
                }
            }

            return ingredients;
        }

        private ItemStack[] getItemsInInterval(List<ItemStack> stacksIn) {
            int count = start + STACKS_COUNT <= stacksIn.size() ? STACKS_COUNT : stacksIn.size() - start;
            ItemStack[] itemStacks = new ItemStack[count];

            for (int i = 0; i < itemStacks.length; i++) {
                itemStacks[i] = stacksIn.get(start + i);
            }

            return itemStacks;
        }
    }
}
