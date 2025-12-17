package engine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DataManager {
    private static final Map<String, Float> settings = new HashMap<>();
    static {
        try {
            Pattern pattern = Pattern.compile("^\\s*([a-zA-Z][\\w ]*)\\s*[:=]\\s*((\\d+(?:\\.\\d*)?)|(true|false))", Pattern.CASE_INSENSITIVE);
            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("settings.info"))));
            float value;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    try {value = switch (matcher.group(2)) {
                        case "true" -> 1;
                        case "false" -> 0;
                        default -> Float.parseFloat(matcher.group(2));
                    };} catch (NullPointerException | NumberFormatException ignored) {value = 0;}
                    settings.put(matcher.group(1), value);
                }
            }
        } catch (IOException ignored) {}
    }

    private DataManager() {}

    public static String readResource(String resource) {
        try {
            StringBuilder result = new StringBuilder();
            String line;

            BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream(resource))));
            while ((line = reader.readLine()) != null) result.append(line).append("\n");
            return result.toString();
        } catch (NullPointerException | IOException ignored) {
            return "";
        }
    }

    public static float getSetting(String var) {
        if (settings.containsKey(var)) return settings.get(var);
        return 0;
    }

    public static boolean getFlag(String var) {
        if (settings.containsKey(var)) return settings.get(var) != 0;
        return false;
    }
}
