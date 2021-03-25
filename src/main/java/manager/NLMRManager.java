package manager;

import actions.hp.HPEnter;
import actions.hp.HPExit;
import actions.nlmr.NLMREnter;
import actions.nlmr.NLMRExit;
import structure.hp.HeavyProcessStructure;
import structure.nlmr.NLMRStructure;

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

    }

    @Override
    public String getBreakpoints() {
        return null;
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
        this.exits.get(key).execute(this.structures.get(id), date);
    }
}
