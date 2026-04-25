package fr.mqrtin.utility.gui.waypointgui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

public class GuiFlatButton extends GuiButton {

    // Couleur de fond normale (même teinte que la top bar)
    private static final int COLOR_BG          = 0xFF212121;
    private static final int COLOR_BG_HOVER    = 0xFF2E2E2E;
    private static final int COLOR_BG_DISABLED = 0xFF1A1A1A;

    private static final int COLOR_TEXT          = 0xFFFFFFFF;
    private static final int COLOR_TEXT_DISABLED = 0xFF555555;

    public GuiFlatButton(int id, int x, int y, int width, int height, String label) {
        super(id, x, y, width, height, label);
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (!this.visible) return;

        boolean hovered = mouseX >= this.xPosition && mouseY >= this.yPosition
                && mouseX < this.xPosition + this.width
                && mouseY < this.yPosition + this.height;

        int bg = !this.enabled ? COLOR_BG_DISABLED : hovered ? COLOR_BG_HOVER : COLOR_BG;

        // Fond plein
        drawRect(this.xPosition, this.yPosition,
                 this.xPosition + this.width, this.yPosition + this.height, bg);

        // Bordure de 1px (légèrement plus claire)
        int border = this.enabled ? 0xFF3A3A3A : 0xFF252525;
        // top
        drawRect(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + 1, border);
        // bottom
        drawRect(this.xPosition, this.yPosition + this.height - 1, this.xPosition + this.width, this.yPosition + this.height, border);
        // left
        drawRect(this.xPosition, this.yPosition, this.xPosition + 1, this.yPosition + this.height, border);
        // right
        drawRect(this.xPosition + this.width - 1, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, border);

        // Texte centré
        int textColor = this.enabled ? COLOR_TEXT : COLOR_TEXT_DISABLED;
        int textX = this.xPosition + this.width / 2 - mc.fontRendererObj.getStringWidth(this.displayString) / 2;
        int textY = this.yPosition + (this.height - 8) / 2;
        mc.fontRendererObj.drawString(this.displayString, textX, textY, textColor);
    }
}
