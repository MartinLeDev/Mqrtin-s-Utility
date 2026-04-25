package fr.mqrtin.utility.module.impl;

import fr.mqrtin.utility.Main;
import fr.mqrtin.utility.module.ModuleCategory;
import fr.mqrtin.utility.module.impl.property.Property;
import fr.mqrtin.utility.module.impl.property.properties.BooleanProperty;
import fr.mqrtin.utility.module.impl.property.properties.KeyBindProperty;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public abstract class Module {

    private boolean enabled;
    public BooleanProperty enabledProperty;
    private boolean forceEnabled;

    private KeyBinding keybind;
    public KeyBindProperty keybindProperty;

    private final String moduleName;
    private final ModuleCategory category;

    public boolean isEnabled() {
        return enabled;
    }
    public boolean isForceEnabled(){
        return forceEnabled;
    }
    public void setForceEnabled(boolean b){
        forceEnabled = b;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }
        this.enabled = enabled;

        // Mettre à jour la propriété aussi
        if (this.enabledProperty != null) {
            this.enabledProperty.setValue(enabled);
        }

        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }

    public final String getModuleName() {
        return moduleName;
    }

    public ModuleCategory getCategory() {
        return category;
    }

    public final KeyBinding getKeybind() {
        return keybind;
    }

    public final void setKeybind(KeyBinding keybind) {
        this.keybind = keybind;
    }

    public Module(String moduleName, ModuleCategory category, boolean hasKeybind){
        this.moduleName = moduleName;
        this.category = category;

        // Créer la enabledProperty pour gérer l'état du module
        this.enabledProperty = new BooleanProperty("Enabled", false, () -> false);
        this.enabledProperty.setOwner(this);

        if (hasKeybind) {
            this.keybind = new KeyBinding("Module : " + getModuleName(), 0, "Mqrtin's Utility");
            ClientRegistry.registerKeyBinding(getKeybind());
            // Créer et associer la propriété de keybind
            this.keybindProperty = new KeyBindProperty("Keybind", this.keybind);
        }
    }

    public Module(String moduleName, boolean hasKeybind){
        this(moduleName, ModuleCategory.OTHER, hasKeybind);
    }


    public boolean isHidden(){
        return false;
    }

    public boolean toggle(){
        setEnabled(!enabled);
        return enabled;
    }

    public void onEnable(){

    }

    public void onDisable(){

    }

    public void onPropertyChange(Property property){}

    public void verifyValue(String name) {
    }

    public static <T extends Module> T getInstance(Class<T> clazz) {
        return (T) Main.moduleManager.modules.get(clazz);
    }
}
