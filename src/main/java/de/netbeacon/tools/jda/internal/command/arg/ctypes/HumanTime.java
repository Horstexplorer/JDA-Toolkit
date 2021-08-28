package de.netbeacon.tools.jda.internal.command.arg.ctypes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.regex.Pattern;

public class HumanTime {

    private final static Pattern SIMPLE_NUM = Pattern.compile("^\\d+$");
    private final static Pattern EASY_TIME_DIF = Pattern.compile("^((\\d+y\\s*){0,1}(\\d+M\\s*){0,1}(\\d+d\\s*){0,1}(\\d+h\\s*){0,}(\\d+m\\s*){0,1}(\\d+s\\s*){0,1})$");
    private final static Pattern DATE_FORMAT = Pattern.compile("^\\d{4}-\\d{1,2}-\\d{1,2}$");
    private final static DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final static Pattern DATE_TIME_PATTERN = Pattern.compile("^\\d{4}-\\d{1,2}-\\d{1,2}\\s+\\d{1,2}:\\d{1,2}:\\d{1,2}");
    private final static DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final static Pattern SPACE = Pattern.compile("\\s+");
    private final LocalDateTime futureTime;

    private HumanTime(LocalDateTime futureTime) {
        this.futureTime = futureTime;
    }

    public static HumanTime parse(String string) {
        if (SIMPLE_NUM.matcher(string).matches()) {
            return new HumanTime(LocalDateTime.now().plus(Long.parseLong(string), ChronoUnit.MINUTES));
        } else if (EASY_TIME_DIF.matcher(string).matches()) {
            LocalDateTime localDateTime = LocalDateTime.now();
            for (String var : SPACE.split(string)) {
                char last = var.charAt(var.length() - 1);
                int value = Integer.parseInt(var.substring(0, var.length() - 1));
                switch (last) {
                    case 'y' -> localDateTime = localDateTime.plus(value, ChronoUnit.YEARS);
                    case 'M' -> localDateTime = localDateTime.plus(value, ChronoUnit.MONTHS);
                    case 'd' -> localDateTime = localDateTime.plus(value, ChronoUnit.DAYS);
                    case 'h' -> localDateTime = localDateTime.plus(value, ChronoUnit.HOURS);
                    case 'm' -> localDateTime = localDateTime.plus(value, ChronoUnit.MINUTES);
                    case 's' -> localDateTime = localDateTime.plus(value, ChronoUnit.SECONDS);
                }
            }
            return new HumanTime(localDateTime);
        } else if (DATE_FORMAT.matcher(string).matches()) {
            return new HumanTime(LocalDateTime.parse(string, DF));
        } else if (DATE_TIME_PATTERN.matcher(string).matches()) {
            return new HumanTime(LocalDateTime.parse(string, DTF));
        }
        throw new RuntimeException("Failed to parse human time from string");
    }

    public LocalDateTime getFutureTime() {
        return futureTime;
    }

}
