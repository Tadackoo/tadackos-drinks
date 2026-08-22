package net.tadacko.tadackosdrinks.fluid;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.tadacko.tadackosdrinks.TadackosDrinks;
import org.joml.Vector3f;

public class ModFluidsTypes {
    public static final ResourceLocation WATER_STILL_RL = new ResourceLocation("block/water_still");
    public static final ResourceLocation WATER_FLOWING_RL = new ResourceLocation("block/water_flow");
    public static final ResourceLocation WATER_OVERLAY_RL = new ResourceLocation("misc/underwater");
    public static final ResourceLocation MUST_STILL_RL = new ResourceLocation(TadackosDrinks.MOD_ID, "block/must_still");
    public static final ResourceLocation MUST_FLOWING_RL = new ResourceLocation(TadackosDrinks.MOD_ID, "block/must_flow");
    public static final ResourceLocation SPIRIT_SPICED_STILL_RL = new ResourceLocation(TadackosDrinks.MOD_ID, "block/spirit_spiced_still");
    public static final ResourceLocation SPIRIT_SPICED_FLOWING_RL = new ResourceLocation(TadackosDrinks.MOD_ID, "block/spirit_spiced_flow");

    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, TadackosDrinks.MOD_ID);

    private static final FluidType.Properties DEFAULT_PROPS = FluidType.Properties.create()
            .fallDistanceModifier(0F).canExtinguish(true).canConvertToSource(true).supportsBoating(true)
            .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL).sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
            .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH).canHydrate(true);

    public static final RegistryObject<FluidType> WORT_FLUID_TYPE = register("wort_fluid", 0xFF945000, 148, 80, 0 );
    public static final RegistryObject<FluidType> BEER_FLUID_TYPE = register("beer_fluid", 0xFFd9b050, 255, 198, 66 );

    public static final RegistryObject<FluidType> MUST_RED_FLUID_TYPE = register("must_red_fluid", 0xFF4F0D0D, 79, 13, 13,
            MUST_STILL_RL, MUST_FLOWING_RL);
    public static final RegistryObject<FluidType> MUST_WHITE_FLUID_TYPE = register("must_white_fluid", 0xFFE2E294, 226, 226, 148,
            MUST_STILL_RL, MUST_FLOWING_RL);
    public static final RegistryObject<FluidType> WINE_RED_FLUID_TYPE = register("wine_red_fluid", 0xFF4F0D0D, 79, 13, 13 );
    public static final RegistryObject<FluidType> WINE_ROSE_FLUID_TYPE = register("wine_rose_fluid", 0xFFFEB0C2, 254, 176, 194);
    public static final RegistryObject<FluidType> WINE_ORANGE_FLUID_TYPE = register("wine_orange_fluid", 0xFFFFC675, 255, 198, 117);
    public static final RegistryObject<FluidType> WINE_WHITE_FLUID_TYPE = register("wine_white_fluid", 0xFFE2E294, 226, 226, 148);

    public static final RegistryObject<FluidType> MUST_APPLE_FLUID_TYPE = register("must_apple_fluid", 0xFFFED067, 254, 208, 103,
            MUST_STILL_RL, MUST_FLOWING_RL);
    public static final RegistryObject<FluidType> CIDER_FLUID_TYPE = register("cider_fluid", 0xFFFED067, 254, 208, 103);

    public static final RegistryObject<FluidType> MEAD_FLUID_TYPE = register("mead_fluid", 0xFFFFDF7D, 255, 223, 125);

    public static final RegistryObject<FluidType> SPIRIT_FLUID_TYPE = register("spirit_fluid", 0xFFDDF9F6, 221, 249, 246);

    public static final RegistryObject<FluidType> CONCENTRATED_WHISKY_FLUID_TYPE = register("concentrated_whisky_fluid", 0xFFB15300, 177, 83, 0);
    public static final RegistryObject<FluidType> WHISKY_FLUID_TYPE = register("whisky_fluid", 0xFFCA7427, 202, 116, 39);

    public static final RegistryObject<FluidType> CONCENTRATED_BRANDY_FLUID_TYPE = register("concentrated_brandy_fluid", 0xFFA84B05, 168, 75, 5);
    public static final RegistryObject<FluidType> BRANDY_FLUID_TYPE = register("brandy_fluid", 0xFFC16C2C, 193, 108, 44);

    public static final RegistryObject<FluidType> MUST_SUGARCANE_FLUID_TYPE = register("must_sugarcane_fluid", 0xFFD0D65C, 208, 214, 92,
            MUST_STILL_RL, MUST_FLOWING_RL);
    public static final RegistryObject<FluidType> JUICE_SUGARCANE_FLUID_TYPE = register("juice_sugarcane_fluid", 0xFFD0D65C, 208, 214, 92);
    public static final RegistryObject<FluidType> WASH_SUGARCANE_FLUID_TYPE = register("wash_sugarcane_fluid", 0xFF5E2606, 94, 38, 6);

    public static final RegistryObject<FluidType> CONCENTRATED_RUM_AGED_FLUID_TYPE = register("concentrated_rum_aged_fluid", 0xFF983C03, 152, 60, 3);
    public static final RegistryObject<FluidType> RUM_AGED_FLUID_TYPE = register("rum_aged_fluid", 0xFFB25C27, 178, 92, 39);

    public static final RegistryObject<FluidType> WASH_POTATO_FLUID_TYPE = register("wash_potato_fluid", 0xFFE0CC89, 224, 204, 137);

    public static final RegistryObject<FluidType> SPIRIT_SPICED_FLUID_TYPE = register("spirit_spiced_fluid", 0xFFDDF9F6, 221, 249, 246,
            SPIRIT_SPICED_STILL_RL, SPIRIT_SPICED_FLOWING_RL);

    public static final RegistryObject<FluidType> MUST_AGAVE_FLUID_TYPE = register("must_agave_fluid", 0xFF9C5400, 156, 84, 0,
            MUST_STILL_RL, MUST_FLOWING_RL);
    public static final RegistryObject<FluidType> WASH_AGAVE_FLUID_TYPE = register("wash_agave_fluid", 0xFF9C5400, 156, 84, 0);

    public static final RegistryObject<FluidType> CONCENTRATED_TEQUILA_AGED_FLUID_TYPE = register("concentrated_tequila_aged_fluid", 0xFFAF9309, 175, 147, 9);
    public static final RegistryObject<FluidType> TEQUILA_AGED_FLUID_TYPE = register("tequila_aged_fluid", 0xFFC9AF33, 201, 175, 51);

    private static RegistryObject<FluidType> register(String name, int color, float r, float g, float b) {
        return register(name, color, r, g, b, WATER_STILL_RL, WATER_FLOWING_RL);
    }

    private static RegistryObject<FluidType> register(String name, int color, float r, float g, float b, ResourceLocation still, ResourceLocation flowing) {
        return FLUID_TYPES.register(name, () -> new BaseFluidType(still, flowing, WATER_OVERLAY_RL, color,
                new Vector3f(r / 255f, g / 255f, b / 255f), DEFAULT_PROPS));
    }

    public static void register(IEventBus eventBus) {
        FLUID_TYPES.register(eventBus);
    }
}