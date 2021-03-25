package actions.nlmr;

import actions.ConcreteAction;
import structure.hp.HeavyProcessStructure;
import structure.nlmr.NLMRStructure;
import utils.CodeLocation;

public class NLMRExit extends ConcreteAction {
    public NLMRExit(CodeLocation location) {
        super(location);
    }

    public void execute(NLMRStructure processStructure, long date) {
        processStructure.end(date);
    }

    @Override
    public String generateBreakPoint() {
        return null;
    }
}
