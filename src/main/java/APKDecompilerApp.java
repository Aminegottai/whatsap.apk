import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.ProgressBar;
import javafx.stage.FileChooser;
import javafx.concurrent.Task;
import java.io.*;

public class APKDecompilerApp extends Application {
    private TextArea outputArea;
    private Button decompileButton;
    private Button openFolderButton;
    private Button selectButton;
    private ProgressBar progressBar;
    private File selectedApk;
    private String outputDir;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("APK Decompiler (JavaFX)");
        primaryStage.setWidth(800);
        primaryStage.setHeight(600);

        VBox layout = new VBox(10);
        Label apkLabel = new Label("Sélectionner un fichier APK à décompiler:");
        selectButton = new Button("Sélectionner APK");
        decompileButton = new Button("Décompiler APK");
        openFolderButton = new Button("Ouvrir dossier de sortie");
        progressBar = new ProgressBar(0);
        progressBar.setVisible(false);
        outputArea = new TextArea();
        outputArea.setEditable(false);
        decompileButton.setDisable(true);
        openFolderButton.setDisable(true);

        selectButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Fichiers APK", "*.apk")
            );
            selectedApk = fileChooser.showOpenDialog(primaryStage);
            if (selectedApk != null) {
                apkLabel.setText("Sélectionné: " + selectedApk.getName());
                decompileButton.setDisable(false);
                String baseName = selectedApk.getName().replaceFirst("[.][^.]+$", "");
                outputDir = selectedApk.getParent() + File.separator + baseName + "_decompiled";
            }
        });

        decompileButton.setOnAction(e -> {
            if (selectedApk != null) {
                selectButton.setDisable(true);
                decompileButton.setDisable(true);
                progressBar.setVisible(true);
                runDecompilation();
            }
        });

        openFolderButton.setOnAction(e -> {
            if (outputDir != null) {
                try {
                    Runtime.getRuntime().exec("explorer.exe /select," + outputDir);
                } catch (IOException ex) {
                    outputArea.appendText("Erreur lors de l'ouverture du dossier: " + ex.getMessage() + "\n");
                }
            }
        });

        layout.getChildren().addAll(apkLabel, selectButton, decompileButton, progressBar, outputArea, openFolderButton);
        Scene scene = new Scene(layout, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void runDecompilation() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream log = new PrintStream(baos);

        Task<Void> decompileTask = new Task<>() {
            @Override
            protected Void call() {
                updateProgress(0, 1);
                updateMessage("Début de la décompilation de " + selectedApk.getAbsolutePath() + "...\n");

                APKDecompiler decompiler = new APKDecompiler(
                        selectedApk.getAbsolutePath(),
                        outputDir,
                        log
                );

                updateProgress(0.5, 1);
                boolean success = decompiler.decompile();
                updateProgress(1, 1);

                updateMessage(baos.toString());
                if (success) {
                    updateMessage("Décompilation et analyse terminées avec succès!\n");
                    updateMessage("Fichiers disponibles dans: " + outputDir + "\n");
                } else {
                    updateMessage("Échec de la décompilation.\n");
                }

                return null;
            }
        };

        decompileTask.messageProperty().addListener((obs, oldMsg, newMsg) -> {
            outputArea.appendText(newMsg);
        });

        progressBar.progressProperty().bind(decompileTask.progressProperty());

        decompileTask.setOnSucceeded(e -> {
            selectButton.setDisable(false);
            decompileButton.setDisable(false);
            openFolderButton.setDisable(false);
            progressBar.setVisible(false);
        });

        decompileTask.setOnFailed(e -> {
            outputArea.appendText("Erreur lors de la décompilation: " + decompileTask.getException().getMessage() + "\n");
            selectButton.setDisable(false);
            decompileButton.setDisable(false);
            progressBar.setVisible(false);
        });

        new Thread(decompileTask).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}