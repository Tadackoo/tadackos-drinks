package net.tadacko.tadackosdrinks.item.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.tadacko.tadackosdrinks.block.DrinkVariant;
import net.tadacko.tadackosdrinks.client.DrinkRenderHelper;
import net.tadacko.tadackosdrinks.client.DrinkRenderHelper.Volume;
import net.tadacko.tadackosdrinks.item.ModItems;
import net.tadacko.tadackosdrinks.item.PlaceableDrinkwareItem;

public class DrinkItemRenderer extends BlockEntityWithoutLevelRenderer {

    private static DrinkItemRenderer INSTANCE;

    public static DrinkItemRenderer getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DrinkItemRenderer(
                    Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                    Minecraft.getInstance().getEntityModels()
            );
        }
        return INSTANCE;
    }

    public DrinkItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext ctx, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        if (!(stack.getItem() instanceof PlaceableDrinkwareItem drinkItem)) return;

        DrinkVariant variant = drinkItem.getVariant();
        Minecraft mc = Minecraft.getInstance();

        // Cache the empty item and model — used for both glass and transform lookup
        Item emptyItem = getEmptyItem(variant);
        ItemStack emptyStack = new ItemStack(emptyItem);
        BakedModel emptyModel = mc.getItemRenderer().getModel(emptyStack, mc.level, null, 0);

        poseStack.pushPose();
        poseStack.translate(0.5f, 0.5f, 0.5f);

        // Render the base empty glass — renderStatic applies transforms internally
        mc.getItemRenderer().renderStatic(emptyStack, ctx, light, overlay, poseStack, buffers, mc.level, 0);

        // Apply the same transforms for fluid/foam so they align with the glass
        emptyModel.getTransforms().getTransform(ctx).apply(false, poseStack);
        poseStack.translate(-0.5f, -0.5f, -0.5f);

        // Render fluid
        ResourceLocation fluidTex = DrinkRenderHelper.getFluidTexture(variant);
        Volume vol = DrinkRenderHelper.getVolume(variant);
        if (fluidTex != null && vol != null) {
            TextureAtlasSprite fluidSprite = mc.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(fluidTex);
            VertexConsumer vc = buffers.getBuffer(RenderType.translucent());
            DrinkRenderHelper.renderFluid(vc, poseStack.last(), fluidSprite, vol, light, overlay);

            // Render foam if applicable
            ResourceLocation foamTex = DrinkRenderHelper.getFoamTexture(variant);
            if (foamTex != null) {
                TextureAtlasSprite foamSprite = mc.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(foamTex);
                DrinkRenderHelper.renderFoam(vc, poseStack.last(), foamSprite, light, overlay);
            }
        }

        poseStack.popPose();
    }

    private static Item getEmptyItem(DrinkVariant variant) {
        return switch (variant) {
            case BEER, CIDER -> ModItems.BEER_GLASS_EMPTY.get();
            case WINE_RED, WINE_ROSE, WINE_ORANGE, WINE_WHITE, MEAD -> ModItems.WINE_GLASS_EMPTY.get();
            case WHISKY, RUM, RUM_LIGHT -> ModItems.WHISKY_GLASS_EMPTY.get();
            case BRANDY -> ModItems.BRANDY_GLASS_EMPTY.get();
            case SHOT, TEQUILA -> ModItems.SHOT_GLASS_EMPTY.get();
            default -> ModItems.BEER_GLASS_EMPTY.get();
        };
    }
}