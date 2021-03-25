package actions.hp;

import actions.ConcreteAction;
import structure.hp.HeavyProcessStructure;
import structure.iod.OnDrawStructure;
import utils.CodeLocation;

public class HPExit extends ConcreteAction {
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
