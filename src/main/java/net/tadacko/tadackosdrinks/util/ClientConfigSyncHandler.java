package net.tadacko.tadackosdrinks.util;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tadacko.tadackosdrinks.TadackosDrinks;
import net.tadacko.tadackosdrinks.config.ModCommonConfigs;
import net.tadacko.tadackosdrinks.network.ModNetwork;
import net.tadacko.tadackosdrinks.network.SyncPlayerConfigPacket;

@Mod.EventBusSubscriber(modid = TadackosDrinks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientConfigSyncHandler {
    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        ModNetwork.CHANNEL.sendToServer(new SyncPlayerConfigPacket(ModCommonConfigs.BODY_WEIGHT.get(), ModCommonConfigs.RATIO.get()));
    }
}
