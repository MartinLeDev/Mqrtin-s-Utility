package fr.mqrtin.utility.enums;

import com.google.protobuf.Any;
import com.google.protobuf.Message;
import com.lunarclient.apollo.common.v1.BlockLocation;
import com.lunarclient.apollo.common.v1.Location;
import com.lunarclient.apollo.common.v1.Uuid;
import com.lunarclient.apollo.glow.v1.OverrideGlowEffectMessage;
import com.lunarclient.apollo.notification.v1.DisplayNotificationMessage;
import com.lunarclient.apollo.player.v1.UpdatePlayerWorldMessage;
import com.lunarclient.apollo.player.v1.PlayerHandshakeMessage;
import com.lunarclient.apollo.team.v1.UpdateTeamMembersMessage;
import com.lunarclient.apollo.waypoint.v1.DisplayWaypointMessage;
import com.lunarclient.apollo.waypoint.v1.RemoveWaypointMessage;
import fr.mqrtin.utility.module.impl.Module;
import fr.mqrtin.utility.module.modules.QOL.TeamViewModule;
import fr.mqrtin.utility.module.modules.QOL.WaypointModule;
import fr.mqrtin.utility.utils.TextUtils;

import java.awt.*;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public enum ApolloPacketType {

    PLAYER_HANDSHAKE(
            "PlayerHandshakeMessage",
            PlayerHandshakeMessage.class,
            msg -> {
                try {
                    PlayerHandshakeMessage handshake = (PlayerHandshakeMessage) msg;
                    System.out.println("[Apollo] === HANDSHAKE REÇU ===");

                    // Afficher les champs disponibles
                    if (handshake.hasMinecraftVersion()) {
                        System.out.println("[Apollo] Version Minecraft: " + handshake.getMinecraftVersion().getEnum());
                    }

                    if (handshake.hasLunarClientVersion()) {
                        System.out.println("[Apollo] Version Lunar Client: " + handshake.getLunarClientVersion().getSemver());
                        System.out.println("[Apollo] Git Branch: " + handshake.getLunarClientVersion().getGitBranch());
                        System.out.println("[Apollo] Git Commit: " + handshake.getLunarClientVersion().getGitCommit());
                    }

                    System.out.println("[Apollo] Message complet: " + handshake.toString());
                    System.out.println("[Apollo] =========================");
                } catch (Exception e) {
                    System.err.println("[Apollo] Erreur lors du parsing du handshake: " + e.getMessage());
                    e.printStackTrace();
                }
            }
    ),

    DISPLAY_WAYPOINT(
            "DisplayWaypointMessage",
            DisplayWaypointMessage.class,
            msg -> {
                DisplayWaypointMessage displayWaypointMessage = (DisplayWaypointMessage) msg;
                BlockLocation location = displayWaypointMessage.getLocation();

                WaypointModule instance = Module.getInstance(WaypointModule.class);
                instance.getWaypoints().add(new WaypointModule.Waypoint(
                        displayWaypointMessage.getName(),
                        new Color(displayWaypointMessage.getColor().getColor()),
                        location.getX(),
                        location.getY(),
                        location.getZ(),
                        true,
                        displayWaypointMessage.getHidden()
                ));
            }
    ),

    REMOVE_WAYPOINT(
            "RemoveWaypointMessage",
            RemoveWaypointMessage.class,
            msg -> System.out.println("[Apollo] Suppression waypoint: " + msg.toString())
    ),

    DISPLAY_NOTIFICATION(
            "DisplayNotificationMessage",
            DisplayNotificationMessage.class,
            msg -> System.out.println("[Apollo] Notification reçue: " + msg.toString())
    ),

    UPDATE_PLAYER_WORLD(
            "UpdatePlayerWorldMessage",
            UpdatePlayerWorldMessage.class,
            msg -> System.out.println("[Apollo] Monde : " + ((UpdatePlayerWorldMessage) msg).getWorld())
    ),

    OVERRIDE_GLOW_EFFECT(
            "OverrideGlowEffectMessage",
            OverrideGlowEffectMessage.class,
            msg -> {
                TextUtils.log("OverrideGlowEffectMessage received !");
                OverrideGlowEffectMessage msg1 = (OverrideGlowEffectMessage) msg;
                String uuid = uuidFromProtobuf(msg1.getPlayerUuid());
                TextUtils.log("UUID : " + uuid);
                TextUtils.log("COLOR : " + msg1.getColor());
            }
    ),

    UPDATE_TEAM(
        "UpdateTeamMembersMessage",
        UpdateTeamMembersMessage.class,
        msg -> {
            UpdateTeamMembersMessage teamMsg = (UpdateTeamMembersMessage) msg;
            Module.getInstance(TeamViewModule.class).update(teamMsg.getMembersList());
        }
    )


    ;

    private static String uuidFromProtobuf(Uuid protoUuid) {
        long high = protoUuid.getHigh64();
        long low = protoUuid.getLow64();
        return new UUID(high, low).toString();
    }

    private final String typeUrlKey;
    private final Class<? extends Message> messageClass;
    private final Consumer<Message> handler;

    ApolloPacketType(String typeUrlKey, Class<? extends Message> messageClass, Consumer<Message> handler) {
        this.typeUrlKey = typeUrlKey;
        this.messageClass = messageClass;
        this.handler = handler;
    }

    // Cherche l'enum correspondant au typeUrl reçu
    public static Optional<ApolloPacketType> fromTypeUrl(String typeUrl) {
        for (ApolloPacketType type : values()) {
            if (typeUrl.contains(type.typeUrlKey)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }

    // Unpack + dispatch en une seule ligne
    public void handle(Any any) throws Exception {
        this.handler.accept(any.unpack(this.messageClass));
    }
}