package manager;

import actions.iod.IODEnter;
import actions.iod.IODExit;
import actions.iod.IODNew;
import structure.dw.WakeLockStructure;
import structure.hmu.ArrayMapStructure;
import structure.hmu.MapStructure;
import structure.iod.OnDrawStructure;
import structure.nlmr.NLMRStructure;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;

public class IODManager implements Manager{
    private HashMap<String, IODEnter> enters; // Key = CodeLocation
    private HashMap<String, IODExit> exits; // Key = CodeLocation
    private HashMap<String, IODNew> news;
    private HashMap<String, OnDrawStructure> structures;

    public IODManager() {
        this.enters = new HashMap<String, IODEnter>();
        this.exits = new HashMap<String, IODExit>();
        this.news = new HashMap<String, IODNew>();
        this.structures = new HashMap<String, OnDrawStructure>();
    }

    @Override
    public void checkStructures() {
        for (java.util.Map.Entry<String, OnDrawStructure> stringStructureEntry : this.structures.entrySet()) {
            HashMap.Entry<String, OnDrawStructure> pair = (HashMap.Entry) stringStructureEntry;
            System.out.println("Structure : " + pair.getKey() + " has " + pair.getValue().getWorstInstantations());
            pair.getValue().checkStructure();
        }
    }

    public void addEnter(String key, IODEnter enter) {
        this.enters.put(key, enter);
    }

    public void addExit(String key, IODExit exit) {
        this.exits.put(key, exit);
    }

    public void addNew(String key, IODNew newElement) { this.news.put(key, newElement); }

    public void addStructure(String key, OnDrawStructure structure) {this.structures.put(key, structure);}

    public void executeEnter(String key, String id, long date) {
        //this.structures.put(id, this.enters.get(key).execute(id, date));
        /*
        System.out.println("Need : " + key + " and " + id);
        for (java.util.Map.Entry<String, OnDrawStructure> stringStructureEntry : this.structures.entrySet()) {
            HashMap.Entry<String, OnDrawStructure> pair = (HashMap.Entry) stringStructureEntry;
            System.out.println("Structure : " + pair.getKey() + " " + pair.getValue().getId() + " has " + pair.getValue().getWorstInstantations());
        }
        */
        this.enters.get(key).execute(this.structures.get(id), date);
    }

    public void executeExit(String key, String id, long date) {
        this.exits.get(key).execute(this.structures.get(id), date);
    }

    public void executeNew(String key, String id) {
        this.news.get(key).execute(this.structures.get(id));
    }

    @Override
    public void generateCSV(String outputPath) {
        File directory = new File(outputPath);
        if (! directory.exists()){
            directory.mkdir();
        }

        File csvOutputFile = new File(outputPath+"test_IOD.csv");
        try (PrintWriter writer = new PrintWriter(csvOutputFile)) {
            writer.write("apk,package,file,method,average executing time,worst executing time,average number of instantiations,worst number of instantiations \n");
            for (java.util.Map.Entry<String, OnDrawStructure> stringStructureEntry : this.structures.entrySet()) {
                HashMap.Entry<String, OnDrawStructure> pair = (HashMap.Entry) stringStructureEntry;
                if (pair.getValue().hasCodeSmell()) {
                    String fileName = pair.getValue().getLocation().getFileName();
                    String methodName = pair.getValue().getLocation().getMethodName();
                    OnDrawStructure structure =  pair.getValue();
                    writer.write("apk,package,"+fileName+","+methodName+ ","+structure.getAverageTime()+","+structure.getWorstTime()+","+structure.getAverageInstantiations()+","+structure.getWorstInstantations()+"\n");
                }
            }
        } catch (FileNotFoundException e) {
            // Do something
        }
    }

    @Override
    public String getBreakpoints() {
        //Todo
        return null;
    }

    @Override
    public void execute(String key, String fileName, String lineNumber, String code, String id) {
        if ("iodenter".equals(code)) {
            executeEnter(key, fileName, Long.parseLong(id));
        } else if ("iodexit".equals(code)) {
            executeExit(key, fileName, Long.parseLong(id));
        } else if ("iodnew".equals(code)) {
            executeNew(key, fileName);
        }
    }
}
