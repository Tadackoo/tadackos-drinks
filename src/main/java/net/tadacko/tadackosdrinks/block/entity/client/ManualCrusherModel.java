package net.tadacko.tadackosdrinks.block.entity.client;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.tadacko.tadackosdrinks.TadackosDrinks;
import net.tadacko.tadackosdrinks.block.ModBlocks;
import net.tadacko.tadackosdrinks.block.entity.ManualCrusherBlockEntity;
import software.bernie.geckolib.model.GeoModel;

public class ManualCrusherModel extends GeoModel<ManualCrusherBlockEntity> {
    private static final ResourceLocation DEFAULT_TEXTURE = new ResourceLocation(TadackosDrinks.MOD_ID, "textures/block/manual_crusher_oak.png");
    @Override
    public ResourceLocation getModelResource(ManualCrusherBlockEntity animatable) {
        return new ResourceLocation(TadackosDrinks.MOD_ID, "geo/manual_crusher.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ManualCrusherBlockEntity animatable) {
        if (animatable == null) return DEFAULT_TEXTURE;

        Level level = animatable.getLevel();
        if (level == null) return DEFAULT_TEXTURE;

        BlockState state = level.getBlockState(animatable.getBlockPos());
        if (state.getBlock() == ModBlocks.MANUAL_CRUSHER_OAK.get()) {
            return new ResourceLocation(TadackosDrinks.MOD_ID, "textures/block/manual_crusher_oak.png");
        } else if (state.getBlock() == ModBlocks.MANUAL_CRUSHER_SPRUCE.get()) {
            return new ResourceLocation(TadackosDrinks.MOD_ID, "textures/block/manual_crusher_spruce.png");
        } else if (state.getBlock() == ModBlocks.MANUAL_CRUSHER_BIRCH.get()) {
            return new ResourceLocation(TadackosDrinks.MOD_ID, "textures/block/manual_crusher_birch.png");
        } else if (state.getBlock() == ModBlocks.MANUAL_CRUSHER_JUNGLE.get()) {
            return new ResourceLocation(TadackosDrinks.MOD_ID, "textures/block/manual_crusher_jungle.png");
        } else if (state.getBlock() == ModBlocks.MANUAL_CRUSHER_ACACIA.get()) {
            return new ResourceLocation(TadackosDrinks.MOD_ID, "textures/block/manual_crusher_acacia.png");
        } else if (state.getBlock() == ModBlocks.MANUAL_CRUSHER_DARK_OAK.get()) {
            return new ResourceLocation(TadackosDrinks.MOD_ID, "textures/block/manual_crusher_dark_oak.png");
        } else if (state.getBlock() == ModBlocks.MANUAL_CRUSHER_MANGROVE.get()) {
            return new ResourceLocation(TadackosDrinks.MOD_ID, "textures/block/manual_crusher_mangrove.png");
        } else if (state.getBlock() == ModBlocks.MANUAL_CRUSHER_CHERRY.get()) {
            return new ResourceLocation(TadackosDrinks.MOD_ID, "textures/block/manual_crusher_cherry.png");
        } else if (state.getBlock() == ModBlocks.MANUAL_CRUSHER_BAMBOO.get()) {
            return new ResourceLocation(TadackosDrinks.MOD_ID, "textures/block/manual_crusher_bamboo.png");
        } else if (state.getBlock() == ModBlocks.MANUAL_CRUSHER_CRIMSON.get()) {
            return new ResourceLocation(TadackosDrinks.MOD_ID, "textures/block/manual_crusher_crimson.png");
        } else if (state.getBlock() == ModBlocks.MANUAL_CRUSHER_WARPED.get()) {
            return new ResourceLocation(TadackosDrinks.MOD_ID, "textures/block/manual_crusher_warped.png");
        }
        return DEFAULT_TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(ManualCrusherBlockEntity animatable) {
        return new ResourceLocation(TadackosDrinks.MOD_ID, "animations/manual_crusher.animation.json");
    }
}
