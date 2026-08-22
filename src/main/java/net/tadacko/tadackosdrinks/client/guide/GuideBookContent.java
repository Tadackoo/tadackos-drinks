package net.tadacko.tadackosdrinks.client.guide;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.tadacko.tadackosdrinks.TadackosDrinks;
import net.tadacko.tadackosdrinks.block.*;
import net.tadacko.tadackosdrinks.effect.ModEffects;
import net.tadacko.tadackosdrinks.fluid.ModFluids;
import net.tadacko.tadackosdrinks.item.ModItems;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

/**
 * Single source of truth for the guide book's content. Add/edit pages here only -
 * GuideBookScreen just renders whatever list this returns.
 *
 * All user-facing text is resolved through translation keys via t()/tc() at build time
 * (I18n.get is client-only, hence @OnlyIn). Keys live under "tadackosdrinks.guide." in
 * the lang files; t() auto-prepends that prefix. Since buildPages() is called fresh every
 * time the screen opens, the book always reflects the currently selected language - it just
 * won't re-translate live while already open.
 */
@OnlyIn(Dist.CLIENT)
public final class GuideBookContent {
    private GuideBookContent() {}

    private static final String KEY_PREFIX = "tadackosdrinks.guide.";

    /** Resolves a page-local key (auto-prefixed) to the localized string. */
    private static String t(String key) {
        return I18n.get(KEY_PREFIX + key);
    }

    private static final ResourceLocation FERMENTING_BARREL_RECIPE = new ResourceLocation(TadackosDrinks.MOD_ID, "fermenting_barrel_oak");
    private static final ResourceLocation CRUSHER_RECIPE = new ResourceLocation(TadackosDrinks.MOD_ID, "manual_crusher_oak");
    private static final ResourceLocation PRESS_RECIPE = new ResourceLocation(TadackosDrinks.MOD_ID, "manual_press_oak");
    private static final ResourceLocation POT_RECIPE = new ResourceLocation(TadackosDrinks.MOD_ID, "copper_pot");
    private static final ResourceLocation POT_STILL_RECIPE = new ResourceLocation(TadackosDrinks.MOD_ID, "pot_still");
    private static final ResourceLocation COLUMN_STILL_RECIPE = new ResourceLocation(TadackosDrinks.MOD_ID, "column_still");

    private static final ResourceLocation KEG_RECIPE = new ResourceLocation(TadackosDrinks.MOD_ID, "keg");
    private static final ResourceLocation BEER_GLASS_RECIPE = new ResourceLocation(TadackosDrinks.MOD_ID, "beer_glass_empty");
    private static final ResourceLocation WINE_GLASS_RECIPE = new ResourceLocation(TadackosDrinks.MOD_ID, "wine_glass_empty");
    private static final ResourceLocation WHISKY_GLASS_RECIPE = new ResourceLocation(TadackosDrinks.MOD_ID, "whisky_glass_empty");
    private static final ResourceLocation BRANDY_GLASS_RECIPE = new ResourceLocation(TadackosDrinks.MOD_ID, "brandy_glass_empty");
    private static final ResourceLocation SHOT_GLASS_RECIPE = new ResourceLocation(TadackosDrinks.MOD_ID, "shot_glass_empty");

    private static final ResourceLocation WORT_RECIPE = new ResourceLocation(TadackosDrinks.MOD_ID, "wort_wheat_bucket");
    private static final ResourceLocation WORT_HOPPED_RECIPE = new ResourceLocation(TadackosDrinks.MOD_ID, "wort_wheat_hopped_bucket");
    private static final ResourceLocation HONEY_RECIPE = new ResourceLocation(TadackosDrinks.MOD_ID, "diluted_honey_bucket");
    private static final ResourceLocation MOLASSES_RECIPE = new ResourceLocation(TadackosDrinks.MOD_ID, "diluted_molasses_sugarcane_bucket");
    private static final ResourceLocation MASH_RECIPE = new ResourceLocation(TadackosDrinks.MOD_ID, "mash_potato_bucket_wheat");
    private static final ResourceLocation WHISKY_RECIPE = new ResourceLocation(TadackosDrinks.MOD_ID, "whisky_wheat_bucket");
    private static final ResourceLocation BRANDY_RECIPE = new ResourceLocation(TadackosDrinks.MOD_ID, "brandy_apple_bucket");
    private static final ResourceLocation RUM_RECIPE = new ResourceLocation(TadackosDrinks.MOD_ID, "rum_juice_aged_bucket");
    private static final ResourceLocation VODKA_RECIPE = new ResourceLocation(TadackosDrinks.MOD_ID, "vodka_grape_bucket");
    private static final ResourceLocation SPICED_RECIPE = new ResourceLocation(TadackosDrinks.MOD_ID, "spirit_wheat_mid_spiced_bucket");
    private static final ResourceLocation GIN_RECIPE = new ResourceLocation(TadackosDrinks.MOD_ID, "gin_grape_bucket");
    private static final ResourceLocation TEQUILA_RECIPE = new ResourceLocation(TadackosDrinks.MOD_ID, "tequila_aged_bucket");

    private static final String[] WOOD_TYPES = {
            "oak", "spruce", "birch", "jungle", "acacia", "dark_oak", "mangrove", "cherry", "bamboo", "crimson", "warped"
    };

    /** Builds "{prefix}_{wood}" recipe ids for all 9 wood types, e.g. woodRecipeVariants("trellis") -> trellis_oak, trellis_spruce, ... */
    private static ResourceLocation[] woodRecipeVariants(String prefix) {
        ResourceLocation[] ids = new ResourceLocation[WOOD_TYPES.length];
        for (int i = 0; i < WOOD_TYPES.length; i++) ids[i] = new ResourceLocation(TadackosDrinks.MOD_ID, prefix + "_" + WOOD_TYPES[i]);
        return ids;
    }

    static {
        GuideRecipes.registerFamily(FERMENTING_BARREL_RECIPE, List.of(woodRecipeVariants("fermenting_barrel")));
        GuideRecipes.registerFamily(CRUSHER_RECIPE, List.of(woodRecipeVariants("manual_crusher")));
        GuideRecipes.registerFamily(PRESS_RECIPE, List.of(woodRecipeVariants("manual_press")));
        GuideRecipes.registerFamily(PRESS_RECIPE, List.of(woodRecipeVariants("manual_press")));
    }

    private static final int PAGE_W = 110;

    public static List<GuidePage> buildPages(IntConsumer onNavigate) {
        List<GuidePage> pages = new ArrayList<>();

        pages.add(BackgroundDecoratedPage.wrap(
                new TocPage(t("toc.title"), List.of(
                        new TocPage.Entry(t("p0.toc1"), 2),
                        new TocPage.Entry(t("p0.toc2"), 5),
                        new TocPage.Entry(t("p0.toc3"), 12),
                        new TocPage.Entry(t("p0.toc4"), 16),
                        new TocPage.Entry(t("p0.toc5"), 18),
                        new TocPage.Entry(t("p0.toc6"), 20),
                        new TocPage.Entry(t("p0.toc7"), 22),
                        new TocPage.Entry(t("p0.toc8"), 24),
                        new TocPage.Entry(t("p0.toc9"), 26),
                        new TocPage.Entry(t("p0.toc10"), 30),
                        new TocPage.Entry(t("p0.toc11"), 32),
                        new TocPage.Entry(t("p0.toc12"), 34),
                        new TocPage.Entry(t("p0.toc13"), 36)
                ), onNavigate),
                PageBackground.of(
                        BackgroundIcon.item(new ItemStack(ModItems.GUIDE_BOOK.get()), 0, -5, 90, 0.25f)
                ))); // 0

        pages.add(BackgroundDecoratedPage.wrap(
                new TocPage(t("toc.title"), List.of(
                        new TocPage.Entry(t("p1.toc1"), 38),
                        new TocPage.Entry(t("p1.toc2"), 40),
                        new TocPage.Entry(t("p1.toc3"), 42)
                ), onNavigate),
                PageBackground.of(
                        BackgroundIcon.item(new ItemStack(ModItems.GUIDE_BOOK.get()), 0, -5, 90, 0.25f)
                ))); // 1

        pages.add(BackgroundDecoratedPage.wrap(
                new CompositePage(t("p2.title"), 2,
                        new TextPage("", t("p2.body")),
                        new RecipeListPage(List.of(new ResourceLocation(TadackosDrinks.MOD_ID, "guide_book")))),
                PageBackground.of(
                        BackgroundIcon.item(new ItemStack(ModItems.GUIDE_BOOK.get()), 0, -5, 90, 0.25f)
                ))); // 2

        pages.add(BackgroundDecoratedPage.wrap(
                new TextPage(t("p3.title"), t("p3.body")),
                PageBackground.of(
                        BackgroundIcon.item(new ItemStack(ModItems.GUIDE_BOOK.get()), 0, -5, 90, 0.25f)
                ))); // 3

        pages.add(BackgroundDecoratedPage.wrap(
                new TextPage("", ""),
                PageBackground.of(
                        BackgroundIcon.item(new ItemStack(ModItems.GUIDE_BOOK.get()), 0, -5, 90, 0.25f)
                ))); // Empty page for formatting purposes 4

        pages.add(BackgroundDecoratedPage.wrap(
                new CompositePage(t("p5.title"), 2,
                        new TextPage("", t("p5.body")),
                        new RecipeListPage(List.of(new ResourceLocation(TadackosDrinks.MOD_ID, "bread_barley"))),
                        new ScenePage("", buildBarleyScene(), 50, 70)),
                PageBackground.of(
                        BackgroundIcon.item(new ItemStack(ModItems.BARLEY.get()), 0, -5, 90, 0.25f)
                ))); // 5

        pages.add(BackgroundDecoratedPage.wrap(
                new CompositePage(t("p6.title"), 2,
                        new TextPage("", t("p6.body")),
                        new ScenePage("", buildHopScene(), 50, 70)),
                PageBackground.of(
                        BackgroundIcon.item(new ItemStack(ModItems.HOPS.get()), 0, -5, 90, 0.25f)
                ))); // 6
        pages.add(BackgroundDecoratedPage.wrap(
                new RecipeListPage(
                        RecipeSlot.cycling(1, woodRecipeVariants("trellis")),
                        RecipeSlot.of(new ResourceLocation(TadackosDrinks.MOD_ID, "rope_item"))
                ) { @Override public String rawTitle() { return t("p6.title"); }},
                PageBackground.of(
                        BackgroundIcon.item(new ItemStack(ModItems.HOPS.get()), 0, -5, 90, 0.25f)
                ))); // 7

        pages.add(BackgroundDecoratedPage.wrap(
                new CompositePage(t("p8.title"), 2,
                        new TextPage("", t("p8.body")),
                        new ScenePage("", buildGrapeScene(), 50, 70)),
                PageBackground.of(
                        BackgroundIcon.item(new ItemStack(ModItems.GRAPES_RED.get()), 0, -5, 90, 0.25f)
                ))); // 8
        pages.add(BackgroundDecoratedPage.wrap(
                new RecipeListPage(
                        RecipeSlot.cycling(1, woodRecipeVariants("trellis")),
                        RecipeSlot.of(new ResourceLocation(TadackosDrinks.MOD_ID, "trellis_wire_item"))
                ) { @Override public String rawTitle() { return t("p8.title"); }},
                PageBackground.of(
                        BackgroundIcon.item(new ItemStack(ModItems.GRAPES_RED.get()), 0, -5, 90, 0.25f)
                ))); // 9

        pages.add(BackgroundDecoratedPage.wrap(
                new CompositePage(t("p10.title"), 2,
                        new TextPage("", t("p10.body")),
                        new ScenePage("", buildJuniperScene(), 50, 70)),
                PageBackground.of(
                        BackgroundIcon.item(new ItemStack(ModItems.JUNIPER_BERRIES.get()), 0, -5, 90, 0.25f)
                ))); // 10

        pages.add(BackgroundDecoratedPage.wrap(
                new CompositePage(t("p11.title"), 2,
                        new TextPage("", t("p11.body")),
                        new ScenePage("", buildAgaveScene(), 50, 70)),
                PageBackground.of(
                        BackgroundIcon.item(new ItemStack(ModItems.AGAVE_PINA.get()), 0, -5, 90, 0.25f)
                ))); // 11

        pages.add(BackgroundDecoratedPage.wrap(
                new TextPage(t("p12.title"), t("p12.body")),
                PageBackground.of(
                        BackgroundIcon.effect(ModEffects.INEBRIATION.get(), 0, -5, 90, 0.25f)
                ))); // 12
        pages.add(BackgroundDecoratedPage.wrap(
                new TablePage(t("p12.title"),
                        List.of(
                                List.of(t("p12+.table.1.1"), t("p12+.table.1.2")),
                                List.of(t("p12+.table.2.1"), t("p12+.table.2.2")),
                                List.of(t("p12+.table.3.1"), t("p12+.table.3.2")),
                                List.of(t("p12+.table.4.1"), t("p12+.table.4.2")),
                                List.of(t("p12+.table.5.1"), t("p12+.table.5.2")),
                                List.of(t("p12+.table.6.1"), t("p12+.table.6.2"))
                        )),
                PageBackground.of(
                        BackgroundIcon.effect(ModEffects.INEBRIATION.get(), 0, -5, 90, 0.25f)
                ))); // 13

        pages.add(BackgroundDecoratedPage.wrap(
                new TablePage("", t("p13.body"),
                        List.of(
                                List.of(t("p13.table.1.1"), t("p13.table.1.2")),
                                List.of(t("p13.table.2.1"), t("p13.table.2.2")),
                                List.of(t("p13.table.3.1"), t("p13.table.3.2"))
                        ),
                        t("p13.footnote")),
                PageBackground.of(
                        BackgroundIcon.effect(ModEffects.HANGOVER.get(), 0, -5, 90, 0.25f)
                ))); // 14
        pages.add(BackgroundDecoratedPage.wrap(
                new CompositePage(t("p13.title"), 2,
                        new TextPage("", t("p13+.body")),
                        new RecipeListPage(RecipeSlot.cycling(1, new ResourceLocation(TadackosDrinks.MOD_ID, "hangover_cure_slime_ball"),
                                new ResourceLocation(TadackosDrinks.MOD_ID, "hangover_cure_magma_cream"),
                                new ResourceLocation(TadackosDrinks.MOD_ID, "hangover_cure_nether_wart"),
                                new ResourceLocation(TadackosDrinks.MOD_ID, "hangover_cure_chorus_fruit")))),
                PageBackground.of(
                        BackgroundIcon.effect(ModEffects.HANGOVER.get(), 0, -5, 90, 0.25f)
                ))); // 15

        pages.add(BackgroundDecoratedPage.wrap(
                new FlowchartPage(t("p14.title"), buildYeastFlow()),
                PageBackground.of(
                        BackgroundIcon.block(ModBlocks.FERMENTING_BARREL_OAK.get().defaultBlockState(), 0, -5, 120, 0.25f)
                ))); // 16
        pages.add(BackgroundDecoratedPage.wrap(
                new FlowchartPage(t("p15.title"), buildSugarFlow()),
                PageBackground.of(
                        BackgroundIcon.block(ModBlocks.FERMENTING_BARREL_OAK.get().defaultBlockState(), 0, -5, 120, 0.25f)
                ))); // 17

        pages.add(BackgroundDecoratedPage.wrap(
                new FlowchartPage(t("p16.title"), buildBeerFlow()),
                PageBackground.of(
                        BackgroundIcon.block(ModBlocks.FERMENTING_BARREL_OAK.get().defaultBlockState(), 0, -5, 120, 0.25f)
                ))); // 18
        pages.add(BackgroundDecoratedPage.wrap(
                new TablePage(t("p17.title"), List.of(
                        List.of(t("p17.table.1.1"), t("p17.table.1.2")),
                        List.of(t("p17.table.2.1"), t("p17.table.2.2")),
                        List.of(t("p17.table.3.1"), t("p17.table.3.2"))
                )),
                PageBackground.of(
                        BackgroundIcon.effect(MobEffects.DAMAGE_RESISTANCE, 0, -5, 90, 0.25f)
                ))); // 19

        pages.add(BackgroundDecoratedPage.wrap(
                new FlowchartPage(t("p18.title"), buildWineFlow()),
                PageBackground.of(
                        BackgroundIcon.block(ModBlocks.FERMENTING_BARREL_OAK.get().defaultBlockState(), 0, -5, 120, 0.25f)
                ))); // 20
        pages.add(BackgroundDecoratedPage.wrap(
                new TablePage(t("p19.title"),
                        t("p19.body"),
                        List.of(
                                List.of(t("p19.table.1.1"), t("p19.table.1.2")),
                                List.of(t("p19.table.2.1"), t("p19.table.2.2")),
                                List.of(t("p19.table.3.1"), t("p19.table.3.2"))
                        ), ""),
                PageBackground.of(
                        BackgroundIcon.effect(MobEffects.HEALTH_BOOST, -20, 30, 70, 0.25f),
                        BackgroundIcon.effect(MobEffects.ABSORPTION, 20, -40, 70, 0.25f)
                ))); // 21

        pages.add(BackgroundDecoratedPage.wrap(
                new FlowchartPage(t("p20.title"), buildCiderFlow()),
                PageBackground.of(
                        BackgroundIcon.block(ModBlocks.FERMENTING_BARREL_OAK.get().defaultBlockState(), 0, -5, 120, 0.25f)
                ))); // 22
        pages.add(BackgroundDecoratedPage.wrap(
                new TablePage(t("p21.title"), List.of(
                        List.of(t("p21.table.1.1"), t("p21.table.1.2")),
                        List.of(t("p21.table.2.1"), t("p21.table.2.2"))
                )),
                PageBackground.of(
                        BackgroundIcon.effect(MobEffects.DIG_SPEED, 0, -5, 90, 0.25f)
                ))); // 23

        pages.add(BackgroundDecoratedPage.wrap(
                new FlowchartPage(t("p22.title"), buildMeadFlow()),
                PageBackground.of(
                        BackgroundIcon.block(ModBlocks.FERMENTING_BARREL_OAK.get().defaultBlockState(), 0, -5, 120, 0.25f)
                ))); // 24
        pages.add(BackgroundDecoratedPage.wrap(
                new CompositePage(t("p23.title"), 12,
                        new TablePage("", List.of(
                                List.of(t("p23.part1.table.1.1"), t("p23.part1.table.1.2")),
                                List.of(t("p23.part1.table.2.1"), t("p23.part1.table.2.2"))
                        )),
                        new TablePage(t("p23.part2.title"), t("p23.part2.body"),
                                List.of(
                                        List.of(t("p23.part2.table.1.1"), t("p23.part2.table.1.2")),
                                        List.of(t("p23.part2.table.2.1"), t("p23.part2.table.2.2"))
                                ), "")
                ),
                PageBackground.of(
                        BackgroundIcon.effect(ModEffects.WISDOM.get(), 0, -5, 90, 0.25f)
                ))); // 25

        pages.add(BackgroundDecoratedPage.wrap(
                new FlowchartPage(t("p24.title"), buildSugarcaneFlow()),
                PageBackground.of(
                        BackgroundIcon.block(ModBlocks.FERMENTING_BARREL_OAK.get().defaultBlockState(), 0, -5, 120, 0.25f)
                ))); // 26
        pages.add(BackgroundDecoratedPage.wrap(
                new FlowchartPage(t("p25.title"), buildPotatoFlow()),
                PageBackground.of(
                        BackgroundIcon.block(ModBlocks.FERMENTING_BARREL_OAK.get().defaultBlockState(), 0, -5, 120, 0.25f)
                ))); // 27

        pages.add(BackgroundDecoratedPage.wrap(
                new FlowchartPage(t("p26.title"), buildAgaveFlow()),
                PageBackground.of(
                        BackgroundIcon.block(ModBlocks.FERMENTING_BARREL_OAK.get().defaultBlockState(), 0, -5, 120, 0.25f)
                ))); // 28
        pages.add(BackgroundDecoratedPage.wrap(
                new TextPage("", ""),
                PageBackground.of(
                        BackgroundIcon.item(new ItemStack(ModItems.GUIDE_BOOK.get()), 0, -5, 90, 0.25f)
                ))); // Empty page for formatting purposes 29

        pages.add(BackgroundDecoratedPage.wrap(
                new TextPage(t("p28.title"), t("p28.body")),
                PageBackground.of(
                        BackgroundIcon.block(ModBlocks.POT_STILL.get().defaultBlockState(), -20, 20, 120, 0.25f),
                        BackgroundIcon.block(ModBlocks.COLUMN_STILL.get().defaultBlockState().setValue(ColumnStillBlock.PART, ColumnStillPart.MIDDLE),
                                20, -30, 120, 0.25f)
                ))); // 30
        pages.add(BackgroundDecoratedPage.wrap(
                new CompositePage(t("p29.title"), 4,
                        new TablePage("", List.of(
                                List.of(t("p29.part1.table.1.1"), t("p29.part1.table.1.2")),
                                List.of(t("p29.part1.table.2.1"), t("p29.part1.table.2.2")),
                                List.of(t("p29.part1.table.3.1"), t("p29.part1.table.3.2")),
                                List.of(t("p29.part1.table.4.1"), t("p29.part1.table.4.2"))
                        )),
                        new TablePage("", List.of(
                                List.of(t("p29.part2.table.1.1"), t("p29.part2.table.1.2"), t("p29.part2.table.1.3"), t("p29.part2.table.1.4"), t("p29.part2.table.1.5")),
                                List.of(t("p29.part2.table.2.1"), t("p29.part2.table.2.2"), t("p29.part2.table.2.3"), t("p29.part2.table.2.4"), t("p29.part2.table.2.5")),
                                List.of(t("p29.part2.table.3.1"), t("p29.part2.table.3.2"), t("p29.part2.table.3.3"), t("p29.part2.table.3.4"), t("p29.part2.table.3.5")),
                                List.of(t("p29.part2.table.4.1"), t("p29.part2.table.4.2"), t("p29.part2.table.4.3"), t("p29.part2.table.4.4"), t("p29.part2.table.4.5")),
                                List.of(t("p29.part2.table.5.1"), t("p29.part2.table.5.2"), t("p29.part2.table.5.3"), t("p29.part2.table.5.4"), t("p29.part2.table.5.5"))
                        )),
                        new RecipeListPage(List.of(new ResourceLocation(TadackosDrinks.MOD_ID, "condenser")))),
                PageBackground.of(
                        BackgroundIcon.block(ModBlocks.POT_STILL.get().defaultBlockState(), -20, 20, 120, 0.25f),
                        BackgroundIcon.block(ModBlocks.COLUMN_STILL.get().defaultBlockState().setValue(ColumnStillBlock.PART, ColumnStillPart.MIDDLE),
                                20, -30, 120, 0.25f)
                ))); // 31

        pages.add(BackgroundDecoratedPage.wrap(
                new FlowchartPage(t("p30.title"), buildWhiskyFlow()),
                PageBackground.of(
                        BackgroundIcon.block(ModBlocks.POT_STILL.get().defaultBlockState(), -20, 20, 120, 0.25f),
                        BackgroundIcon.block(ModBlocks.COLUMN_STILL.get().defaultBlockState().setValue(ColumnStillBlock.PART, ColumnStillPart.MIDDLE),
                                20, -30, 120, 0.25f)
                ))); // 32
        pages.add(BackgroundDecoratedPage.wrap(
                new CompositePage(t("p31.title"), 12,
                        new TablePage("", List.of(
                                List.of(t("p31.part1.table.1.1"), t("p31.part1.table.1.2")),
                                List.of(t("p31.part1.table.2.1"), t("p31.part1.table.2.2"))
                        )),
                        new TablePage(t("p31.part2.title"), t("p31.part2.body"),
                                List.of(
                                        List.of(t("p31.part2.table.1.1"), t("p31.part2.table.1.2")),
                                        List.of(t("p31.part2.table.2.1"), t("p31.part2.table.2.2"))
                                ), "")
                ),
                PageBackground.of(
                        BackgroundIcon.effect(ModEffects.ERUDITION.get(), 0, -5, 90, 0.25f)
                ))); // 33

        pages.add(BackgroundDecoratedPage.wrap(
                new FlowchartPage(t("p32.title"), buildBrandyFlow()),
                PageBackground.of(
                        BackgroundIcon.block(ModBlocks.POT_STILL.get().defaultBlockState(), -20, 20, 120, 0.25f),
                        BackgroundIcon.block(ModBlocks.COLUMN_STILL.get().defaultBlockState().setValue(ColumnStillBlock.PART, ColumnStillPart.MIDDLE),
                                20, -30, 120, 0.25f)
                ))); // 34
        pages.add(BackgroundDecoratedPage.wrap(
                new CompositePage(t("p33.title"), 12,
                        new TablePage("", List.of(
                                List.of(t("p33.part1.table.1.1"), t("p33.part1.table.1.2")),
                                List.of(t("p33.part1.table.2.1"), t("p33.part1.table.2.2"))
                        )),
                        new TablePage(t("p33.part2.title"), t("p33.part2.body"),
                                List.of(
                                        List.of(t("p33.part2.table.1.1"), t("p33.part2.table.1.2")),
                                        List.of(t("p33.part2.table.2.1"), t("p33.part2.table.2.2"))
                                ), "")
                ),
                PageBackground.of(
                        BackgroundIcon.effect(ModEffects.IMPROVED_DIGESTION.get(), 0, -5, 90, 0.25f)
                ))); // 35

        pages.add(BackgroundDecoratedPage.wrap(
                new FlowchartPage(t("p34.title"), buildRumFlow()),
                PageBackground.of(
                        BackgroundIcon.block(ModBlocks.POT_STILL.get().defaultBlockState(), -20, 20, 120, 0.25f),
                        BackgroundIcon.block(ModBlocks.COLUMN_STILL.get().defaultBlockState().setValue(ColumnStillBlock.PART, ColumnStillPart.MIDDLE),
                                20, -30, 120, 0.25f)
                ))); // 36
        pages.add(BackgroundDecoratedPage.wrap(
                new CompositePage(t("p35.title"), 12,
                        new TablePage("", List.of(
                                List.of(t("p35.part1.table.1.1"), t("p35.part1.table.1.2")),
                                List.of(t("p35.part1.table.2.1"), t("p35.part1.table.2.2")),
                                List.of(t("p35.part1.table.3.1"), t("p35.part1.table.3.2"))
                        )),
                        new TablePage(t("p35.part2.title"), t("p35.part2.body"),
                                List.of(
                                        List.of(t("p35.part2.table.1.1"), t("p35.part2.table.1.2"), t("p35.part2.table.1.3"), t("p35.part2.table.1.4")),
                                        List.of(t("p35.part2.table.2.1"), t("p35.part2.table.2.2"), t("p35.part2.table.2.3"), t("p35.part2.table.2.4")),
                                        List.of(t("p35.part2.table.3.1"), t("p35.part2.table.3.2"), t("p35.part2.table.3.3"), t("p35.part2.table.3.4"))
                                ), "")
                ),
                PageBackground.of(
                        BackgroundIcon.effect(ModEffects.PIRACY.get(), 0, -5, 90, 0.25f)
                ))); // 37

        pages.add(BackgroundDecoratedPage.wrap(
                new FlowchartPage(t("p36.title"), buildVodkaFlow()),
                PageBackground.of(
                        BackgroundIcon.block(ModBlocks.POT_STILL.get().defaultBlockState(), -20, 20, 120, 0.25f),
                        BackgroundIcon.block(ModBlocks.COLUMN_STILL.get().defaultBlockState().setValue(ColumnStillBlock.PART, ColumnStillPart.MIDDLE),
                                20, -30, 120, 0.25f)
                ))); // 38
        pages.add(BackgroundDecoratedPage.wrap(
                new CompositePage(t("p37.title"), 12,
                        new TablePage("", List.of(
                                List.of(t("p37.part1.table.1.1"), t("p37.part1.table.1.2")),
                                List.of(t("p37.part1.table.2.1"), t("p37.part1.table.2.2"))
                        )),
                        new TablePage(t("p37.part2.title"), t("p37.part2.body"),
                                List.of(
                                        List.of(t("p37.part2.table.1.1"), t("p37.part2.table.1.2")),
                                        List.of(t("p37.part2.table.2.1"), t("p37.part2.table.2.2"))
                                ), "")
                ),
                PageBackground.of(
                        BackgroundIcon.effect(ModEffects.CHARISMA.get(), 0, -5, 90, 0.25f)
                ))); // 39

        pages.add(BackgroundDecoratedPage.wrap(
                new FlowchartPage(t("p38.title"), buildGinFlow()),
                PageBackground.of(
                        BackgroundIcon.block(ModBlocks.POT_STILL.get().defaultBlockState(), -20, 20, 120, 0.25f),
                        BackgroundIcon.block(ModBlocks.COLUMN_STILL.get().defaultBlockState().setValue(ColumnStillBlock.PART, ColumnStillPart.MIDDLE),
                                20, -30, 120, 0.25f)
                ))); // 40
        pages.add(BackgroundDecoratedPage.wrap(
                new CompositePage(t("p39.title"), 12,
                        new TablePage("", List.of(
                                List.of(t("p39.part1.table.1.1"), t("p39.part1.table.1.2")),
                                List.of(t("p39.part1.table.2.1"), t("p39.part1.table.2.2"))
                        )),
                        new TablePage(t("p39.part2.title"), t("p39.part2.body"),
                                List.of(
                                        List.of(t("p39.part2.table.1.1"), t("p39.part2.table.1.2")),
                                        List.of(t("p39.part2.table.2.1"), t("p39.part2.table.2.2"))
                                ), "")
                ),
                PageBackground.of(
                        BackgroundIcon.effect(ModEffects.SAVAGERY.get(), 0, -5, 90, 0.25f)
                ))); // 41

        pages.add(BackgroundDecoratedPage.wrap(
                new FlowchartPage(t("p40.title"), buildTequilaFlow()),
                PageBackground.of(
                        BackgroundIcon.block(ModBlocks.POT_STILL.get().defaultBlockState(), -20, 20, 120, 0.25f),
                        BackgroundIcon.block(ModBlocks.COLUMN_STILL.get().defaultBlockState().setValue(ColumnStillBlock.PART, ColumnStillPart.MIDDLE),
                                20, -30, 120, 0.25f)
                ))); // 42
        pages.add(BackgroundDecoratedPage.wrap(
                new TablePage(t("p41.title"), t("p41.body"),
                        List.of(
                                List.of(t("p41.table.1.1"), t("p41.table.1.2")),
                                List.of(t("p41.table.2.1"), t("p41.table.2.2"))
                        ), ""),
                PageBackground.of(
                        BackgroundIcon.effect(ModEffects.INEBRIATION.get(), 0, -5, 90, 0.25f)
                ))); // 43

        return pages;
    }

    // ---- shared icon helpers ----

    private static List<ItemStack> fermentingBarrels() {
        return List.of(
                new ItemStack(ModBlocks.FERMENTING_BARREL_OAK.get()), new ItemStack(ModBlocks.FERMENTING_BARREL_SPRUCE.get()),
                new ItemStack(ModBlocks.FERMENTING_BARREL_BIRCH.get()), new ItemStack(ModBlocks.FERMENTING_BARREL_JUNGLE.get()),
                new ItemStack(ModBlocks.FERMENTING_BARREL_ACACIA.get()), new ItemStack(ModBlocks.FERMENTING_BARREL_DARK_OAK.get()),
                new ItemStack(ModBlocks.FERMENTING_BARREL_MANGROVE.get()), new ItemStack(ModBlocks.FERMENTING_BARREL_CHERRY.get()),
                new ItemStack(ModBlocks.FERMENTING_BARREL_BAMBOO.get()), new ItemStack(ModBlocks.FERMENTING_BARREL_CRIMSON.get()),
                new ItemStack(ModBlocks.FERMENTING_BARREL_WARPED.get())
        );
    }

    private static List<ItemStack> manualCrushers() {
        return List.of(
                new ItemStack(ModBlocks.MANUAL_CRUSHER_OAK.get()), new ItemStack(ModBlocks.MANUAL_CRUSHER_SPRUCE.get()),
                new ItemStack(ModBlocks.MANUAL_CRUSHER_BIRCH.get()), new ItemStack(ModBlocks.MANUAL_CRUSHER_JUNGLE.get()),
                new ItemStack(ModBlocks.MANUAL_CRUSHER_ACACIA.get()), new ItemStack(ModBlocks.MANUAL_CRUSHER_DARK_OAK.get()),
                new ItemStack(ModBlocks.MANUAL_CRUSHER_MANGROVE.get()), new ItemStack(ModBlocks.MANUAL_CRUSHER_CHERRY.get()),
                new ItemStack(ModBlocks.MANUAL_CRUSHER_BAMBOO.get()), new ItemStack(ModBlocks.MANUAL_CRUSHER_CRIMSON.get()),
                new ItemStack(ModBlocks.MANUAL_CRUSHER_WARPED.get())
        );
    }

    private static List<ItemStack> manualPresses() {
        return List.of(
                new ItemStack(ModBlocks.MANUAL_PRESS_OAK.get()), new ItemStack(ModBlocks.MANUAL_PRESS_SPRUCE.get()),
                new ItemStack(ModBlocks.MANUAL_PRESS_BIRCH.get()), new ItemStack(ModBlocks.MANUAL_PRESS_JUNGLE.get()),
                new ItemStack(ModBlocks.MANUAL_PRESS_ACACIA.get()), new ItemStack(ModBlocks.MANUAL_PRESS_DARK_OAK.get()),
                new ItemStack(ModBlocks.MANUAL_PRESS_MANGROVE.get()), new ItemStack(ModBlocks.MANUAL_PRESS_CHERRY.get()),
                new ItemStack(ModBlocks.MANUAL_PRESS_BAMBOO.get()), new ItemStack(ModBlocks.MANUAL_PRESS_CRIMSON.get()),
                new ItemStack(ModBlocks.MANUAL_PRESS_WARPED.get())
        );
    }

    private static List<ItemStack> stills() {
        return List.of(
                new ItemStack(ModBlocks.POT_STILL.get()), new ItemStack(ModBlocks.COLUMN_STILL.get())
        );
    }

    private static Flowchart.SubIcon icon(List<ItemStack> items, int size) {
        return new Flowchart.SubIcon(items, null, 2, 2, size, 0);
    }

    private static Flowchart.SubIcon blockIcon(BlockState state, int size) {
        return new Flowchart.SubIcon(null, List.of(state), 2, 2, size, 0);
    }

    // ---- flowcharts ----

    private static Flowchart buildYeastFlow() {
        Flowchart flow = new Flowchart();
        int w = PAGE_W;

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(Items.WATER_BUCKET)), 20)),
                t("p14.node1.title"), "",
                w/2 - 39, 8, Flowchart.FlowNode.Shape.SQUARE, Flowchart.FlowNode.BorderStyle.FULL, null));

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(fermentingBarrels(), 20)),
                t("p14.node2.title"), t("p14.node2.text"),
                w/2 - 39, 34, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.FULL, List.of(FERMENTING_BARREL_RECIPE)));
        flow.nodes.add(new Flowchart.FlowNode(List.of( // symmetrical offsets are x1,y-7; x-7,y1
                new Flowchart.SubIcon(List.of(new ItemStack(Items.SUGAR, 4)), null, 2, -7, 10, 0),
                new Flowchart.SubIcon(List.of(new ItemStack(Items.WHEAT, 4), new ItemStack(ModItems.BARLEY.get(), 4)), null,
                        -8, 1, 10, 1)),
                t("p14.node3.title"), t("p14.node3.text"),
                w/2 - 13, 34, Flowchart.FlowNode.Shape.SQUARE, Flowchart.FlowNode.BorderStyle.FULL, null));

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(ModItems.YEAST.get(), 8)), 20)),
                t("common.yeast_title"), "",
                w/2 - 39, 60, Flowchart.FlowNode.Shape.SQUARE, Flowchart.FlowNode.BorderStyle.FULL, null));


        flow.addEdge(0, 3);
        flow.addEdge(1, 2);
        return flow;
    }

    private static Flowchart buildSugarFlow() {
        Flowchart flow = new Flowchart();
        int w = PAGE_W;

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(Items.SUGAR_CANE, 2)), 20)),
                t("p15.node1.title"), "",
                w/2 - 39, 8, Flowchart.FlowNode.Shape.SQUARE, Flowchart.FlowNode.BorderStyle.FULL, null));

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(manualCrushers(), 20)),
                t("p15.node2.title"), t("p15.node2.text"),
                w/2 - 39, 34, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.FULL, List.of(CRUSHER_RECIPE)));

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(manualPresses(), 20)),
                t("p15.node3.title"), t("p15.node3.text"),
                w/2 - 39, 60, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.FULL, List.of(PRESS_RECIPE)));

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(ModBlocks.COPPER_POT.get())), 20)),
                t("p15.node4.title"), t("p15.node4.text"),
                w/2 - 39, 86, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.FULL, List.of(POT_RECIPE)));
        flow.nodes.add(new Flowchart.FlowNode(List.of(
                new Flowchart.SubIcon(List.of(new ItemStack(Items.GLASS_BOTTLE)), null, 1, -7, 10, 0),
                new Flowchart.SubIcon(List.of(new ItemStack(Items.SUGAR)), null, -7, 1, 10, 0)),
                t("p15.node5.title"), "",
                w/2 - 13, 86, Flowchart.FlowNode.Shape.SQUARE, Flowchart.FlowNode.BorderStyle.FULL, null));

        flow.nodes.add(new Flowchart.FlowNode(List.of(blockIcon(ModFluids.SYRUP_SUGARCANE.cauldron().get().defaultBlockState(), 20)),
                t("p15.node6.title"), t("p15.node6.text"),
                w/2 - 13, 112, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.FULL, null));
        flow.nodes.add(new Flowchart.FlowNode(List.of(
                new Flowchart.SubIcon(List.of(new ItemStack(ModItems.MOLASSES_SUGARCANE.get())), null, 1, -7, 10,
                        0),
                new Flowchart.SubIcon(List.of(new ItemStack(Items.SUGAR, 6)), null, -7, 1, 10, 0)),
                t("p15.node7.title"), t("p15.node7.text"),
                w/2 + 13, 112, Flowchart.FlowNode.Shape.SQUARE, Flowchart.FlowNode.BorderStyle.FULL, null));

        flow.addEdge(0, 6);
        flow.addEdge(4, 5);
        return flow;
    }

    private static Flowchart buildBeerFlow() {
        Flowchart flow = new Flowchart();
        int w = PAGE_W;

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(Items.WHEAT_SEEDS, 16),
                new ItemStack(ModItems.BARLEY_SEEDS.get(), 16)), 20)),
                t("p16.node1.title"), t("p16.node1.text"),
                w/2 - 39, 8, Flowchart.FlowNode.Shape.SQUARE, Flowchart.FlowNode.BorderStyle.FULL, null));

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(Items.WATER_BUCKET)), 20)),
                t("p16.node2.title"), t("p16.node2.text"),
                w/2 - 39, 34, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.FULL, null));

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(manualCrushers(), 20)),
                t("p16.node3.title"), t("p16.node3.text"),
                w/2 - 39, 60, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.FULL, List.of(CRUSHER_RECIPE)));

        flow.nodes.add(new Flowchart.FlowNode(List.of(blockIcon(Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL,
                3), 20)),
                t("p16.node4.title"), t("p16.node4.text"),
                w/2 - 39, 86, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.FULL, List.of(WORT_RECIPE)));
        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(ModItems.HOPS.get(), 4)), 20)),
                t("p16.node5.title"), t("p16.node5.text"),
                w/2 - 13, 86, Flowchart.FlowNode.Shape.SQUARE, Flowchart.FlowNode.BorderStyle.DASHED, List.of(WORT_HOPPED_RECIPE)));
        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(ModItems.YEAST.get(), 2)), 20)),
                t("common.yeast_title"), t("common.yeast_text"),
                w/2 + 13, 86, Flowchart.FlowNode.Shape.SQUARE, Flowchart.FlowNode.BorderStyle.FULL, null));

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(ModBlocks.COPPER_POT.get())), 20)),
                t("p16.node7.title"), t("p16.node7.text"),
                w/2 - 13, 112, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.FULL, List.of(POT_RECIPE)));
        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(fermentingBarrels(), 20)),
                t("p16.node8.title"), t("p16.node8.text"),
                w/2 + 13, 112, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.FULL, List.of(FERMENTING_BARREL_RECIPE)));
        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(ModItems.BEER_WHEAT_GLASS.get(), 2)), 20)),
                t("p16.node9.title"), t("p16.node9.text"),
                w/2 + 39, 112, Flowchart.FlowNode.Shape.SQUARE, Flowchart.FlowNode.BorderStyle.FULL, List.of(BEER_GLASS_RECIPE)));

        flow.addEdge(0, 8);
        flow.addEdge(3, 4);
        flow.addEdge(5, 7);
        return flow;
    }

    private static Flowchart buildWineFlow() {
        Flowchart flow = new Flowchart();
        int w = PAGE_W;

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(ModItems.GRAPES_RED.get(), 12),
                new ItemStack(ModItems.GRAPES_WHITE.get(), 12)), 20)),
                t("p18.node1.title"), t("p18.node1.text"),
                w/2 - 39, 8, Flowchart.FlowNode.Shape.SQUARE, Flowchart.FlowNode.BorderStyle.FULL, null));
        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(ModItems.GRAPES_RED.get(), 12),
                new ItemStack(ModItems.GRAPES_WHITE.get(), 12)), 20)),
                t("p18.node1.title"), t("p18.node1.text"),
                w/2 + 13, 8, Flowchart.FlowNode.Shape.SQUARE, Flowchart.FlowNode.BorderStyle.FULL, null));

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(manualCrushers(), 20)),
                t("p18.node3.title"), t("p18.node3.text"),
                w/2 - 39, 34, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.FULL, List.of(CRUSHER_RECIPE)));
        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(manualCrushers(), 20)),
                t("p18.node4.title"), t("p18.node4.text"),
                w/2 + 13, 34, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.DASHED, List.of(CRUSHER_RECIPE)));

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(fermentingBarrels(), 20)),
                t("p18.node5.title"), t("p18.node5.text"),
                w/2 - 39, 60, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.FULL, List.of(FERMENTING_BARREL_RECIPE)));
        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(ModItems.YEAST.get(), 2)), 20)),
                t("common.yeast_title"), t("common.yeast_text"),
                w/2 - 13, 60, Flowchart.FlowNode.Shape.SQUARE, Flowchart.FlowNode.BorderStyle.FULL, null));
        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(manualPresses(), 20)),
                t("p18.node7.title"), t("p18.node7.text"),
                w/2 + 13, 60, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.FULL, List.of(PRESS_RECIPE)));

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(manualPresses(), 20)),
                t("p18.node8.title"), t("p18.node8.text"),
                w/2 - 39, 86, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.FULL, List.of(PRESS_RECIPE)));
        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(fermentingBarrels(), 20)),
                t("p18.node9.title"), t("p18.node9.text"),
                w/2 + 13, 86, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.FULL, List.of(FERMENTING_BARREL_RECIPE)));
        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(ModItems.YEAST.get(), 2)), 20)),
                t("common.yeast_title"), t("common.yeast_text"),
                w/2 + 39, 86, Flowchart.FlowNode.Shape.SQUARE, Flowchart.FlowNode.BorderStyle.FULL, null));

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(fermentingBarrels(), 20)),
                t("p18.node11.title"), t("p18.node11.text"),
                w/2 - 39, 112, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.DASHED, List.of(FERMENTING_BARREL_RECIPE)));
        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(ModItems.WINE_RED_GLASS.get(), 5),
                new ItemStack(ModItems.WINE_ORANGE_GLASS.get(), 5)), 20)),
                t("p18.node12.title"), t("p18.node12.text"),
                w/2 - 13, 112, Flowchart.FlowNode.Shape.SQUARE, Flowchart.FlowNode.BorderStyle.FULL, List.of(WINE_GLASS_RECIPE)));
        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(fermentingBarrels(), 20)),
                t("p18.node11.title"), t("p18.node11.text"),
                w/2 + 13, 112, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.DASHED, List.of(FERMENTING_BARREL_RECIPE)));
        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(ModItems.WINE_ROSE_GLASS.get(), 5),
                new ItemStack(ModItems.WINE_WHITE_GLASS.get(), 5)), 20)),
                t("p18.node14.title"), t("p18.node14.text"),
                w/2 + 39, 112, Flowchart.FlowNode.Shape.SQUARE, Flowchart.FlowNode.BorderStyle.FULL, List.of(WINE_GLASS_RECIPE)));

        flow.addEdge(0, 11);
        flow.addEdge(1, 13);
        flow.addEdge(4, 5);
        flow.addEdge(8, 9);
        return flow;
    }

    private static Flowchart buildCiderFlow() {
        Flowchart flow = new Flowchart();
        int w = PAGE_W;

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(Items.APPLE, 8)), 20)),
                t("p20.node1.title"), "",
                w/2 - 39, 8, Flowchart.FlowNode.Shape.SQUARE, Flowchart.FlowNode.BorderStyle.FULL, null));

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(manualCrushers(), 20)),
                t("p20.node2.title"), t("p20.node2.text"),
                w/2 - 39, 34, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.FULL, List.of(CRUSHER_RECIPE)));

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(manualPresses(), 20)),
                t("p20.node3.title"), t("p20.node3.text"),
                w/2 - 39, 60, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.FULL, List.of(PRESS_RECIPE)));

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(fermentingBarrels(), 20)),
                t("p20.node4.title"), t("p20.node4.text"),
                w/2 - 39, 86, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.FULL, List.of(FERMENTING_BARREL_RECIPE)));
        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(ModItems.YEAST.get(), 2)), 20)),
                t("common.yeast_title"), t("common.yeast_text"),
                w/2 - 13, 86, Flowchart.FlowNode.Shape.SQUARE, Flowchart.FlowNode.BorderStyle.FULL, null));

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(fermentingBarrels(), 20)),
                t("p20.node6.title"), t("p20.node6.text"),
                w/2 - 39, 112, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.DASHED, List.of(FERMENTING_BARREL_RECIPE)));
        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(ModItems.CIDER_GLASS.get(), 2)), 20)),
                t("p20.node7.title"), t("p20.node7.text"),
                w/2 - 13, 112, Flowchart.FlowNode.Shape.SQUARE, Flowchart.FlowNode.BorderStyle.FULL, List.of(BEER_GLASS_RECIPE)));

        flow.addEdge(0, 5);
        flow.addEdge(3, 4);
        flow.addEdge(5, 6);
        return flow;
    }

    private static Flowchart buildMeadFlow() {
        Flowchart flow = new Flowchart();
        int w = PAGE_W;

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(Items.HONEY_BOTTLE)), 20)),
                t("p22.node1.title"), "",
                w/2 - 39, 8, Flowchart.FlowNode.Shape.SQUARE, Flowchart.FlowNode.BorderStyle.FULL, null));

        flow.nodes.add(new Flowchart.FlowNode(List.of(blockIcon(Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL,
                3), 20)),
                t("p22.node2.title"), t("p22.node2.text"),
                w/2 - 39, 34, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.FULL, List.of(HONEY_RECIPE)));

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(fermentingBarrels(), 20)),
                t("p22.node3.title"), t("p22.node3.text"),
                w/2 - 39, 60, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.FULL, List.of(FERMENTING_BARREL_RECIPE)));
        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(ModItems.YEAST.get(), 2)), 20)),
                t("common.yeast_title"), t("common.yeast_text"),
                w/2 - 13, 60, Flowchart.FlowNode.Shape.SQUARE, Flowchart.FlowNode.BorderStyle.FULL, null));

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(fermentingBarrels(), 20)),
                t("p22.node5.title"), t("p22.node5.text"),
                w/2 - 39, 86, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.DASHED, List.of(FERMENTING_BARREL_RECIPE)));

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(ModItems.MEAD_GLASS.get(), 5)), 20)),
                t("p22.node6.title"), t("p22.node6.text"),
                w/2 - 39, 112, Flowchart.FlowNode.Shape.SQUARE, Flowchart.FlowNode.BorderStyle.FULL, List.of(WINE_GLASS_RECIPE)));

        flow.addEdge(0, 5);
        flow.addEdge(2, 3);
        return flow;
    }

    private static Flowchart buildSugarcaneFlow() {
        Flowchart flow = new Flowchart();
        int w = PAGE_W;

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(ModItems.MOLASSES_SUGARCANE.get())), 20)),
                t("p24.node1.title"), t("p24.node1.text"),
                w/2 - 39, 8, Flowchart.FlowNode.Shape.SQUARE, Flowchart.FlowNode.BorderStyle.DASHED, null));

        flow.nodes.add(new Flowchart.FlowNode(List.of(blockIcon(Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL,
                3), 20)),
                t("p24.node2.title"), t("p24.node2.text"),
                w/2 - 39, 34, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.DASHED, List.of(MOLASSES_RECIPE)));

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(ModFluids.JUICE_SUGARCANE.bucket().get()),
                new ItemStack(ModFluids.DILUTED_MOLASSES_SUGARCANE.bucket().get())), 20)),
                t("p24.node3.title"), t("p24.node3.text"),
                w/2 - 39, 60, Flowchart.FlowNode.Shape.SQUARE, Flowchart.FlowNode.BorderStyle.FULL, null));

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(fermentingBarrels(), 20)),
                t("p24.node4.title"), t("p24.node4.text"),
                w/2 - 39, 86, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.FULL, List.of(FERMENTING_BARREL_RECIPE)));
        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(ModItems.YEAST.get(), 2)), 20)),
                t("common.yeast_title"), t("common.yeast_text"),
                w/2 - 13, 86, Flowchart.FlowNode.Shape.SQUARE, Flowchart.FlowNode.BorderStyle.FULL, null));

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(ModFluids.WASH_SUGARCANE_JUICE.bucket().get())), 20)),
                t("p24.node6.title"), "",
                w/2 - 39, 112, Flowchart.FlowNode.Shape.SQUARE, Flowchart.FlowNode.BorderStyle.FULL, null));

        flow.addEdge(0, 5);
        flow.addEdge(3, 4);
        return flow;
    }

    private static Flowchart buildPotatoFlow() {
        Flowchart flow = new Flowchart();
        int w = PAGE_W;

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(Items.BAKED_POTATO, 4)), 20)),
                t("p25.node1.title"), "",
                w/2 - 39, 8, Flowchart.FlowNode.Shape.SQUARE, Flowchart.FlowNode.BorderStyle.FULL, null));

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(manualCrushers(), 20)),
                t("p25.node2.title"), t("p25.node2.text"),
                w/2 - 39, 34, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.FULL, List.of(CRUSHER_RECIPE)));

        flow.nodes.add(new Flowchart.FlowNode(List.of(blockIcon(Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL,
                3), 20)),
                t("p25.node3.title"), t("p25.node3.text"),
                w/2 - 39, 60, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.FULL, List.of(MASH_RECIPE)));
        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(ModItems.WHEAT_SEEDS_CRUSHED.get()),
                new ItemStack(ModItems.BARLEY_SEEDS_CRUSHED.get())), 20)),
                t("p25.node4.title"), t("p25.node4.text"),
                w/2 - 13, 60, Flowchart.FlowNode.Shape.SQUARE, Flowchart.FlowNode.BorderStyle.FULL, null));

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(fermentingBarrels(), 20)),
                t("p25.node5.title"), t("p25.node5.text"),
                w/2 - 39, 86, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.FULL, List.of(FERMENTING_BARREL_RECIPE)));
        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(ModItems.YEAST.get(), 2)), 20)),
                t("common.yeast_title"), t("common.yeast_text"),
                w/2 - 13, 86, Flowchart.FlowNode.Shape.SQUARE, Flowchart.FlowNode.BorderStyle.FULL, null));

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(ModFluids.WASH_POTATO.bucket().get())), 20)),
                t("p25.node7.title"), "",
                w/2 - 39, 112, Flowchart.FlowNode.Shape.SQUARE, Flowchart.FlowNode.BorderStyle.FULL, null));

        flow.addEdge(0, 6);
        flow.addEdge(2, 3);
        flow.addEdge(4, 5);
        return flow;
    }

    private static Flowchart buildAgaveFlow() {
        Flowchart flow = new Flowchart();
        int w = PAGE_W;

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(ModItems.AGAVE_PINA.get())), 20)),
                t("p26.node1.title"), t("p26.node1.text"),
                w/2 - 39, 8, Flowchart.FlowNode.Shape.SQUARE, Flowchart.FlowNode.BorderStyle.FULL, null));

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(Items.FURNACE)), 20)),
                t("p26.node2.title"), t("p26.node2.text"),
                w/2 - 39, 34, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.FULL, null));

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(manualCrushers(), 20)),
                t("p26.node3.title"), t("p26.node3.text"),
                w/2 - 39, 60, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.FULL, List.of(CRUSHER_RECIPE)));

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(manualPresses(), 20)),
                t("p26.node4.title"), t("p26.node4.text"),
                w/2 - 39, 86, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.FULL, List.of(PRESS_RECIPE)));
        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(ModItems.YEAST.get(), 2)), 20)),
                t("common.yeast_title"), t("common.yeast_text"),
                w/2 - 13, 86, Flowchart.FlowNode.Shape.SQUARE, Flowchart.FlowNode.BorderStyle.FULL, null));

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(fermentingBarrels(), 20)),
                t("p26.node6.title"), t("p26.node6.text"),
                w/2 - 13, 112, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.FULL, List.of(FERMENTING_BARREL_RECIPE)));
        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(ModFluids.WASH_AGAVE.bucket().get(), 24)), 20)),
                t("p26.node7.title"), "",
                w/2 + 13, 112, Flowchart.FlowNode.Shape.SQUARE, Flowchart.FlowNode.BorderStyle.FULL, null));

        flow.addEdge(0, 6);
        flow.addEdge(4, 5);
        return flow;
    }

    private static Flowchart buildWhiskyFlow() {
        Flowchart flow = new Flowchart();
        int w = PAGE_W;

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(ModFluids.WASH_WHEAT.bucket().get(), 12)), 20)),
                t("p30.node1.title"), t("p30.node1.text"),
                w/2 - 39, 8, Flowchart.FlowNode.Shape.SQUARE, Flowchart.FlowNode.BorderStyle.FULL, null));

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(stills(), 20)),
                t("p30.node2.title"), t("p30.node2.text"),
                w/2 - 39, 34, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.FULL, List.of(POT_STILL_RECIPE, COLUMN_STILL_RECIPE)));

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(fermentingBarrels(), 20)),
                t("p30.node3.title"), t("p30.node3.text"),
                w/2 - 39, 60, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.FULL, List.of(FERMENTING_BARREL_RECIPE)));

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(Items.WATER_BUCKET)), 20)),
                t("p30.node4.title"), t("p30.node4.text"),
                w/2 - 39, 86, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.FULL, List.of(WHISKY_RECIPE)));

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(ModItems.KEG.get())), 20)),
                t("common.keg_title"), t("common.keg_text"),
                w/2 - 39, 112, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.FULL, List.of(KEG_RECIPE)));
        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(ModItems.WHISKY_WHEAT_GLASS.get(), 40)), 20)),
                t("p30.node6.title"), t("p30.node6.text"),
                w/2 - 13, 112, Flowchart.FlowNode.Shape.SQUARE, Flowchart.FlowNode.BorderStyle.FULL, List.of(WHISKY_GLASS_RECIPE)));

        flow.addEdge(0, 5);
        return flow;
    }

    private static Flowchart buildBrandyFlow() {
        Flowchart flow = new Flowchart();
        int w = PAGE_W;

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(ModFluids.WINE_RED.bucket().get(), 12),
                new ItemStack(ModFluids.WINE_ROSE.bucket().get(), 12), new ItemStack(ModFluids.WINE_ORANGE.bucket().get(), 12),
                new ItemStack(ModFluids.WINE_WHITE.bucket().get(), 12), new ItemStack(ModFluids.CIDER.bucket().get(), 12)), 20)),
                t("p32.node1.title"), t("p32.node1.text"),
                w/2 - 39, 8, Flowchart.FlowNode.Shape.SQUARE, Flowchart.FlowNode.BorderStyle.FULL, null));

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(stills(), 20)),
                t("p32.node2.title"), t("p32.node2.text"),
                w/2 - 39, 34, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.FULL, List.of(POT_STILL_RECIPE, COLUMN_STILL_RECIPE)));

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(fermentingBarrels(), 20)),
                t("p32.node3.title"), t("p32.node3.text"),
                w/2 - 39, 60, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.FULL, List.of(FERMENTING_BARREL_RECIPE)));

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(Items.WATER_BUCKET)), 20)),
                t("p32.node4.title"), t("p32.node4.text"),
                w/2 - 39, 86, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.FULL, List.of(BRANDY_RECIPE)));

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(ModItems.KEG.get())), 20)),
                t("common.keg_title"), t("common.keg_text"),
                w/2 - 39, 112, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.FULL, List.of(KEG_RECIPE)));
        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(ModItems.BRANDY_APPLE_GLASS.get(), 40)), 20)),
                t("p32.node6.title"), t("p32.node6.text"),
                w/2 - 13, 112, Flowchart.FlowNode.Shape.SQUARE, Flowchart.FlowNode.BorderStyle.FULL, List.of(BRANDY_GLASS_RECIPE)));

        flow.addEdge(0, 5);
        return flow;
    }

    private static Flowchart buildRumFlow() {
        Flowchart flow = new Flowchart();
        int w = PAGE_W;

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(ModFluids.WASH_SUGARCANE_JUICE.bucket().get(), 12)), 20)),
                t("p34.node1.title"), t("p34.node1.text"),
                w/2 - 39, 8, Flowchart.FlowNode.Shape.SQUARE, Flowchart.FlowNode.BorderStyle.FULL, null));

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(stills(), 20)),
                t("p34.node2.title"), t("p34.node2.text"),
                w/2 - 39, 34, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.FULL, List.of(POT_STILL_RECIPE, COLUMN_STILL_RECIPE)));

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(fermentingBarrels(), 20)),
                t("p34.node3.title"), t("p34.node3.text"),
                w/2 - 39, 60, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.DASHED, List.of(FERMENTING_BARREL_RECIPE)));

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(Items.WATER_BUCKET)), 20)),
                t("p34.node4.title"), t("p34.node4.text"),
                w/2 - 39, 86, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.FULL, List.of(RUM_RECIPE)));
        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(ModItems.KEG.get())), 20)),
                t("common.keg_title"), t("common.keg_text"),
                w/2 - 39, 112, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.FULL, List.of(KEG_RECIPE)));
        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(ModItems.RUM_JUICE_GLASS.get(), 40),
                new ItemStack(ModItems.RUM_JUICE_AGED_GLASS.get(), 40)), 20)),
                t("p34.node6.title"), t("p34.node6.text"),
                w/2 - 13, 112, Flowchart.FlowNode.Shape.SQUARE, Flowchart.FlowNode.BorderStyle.FULL, List.of(WHISKY_GLASS_RECIPE)));

        flow.addEdge(0, 5);
        return flow;
    }

    private static Flowchart buildVodkaFlow() {
        Flowchart flow = new Flowchart();
        int w = PAGE_W;

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(ModFluids.WASH_WHEAT.bucket().get(), 48),
                new ItemStack(ModFluids.WINE_RED.bucket().get(), 48), new ItemStack(ModFluids.WINE_ROSE.bucket().get(), 48),
                new ItemStack(ModFluids.WINE_ORANGE.bucket().get(), 48), new ItemStack(ModFluids.WINE_WHITE.bucket().get(), 48),
                new ItemStack(ModFluids.CIDER.bucket().get(), 48), new ItemStack(ModFluids.MEAD.bucket().get(), 48),
                new ItemStack(ModFluids.WASH_SUGARCANE_JUICE.bucket().get(), 48), new ItemStack(ModFluids.WASH_POTATO.bucket().get(), 48),
                new ItemStack(ModFluids.WASH_AGAVE.bucket().get(), 48)), 20)),
                t("p36.node1.title"), t("p36.node1.text"),
                w/2 - 39, 8, Flowchart.FlowNode.Shape.SQUARE, Flowchart.FlowNode.BorderStyle.FULL, null));

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(ModBlocks.COLUMN_STILL.get())), 20)),
                t("p36.node2.title"), t("p36.node2.text"),
                w/2 - 39, 34, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.FULL, List.of(COLUMN_STILL_RECIPE)));

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(Items.WATER_BUCKET)), 20)),
                t("p36.node3.title"), t("p36.node3.text"),
                w/2 - 39, 60, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.FULL, List.of(VODKA_RECIPE)));

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(ModItems.KEG.get())), 20)),
                t("common.keg_title"), t("common.keg_text"),
                w/2 - 39, 86, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.FULL, List.of(KEG_RECIPE)));

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(ModItems.VODKA_GRAPE_GLASS.get(), 100)), 20)),
                t("p36.node5.title"), t("p36.node5.text"),
                w/2 - 39, 112, Flowchart.FlowNode.Shape.SQUARE, Flowchart.FlowNode.BorderStyle.FULL, List.of(SHOT_GLASS_RECIPE)));

        flow.addEdge(0, 4);
        return flow;
    }

    private static Flowchart buildGinFlow() {
        Flowchart flow = new Flowchart();
        int w = PAGE_W;

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(ModFluids.WASH_WHEAT.bucket().get(), 12),
                new ItemStack(ModFluids.WINE_RED.bucket().get(), 12), new ItemStack(ModFluids.WINE_ROSE.bucket().get(), 12),
                new ItemStack(ModFluids.WINE_ORANGE.bucket().get(), 12), new ItemStack(ModFluids.WINE_WHITE.bucket().get(), 12),
                new ItemStack(ModFluids.CIDER.bucket().get(), 12), new ItemStack(ModFluids.MEAD.bucket().get(), 12),
                new ItemStack(ModFluids.WASH_SUGARCANE_JUICE.bucket().get(), 12), new ItemStack(ModFluids.WASH_POTATO.bucket().get(), 12),
                new ItemStack(ModFluids.WASH_AGAVE.bucket().get(), 12)), 20)),
                t("p38.node1.title"), t("p38.node1.text"),
                w/2 - 39, 8, Flowchart.FlowNode.Shape.SQUARE, Flowchart.FlowNode.BorderStyle.FULL, null));

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(stills(), 20)),
                t("p38.node2.title"), t("p38.node2.text"),
                w/2 - 39, 34, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.FULL, List.of(POT_STILL_RECIPE, COLUMN_STILL_RECIPE)));

        flow.nodes.add(new Flowchart.FlowNode(List.of(blockIcon(ModFluids.SPIRIT_WHEAT_MID.cauldron().get().defaultBlockState(), 20)),
                t("p38.node3.title"), t("p38.node3.text"),
                w/2 - 39, 60, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.FULL, List.of(SPICED_RECIPE)));
        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(ModItems.JUNIPER_BERRIES.get(), 4)), 20)),
                t("p38.node4.title"), t("p38.node4.text"),
                w/2 - 13, 60, Flowchart.FlowNode.Shape.SQUARE, Flowchart.FlowNode.BorderStyle.FULL, null));

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(stills(), 20)),
                t("p38.node5.title"), t("p38.node5.text"),
                w/2 - 39, 86, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.FULL, List.of(POT_STILL_RECIPE, COLUMN_STILL_RECIPE)));

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(Items.WATER_BUCKET)), 20)),
                t("p38.node6.title"), t("p38.node6.text"),
                w/2 - 39, 112, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.FULL, List.of(GIN_RECIPE)));
        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(ModItems.KEG.get())), 20)),
                t("common.keg_title"), t("common.keg_text"),
                w/2 - 13, 112, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.FULL, List.of(KEG_RECIPE)));
        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(ModItems.GIN_GRAPE_GLASS.get(), 40)), 20)),
                t("p38.node8.title"), t("p38.node8.text"),
                w/2 + 13, 112, Flowchart.FlowNode.Shape.SQUARE, Flowchart.FlowNode.BorderStyle.FULL, List.of(SHOT_GLASS_RECIPE)));

        flow.addEdge(0, 7);
        flow.addEdge(2, 3);
        return flow;
    }

    private static Flowchart buildTequilaFlow() {
        Flowchart flow = new Flowchart();
        int w = PAGE_W;

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(ModFluids.WASH_AGAVE.bucket().get(), 24)), 20)),
                t("p40.node1.title"), t("p40.node1.text"),
                w/2 - 39, 8, Flowchart.FlowNode.Shape.SQUARE, Flowchart.FlowNode.BorderStyle.FULL, null));

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(stills(), 20)),
                t("p40.node2.title"), t("p40.node2.text"),
                w/2 - 39, 34, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.FULL, List.of(POT_STILL_RECIPE, COLUMN_STILL_RECIPE)));

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(fermentingBarrels(), 20)),
                t("p40.node3.title"), t("p40.node3.text"),
                w/2 - 39, 60, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.DASHED, List.of(FERMENTING_BARREL_RECIPE)));

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(Items.WATER_BUCKET)), 20)),
                t("p40.node4.title"), t("p40.node4.text"),
                w/2 - 39, 86, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.FULL, List.of(TEQUILA_RECIPE)));

        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(ModItems.KEG.get())), 20)),
                t("common.keg_title"), t("common.keg_text"),
                w/2 - 39, 112, Flowchart.FlowNode.Shape.CIRCLE, Flowchart.FlowNode.BorderStyle.FULL, List.of(KEG_RECIPE)));
        flow.nodes.add(new Flowchart.FlowNode(List.of(icon(List.of(new ItemStack(ModItems.TEQUILA_GLASS.get(), 60),
                new ItemStack(ModItems.TEQUILA_AGED_GLASS.get(), 60)), 20)),
                t("p40.node6.title"), t("p40.node6.text"),
                w/2 - 13, 112, Flowchart.FlowNode.Shape.SQUARE, Flowchart.FlowNode.BorderStyle.FULL, List.of(BRANDY_GLASS_RECIPE)));


        flow.addEdge(0, 5);
        return flow;
    }

    private static BlockScene buildBarleyScene() {
        return new BlockScene()
                .add(0, -1, 0, Blocks.FARMLAND.defaultBlockState())
                .add(0, 0, 0, ModBlocks.BARLEY_CROP.get().defaultBlockState()
                        .setValue(CropBlock.AGE, 7));
    }

    private static BlockScene buildHopScene() {
        return new BlockScene()
                .add(0, -2, 0, Blocks.FARMLAND.defaultBlockState())
                .add(0, -1, 0, ModBlocks.HOP_CROP.get().defaultBlockState()
                        .setValue(HopCropBlock.AGE, 3))
                .add(0, 0, 0, ModBlocks.TRELLIS_OAK.get().defaultBlockState()
                        .setValue(TrellisBlock.ROPE, true)
                        .setValue(TrellisBlock.WEST, true))
                .add(-1, 0, 0, ModBlocks.TRELLIS_OAK.get().defaultBlockState()
                        .setValue(TrellisBlock.EAST, true)
                        .setValue(TrellisBlock.DOWN, true))
                .add(-1, -1, 0, ModBlocks.TRELLIS_OAK.get().defaultBlockState()
                        .setValue(TrellisBlock.UP, true)
                        .setValue(TrellisBlock.DOWN, true))
                .add(-1, -2, 0, Blocks.GRASS_BLOCK.defaultBlockState());
    }

    private static BlockScene buildGrapeScene() {
        return new BlockScene()
                .add(1, -1, 0, Blocks.FARMLAND.defaultBlockState())
                .add(1, 0, 0, ModBlocks.GRAPE_CROP_RED.get().defaultBlockState()
                        .setValue(GrapeCropBlock.AGE, 1)
                        .setValue(GrapeCropBlock.WIRE_WEST, true))
                .add(0, 0, 0, ModBlocks.GRAPE_WIRE_CROP_RED.get().defaultBlockState()
                        .setValue(GrapeWireCropBlock.AGE, 1)
                        .setValue(GrapeWireCropBlock.FACING, Direction.WEST))
                .add(-1, 0, 0, ModBlocks.TRELLIS_OAK.get().defaultBlockState()
                        .setValue(TrellisBlock.WIRE_EAST, true)
                        .setValue(TrellisBlock.DOWN, true))
                .add(-1, -1, 0, Blocks.GRASS_BLOCK.defaultBlockState());
    }

    private static BlockScene buildJuniperScene() {
        return new BlockScene()
                .add(0, -2, 0, Blocks.GRASS_BLOCK.defaultBlockState())
                .add(0, -1, 0, ModBlocks.JUNIPER.get().defaultBlockState()
                        .setValue(JuniperBlock.AGE, 4)
                        .setValue(JuniperBlock.PART, JuniperBlock.JuniperPart.BOTTOM))
                .add(0, 0, 0, ModBlocks.JUNIPER.get().defaultBlockState()
                        .setValue(JuniperBlock.AGE, 4)
                        .setValue(JuniperBlock.PART, JuniperBlock.JuniperPart.MIDDLE))
                .add(0, 1, 0, ModBlocks.JUNIPER.get().defaultBlockState()
                        .setValue(JuniperBlock.AGE, 4)
                        .setValue(JuniperBlock.PART, JuniperBlock.JuniperPart.TOP));
    }

    private static BlockScene buildAgaveScene() {
        return new BlockScene()
                .add(0, -1, 0, Blocks.SAND.defaultBlockState())
                .add(0, 0, 0, ModBlocks.AGAVE.get().defaultBlockState()
                        .setValue(AgaveBlock.AGE, 2));
    }
}