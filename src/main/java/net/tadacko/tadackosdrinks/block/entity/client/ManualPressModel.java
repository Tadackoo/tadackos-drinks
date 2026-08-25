package net.tadacko.tadackosdrinks.block.entity.client;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.tadacko.tadackosdrinks.TadackosDrinks;
import net.tadacko.tadackosdrinks.block.ModBlocks;
import net.tadacko.tadackosdrinks.block.entity.ManualPressBlockEntity;
import software.bernie.geckolib.model.GeoModel;

public class ManualPressModel extends GeoModel<ManualPressBlockEntity> {
    private static final ResourceLocation DEFAULT_TEXTURE = new ResourceLocation(TadackosDrinks.MOD_ID, "textures/block/manual_press_oak.png");
    @Override
    public ResourceLocation getModelResource(ManualPressBlockEntity animatable) {
        return new ResourceLocation(TadackosDrinks.MOD_ID, "geo/manual_press.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ManualPressBlockEntity animatable) {
        if (animatable == null) return DEFAULT_TEXTURE;

        Level level = animatable.getLevel();
        if (level == null) return DEFAULT_TEXTURE;

        BlockState state = level.getBlockState(animatable.getBlockPos());
        if (state.getBlock() == ModBlocks.MANUAL_PRESS_OAK.get()) {
            return new ResourceLocation(TadackosDrinks.MOD_ID, "textures/block/manual_press_oak.png");
        } else if (state.getBlock() == ModBlocks.MANUAL_PRESS_SPRUCE.get()) {
            return new ResourceLocation(TadackosDrinks.MOD_ID, "textures/block/manual_press_spruce.png");
        } else if (state.getBlock() == ModBlocks.MANUAL_PRESS_BIRCH.get()) {
            return new ResourceLocation(TadackosDrinks.MOD_ID, "textures/block/manual_press_birch.png");
        } else if (state.getBlock() == ModBlocks.MANUAL_PRESS_JUNGLE.get()) {
            return new ResourceLocation(TadackosDrinks.MOD_ID, "textures/block/manual_press_jungle.png");
        } else if (state.getBlock() == ModBlocks.MANUAL_PRESS_ACACIA.get()) {
            return new ResourceLocation(TadackosDrinks.MOD_ID, "textures/block/manual_press_acacia.png");
        } else if (state.getBlock() == ModBlocks.MANUAL_PRESS_DARK_OAK.get()) {
            return new ResourceLocation(TadackosDrinks.MOD_ID, "textures/block/manual_press_dark_oak.png");
        } else if (state.getBlock() == ModBlocks.MANUAL_PRESS_MANGROVE.get()) {
            return new ResourceLocation(TadackosDrinks.MOD_ID, "textures/block/manual_press_mangrove.png");
        } else if (state.getBlock() == ModBlocks.MANUAL_PRESS_CHERRY.get()) {
            return new ResourceLocation(TadackosDrinks.MOD_ID, "textures/block/manual_press_cherry.png");
        } else if (state.getBlock() == ModBlocks.MANUAL_PRESS_BAMBOO.get()) {
            return new ResourceLocation(TadackosDrinks.MOD_ID, "textures/block/manual_press_bamboo.png");
        } else if (state.getBlock() == ModBlocks.MANUAL_PRESS_CRIMSON.get()) {
            return new ResourceLocation(TadackosDrinks.MOD_ID, "textures/block/manual_press_crimson.png");
        } else if (state.getBlock() == ModBlocks.MANUAL_PRESS_WARPED.get()) {
            return new ResourceLocation(TadackosDrinks.MOD_ID, "textures/block/manual_press_warped.png");
        }
        return DEFAULT_TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(ManualPressBlockEntity animatable) {
        return new ResourceLocation(TadackosDrinks.MOD_ID, "animations/manual_press.animation.json");
    }
}
