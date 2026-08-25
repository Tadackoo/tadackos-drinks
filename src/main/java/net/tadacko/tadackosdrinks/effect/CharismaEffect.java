package net.tadacko.tadackosdrinks.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tadacko.tadackosdrinks.TadackosDrinks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CharismaEffect extends MobEffect {
    protected CharismaEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        return true;
    }

    @Mod.EventBusSubscriber(modid = TadackosDrinks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class CharismaEventHandler {
        // Two separate maps (server vs client) instead of one shared static map.
        // In singleplayer the client and integrated server share the same JVM/classloader,
        // so a single static map would let both logical sides overwrite each other's entry
        // for the same player id.
        private static final Map<UUID, MerchantOffers> serverOriginalOffersMap = new HashMap<>();
        private static final Map<UUID, MerchantOffers> clientOriginalOffersMap = new HashMap<>();

        public static float charismaMultiplier = 0.1f; // fallback default, overridden by config value

        @SubscribeEvent
        public static void onTradeMenuOpen(PlayerContainerEvent.Open event) {
            if (event.getContainer() instanceof MerchantMenu merchantMenu) {
                Player player = event.getEntity();
                Map<UUID, MerchantOffers> originalOffersMap = mapForSide(player);

                if (player.hasEffect(ModEffects.CHARISMA.get())) {
                    int charismaAmplifier = player.getEffect(ModEffects.CHARISMA.get()).getAmplifier();
                    float charismaDiscount = charismaMultiplier * (charismaAmplifier + 1);

                    MerchantOffers offers = merchantMenu.getOffers();

                    MerchantOffers originalOffers = new MerchantOffers();
                    for (MerchantOffer offer : offers) {
                        originalOffers.add(new MerchantOffer(
                                offer.getBaseCostA().copy(),
                                offer.getCostB().copy(),
                                offer.getResult().copy(),
                                offer.getUses(),
                                offer.getMaxUses(),
                                offer.getXp(),
                                offer.getPriceMultiplier(),
                                offer.getDemand()
                        ));
                    }
                    originalOffersMap.put(player.getUUID(), originalOffers);

                    MerchantOffers newOffers = new MerchantOffers();
                    for (MerchantOffer offer : offers) {
                        int baseCost = offer.getBaseCostA().getCount();
                        int specialPrice = offer.getSpecialPriceDiff();
                        int currentFinalPrice = baseCost + specialPrice;

                        //System.out.println("=== Charisma Debug ===");
                        //System.out.println("baseCostA: " + baseCost);
                        //System.out.println("specialPriceDiff: " + specialPrice);
                        //System.out.println("Current final price: " + currentFinalPrice);

                        int newFinalPrice = Math.max(1, (int)(currentFinalPrice * (1.0f - charismaDiscount)));
                        int newSpecialPrice = newFinalPrice - baseCost;

                        //System.out.println("New final price: " + newFinalPrice);
                        //System.out.println("New specialPriceDiff: " + newSpecialPrice);
                        //System.out.println("Display: ~~" + baseCost + "~~ " + newFinalPrice);

                        MerchantOffer newOffer = new MerchantOffer(
                                offer.getBaseCostA().copy(),
                                offer.getCostB(),
                                offer.getResult().copy(),
                                offer.getUses(),
                                offer.getMaxUses(),
                                offer.getXp(),
                                offer.getPriceMultiplier(),
                                offer.getDemand()
                        );

                        newOffer.addToSpecialPriceDiff(newSpecialPrice);
                        newOffers.add(newOffer);
                    }

                    offers.clear();
                    offers.addAll(newOffers);
                }
            }
        }

        @SubscribeEvent
        public static void onTradeMenuClose(PlayerContainerEvent.Close event) {
            if (event.getContainer() instanceof MerchantMenu merchantMenu) {
                Player player = event.getEntity();
                Map<UUID, MerchantOffers> originalOffersMap = mapForSide(player);

                if (originalOffersMap.containsKey(player.getUUID())) {
                    MerchantOffers offers = merchantMenu.getOffers();
                    MerchantOffers originalOffers = originalOffersMap.get(player.getUUID());

                    offers.clear();
                    offers.addAll(originalOffers);

                    originalOffersMap.remove(player.getUUID());
                }
            }
        }

        @SubscribeEvent
        public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
            // This event only fires server-side, so only the server map needs cleanup.
            serverOriginalOffersMap.remove(event.getEntity().getUUID());
        }

        private static Map<UUID, MerchantOffers> mapForSide(Player player) {
            return player.level().isClientSide() ? clientOriginalOffersMap : serverOriginalOffersMap;
        }
    }
}