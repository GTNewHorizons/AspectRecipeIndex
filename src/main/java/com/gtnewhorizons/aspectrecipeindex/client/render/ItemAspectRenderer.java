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

import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.client.lib.UtilsFX;

public class ItemAspectRenderer implements IItemRenderer {

    private static final ResourceLocation UNKNOWN = new ResourceLocation("thaumcraft", "textures/aspects/_unknown.png");

    private static final Tessellator TESS = Tessellator.instance;

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
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.thePlayer;

        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_CULL_FACE);

        setupTransform(type);

        Aspect aspect = ItemAspect.getAspect(stack);

        if (aspect != null && player != null) {
            String name = player.getCommandSenderName();

            if (ThaumcraftApiHelper.hasDiscoveredAspect(name, aspect)) {
                UtilsFX.drawTag(0, 0, aspect, 0F, 0, 0F);
                GL11.glEnable(GL11.GL_CULL_FACE);
                GL11.glPopMatrix();
                return;
            }
        }

        renderUnknown(mc);

        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glPopMatrix();
    }

    private void renderUnknown(Minecraft mc) {
        mc.getTextureManager().bindTexture(UNKNOWN);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GL11.glColor4f(0.5F, 0.5F, 0.5F, 0.75F);

        TESS.startDrawingQuads();
        TESS.addVertexWithUV(0, 16, 0, 0, 1);
        TESS.addVertexWithUV(16, 16, 0, 1, 1);
        TESS.addVertexWithUV(16, 0, 0, 1, 0);
        TESS.addVertexWithUV(0, 0, 0, 0, 0);
        TESS.draw();

        GL11.glDisable(GL11.GL_BLEND);
    }

    private void setupTransform(ItemRenderType type) {
        switch (type) {
            case ENTITY -> {
                GL11.glScalef(0.05F, -0.05F, 0.05F);
                GL11.glRotatef(90F, 0, 1, 0);
                GL11.glTranslatef(-8F, -8F, 0F);
            }
            case EQUIPPED, EQUIPPED_FIRST_PERSON -> {
                GL11.glScalef(0.08F, 0.08F, 0.08F);
                GL11.glRotatef(-45F, 0, 1, 0);
                GL11.glRotatef(180F, 1, 0, 0);
                GL11.glTranslatef(0F, -20F, 0F);
            }
            default -> {}
        }
    }
}
