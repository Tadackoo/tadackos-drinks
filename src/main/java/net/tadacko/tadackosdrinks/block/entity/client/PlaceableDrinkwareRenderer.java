package net.tadacko.tadackosdrinks.block.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.tadacko.tadackosdrinks.block.DrinkVariant;
import net.tadacko.tadackosdrinks.block.PlaceableDrinkwareBlock;
import net.tadacko.tadackosdrinks.block.entity.PlaceableDrinkwareBlockEntity;
import net.tadacko.tadackosdrinks.client.DrinkRenderHelper;
import net.tadacko.tadackosdrinks.client.DrinkRenderHelper.Volume;

public class PlaceableDrinkwareRenderer
        implements BlockEntityRenderer<PlaceableDrinkwareBlockEntity> {

    public PlaceableDrinkwareRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(PlaceableDrinkwareBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        DrinkVariant variant = be.getBlockState().getValue(PlaceableDrinkwareBlock.VARIANT);

        Volume vol = DrinkRenderHelper.getVolume(variant);
        if (vol == null) return;

        ResourceLocation fluidTex = DrinkRenderHelper.getFluidTexture(variant);
        if (fluidTex == null) return;

        TextureAtlasSprite fluidSprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(fluidTex);

        VertexConsumer vc = buffers.getBuffer(RenderType.translucent());
        DrinkRenderHelper.renderFluid(vc, poseStack.last(), fluidSprite, vol, light, overlay);

        // Render foam if applicable
        ResourceLocation foamTex = DrinkRenderHelper.getFoamTexture(variant);
        if (foamTex != null) {
            TextureAtlasSprite foamSprite = Minecraft.getInstance()
                    .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                    .apply(foamTex);
            DrinkRenderHelper.renderFoam(vc, poseStack.last(), foamSprite, light, overlay);
        }
    }
}