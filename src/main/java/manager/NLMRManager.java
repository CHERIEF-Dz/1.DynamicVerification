package manager;

import actions.hp.HPEnter;
import actions.hp.HPExit;
import actions.nlmr.NLMREnter;
import actions.nlmr.NLMRExit;
import structure.hp.HeavyProcessStructure;
import structure.nlmr.NLMRStructure;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
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
    public void generateCSV(String outputPath) {
        File directory = new File(outputPath);
        if (! directory.exists()){
            directory.mkdir();
        }

        File csvOutputFile = new File(outputPath+"test_NLMR.csv");
        try (PrintWriter writer = new PrintWriter(csvOutputFile)) {
            writer.write("apk,package,file,method,average Memory released,maximum memory released\n");
            for (java.util.Map.Entry<String, NLMRStructure> stringStructureEntry : this.structures.entrySet()) {
                HashMap.Entry<String, NLMRStructure> pair = (HashMap.Entry) stringStructureEntry;
                if (pair.getValue().hasCodeSmell()) {
                    String fileName = pair.getValue().getLocation().getFileName();
                    String methodName = pair.getValue().getLocation().getMethodName();
                    writer.write("apk,package,"+fileName+","+methodName+ ","+pair.getValue().getBetterMemory()+","+pair.getValue().getAverageMemory()+"\n");
                }
            }
        } catch (FileNotFoundException e) {
            // Do something
        }
    }

    @Override
    public String getBreakpoints() {
        return null;
    }

    @Override
    public void execute(String key, String fileName, String lineNumber, String code, String id) {
        key = key.replace("$Runner", "");
        fileName = fileName.replace("$Runner", "");
        if ("nlmrenter".equals(code)) {
            executeEnter(key, fileName, Long.parseLong(id));
        } else if ("nlmrexit".equals(code)) {
            executeExit(key, fileName, Long.parseLong(id));
        }
    }

    public void addEnter(String key, NLMREnter enter) {
        this.enters.put(key, enter);
    }

    public void addExit(String key, NLMRExit exit) {
        this.exits.put(key, exit);
    }

    public void executeEnter(String key, String id, long date) {
        this.structures.put(id, this.enters.get(key).execute(id, date));
    }

    public void executeExit(String key, String id, long date) {
        System.out.println("Key : " + key);
        for (java.util.Map.Entry<String, NLMRExit> exitEntry : this.exits.entrySet()) {
            HashMap.Entry<String, NLMRExit> pair = (HashMap.Entry) exitEntry;
            System.out.println("Exit : " + pair.getKey());
        }
        System.out.println("Id : " + id);
        for (java.util.Map.Entry<String, NLMRStructure> stringStructureEntry : this.structures.entrySet()) {
            HashMap.Entry<String, NLMRStructure> pair = (HashMap.Entry) stringStructureEntry;
            System.out.println("Structure : " + pair.getKey());
        }

        this.exits.get(key).execute(this.structures.get(id), date);
    }
}
