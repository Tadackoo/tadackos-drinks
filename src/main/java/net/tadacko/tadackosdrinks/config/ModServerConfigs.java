package net.tadacko.tadackosdrinks.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.tadacko.tadackosdrinks.TadackosDrinks;
import net.tadacko.tadackosdrinks.block.HopCropBlock;
import net.tadacko.tadackosdrinks.effect.CharismaEffect;
import net.tadacko.tadackosdrinks.item.ModItems;

@Mod.EventBusSubscriber(modid = TadackosDrinks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModServerConfigs {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.DoubleValue CHARISMA_MULTIPLIER;
    public static final ForgeConfigSpec.IntValue MOLASSES_STACK_SIZE;
    public static final ForgeConfigSpec.IntValue KEG_STACK_SIZE;
    public static final ForgeConfigSpec.IntValue GLASS_STACK_SIZE;
    public static final ForgeConfigSpec.IntValue DRINK_STACK_SIZE;

    static {
        BUILDER.comment("These will only work for the world this file belongs to\nCommon configs are located at minecraft/config/tadackosdrinks-common.toml")
                .define("_serverReadMe", "");
        CHARISMA_MULTIPLIER = BUILDER.comment("How much the Charisma effect discounts Villager prices per level (default 0.1)")
                .defineInRange("charismaMultiplier", 0.1, Double.MIN_VALUE, Double.MAX_VALUE); // negative allowed, reverses effect
        MOLASSES_STACK_SIZE = BUILDER.comment("How much Sugarcane Molasses stacks to (default 16)")
                .defineInRange("molassesStackSize", 16, 1, 64);
        KEG_STACK_SIZE = BUILDER.comment("How much Kegs stack to (default 1)")
                .defineInRange("kegStackSize", 1, 1, 64);
        GLASS_STACK_SIZE = BUILDER.comment("How much empty Glasses stack to (default 16)")
                .defineInRange("glassStackSize", 16, 1, 64);
        DRINK_STACK_SIZE = BUILDER.comment("How much Drinks stack to (default 1)")
                .defineInRange("drinkStackSize", 1, 1, 64);

        SPEC = BUILDER.build();
    }

    // assigned here cuz cheaper (marginally), prevents "Cannot get config value before config is loaded."
    @SubscribeEvent
    public static void onConfigLoad(ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC) {
            // can't be in common, used in client code, would cause desync
            CharismaEffect.CharismaEventHandler.charismaMultiplier = CHARISMA_MULTIPLIER.get().floatValue();
            ModItems.molassesStackSize = MOLASSES_STACK_SIZE.get();
            ModItems.kegStackSize = KEG_STACK_SIZE.get();
            ModItems.glassStackSize = GLASS_STACK_SIZE.get();
            ModItems.drinkStackSize = DRINK_STACK_SIZE.get();
        }
    }
}
