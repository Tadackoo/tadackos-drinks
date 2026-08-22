package net.tadacko.tadackosdrinks.effect;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.item.ItemStack;
import net.tadacko.tadackosdrinks.TadackosDrinks;
import net.tadacko.tadackosdrinks.item.ModItems;

import java.util.ArrayList;
import java.util.List;

public class HangoverEffect extends MobEffect {
    private static final String KEY_EFFECTS_APPLIED = "hangover_effects_applied";

    protected HangoverEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) return;

        MobEffectInstance inst = entity.getEffect(this);
        if (inst == null) return;

        // Use NBT to track if we've already applied the secondary effects
        CompoundTag root = entity.getPersistentData();
        CompoundTag persistent = root.getCompound(TadackosDrinks.MOD_ID);
        if (!persistent.getBoolean(KEY_EFFECTS_APPLIED)) {
            int duration = inst.getDuration();

            if (amplifier == 1) {
                entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 0, false, false, false));
                entity.addEffect(new MobEffectInstance(ModEffects.VULNERABILITY.get(), duration, 0, false, false, false));
            } else if (amplifier == 2) {
                entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 1, false, false, false));
                entity.addEffect(new MobEffectInstance(ModEffects.VULNERABILITY.get(), duration, 1, false, false, false));
            }

            persistent.putBoolean(KEY_EFFECTS_APPLIED, true);
            root.put(TadackosDrinks.MOD_ID, persistent);
        }
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
        super.removeAttributeModifiers(entity, attributes, amplifier);
        // Clean up the flag when effect is removed
        entity.getPersistentData().getCompound(TadackosDrinks.MOD_ID).remove(KEY_EFFECTS_APPLIED);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true; // Ensures applyEffectTick is called
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
        ret.add(new ItemStack(ModItems.HANGOVER_CURE.get()));
        return ret;
    }
}
