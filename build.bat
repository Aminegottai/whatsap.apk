@echo off
echo ===== Configuration de l'environnement =====
set "JAVA_HOME=C:\Users\moham\AppData\Local\Programs\Eclipse Adoptium\jdk-21.0.7.6-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"
set "PROJECT_DIR=C:\Users\moham\IdeaProjects\tool"
set "LIB_DIR=%PROJECT_DIR%\src\main\java\lib"
set "CLASSPATH=.;%LIB_DIR%\apktool.jar;%LIB_DIR%\javafx-base-21.0.1.jar;%LIB_DIR%\javafx-controls-21.0.1.jar;%LIB_DIR%\javafx-fxml-21.0.1.jar;%LIB_DIR%\javafx-graphics-21.0.1.jar"

echo ===== Vérification manuelle des dépendances =====
echo Vérification de %LIB_DIR%\ :
if not exist "%PROJECT_DIR%" (
    echo Erreur : Le dossier du projet %PROJECT_DIR% n'existe pas.
    pause
    exit /b 1
)
if not exist "%LIB_DIR%" (
    echo Erreur : Le dossier %LIB_DIR% n'existe pas. Créez-le et ajoutez les JAR.
    pause
    exit /b 1
)
dir "%LIB_DIR%"
if not exist "%LIB_DIR%\javafx-controls-21.0.1.jar" (
    echo Erreur : javafx-controls-21.0.1.jar est manquant dans %LIB_DIR%.
    pause
    exit /b 1
)
if not exist "%LIB_DIR%\apktool.jar" (
    echo Erreur : apktool.jar est manquant dans %LIB_DIR%.
    pause
    exit /b 1
)

echo ===== Compilation des fichiers Java =====
javac --module-path "%LIB_DIR%" --add-modules javafx.controls,javafx.fxml -d out src\main\java\*.java src\main\java\tools\*.java
if %ERRORLEVEL% neq 0 (
    echo Compilation échouée. Vérifiez les erreurs ci-dessus.
    pause
    exit /b %ERRORLEVEL%
)


echo ===== Démarrage de l'application =====
java -cp "out;%CLASSPATH%" --module-path "%LIB_DIR%" --add-modules javafx.controls,javafx.fxml APKDecompilerApp
if %ERRORLEVEL% neq 0 (
    echo Exécution échouée. Vérifiez les erreurs ci-dessus.
    pause
    exit /b %ERRORLEVEL%
)