package net.tadacko.tadackosdrinks.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.tadacko.tadackosdrinks.TadackosDrinks;
import net.tadacko.tadackosdrinks.block.ModBlocks;
import net.tadacko.tadackosdrinks.block.AgaveBlock;
import net.tadacko.tadackosdrinks.worldgen.feature.ModFeatures;

public class ModConfiguredFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> JUNIPER_KEY = registerKey("juniper");
    public static final ResourceKey<ConfiguredFeature<?, ?>> AGAVE_KEY = registerKey("agave");

    public static void bootstrap(BootstapContext<ConfiguredFeature<?, ?>> context) {
        register(context, JUNIPER_KEY, ModFeatures.JUNIPER_FEATURE.get(), NoneFeatureConfiguration.INSTANCE);

        BlockState maxAge = ModBlocks.AGAVE.get().defaultBlockState().setValue(AgaveBlock.AGE, AgaveBlock.MAX_AGE);
        BlockState maxAgeMinusOne = ModBlocks.AGAVE.get().defaultBlockState().setValue(AgaveBlock.AGE, AgaveBlock.MAX_AGE - 1);

        WeightedStateProvider agaveProvider = new WeightedStateProvider(
                SimpleWeightedRandomList.<BlockState>builder().add(maxAge, 1).add(maxAgeMinusOne, 1).build());

        register(context, AGAVE_KEY, Feature.FLOWER, new RandomPatchConfiguration(12, 6, 2,
                PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(agaveProvider))));
    }

    public static ResourceKey<ConfiguredFeature<?, ?>> registerKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, new ResourceLocation(TadackosDrinks.MOD_ID, name));
    }

    private static <FC extends FeatureConfiguration, F extends Feature<FC>>
    void register(BootstapContext<ConfiguredFeature<?, ?>> context, ResourceKey<ConfiguredFeature<?, ?>> key, F feature, FC configuration) {
        context.register(key, new ConfiguredFeature<>(feature, configuration));
    }
}
