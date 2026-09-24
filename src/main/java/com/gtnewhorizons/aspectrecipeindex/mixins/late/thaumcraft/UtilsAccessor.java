package com.gtnewhorizons.aspectrecipeindex.mixins.late.thaumcraft;

import net.minecraft.item.Item;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import thaumcraft.common.lib.utils.Utils;

@Mixin(value = Utils.class, remap = false)
public interface UtilsAccessor {

    @Invoker("getGearItemForSlot")
    static Item getGearItemForSlot(int slot, int tier) {
        throw new AssertionError();
    }
}
