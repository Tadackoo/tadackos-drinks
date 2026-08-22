package net.tadacko.tadackosdrinks.block.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import net.tadacko.tadackosdrinks.block.entity.CopperPotBlockEntity;

public class CopperPotRenderer implements BlockEntityRenderer<CopperPotBlockEntity> {

    private static final float FLUID_HEIGHT = 14f / 16f;

    public CopperPotRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(CopperPotBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {

        FluidStack fluid = blockEntity.getFluid();

        TextureAtlasSprite sprite = null;
        float red = 1f, green = 1f, blue = 1f, alpha = 1f;

        if (!fluid.isEmpty()) {
            var fluidExt = IClientFluidTypeExtensions.of(fluid.getFluid());
            sprite = Minecraft.getInstance()
                    .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                    .apply(fluidExt.getStillTexture());

            int color = fluidExt.getTintColor(fluid);
            red   = ((color >> 16) & 0xFF) / 255f;
            green = ((color >> 8) & 0xFF) / 255f;
            blue  = (color & 0xFF) / 255f;
            alpha = ((color >> 24) & 0xFF) / 255f;
        }

        poseStack.pushPose();

        // --- SINGLE FLUID QUAD ---
        if (!fluid.isEmpty()) {
            VertexConsumer consumer = bufferSource.getBuffer(RenderType.translucent());

            renderFluidQuad(consumer, poseStack, sprite,
                    1/16f, FLUID_HEIGHT, 1/16f,
                    15/16f, FLUID_HEIGHT, 15/16f,
                    red, green, blue, alpha,
                    combinedLight, combinedOverlay);
        }

        poseStack.popPose();
    }

    // --- SHARED FLUID QUAD (unchanged, reusable) ---
    private void renderFluidQuad(VertexConsumer consumer, PoseStack poseStack, TextureAtlasSprite sprite,
                                 float minX, float y, float minZ,
                                 float maxX, float y2, float maxZ,
                                 float red, float green, float blue, float alpha,
                                 int combinedLight, int combinedOverlay) {

        PoseStack.Pose pose = poseStack.last();

        float uSize = (maxX - minX) * 16f;
        float vSize = (maxZ - minZ) * 16f;

        consumer.vertex(pose.pose(), minX, y, minZ)
                .color(red, green, blue, alpha)
                .uv(sprite.getU(0), sprite.getV(0))
                .overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 0, 1, 0).endVertex();

        consumer.vertex(pose.pose(), minX, y, maxZ)
                .color(red, green, blue, alpha)
                .uv(sprite.getU(0), sprite.getV(vSize))
                .overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 0, 1, 0).endVertex();

        consumer.vertex(pose.pose(), maxX, y, maxZ)
                .color(red, green, blue, alpha)
                .uv(sprite.getU(uSize), sprite.getV(vSize))
                .overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 0, 1, 0).endVertex();

        consumer.vertex(pose.pose(), maxX, y, minZ)
                .color(red, green, blue, alpha)
                .uv(sprite.getU(uSize), sprite.getV(0))
                .overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 0, 1, 0).endVertex();
    }
}