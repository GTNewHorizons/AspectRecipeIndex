package com.gtnewhorizons.aspectrecipeindex.mixins.late.thaumcraft;

import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gtnewhorizons.aspectrecipeindex.client.research.ResearchIdDisplay;

import thaumcraft.api.research.ResearchItem;
import thaumcraft.client.gui.GuiResearchBrowser;

@Mixin(value = GuiResearchBrowser.class, remap = false)
public class GuiResearchBrowserMixin extends GuiScreen {

    @Shadow
    private ResearchItem currentHighlight;

    @Unique
    private int aspectrecipeindex$hoverX;
    @Unique
    private int aspectrecipeindex$hoverY;

    @Unique
    private boolean aspectrecipeindex$isFresh(int x, int y) {
        return x == aspectrecipeindex$hoverX && y == aspectrecipeindex$hoverY;
    }

    @Inject(method = "drawScreen", at = @At("TAIL"), remap = true)
    private void aspectrecipeindex$showResearchId(int mouseX, int mouseY, float partialTicks, CallbackInfo callback) {
        aspectrecipeindex$hoverX = mouseX;
        aspectrecipeindex$hoverY = mouseY;
        if (currentHighlight == null) return;
        var lines = ResearchIdDisplay.lines(currentHighlight.key);
        if (!lines.isEmpty()) {
            int x = Math.max(4, (width - 256) / 2);
            int y = Math.min(height - lines.size() * 10 - 5, (height + 230) / 2 + 8);
            drawHoveringText(lines, x, y, fontRendererObj);
        }
    }

    @Inject(method = "keyTyped", at = @At("HEAD"), cancellable = true, remap = true)
    private void aspectrecipeindex$copyResearchId(char character, int keyCode, CallbackInfo callback) {
        int x = Mouse.getX() * width / Math.max(1, mc.displayWidth);
        int y = height - Mouse.getY() * height / Math.max(1, mc.displayHeight) - 1;
        if (currentHighlight != null && aspectrecipeindex$isFresh(x, y)
                && ResearchIdDisplay.copyKey(currentHighlight.key, keyCode))
            callback.cancel();
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true, remap = true)
    private void aspectrecipeindex$copyResearchIdMouse(int mouseX, int mouseY, int button, CallbackInfo callback) {
        if (currentHighlight != null && aspectrecipeindex$isFresh(mouseX, mouseY)
                && ResearchIdDisplay.copyMouse(currentHighlight.key, button))
            callback.cancel();
    }
}
