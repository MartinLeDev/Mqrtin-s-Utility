package fr.mqrtin.utility.manager.team;

import com.lunarclient.apollo.team.v1.TeamMember;
import net.minecraft.client.Minecraft;
import java.awt.*;
import java.util.UUID;

public class TeamMate {

    public final UUID uuid;
    public String name;
    public String worldName;
    public double posX;
    public double posY;
    public double posZ;
    public Color color;
    public boolean isInRender;

    public TeamMate(TeamMember teamMember){
        this.uuid = new UUID(teamMember.getPlayerUuid().getHigh64(), teamMember.getPlayerUuid().getLow64());
        update(teamMember);
    }

    public void update(TeamMember teamMember){
        String pName = teamMember.getAdventureJsonPlayerName();
        this.name = pName.isEmpty() ? "" : pName;
        this.posX = teamMember.hasLocation() ? teamMember.getLocation().getX() : Minecraft.getMinecraft().theWorld.getPlayerEntityByUUID(uuid).posX;
        this.posY = teamMember.hasLocation() ? teamMember.getLocation().getY() : Minecraft.getMinecraft().theWorld.getPlayerEntityByUUID(uuid).posY;
        this.posZ = teamMember.hasLocation() ? teamMember.getLocation().getZ() : Minecraft.getMinecraft().theWorld.getPlayerEntityByUUID(uuid).posZ;
        this.worldName = teamMember.hasLocation() ? teamMember.getLocation().getWorld() : Minecraft.getMinecraft().theWorld.provider.getDimensionName();
        this.color = new Color(teamMember.getMarkerColor().getColor());
        this.isInRender = teamMember.hasLocation();
    }


}
