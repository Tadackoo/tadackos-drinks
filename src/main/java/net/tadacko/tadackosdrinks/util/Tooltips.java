package net.tadacko.tadackosdrinks.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.tadacko.tadackosdrinks.TadackosDrinks;
import net.tadacko.tadackosdrinks.fluid.ModFluids;
import net.tadacko.tadackosdrinks.item.ModItems;
import net.tadacko.tadackosdrinks.item.KegItem;

import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = TadackosDrinks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class Tooltips {
    // Built once, on first tooltip render (items are guaranteed registered by then).
    // Not built eagerly at class-load time to avoid touching item RegistryObjects before registration completes.
    private static Map<Item, List<Component>> TOOLTIP_MAP;

    private static Map<Item, List<Component>> getTooltipMap() {
        if (TOOLTIP_MAP == null) {
            TOOLTIP_MAP = Map.<Item, List<Component>>ofEntries(
                    Map.entry(ModItems.GRAPE_SEEDS_RED.get(), List.of(Component.translatable("tooltip.tadackosdrinks.red").withStyle(ChatFormatting.DARK_RED))),
                    Map.entry(ModItems.GRAPE_SEEDS_WHITE.get(), List.of(Component.translatable("tooltip.tadackosdrinks.white").withStyle(ChatFormatting.YELLOW))),
                    Map.entry(ModItems.GRAPES_RED.get(), List.of(Component.translatable("tooltip.tadackosdrinks.red").withStyle(ChatFormatting.DARK_RED))),
                    Map.entry(ModItems.GRAPES_WHITE.get(), List.of(Component.translatable("tooltip.tadackosdrinks.white").withStyle(ChatFormatting.YELLOW))),

                    Map.entry(ModItems.HANGOVER_ICON.get(), List.of(Component.translatable(
                            "tooltip.tadackosdrinks.advancement_icon"))),
                    Map.entry(ModItems.INEBRIATION_ICON.get(), List.of(Component.translatable(
                            "tooltip.tadackosdrinks.advancement_icon"))),


                    Map.entry(ModItems.BEER_WHEAT_GLASS.get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv5").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.wheat").withStyle(ChatFormatting.YELLOW))),
                    Map.entry(ModItems.BEER_WHEAT_HOPPED_GLASS.get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv5").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.wheat").withStyle(ChatFormatting.YELLOW),
                            Component.translatable("tooltip.tadackosdrinks.hopped").withStyle(ChatFormatting.DARK_GREEN))),
                    Map.entry(ModItems.BEER_BARLEY_GLASS.get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv5").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.barley").withStyle(ChatFormatting.GOLD))),
                    Map.entry(ModItems.BEER_BARLEY_HOPPED_GLASS.get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv5").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.barley").withStyle(ChatFormatting.GOLD),
                            Component.translatable("tooltip.tadackosdrinks.hopped").withStyle(ChatFormatting.DARK_GREEN))),

                    Map.entry(ModItems.WINE_RED_GLASS.get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv12").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.red").withStyle(ChatFormatting.DARK_RED))),
                    Map.entry(ModItems.WINE_RED_AGED_GLASS.get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv12").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.red").withStyle(ChatFormatting.DARK_RED),
                            Component.translatable("tooltip.tadackosdrinks.aged").withStyle(ChatFormatting.DARK_AQUA))),
                    Map.entry(ModItems.WINE_ROSE_GLASS.get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv12").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.rose").withStyle(ChatFormatting.LIGHT_PURPLE))),
                    Map.entry(ModItems.WINE_ROSE_AGED_GLASS.get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv12").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.rose").withStyle(ChatFormatting.LIGHT_PURPLE),
                            Component.translatable("tooltip.tadackosdrinks.aged").withStyle(ChatFormatting.DARK_AQUA))),
                    Map.entry(ModItems.WINE_ORANGE_GLASS.get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv12").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.orange").withStyle(ChatFormatting.GOLD))),
                    Map.entry(ModItems.WINE_ORANGE_AGED_GLASS.get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv12").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.orange").withStyle(ChatFormatting.GOLD),
                            Component.translatable("tooltip.tadackosdrinks.aged").withStyle(ChatFormatting.DARK_AQUA))),
                    Map.entry(ModItems.WINE_WHITE_GLASS.get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv12").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.white").withStyle(ChatFormatting.YELLOW))),
                    Map.entry(ModItems.WINE_WHITE_AGED_GLASS.get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv12").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.white").withStyle(ChatFormatting.YELLOW),
                            Component.translatable("tooltip.tadackosdrinks.aged").withStyle(ChatFormatting.DARK_AQUA))),

                    Map.entry(ModItems.CIDER_GLASS.get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv5").withStyle(ChatFormatting.GRAY))),
                    Map.entry(ModItems.CIDER_AGED_GLASS.get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv5").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.aged").withStyle(ChatFormatting.DARK_AQUA))),

                    Map.entry(ModItems.MEAD_GLASS.get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv12").withStyle(ChatFormatting.GRAY))),
                    Map.entry(ModItems.MEAD_AGED_GLASS.get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv12").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.aged").withStyle(ChatFormatting.DARK_AQUA))),

                    Map.entry(ModItems.WHISKY_WHEAT_GLASS.get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv40").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.wheat").withStyle(ChatFormatting.YELLOW))),
                    Map.entry(ModItems.WHISKY_BARLEY_GLASS.get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv40").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.barley").withStyle(ChatFormatting.GOLD))),

                    Map.entry(ModItems.BRANDY_GRAPE_GLASS.get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv40").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.grape").withStyle(ChatFormatting.DARK_PURPLE))),
                    Map.entry(ModItems.BRANDY_APPLE_GLASS.get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv40").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.apple").withStyle(ChatFormatting.RED))),

                    Map.entry(ModItems.RUM_JUICE_GLASS.get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv40").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.sugarcane_juice").withStyle(ChatFormatting.GREEN),
                            Component.translatable("tooltip.tadackosdrinks.light").withStyle(ChatFormatting.AQUA))),
                    Map.entry(ModItems.RUM_JUICE_AGED_GLASS.get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv40").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.sugarcane_juice").withStyle(ChatFormatting.GREEN),
                            Component.translatable("tooltip.tadackosdrinks.dark").withStyle(ChatFormatting.DARK_AQUA))),
                    Map.entry(ModItems.RUM_MOLASSES_GLASS.get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv40").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.sugarcane_molasses").withStyle(ChatFormatting.DARK_GREEN),
                            Component.translatable("tooltip.tadackosdrinks.light").withStyle(ChatFormatting.AQUA))),
                    Map.entry(ModItems.RUM_MOLASSES_AGED_GLASS.get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv40").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.sugarcane_molasses").withStyle(ChatFormatting.DARK_GREEN),
                            Component.translatable("tooltip.tadackosdrinks.dark").withStyle(ChatFormatting.DARK_AQUA))),

                    Map.entry(ModItems.VODKA_GRAPE_GLASS.get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv40").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.grape").withStyle(ChatFormatting.DARK_PURPLE))),
                    Map.entry(ModItems.VODKA_APPLE_GLASS.get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv40").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.apple").withStyle(ChatFormatting.RED))),
                    Map.entry(ModItems.VODKA_HONEY_GLASS.get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv40").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.honey").withStyle(ChatFormatting.LIGHT_PURPLE))),
                    Map.entry(ModItems.VODKA_SUGARCANE_JUICE_GLASS.get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv40").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.sugarcane_juice").withStyle(ChatFormatting.GREEN))),
                    Map.entry(ModItems.VODKA_SUGARCANE_MOLASSES_GLASS.get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv40").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.sugarcane_molasses").withStyle(ChatFormatting.DARK_GREEN))),
                    Map.entry(ModItems.VODKA_POTATO_GLASS.get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv40").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.potato").withStyle(ChatFormatting.DARK_BLUE))),
                    Map.entry(ModItems.VODKA_AGAVE_GLASS.get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv40").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.agave").withStyle(ChatFormatting.AQUA))),
                    Map.entry(ModItems.VODKA_WHEAT_GLASS.get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv40").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.wheat").withStyle(ChatFormatting.YELLOW))),
                    Map.entry(ModItems.VODKA_BARLEY_GLASS.get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv40").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.barley").withStyle(ChatFormatting.GOLD))),

                    Map.entry(ModItems.GIN_GRAPE_GLASS.get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv40").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.grape").withStyle(ChatFormatting.DARK_PURPLE))),
                    Map.entry(ModItems.GIN_APPLE_GLASS.get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv40").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.apple").withStyle(ChatFormatting.RED))),
                    Map.entry(ModItems.GIN_HONEY_GLASS.get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv40").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.honey").withStyle(ChatFormatting.LIGHT_PURPLE))),
                    Map.entry(ModItems.GIN_SUGARCANE_JUICE_GLASS.get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv40").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.sugarcane_juice").withStyle(ChatFormatting.GREEN))),
                    Map.entry(ModItems.GIN_SUGARCANE_MOLASSES_GLASS.get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv40").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.sugarcane_molasses").withStyle(ChatFormatting.DARK_GREEN))),
                    Map.entry(ModItems.GIN_POTATO_GLASS.get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv40").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.potato").withStyle(ChatFormatting.DARK_BLUE))),
                    Map.entry(ModItems.GIN_AGAVE_GLASS.get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv40").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.agave").withStyle(ChatFormatting.DARK_BLUE))),
                    Map.entry(ModItems.GIN_WHEAT_GLASS.get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv40").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.wheat").withStyle(ChatFormatting.YELLOW))),
                    Map.entry(ModItems.GIN_BARLEY_GLASS.get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv40").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.barley").withStyle(ChatFormatting.GOLD))),

                    Map.entry(ModItems.TEQUILA_GLASS.get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv40").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.blanco").withStyle(ChatFormatting.AQUA))),
                    Map.entry(ModItems.TEQUILA_AGED_GLASS.get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv40").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.anejo").withStyle(ChatFormatting.DARK_AQUA))),

                    Map.entry(ModFluids.WORT_BARLEY.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.barley").withStyle(ChatFormatting.GOLD))),
                    Map.entry(ModFluids.WORT_BARLEY_HOPPED.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.barley").withStyle(ChatFormatting.GOLD),
                            Component.translatable("tooltip.tadackosdrinks.hopped").withStyle(ChatFormatting.DARK_GREEN))),
                    Map.entry(ModFluids.WORT_BARLEY_BOILED.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.barley").withStyle(ChatFormatting.GOLD),
                            Component.translatable("tooltip.tadackosdrinks.boiled").withStyle(ChatFormatting.AQUA))),
                    Map.entry(ModFluids.WORT_BARLEY_BOILED_HOPPED.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.barley").withStyle(ChatFormatting.GOLD),
                            Component.translatable("tooltip.tadackosdrinks.boiled").withStyle(ChatFormatting.AQUA),
                            Component.translatable("tooltip.tadackosdrinks.hopped").withStyle(ChatFormatting.DARK_GREEN))),
                    Map.entry(ModFluids.WORT_WHEAT.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.wheat").withStyle(ChatFormatting.YELLOW))),
                    Map.entry(ModFluids.WORT_WHEAT_HOPPED.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.wheat").withStyle(ChatFormatting.YELLOW),
                            Component.translatable("tooltip.tadackosdrinks.hopped").withStyle(ChatFormatting.DARK_GREEN))),
                    Map.entry(ModFluids.WORT_WHEAT_BOILED.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.wheat").withStyle(ChatFormatting.YELLOW),
                            Component.translatable("tooltip.tadackosdrinks.boiled").withStyle(ChatFormatting.AQUA))),
                    Map.entry(ModFluids.WORT_WHEAT_BOILED_HOPPED.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.wheat").withStyle(ChatFormatting.YELLOW),
                            Component.translatable("tooltip.tadackosdrinks.boiled").withStyle(ChatFormatting.AQUA),
                            Component.translatable("tooltip.tadackosdrinks.hopped").withStyle(ChatFormatting.DARK_GREEN))),

                    Map.entry(ModFluids.WASH_BARLEY.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv5").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.barley").withStyle(ChatFormatting.GOLD))),
                    Map.entry(ModFluids.WASH_WHEAT.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv5").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.wheat").withStyle(ChatFormatting.YELLOW))),
                    Map.entry(ModFluids.BEER_BARLEY.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv5").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.barley").withStyle(ChatFormatting.GOLD))),
                    Map.entry(ModFluids.BEER_BARLEY_HOPPED.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv5").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.barley").withStyle(ChatFormatting.GOLD),
                            Component.translatable("tooltip.tadackosdrinks.hopped").withStyle(ChatFormatting.DARK_GREEN))),
                    Map.entry(ModFluids.BEER_WHEAT.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv5").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.wheat").withStyle(ChatFormatting.YELLOW))),
                    Map.entry(ModFluids.BEER_WHEAT_HOPPED.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv5").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.wheat").withStyle(ChatFormatting.YELLOW),
                            Component.translatable("tooltip.tadackosdrinks.hopped").withStyle(ChatFormatting.DARK_GREEN))),

                    Map.entry(ModFluids.MUST_RED.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.red").withStyle(ChatFormatting.DARK_RED))),
                    Map.entry(ModFluids.MUST_RED_FERMENTED.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv12").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.red").withStyle(ChatFormatting.DARK_RED))),
                    Map.entry(ModFluids.JUICE_GRAPE_ROSE.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.rose").withStyle(ChatFormatting.LIGHT_PURPLE))),
                    Map.entry(ModFluids.MUST_WHITE.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.white").withStyle(ChatFormatting.YELLOW))),
                    Map.entry(ModFluids.MUST_WHITE_FERMENTED.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv12").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.white").withStyle(ChatFormatting.YELLOW))),
                    Map.entry(ModFluids.JUICE_GRAPE_WHITE.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.white").withStyle(ChatFormatting.YELLOW))),

                    Map.entry(ModFluids.WINE_RED.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv12").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.red").withStyle(ChatFormatting.DARK_RED))),
                    Map.entry(ModFluids.WINE_RED_AGED.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv12").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.red").withStyle(ChatFormatting.DARK_RED),
                            Component.translatable("tooltip.tadackosdrinks.aged").withStyle(ChatFormatting.DARK_AQUA))),
                    Map.entry(ModFluids.WINE_ROSE.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv12").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.rose").withStyle(ChatFormatting.LIGHT_PURPLE))),
                    Map.entry(ModFluids.WINE_ROSE_AGED.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv12").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.rose").withStyle(ChatFormatting.LIGHT_PURPLE),
                            Component.translatable("tooltip.tadackosdrinks.aged").withStyle(ChatFormatting.DARK_AQUA))),
                    Map.entry(ModFluids.WINE_ORANGE.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv12").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.orange").withStyle(ChatFormatting.GOLD))),
                    Map.entry(ModFluids.WINE_ORANGE_AGED.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv12").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.orange").withStyle(ChatFormatting.GOLD),
                            Component.translatable("tooltip.tadackosdrinks.aged").withStyle(ChatFormatting.DARK_AQUA))),
                    Map.entry(ModFluids.WINE_WHITE.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv12").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.white").withStyle(ChatFormatting.YELLOW))),
                    Map.entry(ModFluids.WINE_WHITE_AGED.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv12").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.white").withStyle(ChatFormatting.YELLOW),
                            Component.translatable("tooltip.tadackosdrinks.aged").withStyle(ChatFormatting.DARK_AQUA))),

                    Map.entry(ModFluids.CIDER.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv5").withStyle(ChatFormatting.GRAY))),
                    Map.entry(ModFluids.CIDER_AGED.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv5").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.aged").withStyle(ChatFormatting.DARK_AQUA))),

                    Map.entry(ModFluids.MEAD.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv12").withStyle(ChatFormatting.GRAY))),
                    Map.entry(ModFluids.MEAD_AGED.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv12").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.aged").withStyle(ChatFormatting.DARK_AQUA))),

                    Map.entry(ModFluids.SPIRIT_WHEAT_LOW.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv30").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.wheat").withStyle(ChatFormatting.YELLOW))),
                    Map.entry(ModFluids.SPIRIT_WHEAT_MID.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv60").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.wheat").withStyle(ChatFormatting.YELLOW))),
                    Map.entry(ModFluids.SPIRIT_WHEAT_HIGH.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv80").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.wheat").withStyle(ChatFormatting.YELLOW))),
                    Map.entry(ModFluids.SPIRIT_BARLEY_LOW.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv30").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.barley").withStyle(ChatFormatting.GOLD))),
                    Map.entry(ModFluids.SPIRIT_BARLEY_MID.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv60").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.barley").withStyle(ChatFormatting.GOLD))),
                    Map.entry(ModFluids.SPIRIT_BARLEY_HIGH.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv80").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.barley").withStyle(ChatFormatting.GOLD))),
                    Map.entry(ModFluids.SPIRIT_GRAPE_LOW.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv30").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.grape").withStyle(ChatFormatting.DARK_PURPLE))),
                    Map.entry(ModFluids.SPIRIT_GRAPE_MID.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv60").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.grape").withStyle(ChatFormatting.DARK_PURPLE))),
                    Map.entry(ModFluids.SPIRIT_GRAPE_HIGH.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv80").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.grape").withStyle(ChatFormatting.DARK_PURPLE))),
                    Map.entry(ModFluids.SPIRIT_APPLE_LOW.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv30").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.apple").withStyle(ChatFormatting.RED))),
                    Map.entry(ModFluids.SPIRIT_APPLE_MID.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv60").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.apple").withStyle(ChatFormatting.RED))),
                    Map.entry(ModFluids.SPIRIT_APPLE_HIGH.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv80").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.apple").withStyle(ChatFormatting.RED))),
                    Map.entry(ModFluids.SPIRIT_HONEY_LOW.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv30").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.honey").withStyle(ChatFormatting.LIGHT_PURPLE))),
                    Map.entry(ModFluids.SPIRIT_HONEY_MID.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv60").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.honey").withStyle(ChatFormatting.LIGHT_PURPLE))),
                    Map.entry(ModFluids.SPIRIT_HONEY_HIGH.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv80").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.honey").withStyle(ChatFormatting.LIGHT_PURPLE))),
                    Map.entry(ModFluids.SPIRIT_SUGARCANE_JUICE_LOW.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv30").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.sugarcane_juice").withStyle(ChatFormatting.GREEN))),
                    Map.entry(ModFluids.SPIRIT_SUGARCANE_JUICE_MID.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv60").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.sugarcane_juice").withStyle(ChatFormatting.GREEN))),
                    Map.entry(ModFluids.SPIRIT_SUGARCANE_MOLASSES_LOW.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv30").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.sugarcane_molasses").withStyle(ChatFormatting.DARK_GREEN))),
                    Map.entry(ModFluids.SPIRIT_SUGARCANE_MOLASSES_MID.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv60").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.sugarcane_molasses").withStyle(ChatFormatting.DARK_GREEN))),
                    Map.entry(ModFluids.SPIRIT_POTATO_LOW.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv30").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.potato").withStyle(ChatFormatting.DARK_BLUE))),
                    Map.entry(ModFluids.SPIRIT_POTATO_MID.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv60").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.potato").withStyle(ChatFormatting.DARK_BLUE))),
                    Map.entry(ModFluids.SPIRIT_POTATO_HIGH.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv80").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.potato").withStyle(ChatFormatting.DARK_BLUE))),
                    Map.entry(ModFluids.SPIRIT_AGAVE_LOW.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv30").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.agave").withStyle(ChatFormatting.AQUA))),
                    Map.entry(ModFluids.SPIRIT_AGAVE_HIGH.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv80").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.agave").withStyle(ChatFormatting.AQUA))),

                    Map.entry(ModFluids.CONCENTRATED_WHISKY_WHEAT.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv80").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.wheat").withStyle(ChatFormatting.YELLOW))),
                    Map.entry(ModFluids.WHISKY_WHEAT.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv40").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.wheat").withStyle(ChatFormatting.YELLOW))),
                    Map.entry(ModFluids.CONCENTRATED_WHISKY_BARLEY.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv80").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.barley").withStyle(ChatFormatting.GOLD))),
                    Map.entry(ModFluids.WHISKY_BARLEY.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv40").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.barley").withStyle(ChatFormatting.GOLD))),

                    Map.entry(ModFluids.CONCENTRATED_BRANDY_GRAPE.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv80").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.grape").withStyle(ChatFormatting.DARK_PURPLE))),
                    Map.entry(ModFluids.BRANDY_GRAPE.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv40").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.grape").withStyle(ChatFormatting.DARK_PURPLE))),
                    Map.entry(ModFluids.CONCENTRATED_BRANDY_APPLE.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv80").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.apple").withStyle(ChatFormatting.RED))),
                    Map.entry(ModFluids.BRANDY_APPLE.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv40").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.apple").withStyle(ChatFormatting.RED))),

                    Map.entry(ModFluids.WASH_SUGARCANE_JUICE.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv12").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.sugarcane_juice").withStyle(ChatFormatting.GREEN))),
                    Map.entry(ModFluids.WASH_SUGARCANE_MOLASSES.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv12").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.sugarcane_molasses").withStyle(ChatFormatting.DARK_GREEN))),

                    Map.entry(ModFluids.CONCENTRATED_RUM_JUICE.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv80").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.sugarcane_juice").withStyle(ChatFormatting.GREEN),
                            Component.translatable("tooltip.tadackosdrinks.light").withStyle(ChatFormatting.AQUA))),
                    Map.entry(ModFluids.RUM_JUICE.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv40").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.sugarcane_juice").withStyle(ChatFormatting.GREEN),
                            Component.translatable("tooltip.tadackosdrinks.light").withStyle(ChatFormatting.AQUA))),
                    Map.entry(ModFluids.CONCENTRATED_RUM_JUICE_AGED.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv80").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.sugarcane_juice").withStyle(ChatFormatting.GREEN),
                            Component.translatable("tooltip.tadackosdrinks.dark").withStyle(ChatFormatting.DARK_AQUA))),
                    Map.entry(ModFluids.RUM_JUICE_AGED.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv40").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.sugarcane_juice").withStyle(ChatFormatting.GREEN),
                            Component.translatable("tooltip.tadackosdrinks.dark").withStyle(ChatFormatting.DARK_AQUA))),
                    Map.entry(ModFluids.CONCENTRATED_RUM_MOLASSES.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv80").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.sugarcane_molasses").withStyle(ChatFormatting.DARK_GREEN),
                            Component.translatable("tooltip.tadackosdrinks.light").withStyle(ChatFormatting.AQUA))),
                    Map.entry(ModFluids.RUM_MOLASSES.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv40").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.sugarcane_molasses").withStyle(ChatFormatting.DARK_GREEN),
                            Component.translatable("tooltip.tadackosdrinks.light").withStyle(ChatFormatting.AQUA))),
                    Map.entry(ModFluids.CONCENTRATED_RUM_MOLASSES_AGED.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv80").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.sugarcane_molasses").withStyle(ChatFormatting.DARK_GREEN),
                            Component.translatable("tooltip.tadackosdrinks.dark").withStyle(ChatFormatting.DARK_AQUA))),
                    Map.entry(ModFluids.RUM_MOLASSES_AGED.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv40").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.sugarcane_molasses").withStyle(ChatFormatting.DARK_GREEN),
                            Component.translatable("tooltip.tadackosdrinks.dark").withStyle(ChatFormatting.DARK_AQUA))),

                    Map.entry(ModFluids.WASH_POTATO.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv12").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.potato").withStyle(ChatFormatting.DARK_BLUE))),

                    Map.entry(ModFluids.CONCENTRATED_VODKA_GRAPE.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv95").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.grape").withStyle(ChatFormatting.DARK_PURPLE))),
                    Map.entry(ModFluids.VODKA_GRAPE.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv40").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.grape").withStyle(ChatFormatting.DARK_PURPLE))),
                    Map.entry(ModFluids.CONCENTRATED_VODKA_APPLE.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv95").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.apple").withStyle(ChatFormatting.RED))),
                    Map.entry(ModFluids.VODKA_APPLE.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv40").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.apple").withStyle(ChatFormatting.RED))),
                    Map.entry(ModFluids.CONCENTRATED_VODKA_HONEY.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv95").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.honey").withStyle(ChatFormatting.LIGHT_PURPLE))),
                    Map.entry(ModFluids.VODKA_HONEY.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv40").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.honey").withStyle(ChatFormatting.LIGHT_PURPLE))),
                    Map.entry(ModFluids.CONCENTRATED_VODKA_SUGARCANE_JUICE.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv95").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.sugarcane_juice").withStyle(ChatFormatting.GREEN))),
                    Map.entry(ModFluids.VODKA_SUGARCANE_JUICE.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv40").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.sugarcane_juice").withStyle(ChatFormatting.GREEN))),
                    Map.entry(ModFluids.CONCENTRATED_VODKA_SUGARCANE_MOLASSES.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv95").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.sugarcane_molasses").withStyle(ChatFormatting.DARK_GREEN))),
                    Map.entry(ModFluids.VODKA_SUGARCANE_MOLASSES.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv40").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.sugarcane_molasses").withStyle(ChatFormatting.DARK_GREEN))),
                    Map.entry(ModFluids.CONCENTRATED_VODKA_POTATO.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv95").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.potato").withStyle(ChatFormatting.DARK_BLUE))),
                    Map.entry(ModFluids.VODKA_POTATO.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv40").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.potato").withStyle(ChatFormatting.DARK_BLUE))),
                    Map.entry(ModFluids.CONCENTRATED_VODKA_AGAVE.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv95").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.agave").withStyle(ChatFormatting.AQUA))),
                    Map.entry(ModFluids.VODKA_AGAVE.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv40").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.agave").withStyle(ChatFormatting.AQUA))),
                    Map.entry(ModFluids.CONCENTRATED_VODKA_WHEAT.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv95").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.wheat").withStyle(ChatFormatting.YELLOW))),
                    Map.entry(ModFluids.VODKA_WHEAT.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv40").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.wheat").withStyle(ChatFormatting.YELLOW))),
                    Map.entry(ModFluids.CONCENTRATED_VODKA_BARLEY.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv95").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.barley").withStyle(ChatFormatting.GOLD))),
                    Map.entry(ModFluids.VODKA_BARLEY.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv40").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.barley").withStyle(ChatFormatting.GOLD))),

                    Map.entry(ModFluids.SPIRIT_GRAPE_MID_SPICED.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv60").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.grape").withStyle(ChatFormatting.DARK_PURPLE),
                            Component.translatable("tooltip.tadackosdrinks.spiced").withStyle(ChatFormatting.BLUE))),
                    Map.entry(ModFluids.CONCENTRATED_GIN_GRAPE.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv80").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.grape").withStyle(ChatFormatting.DARK_PURPLE))),
                    Map.entry(ModFluids.GIN_GRAPE.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv40").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.grape").withStyle(ChatFormatting.DARK_PURPLE))),
                    Map.entry(ModFluids.SPIRIT_APPLE_MID_SPICED.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv60").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.apple").withStyle(ChatFormatting.RED),
                            Component.translatable("tooltip.tadackosdrinks.spiced").withStyle(ChatFormatting.BLUE))),
                    Map.entry(ModFluids.CONCENTRATED_GIN_APPLE.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv80").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.apple").withStyle(ChatFormatting.RED))),
                    Map.entry(ModFluids.GIN_APPLE.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv40").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.apple").withStyle(ChatFormatting.RED))),
                    Map.entry(ModFluids.SPIRIT_HONEY_MID_SPICED.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv60").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.honey").withStyle(ChatFormatting.LIGHT_PURPLE),
                            Component.translatable("tooltip.tadackosdrinks.spiced").withStyle(ChatFormatting.BLUE))),
                    Map.entry(ModFluids.CONCENTRATED_GIN_HONEY.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv80").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.honey").withStyle(ChatFormatting.LIGHT_PURPLE))),
                    Map.entry(ModFluids.GIN_HONEY.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv40").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.honey").withStyle(ChatFormatting.LIGHT_PURPLE))),
                    Map.entry(ModFluids.SPIRIT_SUGARCANE_JUICE_MID_SPICED.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv60").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.sugarcane_juice").withStyle(ChatFormatting.GREEN),
                            Component.translatable("tooltip.tadackosdrinks.spiced").withStyle(ChatFormatting.BLUE))),
                    Map.entry(ModFluids.CONCENTRATED_GIN_SUGARCANE_JUICE.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv80").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.sugarcane_juice").withStyle(ChatFormatting.GREEN))),
                    Map.entry(ModFluids.GIN_SUGARCANE_JUICE.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv40").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.sugarcane_juice").withStyle(ChatFormatting.GREEN))),
                    Map.entry(ModFluids.SPIRIT_SUGARCANE_MOLASSES_MID_SPICED.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv60").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.sugarcane_molasses").withStyle(ChatFormatting.DARK_GREEN),
                            Component.translatable("tooltip.tadackosdrinks.spiced").withStyle(ChatFormatting.BLUE))),
                    Map.entry(ModFluids.CONCENTRATED_GIN_SUGARCANE_MOLASSES.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv80").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.sugarcane_molasses").withStyle(ChatFormatting.DARK_GREEN))),
                    Map.entry(ModFluids.GIN_SUGARCANE_MOLASSES.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv40").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.sugarcane_molasses").withStyle(ChatFormatting.DARK_GREEN))),
                    Map.entry(ModFluids.SPIRIT_POTATO_MID_SPICED.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv60").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.potato").withStyle(ChatFormatting.DARK_BLUE),
                            Component.translatable("tooltip.tadackosdrinks.spiced").withStyle(ChatFormatting.BLUE))),
                    Map.entry(ModFluids.CONCENTRATED_GIN_POTATO.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv80").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.potato").withStyle(ChatFormatting.DARK_BLUE))),
                    Map.entry(ModFluids.GIN_POTATO.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv40").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.potato").withStyle(ChatFormatting.DARK_BLUE))),
                    Map.entry(ModFluids.SPIRIT_AGAVE_MID_SPICED.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv60").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.agave").withStyle(ChatFormatting.AQUA),
                            Component.translatable("tooltip.tadackosdrinks.spiced").withStyle(ChatFormatting.BLUE))),
                    Map.entry(ModFluids.CONCENTRATED_GIN_AGAVE.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv80").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.agave").withStyle(ChatFormatting.AQUA))),
                    Map.entry(ModFluids.GIN_AGAVE.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv40").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.agave").withStyle(ChatFormatting.AQUA))),
                    Map.entry(ModFluids.SPIRIT_WHEAT_MID_SPICED.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv60").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.wheat").withStyle(ChatFormatting.YELLOW),
                            Component.translatable("tooltip.tadackosdrinks.spiced").withStyle(ChatFormatting.BLUE))),
                    Map.entry(ModFluids.CONCENTRATED_GIN_WHEAT.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv80").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.wheat").withStyle(ChatFormatting.YELLOW))),
                    Map.entry(ModFluids.GIN_WHEAT.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv40").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.wheat").withStyle(ChatFormatting.YELLOW))),
                    Map.entry(ModFluids.SPIRIT_BARLEY_MID_SPICED.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv60").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.barley").withStyle(ChatFormatting.GOLD),
                            Component.translatable("tooltip.tadackosdrinks.spiced").withStyle(ChatFormatting.BLUE))),
                    Map.entry(ModFluids.CONCENTRATED_GIN_BARLEY.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv80").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.barley").withStyle(ChatFormatting.GOLD))),
                    Map.entry(ModFluids.GIN_BARLEY.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv40").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.barley").withStyle(ChatFormatting.GOLD))),

                    Map.entry(ModFluids.WASH_AGAVE.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv5").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.agave").withStyle(ChatFormatting.AQUA))),

                    Map.entry(ModFluids.CONCENTRATED_TEQUILA.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv60").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.blanco").withStyle(ChatFormatting.AQUA))),
                    Map.entry(ModFluids.TEQUILA.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv40").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.blanco").withStyle(ChatFormatting.AQUA))),
                    Map.entry(ModFluids.CONCENTRATED_TEQUILA_AGED.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv60").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.anejo").withStyle(ChatFormatting.DARK_AQUA))),
                    Map.entry(ModFluids.TEQUILA_AGED.bucket().get(), List.of(Component.translatable("tooltip.tadackosdrinks.abv40").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.tadackosdrinks.anejo").withStyle(ChatFormatting.DARK_AQUA)))
            );
        }
        return TOOLTIP_MAP;
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        Map<Item, List<Component>> tooltipMap = getTooltipMap();

        ItemStack stack = event.getItemStack();
        List<Component> tooltips = tooltipMap.get(stack.getItem());
        int insertIndex = 1;

        // For the keg, show the same descriptor tags as whatever item represents the fluid currently inside it,
        // inserted after the "Contains: ..." line KegItem adds (rather than right under the name)
        if (stack.getItem() == ModItems.KEG.get()) {
            FluidStack fluid = KegItem.getFluidStack(stack);
            if (!fluid.isEmpty()) {
                // Derive bucket item from fluid registry name
                ResourceLocation fluidId = ForgeRegistries.FLUIDS.getKey(fluid.getFluid());
                if (fluidId != null) {
                    String path = fluidId.getPath().replace("_fluid", "");
                    ResourceLocation bucketId = new ResourceLocation(fluidId.getNamespace(), path + "_bucket");
                    Item bucketItem = ForgeRegistries.ITEMS.getValue(bucketId);
                    if (bucketItem != null && bucketItem != Items.AIR) {
                        tooltips = tooltipMap.get(bucketItem);
                        insertIndex = 2;
                    }
                }
            }
        }

        if (tooltips != null) {
            event.getToolTip().addAll(insertIndex, tooltips); // Ensures all tooltips are added correctly
        }
    }
}