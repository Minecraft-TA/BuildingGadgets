package com.direwolf20.buildinggadgets.common.entities;

import com.direwolf20.buildinggadgets.common.blocks.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

public class ConstructionBlockEntityRender extends Render {

    public ConstructionBlockEntityRender() {
        this.shadowSize = 0F;
    }

    @Override
    public void doRender(Entity e, double x, double y, double z, float entityYaw, float partialTicks) {
        ConstructionBlockEntity entity = (ConstructionBlockEntity) e;
        RenderBlocks renderBlocks = new RenderBlocks(e.worldObj);
        Minecraft mc = Minecraft.getMinecraft();
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_CONSTANT_ALPHA, GL11.GL_ONE_MINUS_CONSTANT_ALPHA);

        mc.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
        int teCounter = entity.ticksExisted;
        int maxLife = entity.maxLife;
        if (teCounter > maxLife) {
            teCounter = maxLife;
        }
        float scale = (float) (maxLife - teCounter) / maxLife;
        if (entity.getMakingPaste()) {
            scale = (float) teCounter / maxLife;
        }
        GL11.glTranslated(x, y, z);
        GL11.glTranslatef(-0.0005f, -0.0005f, -0.0005f);
        GL11.glScalef(1.001f, 1.001f, 1.001f);//Slightly Larger block to avoid z-fighting.
        GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);

        GL14.glBlendColor(1F, 1F, 1F, scale); //Set the alpha of the blocks we are rendering
        Block renderBlockState = ModBlocks.constructionBlockDense;
        if (renderBlockState == null) {
            renderBlockState = Blocks.cobblestone;
        }

        // TODO: Won't work
//        renderBlocks.renderBlockByRenderType(renderBlockState, (int) x, (int) y, (int) z);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity p_110775_1_) {
        return null;
    }
}
