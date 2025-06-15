import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

public class ApkAnalysisHelper {
    private static final Map<String, Pattern> SENSITIVE_PATTERNS = new HashMap<>();
    static {
        SENSITIVE_PATTERNS.put("API Key", Pattern.compile("(api[_-]?key|apikey)[\"'\\s:=]{0,3}([a-z0-9_\\-]{16,})", Pattern.CASE_INSENSITIVE));
        SENSITIVE_PATTERNS.put("Secret", Pattern.compile("(secret|password|token)[\"'\\s:=]{0,3}([a-z0-9_\\-]{8,})", Pattern.CASE_INSENSITIVE));
        SENSITIVE_PATTERNS.put("JWT Token", Pattern.compile("eyJ[A-Za-z0-9_-]{10,}\\.[A-Za-z0-9._-]+\\.[A-Za-z0-9._\\-]+"));
    }

    public static void scanForSensitiveStrings(String folderPath, PrintStream log) {
        log.println("Recherche de chaînes sensibles dans: " + folderPath);
        List<String> fileExtensions = Arrays.asList(".xml", ".smali", ".java", ".kt", ".json", ".properties", ".txt");

        try {
            Files.walk(Paths.get(folderPath))
                    .filter(Files::isRegularFile)
                    .filter(path -> fileExtensions.stream().anyMatch(path.toString().toLowerCase()::endsWith))
                    .forEach(path -> {
                        try {
                            List<String> lines = Files.readAllLines(path);
                            int lineNum = 0;
                            for (String line : lines) {
                                lineNum++;
                                for (Map.Entry<String, Pattern> entry : SENSITIVE_PATTERNS.entrySet()) {
                                    Matcher matcher = entry.getValue().matcher(line);
                                    if (matcher.find()) {
                                        log.printf("Potentiel %s trouvé dans %s:%d%n  Contenu: %s%n",
                                                entry.getKey(), path, lineNum, line.trim());
                                    }
                                }
                            }
                        } catch (IOException e) {
                            log.println("Erreur de lecture du fichier " + path + ": " + e.getMessage());
                        }
                    });
        } catch (IOException e) {
            log.println("Erreur lors de la navigation dans " + folderPath + ": " + e.getMessage());
        }
    }

    public static String parseManifest(String manifestFilePath, PrintStream log) {
        log.println("Analyse de AndroidManifest.xml à: " + manifestFilePath);
        File manifestFile = new File(manifestFilePath);
        if (!manifestFile.exists()) {
            log.println("Le fichier manifest n'existe pas: " + manifestFilePath);
            return null;
        }
        String packageName = null;
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setNamespaceAware(true);
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(manifestFile);
            doc.getDocumentElement().normalize();

            packageName = doc.getDocumentElement().getAttribute("package");
            log.println("Package: " + packageName);

            NodeList permissions = doc.getElementsByTagName("uses-permission");
            log.println("Permissions:");
            for (int i = 0; i < permissions.getLength(); i++) {
                Element elem = (Element) permissions.item(i);
                String permName = elem.getAttribute("android:name");
                if (!permName.isEmpty()) {
                    log.println(" - " + permName);
                }
            }

            NodeList activities = doc.getElementsByTagName("activity");
            log.println("Activités exportées:");
            for (int i = 0; i < activities.getLength(); i++) {
                Element activity = (Element) activities.item(i);
                if (activity.hasAttribute("android:exported") && activity.getAttribute("android:exported").equalsIgnoreCase("true")) {
                    String actName = activity.getAttribute("android:name");
                    log.println(" - " + actName);
                }
            }

        } catch (Exception e) {
            log.println("Erreur lors de l'analyse du manifest: " + e.getMessage());
        }
        return packageName;
    }
}