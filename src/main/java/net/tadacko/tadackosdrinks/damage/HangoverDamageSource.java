package net.tadacko.tadackosdrinks.damage;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;

import java.util.Random;

public class HangoverDamageSource extends DamageSource {
    private static final Random RAND = new Random();
    private static final int VARIANTS = 4; // amount of translation keys

    public HangoverDamageSource(Holder<DamageType> type) {
        super(type);
    }

    @Override
    public Component getLocalizedDeathMessage(LivingEntity victim) {
        int choice = 1 + RAND.nextInt(VARIANTS);
        return Component.translatable("death.attack.hangover." + choice, victim.getName());
    }
}
