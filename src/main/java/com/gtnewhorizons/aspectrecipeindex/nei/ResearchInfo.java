package com.gtnewhorizons.aspectrecipeindex.nei;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import java.util.Set;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import com.gtnewhorizons.aspectrecipeindex.util.Util;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.recipe.GuiRecipe;
import cpw.mods.fml.common.Loader;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchCategoryList;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.lib.research.ResearchManager;
import tuhljin.automagy.config.ModResearchItems;

public class ResearchInfo {

    private final boolean isResearched;
    private final String category;
    private final String research;
    private final ResearchItem researchItem;
    private final ResourceLocation icon;
    private int prevX, prevY;

    public ResearchInfo(ResearchItem researchItem, boolean isResearched) {
        this.researchItem = researchItem;
        if (researchItem != null) {
            this.category = ResearchCategories.getCategoryName(researchItem.category);
            ResearchCategoryList list = ResearchCategories.getResearchList(researchItem.category);
            if (list != null && list.icon != null) {
                icon = list.icon;
            } else {
                icon = null;
            }
            this.research = researchItem.getName();
        } else {
            category = null;
            icon = null;
            research = "N/A";
        }
        this.isResearched = isResearched;
    }

    public void onHover(List<String> list) {
        list.add(
                String.format(
                        "%s%s%s: %s",
                        EnumChatFormatting.UNDERLINE,
                        isResearched ? EnumChatFormatting.GREEN : EnumChatFormatting.RED,
                        category != null ? category : "N/A",
                        research));
        try {
            getResearchPrerequisites(list, researchItem);
        } catch (Exception ignored) {} // modded thaum is weird sometimes
        if (list.size() > 1) {
            list.add(1, "");
        }
    }

    public void onDraw(int x, int y) {
        prevX = x;
        prevY = y;
        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GL11.glEnable(GL11.GL_BLEND);
        if (icon != null) {
            UtilsFX.bindTexture(icon);
        } else {
            UtilsFX.bindTexture("textures/items/thaumonomicon.png");
        }
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glTranslated(x, y, 0);
        GL11.glScaled(0.8, 0.8, 0);
        UtilsFX.drawTexturedQuadFull(0, 0, 0.0D);

        if (!isResearched) {
            GL11.glTranslated(20, 1, 0);
            GL11.glScaled(1.9, 1.9, 0);
            GuiDraw.drawString("X", 0, 0, 0xAB0000, false);
        }
        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }

    public Rectangle getRect(GuiRecipe<?> gui, int recipeIndex) {
        final Point offset = gui.getRecipePosition(recipeIndex);
        final int width = isResearched ? 13 : 24;
        return new Rectangle(gui.guiLeft + offset.x + prevX, gui.guiTop + offset.y + prevY, width, 13);
    }

    private static void getResearchPrerequisites(List<String> list, ResearchItem researchItem) {
        if (researchItem != null) {
            // Parent research
            getResearchListByName(list, researchItem.parents, Util.getUsername(), "parents");
            // Parent hidden research
            getResearchListByName(list, researchItem.parentsHidden, Util.getUsername(), "parentsHidden");
            // Item scan
            if (researchItem.getItemTriggers() != null && researchItem.getItemTriggers().length != 0) {
                list.add(StatCollector.translateToLocal("aspectrecipeindex.research.prerequisites.item") + ":");
                for (ItemStack itemStack : researchItem.getItemTriggers()) {
                    String displayName = itemStack.getDisplayName();
                    list.add("    " + displayName);
                }
            }
            // Entity scan
            if (researchItem.getEntityTriggers() != null && researchItem.getEntityTriggers().length != 0) {
                list.add(StatCollector.translateToLocal("aspectrecipeindex.research.prerequisites.entity") + ":");
                for (String entityKey : researchItem.getEntityTriggers()) {
                    String entityName = StatCollector.translateToLocal("entity." + entityKey + ".name");
                    list.add("    " + entityName);
                }
            }
            // Aspect scan
            if (researchItem.getAspectTriggers() != null && researchItem.getAspectTriggers().length != 0) {
                list.add(StatCollector.translateToLocal("aspectrecipeindex.research.prerequisites.aspect") + ":");
                for (Aspect aspect : researchItem.getAspectTriggers()) {
                    String aspectName = aspect.getName() + " - " + aspect.getLocalizedDescription();
                    list.add("    " + aspectName);
                }
            }
            // Kill scan
            if (Loader.isModLoaded("Automagy") && researchItem.category.equals("AUTOMAGY")) {
                Set<String> killList = Util.getKeysByValue(ModResearchItems.cluesOnKill, researchItem.key);
                if (!killList.isEmpty()) {
                    list.add(StatCollector.translateToLocal("aspectrecipeindex.research.prerequisites.kill") + ":");
                    for (String entityKey : killList) {
                        list.add("    " + StatCollector.translateToLocal("entity." + entityKey + ".name"));
                    }
                }
            }
        }
    }

    private static void getResearchListByName(List<String> list, String[] researchKeys, String playerName,
            String keysName) {
        if (researchKeys != null && researchKeys.length != 0) {
            int needResearch = 0;
            list.add(StatCollector.translateToLocal("aspectrecipeindex.research.prerequisites." + keysName) + ":");
            for (String researchKey : researchKeys) {
                String researchName = ResearchCategories
                        .getCategoryName(ResearchCategories.getResearch(researchKey).category) + ": "
                        + ResearchCategories.getResearch(researchKey).getName();
                if (ResearchManager.isResearchComplete(playerName, researchKey)) {
                    if (researchKeys.length <= 10) {
                        researchName = EnumChatFormatting.GREEN + "" + EnumChatFormatting.STRIKETHROUGH + researchName;
                        list.add(EnumChatFormatting.RESET + "    " + researchName);
                    }
                } else {
                    needResearch++;
                    researchName = EnumChatFormatting.RED + researchName;
                    list.add(EnumChatFormatting.RESET + "    " + researchName);
                }
            }
            if (researchKeys.length > 10 && needResearch == 0) {
                list.add(
                        EnumChatFormatting.GREEN + "    "
                                + StatCollector
                                        .translateToLocal("aspectrecipeindex.research.prerequisites.allresearched"));
            }
        }
    }

}
