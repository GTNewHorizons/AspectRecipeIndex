package com.gtnewhorizons.aspectrecipeindex.client.render;

import static net.minecraftforge.client.IItemRenderer.ItemRenderType.INVENTORY;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

import com.gtnewhorizons.aspectrecipeindex.common.items.ItemAspect;
import com.gtnewhorizons.aspectrecipeindex.util.ARIConfig;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.Thaumcraft;

public class ItemAspectRenderer implements IItemRenderer {

    private static final ResourceLocation UNKNOWN = new ResourceLocation("thaumcraft", "textures/aspects/_unknown.png");

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return true;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return type != INVENTORY;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack stack, Object... data) {
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_CULL_FACE);

        setupTransform(type);

        Aspect aspect = ItemAspect.getAspect(stack);
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;

        if (aspect != null && player != null) {
            if (ARIConfig.showUndiscoveredAspects
                    || Thaumcraft.proxy.playerKnowledge.hasDiscoveredAspect(player.getCommandSenderName(), aspect)) {

                UtilsFX.drawTag(0, 0, aspect, 0.0F, 0, 0.0F);
                GL11.glPopMatrix();
                return;
            }
        }

        renderUnknown();

        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glPopMatrix();
    }

    private void renderUnknown() {
        Minecraft mc = Minecraft.getMinecraft();
        mc.getTextureManager().bindTexture(UNKNOWN);
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        UtilsFX.bindTexture("textures/aspects/_unknown.png");
        GL11.glColor4f(0.5F, 0.5F, 0.5F, 0.75F);
        Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();
        tess.addVertexWithUV(0, 16, 0.0F, 0.0D, 1.0D);
        tess.addVertexWithUV(16, 16, 0.0F, 1.0D, 1.0D);
        tess.addVertexWithUV(16, 0, 0.0F, 1.0D, 0.0D);
        tess.addVertexWithUV(0, 0, 0.0F, 0.0D, 0.0D);
        tess.draw();
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    private void setupTransform(ItemRenderType type) {
        switch (type) {
            case ENTITY:
                GL11.glScalef(0.05F, -0.05F, 0.05F);
                GL11.glRotatef(90F, 0, 1, 0);
                GL11.glTranslatef(-8F, -8F, 0F);
                break;
            case EQUIPPED, EQUIPPED_FIRST_PERSON:
                GL11.glScalef(0.08F, 0.08F, 0.08F);
                GL11.glRotatef(-45F, 0, 1, 0);
                GL11.glRotatef(180F, 1, 0, 0);
                GL11.glTranslatef(0F, -20F, 0F);
                break;
            default:
                break;
        }
    }
}
