package net.tadacko.tadackosdrinks.block.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import net.tadacko.tadackosdrinks.block.entity.ManualPressBlockEntity;
import org.joml.Matrix4f;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class ManualPressRenderer implements BlockEntityRenderer<ManualPressBlockEntity> {
    private final GeoBlockRenderer<ManualPressBlockEntity> geoRenderer;

    public ManualPressRenderer(BlockEntityRendererProvider.Context context) {
        this.geoRenderer = new GeoBlockRenderer<>(new ManualPressModel());
    }

    @Override
    public void render(ManualPressBlockEntity blockEntity, float partialTicks, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        // First render the GeckoLib animated model
        geoRenderer.render(blockEntity, partialTicks, poseStack, bufferSource, packedLight, packedOverlay);

        // Then render the fluid on top
        renderFluid(blockEntity, partialTicks, poseStack, bufferSource, packedLight, packedOverlay);
    }

    private void renderFluid(ManualPressBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                             MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        FluidStack fluidStack = blockEntity.getFluidTank().getFluid();

        if (fluidStack.isEmpty()) {
            return;
        }

        Fluid fluid = fluidStack.getFluid();
        IClientFluidTypeExtensions fluidTypeExtensions = IClientFluidTypeExtensions.of(fluid);
        TextureAtlasSprite sprite = net.minecraft.client.Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(fluidTypeExtensions.getStillTexture());

        int color = fluidTypeExtensions.getTintColor(fluidStack);
        float red = ((color >> 16) & 0xFF) / 255f;
        float green = ((color >> 8) & 0xFF) / 255f;
        float blue = (color & 0xFF) / 255f;
        float alpha = ((color >> 24) & 0xFF) / 255f;

        // Calculate the squish factor based on processing time
        float squishFactor = calculateSquishFactor(blockEntity, partialTick);

        poseStack.pushPose();

        // Center the fluid block (12x12x4 centered in a 16x16x16 block)
        float minX = 2f / 16f;
        float maxX = 14f / 16f;
        float minZ = 2f / 16f;
        float maxZ = 14f / 16f;
        float minY = 2f / 16f;
        float maxY = minY + (4f / 16f) * squishFactor; // Height decreases as it's squished

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.translucent());
        Matrix4f matrix = poseStack.last().pose();

        // Render all 6 faces of the fluid cube
        // Bottom face (Y-)
        renderQuad(vertexConsumer, matrix,
                minX, minY, minZ,
                maxX, minY, minZ,
                maxX, minY, maxZ,
                minX, minY, maxZ,
                sprite, red, green, blue, alpha, combinedLight, 0, -1, 0);

        // Top face (Y+) - only render if height > 0
        if (maxY > 0.001f) {
            renderQuad(vertexConsumer, matrix,
                    minX, maxY, minZ,
                    minX, maxY, maxZ,
                    maxX, maxY, maxZ,
                    maxX, maxY, minZ,
                    sprite, red, green, blue, alpha, combinedLight, 0, 1, 0);
        }

        // North face (Z-) - reversed vertex order
        if (maxY > 0.001f) {
            renderQuad(vertexConsumer, matrix,
                    minX, maxY, minZ,
                    maxX, maxY, minZ,
                    maxX, minY, minZ,
                    minX, minY, minZ,
                    sprite, red, green, blue, alpha, combinedLight, 0, 0, -1);
        }

        // South face (Z+) - reversed vertex order
        if (maxY > 0.001f) {
            renderQuad(vertexConsumer, matrix,
                    maxX, maxY, maxZ,
                    minX, maxY, maxZ,
                    minX, minY, maxZ,
                    maxX, minY, maxZ,
                    sprite, red, green, blue, alpha, combinedLight, 0, 0, 1);
        }

        // West face (X-) - reversed vertex order
        if (maxY > 0.001f) {
            renderQuad(vertexConsumer, matrix,
                    minX, maxY, maxZ,
                    minX, maxY, minZ,
                    minX, minY, minZ,
                    minX, minY, maxZ,
                    sprite, red, green, blue, alpha, combinedLight, -1, 0, 0);
        }

        // East face (X+) - reversed vertex order
        if (maxY > 0.001f) {
            renderQuad(vertexConsumer, matrix,
                    maxX, maxY, minZ,
                    maxX, maxY, maxZ,
                    maxX, minY, maxZ,
                    maxX, minY, minZ,
                    sprite, red, green, blue, alpha, combinedLight, 1, 0, 0);
        }

        poseStack.popPose();
    }

    private float calculateSquishFactor(ManualPressBlockEntity blockEntity, float partialTick) {
        if (!blockEntity.isProcessing() && !blockEntity.isReturning()) {
            return 1.0f; // Full height when idle
        }

        float currentTime = blockEntity.getProgress() + partialTick;
        float timeToBottom = 55f;
        // total time = 120f

        if (currentTime <= timeToBottom) {
            // Forward animation: squish from 1.0 to 0.0
            return 1.0f - (currentTime / timeToBottom);
        } else {
            return 0f;
        }
    }

    private void renderQuad(VertexConsumer consumer, Matrix4f matrix,
                            float x0, float y0, float z0,
                            float x1, float y1, float z1,
                            float x2, float y2, float z2,
                            float x3, float y3, float z3,
                            TextureAtlasSprite sprite,
                            float r, float g, float b, float a,
                            int light, float normalX, float normalY, float normalZ) {

        float minU = sprite.getU0();
        float maxU = sprite.getU1();
        float minV = sprite.getV0();
        float maxV = sprite.getV1();

        consumer.vertex(matrix, x0, y0, z0).color(r, g, b, a).uv(minU, minV).uv2(light).normal(normalX, normalY, normalZ).endVertex();
        consumer.vertex(matrix, x1, y1, z1).color(r, g, b, a).uv(maxU, minV).uv2(light).normal(normalX, normalY, normalZ).endVertex();
        consumer.vertex(matrix, x2, y2, z2).color(r, g, b, a).uv(maxU, maxV).uv2(light).normal(normalX, normalY, normalZ).endVertex();
        consumer.vertex(matrix, x3, y3, z3).color(r, g, b, a).uv(minU, maxV).uv2(light).normal(normalX, normalY, normalZ).endVertex();
    }
}