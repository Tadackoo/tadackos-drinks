package net.tadacko.tadackosdrinks.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.tadacko.tadackosdrinks.TadackosDrinks;

import java.util.Optional;

public class ModNetwork {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(TadackosDrinks.MOD_ID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    private static int id = 0;

    public static void register() {
        CHANNEL.registerMessage(id++, RequestLightDamagePacket.class,
                RequestLightDamagePacket::encode,
                RequestLightDamagePacket::decode,
                RequestLightDamagePacket::handle);

        CHANNEL.registerMessage(id++, RequestSoundDamagePacket.class,
                RequestSoundDamagePacket::encode,
                RequestSoundDamagePacket::decode,
                RequestSoundDamagePacket::handle);

        // clientbound packet from server -> client
        CHANNEL.registerMessage(id++, SetTrellisFirstPacket.class,
                SetTrellisFirstPacket::encode,
                SetTrellisFirstPacket::decode,
                SetTrellisFirstPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));

        CHANNEL.registerMessage(id++, SyncPlayerConfigPacket.class,
                SyncPlayerConfigPacket::encode,
                SyncPlayerConfigPacket::decode,
                SyncPlayerConfigPacket::handle);
    }
}