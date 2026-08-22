package net.tadacko.tadackosdrinks.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.tadacko.tadackosdrinks.TadackosDrinks;

public class ModCreativeModeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB,
            TadackosDrinks.MOD_ID);

    public static RegistryObject<CreativeModeTab> TADACKOS_DRINKS_TAB = CREATIVE_MODE_TABS.register("tadackos_drinks_tab", () ->
            CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.BEER_WHEAT_GLASS.get()))
                    .title(Component.translatable("itemgroup.tadackosdrinks.main")).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
