package net.tadacko.tadackosdrinks.block.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.tadacko.tadackosdrinks.block.entity.ManualCrusherBlockEntity;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class ManualCrusherRenderer implements BlockEntityRenderer<ManualCrusherBlockEntity> {
    private final GeoBlockRenderer<ManualCrusherBlockEntity> geoRenderer;
    public ManualCrusherRenderer(BlockEntityRendererProvider.Context context) {
        this.geoRenderer = new GeoBlockRenderer<>(new ManualCrusherModel());
    }

    @Override
    public void render(ManualCrusherBlockEntity blockEntity, float partialTicks, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        // Delegate rendering to GeoBlockRenderer
        geoRenderer.render(blockEntity, partialTicks, poseStack, bufferSource, packedLight, packedOverlay);

        // Render item if it is being processed
        if (blockEntity.isProcessing) {
            ItemStack seedStack = blockEntity.getSeedStack();
            if (!seedStack.isEmpty()) {
                poseStack.pushPose();

                // Translate and scale to position the seeds inside the crusher
                poseStack.translate(0.5, 0.3, 0.5);
                poseStack.scale(0.5f, 0.5f, 0.5f);
                poseStack.mulPose(Axis.YP.rotationDegrees(90));

                Direction facing = blockEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);

                float rotationAngle = switch (facing) {
                    case NORTH -> 0;
                    case EAST -> 90;
                    case SOUTH -> 180;
                    case WEST -> 270;
                    default -> 0;
                };
                poseStack.mulPose(Axis.YP.rotationDegrees(rotationAngle));

                // Render the item
                Minecraft.getInstance().getItemRenderer().renderStatic(
                        seedStack, ItemDisplayContext.GUI, packedLight, packedOverlay,
                        poseStack, bufferSource, blockEntity.getLevel(), 0);

                poseStack.popPose();
            }
        }
    }
}
