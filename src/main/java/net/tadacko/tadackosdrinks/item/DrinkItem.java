package net.tadacko.tadackosdrinks.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.tadacko.tadackosdrinks.TadackosDrinks;
import net.tadacko.tadackosdrinks.block.DrinkVariant;
import net.tadacko.tadackosdrinks.effect.ModEffects;
import net.tadacko.tadackosdrinks.util.BacUtils;

public class DrinkItem extends PlaceableDrinkwareItem {
    private final MobEffect effect;
    private final int durationMultiplier;
    private final int amplifier;
    protected final double abv;
    protected final double volumeL;
    private final Item emptyDrinkware;

    private static final String KEY_BAC_PERCENT = "bac_percent";
    private static final String TAG_SESSION = "inebriation_session";
    private static final String KEY_MAX_AMP = "max_amp";
    private static final String KEY_HANGOVER_APPLIED = "hangover_done";

    public static boolean drinkSecondaryEffects = true; // fallback default, overridden by config value

    public DrinkItem(Properties properties, MobEffect effect, int durationMultiplier, int amplifier, double abv, double volumeL,
                     DrinkVariant variant, Item emptyDrinkware) {
        super(properties, variant);
        this.effect = effect;
        this.durationMultiplier = durationMultiplier;
        this.amplifier = amplifier;
        this.abv = abv;
        this.volumeL = volumeL;
        this.emptyDrinkware = emptyDrinkware;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity) {
        super.finishUsingItem(pStack, pLevel, pLivingEntity);

        if (pLivingEntity instanceof ServerPlayer serverplayer) {
            CriteriaTriggers.CONSUME_ITEM.trigger(serverplayer, pStack);
            serverplayer.awardStat(Stats.ITEM_USED.get(this));
        }

        if (!pLevel.isClientSide) {
            CompoundTag root = pLivingEntity.getPersistentData();
            CompoundTag persistent = root.getCompound(TadackosDrinks.MOD_ID);

            double currentBacPercent = persistent.contains(KEY_BAC_PERCENT) ? persistent.getDouble(KEY_BAC_PERCENT) : 0.0;

            double bodyWeightKg = BacUtils.DEFAULT_BODY_WEIGHT_KG;
            if (BacUtils.characterConfigAllowed) {
                if (persistent.contains(BacUtils.KEY_BODY_WEIGHT_KG)) {
                    double w = persistent.getDouble(BacUtils.KEY_BODY_WEIGHT_KG);
                    if (w > 0.0) bodyWeightKg = w;
                }
            }
            double ratio = BacUtils.DEFAULT_RATIO;
            if (BacUtils.characterConfigAllowed) {
                if (persistent.contains(BacUtils.KEY_RATIO)) {
                    double r = persistent.getDouble(BacUtils.KEY_RATIO);
                    if (r > 0.0) ratio = r;
                }
            }

            double bacIncreasePercent = BacUtils.bacIncreasePercent(this.abv, this.volumeL, bodyWeightKg, ratio);

            currentBacPercent += bacIncreasePercent;
            if (currentBacPercent < 0.0) currentBacPercent = 0.0;
            if (currentBacPercent > 5.0) currentBacPercent = 5.0;

            persistent.putDouble(KEY_BAC_PERCENT, currentBacPercent);

            int newAmp = BacUtils.bacToAmplifier(currentBacPercent);
            long segmentTicks = BacUtils.computeSegmentTicksForAmp(currentBacPercent, newAmp);
            int applyDuration = segmentTicks > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) segmentTicks;

            pLivingEntity.addEffect(new MobEffectInstance(ModEffects.INEBRIATION.get(), applyDuration, newAmp, false, true, true));

            CompoundTag session = persistent.contains(TAG_SESSION) ? persistent.getCompound(TAG_SESSION) : new CompoundTag();
            int recordedMax = session.getInt(KEY_MAX_AMP);
            if (newAmp > recordedMax) session.putInt(KEY_MAX_AMP, newAmp);
            session.putBoolean(KEY_HANGOVER_APPLIED, false);
            persistent.put(TAG_SESSION, session);
            root.put(TadackosDrinks.MOD_ID, persistent);

            //System.out.println(String.format("Drink: abv=%.3f vol=%.3fL => ethanol=%.3fg", this.abv, this.volumeL, ethanol));
            //System.out.println(String.format("BAC +%.5f => now %.5f%% ; amp=%d ; duration=%d ticks (%.1fs)", bacIncreasePercent, currentBacPercent,
            //        newAmp, applyDuration, applyDuration / 20.0));

            if (drinkSecondaryEffects && effect != null) {
                int duration = durationMultiplier * BacUtils.computeEffectDurationTicks(this.abv, this.volumeL, pLivingEntity);
                pLivingEntity.addEffect(new MobEffectInstance(effect, duration, amplifier, false, true, true));
            }
        }

        if (pStack.isEmpty()) {
            return new ItemStack(emptyDrinkware);
        } else {
            if (pLivingEntity instanceof Player && !((Player) pLivingEntity).getAbilities().instabuild) {
                ItemStack itemstack = new ItemStack(emptyDrinkware);
                Player player = (Player) pLivingEntity;
                if (!player.getInventory().add(itemstack)) player.drop(itemstack, false);
            }
            return pStack;
        }
    }

    @Override
    public int getUseDuration(ItemStack pStack) { return 40; }

    @Override
    public UseAnim getUseAnimation(ItemStack pStack) { return UseAnim.DRINK; }

    @Override
    public SoundEvent getDrinkingSound() { return SoundEvents.GENERIC_DRINK; }

    @Override
    public SoundEvent getEatingSound() { return SoundEvents.GENERIC_DRINK; }
}
