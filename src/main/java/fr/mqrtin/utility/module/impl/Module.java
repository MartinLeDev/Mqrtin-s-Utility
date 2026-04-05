package fr.mqrtin.utility.module.impl;

public abstract class Module {

    private boolean enabled;

    private final String moduleName;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public final String getModuleName() {
        return moduleName;
    }

    public Module(String moduleName){
        this.moduleName = moduleName;
    }


    public boolean isHidden(){
        return false;
    }

    public void onEnable(){

    }

    public void onDisable(){

    }
}
