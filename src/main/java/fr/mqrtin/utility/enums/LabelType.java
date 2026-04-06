package fr.mqrtin.utility.enums;

import java.text.MessageFormat;
import java.util.regex.Matcher;

public enum LabelType {

    ARROW("{0} -> §c{1}"),
    SEMI_ARROW("{0} > §c{1}"),
    EQUALS("{0} = §c{1}"),
    COLON("{0}: §c{1}"),
    DASH("{0} - {1}"),
    SPACE("{0} {1}"),
    RAW_VALUE("{1}"),
    INVERTED("{1} {0}")
    ;
    private final String format;

    LabelType(String formatString){
        this.format = formatString;
    }

    @Override
    public String toString() {
        return format;
    }

    public String getFormatted(String... args){
        if (args == null || args.length == 0) {
            return format;
        }

        return MessageFormat.format(format, (Object[]) args);
    }

}
