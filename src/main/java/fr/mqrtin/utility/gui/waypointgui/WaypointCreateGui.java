package fr.mqrtin.utility.gui.waypointgui;

import fr.mqrtin.utility.module.modules.QOL.WaypointModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.io.IOException;

public class WaypointCreateGui extends GuiScreen {

    private final WaypointModule waypointModuleInstance;
    private final WaypointModule.Waypoint editingWaypoint;

    private GuiTextField nameField, xField, yField, zField, colorField;

    private Color selectedColor;

    private float hue = 0f;
    private float saturation = 1f;
    private float brightness = 1f;

    private boolean draggingHue, draggingSat, draggingBri;

    public WaypointCreateGui(WaypointModule waypointModuleInstance) {
        this.waypointModuleInstance = waypointModuleInstance;
        this.editingWaypoint = null;
        this.selectedColor = Color.WHITE;
    }

    public WaypointCreateGui(WaypointModule waypointModuleInstance, WaypointModule.Waypoint waypoint) {
        this.waypointModuleInstance = waypointModuleInstance;
        this.editingWaypoint = waypoint;
        this.selectedColor = waypoint.color;
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) throws IOException {
        if (nameField.textboxKeyTyped(typedChar, keyCode)) return;
        if (xField.textboxKeyTyped(typedChar, keyCode)) return;
        if (yField.textboxKeyTyped(typedChar, keyCode)) return;
        if (zField.textboxKeyTyped(typedChar, keyCode)) return;
        if (colorField.textboxKeyTyped(typedChar, keyCode)) return;

        try {
            String colorStr = colorField.getText();
            if (colorStr.length() == 6) {
                selectedColor = new Color(Integer.parseInt(colorStr, 16));

                float[] hsb = Color.RGBtoHSB(
                        selectedColor.getRed(),
                        selectedColor.getGreen(),
                        selectedColor.getBlue(),
                        null
                );

                hue = hsb[0];
                saturation = hsb[1];
                brightness = hsb[2];
            }
        } catch (Exception ignored) {}

        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void initGui() {
        int guiWidth = 350;
        int guiHeight = 300;
        int x = (this.width - guiWidth) / 2;
        int y = (this.height - guiHeight) / 2;

        nameField = new GuiTextField(0, fontRendererObj, x + 10, y + 55, guiWidth - 20, 18);

        xField = new GuiTextField(1, fontRendererObj, x + 10, y + 95, 90, 18);
        yField = new GuiTextField(2, fontRendererObj, x + guiWidth / 2 - 45, y + 95, 90, 18);
        zField = new GuiTextField(3, fontRendererObj, x + guiWidth - 100, y + 95, 90, 18);
        nameField.setFocused(true);
        colorField = new GuiTextField(4, fontRendererObj, x + 10, y + 135, guiWidth - 20, 18);

        if (editingWaypoint != null) {
            nameField.setText(editingWaypoint.name);
            xField.setText(String.valueOf((int) editingWaypoint.x));
            yField.setText(String.valueOf((int) editingWaypoint.y));
            zField.setText(String.valueOf((int) editingWaypoint.z));

            selectedColor = editingWaypoint.color;

            float[] hsb = Color.RGBtoHSB(
                    selectedColor.getRed(),
                    selectedColor.getGreen(),
                    selectedColor.getBlue(),
                    null
            );

            hue = hsb[0];
            saturation = hsb[1];
            brightness = hsb[2];

        } else {
            xField.setText(String.valueOf((int) Minecraft.getMinecraft().thePlayer.posX));
            yField.setText(String.valueOf((int) Minecraft.getMinecraft().thePlayer.posY));
            zField.setText(String.valueOf((int) Minecraft.getMinecraft().thePlayer.posZ));
        }

        colorField.setText(String.format("%06X", selectedColor.getRGB() & 0xFFFFFF));

        buttonList.add(new GuiButton(0, x + guiWidth / 2 - 125, y + guiHeight - 30, 120, 20, "Cancel"));
        buttonList.add(new GuiButton(1, x + guiWidth / 2 + 5, y + guiHeight - 30, 120, 20,
                editingWaypoint != null ? "Save" : "Create"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        int guiWidth = 350;
        int guiHeight = 300;
        int x = (this.width - guiWidth) / 2;
        int y = (this.height - guiHeight) / 2;

        drawRect(x, y, x + guiWidth, y + guiHeight, 0xCC1A1A1A);
        drawRect(x, y, x + guiWidth, y + 40, 0xCC212121);

        fontRendererObj.drawString(
                editingWaypoint != null ? "EDIT WAYPOINT" : "NEW WAYPOINT",
                x + 10, y + 14, 0xFFFFFF
        );

        fontRendererObj.drawString("Name", x + 10, y + 45, 0xAAAAAA);
        fontRendererObj.drawString("X", x + 10, y + 85, 0xAAAAAA);
        fontRendererObj.drawString("Y", x + guiWidth / 2 - 45, y + 85, 0xAAAAAA);
        fontRendererObj.drawString("Z", x + guiWidth - 100, y + 85, 0xAAAAAA);
        fontRendererObj.drawString("Color", x + 10, y + 125, 0xAAAAAA);

        drawRect(x + 10, y + 160, x + 40, y + 185, selectedColor.getRGB());

        nameField.drawTextBox();
        xField.drawTextBox();
        yField.drawTextBox();
        zField.drawTextBox();
        colorField.drawTextBox();

        drawColorSliders(x + 10, y + 195, guiWidth - 20);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawColorSliders(int x, int y, int width) {
        int height = 8;
        int spacing = 6;

        // =========================
        // HUE
        // =========================
        for (int i = 0; i < width; i++) {
            float h = (float) i / width;
            drawRect(x + i, y, x + i + 1, y + height, Color.HSBtoRGB(h, 1f, 1f));
        }

        drawRect(x + (int)(hue * width) - 1, y,
                x + (int)(hue * width) + 1, y + height, 0xFF000000);


        // =========================
        // SATURATION (FIX)
        // =========================
        int satY = y + height + spacing;

        for (int i = 0; i < width; i++) {
            float sat = (float) i / width;
            int color = Color.HSBtoRGB(hue, sat, 1f);

            drawRect(x + i, satY, x + i + 1, satY + height, color);
        }

        drawRect(x + (int)(saturation * width) - 1, satY,
                x + (int)(saturation * width) + 1, satY + height, 0xFF000000);


        // =========================
        // BRIGHTNESS (FIX)
        // =========================
        int briY = satY + height + spacing;

        for (int i = 0; i < width; i++) {
            float bri = (float) i / width;
            int color = Color.HSBtoRGB(hue, saturation, bri);

            drawRect(x + i, briY, x + i + 1, briY + height, color);
        }

        drawRect(x + (int)(brightness * width) - 1, briY,
                x + (int)(brightness * width) + 1, briY + height, 0xFF000000);


        // =========================
        // UPDATE COLOR
        // =========================
        selectedColor = Color.getHSBColor(hue, saturation, brightness);

        if (draggingHue || draggingSat || draggingBri) {
            colorField.setText(String.format("%06X", selectedColor.getRGB() & 0xFFFFFF));
        }
    }

    private void drawHorizontalGradientRect(int left, int top, int right, int bottom, int startColor, int endColor) {
        float sa = (startColor >> 24 & 255) / 255f;
        float sr = (startColor >> 16 & 255) / 255f;
        float sg = (startColor >> 8 & 255) / 255f;
        float sb = (startColor & 255) / 255f;

        float ea = (endColor >> 24 & 255) / 255f;
        float er = (endColor >> 16 & 255) / 255f;
        float eg = (endColor >> 8 & 255) / 255f;
        float eb = (endColor & 255) / 255f;

        Tessellator t = Tessellator.getInstance();
        WorldRenderer w = t.getWorldRenderer();

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glShadeModel(GL11.GL_SMOOTH);

        w.begin(7, DefaultVertexFormats.POSITION_COLOR);
        w.pos(left, bottom, 0).color(sr, sg, sb, sa).endVertex();
        w.pos(left, top, 0).color(sr, sg, sb, sa).endVertex();
        w.pos(right, top, 0).color(er, eg, eb, ea).endVertex();
        w.pos(right, bottom, 0).color(er, eg, eb, ea).endVertex();
        t.draw();

        // 🔥 RESET CLEAN (c’était ça le problème)
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        nameField.mouseClicked(mouseX, mouseY, button);
        xField.mouseClicked(mouseX, mouseY, button);
        yField.mouseClicked(mouseX, mouseY, button);
        zField.mouseClicked(mouseX, mouseY, button);
        colorField.mouseClicked(mouseX, mouseY, button);

        int guiWidth = 350;
        int x = (this.width - guiWidth) / 2 + 10;
        int y = (this.height - 300) / 2 + 195;
        int width = guiWidth - 20;

        int height = 8;
        int spacing = 6;

        if (isHover(mouseX, mouseY, x, y, width, height)) draggingHue = true;
        if (isHover(mouseX, mouseY, x, y + height + spacing, width, height)) draggingSat = true;
        if (isHover(mouseX, mouseY, x, y + (height + spacing) * 2, width, height)) draggingBri = true;

        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int button, long time) {
        int guiWidth = 350;
        int x = (this.width - guiWidth) / 2 + 10;
        int width = guiWidth - 20;

        if (draggingHue) hue = clamp((mouseX - x) / (float) width);
        if (draggingSat) saturation = clamp((mouseX - x) / (float) width);
        if (draggingBri) brightness = clamp((mouseX - x) / (float) width);

        super.mouseClickMove(mouseX, mouseY, button, time);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        draggingHue = draggingSat = draggingBri = false;
        super.mouseReleased(mouseX, mouseY, state);
    }

    private boolean isHover(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private float clamp(float v) {
        return Math.max(0f, Math.min(1f, v));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            Minecraft.getMinecraft().displayGuiScreen(new WaypointGui());
        }

        if (button.id == 1) {
            String name = nameField.getText().trim();
            if (name.isEmpty()) return;

            try {
                float wx = Float.parseFloat(xField.getText());
                float wy = Float.parseFloat(yField.getText());
                float wz = Float.parseFloat(zField.getText());

                if (editingWaypoint != null) {
                    editingWaypoint.name = name;
                    editingWaypoint.x = wx;
                    editingWaypoint.y = wy;
                    editingWaypoint.z = wz;
                    editingWaypoint.color = selectedColor;
                } else {
                    waypointModuleInstance.getWaypoints().add(
                            new WaypointModule.Waypoint(name, selectedColor, wx, wy, wz)
                    );
                }

                waypointModuleInstance.saveWaypoints();
                Minecraft.getMinecraft().displayGuiScreen(new WaypointGui());

            } catch (NumberFormatException e) {
                xField.setTextColor(0xFFFF4444);
                yField.setTextColor(0xFFFF4444);
                zField.setTextColor(0xFFFF4444);
            }
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}