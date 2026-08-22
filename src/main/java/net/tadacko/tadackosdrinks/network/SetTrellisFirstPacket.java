package net.tadacko.tadackosdrinks.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.tadacko.tadackosdrinks.client.TrellisWireClientState;

import java.util.function.Supplier;

public class SetTrellisFirstPacket {
    private final long posLong;
    private final boolean has;

    public SetTrellisFirstPacket(long posLong, boolean has) {
        this.posLong = posLong;
        this.has = has;
    }

    public static void encode(SetTrellisFirstPacket pkt, FriendlyByteBuf buf) {
        buf.writeLong(pkt.posLong);
        buf.writeBoolean(pkt.has);
    }

    public static SetTrellisFirstPacket decode(FriendlyByteBuf buf) {
        long posLong = buf.readLong();
        boolean has = buf.readBoolean();
        return new SetTrellisFirstPacket(posLong, has);
    }

    public static void handle(final SetTrellisFirstPacket pkt, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        // run on client thread
        ctx.enqueueWork(() -> {
            if (pkt.has) {
                TrellisWireClientState.setFirstPos(pkt.posLong);
            } else {
                TrellisWireClientState.clearFirstPos();
            }
        });
        ctx.setPacketHandled(true);
    }
}
