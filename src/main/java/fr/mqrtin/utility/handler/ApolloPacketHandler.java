package fr.mqrtin.utility.handler;

import com.google.protobuf.Any;
import com.lunarclient.apollo.common.v1.LunarClientVersion;
import com.lunarclient.apollo.common.v1.MinecraftVersion;
import com.lunarclient.apollo.player.v1.PlayerHandshakeMessage;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import fr.mqrtin.utility.enums.ApolloPacketType;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.server.S3FPacketCustomPayload;

import java.util.Optional;

public class ApolloPacketHandler {

    public static final String APOLLO_CHANNEL = "lunar:apollo";
    private static boolean handshakeSent = false;
    private static boolean serverReceivedRegister = false;
    private static int ticksWaitingForRegister = 0;

    /**
     * Injecter un handler Netty + envoyer le REGISTER au serveur
     */
    @SubscribeEvent
    public void onServerJoin(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        System.out.println("[ApolloHandler] Connexion au serveur, injection du handler Netty...");
        handshakeSent = false;
        serverReceivedRegister = false;
        ticksWaitingForRegister = 0;

        try {
            // Ajouter un intercepteur Netty pour recevoir les paquets Apollo
            event.manager.channel().pipeline().addBefore(
                "packet_handler",
                "apollo_interceptor",
                new ChannelDuplexHandler() {
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        // Intercepter les paquets S3FPacketCustomPayload
                        if (msg instanceof S3FPacketCustomPayload) {
                            S3FPacketCustomPayload packet = (S3FPacketCustomPayload) msg;
                            System.out.println("[ApolloHandler] 📦 Plugin message reçu: " + packet.getChannelName());

                            // Apollo reçu !
                            if (packet.getChannelName().equals(APOLLO_CHANNEL)) {
                                System.out.println("[ApolloHandler] ✅ PAQUET APOLLO REÇU ! (" +
                                    packet.getBufferData().readableBytes() + " bytes)");
                                serverReceivedRegister = true;

                                try {
                                    byte[] data = new byte[packet.getBufferData().readableBytes()];
                                    packet.getBufferData().getBytes(
                                        packet.getBufferData().readerIndex(), data
                                    );

                                    Any any = Any.parseFrom(data);
                                    System.out.println("[ApolloHandler] TypeUrl: " + any.getTypeUrl());
                                    handleAnyMessage(any);
                                } catch (Exception e) {
                                    System.err.println("[ApolloHandler] Erreur parsing: " + e.getMessage());
                                    e.printStackTrace();
                                }
                            }
                        }

                        super.channelRead(ctx, msg);
                    }
                }
            );

            System.out.println("[ApolloHandler] ✅ Handler Netty injecté avec succès");
        } catch (Exception e) {
            System.err.println("[ApolloHandler] ❌ Erreur injection Netty: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Envoyer le REGISTER et le handshake au tick suivant
     */
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (handshakeSent || Minecraft.getMinecraft().getNetHandler() == null ||Minecraft.getMinecraft().getCurrentServerData() == null) {
            return;
        }

        // Attendre 10 ticks pour que le serveur soit prêt
        if (ticksWaitingForRegister < 10) {
            ticksWaitingForRegister++;
            if (ticksWaitingForRegister == 1) {
                System.out.println("[ApolloHandler] ⏳ Attente de 10 ticks avant envoi...");
            }
            return;
        }

        // Envoyer le REGISTER
        if (ticksWaitingForRegister == 10) {
            try {
                registerApolloChannel();
                System.out.println("[ApolloHandler] ✅ REGISTER envoyé");
                ticksWaitingForRegister = 11;
            } catch (Exception e) {
                System.err.println("[ApolloHandler] Erreur REGISTER: " + e.getMessage());
                e.printStackTrace();
            }
            return;
        }

        // Attendre 3 ticks supplémentaires, puis envoyer le handshake
        if (ticksWaitingForRegister < 14) {
            ticksWaitingForRegister++;
            return;
        }

        // Envoyer le handshake
        if (ticksWaitingForRegister >= 14) {
            handshakeSent = true;
            try {
                PlayerHandshakeMessage handshake = PlayerHandshakeMessage.newBuilder()
                    .setMinecraftVersion(
                        MinecraftVersion.newBuilder()
                            .setEnum("v1_8")
                            .build()
                    )
                    .setLunarClientVersion(
                        LunarClientVersion.newBuilder()
                            .setSemver("v2.21.38-2617")
                            .setGitBranch("master")
                            .setGitCommit("b0cb5a765ebc0a83ba1a7dfd3096f2f074fca35b")
                            .build()
                    )
                    .build();

                Any any = Any.pack(handshake);
                sendApolloPacket(any);
                System.out.println("[ApolloHandler] ✅ Handshake ENVOYÉ avec succès !");
            } catch (Exception e) {
                System.err.println("[ApolloHandler] ❌ Erreur handshake : " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Envoyer le REGISTER pour lunar:apollo
     */
    private void registerApolloChannel() {
        try {
            String channelName = APOLLO_CHANNEL;
            byte[] channelBytes = channelName.getBytes("UTF-8");

            System.out.println("[ApolloHandler] 📤 Envoi REGISTER: " + channelName);

            PacketBuffer payloadBuffer = new PacketBuffer(Unpooled.wrappedBuffer(channelBytes));
            // ✅ "REGISTER" pas "minecraft:register" (format pré-1.13 pour Spigot 1.8)
            C17PacketCustomPayload registerPacket = new C17PacketCustomPayload("REGISTER", payloadBuffer);

            if (Minecraft.getMinecraft().getNetHandler() != null) {
                Minecraft.getMinecraft().getNetHandler().addToSendQueue(registerPacket);
                System.out.println("[ApolloHandler] ✅ REGISTER packet envoyé au serveur");
            }
        } catch (Exception e) {
            System.err.println("[ApolloHandler] ❌ Erreur REGISTER: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Envoyer le handshake Apollo
     */
    private void sendApolloPacket(Any any) {
        try {
            byte[] data = any.toByteArray();
            PacketBuffer packetBuffer = new PacketBuffer(Unpooled.wrappedBuffer(data));
            C17PacketCustomPayload packet = new C17PacketCustomPayload(APOLLO_CHANNEL, packetBuffer);

            System.out.println("[ApolloHandler] 📤 Envoi Handshake: " + any.getTypeUrl() + " (" + data.length + " bytes)");

            if (Minecraft.getMinecraft().getNetHandler() != null) {
                Minecraft.getMinecraft().getNetHandler().addToSendQueue(packet);
                System.out.println("[ApolloHandler] ✅ Handshake packet envoyé au serveur");
            }
        } catch (Exception e) {
            System.err.println("[ApolloHandler] ❌ Erreur envoi handshake: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleAnyMessage(Any any) {
        System.out.println("[ApolloHandler] Traitement du message avec typeUrl: " + any.getTypeUrl());

        Optional<ApolloPacketType> optionalType = ApolloPacketType.fromTypeUrl(any.getTypeUrl());

        if (optionalType.isPresent()) {
            try {
                ApolloPacketType type = optionalType.get();
                System.out.println("[ApolloHandler] Type trouvé: " + type.name());
                type.handle(any);
                System.out.println("[ApolloHandler] ✅ Message traité");
            } catch (Exception e) {
                System.err.println("[Apollo] Erreur: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("[ApolloHandler] ⚠️ Type inconnu: " + any.getTypeUrl());
        }
    }
}