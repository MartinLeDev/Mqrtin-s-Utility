package fr.mqrtin.utility.module.modules.QOL;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.mqrtin.utility.event.EventTarget;
import fr.mqrtin.utility.event.events.Render3DEvent;
import fr.mqrtin.utility.event.events.TickEvent;
import fr.mqrtin.utility.gui.clickgui.ClickGui;
import fr.mqrtin.utility.gui.waypointgui.WaypointGui;
import fr.mqrtin.utility.module.ModuleCategory;
import fr.mqrtin.utility.module.impl.Module;
import fr.mqrtin.utility.module.property.properties.IntProperty;
import fr.mqrtin.utility.module.property.properties.KeyBindProperty;
import fr.mqrtin.utility.module.property.properties.PercentProperty;
import fr.mqrtin.utility.utils.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.settings.KeyBinding;

import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;


public class WaypointModule extends Module {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final List<Waypoint> waypoints;
    private final File waypointsFile;

    public List<Waypoint> getWaypoints() {
        return waypoints;
    }

    private final IntProperty waypointPaddingSize = new IntProperty("Padding Size", 5, 0, 50);
    private final IntProperty waypointMaxDistance = new IntProperty("Max Distance", 500, -1, 5000);
    private final PercentProperty waypointScale = new PercentProperty("Waypoint size", 100, 0, 500, () -> true);
    private final KeyBindProperty keyBindProperty = new KeyBindProperty("Open GUI", new KeyBinding("Open GUI", 0, "Mqrtin's waypoint"));

    public WaypointModule() {
        super("Waypoint", ModuleCategory.QOL, false);
        this.waypoints = new ArrayList<>();

        // Créer le dossier de configuration
        File minecraftDir = Minecraft.getMinecraft().mcDataDir;
        File configDir = new File(minecraftDir, "Mqrtin");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }

        this.waypointsFile = new File(configDir, "waypoints.json");
        loadWaypoints();
    }

    // ...existing code...
    @EventTarget
    public void onTick(TickEvent event){
        if (event.getStage() != TickEvent.Stage.END) return;
        if(!keyBindProperty.getKeyBinding().isPressed())
            return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer != null) {
            mc.displayGuiScreen(new WaypointGui());
        }
    }

    @EventTarget
    public void onRender3D(Render3DEvent event){
        if(!isEnabled())
            return;
        if(Minecraft.getMinecraft().theWorld == null || Minecraft.getMinecraft().thePlayer == null) return;

        float partialTicks = event.getPartialTicks();
        if (partialTicks < 0.0F) {
            return;
        }

        waypoints.forEach( waypoint -> {
            RenderUtil.renderNameTag(waypoint.toNametag(), waypointMaxDistance.getValue(), waypointScale.getValue(), waypointPaddingSize.getValue());
        });
    }

    public void saveWaypoints() {
        try {
            JsonArray waypointsArray = new JsonArray();
            for (Waypoint waypoint : waypoints) {
                if(waypoint.temporary) continue;
                JsonObject wpJson = new JsonObject();
                wpJson.addProperty("name", waypoint.name);
                wpJson.addProperty("x", waypoint.x);
                wpJson.addProperty("y", waypoint.y);
                wpJson.addProperty("z", waypoint.z);
                wpJson.addProperty("color", String.format("%06X", waypoint.color.getRGB() & 0xFFFFFF));
                wpJson.addProperty("world", waypoint.world);
                wpJson.addProperty("serverIp", waypoint.serverIp);
                waypointsArray.add(wpJson);
            }

            if (!waypointsFile.exists()) {
                waypointsFile.createNewFile();
            }

            try (FileWriter writer = new FileWriter(waypointsFile)) {
                GSON.toJson(waypointsArray, writer);
                writer.flush();
            }

            System.out.println("[WaypointModule] Waypoints sauvegardés: " + waypointsFile.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("[WaypointModule] Erreur lors de la sauvegarde: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void loadWaypoints() {
        try {
            if (!waypointsFile.exists()) {
                System.out.println("[WaypointModule] Aucune sauvegarde trouvée");
                return;
            }

            try (FileReader reader = new FileReader(waypointsFile)) {
                JsonArray waypointsArray = GSON.fromJson(reader, JsonArray.class);
                if (waypointsArray == null) {
                    System.out.println("[WaypointModule] Fichier vide ou invalide");
                    return;
                }

                waypoints.clear();
                for (JsonElement element : waypointsArray) {
                    JsonObject wpJson = element.getAsJsonObject();
                    String name = wpJson.get("name").getAsString();
                    float x = wpJson.get("x").getAsFloat();
                    float y = wpJson.get("y").getAsFloat();
                    float z = wpJson.get("z").getAsFloat();
                    String colorStr = wpJson.get("color").getAsString();
                    String world = wpJson.has("world") ? wpJson.get("world").getAsString() : "world";
                    String serverIp = wpJson.has("serverIp") ? wpJson.get("serverIp").getAsString() : "singleplayer";

                    int colorInt = (int) Long.parseLong(colorStr, 16);
                    Color color = new Color(colorInt);

                    waypoints.add(new Waypoint(name, color, world, serverIp, x, y, z, false));
                }

                System.out.println("[WaypointModule] " + waypoints.size() + " waypoints chargés");
            }
        } catch (Exception e) {
            System.err.println("[WaypointModule] Erreur lors du chargement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static class Waypoint{
        public String name;
        public String world;
        public String serverIp;
        public float x;
        public float y;
        public float z;
        public Color color;
        public boolean temporary;

        public RenderUtil.NametagData toNametag(){
            return new RenderUtil.NametagData(name, color, x, y, z);
        }

        public Waypoint(String name, float x, float y, float z){
            this.name = name;
            this.color = Color.WHITE;
            this.world = Minecraft.getMinecraft().theWorld.getWorldType().getWorldTypeName();
            ServerData currentServerData = Minecraft.getMinecraft().getCurrentServerData();
            if(currentServerData != null){
                String serverIP = currentServerData.serverIP;
                this.serverIp = serverIP == null || serverIP.isEmpty() ? "singleplayer" : serverIP;
            } else {
                this.serverIp = Minecraft.getMinecraft().getIntegratedServer().getName();
            }
            this.x = x;
            this.y = y;
            this.z = z;
            this.temporary = false;
        }
        public Waypoint(String name, Color color, float x, float y, float z){
            this.name = name;
            this.color = Color.WHITE;
            this.world = Minecraft.getMinecraft().theWorld.getWorldType().getWorldTypeName();
            ServerData currentServerData = Minecraft.getMinecraft().getCurrentServerData();
            if(currentServerData != null){
                String serverIP = currentServerData.serverIP;
                this.serverIp = serverIP == null || serverIP.isEmpty() ? "singleplayer" : serverIP;
            } else {
                this.serverIp = Minecraft.getMinecraft().getIntegratedServer().getName();
            }
            this.x = x;
            this.y = y;
            this.z = z;
            this.temporary = false;
        }

        public Waypoint(String name, Color color, float x, float y, float z, boolean temporary){
            this.name = name;
            this.color = Color.WHITE;
            this.world = Minecraft.getMinecraft().theWorld.getWorldType().getWorldTypeName();
            ServerData currentServerData = Minecraft.getMinecraft().getCurrentServerData();
            if(currentServerData != null){
                String serverIP = currentServerData.serverIP;
                this.serverIp = serverIP == null || serverIP.isEmpty() ? "singleplayer" : serverIP;
            } else {
                this.serverIp = Minecraft.getMinecraft().getIntegratedServer().getName();
            }
            this.x = x;
            this.y = y;
            this.z = z;
            this.temporary = temporary;
        }

        public Waypoint(String name, Color color, String world, String serverIp, float x, float y, float z, boolean temporary){
            this.name = name;
            this.color = color;
            this.world = world;
            this.serverIp = serverIp;
            this.x = x;
            this.y = y;
            this.z = z;
            this.temporary = temporary;
        }

    }
}
