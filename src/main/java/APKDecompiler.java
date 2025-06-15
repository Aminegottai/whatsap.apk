import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import tools.ApktoolWrapper;
import tools.Dex2JarWrapper;

public class APKDecompiler {
    private String apkPath;
    private String outputDir;
    private PrintStream log;

    public APKDecompiler(String apkPath, String outputDir, PrintStream log) {
        this.apkPath = apkPath;
        this.outputDir = outputDir;
        this.log = log != null ? log : System.out;
    }

    public boolean decompile() {
        log.println("Décompilation de l'APK: " + apkPath);
        log.println("Dossier de sortie: " + outputDir);

        try {
            File apkFile = new File(apkPath);
            if (!apkFile.exists()) {
                log.println("Erreur: Le fichier APK n'existe pas.");
                return false;
            }

            File outputDirFile = new File(outputDir);
            if (!outputDirFile.exists()) {
                outputDirFile.mkdirs();
            }

            // Décompiler avec apktool
            log.println("Utilisation d'apktool pour décompiler...");
            ApktoolWrapper.decompileApk(apkPath, outputDir, log);

            // Décompiler les fichiers .dex avec dex2jar
            String dexFile = outputDir + "/classes.dex";
            String jarOutput = outputDir + "/classes.jar";
            if (new File(dexFile).exists()) {
                log.println("Conversion des fichiers .dex en .jar...");
                Dex2JarWrapper.convertDexToJar(dexFile, jarOutput, log);
            }

            // Analyser AndroidManifest.xml
            String manifestPath = outputDir + "/AndroidManifest.xml";
            String packageName = ApkAnalysisHelper.parseManifest(manifestPath, log);
            if (packageName != null) {
                log.println("Analyse du manifeste terminée pour le package: " + packageName);
            }

            // Scanner les chaînes sensibles
            ApkAnalysisHelper.scanForSensitiveStrings(outputDir, log);

            log.println("Décompilation et analyse terminées avec succès.");
            return true;

        } catch (Exception e) {
            log.println("Erreur lors de la décompilation: " + e.getMessage());
            e.printStackTrace(log);
            return false;
        }
    }
}