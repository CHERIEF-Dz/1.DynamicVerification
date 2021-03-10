package manager;

import actions.hmu.HMUAddition;
import actions.hmu.HMUClean;
import actions.hmu.HMUDeletion;
import actions.hmu.HMUImplementation;
import structure.hmu.MapStructure;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
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

    public void addDeletion(String key, HMUDeletion deletion) {
        this.deletions.put(key, deletion);
    }

    public void addClean(String key, HMUClean clean) {
        this.cleans.put(key, clean);
    }

    public void executeImplementation(String key, String id) {
        System.out.println("Implementation : " + key);
        System.out.println("Implementations : ");
        for (java.util.Map.Entry<String, HMUImplementation> stringStructureEntry : this.implementations.entrySet()) {
            HashMap.Entry<String, HMUImplementation> pair = (HashMap.Entry) stringStructureEntry;
            System.out.println(pair.getKey());
        }
        System.out.println("Additions : ");
        for (java.util.Map.Entry<String, HMUAddition> stringStructureEntry : this.additions.entrySet()) {
            HashMap.Entry<String, HMUAddition> pair = (HashMap.Entry) stringStructureEntry;
            System.out.println(pair.getKey());
        }
        System.out.println("Deletions : ");
        for (java.util.Map.Entry<String, HMUDeletion> stringStructureEntry : this.deletions.entrySet()) {
            HashMap.Entry<String, HMUDeletion> pair = (HashMap.Entry) stringStructureEntry;
            System.out.println(pair.getKey());
        }
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
    public void generateCSV() {
        File csvOutputFile = new File("test_HMU.csv");
        try (PrintWriter writer = new PrintWriter(csvOutputFile)) {
            writer.write("apk, package, file, method\n");
            for (java.util.Map.Entry<String, MapStructure> stringStructureEntry : this.structures.entrySet()) {
                HashMap.Entry<String, MapStructure> pair = (HashMap.Entry) stringStructureEntry;
                if (pair.getValue().hasCodeSmell()) {
                    String fileName = pair.getValue().getLocation().getFileName();
                    String methodName = pair.getValue().getLocation().getMethodName();
                    writer.write("apk,package,"+fileName+","+methodName+"\n");
                }
            }
        } catch (FileNotFoundException e) {
            // Do something
        }
    }
}
