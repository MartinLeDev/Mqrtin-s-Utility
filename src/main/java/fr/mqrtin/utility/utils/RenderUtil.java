package fr.mqrtin.utility.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;

public class RenderUtil {

    public static void renderNameTag(NametagData data, int maxDistance, int waypointScale, int paddingSize) {
        Minecraft mc = Minecraft.getMinecraft();

        double x = data.getX() - mc.getRenderManager().viewerPosX;
        double y = data.getY() - mc.getRenderManager().viewerPosY;
        double z = data.getZ() - mc.getRenderManager().viewerPosZ;

        double distance = mc.thePlayer.getDistance(data.getX(), data.getY(), data.getZ());

        if (maxDistance != -1 && distance > maxDistance) {
            return;
        }

        // For waypoints beyond render distance, normalize the direction to avoid centering
        // Use Minecraft's render distance (chunks) converted to blocks (~16 blocks per chunk)
        double renderDistanceBlocks = mc.gameSettings.renderDistanceChunks * 16.0 * 0.95; // 0.95 for safety margin
        double renderDistanceMeters = Math.sqrt(x * x + y * y + z * z);
        double displayDistance = distance; // Use actual distance for scale by default

        if (renderDistanceMeters > renderDistanceBlocks) {
            // Normalize coordinates to prevent far waypoints from centering on screen
            double length = renderDistanceMeters;
            double normalizedDistance = renderDistanceBlocks * 0.8;
            x = (x / length) * normalizedDistance;
            y = (y / length) * normalizedDistance;
            z = (z / length) * normalizedDistance;
            displayDistance = normalizedDistance; // Use normalized distance for consistent scale
        }

        GlStateManager.pushMatrix();

        GlStateManager.translate(x, y + 0.5, z);
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0F, 1F, 0F);
        GlStateManager.rotate(mc.getRenderManager().playerViewX, 1F, 0F, 0F);

        float scale = (float) (displayDistance * 0.01f * waypointScale / 100);
        scale = Math.max(scale, 0.02f);

        GlStateManager.scale(-scale, -scale, 1);

        String text = "§f" + data.getText() + " §7(" + (int) distance + "m)";
        int width = mc.fontRendererObj.getStringWidth(text) / 2;

        int padding = paddingSize;

        int left = -width - padding;
        int right = width + padding;
        int top = -mc.fontRendererObj.FONT_HEIGHT - padding;
        int bottom = padding;

        int bgColor = 0x55000000;
        int outlineColor = data.getColor() != null ? data.getColor().getRGB() : 0xFFFFFFFF;


        GlStateManager.disableDepth();

        // Background
        Gui.drawRect(left, top, right, bottom, bgColor);

        int thickness = 1;

        // Outline
        Gui.drawRect(left, top, right, top + thickness, outlineColor);           // Top
        Gui.drawRect(left, bottom - thickness, right, bottom, outlineColor);     // Bottom
        Gui.drawRect(left, top, left + thickness, bottom, outlineColor);         // Left
        Gui.drawRect(right - thickness, top, right, bottom, outlineColor);       // Right

        // Texte
        mc.fontRendererObj.drawString(
                text,
                -width,
                -mc.fontRendererObj.FONT_HEIGHT,
                0xFFFFFF,
                true
        );

        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }


    public static final class NametagData {
        /** Le texte affiché par le nametag */
        private final String text;

        /** La couleur du texte (peut être null pour la couleur par défaut) */
        private final Color color;

        /** Coordonnée X de la position du nametag dans le monde */
        private final double x;

        /** Coordonnée Y de la position du nametag dans le monde */
        private final double y;

        /** Coordonnée Z de la position du nametag dans le monde */
        private final double z;

        /**
         * Crée une nouvelle instance de NametagData avec un texte, une couleur et une position.
         *
         * @param text Le texte à afficher (ne doit pas être null)
         * @param color La couleur du texte en RGB (peut être null pour la couleur par défaut blanche)
         * @param x La coordonnée X dans le monde
         * @param y La coordonnée Y dans le monde
         * @param z La coordonnée Z dans le monde
         */
        public NametagData(String text, Color color, double x, double y, double z) {
            this.text = text;
            this.color = color;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        /**
         * Retourne le texte du nametag.
         *
         * @return le texte à afficher
         */
        public String getText() {
            return text;
        }

        /**
         * Retourne la couleur du nametag.
         *
         * @return la couleur RGB du texte, ou null pour la couleur par défaut
         */
        public Color getColor() {
            return color;
        }

        /**
         * Retourne la coordonnée X du nametag.
         *
         * @return la coordonnée X dans le monde
         */
        public double getX() {
            return x;
        }

        /**
         * Retourne la coordonnée Y du nametag.
         *
         * @return la coordonnée Y dans le monde
         */
        public double getY() {
            return y;
        }

        /**
         * Retourne la coordonnée Z du nametag.
         *
         * @return la coordonnée Z dans le monde
         */
        public double getZ() {
            return z;
        }
    }
}
