package net.tadacko.tadackosdrinks.util;

import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.tadacko.tadackosdrinks.fluid.ModFluids;

/**
 * Sets up cauldron fill/empty interactions for every fluid registered in ModFluids.
 * <p>
 * Call bootstrap() from FMLCommonSetupEvent (enqueueWork) after registry events fire.
 */
public class WortCauldronInteraction {
    public static void bootstrap() {
        for (ModFluids.FluidEntry entry : ModFluids.ALL_FLUIDS) {
            var bucketItem = entry.bucket().get();
            var cauldronState = entry.cauldron().get().defaultBlockState();
            var map = entry.cauldronInteractions();

            // fill cauldron
            CauldronInteraction.EMPTY.put(bucketItem, (state, level, pos, player, hand, stack) ->
                            CauldronInteraction.emptyBucket(level, pos, player, hand, stack,
                                    cauldronState, SoundEvents.BUCKET_EMPTY));

            // fill bucket
            map.put(Items.BUCKET, (state, level, pos, player, hand, stack) ->
                            CauldronInteraction.fillBucket(state, level, pos, player, hand, stack,
                                    new ItemStack(bucketItem), s -> true, SoundEvents.BUCKET_FILL));
        }
    }
}