import java.io.File;
import java.io.PrintStream;

public class Main {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java Main <chemin_apk> <dossier_sortie>");
            return;
        }

        String apkPath = args[0];
        String outputDir = args[1];
        PrintStream log = System.out;

        File apkFile = new File(apkPath);
        if (!apkFile.exists()) {
            System.out.println("Erreur: Le fichier APK n'existe pas: " + apkPath);
            return;
        }

        File outputDirFile = new File(outputDir);
        if (!outputDirFile.exists()) {
            outputDirFile.mkdirs();
        }

        APKDecompiler decompiler = new APKDecompiler(apkPath, outputDir, log);
        boolean success = decompiler.decompile();

        if (success) {
            System.out.println("Décompilation réussie. Résultats disponibles dans: " + outputDir);
        } else {
            System.out.println("Échec de la décompilation. Vérifiez les logs ci-dessus.");
        }
    }
}