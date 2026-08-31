package net.tadacko.tadackosdrinks.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.tadacko.tadackosdrinks.TadackosDrinks;
import net.tadacko.tadackosdrinks.block.FermentingBarrelBlock;
import net.tadacko.tadackosdrinks.effect.*;
import net.tadacko.tadackosdrinks.item.DrinkItem;
import net.tadacko.tadackosdrinks.item.TequilaDrinkItem;
import net.tadacko.tadackosdrinks.util.BacUtils;
import net.tadacko.tadackosdrinks.util.ThrownItemToCauldronEvent;
import net.tadacko.tadackosdrinks.util.Tooltips;
import net.tadacko.tadackosdrinks.util.WaterInteractionHandler;

@Mod.EventBusSubscriber(modid = TadackosDrinks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModCommonConfigs {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.DoubleValue BODY_WEIGHT;
    public static final ForgeConfigSpec.DoubleValue RATIO;

    public static final ForgeConfigSpec.BooleanValue CHARACTER_CONFIG_ALLOWED;
    public static final ForgeConfigSpec.BooleanValue DRINK_SECONDARY_EFFECTS;
    public static final ForgeConfigSpec.BooleanValue THROW_INGREDIENT_CAULDRON;
    public static final ForgeConfigSpec.BooleanValue THROW_INGREDIENT_BARREL;
    public static final ForgeConfigSpec.BooleanValue THROW_MALTING;
    public static final ForgeConfigSpec.DoubleValue BAC_ELIMINATION_RATE;
    public static final ForgeConfigSpec.DoubleValue ABV_BEER;
    public static final ForgeConfigSpec.DoubleValue ABV_WINE;
    public static final ForgeConfigSpec.DoubleValue ABV_CIDER;
    public static final ForgeConfigSpec.DoubleValue ABV_MEAD;
    public static final ForgeConfigSpec.DoubleValue ABV_SPIRIT_LOW;
    public static final ForgeConfigSpec.DoubleValue ABV_SPIRIT_MID;
    public static final ForgeConfigSpec.DoubleValue ABV_SPIRIT_HIGH;
    public static final ForgeConfigSpec.DoubleValue ABV_SPIRIT_MAX;
    public static final ForgeConfigSpec.DoubleValue ABV_WHISKY;
    public static final ForgeConfigSpec.DoubleValue ABV_BRANDY;
    public static final ForgeConfigSpec.DoubleValue ABV_RUM;
    public static final ForgeConfigSpec.DoubleValue ABV_VODKA;
    public static final ForgeConfigSpec.DoubleValue ABV_GIN;
    public static final ForgeConfigSpec.DoubleValue ABV_TEQUILA;
    public static final ForgeConfigSpec.DoubleValue INEBRIATION_1_THRESHOLD;
    public static final ForgeConfigSpec.DoubleValue INEBRIATION_2_THRESHOLD;
    public static final ForgeConfigSpec.DoubleValue INEBRIATION_3_THRESHOLD;
    public static final ForgeConfigSpec.DoubleValue INEBRIATION_4_THRESHOLD;
    public static final ForgeConfigSpec.DoubleValue INEBRIATION_5_THRESHOLD;
    public static final ForgeConfigSpec.DoubleValue STUMBLE_STRENGTH;
    public static final ForgeConfigSpec.DoubleValue STUMBLE_DAMP_FACTOR;
    public static final ForgeConfigSpec.IntValue STUMBLE_CHANGE_MIN;
    public static final ForgeConfigSpec.IntValue STUMBLE_CHANGE_MAX;
    public static final ForgeConfigSpec.DoubleValue STUMBLE_DIRECTION_CHANGE_MAX;
    public static final ForgeConfigSpec.DoubleValue STUMBLE_SHARP_TURN_CHANCE;
    public static final ForgeConfigSpec.DoubleValue STUMBLE_JITTER_STRENGTH;
    public static final ForgeConfigSpec.IntValue CAMARADERIE_RANGE;
    public static final ForgeConfigSpec.IntValue CAMARADERIE_PLAYER_CAP;
    public static final ForgeConfigSpec.DoubleValue CAMARADERIE_MULTIPLIER;
    public static final ForgeConfigSpec.IntValue HANGOVER_BASE_DURATION;
    public static final ForgeConfigSpec.DoubleValue VULNERABILITY_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue WISDOM_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue PIRACY_0_NOTHING_CHANCE;
    public static final ForgeConfigSpec.DoubleValue PIRACY_0_GOLD_CHANCE;
    public static final ForgeConfigSpec.DoubleValue PIRACY_0_EMERALD_CHANCE;
    public static final ForgeConfigSpec.DoubleValue PIRACY_1_NOTHING_CHANCE;
    public static final ForgeConfigSpec.DoubleValue PIRACY_1_GOLD_CHANCE;
    public static final ForgeConfigSpec.DoubleValue PIRACY_1_EMERALD_CHANCE;
    public static final ForgeConfigSpec.DoubleValue SAVAGERY_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue TEQUILA_DURATION_MULTIPLIER;

    static {
        BUILDER.push("Character");
        BUILDER.comment("These will work in both singleplayer and multiplayer for your player only\nThey will only work when characterConfigAllowed is " +
                "set to true on the server\nThey will work across all worlds").define("_characterReadMe", "");
        BODY_WEIGHT = BUILDER.comment("Your character's body weight in kilograms, used for BAC calculation, higher = more tolerance (default 70)")
                .defineInRange("bodyWeight", 70, 0, Double.MAX_VALUE);
        RATIO = BUILDER.comment("Your character's alcohol distribution ratio, used for BAC calculation, higher = more tolerance (default 0.7)")
                .defineInRange("ratio", 0.7, 0, Double.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("Generic");
        BUILDER.comment("These will only work in singleplayer or when edited in a server's files\nThey will work across all singleplayer worlds\n" +
                "Server configs are located at minecraft/saves/worldname/serverconfig/tadackosdrinks-server.toml").define("_genericReadMe", "");
        CHARACTER_CONFIG_ALLOWED = BUILDER.comment("Allow players to change their body weight and alcohol distribution ratio (default true)")
                .define("characterConfigAllowed", true);
        DRINK_SECONDARY_EFFECTS = BUILDER.comment("Enable secondary effects of drinks (Inebriation is the main effect) (default true)")
                .define("drinkSecondaryEffects", true);
        THROW_INGREDIENT_CAULDRON = BUILDER.comment("Enable throwing/dropping (Q) to add ingredients to the Cauldron (default false)")
                .define("throwIngredientCauldron", false);
        THROW_INGREDIENT_BARREL = BUILDER.comment("Enable throwing/dropping (Q) to add ingredients to the Fermenting Barrel (default false)")
                .define("throwIngredientBarrel", false);
        THROW_MALTING = BUILDER.comment("Enable throwing/dropping (Q) to malt Seeds (default false)")
                .define("throwMalting", false);
        BAC_ELIMINATION_RATE = BUILDER.comment("BAC elimination rate in percent per hour (default 0.15)")
                .defineInRange("BACEliminationRate", 0.15, 0, Double.MAX_VALUE);
        BUILDER.push("Drinks");
        ABV_BEER = BUILDER.comment("Alcohol by volume of Beer in decimal (default 0.05)")
                .defineInRange("ABVBeer", 0.05, -Double.MAX_VALUE, Double.MAX_VALUE); // negative allowed, reverses effect?
        ABV_WINE = BUILDER.comment("Alcohol by volume of Wine in decimal (default 0.12)")
                .defineInRange("ABVWine", 0.12, -Double.MAX_VALUE, Double.MAX_VALUE); // negative allowed, reverses effect?
        ABV_CIDER = BUILDER.comment("Alcohol by volume of Cider in decimal (default 0.05)")
                .defineInRange("ABVCider", 0.05, -Double.MAX_VALUE, Double.MAX_VALUE); // negative allowed, reverses effect?
        ABV_MEAD = BUILDER.comment("Alcohol by volume of Mead in decimal (default 0.12)")
                .defineInRange("ABVMead", 0.12, -Double.MAX_VALUE, Double.MAX_VALUE); // negative allowed, reverses effect?
        ABV_SPIRIT_LOW = BUILDER.comment("Alcohol by volume of Low Wine in decimal (cosmetic only - used in tooltips) (default 0.3)")
                .defineInRange("ABVSpiritLow", 0.3, -Double.MAX_VALUE, Double.MAX_VALUE); // negative allowed, reverses effect?
        ABV_SPIRIT_MID = BUILDER.comment("Alcohol by volume of Mid-Proof Spirit in decimal (cosmetic only - used in tooltips) (default 0.6)")
                .defineInRange("ABVSpiritMid", 0.6, -Double.MAX_VALUE, Double.MAX_VALUE); // negative allowed, reverses effect?
        ABV_SPIRIT_HIGH = BUILDER.comment("Alcohol by volume of High-Proof Spirit in decimal (cosmetic only - used in tooltips) (default 0.8)")
                .defineInRange("ABVSpiritHigh", 0.8, -Double.MAX_VALUE, Double.MAX_VALUE); // negative allowed, reverses effect?
        ABV_SPIRIT_MAX = BUILDER.comment("Alcohol by volume of Rectified Spirit in decimal (cosmetic only - used in tooltips) (default 0.95)")
                .defineInRange("ABVSpiritMax", 0.95, -Double.MAX_VALUE, Double.MAX_VALUE); // negative allowed, reverses effect?
        ABV_WHISKY = BUILDER.comment("Alcohol by volume of Whisky in decimal (default 0.4)")
                .defineInRange("ABVWhisky", 0.4, -Double.MAX_VALUE, Double.MAX_VALUE); // negative allowed, reverses effect?
        ABV_BRANDY = BUILDER.comment("Alcohol by volume of Brandy in decimal (default 0.4)")
                .defineInRange("ABVBrandy", 0.4, -Double.MAX_VALUE, Double.MAX_VALUE); // negative allowed, reverses effect?
        ABV_RUM = BUILDER.comment("Alcohol by volume of Rum in decimal (default 0.4)")
                .defineInRange("ABVRum", 0.4, -Double.MAX_VALUE, Double.MAX_VALUE); // negative allowed, reverses effect?
        ABV_VODKA = BUILDER.comment("Alcohol by volume of Vodka in decimal (default 0.4)")
                .defineInRange("ABVVodka", 0.4, -Double.MAX_VALUE, Double.MAX_VALUE); // negative allowed, reverses effect?
        ABV_GIN = BUILDER.comment("Alcohol by volume of Gin in decimal (default 0.4)")
                .defineInRange("ABVGin", 0.4, -Double.MAX_VALUE, Double.MAX_VALUE); // negative allowed, reverses effect?
        ABV_TEQUILA = BUILDER.comment("Alcohol by volume of Tequila in decimal (default 0.4)")
                .defineInRange("ABVTequila", 0.4, -Double.MAX_VALUE, Double.MAX_VALUE); // negative allowed, reverses effect?
        BUILDER.pop();
        BUILDER.push("Effects");
        BUILDER.comment("BAC thresholds in percent for each level of the Inebriation effect").define("inebriationReadMe", "");
        INEBRIATION_1_THRESHOLD = BUILDER.comment("Threshold for Inebriation II (default 0.04)")
                .defineInRange("inebriation1Threshold", 0.04, 0, Double.MAX_VALUE);
        INEBRIATION_2_THRESHOLD = BUILDER.comment("Threshold for Inebriation III (default 0.09)")
                .defineInRange("inebriation2Threshold", 0.09, 0, Double.MAX_VALUE);
        INEBRIATION_3_THRESHOLD = BUILDER.comment("Threshold for Inebriation IV (default 0.16)")
                .defineInRange("inebriation3Threshold", 0.16, 0, Double.MAX_VALUE);
        INEBRIATION_4_THRESHOLD = BUILDER.comment("Threshold for Inebriation V (default 0.23)")
                .defineInRange("inebriation4Threshold", 0.23, 0, Double.MAX_VALUE);
        INEBRIATION_5_THRESHOLD = BUILDER.comment("Threshold for Inebriation VI (default 0.32)")
                .defineInRange("inebriation5Threshold", 0.32, 0, Double.MAX_VALUE);
        STUMBLE_STRENGTH = BUILDER.comment("Strength/speed of Inebriation Stumble in blocks per tick (default 0.02)")
                .defineInRange("stumbleStrength", 0.02, -Double.MAX_VALUE, Double.MAX_VALUE); // negative allowed, reverses effect?
        STUMBLE_DAMP_FACTOR = BUILDER.comment("How much previous velocity Inebriation Stumble keeps when changing direction (default 0.8)")
                .defineInRange("stumbleDampFactor", 0.8, -Double.MAX_VALUE, Double.MAX_VALUE); // negative allowed, reverses effect?
        STUMBLE_CHANGE_MIN = BUILDER.comment("Minimum time in ticks Inebriation Stumble takes to arrive at target angle (default 5)")
                .defineInRange("stumbleChangeMin", 5, 0, Integer.MAX_VALUE);
        STUMBLE_CHANGE_MAX = BUILDER.comment("Maximum time in ticks Inebriation Stumble takes to arrive at target angle (default 10)")
                .defineInRange("stumbleChangeMax", 10, 0, Integer.MAX_VALUE);
        STUMBLE_DIRECTION_CHANGE_MAX = BUILDER.comment("Maximum offset in decimal of pi when choosing new Inebriation Stumble direction (default 0.9)")
                .defineInRange("stumbleDirectionChangeMax", 0.9, 0, 1);
        STUMBLE_SHARP_TURN_CHANCE = BUILDER.comment("Chance in decimal to force a sharp turn when choosing new Inebriation Stumble direction (default 0.2)")
                .defineInRange("stumbleSharpTurnChance", 0.2, 0, 1);
        STUMBLE_JITTER_STRENGTH = BUILDER.comment("Random Inebriation Stumble jitter in radians per tick (default 0.05)")
                .defineInRange("stumbleJitterStrength", 0.05, -Double.MAX_VALUE, Double.MAX_VALUE); // negative allowed, reverses effect?
        CAMARADERIE_RANGE = BUILDER.comment("How many blocks far a player can be to count towards the Camaraderie effect (default 50)")
                .defineInRange("camaraderieRange", 50, 0, Integer.MAX_VALUE);
        CAMARADERIE_PLAYER_CAP = BUILDER.comment("How much the Camaraderie effect stacks to/how many players within range it counts (default 4)")
                .defineInRange("camaraderiePlayerCap", 4, 0, Integer.MAX_VALUE);
        CAMARADERIE_MULTIPLIER = BUILDER.comment("How much Strength/Resistance equivalent the Camaraderie effect applies per player (default 0.5)")
                .defineInRange("camaraderieMultiplier", 0.5, 0.0, Double.MAX_VALUE);
        HANGOVER_BASE_DURATION = BUILDER.comment("How long Hangover lasts in ticks per level (default 24000)")
                .defineInRange("hangoverBaseDuration", 24000, -1, Integer.MAX_VALUE);
        VULNERABILITY_MULTIPLIER = BUILDER.comment("How much the Vulnerability effect increases damage taken per level (default 0.2)")
                .defineInRange("vulnerabilityMultiplier", 0.2, -Double.MAX_VALUE, Double.MAX_VALUE); // negative allowed, reverses effect
        WISDOM_MULTIPLIER = BUILDER.comment("How much the Wisdom effect multiplies XP by per level (default 1.5)")
                .defineInRange("wisdomMultiplier", 1.5, -Double.MAX_VALUE, Double.MAX_VALUE); // negative allowed, reverses effect
        BUILDER.comment("What chance in percent the Piracy effect has for first 3 drop options, should add up to less than 100, the rest is chance for " +
                "Diamond").define("piracyReadMe", "");
        PIRACY_0_NOTHING_CHANCE = BUILDER.comment("Chance for nothing (default 50)")
                .defineInRange("piracy0NothingChance", 50, 0.0, 100.0);
        PIRACY_0_GOLD_CHANCE = BUILDER.comment("Chance for Gold Ingot (default 40)")
                .defineInRange("piracy0GoldChance", 40, 0.0, 100.0);
        PIRACY_0_EMERALD_CHANCE = BUILDER.comment("Chance for Emerald (default 7)")
                .defineInRange("piracy0EmeraldChance", 7, 0.0, 100.0);
        PIRACY_1_NOTHING_CHANCE = BUILDER.comment("Chance for nothing (default 30)")
                .defineInRange("piracy1NothingChance", 30, 0.0, 100.0);
        PIRACY_1_GOLD_CHANCE = BUILDER.comment("Chance for Gold Ingot (default 50)")
                .defineInRange("piracy1GoldChance", 50, 0.0, 100.0);
        PIRACY_1_EMERALD_CHANCE = BUILDER.comment("Chance for Emerald (default 15)")
                .defineInRange("piracy1EmeraldChance", 15, 0.0, 100.0);
        SAVAGERY_MULTIPLIER = BUILDER.comment("How much the Savagery effect increases critical hit damage per level (default 0.25)")
                .defineInRange("savageryMultiplier", 0.25, -Double.MAX_VALUE, Double.MAX_VALUE); // negative allowed, reverses effect
        TEQUILA_DURATION_MULTIPLIER = BUILDER.comment("How much of its base duration Tequila distributes per level (default 2)")
                .defineInRange("tequilaDurationMultiplier", 2, 0, Double.MAX_VALUE);
        BUILDER.pop();
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    // assigned here cuz cheaper (marginally), prevents "Cannot get config value before config is loaded."
    @SubscribeEvent
    public static void onConfigLoad(ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC) {
            // none of these are used in any client side code, they're in common to work across all singleplayer worlds
            BacUtils.characterConfigAllowed = CHARACTER_CONFIG_ALLOWED.get();
            DrinkItem.drinkSecondaryEffects = DRINK_SECONDARY_EFFECTS.get();
            FermentingBarrelBlock.throwIngredientBarrel = THROW_INGREDIENT_BARREL.get();
            ThrownItemToCauldronEvent.throwIngredientCauldron = THROW_INGREDIENT_CAULDRON.get();
            if (!ThrownItemToCauldronEvent.throwIngredientCauldron) ThrownItemToCauldronEvent.clearTracked();
            WaterInteractionHandler.throwMalting = THROW_MALTING.get();
            if (!WaterInteractionHandler.throwMalting) WaterInteractionHandler.clearTracked();
            BacUtils.BACEliminationRatePercentPerHour = BAC_ELIMINATION_RATE.get();
            DrinkItem.ABVBeer = ABV_BEER.get();
            DrinkItem.ABVWine = ABV_WINE.get();
            DrinkItem.ABVCider = ABV_CIDER.get();
            DrinkItem.ABVMead = ABV_MEAD.get();
            DrinkItem.ABVSpiritLow = ABV_SPIRIT_LOW.get();
            DrinkItem.ABVSpiritMid = ABV_SPIRIT_MID.get();
            DrinkItem.ABVSpiritHigh = ABV_SPIRIT_HIGH.get();
            DrinkItem.ABVSpiritMax = ABV_SPIRIT_MAX.get();
            DrinkItem.ABVWhisky = ABV_WHISKY.get();
            DrinkItem.ABVBrandy = ABV_BRANDY.get();
            DrinkItem.ABVRum = ABV_RUM.get();
            DrinkItem.ABVVodka = ABV_VODKA.get();
            DrinkItem.ABVGin = ABV_GIN.get();
            DrinkItem.ABVTequila = ABV_TEQUILA.get();
            Tooltips.TOOLTIP_MAP = null; // clear cache
            BacUtils.inebriation1Threshold = INEBRIATION_1_THRESHOLD.get();
            BacUtils.inebriation2Threshold = INEBRIATION_2_THRESHOLD.get();
            BacUtils.inebriation3Threshold = INEBRIATION_3_THRESHOLD.get();
            BacUtils.inebriation4Threshold = INEBRIATION_4_THRESHOLD.get();
            BacUtils.inebriation5Threshold = INEBRIATION_5_THRESHOLD.get();
            InebriationEffect.stumbleStrength = STUMBLE_STRENGTH.get();
            InebriationEffect.stumbleDampFactor = STUMBLE_DAMP_FACTOR.get();
            InebriationEffect.stumbleChangeMinTicks = STUMBLE_CHANGE_MIN.get();
            InebriationEffect.stumbleChangeMaxTicks = STUMBLE_CHANGE_MAX.get();
            InebriationEffect.stumbleDirectionChangeMax = STUMBLE_DIRECTION_CHANGE_MAX.get();
            InebriationEffect.stumbleSharpTurnChance = STUMBLE_SHARP_TURN_CHANCE.get();
            InebriationEffect.stumbleJitterStrength = STUMBLE_JITTER_STRENGTH.get();
            CamaraderieEffect.camaraderieRange = CAMARADERIE_RANGE.get();
            CamaraderieEffect.camaraderiePlayerCap = CAMARADERIE_PLAYER_CAP.get();
            CamaraderieEffect.CamaraderieEventHandler.camaraderieMultiplier = CAMARADERIE_MULTIPLIER.get();
            InebriationEffect.hangoverBaseDuration = HANGOVER_BASE_DURATION.get();
            VulnerabilityEffect.VulnerabilityEventHandler.vulnerabilityMultiplier = VULNERABILITY_MULTIPLIER.get().floatValue();
            WisdomEffect.WisdomEventHandler.wisdomMultiplier = WISDOM_MULTIPLIER.get().floatValue();
            PiracyEffect.PiracyEventHandler.piracy0NothingChance = PIRACY_0_NOTHING_CHANCE.get().floatValue();
            PiracyEffect.PiracyEventHandler.piracy0GoldChance = PIRACY_0_GOLD_CHANCE.get().floatValue();
            PiracyEffect.PiracyEventHandler.piracy0EmeraldChance = PIRACY_0_EMERALD_CHANCE.get().floatValue();
            PiracyEffect.PiracyEventHandler.piracy1NothingChance = PIRACY_1_NOTHING_CHANCE.get().floatValue();
            PiracyEffect.PiracyEventHandler.piracy1GoldChance = PIRACY_1_GOLD_CHANCE.get().floatValue();
            PiracyEffect.PiracyEventHandler.piracy1EmeraldChance = PIRACY_1_EMERALD_CHANCE.get().floatValue();
            SavageryEffect.SavageryEventHandler.savageryMultiplier = SAVAGERY_MULTIPLIER.get().floatValue();
            TequilaDrinkItem.tequilaDurationMultiplier = TEQUILA_DURATION_MULTIPLIER.get();
        }
    }
}
