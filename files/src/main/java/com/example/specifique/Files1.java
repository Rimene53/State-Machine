package com.example.specifique;

import org.apache.commons.scxml.Context;
import org.apache.commons.scxml.SCXMLExecutor;
import org.apache.commons.scxml.TriggerEvent;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

public class Files1 {

    public static void initialize(Context context) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.println("Entrez le chemin du dossier à traiter:");
            String folderPath = br.readLine().trim();
            System.out.println("Entrez l'extension des fichiers à traiter:");
            String fileExtension = br.readLine().trim();

            context.set("folderPath", folderPath);
            context.set("fileExtension", fileExtension);
            context.set("currentFileIndex", 0);
            context.set("extractedText", "");
            context.set("currentFile", null);
            context.set("newFile", null);
            context.set("filesToProcess", null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static void process(String currentState, SCXMLExecutor executor, Context context) {
        String folderPath = (String) context.get("folderPath");
        String fileExtension = (String) context.get("fileExtension");
        Integer idxObj = (Integer) context.get("currentFileIndex");
        int currentFileIndex = idxObj == null ? 0 : idxObj;
        File[] filesToProcess = (File[]) context.get("filesToProcess");
        File currentFile = (File) context.get("currentFile");
        File newFile = (File) context.get("newFile");
        String extractedText = (String) context.get("extractedText");
        if (extractedText == null) extractedText = "";

        System.out.println("Processing state: " + currentState);

        switch (currentState) {
            case "readParameter" -> {
                System.out.printf("Lecture des paramètres - Dossier: %s, Extension: %s%n", folderPath, fileExtension);
                trigger(executor, "parameterRead");
            }
            case "checkParameter" -> {
                System.out.println("Validation des paramètres...");
                if (!isValidParameters(folderPath, fileExtension)) {
                    System.out.println("ERREUR: Paramètres invalides");
                    trigger(executor, "parameterInvalid");
                    return;
                }
                trigger(executor, "parameterValid");
            }
            case "searchFiles" -> {
                System.out.printf("Recherche des fichiers .%s dans %s%n", fileExtension, folderPath);
                filesToProcess = getFiles(folderPath, fileExtension);
                context.set("filesToProcess", filesToProcess);
                System.out.println("Nombre de fichiers trouvés : " + (filesToProcess != null ? filesToProcess.length : 0));
                if (!hasFiles(filesToProcess)) {
                    System.out.printf("Aucun fichier .%s trouvé dans %s. Arrêt du traitement.%n", fileExtension, folderPath);
                    trigger(executor, "noFilesFound");
                    return;
                }
                trigger(executor, "filesFound");
            }
            case "extractText" -> {
                if (hasFiles(filesToProcess) && currentFileIndex < filesToProcess.length) {
                    currentFile = filesToProcess[currentFileIndex];
                    context.set("currentFile", currentFile);
                    System.out.println("Traitement du fichier : " + currentFile.getAbsolutePath());
                    extractedText = extractTextFromFile(currentFile);
                    context.set("extractedText", extractedText);
                    System.out.println("Extraction du texte de " + currentFile.getName() + "... " +
                            (extractedText.isEmpty() ? "Vide" : "OK, longueur: " + extractedText.length()));
                    trigger(executor, !extractedText.isEmpty() ? "textExtracted" : "continue");
                } else {
                    System.out.println("Aucun fichier à traiter ou index hors limites: " + currentFileIndex);
                    trigger(executor, "continue");
                }
            }
            case "moveOriginalFile" -> {
                if (currentFile != null) {
                    System.out.println("Déplacement du fichier original vers archive : " + currentFile.getAbsolutePath());
                    moveOriginalFile(currentFile, folderPath);
                    trigger(executor, "originalFileMoved");
                } else {
                    trigger(executor, "continue");
                }
            }
            case "generateNewFile" -> {
                if (currentFile != null) {
                    newFile = createNewFile(currentFile, folderPath, fileExtension);
                    context.set("newFile", newFile);
                    trigger(executor, "fileGenerated");
                } else {
                    trigger(executor, "continue");
                }
            }
            case "moveText" -> {
                if (newFile != null && !extractedText.isEmpty()) {
                    writeTextToFile(newFile, extractedText);
                    trigger(executor, "textMoved");
                } else {
                    trigger(executor, "continue");
                }
            }
            case "moveFileToNewFolder" -> {
                if (newFile != null) {
                    moveNewFile(newFile, folderPath);
                    trigger(executor, "fileMoved");
                } else {
                    trigger(executor, "continue");
                }
            }
            case "checkNextFile" -> {
                currentFileIndex++;
                context.set("currentFileIndex", currentFileIndex);
                trigger(executor, hasMoreFiles(filesToProcess, currentFileIndex) ? "nextFileExists" : "noMoreFiles");
            }
            case "displayInvalidParameter" -> trigger(executor, "messageDisplayed");
            case "displayNoFilesFound" -> trigger(executor, "messageDisplayed");
            case "displayCompletion" -> trigger(executor, "messageDisplayed");
            default -> System.out.println("État non géré: " + currentState);
        }
    }

    private static void trigger(SCXMLExecutor executor, String event) {
        try {
            if (!executor.getCurrentStatus().isFinal()) {
                System.out.println("Événement déclenché : " + event);
                executor.triggerEvent(new TriggerEvent(event, TriggerEvent.SIGNAL_EVENT));
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du déclenchement de l'événement : " + e.getMessage());
        }
    }

    private static boolean isValidParameters(String folderPath, String fileExtension) {
        File folder = new File(folderPath);
        return folder.exists() && folder.isDirectory() && fileExtension != null && !fileExtension.isEmpty();
    }

    private static boolean hasFiles(File[] filesToProcess) {
        return filesToProcess != null && filesToProcess.length > 0;
    }

    private static boolean hasMoreFiles(File[] filesToProcess, int currentFileIndex) {
        return filesToProcess != null && currentFileIndex < filesToProcess.length;
    }

    private static File[] getFiles(String folderPath, String fileExtension) {
        File folder = new File(folderPath);
        System.out.println("Chemin dossier : " + folder.getAbsolutePath());
        System.out.println("Le dossier existe ? " + folder.exists());
        System.out.println("Est un dossier ? " + folder.isDirectory());

        String[] liste = folder.list();
        System.out.println("Contenu du dossier : " + (liste == null ? "null" : Arrays.toString(liste)));

        if (!folder.exists() || !folder.isDirectory()) return new File[0];

        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith("." + fileExtension.toLowerCase()));
        if (files != null) {
            for (File f : files) System.out.println("Fichier trouvé : " + f.getName());
        }
        return files != null ? files : new File[0];
    }

    private static String extractTextFromFile(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder text = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                text.append(line).append("\n");
            }
            return text.toString();
        } catch (IOException e) {
            System.err.println("Erreur extraction texte : " + e.getMessage());
            return "";
        }
    }

    private static void moveOriginalFile(File file, String folderPath) {
        try {
            File archiveFolder = new File(folderPath, "archive");
            if (!archiveFolder.exists()) archiveFolder.mkdirs();
            File destFile = new File(archiveFolder, file.getName());
            Files.move(file.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("Erreur déplacement fichier original : " + e.getMessage());
        }
    }

    private static void moveNewFile(File file, String folderPath) {
        try {
            File processedFolder = new File(folderPath, "processed");
            if (!processedFolder.exists()) processedFolder.mkdirs();
            File destFile = new File(processedFolder, file.getName());
            Files.move(file.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("Erreur déplacement nouveau fichier : " + e.getMessage());
        }
    }

    private static File createNewFile(File originalFile, String folderPath, String fileExtension) {
        try {
            File outputFolder = new File(folderPath, "output");
            if (!outputFolder.exists()) outputFolder.mkdirs();
            String newFileName = originalFile.getName().replace("." + fileExtension, "_processed." + fileExtension);
            return new File(outputFolder, newFileName);
        } catch (Exception e) {
            System.err.println("Erreur création nouveau fichier : " + e.getMessage());
            return null;
        }
    }

    private static void writeTextToFile(File file, String text) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(text);
        } catch (IOException e) {
            System.err.println("Erreur écriture fichier : " + e.getMessage());
        }
    }
}
