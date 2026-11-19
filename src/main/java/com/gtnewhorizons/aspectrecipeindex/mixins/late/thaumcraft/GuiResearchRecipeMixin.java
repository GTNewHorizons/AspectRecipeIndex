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
import thaumcraft.client.gui.GuiResearchRecipe;

@Mixin(value = GuiResearchRecipe.class, remap = false)
public class GuiResearchRecipeMixin extends GuiScreen {

    @Shadow
    private ResearchItem research;

    @Unique
    private boolean aspectrecipeindex$isIdArea(int x, int y) {
        int top = (height - 181) / 2;
        return x >= (width - 256) / 2 && x < (width + 256) / 2 && y >= top - 24 && y < top + 16;
    }

    @Inject(method = "drawScreen", at = @At("TAIL"), remap = true)
    private void aspectrecipeindex$showResearchId(int mouseX, int mouseY, float partialTicks, CallbackInfo callback) {
        if (research == null) return;
        var lines = ResearchIdDisplay.lines(research.key);
        if (lines.isEmpty()) return;
        String label = lines.getFirst();
        fontRendererObj.drawStringWithShadow(
                label,
                (width - fontRendererObj.getStringWidth(label)) / 2,
                Math.max(3, (height - 181) / 2 - 18),
                0xFFFFFF);
        if (aspectrecipeindex$isIdArea(mouseX, mouseY)) drawHoveringText(lines, mouseX, mouseY, fontRendererObj);
    }

    @Inject(method = "keyTyped", at = @At("HEAD"), cancellable = true, remap = true)
    private void aspectrecipeindex$copyResearchId(char character, int keyCode, CallbackInfo callback) {
        int x = Mouse.getX() * width / Math.max(1, mc.displayWidth);
        int y = height - Mouse.getY() * height / Math.max(1, mc.displayHeight) - 1;
        if (research != null && aspectrecipeindex$isIdArea(x, y) && ResearchIdDisplay.copyKey(research.key, keyCode))
            callback.cancel();
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true, remap = true)
    private void aspectrecipeindex$copyResearchIdMouse(int x, int y, int button, CallbackInfo callback) {
        if (research != null && aspectrecipeindex$isIdArea(x, y) && ResearchIdDisplay.copyMouse(research.key, button))
            callback.cancel();
    }
}
