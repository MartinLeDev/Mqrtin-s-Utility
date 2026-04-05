package fr.mqrtin.utility;

import fr.mqrtin.utility.module.ModuleManager;
import fr.mqrtin.utility.module.modules.misc.LabelModule;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.lwjgl.input.Keyboard;

@Mod(modid = "mqrtinsutility", name = "Mqrtin's Utility", version = "1.0.0")
public class Main {
    public static KeyBinding openGuiKeybind = new KeyBinding("Open GUI", Keyboard.KEY_RCONTROL, "Mqrtin's Utility");

    public static Main instance;
    public static ModuleManager moduleManager;

    @EventHandler
    public void init(FMLInitializationEvent event) {
        System.out.println("Mqrtin's Utility init");
        moduleManager = new ModuleManager();
        moduleManager.register(new LabelModule());
        instance = this;
        ClientRegistry.registerKeyBinding(openGuiKeybind);
    }
}
