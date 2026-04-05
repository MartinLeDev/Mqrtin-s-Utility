package fr.mqrtin.utility.module;

import fr.mqrtin.utility.module.impl.Module;

import java.util.HashMap;
import java.util.Map;

public class ModuleManager {

    public final Map<Class<?>, Module> modules;

    public ModuleManager() {
        modules = new HashMap<Class<?>, Module>();
    }

    public void register(Module... modules){
        for (Module module : modules) {
            this.modules.put(module.getClass(), module);
        }
    }
}

