package engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.*;
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

    public static boolean resourceExists(String resource) {
        return ClassLoader.getSystemResource(resource) != null;
    }

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

    public static List<File> getAllDirectoryResourceFiles(String path) {
        path += "/";
        List<File> list = new ArrayList<>();
        try {
            ClassLoader.getSystemResourceAsStream(path);
            File[] listOfFiles = new File(ClassLoader.getSystemResource(path).toURI()).listFiles();
            assert listOfFiles != null;
            for (File file : listOfFiles) {
                if (file.isFile()) list.add(file);
                else if (file.isDirectory()) list.addAll(getAllDirectoryResourceFiles(path + file.getName()));
            }
        } catch (URISyntaxException e) {throw new RuntimeException(e);}
        return list;
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
