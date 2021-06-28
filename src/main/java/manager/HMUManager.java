package manager;

import events.dw.DWAcquire;
import events.dw.DWRelease;
import events.hmu.HMUAddition;
import events.hmu.HMUClean;
import events.hmu.HMUDeletion;
import events.hmu.HMUImplementation;
import structure.hmu.ArrayMapStructure;
import structure.hmu.MapStructure;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class HMUManager implements Manager{
    private HashMap<String, HMUImplementation> implementations; // Key = CodeLocation
    private HashMap<String, HMUAddition> additions; // Key = CodeLocation
    private HashMap<String, HMUDeletion> deletions; // Key = CodeLocation
    private HashMap<String, HMUClean> cleans; // Key = CodeLocation
    private HashMap<String, MapStructure> structures; // Key = unique id

    public HMUManager() {
        this.implementations = new HashMap<String, HMUImplementation>();
        this.additions = new HashMap<String, HMUAddition>();
        this.deletions = new HashMap<String, HMUDeletion>();
        this.cleans = new HashMap<String, HMUClean>();
        this.structures = new HashMap<String, MapStructure>();
    }

    public void addImplementation(String key, HMUImplementation implementation) {
        //System.out.println(implementation.generateBreakPoint());
        this.implementations.put(key, implementation);
    }

    public void addAddition(String key, HMUAddition addition) {
        //System.out.println(addition.generateBreakPoint());
        this.additions.put(key, addition);
    }

    public String getBreakpoints() {
        String tags = "";
        for (Map.Entry<String, HMUImplementation> implementationStructureEntry : this.implementations.entrySet()) {
            HashMap.Entry<String, HMUImplementation> pair = (HashMap.Entry) implementationStructureEntry;
            tags+=pair.getValue().generateBreakPoint()+"\n";
        }
        for (Map.Entry<String, HMUAddition> additionStructureEntry : this.additions.entrySet()) {
            HashMap.Entry<String, HMUAddition> pair = (HashMap.Entry) additionStructureEntry;
            tags+=pair.getValue().generateBreakPoint()+"\n";
        }
        return tags;
    }

    @Override
    public void execute(String key, String fileName, String lineNumber, String code, String id) {
        if ("hmuimpl".equals(code)) {
            executeImplementation(key, id);
        } else if ("hmuadd".equals(code)) {
            //System.out.println("Addition line !");
            executeAddition(key, id);
        } else if ("hmudel".equals(code)) {
            executeDeletion(key, id);
        } else if ("hmucln".equals(code)) {
            executeClean(key, id);
        }
    }

    public void addDeletion(String key, HMUDeletion deletion) {
        this.deletions.put(key, deletion);
    }

    public void addClean(String key, HMUClean clean) {
        this.cleans.put(key, clean);
    }

    public void executeImplementation(String key, String id) {
        /*
        System.out.println("Need : " + key + " and " + id);
        for (java.util.Map.Entry<String, MapStructure> stringStructureEntry : this.structures.entrySet()) {
            HashMap.Entry<String, MapStructure> pair = (HashMap.Entry) stringStructureEntry;
            System.out.println("Structure : " + pair.getKey() + " " + pair.getValue().getId());
        }
        */
        this.structures.put(id, this.implementations.get(key).execute(id));
    }

    public void executeAddition(String key, String id) {
        this.additions.get(key).execute(this.structures.get(id));
    }

    public void executeDeletion(String key, String id) {
        this.deletions.get(key).execute(this.structures.get(id));
    }

    public void executeClean(String key, String id) {
        this.cleans.get(key).execute(this.structures.get(id));
    }

    public void checkStructures() {
        for (java.util.Map.Entry<String, MapStructure> stringStructureEntry : this.structures.entrySet()) {
            HashMap.Entry<String, MapStructure> pair = (HashMap.Entry) stringStructureEntry;
            pair.getValue().checkStructure();
        }
    }

    @Override
    public void generateCSV(String outputPath, String apkName, String packageName, boolean returnAllInstances) throws IOException {
        File directory = new File(outputPath);
        if (! directory.exists()){
            directory.mkdir();
        }

        //Print for coverage
        if (returnAllInstances) {
            File coverageOutputfile = new File(outputPath + "coverage.csv");
            try (PrintWriter writer = new PrintWriter(new FileWriter(coverageOutputfile, true))) {
                writer.write("Number of HMU Implementations," + this.implementations.size() + "\n");
                writer.write("Number of HMU Additions," + this.additions.size() + "\n");
                writer.write("Number of HMU Deletions," + this.deletions.size() + "\n");
                writer.write("Number of HMU Cleans," + this.cleans.size() + "\n");
            } catch (FileNotFoundException e) {
                // Do something
            }

            File executionOutputFile = new File(outputPath + "execution.csv");
            try (PrintWriter writer = new PrintWriter(new FileWriter(executionOutputFile, true))) {
                int executionSumImplementation=0;
                for (Map.Entry<String, HMUImplementation> executioncountEntry : this.implementations.entrySet()) {
                    if (executioncountEntry.getValue().isExecuted) {
                        executionSumImplementation++;
                    }
                }
                int executionSumAddition=0;
                for (Map.Entry<String, HMUAddition> executioncountEntry : this.additions.entrySet()) {
                    if (executioncountEntry.getValue().isExecuted) {
                        executionSumAddition++;
                    }
                }
                int executionSumDeletion=0;
                for (Map.Entry<String, HMUDeletion> executioncountEntry : this.deletions.entrySet()) {
                    if (executioncountEntry.getValue().isExecuted) {
                        executionSumDeletion++;
                    }
                }
                int executionSumClean=0;
                for (Map.Entry<String, HMUClean> executioncountEntry : this.cleans.entrySet()) {
                    if (executioncountEntry.getValue().isExecuted) {
                        executionSumClean++;
                    }
                }
                writer.write("Number of HMU Implementations," + executionSumImplementation + "\n");
                writer.write("Number of HMU Additions," + executionSumAddition + "\n");
                writer.write("Number of HMU Deletions," + executionSumDeletion + "\n");
                writer.write("Number of HMU Cleans," + executionSumClean + "\n");
            } catch (FileNotFoundException e) {
                // Do something
            }
        }


        HashMap<String, MapStructure> selectedMaps = new HashMap<String, MapStructure>();
        HashMap<String, Integer> selectedMapsValues = new HashMap<String, Integer>();

        for (java.util.Map.Entry<String, MapStructure> stringStructureEntry : this.structures.entrySet()) {
            HashMap.Entry<String, MapStructure> pair = (HashMap.Entry) stringStructureEntry;
            if (pair.getValue().hasCodeSmell()) {
                String fileName = pair.getValue().getLocation().getFileName();
                String methodName = pair.getValue().getLocation().getMethodName();
                String selectionKey = fileName + "/" + packageName + "/" + methodName;
                if (selectedMaps.containsKey(selectionKey)) {
                    if (selectedMapsValues.get(selectionKey) < pair.getValue().getMaximumSize()) {
                        selectedMaps.put(selectionKey, pair.getValue());
                        selectedMapsValues.put(selectionKey, pair.getValue().getMaximumSize());
                    }
                }
                else {
                    selectedMaps.put(selectionKey, pair.getValue());
                    selectedMapsValues.put(selectionKey, pair.getValue().getMaximumSize());
                }
            }
        }

        File csvOutputFile = new File(outputPath+"results_HMU.csv");
        try (PrintWriter writer = new PrintWriter(csvOutputFile)) {
            writer.write("apk,package,file,method,structure Type,maximumSize\n");
            for (java.util.Map.Entry<String, MapStructure> stringStructureEntry : selectedMaps.entrySet()) {
                HashMap.Entry<String, MapStructure> pair = (HashMap.Entry) stringStructureEntry;
                if (pair.getValue().hasCodeSmell()) {
                    String fileName = pair.getValue().getLocation().getFileName();
                    String methodName = pair.getValue().getLocation().getMethodName();
                    String structureType = "HashMap";
                    if (pair.getValue() instanceof ArrayMapStructure) {
                        if (((ArrayMapStructure) pair.getValue()).isSimple) {
                            structureType = "SimpleArrayMap";
                        }
                        else {
                            structureType = "ArrayMap";
                        }
                    }
                    writer.write(apkName+ ","+ packageName +","+fileName+","+methodName+ "," + structureType + "," + pair.getValue().getMaximumSize()+ "\n");
                }
            }
        } catch (FileNotFoundException e) {
            // Do something
        }

        if (returnAllInstances) {
            File csvOutputFileAll = new File(outputPath + "results_HMU_all.csv");
            try (PrintWriter writer = new PrintWriter(csvOutputFileAll)) {
                writer.write("apk,package,file,method,structure Type,maximumSize\n");
                for (java.util.Map.Entry<String, MapStructure> stringStructureEntry : selectedMaps.entrySet()) {
                    HashMap.Entry<String, MapStructure> pair = (HashMap.Entry) stringStructureEntry;
                    String fileName = pair.getValue().getLocation().getFileName();
                    String methodName = pair.getValue().getLocation().getMethodName();
                    String structureType = "HashMap";
                    if (pair.getValue() instanceof ArrayMapStructure) {
                        if (((ArrayMapStructure) pair.getValue()).isSimple) {
                            structureType = "SimpleArrayMap";
                        } else {
                            structureType = "ArrayMap";
                        }
                    }
                    writer.write(apkName + "," + packageName + "," + fileName + "," + methodName + "," + structureType + "," + pair.getValue().getMaximumSize() + "\n");
                }
            } catch (FileNotFoundException e) {
                // Do something
            }
        }
    }
}
