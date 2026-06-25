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

        private static final ColorResource.Factory color = new ColorResource.Factory("aspectrecipeindex");

        public static final ColorResource
        // spotless:off
          text             = color.rgb("text",            "0x404040"),
          instabilityOff   = color.rgb("instabilityOff",  "0xFFFFFF"),
          instability0     = color.rgb("instability0",    "0x0000AA"),
          instability1     = color.rgb("instability1",    "0x5555FF"),
          instability2     = color.rgb("instability2",    "0xAA00AA"),
          instability3     = color.rgb("instability3",    "0xFFFF55"),
          instability4     = color.rgb("instability4",    "0xFFAA00"),
          instability5     = color.rgb("instability5",    "0xAA0000");
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
