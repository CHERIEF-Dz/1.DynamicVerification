package manager;

import events.nlmr.NLMREnter;
import events.nlmr.NLMRExit;
import staticanalyzis.NLMRAnalyzer;
import structure.nlmr.NLMRStructure;

import java.io.*;
import java.util.HashMap;

public class NLMRManager implements Manager{
    private HashMap<String, NLMREnter> enters; // Key = CodeLocation
    private HashMap<String, NLMRExit> exits; // Key = CodeLocation
    private HashMap<String, NLMRStructure> structures;

    public NLMRManager() {
        this.enters = new HashMap<String, NLMREnter>();
        this.exits = new HashMap<String, NLMRExit>();
        this.structures = new HashMap<String, NLMRStructure>();
    }

    @Override
    public void checkStructures() {
        for (java.util.Map.Entry<String, NLMRStructure> stringStructureEntry : this.structures.entrySet()) {
            HashMap.Entry<String, NLMRStructure> pair = (HashMap.Entry) stringStructureEntry;
            pair.getValue().checkStructure();
        }
    }

    @Override
    public void generateCSV(String outputPath, String apkName, String packageName, boolean returnAllInstances) throws IOException {
        File directory = new File(outputPath);
        if (! directory.exists()){
            directory.mkdir();
        }

        if (returnAllInstances) {
            //Print for coverage
            File coverageOutputfile = new File(outputPath + "coverage.csv");
            try (PrintWriter writer = new PrintWriter(new FileWriter(coverageOutputfile, true))) {
                writer.write("Number of NLMR methods," + this.enters.size() + "\n");
            } catch (FileNotFoundException e) {
                // Do something
            }
        }


        File csvOutputFile = new File(outputPath+"results_NLMR.csv");
        try (PrintWriter writer = new PrintWriter(csvOutputFile)) {
            writer.write("apk,package,file,method,average Memory released,maximum memory released\n");
            for (java.util.Map.Entry<String, NLMRStructure> stringStructureEntry : this.structures.entrySet()) {
                HashMap.Entry<String, NLMRStructure> pair = (HashMap.Entry) stringStructureEntry;
                if (pair.getValue().hasCodeSmell()) {
                    String fileName = pair.getValue().getLocation().getFileName();
                    String methodName = pair.getValue().getLocation().getMethodName();
                    writer.write(apkName+ ","+ packageName +","+fileName+","+methodName+ ","+pair.getValue().getBetterMemory()+","+pair.getValue().getAverageMemory()+"\n");
                }
            }
        } catch (FileNotFoundException e) {
            // Do something
        }

        if (returnAllInstances) {
            File csvOutputFileAll = new File(outputPath + "results_NLMR_all.csv");
            try (PrintWriter writer = new PrintWriter(csvOutputFileAll)) {
                writer.write("apk,package,file,method,average Memory released,maximum memory released\n");
                for (java.util.Map.Entry<String, NLMRStructure> stringStructureEntry : this.structures.entrySet()) {
                    HashMap.Entry<String, NLMRStructure> pair = (HashMap.Entry) stringStructureEntry;
                    String fileName = pair.getValue().getLocation().getFileName();
                    String methodName = pair.getValue().getLocation().getMethodName();
                    writer.write(apkName + "," + packageName + "," + fileName + "," + methodName + "," + pair.getValue().getBetterMemory() + "," + pair.getValue().getAverageMemory() + "\n");
                }
            } catch (FileNotFoundException e) {
                // Do something
            }
        }
    }

    @Override
    public void execute(String key, String fileName, String lineNumber, String code, String id) {
        key = key.replace(NLMRAnalyzer.runnerSuffix, "");
        fileName = fileName.replace(NLMRAnalyzer.runnerSuffix, "");
        if ("nlmrenter".equals(code)) {
            executeEnter(key.replace("$onTrimMemory",""), fileName.replace("$onTrimMemory",""), Long.parseLong(id));
        } else if ("nlmrexit".equals(code)) {
            executeExit(key.replace("$run",""), fileName.replace("$run",""), Long.parseLong(id));
        }
    }

    public void addEnter(String key, NLMREnter enter) {
        this.enters.put(key, enter);
    }

    public void addExit(String key, NLMRExit exit) {
        this.exits.put(key, exit);
    }

    public void addStructure(String key, NLMRStructure structure) {this.structures.put(key, structure);}

    public void executeEnter(String key, String id, long date) {
        this.enters.get(key).execute(this.structures.get(id), date);
    }

    public void executeExit(String key, String id, long date) {
        this.exits.get(key).execute(this.structures.get(id), date);
    }
}
