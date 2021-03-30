package manager;

import actions.hp.HPEnter;
import actions.hp.HPExit;
import structure.hp.HeavyProcessStructure;
import structure.iod.OnDrawStructure;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;

public class HPManager implements Manager{

    private HashMap<String, HPEnter> enters; // Key = CodeLocation
    private HashMap<String, HPExit> exits; // Key = CodeLocation
    private HashMap<String, HeavyProcessStructure> structures;

    public HPManager() {
        this.enters = new HashMap<String, HPEnter>();
        this.exits = new HashMap<String, HPExit>();
        this.structures = new HashMap<String, HeavyProcessStructure>();
    }

    @Override
    public void checkStructures() {
        for (java.util.Map.Entry<String, HeavyProcessStructure> stringStructureEntry : this.structures.entrySet()) {
            HashMap.Entry<String, HeavyProcessStructure> pair = (HashMap.Entry) stringStructureEntry;
            pair.getValue().checkStructure();
        }
    }

    @Override
    public void generateCSV(String outputPath) {
        File directory = new File(outputPath);
        if (! directory.exists()){
            directory.mkdir();
        }

        File csvOutputFile = new File(outputPath+"test_HP.csv");
        try (PrintWriter writer = new PrintWriter(csvOutputFile)) {
            writer.write("apk,package,file,method\n");
            for (java.util.Map.Entry<String, HeavyProcessStructure> stringStructureEntry : this.structures.entrySet()) {
                HashMap.Entry<String, HeavyProcessStructure> pair = (HashMap.Entry) stringStructureEntry;
                if (pair.getValue().hasCodeSmell()) {
                    String fileName = pair.getValue().getLocation().getFileName();
                    String methodName = pair.getValue().getLocation().getMethodName();
                    writer.write("apk,package,"+fileName+","+methodName+ "\n");
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
        if ("hasenter".equals(code)) {
            executeEnter(key, fileName, Long.parseLong(id));
        } else if ("hasexit".equals(code)) {
            executeExit(key, fileName, Long.parseLong(id));
        } else if ("hbrenter".equals(code)) {
            executeEnter(key, fileName, Long.parseLong(id));
        } else if ("hbrexit".equals(code)) {
            executeExit(key, fileName, Long.parseLong(id));
        } else if ("hssenter".equals(code)) {
            executeEnter(key, fileName, Long.parseLong(id));
        } else if ("hssexit".equals(code)) {
            executeExit(key, fileName, Long.parseLong(id));
        }
    }

    public void addEnter(String key, HPEnter enter) {
        this.enters.put(key, enter);
    }

    public void addExit(String key, HPExit exit) {
        this.exits.put(key, exit);
    }

    public void executeEnter(String key, String id, long date) {
        this.structures.put(id, this.enters.get(key).execute(id, date));
    }

    public void executeExit(String key, String id, long date) {
        this.exits.get(key).execute(this.structures.get(id), date);
    }

}
