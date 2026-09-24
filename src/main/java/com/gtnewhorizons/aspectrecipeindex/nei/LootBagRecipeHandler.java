package com.gtnewhorizons.aspectrecipeindex.nei;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import com.gtnewhorizons.aspectrecipeindex.mixins.late.thaumcraft.UtilsAccessor;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.PositionedStack;
import codechicken.nei.drawable.DrawableBuilder;
import codechicken.nei.drawable.DrawableResource;
import codechicken.nei.recipe.TemplateRecipeHandler;
import thaumcraft.api.internal.WeightedRandomLoot;
import thaumcraft.common.config.ConfigItems;

public class LootBagRecipeHandler extends TemplateRecipeHandler {

    private static final String OVERLAY = "thaumcraft.loot_bag";
    private static final int ITEMS_PER_PAGE = 45;
    private static final DrawableResource SLOT_BACKGROUND = new DrawableBuilder("nei:textures/slot.png", 0, 0, 18, 18)
            .setTextureSize(18, 18).build();

    @Override
    public String getRecipeName() {
        return StatCollector.translateToLocal("aspectrecipeindex.loot_bag.title");
    }

    @Override
    public String getOverlayIdentifier() {
        return OVERLAY;
    }

    @Override
    public String getGuiTexture() {
        return "nei:textures/gui/recipebg.png";
    }

    @Override
    public int getRecipeHeight(int recipeIndex) {
        return 168;
    }

    @Override
    public void drawBackground(int recipeIndex) {
        GL11.glColor4f(1, 1, 1, 1);
        GuiDraw.changeTexture(getGuiTexture());
        GuiDraw.drawTexturedModalRect(0, 0, 7, 11, 166, 10);
        for (int y = 10; y < 130; y += 30) {
            GuiDraw.drawTexturedModalRect(0, y, 7, 25, 166, 30);
        }
        GuiDraw.drawTexturedModalRect(0, 130, 7, 25, 166, 28);
        GuiDraw.drawTexturedModalRect(0, 158, 7, 66, 166, 10);
        SLOT_BACKGROUND.draw(2, 2);
        for (int row = 0; row < 5; row++) {
            for (int column = 0; column < 9; column++) {
                SLOT_BACKGROUND.draw(2 + column * 18, 33 + row * 18);
            }
        }
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        if (OVERLAY.equals(outputId)) {
            for (int tier = 0; tier < 3; tier++) {
                addBagRecipes(tier);
            }
        } else {
            super.loadCraftingRecipes(outputId, results);
        }
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        loadBagRecipes(result);
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        loadBagRecipes(ingredient);
    }

    private void loadBagRecipes(ItemStack stack) {
        if (stack != null && stack.getItem() == ConfigItems.itemLootbag
                && stack.getItemDamage() >= 0
                && stack.getItemDamage() < 3) {
            addBagRecipes(stack.getItemDamage());
        }
    }

    private void addBagRecipes(int tier) {
        List<ItemStack> drops = new ArrayList<>();
        List<WeightedRandomLoot> loot = switch (tier) {
            case 1 -> WeightedRandomLoot.lootBagUncommon;
            case 2 -> WeightedRandomLoot.lootBagRare;
            default -> WeightedRandomLoot.lootBagCommon;
        };
        for (WeightedRandomLoot entry : loot) {
            if (entry.item != null) drops.add(entry.item.copy());
        }
        if (tier > 0) {
            for (int slot = 0; slot < 5; slot++) {
                for (int gearTier = 0; gearTier <= 6; gearTier++) {
                    Item item = UtilsAccessor.getGearItemForSlot(slot, gearTier);
                    if (item != null) drops.add(new ItemStack(item));
                }
            }
        }
        for (int start = 0; start < drops.size(); start += ITEMS_PER_PAGE) {
            arecipes.add(new CachedLootBagRecipe(tier, drops, start));
        }
    }

    @Override
    public void drawExtras(int recipeIndex) {
        GuiDraw.drawString(StatCollector.translateToLocal("aspectrecipeindex.loot_bag.drops"), 3, 22, 0x404040, false);
        if (((CachedLootBagRecipe) arecipes.get(recipeIndex)).hasGear) {
            GuiDraw.drawString(
                    StatCollector.translateToLocal("aspectrecipeindex.loot_bag.gear"),
                    3,
                    125,
                    0x404040,
                    false);
        }
    }

    private class CachedLootBagRecipe extends CachedRecipe {

        private final PositionedStack bag;
        private final List<PositionedStack> drops = new ArrayList<>();
        private final boolean hasGear;

        private CachedLootBagRecipe(int tier, List<ItemStack> allDrops, int start) {
            bag = new PositionedStack(new ItemStack(ConfigItems.itemLootbag, 1, tier), 3, 3);
            hasGear = tier > 0;
            for (int i = start; i < Math.min(start + ITEMS_PER_PAGE, allDrops.size()); i++) {
                int position = i - start;
                drops.add(new PositionedStack(allDrops.get(i), 3 + 18 * (position % 9), 34 + 18 * (position / 9)));
            }
        }

        @Override
        public List<PositionedStack> getIngredients() {
            return Collections.singletonList(bag);
        }

        @Override
        public PositionedStack getResult() {
            return null;
        }

        @Override
        public List<PositionedStack> getOtherStacks() {
            return drops;
        }
    }
}
