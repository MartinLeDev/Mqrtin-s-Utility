package fr.mqrtin.utility.module.impl.property.properties;

import com.google.gson.JsonObject;
import fr.mqrtin.utility.module.impl.property.Property;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

import java.util.function.BooleanSupplier;

/**
 * Propriété pour gérer les KeyBindings (touches)
 */
public class KeyBindProperty extends Property<Integer> {

    private final KeyBinding keyBinding;

    public KeyBindProperty(String name, KeyBinding keyBinding) {
        this(name, keyBinding, null);
    }

    public KeyBindProperty(String name, KeyBinding keyBinding, BooleanSupplier visibilityCheck) {
        super(name, keyBinding.getKeyCode(), visibilityCheck);
        this.keyBinding = keyBinding;
    }

    @Override
    public String getValuePrompt() {
        return "Appuyez sur une touche";
    }

    @Override
    public String formatValue() {
        return String.format("&e%s", Keyboard.getKeyName(this.getValue()));
    }

    @Override
    public boolean parseString(String string) {
        try {
            int keyCode = Integer.parseInt(string);
            this.setValue(keyCode);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public boolean read(JsonObject jsonObject) {
        try {
            if (jsonObject.has(this.getName())) {
                int keyCode = jsonObject.get(this.getName()).getAsNumber().intValue();
                this.setValue(keyCode);
                this.keyBinding.setKeyCode(keyCode);
                return true;
            }
        } catch (Exception e) {
            System.err.println("[KeyBindProperty] Erreur lors de la lecture: " + e.getMessage());
        }
        return false;
    }

    @Override
    public void write(JsonObject jsonObject) {
        // Sauvegarder le code clavier actuel du binding
        jsonObject.addProperty(this.getName(), this.keyBinding.getKeyCode());
    }

    @Override
    public boolean setValue(Object object) {
        if (!(object instanceof Integer)) {
            return false;
        }

        Integer keyCode = (Integer) object;
        boolean result = super.setValue(keyCode);

        // Mettre à jour le KeyBinding avec le nouveau code clavier
        if (result && this.keyBinding != null) {
            this.keyBinding.setKeyCode(keyCode);
        }

        return result;
    }

    public KeyBinding getKeyBinding() {
        return this.keyBinding;
    }
}

