package events.hp;

import events.ConcreteEvent;
import structure.HeavyProcessStructure;
import utils.CodeLocation;

public class HPExit extends ConcreteEvent {
    public HPExit(CodeLocation location) {
        super(location);
    }

    public void execute(HeavyProcessStructure processStructure, long date) {
        this.isExecuted=true;
        processStructure.end(date);
    }

    @Override
    public String generateBreakPoint() {
        return null;
    }
}
