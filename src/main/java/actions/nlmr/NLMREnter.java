package actions.nlmr;

import actions.ConcreteAction;
import structure.hp.HeavyProcessStructure;
import structure.nlmr.NLMRStructure;
import utils.CodeLocation;

public class NLMREnter extends ConcreteAction {

    public NLMREnter(CodeLocation location) {
        super(location);
    }

    public NLMRStructure execute(String id, long date) {
        NLMRStructure toReturn = new NLMRStructure(this.location, id);
        toReturn.begin(date);
        return toReturn;
    }

    @Override
    public String generateBreakPoint() {
        return null;
    }
}
