package com.gtnewhorizons.aspectrecipeindex.mixins.late.thaumcraft;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gtnewhorizons.aspectrecipeindex.client.ThaumcraftHooks;

import thaumcraft.client.gui.MappingThread;

@Mixin(value = MappingThread.class, remap = false)
public class MappingThreadMixin {

    @Shadow
    Map<String, Integer> idMappings;

    /**
     * @author TimeConqueror
     * @reason Count iterations of idMappings for loading progress of Items Containing Aspect handler.
     */
    @Inject(method = "run", at = @At(value = "HEAD"))
    public void retrieveTotalToLoad(CallbackInfo ci) {
        ThaumcraftHooks.setTotalToLoad(idMappings.size());
    }

    /**
     * @author koolkrafter5
     * @reason Count iterations of idMappings for loading progress of Items Containing Aspect handler.
     */
    @Inject(
            method = "run",
            at = @At(value = "INVOKE", target = "Ljava/util/Iterator;next()Ljava/lang/Object;", ordinal = 0))
    private void onLoopIteration(CallbackInfo ci) {
        ThaumcraftHooks.incrementLoadedItems();
    }

    /**
     * @author TimeConqueror
     * @reason Count iterations of idMappings for loading progress of Items Containing Aspect handler.
     */
    @Inject(method = "run", at = @At(value = "TAIL"))
    public void onAllDataLoaded(CallbackInfo ci) {
        ThaumcraftHooks.setAllDataLoaded();
    }
}
