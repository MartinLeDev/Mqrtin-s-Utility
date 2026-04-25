package fr.mqrtin.utility.manager.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import fr.mqrtin.utility.module.impl.property.Property;
import fr.mqrtin.utility.module.impl.property.PropertyManager;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final File configFile;
    private final PropertyManager propertyManager;

    public ConfigManager(PropertyManager propertyManager) {
        this.propertyManager = propertyManager;

        // Créer le dossier .minecraft/Mqrtin/
        File minecraftDir = Minecraft.getMinecraft().mcDataDir;
        File configDir = new File(minecraftDir, "Mqrtin");

        if (!configDir.exists()) {
            configDir.mkdirs();
        }

        this.configFile = new File(configDir, "config.json");
    }

    /**
     * Charge la configuration depuis le fichier JSON
     */
    public void loadConfig() {
        if (!configFile.exists()) {
            System.out.println("[ConfigManager] Aucune configuration existante");
            return;
        }

        try (FileReader reader = new FileReader(configFile)) {
            JsonObject configJson = GSON.fromJson(reader, JsonObject.class);

            // Vérifier si le JSON est valide
            if (configJson == null) {
                System.out.println("[ConfigManager] Le fichier config.json est vide ou invalide");
                return;
            }

            // Parcourir chaque module
            for (Map.Entry<Class<?>, ArrayList<Property<?>>> entry : propertyManager.properties.entrySet()) {
                String moduleName = entry.getKey().getSimpleName();
                ArrayList<Property<?>> properties = entry.getValue();

                // Chercher le module dans la config
                if (configJson.has(moduleName)) {
                    JsonObject moduleJson = configJson.getAsJsonObject(moduleName);

                    // Charger chaque propriété
                    for (Property<?> property : properties) {
                        try {
                            if (property != null) {
                                property.read(moduleJson);
                            }
                        } catch (Exception e) {
                            System.err.println("[ConfigManager] Erreur lors de la lecture de la propriété " + property.getName() + ": " + e.getMessage());
                        }
                    }

                    System.out.println("[ConfigManager] Module chargé: " + moduleName);
                } else {
                    System.out.println("[ConfigManager] Module non trouvé dans la config: " + moduleName);
                }
            }

            System.out.println("[ConfigManager] Configuration chargée avec succès");
        } catch (IOException e) {
            System.err.println("[ConfigManager] Erreur lors de la lecture de la config: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("[ConfigManager] Erreur inattendue lors du chargement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Sauvegarde la configuration dans le fichier JSON
     */
    public void saveConfig() {
        try {
            // Vérifier que propertyManager est valide
            if (propertyManager == null || propertyManager.properties == null || propertyManager.properties.isEmpty()) {
                System.err.println("[ConfigManager] PropertyManager invalide ou vide");
                return;
            }

            JsonObject configJson = new JsonObject();

            // Parcourir chaque module
            for (Map.Entry<Class<?>, ArrayList<Property<?>>> entry : propertyManager.properties.entrySet()) {
                String moduleName = entry.getKey().getSimpleName();
                ArrayList<Property<?>> properties = entry.getValue();

                JsonObject moduleJson = new JsonObject();

                // Sauvegarder chaque propriété
                for (Property<?> property : properties) {
                    if (property != null) {
                        property.write(moduleJson);
                    }
                }

                configJson.add(moduleName, moduleJson);
            }

            // Créer le fichier s'il n'existe pas
            if (!configFile.exists()) {
                configFile.createNewFile();
            }

            // Écrire le JSON
            try (FileWriter writer = new FileWriter(configFile)) {
                GSON.toJson(configJson, writer);
                writer.flush();
            }

            System.out.println("[ConfigManager] Configuration sauvegardée: " + configFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("[ConfigManager] Erreur lors de la sauvegarde de la config: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("[ConfigManager] Erreur inattendue lors de la sauvegarde: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

