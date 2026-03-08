package com.gtnewhorizons.aspectrecipeindex.common.items;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;

import com.gtnewhorizons.aspectrecipeindex.util.ARIConfig;
import com.gtnewhorizons.aspectrecipeindex.util.TCUtil;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.playerdata.PacketAspectPool;
import thaumcraft.common.lib.research.ResearchManager;

public class ItemAspect extends Item {

    public ItemAspect() {
        super();
        this.setUnlocalizedName("aspect");
        this.setHasSubtypes(true);
        this.setCreativeTab(Thaumcraft.tabTC);
    }

    public static Aspect getAspect(ItemStack item) {
        if (item == null || !item.hasTagCompound()) {
            return null;
        }
        return Aspect.getAspect(item.getTagCompound().getString("Aspect"));
    }

    public static void setAspect(ItemStack item, Aspect aspect) {
        if (item == null) return;
        if (!item.hasTagCompound()) {
            item.setTagCompound(new NBTTagCompound());
        }
        item.getTagCompound().setString("Aspect", aspect.getTag());
    }

    public ItemStack onItemRightClick(ItemStack item, World world, EntityPlayer player) {
        if (!world.isRemote) {
            Aspect aspect = getAspect(item);
            if (aspect == null) return item;
            Thaumcraft.proxy.playerKnowledge.addAspectPool(player.getCommandSenderName(), aspect, (short) 1);
            ResearchManager.scheduleSave(player);
            if (player instanceof EntityPlayerMP playerMP && !(player instanceof FakePlayer)) {
                PacketHandler.INSTANCE.sendTo(
                        new PacketAspectPool(
                                aspect.getTag(),
                                (short) 1,
                                Thaumcraft.proxy.playerKnowledge
                                        .getAspectPoolFor(player.getCommandSenderName(), aspect)),
                        playerMP);
            }
        }

        if (!player.capabilities.isCreativeMode) --item.stackSize;
        return item;
    }

    @SideOnly(Side.CLIENT)
    public String getItemStackDisplayName(ItemStack item) {
        Aspect aspect = getAspect(item);
        if (item == null || aspect == null) {
            return StatCollector.translateToLocal("tc.aspect.unknown");
        }
        String username = TCUtil.getUsername();
        if (ThaumcraftApiHelper.hasDiscoveredAspect(username, aspect)) {
            return aspect.getName();
        }
        if (ARIConfig.showUndiscoveredAspectNames) {
            return StatCollector.translateToLocalFormatted("aspectrecipeindex.aspect.undiscovered", aspect.getName());
        }
        return StatCollector.translateToLocal("tc.aspect.unknown");
    }

    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs tabs, List<ItemStack> stacks) {
        for (Aspect tag : Aspect.aspects.values()) {
            ItemStack stack = new ItemStack(this, 1, 0);
            setAspect(stack, tag);
            stacks.add(stack);
        }
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack item, EntityPlayer player, List<String> tooltips, boolean advanced) {
        Aspect aspect = getAspect(item);
        if (item == null || aspect == null
                || !ARIConfig.showUndiscoveredAspectNames
                || !ThaumcraftApiHelper.hasDiscoveredAspect(TCUtil.getUsername(), aspect)) {
            tooltips.add(StatCollector.translateToLocal("tc.aspect.unknown"));
        } else {
            tooltips.add(StatCollector.translateToLocal(aspect.getLocalizedDescription()));
        }
        super.addInformation(item, player, tooltips, advanced);
    }
}
