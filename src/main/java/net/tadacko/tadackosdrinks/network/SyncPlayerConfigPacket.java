package net.tadacko.tadackosdrinks.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.tadacko.tadackosdrinks.util.BacUtils;

import java.util.function.Supplier;

/**
 * Sent client -> server on login to sync the player's locally-configured
 * body weight / ratio into their server-side persistent data.
 */
public class SyncPlayerConfigPacket {
    private final double bodyWeightKg;
    private final double ratio;

    public SyncPlayerConfigPacket(double bodyWeightKg, double ratio) {
        this.bodyWeightKg = bodyWeightKg;
        this.ratio = ratio;
    }

    public static void encode(SyncPlayerConfigPacket packet, FriendlyByteBuf buf) {
        buf.writeDouble(packet.bodyWeightKg);
        buf.writeDouble(packet.ratio);
    }

    public static SyncPlayerConfigPacket decode(FriendlyByteBuf buf) {
        return new SyncPlayerConfigPacket(buf.readDouble(), buf.readDouble());
    }

    public static void handle(SyncPlayerConfigPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.getSender(); // identity comes from the connection, not the packet
            if (sender == null) return;
            BacUtils.setPlayerNBT(sender, packet.bodyWeightKg, packet.ratio);
        });
        ctx.setPacketHandled(true);
    }
}