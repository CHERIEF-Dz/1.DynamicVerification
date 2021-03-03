package staticanalyzis;

import actions.hmu.HMUAddition;
import actions.hmu.HMUClean;
import actions.hmu.HMUDeletion;
import actions.hmu.HMUImplementation;
import utils.CodeLocation;
import utils.HMUManager;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Analyzer {



    public Analyzer() {

    }

    private void checkHMUInst(String line, String path, String name, String methodName, int lineNumber, HMUManager manager) {
        String regex = "[^\\s]+[\\s]+\\=[\\s]+new[\\s]+HashMap<[^>]*>\\(\\);";
        Pattern pat = Pattern.compile(regex);
        Matcher m = pat.matcher(line);
        if (m.find()) {
            String key=name+":"+lineNumber;
            String variableName=m.group(0).split("=")[0].replaceAll("\\s","");
            manager.addImplementation(key, new HMUImplementation(new CodeLocation(path, name, methodName, lineNumber), "HashMap", variableName));
        }
        else {
            regex = "[^\\s]+[\\s]+\\=[\\s]+new[\\s]+ArrayMap<[^>]*>\\(\\);";
            pat = Pattern.compile(regex);
            m = pat.matcher(line);
            if (m.find()) {
                String key=name+":"+lineNumber;
                String variableName=m.group(0).split("=")[0].replaceAll("\\s","");
                manager.addImplementation(key, new HMUImplementation(new CodeLocation(path, name, methodName, lineNumber), "ArrayMap", variableName));
            }
            else {
                regex = "[^\\s]+[\\s]+\\=[\\s]+new[\\s]+SimpleArrayMap<[^>]*>\\(\\);";
                pat = Pattern.compile(regex);
                m = pat.matcher(line);
                if (m.find()) {
                    String key=name+":"+lineNumber;
                    String variableName=m.group(0).split("=")[0].replaceAll("\\s","");
                    manager.addImplementation(key, new HMUImplementation(new CodeLocation(path, name, methodName, lineNumber), "SimpleArrayMap", variableName));
                }
            }
        }
    }

    //Limité par les expressions régulières et analyse statique

    public void checkHMUAdd(String line, String path, String name, String methodName, int lineNumber, HMUManager manager) {
        String regex = "[^\\.\\s]+\\.put\\([^;]+;";
        Pattern pat = Pattern.compile(regex);
        Matcher m = pat.matcher(line);
        if (m.find()) {
            String key=name+":"+lineNumber;
            String variableName=m.group(0).split("\\.")[0];
            manager.addAddition(key, new HMUAddition(new CodeLocation(path, name, methodName, lineNumber), variableName));
        }
    }

    public void checkHMUDel(String line, String path, String name, String methodName, int lineNumber, HMUManager manager) {
        String regex = "[^\\.\\s]+\\.remove\\([^;]+;";
        Pattern pat = Pattern.compile(regex);
        Matcher m = pat.matcher(line);
        if (m.find()) {
            String key=name+":"+lineNumber;
            String variableName=m.group(0).split("\\.")[0];
            manager.addDeletion(key, new HMUDeletion(new CodeLocation(path, name, methodName, lineNumber), variableName));
        }
    }

    private void checkHMUClean(String line, String path, String name, String methodName, int lineNumber, HMUManager manager) {
        String regex = "[^\\.\\s]+\\.clear\\([^;]+;";
        Pattern pat = Pattern.compile(regex);
        Matcher m = pat.matcher(line);
        if (m.find()) {
            String key=name+":"+lineNumber;
            String variableName=m.group(0).split("\\.")[0];
            manager.addClean(key, new HMUClean(new CodeLocation(path, name, methodName, lineNumber), variableName));
        }
    }

    private boolean isJavaFile(String fileName) {
        return fileName.endsWith(".java");
    }

    public void listFilesForFolder(final File folder, HMUManager manager, String originalPath) throws IOException {
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry, manager, originalPath);
            } else {
                String fileName = fileEntry.getName();
                String methodName = "";
                if (this.isJavaFile(fileName)) {
                    String path = fileEntry.getPath();
                    path = path.replace(originalPath.replace("/", "\\"), "").replace("\\", "/");
                    //System.out.println("File : " + fileName);
                    //System.out.println("Path : " + path);
                    List<String> lines = Files.readAllLines(fileEntry.toPath(), StandardCharsets.UTF_8);
                    int lineNumber = 1;
                    for (String line : lines) {
                        //Instanciation
                        checkHMUInst(line, path, fileName, methodName, lineNumber, manager);
                        checkHMUAdd(line, path, fileName, methodName, lineNumber, manager);
                        checkHMUDel(line, path, fileName, methodName, lineNumber, manager);
                        checkHMUClean(line, path, fileName, methodName, lineNumber, manager);
                        lineNumber++;
                    }
                }
            }
        }
    }

    public void analyze(HMUManager manager, String originalPath) throws IOException {
        final File folder = new File(originalPath);
        listFilesForFolder(folder, manager, originalPath);
    }
}
