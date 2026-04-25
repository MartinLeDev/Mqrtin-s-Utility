package fr.mqrtin.utility.module.modules.QOL;

import com.lunarclient.apollo.team.v1.TeamMember;
import fr.mqrtin.utility.event.EventTarget;
import fr.mqrtin.utility.event.events.Render3DEvent;
import fr.mqrtin.utility.event.events.ServerDisconnectEvent;
import fr.mqrtin.utility.manager.team.TeamMate;
import fr.mqrtin.utility.module.ModuleCategory;
import fr.mqrtin.utility.module.impl.Module;
import fr.mqrtin.utility.utils.RenderUtil;
import net.minecraft.client.Minecraft;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TeamViewModule extends Module {
    private final List<TeamMate> teamMates;

    public TeamViewModule(){
        super("TeamView", ModuleCategory.QOL, false);
        this.teamMates = new ArrayList<>();
    }

    public void update(List<TeamMember> members){
        // Mettre à jour les coéquipiers existants et en ajouter de nouveaux
        for (TeamMember member : members) {
            TeamMate existingMate = teamMates.stream()
                    .filter(mate -> mate.uuid.equals(new java.util.UUID(member.getPlayerUuid().getHigh64(), member.getPlayerUuid().getLow64())))
                    .findFirst()
                    .orElse(null);

            if (existingMate != null) {
                existingMate.update(member);
            } else {
                teamMates.add(new TeamMate(member));
            }
        }

        // Supprimer les coéquipiers qui ne sont plus dans la liste
        teamMates.removeIf(mate -> members.stream()
                .noneMatch(member -> mate.uuid.equals(new java.util.UUID(member.getPlayerUuid().getHigh64(), member.getPlayerUuid().getLow64()))));
    }

    @EventTarget
    public void onServerLeave(ServerDisconnectEvent event){
        teamMates.clear();
    }

    @EventTarget
    public void onRender3DEvent(Render3DEvent event){
        if(!isEnabled()) return;
        Minecraft minecraft = Minecraft.getMinecraft();
        if(minecraft.thePlayer == null || minecraft.theWorld == null) return;
        teamMates.forEach(mate -> {
            RenderUtil.renderTeamArrow(new RenderUtil.TeamArrowData(
                    mate.name, mate.color, mate.posX, mate.posY, mate.posZ
            ), -1, 1);
        });

        RenderUtil.renderTeamArrow(new RenderUtil.TeamArrowData(
                "Test", Color.GREEN, 0, 60, 0
        ), -1, 1);
    }
}
