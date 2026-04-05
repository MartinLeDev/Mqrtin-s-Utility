package fr.mqrtin.utility.event;

import fr.mqrtin.utility.Main;

import java.lang.reflect.Method;

public final class EventManager {

    private EventManager() {

    }

    public static void call(Object event) {
        Main.moduleManager.modules.values().forEach(module -> {
            for (Method method : module.getClass().getMethods()) {
                if(!method.isAnnotationPresent(EventTarget.class)) continue;
                if(method.getParameterCount() != 1) continue;
                if(method.getParameters()[0].getType() != event.getClass()) continue;
                try {
                    method.invoke(module, event);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }
}

