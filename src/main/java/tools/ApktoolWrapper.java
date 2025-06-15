package tools;

import java.io.*;
import java.util.concurrent.TimeUnit;

public class ApktoolWrapper {
    private static final String APKTOOL_JAR = "lib/apktool.jar";

    public static void decompileApk(String apkPath, String outputDir, PrintStream log) throws IOException, InterruptedException {
        File apktoolFile = new File(APKTOOL_JAR);
        if (!apktoolFile.exists()) {
            log.println("Erreur: apktool.jar non trouvé dans " + APKTOOL_JAR);
            throw new IOException("apktool.jar manquant");
        }

        ProcessBuilder pb = new ProcessBuilder(
                "java", "-jar", APKTOOL_JAR, "d", apkPath, "-f", "-o", outputDir
        );
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
             BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.println(line);
            }
            while ((line = errorReader.readLine()) != null) {
                log.println("Erreur apktool: " + line);
            }
        }

        if (!process.waitFor(30, TimeUnit.SECONDS)) {
            process.destroy();
            log.println("Erreur: Décompilation apktool a expiré.");
            throw new IOException("Timeout lors de la décompilation");
        }

        if (process.exitValue() != 0) {
            log.println("Erreur: Échec de la décompilation avec apktool.");
            throw new IOException("Échec apktool");
        }
    }
}