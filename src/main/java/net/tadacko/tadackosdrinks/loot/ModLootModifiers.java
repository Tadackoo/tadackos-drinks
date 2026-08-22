package net.tadacko.tadackosdrinks.loot;

import com.mojang.serialization.Codec;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.tadacko.tadackosdrinks.TadackosDrinks;

/**
 * Register the codec (serializer) for our loot modifier so Forge can read the JSON.
 */
public class ModLootModifiers {
    // Create deferred register for global loot modifier serializers
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> GLM_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, TadackosDrinks.MOD_ID);

    // registry name "add_seed" -> JSON "type": "mymod:add_seed"
    public static final RegistryObject<Codec<AddSeedModifier>> ADD_SEED =
            GLM_SERIALIZERS.register("add_seed", () -> AddSeedModifier.CODEC);

    public static void register(IEventBus modEventBus) {
        GLM_SERIALIZERS.register(modEventBus);
    }
}