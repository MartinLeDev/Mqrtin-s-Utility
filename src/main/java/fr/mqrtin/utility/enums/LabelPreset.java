package fr.mqrtin.utility.enums;

public enum LabelPreset {


    CLOCK("Clock","{clock_hour}:{clock_minute}:{clock_second}"),
    CPS("CPS","{cps_left} | {cps_right}"),
    COORDINATES("Coordinates","{player_x} {player_y} {player_z}"),
    DIRECTION("Direction","{player_facing}")
    ;
    private final String labelName;
    private final String labelValue;

    LabelPreset(String name, String value){
        this.labelName = name;
        this.labelValue = value;
    }


    public static String getFormat(LabelPreset preset, LabelType type){
        return type.getFormatted(preset.getLabelName(), preset.getLabelValue());
    }

    public String getLabelName() {
        return labelName;
    }

    public String getLabelValue() {
        return labelValue;
    }
}
