package fr.mqrtin.utility.module.property;


import fr.mqrtin.utility.module.impl.Module;
import fr.mqrtin.utility.module.property.properties.KeyBindProperty;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class PropertyManager {
    public LinkedHashMap<Class<?>, ArrayList<Property<?>>> properties = new LinkedHashMap<>();

    public Property<?> getProperty(Module module, String string) {
        ArrayList<Property<?>> props = properties.get(module.getClass());
        if (props == null) return null;
        for (Property<?> property : props) {
            if (property.getName().replace("-", "").equalsIgnoreCase(string.replace("-", ""))) {
                return property;
            }
        }
        return null;
    }

    /**
     * Découvre et enregistre automatiquement les propriétés d'un module via reflection
     */
    public void registerModule(Module module) {
        ArrayList<Property<?>> moduleProperties = new ArrayList<>();
        Class<?> moduleClass = module.getClass();

        // Ajouter la enabledProperty en premier
        if (module.enabledProperty != null) {
            moduleProperties.add(module.enabledProperty);
        }

        // Parcourir tous les champs du module
        for (Field field : moduleClass.getDeclaredFields()) {
            // Vérifier si le champ est une Property
            if (Property.class.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                try {
                    Property<?> property = (Property<?>) field.get(module);
                    if (property != null) {
                        moduleProperties.add(property);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        // Ajouter la keybindProperty
        if (module.keybindProperty != null) {
            moduleProperties.add(module.keybindProperty);
        }

        moduleProperties.forEach(property -> {
            if(property instanceof KeyBindProperty)
                ClientRegistry.registerKeyBinding(((KeyBindProperty) property).getKeyBinding());
        });

        // Enregistrer les propriétés du module
        if (!moduleProperties.isEmpty()) {
            properties.put(moduleClass, moduleProperties);
        }
    }
}
