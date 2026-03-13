package com.gtnewhorizons.aspectrecipeindex.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import com.gtnewhorizons.aspectrecipeindex.AspectRecipeIndex;
import com.gtnewhorizons.aspectrecipeindex.common.items.ItemAspect;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssentiaContainerItem;
import thaumcraft.common.items.ItemWispEssence;

public class Util {

    @SideOnly(Side.CLIENT)
    private static String username = null;
    private static final String[] UNLOCALIZED_COLORS = { "aspectrecipeindex.gui.textColor",
            "aspectrecipeindex.gui.instabilityColor0", "aspectrecipeindex.gui.instabilityColor1",
            "aspectrecipeindex.gui.instabilityColor2", "aspectrecipeindex.gui.instabilityColor3",
            "aspectrecipeindex.gui.instabilityColor4", "aspectrecipeindex.gui.instabilityColor5",
            "aspectrecipeindex.gui.researchNameColor", "aspectrecipeindex.gui.loadingTextColor" };
    private static HashMap<String, Integer> colors;

    public static boolean shouldShowRecipe(String researchKey) {
        return ARIConfig.showLockedRecipes || ThaumcraftApiHelper.isResearchComplete(Util.getUsername(), researchKey);
    }

    public static boolean shouldShowAspect(Aspect aspect) {
        return ARIConfig.showUndiscoveredAspectRecipes
                || ThaumcraftApiHelper.hasDiscoveredAspect(getUsername(), aspect);
    }

    public static <T, E> Set<T> getKeysByValue(Map<T, E> map, E value) {
        return map.entrySet().stream().filter(entry -> Objects.equals(entry.getValue(), value)).map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    @SideOnly(Side.CLIENT)
    public static String getUsername() {
        if (username == null) username = Minecraft.getMinecraft().thePlayer.getCommandSenderName();
        return username;
    }

    public static void updateColorOverride() {
        colors = new HashMap<>();
        for (String c : UNLOCALIZED_COLORS) {
            String hex = StatCollector.translateToLocal(c);
            int color = 0x000000;
            if (hex.length() <= 6) {
                try {
                    color = Integer.parseUnsignedInt(hex, 16);
                } catch (NumberFormatException e) {
                    AspectRecipeIndex.LOGGER.warn("Couldn't format color correctly for: {}", c);
                }
            }
            colors.put(c, color);
        }
    }

    public static int getColor(String key) {
        return colors.get(key) != null ? colors.get(key) : 0x000000;
    }

    public static List<Aspect> getEssentiaFromItem(ItemStack input) {
        List<Aspect> inputAspects = new ArrayList<>();
        if (input.getItem() instanceof ItemAspect) {
            Aspect aspect = ItemAspect.getAspect(input);
            if (aspect != null) {
                inputAspects.add(aspect);
            }
        } else if (!(input.getItem() instanceof ItemWispEssence)
                && input.getItem() instanceof IEssentiaContainerItem container) {
                    AspectList aspects = container.getAspects(input);
                    if (aspects != null && aspects.size() > 0) {
                        inputAspects.addAll(aspects.aspects.keySet());
                    }
                }
        return inputAspects;
    }
}
