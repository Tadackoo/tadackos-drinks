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
import net.tadacko.tadackosdrinks.block.FermentingBarrelBlock;
import net.tadacko.tadackosdrinks.block.entity.FermentingBarrelBlockEntity;
import net.tadacko.tadackosdrinks.block.BarrelState;

public class FermentingBarrelRenderer implements BlockEntityRenderer<FermentingBarrelBlockEntity> {
    private static final float MIN_HEIGHT = 2f / 16f;  // Bottom of barrel interior
    private static final float MAX_HEIGHT = 14f / 16f; // Top liquid level
    private static final int MAX_FLUID = 1000;

    public static boolean fluidTranslucent = true; // fallback default, overridden by config value

    public FermentingBarrelRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(FermentingBarrelBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        FluidStack fluid = blockEntity.getFluid();

        // Get fluid texture (used for fluid top rendering)
        IClientFluidTypeExtensions fluidTypeExtensions = null;
        TextureAtlasSprite sprite = null;
        int color = 0xFFFFFFFF;
        float red = 1f, green = 1f, blue = 1f, alpha = 1f;
        if (!fluid.isEmpty()) {
            fluidTypeExtensions = IClientFluidTypeExtensions.of(fluid.getFluid());
            ResourceLocation stillTexture = fluidTypeExtensions.getStillTexture();
            sprite = Minecraft.getInstance()
                    .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                    .apply(stillTexture);

            // Get fluid color
            color = fluidTypeExtensions.getTintColor(fluid);
            red = ((color >> 16) & 0xFF) / 255f;
            green = ((color >> 8) & 0xFF) / 255f;
            blue = (color & 0xFF) / 255f;
            alpha = ((color >> 24) & 0xFF) / 255f;
        }

        // Calculate fluid height based on amount
        float fluidHeight = MIN_HEIGHT;
        if (!fluid.isEmpty()) {
            float fluidPercentage = (float) fluid.getAmount() / MAX_FLUID;
            fluidHeight = MIN_HEIGHT + (fluidPercentage * (MAX_HEIGHT - MIN_HEIGHT));
        }

        poseStack.pushPose();

        if (!fluid.isEmpty()) {
            VertexConsumer consumer = fluidTranslucent ? bufferSource.getBuffer(RenderType.translucent()) :
                    bufferSource.getBuffer(RenderType.solid());
            PoseStack.Pose pose = poseStack.last();

            // Render only the top surface of the fluid at calculated height
            // Main center liquid surface
            renderFluidQuad(consumer, pose, sprite,
                    3/16f, fluidHeight, 3/16f,
                    13/16f, 13/16f,
                    red, green, blue, alpha, combinedLight, combinedOverlay);

            // Left edge
            renderFluidQuad(consumer, pose, sprite,
                    2/16f, fluidHeight, 4/16f,
                    3/16f, 12/16f,
                    red, green, blue, alpha, combinedLight, combinedOverlay);

            // Bottom edge
            renderFluidQuad(consumer, pose, sprite,
                    4/16f, fluidHeight, 13/16f,
                    12/16f, 14/16f,
                    red, green, blue, alpha, combinedLight, combinedOverlay);

            // Right edge
            renderFluidQuad(consumer, pose, sprite,
                    13/16f, fluidHeight, 4/16f,
                    14/16f, 12/16f,
                    red, green, blue, alpha, combinedLight, combinedOverlay);

            // Top edge
            renderFluidQuad(consumer, pose, sprite,
                    4/16f, fluidHeight, 2/16f,
                    12/16f, 3/16f,
                    red, green, blue, alpha, combinedLight, combinedOverlay);
        }

        // Only render the clock hand if the blockstate's CLOCK is true and STATE is CLOSED or YEAST
        BlockState blockState = blockEntity.getBlockState();

        // Safety: if the blockState doesn't contain the property for some reason, skip
        if (blockState.hasProperty(FermentingBarrelBlock.CLOCK) && blockState.getValue(FermentingBarrelBlock.CLOCK)) {
            // check the STATE enum property
            BarrelState stateValue = blockState.getValue(FermentingBarrelBlock.STATE);
            if (stateValue == BarrelState.CLOSED || stateValue == BarrelState.YEAST) {
                renderClockHand(blockEntity, poseStack, bufferSource, combinedLight, combinedOverlay);
            }
        }

        poseStack.popPose();
    }

    public static void renderFluidQuad(VertexConsumer consumer, PoseStack.Pose pose, TextureAtlasSprite sprite,
                                 float minX, float y, float minZ,
                                 float maxX, float maxZ,
                                 float red, float green, float blue, float alpha,
                                 int combinedLight, int combinedOverlay) {
        // Calculate UV coordinates based on quad size
        float uSize = (maxX - minX) * 16f;
        float vSize = (maxZ - minZ) * 16f;

        // Top face of the liquid surface
        consumer.vertex(pose.pose(), minX, y, minZ)
                .color(red, green, blue, alpha)
                .uv(sprite.getU(0), sprite.getV(0))
                .overlayCoords(combinedOverlay)
                .uv2(combinedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();

        consumer.vertex(pose.pose(), minX, y, maxZ)
                .color(red, green, blue, alpha)
                .uv(sprite.getU(0), sprite.getV(vSize))
                .overlayCoords(combinedOverlay)
                .uv2(combinedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();

        consumer.vertex(pose.pose(), maxX, y, maxZ)
                .color(red, green, blue, alpha)
                .uv(sprite.getU(uSize), sprite.getV(vSize))
                .overlayCoords(combinedOverlay)
                .uv2(combinedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();

        consumer.vertex(pose.pose(), maxX, y, minZ)
                .color(red, green, blue, alpha)
                .uv(sprite.getU(uSize), sprite.getV(0))
                .overlayCoords(combinedOverlay)
                .uv2(combinedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
    }

    /**
     * Renders a small red clock hand that rotates around pivot X=8, Y=15.5, Z=10 (these are in 16ths).
     * Dimensions interpreted as: widthX = 1, heightY = 0.5, lengthZ = 2.5 (all in 1/16 units).
     */
    private void renderClockHand(FermentingBarrelBlockEntity blockEntity, PoseStack poseStack,
                                 MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        // --- compute pivot based on block facing ---
        // centre of block
        final float centerX = 8f / 16f;
        final float centerZ = 8f / 16f;
        final float pivotY = 15.5f / 16f; // unchanged

        // base offset (this points toward +Z / south in model space)
        final float baseOffsetX = 0f;
        final float baseOffsetZ = -(10f - 8f) / 16f; // original pivotZ (10/16) minus centre (8/16)

        // read block facing safely from the blockstate
        BlockState state = blockEntity.getBlockState();
        Direction facing = Direction.SOUTH; // default fallback
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        }

        // rotate the base offset clockwise in 90° steps depending on facing
        int rot;
        switch (facing) {
            case SOUTH -> rot = 0;
            case EAST  -> rot = 1;
            case NORTH -> rot = 2;
            case WEST  -> rot = 3;
            default    -> rot = 0;
        }

        float rx = baseOffsetX;
        float rz = baseOffsetZ;
        for (int i = 0; i < rot; i++) {
            float nx = rz;
            float nz = -rx;
            rx = nx;
            rz = nz;
        }

        // final pivot in block space
        final float pivotX = centerX + rx;
        final float pivotZ = centerZ + rz;

        // Dimensions (interpreted as sixteenths)
        final float sizeX = 1f / 16f;      // 1 -> 1/16
        final float sizeY = 0.5f / 16f;    // 0.5 -> 0.5/16
        final float sizeZ = 2.5f / 16f;    // 2.5 -> 2.5/16

        // Decide denominator: if current fluid is one of the aging fluids, use maxAgingProgress
        int denom = blockEntity.getMaxProgress();
        FluidStack fluid = blockEntity.getFluid();
        if (!fluid.isEmpty()) {
            if (FermentingBarrelBlockEntity.AGING_RESULTS.containsKey(fluid.getFluid())) {
                denom = blockEntity.getMaxAgingProgress();
            }
        }

        int progress = blockEntity.getProgress();
        float safeDenom = Math.max(1, denom);
        float angle = -(progress / safeDenom) * 360f;

        // Setup for drawing: translate to pivot, rotate, then draw a rectangular prism that starts at pivot and extends along +Z
        poseStack.pushPose();

        // move to pivot
        poseStack.translate(pivotX, pivotY, pivotZ);

        float initialRotationOffset = -(rot * 90f) + (rot % 2 == 0 ? 180f : 0f); // rotates the hand orientation to align with facing
        // rotate around Y (vertical axis)
        poseStack.mulPose(Axis.YP.rotationDegrees(initialRotationOffset + angle));

        // after rotation, we want the box to start at the pivot and extend outward along +Z.
        // translate so the box's minX is -sizeX/2, minY is -sizeY/2, minZ is 0
        poseStack.translate(-sizeX / 2f, 0f, 0f);
        poseStack.translate(0f, 0f, -0.5f / 16f); // move hand 0.5 toward block center


        // Now draw box from (0,0,0) to (sizeX, sizeY, sizeZ)
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.solid());

        // red color (opaque)
        float r = 215/255f, g = 0f, b = 0f, a = 1f;

        // Min / Max coordinates after transforms (local to current pose)
        float minX = 0f, minY = 0f, minZ = 0f;
        float maxX = sizeX, maxY = sizeY, maxZ = sizeZ;

        // Bind a known white sprite from the block atlas so vertex colors tint correctly
        TextureAtlasSprite white = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(new ResourceLocation("minecraft", "block/white_concrete"));

        // compute UV sizes in sprite pixels (same technique as your fluid code)
        PoseStack.Pose pose = poseStack.last();
        float uSize = (maxX - minX) * 16f;
        float vSize = (maxZ - minZ) * 16f;

// Front face (+Z)
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

// Back face (-Z)
        consumer.vertex(pose.pose(), maxX, minY, minZ).color(r,g,b,a)
                .uv(white.getU(0f), white.getV(0f)).overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 0,0,-1).endVertex();
        consumer.vertex(pose.pose(), minX, minY, minZ).color(r,g,b,a)
                .uv(white.getU(uSize), white.getV(0f)).overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 0,0,-1).endVertex();
        consumer.vertex(pose.pose(), minX, maxY, minZ).color(r,g,b,a)
                .uv(white.getU(uSize), white.getV(vSize)).overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 0,0,-1).endVertex();
        consumer.vertex(pose.pose(), maxX, maxY, minZ).color(r,g,b,a)
                .uv(white.getU(0f), white.getV(vSize)).overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 0,0,-1).endVertex();

// Left face (-X)
        consumer.vertex(pose.pose(), minX, minY, minZ).color(r,g,b,a)
                .uv(white.getU(0f), white.getV(0f)).overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), -1,0,0).endVertex();
        consumer.vertex(pose.pose(), minX, minY, maxZ).color(r,g,b,a)
                .uv(white.getU(uSize), white.getV(0f)).overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), -1,0,0).endVertex();
        consumer.vertex(pose.pose(), minX, maxY, maxZ).color(r,g,b,a)
                .uv(white.getU(uSize), white.getV(vSize)).overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), -1,0,0).endVertex();
        consumer.vertex(pose.pose(), minX, maxY, minZ).color(r,g,b,a)
                .uv(white.getU(0f), white.getV(vSize)).overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), -1,0,0).endVertex();

// Right face (+X)
        consumer.vertex(pose.pose(), maxX, minY, maxZ).color(r,g,b,a)
                .uv(white.getU(0f), white.getV(0f)).overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 1,0,0).endVertex();
        consumer.vertex(pose.pose(), maxX, minY, minZ).color(r,g,b,a)
                .uv(white.getU(uSize), white.getV(0f)).overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 1,0,0).endVertex();
        consumer.vertex(pose.pose(), maxX, maxY, minZ).color(r,g,b,a)
                .uv(white.getU(uSize), white.getV(vSize)).overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 1,0,0).endVertex();
        consumer.vertex(pose.pose(), maxX, maxY, maxZ).color(r,g,b,a)
                .uv(white.getU(0f), white.getV(vSize)).overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 1,0,0).endVertex();

// Top face (+Y)
        consumer.vertex(pose.pose(), minX, maxY, maxZ).color(r,g,b,a)
                .uv(white.getU(0f), white.getV(0f)).overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 0,1,0).endVertex();
        consumer.vertex(pose.pose(), maxX, maxY, maxZ).color(r,g,b,a)
                .uv(white.getU(uSize), white.getV(0f)).overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 0,1,0).endVertex();
        consumer.vertex(pose.pose(), maxX, maxY, minZ).color(r,g,b,a)
                .uv(white.getU(uSize), white.getV(vSize)).overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 0,1,0).endVertex();
        consumer.vertex(pose.pose(), minX, maxY, minZ).color(r,g,b,a)
                .uv(white.getU(0f), white.getV(vSize)).overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 0,1,0).endVertex();

// Bottom face (-Y)
        consumer.vertex(pose.pose(), minX, minY, minZ).color(r,g,b,a)
                .uv(white.getU(0f), white.getV(0f)).overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 0,-1,0).endVertex();
        consumer.vertex(pose.pose(), maxX, minY, minZ).color(r,g,b,a)
                .uv(white.getU(uSize), white.getV(0f)).overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 0,-1,0).endVertex();
        consumer.vertex(pose.pose(), maxX, minY, maxZ).color(r,g,b,a)
                .uv(white.getU(uSize), white.getV(vSize)).overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 0,-1,0).endVertex();
        consumer.vertex(pose.pose(), minX, minY, maxZ).color(r,g,b,a)
                .uv(white.getU(0f), white.getV(vSize)).overlayCoords(combinedOverlay).uv2(combinedLight)
                .normal(pose.normal(), 0,-1,0).endVertex();

        poseStack.popPose();
    }
}
