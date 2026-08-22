package net.tadacko.tadackosdrinks.effect;

import net.minecraft.world.effect.HealthBoostMobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.tadacko.tadackosdrinks.TadackosDrinks;
import net.minecraft.world.effect.MobEffect;

import java.util.UUID;

public class ModEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS
            = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, TadackosDrinks.MOD_ID);

    private static final UUID HEALTH_BOOST_RED_UUID = UUID.fromString("66438931-bd93-4e9f-b28e-aff74e4f1aee");
    private static final UUID HEALTH_BOOST_ROSE_UUID = UUID.fromString("7eaee5e4-6693-4e3b-9667-cf6607ef6bcf");

    public static final RegistryObject<MobEffect> INEBRIATION = MOB_EFFECTS.register("inebriation",
            () -> new InebriationEffect(MobEffectCategory.HARMFUL, 16762624));
    public static final RegistryObject<MobEffect> CAMARADERIE = MOB_EFFECTS.register("camaraderie",
            () -> new CamaraderieEffect(MobEffectCategory.BENEFICIAL, 16762624));
    public static final RegistryObject<MobEffect> MILD_NAUSEA = MOB_EFFECTS.register("mild_nausea",
            () -> new MildNauseaEffect(MobEffectCategory.HARMFUL, 5578058));
    public static final RegistryObject<MobEffect> HANGOVER = MOB_EFFECTS.register("hangover",
            () -> new HangoverEffect(MobEffectCategory.HARMFUL, 5578058));
    public static final RegistryObject<MobEffect> VULNERABILITY = MOB_EFFECTS.register("vulnerability",
            () -> new VulnerabilityEffect(MobEffectCategory.HARMFUL, 9520880));
    public static final RegistryObject<MobEffect> HEALTH_BOOST_RED = MOB_EFFECTS.register("health_boost_red",
            () -> new HealthBoostMobEffect(MobEffectCategory.BENEFICIAL, 16284963)
                    .addAttributeModifier(Attributes.MAX_HEALTH, HEALTH_BOOST_RED_UUID.toString(), 4.0D, AttributeModifier.Operation.ADDITION));
    public static final RegistryObject<MobEffect> HEALTH_BOOST_ROSE = MOB_EFFECTS.register("health_boost_rose",
            () -> new HealthBoostMobEffect(MobEffectCategory.BENEFICIAL, 16284963)
                    .addAttributeModifier(Attributes.MAX_HEALTH, HEALTH_BOOST_ROSE_UUID.toString(), 4.0D, AttributeModifier.Operation.ADDITION));
    public static final RegistryObject<MobEffect> ABSORPTION_ORANGE = MOB_EFFECTS.register("absorption_orange",
            () -> new AbsorptionCloneEffect(MobEffectCategory.BENEFICIAL, 2445989));
    public static final RegistryObject<MobEffect> ABSORPTION_WHITE = MOB_EFFECTS.register("absorption_white",
            () -> new AbsorptionCloneEffect(MobEffectCategory.BENEFICIAL, 2445989));
    public static final RegistryObject<MobEffect> WISDOM = MOB_EFFECTS.register("wisdom",
            () -> new WisdomEffect(MobEffectCategory.BENEFICIAL, 12779366));
    public static final RegistryObject<MobEffect> ERUDITION = MOB_EFFECTS.register("erudition",
            () -> new EruditionEffect(MobEffectCategory.BENEFICIAL, 16185078));
    public static final RegistryObject<MobEffect> IMPROVED_DIGESTION = MOB_EFFECTS.register("improved_digestion",
            () -> new ImprovedDigestionEffect(MobEffectCategory.BENEFICIAL, 16262179));
    public static final RegistryObject<MobEffect> PIRACY = MOB_EFFECTS.register("piracy",
            () -> new PiracyEffect(MobEffectCategory.BENEFICIAL, 8954814));
    public static final RegistryObject<MobEffect> CHARISMA = MOB_EFFECTS.register("charisma",
            () -> new CharismaEffect(MobEffectCategory.BENEFICIAL, 4521796));
    public static final RegistryObject<MobEffect> SAVAGERY = MOB_EFFECTS.register("savagery",
            () -> new SavageryEffect(MobEffectCategory.BENEFICIAL, 16762624));

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}
