package com.gtnewhorizons.aspectrecipeindex.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import com.gtnewhorizon.gtnhlib.color.ColorResource;
import com.gtnewhorizons.aspectrecipeindex.common.items.ItemAspect;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssentiaContainerItem;
import thaumcraft.common.items.ItemWispEssence;

public class Util {

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
        if (Minecraft.getMinecraft().thePlayer != null) {
            return Minecraft.getMinecraft().thePlayer.getCommandSenderName();
        }
        return "   "; // return invalid username
    }

    public static class ColorUtils {

        private static final ColorResource.Factory color = new ColorResource.Factory("mymod");

        public static final ColorResource
        // spotless:off
          text             = color.rgb("text",            "#404040"),
          instabilityOff   = color.rgb("instabilityOff",  "#FFFFFF"),
          instability0     = color.rgb("instability0",    "#0000AA"),
          instability1     = color.rgb("instability1",    "#5555FF"),
          instability2     = color.rgb("instability2",    "#AA00AA"),
          instability3     = color.rgb("instability3",    "#FFFF55"),
          instability4     = color.rgb("instability4",    "#FFAA00"),
          instability5     = color.rgb("instability5",    "#AA0000");
        //researchName     = color.rgb("researchName",    "#000000"),
        //loadingText      = color.rgb("loadingText",     "#00CC00");


      // spotless:on
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
