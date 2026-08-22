package net.tadacko.tadackosdrinks.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.fluids.FluidType;
import net.tadacko.tadackosdrinks.TadackosDrinks;
import net.tadacko.tadackosdrinks.fluid.BaseFluidType;
import net.tadacko.tadackosdrinks.fluid.ModFluids;

import java.util.Objects;

public class ModBlockStateProvider extends BlockStateProvider {

    private static final ResourceLocation CAULDRON_FULL_PARENT =
            new ResourceLocation("minecraft", "block/template_cauldron_full");

    public ModBlockStateProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, TadackosDrinks.MOD_ID, helper);
    }

    @Override
    protected void registerStatesAndModels() {
        // One model per fluid, parented to vanilla's own "full cauldron" template. Only the "content"/"particle" texture slot differs -
        // everything else (rim, sides, inside walls) is inherited from the vanilla template, so
        // this correctly shows each fluid's real still texture instead of a flat tinted overlay.
        // The block color handler (see TadackosDrinks.onBlockColorRegister) still supplies the
        // tint for the content quad, same as vanilla does for water.
        for (ModFluids.FluidEntry entry : ModFluids.ALL_FLUIDS) {
            FluidType fluidType = entry.source().get().getFluidType();
            ResourceLocation stillTexture = ((BaseFluidType) fluidType).getStillTexture();
            ResourceLocation waterTexture = new ResourceLocation("block/water_still");

            if (Objects.equals(stillTexture, waterTexture)) {
                simpleBlock(entry.cauldron().get(), models().getExistingFile(mcLoc("block/water_cauldron_full")));
            } else {
                ModelFile cauldronModel = models()
                        .withExistingParent(entry.cauldron().getId().getPath(), CAULDRON_FULL_PARENT)
                        .texture("particle", stillTexture)
                        .texture("content", stillTexture);

                simpleBlock(entry.cauldron().get(), cauldronModel);
            }
            //simpleBlock(entry.block().get(), models().getExistingFile(mcLoc("block/water")));
        }
    }
}