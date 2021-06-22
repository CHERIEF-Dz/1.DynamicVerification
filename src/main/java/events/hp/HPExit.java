package events.hp;

import events.ConcreteEvent;
import structure.hp.HeavyProcessStructure;
import utils.CodeLocation;

public class HPExit extends ConcreteEvent {
    public HPExit(CodeLocation location) {
        super(location);
    }

    public void execute(HeavyProcessStructure processStructure, long date) {
        processStructure.end(date);
    }

    @Override
    public String generateBreakPoint() {
        return null;
    }
}
