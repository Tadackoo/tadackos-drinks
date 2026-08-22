package net.tadacko.tadackosdrinks.damage;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageType;
import net.tadacko.tadackosdrinks.TadackosDrinks;

public class ModDamageSources {
    public static final ResourceKey<DamageType> INEBRIATION_KEY =
            ResourceKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE,
                    new ResourceLocation(TadackosDrinks.MOD_ID, "inebriation"));
    public static final ResourceKey<DamageType> HANGOVER_KEY =
            ResourceKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE,
                    new ResourceLocation(TadackosDrinks.MOD_ID, "hangover"));

    public static InebriationDamageSource inebriation(ServerLevel level) {
        Holder<DamageType> holder = level.registryAccess()
                .registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE)
                .getHolderOrThrow(INEBRIATION_KEY);
        return new InebriationDamageSource(holder);
    }
    public static HangoverDamageSource hangover(ServerLevel level) {
        Holder<DamageType> holder = level.registryAccess()
                .registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE)
                .getHolderOrThrow(HANGOVER_KEY);
        return new HangoverDamageSource(holder);
    }
}
