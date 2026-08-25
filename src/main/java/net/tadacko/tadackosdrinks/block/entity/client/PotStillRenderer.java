package net.tadacko.tadackosdrinks.block.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import net.tadacko.tadackosdrinks.block.PotStillBlock;
import net.tadacko.tadackosdrinks.block.entity.PotStillBlockEntity;

public class PotStillRenderer implements BlockEntityRenderer<PotStillBlockEntity> {
    private static final float MIN_HEIGHT = 1f / 16f;
    private static final float MAX_HEIGHT = 7.99f / 16f;
    private static final int MAX_FLUID = 3000;

    public PotStillRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(PotStillBlockEntity blockEntity, float partialTick, PoseStack poseStack,
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

        // Height calculation
        float fluidHeight = MIN_HEIGHT;
        if (!fluid.isEmpty()) {
            float pct = (float) fluid.getAmount() / MAX_FLUID;
            fluidHeight = MIN_HEIGHT + pct * (MAX_HEIGHT - MIN_HEIGHT);
        }

        poseStack.pushPose();

        // --- SINGLE FLUID QUAD ---
        if (!fluid.isEmpty()) {
            VertexConsumer consumer = bufferSource.getBuffer(RenderType.translucent());

            FermentingBarrelRenderer.renderFluidQuad(consumer, poseStack.last(), sprite,
                    3/16f, fluidHeight, 3/16f,
                    13/16f, 13/16f,
                    red, green, blue, alpha,
                    combinedLight, combinedOverlay);
        }

        renderGlassWindow(blockEntity, poseStack, bufferSource, combinedLight, combinedOverlay);

        if (blockEntity.getBlockState().getValue(PotStillBlock.CLOCK)) {
            renderClockHand(blockEntity, poseStack, bufferSource, combinedLight, combinedOverlay);
        }

        poseStack.popPose();
    }

    // --- CLOCK HAND (simplified: no aging logic) ---
    private void renderClockHand(PotStillBlockEntity blockEntity, PoseStack poseStack,
                                 MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {

        // --- PIVOT (front of block) ---
        final float basePivotX = 8f / 16f;
        final float basePivotY = 4f / 16f;
        final float basePivotZ = 1.25f / 16f;

        BlockState state = blockEntity.getBlockState();
        Direction facing = state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                ? state.getValue(BlockStateProperties.HORIZONTAL_FACING)
                : Direction.SOUTH;

        // --- ROTATE PIVOT TO MATCH FACING ---
        final float centerX = 8f / 16f;
        final float centerZ = 8f / 16f;

        float offX = basePivotX - centerX;
        float offZ = basePivotZ - centerZ;

        int rot = switch (facing) {
            case EAST -> 1;
            case NORTH -> 2;
            case WEST -> 3;
            default -> 0;
        };

        for (int i = 0; i < rot; i++) {
            float nx = offZ;
            float nz = -offX;
            offX = nx;
            offZ = nz;
        }

        // FIX: correct side (this was flipped before)
        // remove the inversion that caused wrong side
        offX = -offX;
        offZ = -offZ;

        float pivotX = centerX + offX;
        float pivotY = basePivotY;
        float pivotZ = centerZ + offZ;

        // --- HAND DIMENSIONS (vertical) ---
        final float sizeX = 1f / 16f;
        final float sizeY = 2.5f / 16f; // length (UP)
        final float sizeZ = 0.5f / 16f;

        float denom = Math.max(1, blockEntity.getMaxProgress());
        float angle = -(blockEntity.getProgress() / denom) * 360f;

        poseStack.pushPose();

        // move to pivot
        poseStack.translate(pivotX, pivotY, pivotZ);

        // FIX: orient hand to block facing
        switch (facing) {
            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(0f));
            case NORTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180f));
            case EAST  -> poseStack.mulPose(Axis.YP.rotationDegrees(-90f));
            case WEST  -> poseStack.mulPose(Axis.YP.rotationDegrees(90f));
        }

        // --- CRITICAL FIX: rotate around axis perpendicular to face ---
        // this is the ONLY rotation we need
        switch (facing) {
            case NORTH, SOUTH -> poseStack.mulPose(Axis.ZP.rotationDegrees(angle));
            case EAST, WEST   -> poseStack.mulPose(Axis.ZN.rotationDegrees(angle));
        }

        // offset so hand starts BELOW pivot
        poseStack.translate(-sizeX / 2f, -0.5f / 16f, -sizeZ / 2f);

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.solid());
        float r = 215 / 255f, g = 0f, b = 0f, a = 1f;

        TextureAtlasSprite white = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(new ResourceLocation("minecraft", "block/white_concrete"));

        PoseStack.Pose pose = poseStack.last();

        float minX = 0f, minY = 0f, minZ = 0f;
        float maxX = sizeX, maxY = sizeY, maxZ = sizeZ;

        float uSize = (maxX - minX) * 16f;
        float vSize = (maxZ - minZ) * 16f;

        // --- ALL 6 FACES (unchanged) ---
        // Front
        consumer.vertex(pose.pose(), minX, minY, maxZ).color(r,g,b,a)
                .uv(white.getU(0f), white.getV(0f))
                .overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 0,0,1).endVertex();
        consumer.vertex(pose.pose(), maxX, minY, maxZ).color(r,g,b,a)
                .uv(white.getU(uSize), white.getV(0f))
                .overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 0,0,1).endVertex();
        consumer.vertex(pose.pose(), maxX, maxY, maxZ).color(r,g,b,a)
                .uv(white.getU(uSize), white.getV(vSize))
                .overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 0,0,1).endVertex();
        consumer.vertex(pose.pose(), minX, maxY, maxZ).color(r,g,b,a)
                .uv(white.getU(0f), white.getV(vSize))
                .overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 0,0,1).endVertex();

        // Back
        consumer.vertex(pose.pose(), maxX, minY, minZ).color(r,g,b,a)
                .uv(white.getU(0f), white.getV(0f))
                .overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 0,0,-1).endVertex();
        consumer.vertex(pose.pose(), minX, minY, minZ).color(r,g,b,a)
                .uv(white.getU(uSize), white.getV(0f))
                .overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 0,0,-1).endVertex();
        consumer.vertex(pose.pose(), minX, maxY, minZ).color(r,g,b,a)
                .uv(white.getU(uSize), white.getV(vSize))
                .overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 0,0,-1).endVertex();
        consumer.vertex(pose.pose(), maxX, maxY, minZ).color(r,g,b,a)
                .uv(white.getU(0f), white.getV(vSize))
                .overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 0,0,-1).endVertex();

        // Left
        consumer.vertex(pose.pose(), minX, minY, minZ).color(r,g,b,a)
                .uv(white.getU(0f), white.getV(0f))
                .overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), -1,0,0).endVertex();
        consumer.vertex(pose.pose(), minX, minY, maxZ).color(r,g,b,a)
                .uv(white.getU(uSize), white.getV(0f))
                .overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), -1,0,0).endVertex();
        consumer.vertex(pose.pose(), minX, maxY, maxZ).color(r,g,b,a)
                .uv(white.getU(uSize), white.getV(vSize))
                .overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), -1,0,0).endVertex();
        consumer.vertex(pose.pose(), minX, maxY, minZ).color(r,g,b,a)
                .uv(white.getU(0f), white.getV(vSize))
                .overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), -1,0,0).endVertex();

        // Right
        consumer.vertex(pose.pose(), maxX, minY, maxZ).color(r,g,b,a)
                .uv(white.getU(0f), white.getV(0f))
                .overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 1,0,0).endVertex();
        consumer.vertex(pose.pose(), maxX, minY, minZ).color(r,g,b,a)
                .uv(white.getU(uSize), white.getV(0f))
                .overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 1,0,0).endVertex();
        consumer.vertex(pose.pose(), maxX, maxY, minZ).color(r,g,b,a)
                .uv(white.getU(uSize), white.getV(vSize))
                .overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 1,0,0).endVertex();
        consumer.vertex(pose.pose(), maxX, maxY, maxZ).color(r,g,b,a)
                .uv(white.getU(0f), white.getV(vSize))
                .overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 1,0,0).endVertex();

        // Top
        consumer.vertex(pose.pose(), minX, maxY, maxZ).color(r,g,b,a)
                .uv(white.getU(0f), white.getV(0f))
                .overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 0,1,0).endVertex();
        consumer.vertex(pose.pose(), maxX, maxY, maxZ).color(r,g,b,a)
                .uv(white.getU(uSize), white.getV(0f))
                .overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 0,1,0).endVertex();
        consumer.vertex(pose.pose(), maxX, maxY, minZ).color(r,g,b,a)
                .uv(white.getU(uSize), white.getV(vSize))
                .overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 0,1,0).endVertex();
        consumer.vertex(pose.pose(), minX, maxY, minZ).color(r,g,b,a)
                .uv(white.getU(0f), white.getV(vSize))
                .overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 0,1,0).endVertex();

        // Bottom
        consumer.vertex(pose.pose(), minX, minY, minZ).color(r,g,b,a)
                .uv(white.getU(0f), white.getV(0f))
                .overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 0,-1,0).endVertex();
        consumer.vertex(pose.pose(), maxX, minY, minZ).color(r,g,b,a)
                .uv(white.getU(uSize), white.getV(0f))
                .overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 0,-1,0).endVertex();
        consumer.vertex(pose.pose(), maxX, minY, maxZ).color(r,g,b,a)
                .uv(white.getU(uSize), white.getV(vSize))
                .overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 0,-1,0).endVertex();
        consumer.vertex(pose.pose(), minX, minY, maxZ).color(r,g,b,a)
                .uv(white.getU(0f), white.getV(vSize))
                .overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 0,-1,0).endVertex();

        poseStack.popPose();
    }

    private void renderGlassWindow(PotStillBlockEntity blockEntity, PoseStack poseStack,
                                   MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {

        // --- bounds ---
        float minX = 7f / 16f;
        float maxX = 9f / 16f;
        float minY = 9f / 16f;
        float maxY = 11f / 16f;
        float minZ = 4f / 16f;
        float maxZ = 5f / 16f;

        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(new ResourceLocation("minecraft", "block/light_blue_stained_glass"));

        float u0 = sprite.getU(1);
        float u1 = sprite.getU(2);
        float v0 = sprite.getV(1);
        float v1 = sprite.getV(2);

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.translucent());

        BlockState state = blockEntity.getBlockState();
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);

        poseStack.pushPose();

        // --- rotate with block ---
        poseStack.translate(0.5f, 0f, 0.5f);

        switch (facing) {
            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(0f));
            case NORTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180f));
            case EAST  -> poseStack.mulPose(Axis.YP.rotationDegrees(-90f));
            case WEST  -> poseStack.mulPose(Axis.YP.rotationDegrees(90f));
        }

        poseStack.translate(-0.5f, 0f, -0.5f);

        // --- FIX: flip Z for north/south ---
        if (facing == Direction.NORTH || facing == Direction.SOUTH) {
            float newMinZ = 1f - maxZ;
            float newMaxZ = 1f - minZ;
            minZ = newMinZ;
            maxZ = newMaxZ;
        }

        PoseStack.Pose pose = poseStack.last();

        float r = 1f, g = 1f, b = 1f, a = 1f;

        // --- NORTH ---
        consumer.vertex(pose.pose(), minX, minY, minZ).color(r,g,b,a).uv(u0,v1)
                .overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 0,0,-1).endVertex();
        consumer.vertex(pose.pose(), maxX, minY, minZ).color(r,g,b,a).uv(u1,v1)
                .overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 0,0,-1).endVertex();
        consumer.vertex(pose.pose(), maxX, maxY, minZ).color(r,g,b,a).uv(u1,v0)
                .overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 0,0,-1).endVertex();
        consumer.vertex(pose.pose(), minX, maxY, minZ).color(r,g,b,a).uv(u0,v0)
                .overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 0,0,-1).endVertex();

        // --- SOUTH ---
        consumer.vertex(pose.pose(), minX, minY, maxZ).color(r,g,b,a).uv(u0,v1)
                .overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 0,0,1).endVertex();
        consumer.vertex(pose.pose(), minX, maxY, maxZ).color(r,g,b,a).uv(u0,v0)
                .overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 0,0,1).endVertex();
        consumer.vertex(pose.pose(), maxX, maxY, maxZ).color(r,g,b,a).uv(u1,v0)
                .overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 0,0,1).endVertex();
        consumer.vertex(pose.pose(), maxX, minY, maxZ).color(r,g,b,a).uv(u1,v1)
                .overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 0,0,1).endVertex();

        // --- TOP ---
        consumer.vertex(pose.pose(), minX, maxY, minZ).color(r,g,b,a).uv(u0,v1)
                .overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 0,1,0).endVertex();
        consumer.vertex(pose.pose(), minX, maxY, maxZ).color(r,g,b,a).uv(u0,v0)
                .overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 0,1,0).endVertex();
        consumer.vertex(pose.pose(), maxX, maxY, maxZ).color(r,g,b,a).uv(u1,v0)
                .overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 0,1,0).endVertex();
        consumer.vertex(pose.pose(), maxX, maxY, minZ).color(r,g,b,a).uv(u1,v1)
                .overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 0,1,0).endVertex();

        // --- BOTTOM ---
        consumer.vertex(pose.pose(), minX, minY, minZ).color(r,g,b,a).uv(u0,v1)
                .overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 0,-1,0).endVertex();
        consumer.vertex(pose.pose(), minX, minY, maxZ).color(r,g,b,a).uv(u0,v0)
                .overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 0,-1,0).endVertex();
        consumer.vertex(pose.pose(), maxX, minY, maxZ).color(r,g,b,a).uv(u1,v0)
                .overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 0,-1,0).endVertex();
        consumer.vertex(pose.pose(), maxX, minY, minZ).color(r,g,b,a).uv(u1,v1)
                .overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 0,-1,0).endVertex();

        poseStack.popPose();
    }
}