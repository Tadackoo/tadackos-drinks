package net.tadacko.tadackosdrinks.worldgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.tadacko.tadackosdrinks.block.ModBlocks;
import net.tadacko.tadackosdrinks.block.JuniperBlock;

public class JuniperFeature extends Feature<NoneFeatureConfiguration> {
    public JuniperFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos pos = context.origin();
        RandomSource random = context.random();

        JuniperBlock juniper = (JuniperBlock) ModBlocks.JUNIPER.get();

        // ground/light check + target space must be empty, same rule the block itself uses
        BlockState defaultState = juniper.defaultBlockState();
        if (!defaultState.canSurvive(level, pos) || !level.getBlockState(pos).canBeReplaced()) {
            return false;
        }

        int age = random.nextInt(JuniperBlock.MAX_AGE); // 0 (inclusive) .. MAX_AGE-1 (inclusive)
        juniper.growColumn(level, pos, age);
        return true;
    }
}