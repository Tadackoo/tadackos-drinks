package net.tadacko.tadackosdrinks.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.tadacko.tadackosdrinks.TadackosDrinks;

public class BacUtils {
    public static final double ETHANOL_DENSITY_G_PER_L = 789.5;
    public static final double DEFAULT_BODY_WEIGHT_KG = 70.0; // world average 62kg
    public static final double DEFAULT_RATIO = 0.7; // male 0.71, female 0.58

    public static double BACEliminationRatePercentPerHour = 0.15; // fallback default, overridden by config value

    // thresholds (percent units) - fallback defaults, overridden by config values
    public static double inebriation1Threshold = 0.04;
    public static double inebriation2Threshold = 0.09;
    public static double inebriation3Threshold = 0.16;
    public static double inebriation4Threshold = 0.23;
    public static double inebriation5Threshold = 0.32;
    public static final double[] BAC_THRESHOLDS = {
            inebriation1Threshold, inebriation2Threshold, inebriation3Threshold, inebriation4Threshold, inebriation5Threshold, Double.MAX_VALUE
    };

    public static boolean characterConfigAllowed = true; // fallback default, overridden by config value

    public static final String KEY_BODY_WEIGHT_KG = "body_weight_kg";
    public static final String KEY_RATIO = "ratio";

    private static double ethanolGrams(double abv, double volumeL) {
        return abv * volumeL * ETHANOL_DENSITY_G_PER_L;
    }

    public static double bacIncreasePercent(double abv, double volumeL, double bodyWeightKg, double r) {
        return (ethanolGrams(abv, volumeL) / (bodyWeightKg * 1000.0 * r)) * 100.0;
    }

    public static int bacToAmplifier(double bacPercent) {
        for (int i = 0; i < BAC_THRESHOLDS.length; i++) {
            if (bacPercent <= BAC_THRESHOLDS[i]) return i;
        }
        return BAC_THRESHOLDS.length - 1;
    }

    // compute ticks (ceil) until BAC falls from currentBacPercent to lower threshold for ampIdx
    // lower threshold = 0.0 for amp0, or BAC_THRESHOLDS[ampIdx-1] for amp>0
    public static long computeSegmentTicksForAmp(double currentBacPercent, int ampIdx) {
        double lowerThreshold = (ampIdx == 0) ? 0.0 : BAC_THRESHOLDS[ampIdx - 1];
        double diff = currentBacPercent - lowerThreshold;
        if (diff <= 0.0) return 0L;
        double hours = diff / BACEliminationRatePercentPerHour;
        double ticksDouble = hours * 72000.0;
        long ticks = (long) Math.ceil(ticksDouble);
        return Math.max(1L, ticks);
    }

    public static int computeEffectDurationTicks(double abv, double volumeL, LivingEntity player) {
        CompoundTag root = player.getPersistentData();
        CompoundTag persistent = root.getCompound(TadackosDrinks.MOD_ID);
        double bodyWeightKg = DEFAULT_BODY_WEIGHT_KG;
        if (characterConfigAllowed) {
            if (persistent.contains(KEY_BODY_WEIGHT_KG)) {
                double w = persistent.getDouble(KEY_BODY_WEIGHT_KG);
                if (w > 0.0) bodyWeightKg = w;
            }
        }
        double ratio = DEFAULT_RATIO;
        if (characterConfigAllowed) {
            if (persistent.contains(KEY_RATIO)) {
                double r = persistent.getDouble(KEY_RATIO);
                if (r > 0.0) ratio = r;
            }
        }

        // avoid divide-by-zero
        double hours;
        if (BACEliminationRatePercentPerHour <= 0.0) {
            hours = 0.0;
        } else {
            hours = bacIncreasePercent(abv, volumeL, bodyWeightKg, ratio) / BACEliminationRatePercentPerHour;
        }

        double ticksDouble = hours * 72000.0;
        long ticksLong = (long) Math.ceil(ticksDouble);

        if (ticksLong < 1L) ticksLong = 1L;
        if (ticksLong > Integer.MAX_VALUE) ticksLong = Integer.MAX_VALUE;

        return (int) ticksLong;
    }

    public static void setPlayerNBT(ServerPlayer player, double bodyWeightKg, double ratio) {
        CompoundTag root = player.getPersistentData();
        CompoundTag persistent = root.getCompound(TadackosDrinks.MOD_ID);
        persistent.putDouble(KEY_BODY_WEIGHT_KG, bodyWeightKg);
        persistent.putDouble(KEY_RATIO, ratio);
        root.put(TadackosDrinks.MOD_ID, persistent); // getCompound() returns an orphaned tag if the key didn't exist — must be written back
    }
}
