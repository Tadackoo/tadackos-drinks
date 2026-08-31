package net.tadacko.tadackosdrinks.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.tadacko.tadackosdrinks.util.Tooltips;

import java.util.function.Supplier;

public class SyncABVConfigPacket {
    private final double ABVBeer;
    private final double ABVWine;
    private final double ABVCider;
    private final double ABVMead;
    private final double ABVSpiritLow;
    private final double ABVSpiritMid;
    private final double ABVSpiritHigh;
    private final double ABVSpiritMax;
    private final double ABVWhisky;
    private final double ABVBrandy;
    private final double ABVRum;
    private final double ABVVodka;
    private final double ABVGin;
    private final double ABVTequila;

    public SyncABVConfigPacket(double ABVBeer, double ABVWine, double ABVCider, double ABVMead, double ABVSpiritLow, double ABVSpiritMid,
                               double ABVSpiritHigh, double ABVSpiritMax, double ABVWhisky, double ABVBrandy, double ABVRum, double ABVVodka, double ABVGin,
                               double ABVTequila) {
        this.ABVBeer = ABVBeer;
        this.ABVWine = ABVWine;
        this.ABVCider = ABVCider;
        this.ABVMead = ABVMead;
        this.ABVSpiritLow = ABVSpiritLow;
        this.ABVSpiritMid = ABVSpiritMid;
        this.ABVSpiritHigh = ABVSpiritHigh;
        this.ABVSpiritMax = ABVSpiritMax;
        this.ABVWhisky = ABVWhisky;
        this.ABVBrandy = ABVBrandy;
        this.ABVRum = ABVRum;
        this.ABVVodka = ABVVodka;
        this.ABVGin = ABVGin;
        this.ABVTequila = ABVTequila;
    }

    public static void encode(SyncABVConfigPacket pkt, FriendlyByteBuf buf) {
        buf.writeDouble(pkt.ABVBeer);
        buf.writeDouble(pkt.ABVWine);
        buf.writeDouble(pkt.ABVCider);
        buf.writeDouble(pkt.ABVMead);
        buf.writeDouble(pkt.ABVSpiritLow);
        buf.writeDouble(pkt.ABVSpiritMid);
        buf.writeDouble(pkt.ABVSpiritHigh);
        buf.writeDouble(pkt.ABVSpiritMax);
        buf.writeDouble(pkt.ABVWhisky);
        buf.writeDouble(pkt.ABVBrandy);
        buf.writeDouble(pkt.ABVRum);
        buf.writeDouble(pkt.ABVVodka);
        buf.writeDouble(pkt.ABVGin);
        buf.writeDouble(pkt.ABVTequila);
    }

    public static SyncABVConfigPacket decode(FriendlyByteBuf buf) {
        return new SyncABVConfigPacket(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(),
                buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    public static void handle(final SyncABVConfigPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();

        ctx.enqueueWork(() -> {
            if (ctx.getDirection().getReceptionSide().isClient()) {
                Tooltips.setABVValues(packet.ABVBeer, packet.ABVWine, packet.ABVCider, packet.ABVMead, packet.ABVSpiritLow, packet.ABVSpiritMid,
                        packet.ABVSpiritHigh, packet.ABVSpiritMax, packet.ABVWhisky, packet.ABVBrandy, packet.ABVRum, packet.ABVVodka, packet.ABVGin,
                        packet.ABVTequila);
            }
        });
        ctx.setPacketHandled(true);
    }
}
