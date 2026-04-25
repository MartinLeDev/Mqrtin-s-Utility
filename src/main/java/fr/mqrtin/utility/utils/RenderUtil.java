package fr.mqrtin.utility.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class RenderUtil {

    private static final ResourceLocation TEAM_ARROW_TEXTURE =
            new ResourceLocation("mqrtins", "icons/team-arrow.png");

    public static void renderNameTag(NametagData data, int maxDistance, int waypointScale, int paddingSize) {
        Minecraft mc = Minecraft.getMinecraft();

        double x = data.getX() - mc.getRenderManager().viewerPosX;
        double y = data.getY() - mc.getRenderManager().viewerPosY;
        double z = data.getZ() - mc.getRenderManager().viewerPosZ;

        double distance = mc.thePlayer.getDistance(data.getX(), data.getY(), data.getZ());

        if (maxDistance != -1 && distance > maxDistance) {
            return;
        }

        double renderDistanceBlocks = mc.gameSettings.renderDistanceChunks * 16.0 * 0.95;
        double renderDistanceMeters = Math.sqrt(x * x + y * y + z * z);
        double displayDistance = distance;

        if (renderDistanceMeters > renderDistanceBlocks) {
            double length = renderDistanceMeters;
            double normalizedDistance = renderDistanceBlocks * 0.8;
            x = (x / length) * normalizedDistance;
            y = (y / length) * normalizedDistance;
            z = (z / length) * normalizedDistance;
            displayDistance = normalizedDistance;
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
        Gui.drawRect(left, top, right, top + thickness, outlineColor);
        Gui.drawRect(left, bottom - thickness, right, bottom, outlineColor);
        Gui.drawRect(left, top, left + thickness, bottom, outlineColor);
        Gui.drawRect(right - thickness, top, right, bottom, outlineColor);

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

    public static void renderTeamArrow(TeamArrowData data, int maxDistance, float iconScale) {
        Minecraft mc = Minecraft.getMinecraft();

        double x = data.getX() - mc.getRenderManager().viewerPosX;
        double y = data.getY() - mc.getRenderManager().viewerPosY;
        double z = data.getZ() - mc.getRenderManager().viewerPosZ;

        double distance = mc.thePlayer.getDistance(data.getX(), data.getY(), data.getZ());

        if (maxDistance != -1 && distance > maxDistance) {
            return;
        }

        double renderDistanceBlocks = mc.gameSettings.renderDistanceChunks * 16.0 * 0.95;
        double renderDistanceMeters = Math.sqrt(x * x + y * y + z * z);
        double displayDistance = distance;

        if (renderDistanceMeters > renderDistanceBlocks) {
            double normalizedDistance = renderDistanceBlocks * 0.8;
            x = (x / renderDistanceMeters) * normalizedDistance;
            y = (y / renderDistanceMeters) * normalizedDistance;
            z = (z / renderDistanceMeters) * normalizedDistance;
            displayDistance = normalizedDistance;
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y + 2.0, z);

        // Billboard : toujours face à la caméra
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0F, 1F, 0F);
        GlStateManager.rotate(mc.getRenderManager().playerViewX, 1F, 0F, 0F);

        float scale = (float) (displayDistance * 0.01f * iconScale / 100f);
        scale = Math.max(scale, 0.03f);
        GlStateManager.scale(-scale, -scale, 1f);

        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // Colorisation de l'icône avec la couleur du joueur
        if (data.getColor() != null) {
            Color c = data.getColor();
            GlStateManager.color(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, 1f);
        } else {
            GlStateManager.color(1f, 1f, 1f, 1f);
        }

        // Bind et rendu de la texture
        mc.getTextureManager().bindTexture(TEAM_ARROW_TEXTURE);

        float size = 8f;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer wr = tessellator.getWorldRenderer();

        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        wr.pos(-size, -size, 0).tex(0, 0).endVertex();
        wr.pos(-size,  size, 0).tex(0, 1).endVertex();
        wr.pos( size,  size, 0).tex(1, 1).endVertex();
        wr.pos( size, -size, 0).tex(1, 0).endVertex();
        tessellator.draw();

        // Nom du joueur + distance sous l'icône (style Lunar)
        if (data.getText() != null && !data.getText().isEmpty()) {
            GlStateManager.pushMatrix();
            float textScale = 0.6f;
            GlStateManager.scale(textScale, textScale, 1f);
            GlStateManager.color(1f, 1f, 1f, 1f);

            String label = "§f" + data.getText() + " §7(" + (int) distance + "m)";
            int textWidth = mc.fontRendererObj.getStringWidth(label) / 2;

            int padding = 2;
            int top = (int) (size / textScale) + 2;

            Gui.drawRect(
                    -textWidth - padding, top,
                    textWidth + padding, top + mc.fontRendererObj.FONT_HEIGHT + padding,
                    0x55000000
            );

            mc.fontRendererObj.drawString(label, -textWidth, top + padding / 2, 0xFFFFFF, true);
            GlStateManager.popMatrix();
        }

        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GlStateManager.color(1f, 1f, 1f, 1f);
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

    public static final class TeamArrowData {
        /** Le texte affiché sous l'icône (nom du joueur) */
        private final String text;

        /** La couleur de teinte de l'icône (peut être null pour blanc) */
        private final Color color;

        /** Coordonnée X de la position de l'icône dans le monde */
        private final double x;

        /** Coordonnée Y de la position de l'icône dans le monde */
        private final double y;

        /** Coordonnée Z de la position de l'icône dans le monde */
        private final double z;

        /**
         * Crée une nouvelle instance de TeamArrowData.
         *
         * @param text  Le nom du joueur à afficher sous l'icône (peut être null)
         * @param color La couleur de teinte de l'icône (peut être null pour blanc)
         * @param x     La coordonnée X dans le monde
         * @param y     La coordonnée Y dans le monde
         * @param z     La coordonnée Z dans le monde
         */
        public TeamArrowData(String text, Color color, double x, double y, double z) {
            this.text = text;
            this.color = color;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        /**
         * Retourne le texte affiché sous l'icône.
         *
         * @return le nom du joueur ou null
         */
        public String getText() {
            return text;
        }

        /**
         * Retourne la couleur de teinte de l'icône.
         *
         * @return la couleur RGB, ou null pour blanc par défaut
         */
        public Color getColor() {
            return color;
        }

        /**
         * Retourne la coordonnée X de l'icône.
         *
         * @return la coordonnée X dans le monde
         */
        public double getX() {
            return x;
        }

        /**
         * Retourne la coordonnée Y de l'icône.
         *
         * @return la coordonnée Y dans le monde
         */
        public double getY() {
            return y;
        }

        /**
         * Retourne la coordonnée Z de l'icône.
         *
         * @return la coordonnée Z dans le monde
         */
        public double getZ() {
            return z;
        }
    }
}