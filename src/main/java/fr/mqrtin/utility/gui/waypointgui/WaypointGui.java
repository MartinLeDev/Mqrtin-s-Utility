package fr.mqrtin.utility.gui.waypointgui;

import fr.mqrtin.utility.module.impl.Module;
import fr.mqrtin.utility.module.modules.QOL.WaypointModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.util.List;

public class WaypointGui extends GuiScreen {

    private final WaypointModule waypointModuleInstance;
    private final List<WaypointModule.Waypoint> waypoints;

    public WaypointGui(){
        this.waypointModuleInstance = Module.getInstance(WaypointModule.class);
        this.waypoints = waypointModuleInstance.getWaypoints();
    }

    private int selectedWaypoint = -1; // index du waypoint sélectionné, -1 = aucun

    @Override
    public void initGui() {
        int guiWidth = 350;
        int guiHeight = 400;
        int x = (this.width - guiWidth) / 2;
        int y = (this.height - guiHeight) / 2;

        // Delete à gauche
        GuiButton deleteBtn = new GuiButton(0, x + 5, y + guiHeight - 40, 100, 20, "Delete");
        deleteBtn.enabled = (selectedWaypoint != -1);
        this.buttonList.add(deleteBtn);

        // Edit au milieu
        GuiButton editBtn = new GuiButton(2, x + guiWidth/2 - 50, y + guiHeight - 40, 100, 20, "Edit");
        editBtn.enabled = (selectedWaypoint != -1);
        this.buttonList.add(editBtn);

        // New à droite
        this.buttonList.add(new GuiButton(1, x + guiWidth - 105, y + guiHeight - 40, 100, 20, "New"));
    }


    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        int guiWidth = 350;
        int guiHeight = 400;
        int x = (this.width - guiWidth) / 2;
        int y = (this.height - guiHeight) / 2;

        // Fond principal
        drawRect(x, y, x + guiWidth, y + guiHeight, 0xCC1A1A1A);

        // TopBar
        drawRect(x, y, x + guiWidth, y + 50, 0xCC212121);
        this.fontRendererObj.drawString("SINGLEPLAYER", x + 5, y + 18, 0xFFFFFF);

        // Waypoints
        for (int i = 0; i < waypoints.size(); i++) {
            WaypointModule.Waypoint wp = waypoints.get(i);
            int wy = y + 62 + (i * 50);
            int entryHeight = 40;

            // Surligner si sélectionné
            if (selectedWaypoint == i) {
                drawRect(x + 5, wy, x + guiWidth - 5, wy + entryHeight, 0xFF555555);
            } else {
                drawRect(x + 5, wy, x + guiWidth - 5, wy + entryHeight, 0xFF3D3D3D);
            }

            // Carré couleur
            drawRect(x + 5, wy, x + 45, wy + entryHeight, wp.color.getRGB());
            this.fontRendererObj.drawString(wp.name, x + 52, wy + 5, 0xFFFFFF);
            this.fontRendererObj.drawString(wp.x + " " + wp.y + " " + wp.z, x + 52, wy + 20, 0xFF636363);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        int guiWidth = 350;
        int guiHeight = 400;
        int x = (this.width - guiWidth) / 2;
        int y = (this.height - guiHeight) / 2;

        for (int i = 0; i < waypoints.size(); i++) {
            int wy = y + 62 + (i * 50);

            // Vérifier si le clic est sur un waypoint
            if (mouseX >= x + 5 && mouseX <= x + guiWidth - 5 &&
                    mouseY >= wy && mouseY <= wy + 40) {

                selectedWaypoint = (selectedWaypoint == i) ? -1 : i; // toggle sélection

                // Mettre à jour l'état des boutons
                this.buttonList.clear();
                this.initGui();
                return;
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0 && selectedWaypoint != -1) {
            waypoints.remove(selectedWaypoint);
            selectedWaypoint = -1;
            waypointModuleInstance.saveWaypoints();
            this.buttonList.clear();
            this.initGui();
        }
        if (button.id == 1) {
            Minecraft.getMinecraft().displayGuiScreen(new WaypointCreateGui(waypointModuleInstance));
        }
        if (button.id == 2 && selectedWaypoint != -1) {
            // Passer le waypoint existant au GUI d'édition
            Minecraft.getMinecraft().displayGuiScreen(
                    new WaypointCreateGui(waypointModuleInstance, waypoints.get(selectedWaypoint))
            );
        }
    }
    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}