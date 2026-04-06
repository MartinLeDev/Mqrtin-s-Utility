package fr.mqrtin.utility.enums;

import fr.mqrtin.utility.module.modules.hidden.CPSCounter;
import net.minecraft.client.Minecraft;

import java.util.function.Supplier;

public enum LabelValue {


    CLOCK_HOUR("clock_hour", () -> String.valueOf(java.time.LocalTime.now().getHour())),
    CLOCK_MINUTE("clock_minute", () -> String.valueOf(java.time.LocalTime.now().getMinute())),
    CLOCK_SECOND("clock_second", () -> String.valueOf(java.time.LocalTime.now().getSecond())),
    PLAYER_X("player_x", () -> Minecraft.getMinecraft().thePlayer.getPosition().getX() + ""),
    PLAYER_Y("player_y", () -> Minecraft.getMinecraft().thePlayer.getPosition().getY() + ""),
    PLAYER_Z("player_z", () -> Minecraft.getMinecraft().thePlayer.getPosition().getZ() + ""),
    PLAYER_FACING("player_facing", () -> Minecraft.getMinecraft().thePlayer.getHorizontalFacing().getName()),
    CPS_LEFT("cps_left", () -> CPSCounter.getLeftCPS() + ""),
    CPS_RIGHT("cps_right", () -> CPSCounter.getRightCPS() + ""),
    FPS("fps", () -> String.valueOf(Minecraft.getDebugFPS()))

    ;
    private final String name;
    private final Supplier<String> getValue;

    LabelValue(String name, Supplier<String> value){
        this.name = name;
        this.getValue = value;
    }

    public String getName() {
        return name;
    }

    public Supplier<String> getGetValue() {
        return getValue;
    }

    public String getValue(){
        return getValue.get();
    }
}
