package tools;

import java.io.*;
import java.util.concurrent.TimeUnit;

public class Dex2JarWrapper {
    private static final String DEX2JAR_SCRIPT = "lib/dex2jar/d2j-dex2jar.bat";

    public static void convertDexToJar(String dexFile, String jarOutput, PrintStream log) throws IOException, InterruptedException {
        File dex2jarFile = new File(DEX2JAR_SCRIPT);
        if (!dex2jarFile.exists()) {
            log.println("Erreur: dex2jar non trouvé dans " + DEX2JAR_SCRIPT);
            throw new IOException("dex2jar manquant");
        }

        ProcessBuilder pb = new ProcessBuilder(
                DEX2JAR_SCRIPT, dexFile, "-o", jarOutput
        );
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
             BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.println(line);
            }
            while ((line = errorReader.readLine()) != null) {
                log.println("Erreur dex2jar: " + line);
            }
        }

        if (!process.waitFor(30, TimeUnit.SECONDS)) {
            process.destroy();
            log.println("Erreur: Conversion dex2jar a expiré.");
            throw new IOException("Timeout lors de la conversion");
        }

        if (process.exitValue() != 0) {
            log.println("Erreur: Échec de la conversion avec dex2jar.");
            throw new IOException("Échec dex2jar");
        }
    }
}