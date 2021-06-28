package manager;

import events.ConcreteEvent;
import events.dw.DWAcquire;
import events.dw.DWRelease;
import structure.dw.WakeLockStructure;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class DWManager implements Manager {
    private HashMap<String, DWAcquire> acquires;
    private HashMap<String, DWRelease> releases;
    private HashMap<String, WakeLockStructure> structures;

    public DWManager() {
        this.acquires = new HashMap<String, DWAcquire>();
        this.releases = new HashMap<String, DWRelease>();
        this.structures = new HashMap<String, WakeLockStructure>();
    }

    public void addAcquire(String key, DWAcquire acquire) {
        this.acquires.put(key, acquire);
    }

    public void addRelease(String key, DWRelease release) {
        this.releases.put(key, release);
    }

    public void executeAcquire(String key, String id) {
        this.structures.put(id, this.acquires.get(key).execute(id));
    }

    public void executeRelease(String key, String id) {
        // If there is a release without acquire, no need
        if (this.structures.get(id)!=null) {
            this.releases.get(key).execute(this.structures.get(id));
        }
    }

    public void checkStructures() {
        for (java.util.Map.Entry<String, WakeLockStructure> stringStructureEntry : this.structures.entrySet()) {
            HashMap.Entry<String, WakeLockStructure> pair = (HashMap.Entry) stringStructureEntry;
            pair.getValue().checkStructure();
        }
    }

    public void generateCSV(String outputPath, String apkName, String packageName, boolean returnAllInstances) throws IOException {

        File directory = new File(outputPath);
        if (! directory.exists()){
            directory.mkdir();
        }

        //Print for coverage
        if (returnAllInstances) {
            File coverageOutputfile = new File(outputPath + "coverage.csv");
            try (PrintWriter writer = new PrintWriter(new FileWriter(coverageOutputfile, true))) {
                writer.write("Number of DW Acquires," + this.acquires.size() + "\n");
                writer.write("Number of DW Releases," + this.releases.size() + "\n");
            } catch (FileNotFoundException e) {
                // Do something
            }

            File executionOutputFile = new File(outputPath + "execution.csv");
            try (PrintWriter writer = new PrintWriter(new FileWriter(executionOutputFile, true))) {
                int executionSumAcquire=0;
                for (Map.Entry<String, DWAcquire> executioncountEntry : this.acquires.entrySet()) {
                    if (executioncountEntry.getValue().isExecuted) {
                        executionSumAcquire++;
                    }
                }
                int executionSumRelease=0;
                for (Map.Entry<String, DWRelease> executioncountEntry : this.releases.entrySet()) {
                    if (executioncountEntry.getValue().isExecuted) {
                        executionSumRelease++;
                    }
                }
                writer.write("Number of DW Acquires," + executionSumAcquire + "\n");
                writer.write("Number of DW Releases," + executionSumRelease + "\n");
            } catch (FileNotFoundException e) {
                // Do something
            }
        }

        File csvOutputFile = new File(outputPath+"results_DW.csv");
        try (PrintWriter writer = new PrintWriter(csvOutputFile)) {
            writer.write("apk, package, file, method\n");
            for (java.util.Map.Entry<String, WakeLockStructure> stringStructureEntry : this.structures.entrySet()) {
                HashMap.Entry<String, WakeLockStructure> pair = (HashMap.Entry) stringStructureEntry;
                if (pair.getValue().hasCodeSmell()) {
                    String fileName = pair.getValue().getLocation().getFileName();
                    String methodName = pair.getValue().getLocation().getMethodName();
                    writer.write(apkName+ ","+ packageName +","+fileName+","+methodName+"\n");
                }
            }
        } catch (FileNotFoundException e) {
            // Do something
        }

        if (returnAllInstances) {
            File csvOutputFileAll = new File(outputPath + "results_DW_all.csv");
            try (PrintWriter writer = new PrintWriter(csvOutputFileAll)) {
                writer.write("apk, package, file, method\n");
                for (java.util.Map.Entry<String, WakeLockStructure> stringStructureEntry : this.structures.entrySet()) {
                    HashMap.Entry<String, WakeLockStructure> pair = (HashMap.Entry) stringStructureEntry;
                    String fileName = pair.getValue().getLocation().getFileName();
                    String methodName = pair.getValue().getLocation().getMethodName();
                    writer.write(apkName + "," + packageName + "," + fileName + "," + methodName + "\n");
                }
            } catch (FileNotFoundException e) {
                // Do something
            }
        }
    }

    @Override
    public void execute(String key, String fileName, String lineNumber, String code, String id) {
        if ("dwacq".equals(code)) {
            executeAcquire(key, id);
        } else if ("dwrel".equals(code)) {
            executeRelease(key, id);
        }
    }
}
