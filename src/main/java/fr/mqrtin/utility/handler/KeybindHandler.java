package fr.mqrtin.utility.handler;

import fr.mqrtin.utility.Main;
import fr.mqrtin.utility.event.EventDispatcher;
import fr.mqrtin.utility.event.events.TickEvent;
import fr.mqrtin.utility.module.property.PropertyManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class KeybindHandler {

    @SubscribeEvent
    public void onClientTick(ClientTickEvent event) {
        if (event.phase != ClientTickEvent.Phase.END) return;

        // Dispatcher l'événement TickEvent personnalisé à tous les modules
        EventDispatcher.dispatchEvent(new TickEvent(TickEvent.Stage.END));

        // Gérer les keybinds
        Main.moduleManager.modules.forEach(((aClass, module) -> {
            KeyBinding keybind = module.getKeybind();
            if (keybind != null && keybind.isPressed()) {
                module.setEnabled(!module.isEnabled());
            }
        }));
    }
}