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
import net.tadacko.tadackosdrinks.block.ColumnStillBlock;
import net.tadacko.tadackosdrinks.block.entity.ColumnStillBlockEntity;

public class ColumnStillRenderer implements BlockEntityRenderer<ColumnStillBlockEntity> {

    // Window bounds (local to a single segment, defined for default SOUTH facing, then rotated)
    private static final float WINDOW_MIN_X = 7f / 16f;
    private static final float WINDOW_MAX_X = 9f / 16f;
    private static final float LOWER_WINDOW_MIN_Y = 4f / 16f;
    private static final float LOWER_WINDOW_MAX_Y = 6f / 16f;
    private static final float UPPER_WINDOW_MIN_Y = 10f / 16f;
    private static final float UPPER_WINDOW_MAX_Y = 12f / 16f;
    private static final float WINDOW_Z = 2f / 16f;
    private static final float FLUID_Z = 3f / 16f;

    // Fluid surface caps, rendered at the fluid's current fill height (perpendicular to the front gauge quad)
    private static final float SURFACE_NEAR_MIN_Z = 3f / 16f;
    private static final float SURFACE_NEAR_MAX_Z = 4f / 16f;
    private static final float SURFACE_FAR_MIN_X = 4f / 16f;
    private static final float SURFACE_FAR_MAX_X = 12f / 16f;
    private static final float SURFACE_FAR_MIN_Z = 4f / 16f;
    private static final float SURFACE_FAR_MAX_Z = 12f / 16f;

    private static final ResourceLocation GLASS_TEXTURE = new ResourceLocation("block/light_blue_stained_glass");

    public ColumnStillRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(ColumnStillBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {

        BlockState state = blockEntity.getBlockState();

        // Guard: only the master (bottom, SEGMENT 0) renders the column. Without this, a stray
        // block entity left behind mid-merge (its cached state already updated to the new HEIGHT/
        // SEGMENT, but not yet removed by ColumnStillBlock#syncBlockEntities) would render a whole
        // extra column's worth of geometry starting from its own - now wrong - position.
        if (state.hasProperty(ColumnStillBlock.SEGMENT) && state.getValue(ColumnStillBlock.SEGMENT) != 0) {
            return;
        }

        Direction facing = state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                ? state.getValue(BlockStateProperties.HORIZONTAL_FACING)
                : Direction.SOUTH;
        int height = state.hasProperty(ColumnStillBlock.HEIGHT) ? state.getValue(ColumnStillBlock.HEIGHT) : 1;

        FluidStack fluid = blockEntity.getFluid();
        TextureAtlasSprite fluidSprite = null;
        float fr = 1f, fg = 1f, fb = 1f, fa = 1f;

        if (!fluid.isEmpty()) {
            var fluidExt = IClientFluidTypeExtensions.of(fluid.getFluid());
            fluidSprite = Minecraft.getInstance()
                    .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                    .apply(fluidExt.getStillTexture());

            int color = fluidExt.getTintColor(fluid);
            fr = ((color >> 16) & 0xFF) / 255f;
            fg = ((color >> 8) & 0xFF) / 255f;
            fb = (color & 0xFF) / 255f;
            fa = ((color >> 24) & 0xFF) / 255f;
        }

        float pct = blockEntity.getFillPercent();

        TextureAtlasSprite glassSprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(GLASS_TEXTURE);

        for (int i = 0; i < height; i++) {
            boolean isBottom = i == 0;
            boolean isTop = i == height - 1;

            // Bottom (including the single-block edge case, isBottom && isTop) shows only the upper
            // window. A true top-only segment shows only the lower window. Middle segments show both.
            boolean showUpper;
            boolean showLower;
            if (isBottom) {
                showUpper = true;
                showLower = false;
            } else if (isTop) {
                showUpper = false;
                showLower = true;
            } else {
                showUpper = true;
                showLower = true;
            }

            poseStack.pushPose();
            poseStack.translate(0, i, 0);

            // Rotate the segment's local geometry to match block facing (coords above are defined for SOUTH)
            poseStack.translate(0.5f, 0f, 0.5f);
            switch (facing) {
                case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180f));
                case NORTH -> poseStack.mulPose(Axis.YP.rotationDegrees(0f));
                case EAST -> poseStack.mulPose(Axis.YP.rotationDegrees(-90f));
                case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(90f));
            }
            poseStack.translate(-0.5f, 0f, -0.5f);

            PoseStack.Pose pose = poseStack.last();

            if (fluidSprite != null) {
                VertexConsumer fluidConsumer = FermentingBarrelRenderer.fluidTranslucent ? bufferSource.getBuffer(RenderType.translucent()) :
                        bufferSource.getBuffer(RenderType.solid());

                if (showLower) {
                    renderFluidForWindow(fluidConsumer, pose, fluidSprite,
                            LOWER_WINDOW_MIN_Y, LOWER_WINDOW_MAX_Y, pct,
                            fr, fg, fb, fa, combinedLight, combinedOverlay);
                }
                if (showUpper) {
                    renderFluidForWindow(fluidConsumer, pose, fluidSprite,
                            UPPER_WINDOW_MIN_Y, UPPER_WINDOW_MAX_Y, pct,
                            fr, fg, fb, fa, combinedLight, combinedOverlay);
                }
            }

            VertexConsumer glassConsumer = bufferSource.getBuffer(RenderType.translucent());

            if (showLower) {
                renderGlassQuad(glassConsumer, pose, glassSprite,
                        WINDOW_MIN_X, LOWER_WINDOW_MIN_Y, WINDOW_MAX_X, LOWER_WINDOW_MAX_Y, WINDOW_Z,
                        combinedLight, combinedOverlay);
            }
            if (showUpper) {
                renderGlassQuad(glassConsumer, pose, glassSprite,
                        WINDOW_MIN_X, UPPER_WINDOW_MIN_Y, WINDOW_MAX_X, UPPER_WINDOW_MAX_Y, WINDOW_Z,
                        combinedLight, combinedOverlay);
            }

            poseStack.popPose();
        }

        // Clock is only meaningful on the bottom segment - render once at the BE's own (untranslated) pose
        if (state.hasProperty(ColumnStillBlock.CLOCK) && state.getValue(ColumnStillBlock.CLOCK)) {
            renderClockHand(blockEntity, poseStack, bufferSource, combinedLight, combinedOverlay);
        }
    }

    @Override
    public int getViewDistance() {
        return 128;
    }

    /** Front gauge quad plus its two perpendicular surface caps, all at the window's current fill height. */
    private void renderFluidForWindow(VertexConsumer consumer, PoseStack.Pose pose, TextureAtlasSprite sprite,
                                      float windowMinY, float windowMaxY, float pct,
                                      float red, float green, float blue, float alpha,
                                      int combinedLight, int combinedOverlay) {

        float fillY = windowMinY + pct * (windowMaxY - windowMinY);
        if (fillY <= windowMinY) return; // empty - nothing to draw for this window

        // Vertical gauge, front-facing, behind the glass
        renderFluidQuad(consumer, pose, sprite,
                WINDOW_MIN_X, windowMinY, WINDOW_MAX_X, fillY, FLUID_Z,
                red, green, blue, alpha, combinedLight, combinedOverlay);

        // Surface cap 1: narrow, right behind the gauge (matches window width)
        FermentingBarrelRenderer.renderFluidQuad(consumer, pose, sprite,
                WINDOW_MIN_X, fillY, SURFACE_NEAR_MIN_Z, WINDOW_MAX_X, SURFACE_NEAR_MAX_Z,
                red, green, blue, alpha, combinedLight, combinedOverlay);

        // Surface cap 2: wide, representing the full interior tube cross-section
        FermentingBarrelRenderer.renderFluidQuad(consumer, pose, sprite,
                SURFACE_FAR_MIN_X, fillY, SURFACE_FAR_MIN_Z, SURFACE_FAR_MAX_X, SURFACE_FAR_MAX_Z,
                red, green, blue, alpha, combinedLight, combinedOverlay);
    }

    /** Glass window: fixed tiny interior UV sample (u1-2, v1-2), matching the pot still's convention. */
    private void renderGlassQuad(VertexConsumer consumer, PoseStack.Pose pose, TextureAtlasSprite sprite,
                                 float minX, float minY, float maxX, float maxY, float z,
                                 int combinedLight, int combinedOverlay) {
        float u0 = sprite.getU(1);
        float u1 = sprite.getU(2);
        float v0 = sprite.getV(1);
        float v1 = sprite.getV(2);
        renderDoubleSidedQuad(consumer, pose, minX, minY, maxX, maxY, z, u0, u1, v0, v1,
                1f, 1f, 1f, 1f, combinedLight, combinedOverlay);
    }

    /** Vertical fluid gauge: UV proportional to quad size, same convention as the pot still's fluid quad. */
    private void renderFluidQuad(VertexConsumer consumer, PoseStack.Pose pose, TextureAtlasSprite sprite,
                                 float minX, float minY, float maxX, float maxY, float z,
                                 float red, float green, float blue, float alpha,
                                 int combinedLight, int combinedOverlay) {
        float uSize = (maxX - minX) * 16f;
        float vSize = (maxY - minY) * 16f;
        float u0 = sprite.getU(0);
        float u1 = sprite.getU(uSize);
        float v0 = sprite.getV(0);
        float v1 = sprite.getV(vSize);
        renderDoubleSidedQuad(consumer, pose, minX, minY, maxX, maxY, z, u0, u1, v0, v1,
                red, green, blue, alpha, combinedLight, combinedOverlay);
    }

    /**
     * Flat quad rendered with both windings/normals (0,0,-1) and (0,0,1) so it isn't back-face culled
     * by RenderType.translucent() regardless of which side of the block the camera is on.
     */
    private void renderDoubleSidedQuad(VertexConsumer consumer, PoseStack.Pose pose,
                                       float minX, float minY, float maxX, float maxY, float z,
                                       float u0, float u1, float v0, float v1,
                                       float red, float green, float blue, float alpha,
                                       int combinedLight, int combinedOverlay) {

        // Winding A -> normal (0,0,-1)
        consumer.vertex(pose.pose(), minX, minY, z).color(red, green, blue, alpha)
                .uv(u0, v1).overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 0, 0, -1).endVertex();
        consumer.vertex(pose.pose(), maxX, minY, z).color(red, green, blue, alpha)
                .uv(u1, v1).overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 0, 0, -1).endVertex();
        consumer.vertex(pose.pose(), maxX, maxY, z).color(red, green, blue, alpha)
                .uv(u1, v0).overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 0, 0, -1).endVertex();
        consumer.vertex(pose.pose(), minX, maxY, z).color(red, green, blue, alpha)
                .uv(u0, v0).overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 0, 0, -1).endVertex();

        // Winding B (reversed) -> normal (0,0,1)
        consumer.vertex(pose.pose(), minX, maxY, z).color(red, green, blue, alpha)
                .uv(u0, v0).overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 0, 0, 1).endVertex();
        consumer.vertex(pose.pose(), maxX, maxY, z).color(red, green, blue, alpha)
                .uv(u1, v0).overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 0, 0, 1).endVertex();
        consumer.vertex(pose.pose(), maxX, minY, z).color(red, green, blue, alpha)
                .uv(u1, v1).overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 0, 0, 1).endVertex();
        consumer.vertex(pose.pose(), minX, minY, z).color(red, green, blue, alpha)
                .uv(u0, v1).overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 0, 0, 1).endVertex();
    }

    // --- CLOCK HAND (copied from PotStillRenderer) ---
    private void renderClockHand(ColumnStillBlockEntity blockEntity, PoseStack poseStack,
                                 MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {

        final float basePivotX = 8f / 16f;
        final float basePivotY = 4f / 16f;
        final float basePivotZ = 1.25f / 16f;

        BlockState state = blockEntity.getBlockState();
        Direction facing = state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                ? state.getValue(BlockStateProperties.HORIZONTAL_FACING)
                : Direction.SOUTH;

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

        offX = -offX;
        offZ = -offZ;

        float pivotX = centerX + offX;
        float pivotY = basePivotY;
        float pivotZ = centerZ + offZ;

        final float sizeX = 1f / 16f;
        final float sizeY = 2.5f / 16f;
        final float sizeZ = 0.5f / 16f;

        float denom = Math.max(1, blockEntity.getMaxProgress());
        float angle = -(blockEntity.getProgress() / denom) * 360f;

        poseStack.pushPose();

        poseStack.translate(pivotX, pivotY, pivotZ);

        switch (facing) {
            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(0f));
            case NORTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180f));
            case EAST -> poseStack.mulPose(Axis.YP.rotationDegrees(-90f));
            case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(90f));
        }

        switch (facing) {
            case NORTH, SOUTH -> poseStack.mulPose(Axis.ZP.rotationDegrees(angle));
            case EAST, WEST -> poseStack.mulPose(Axis.ZN.rotationDegrees(angle));
        }

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
}