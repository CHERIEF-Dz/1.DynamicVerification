package manager;

import actions.iod.IODEnter;
import actions.iod.IODExit;
import structure.dw.WakeLockStructure;
import structure.iod.OnDrawStructure;

import java.util.HashMap;

public class IODManager implements Manager{
    private HashMap<String, IODEnter> enters; // Key = CodeLocation
    private HashMap<String, IODExit> exits; // Key = CodeLocation
    private HashMap<String, OnDrawStructure> structures;

    public IODManager() {
        this.enters = new HashMap<String, IODEnter>();
        this.exits = new HashMap<String, IODExit>();
        this.structures = new HashMap<String, OnDrawStructure>();
    }

    @Override
    public void checkStructures() {
        for (java.util.Map.Entry<String, OnDrawStructure> stringStructureEntry : this.structures.entrySet()) {
            HashMap.Entry<String, OnDrawStructure> pair = (HashMap.Entry) stringStructureEntry;
            pair.getValue().checkStructure();
        }
    }

    public void addEnter(String key, IODEnter enter) {
        this.enters.put(key, enter);
    }

    public void addExit(String key, IODExit exit) {
        this.exits.put(key, exit);
    }

    public void executeEnter(String key, String id, long date) {
        this.structures.put(id, this.enters.get(key).execute(id, date));
    }

    public void executeExit(String key, String id, long date) {
        this.exits.get(key).execute(this.structures.get(id), date);
    }

    @Override
    public void generateCSV(String outputPath) {
        //Todo
    }

    @Override
    public String getBreakpoints() {
        //Todo
        return null;
    }
}
