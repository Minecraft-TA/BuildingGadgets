package com.direwolf20.buildinggadgets.common.entities;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.blocks.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import javax.annotation.Nullable;


public class BlockBuildEntityRender extends Render {

    public BlockBuildEntityRender() {
        this.shadowSize = 0F;
    }

    @Override
    public void doRender(Entity e, double x, double y, double z, float entityYaw, float partialTicks) {
        BlockBuildEntity entity = (BlockBuildEntity) e;
        RenderBlocks renderBlocks = new RenderBlocks(e.worldObj);
        Minecraft mc = Minecraft.getMinecraft();
        GL11.glPushMatrix();

        int toolMode = entity.getToolMode();
        mc.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
        int teCounter = entity.ticksExisted;
        int maxLife = entity.maxLife;
        if (teCounter > maxLife) {
            teCounter = maxLife;
        }
        float scale = (float) (teCounter) / (float) maxLife;
        if (scale >= 1.0f) {
            scale = 0.99f;
        }
        if (toolMode == 2 || toolMode == 3) {
            scale = (float) (maxLife - teCounter) / maxLife;
        }
        float trans = (1 - scale) / 2;
        GL11.glTranslated(x, y, z);
        GL11.glTranslatef(trans, trans, trans);
        GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
        GL11.glScalef(scale, scale, scale);


        //IBlockState renderBlockState = blocks.COBBLESTONE.getDefaultState();

        Block renderBlockState = entity.getSetBlock().getBlock();
        if (entity.getUsingConstructionPaste() && toolMode == 1) {
            renderBlockState = ModBlocks.constructionBlock;
        }
        if (renderBlockState == null) {
            renderBlockState = Blocks.cobblestone;
        }
        try {
            // TODO: Won't work
//            renderBlocks.renderBlockByRenderType(renderBlockState, (int) x, (int) y, (int) z);
        } catch (Throwable t) {
            Tessellator tessellator = Tessellator.instance;
            try {
                // If the buffer is already not drawing then it'll throw
                // and IllegalStateException... Very rare
//                bufferBuilder.finishDrawing();
            } catch (IllegalStateException ex) {
                BuildingGadgets.logger.error(getClass().getSimpleName() + ": Error within rendering method -> " + ex);
            }
        }
        GL11.glPopMatrix();


        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT); // TODO: Could be wrong, but 8256 (0x2040) doesn't exist

        GL11.glEnable(GL11.GL_BLEND);
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDepthMask(false);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        double minX = x;
        double minY = y;
        double minZ = z;
        double maxX = x + 1;
        double maxY = y + 1;
        double maxZ = z + 1;
        float red = 0f;
        float green = 1f;
        float blue = 1f;
        if (toolMode == 2 || toolMode == 3) {
            red = 1f;
            green = 0.25f;
            blue = 0.25f;
        }
        float alpha = (1f - (scale));
        if (alpha < 0.051f) {
            alpha = 0.051f;
        }
        if (alpha > 0.33f) {
            alpha = 0.33f;
        }
        //down
        tessellator.setColorRGBA_F(red, green, blue, alpha);
        tessellator.addVertex(minX, minY, minZ);
        tessellator.setColorRGBA_F(red, green, blue, alpha);
        tessellator.addVertex(maxX, minY, minZ);
        tessellator.setColorRGBA_F(red, green, blue, alpha);
        tessellator.addVertex(maxX, minY, maxZ);
        tessellator.setColorRGBA_F(red, green, blue, alpha);
        tessellator.addVertex(minX, minY, maxZ);

        //up
        tessellator.setColorRGBA_F(red, green, blue, alpha);
        tessellator.addVertex(minX, maxY, minZ);
        tessellator.setColorRGBA_F(red, green, blue, alpha);
        tessellator.addVertex(minX, maxY, maxZ);
        tessellator.setColorRGBA_F(red, green, blue, alpha);
        tessellator.addVertex(maxX, maxY, maxZ);
        tessellator.setColorRGBA_F(red, green, blue, alpha);
        tessellator.addVertex(maxX, maxY, minZ);

        //north
        tessellator.setColorRGBA_F(red, green, blue, alpha);
        tessellator.addVertex(minX, minY, minZ);
        tessellator.setColorRGBA_F(red, green, blue, alpha);
        tessellator.addVertex(minX, maxY, minZ);
        tessellator.setColorRGBA_F(red, green, blue, alpha);
        tessellator.addVertex(maxX, maxY, minZ);
        tessellator.setColorRGBA_F(red, green, blue, alpha);
        tessellator.addVertex(maxX, minY, minZ);

        //south
        tessellator.setColorRGBA_F(red, green, blue, alpha);
        tessellator.addVertex(minX, minY, maxZ);
        tessellator.setColorRGBA_F(red, green, blue, alpha);
        tessellator.addVertex(maxX, minY, maxZ);
        tessellator.setColorRGBA_F(red, green, blue, alpha);
        tessellator.addVertex(maxX, maxY, maxZ);
        tessellator.setColorRGBA_F(red, green, blue, alpha);
        tessellator.addVertex(minX, maxY, maxZ);

        //east
        tessellator.setColorRGBA_F(red, green, blue, alpha);
        tessellator.addVertex(maxX, minY, minZ);
        tessellator.setColorRGBA_F(red, green, blue, alpha);
        tessellator.addVertex(maxX, maxY, minZ);
        tessellator.setColorRGBA_F(red, green, blue, alpha);
        tessellator.addVertex(maxX, maxY, maxZ);
        tessellator.setColorRGBA_F(red, green, blue, alpha);
        tessellator.addVertex(maxX, minY, maxZ);

        //west
        tessellator.setColorRGBA_F(red, green, blue, alpha);
        tessellator.addVertex(minX, minY, minZ);
        tessellator.setColorRGBA_F(red, green, blue, alpha);
        tessellator.addVertex(minX, minY, maxZ);
        tessellator.setColorRGBA_F(red, green, blue, alpha);
        tessellator.addVertex(minX, maxY, maxZ);
        tessellator.setColorRGBA_F(red, green, blue, alpha);
        tessellator.addVertex(minX, maxY, minZ);
        tessellator.draw();

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDepthMask(true);

        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return null;
    }
}
