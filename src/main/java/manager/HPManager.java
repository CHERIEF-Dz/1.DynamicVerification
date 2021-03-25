package manager;

import actions.hp.HPEnter;
import actions.hp.HPExit;
import structure.hp.HeavyProcessStructure;

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

    }

    @Override
    public String getBreakpoints() {
        return null;
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
