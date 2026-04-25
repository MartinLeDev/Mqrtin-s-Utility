package fr.mqrtin.utility.gui.waypointgui;

import fr.mqrtin.utility.module.impl.Module;
import fr.mqrtin.utility.module.modules.QOL.WaypointModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import fr.mqrtin.utility.gui.waypointgui.GuiFlatButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WaypointGui extends GuiScreen {

    private final WaypointModule waypointModuleInstance;
    private final List<WaypointModule.Waypoint> waypoints;

    public WaypointGui(){
        this.waypointModuleInstance = Module.getInstance(WaypointModule.class);
        this.waypoints = waypointModuleInstance.getWaypoints();
    }

    private int selectedWaypoint = -1;

    // ── Pagination ──────────────────────────────────────────────────
    private int currentPage = 0;
    private static final int ENTRIES_PER_PAGE = 5;
    // ────────────────────────────────────────────────────────────────

    // ── Filtre serveur/monde ─────────────────────────────────────────
    private boolean filterByServer = true;
    // ────────────────────────────────────────────────────────────────

    /** Retourne les indices visibles selon le filtre actif. */
    private List<Integer> getVisibleIndices() {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < waypoints.size(); i++) {
            WaypointModule.Waypoint wp = waypoints.get(i);
            if (filterByServer ? wp.isInTheSameServer() : !wp.hidden) list.add(i);
        }
        return list;
    }

    private int getTotalPages(List<Integer> visible) {
        return Math.max(1, (int) Math.ceil(visible.size() / (double) ENTRIES_PER_PAGE));
    }

    @Override
    public void initGui() {
        int guiWidth = 350;
        int guiHeight = 400;
        int x = (this.width - guiWidth) / 2;
        int y = (this.height - guiHeight) / 2;

        // Delete – gauche
        GuiButton deleteBtn = new GuiFlatButton(0, x + 5, y + guiHeight - 40, 75, 20, "Delete");
        deleteBtn.enabled = (selectedWaypoint != -1);
        this.buttonList.add(deleteBtn);

        // Hide/Show – centre-gauche
        GuiButton hideBtn = new GuiFlatButton(3, x + 85, y + guiHeight - 40, 75, 20, getHideShowLabel());
        hideBtn.enabled = (selectedWaypoint != -1);
        this.buttonList.add(hideBtn);

        // Edit – centre-droit
        GuiButton editBtn = new GuiFlatButton(2, x + guiWidth / 2 + 5, y + guiHeight - 40, 75, 20, "Edit");
        editBtn.enabled = (selectedWaypoint != -1);
        this.buttonList.add(editBtn);

        // New – droite
        this.buttonList.add(new GuiFlatButton(1, x + guiWidth - 80, y + guiHeight - 40, 75, 20, "New"));

        // ── Bouton filtre serveur (top bar, droite) ──────────────────
        GuiButton filterBtn = new GuiFlatButton(6, x + guiWidth - 80, y + 15, 75, 20,
                filterByServer ? "All worlds" : "This world");
        this.buttonList.add(filterBtn);
        // ────────────────────────────────────────────────────────────

        // ── Boutons de pagination ────────────────────────────────────
        List<Integer> vis = getVisibleIndices();

        GuiButton prevBtn = new GuiFlatButton(4, x + guiWidth / 2 - 60, y + guiHeight - 68, 20, 16, "<");
        prevBtn.enabled = (currentPage > 0);
        this.buttonList.add(prevBtn);

        GuiButton nextBtn = new GuiFlatButton(5, x + guiWidth / 2 + 40, y + guiHeight - 68, 20, 16, ">");
        nextBtn.enabled = (currentPage < getTotalPages(vis) - 1);
        this.buttonList.add(nextBtn);
        // ────────────────────────────────────────────────────────────
    }

    private String getHideShowLabel() {
        if (selectedWaypoint != -1 && selectedWaypoint < waypoints.size()) {
            return waypoints.get(selectedWaypoint).hidden ? "Show" : "Hide";
        }
        return "Hide";
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
        this.fontRendererObj.drawString(WaypointModule.Waypoint.getIP().toUpperCase(), x + 5, y + 18, 0xFFFFFF);

        // ── Pagination ───────────────────────────────────────────────
        List<Integer> visible = getVisibleIndices();
        int totalPages = getTotalPages(visible);
        if (currentPage >= totalPages) currentPage = totalPages - 1;

        int pageStart = currentPage * ENTRIES_PER_PAGE;
        int pageEnd   = Math.min(pageStart + ENTRIES_PER_PAGE, visible.size());

        String pageLabel = "Page " + (currentPage + 1) + " / " + totalPages;
        int labelWidth = fontRendererObj.getStringWidth(pageLabel);
        fontRendererObj.drawString(pageLabel, x + guiWidth / 2 - labelWidth / 2, y + guiHeight - 63, 0xAAAAAA);
        // ────────────────────────────────────────────────────────────

        // Entrées de la page courante
        for (int slot = 0; slot < ENTRIES_PER_PAGE; slot++) {
            int idx = pageStart + slot;
            if (idx >= pageEnd) break;

            int waypointIndex = visible.get(idx);
            WaypointModule.Waypoint wp = waypoints.get(waypointIndex);

            int wy = y + 62 + (slot * 50);
            int entryHeight = 40;

            if (selectedWaypoint == waypointIndex) {
                drawRect(x + 5, wy, x + guiWidth - 5, wy + entryHeight, 0xFF555555);
            } else {
                drawRect(x + 5, wy, x + guiWidth - 5, wy + entryHeight, 0xFF3D3D3D);
            }

            int displayColor = wp.hidden ? 0xFF666666 : wp.color.getRGB();
            drawRect(x + 5, wy, x + 45, wy + entryHeight, displayColor);

            int nameColor = wp.hidden ? 0xFF888888 : 0xFFFFFF;
            this.fontRendererObj.drawString(wp.name, x + 52, wy + 5, nameColor);
            this.fontRendererObj.drawString(wp.x + " " + wp.y + " " + wp.z + (filterByServer ? "" : "  -  " + wp.serverIp), x + 52, wy + 20, 0xFF636363);

            if (wp.hidden) {
                this.fontRendererObj.drawString("[hidden]", x + guiWidth - 60, wy + 5, 0xFF888888);
            }
            if (wp.temporary) {
                this.fontRendererObj.drawString("[temp]", x + guiWidth - 55, wy + 20, 0xFF4488CC);
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        int guiWidth = 350;
        int guiHeight = 400;
        int x = (this.width - guiWidth) / 2;
        int y = (this.height - guiHeight) / 2;

        List<Integer> visible = getVisibleIndices();
        int pageStart = currentPage * ENTRIES_PER_PAGE;
        int pageEnd   = Math.min(pageStart + ENTRIES_PER_PAGE, visible.size());

        for (int slot = 0; slot < ENTRIES_PER_PAGE; slot++) {
            int idx = pageStart + slot;
            if (idx >= pageEnd) break;

            int waypointIndex = visible.get(idx);
            int wy = y + 62 + (slot * 50);

            if (mouseX >= x + 5 && mouseX <= x + guiWidth - 5 &&
                    mouseY >= wy && mouseY <= wy + 40) {

                selectedWaypoint = (selectedWaypoint == waypointIndex) ? -1 : waypointIndex;
                this.buttonList.clear();
                this.initGui();
                return;
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        // Delete
        if (button.id == 0 && selectedWaypoint != -1) {
            waypoints.remove(selectedWaypoint);
            selectedWaypoint = -1;
            List<Integer> vis = getVisibleIndices();
            if (currentPage >= getTotalPages(vis)) currentPage = Math.max(0, getTotalPages(vis) - 1);
            waypointModuleInstance.saveWaypoints();
            this.buttonList.clear();
            this.initGui();
        }
        // New
        if (button.id == 1) {
            Minecraft.getMinecraft().displayGuiScreen(new WaypointCreateGui(waypointModuleInstance));
        }
        // Edit
        if (button.id == 2 && selectedWaypoint != -1) {
            Minecraft.getMinecraft().displayGuiScreen(
                    new WaypointCreateGui(waypointModuleInstance, waypoints.get(selectedWaypoint))
            );
        }
        // Hide/Show
        if (button.id == 3 && selectedWaypoint != -1) {
            waypoints.get(selectedWaypoint).hidden = !waypoints.get(selectedWaypoint).hidden;
            waypointModuleInstance.saveWaypoints();
            this.buttonList.clear();
            this.initGui();
        }
        // Page précédente
        if (button.id == 4 && currentPage > 0) {
            currentPage--;
            selectedWaypoint = -1;
            this.buttonList.clear();
            this.initGui();
        }
        // Page suivante
        if (button.id == 5) {
            List<Integer> vis = getVisibleIndices();
            if (currentPage < getTotalPages(vis) - 1) {
                currentPage++;
                selectedWaypoint = -1;
                this.buttonList.clear();
                this.initGui();
            }
        }
        // Filtre serveur/monde
        if (button.id == 6) {
            filterByServer = !filterByServer;
            currentPage = 0;
            selectedWaypoint = -1;
            this.buttonList.clear();
            this.initGui();
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}