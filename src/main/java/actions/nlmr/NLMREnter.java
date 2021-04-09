package actions.nlmr;

import actions.ConcreteAction;
import structure.hp.HeavyProcessStructure;
import structure.nlmr.NLMRStructure;
import utils.CodeLocation;

public class NLMREnter extends ConcreteAction {

    public NLMREnter(CodeLocation location) {
        super(location);
    }

    public void execute(NLMRStructure processStructure, long date) {
        //NLMRStructure toReturn = new NLMRStructure(this.location, id);
        processStructure.begin(date);
    }

    @Override
    public String generateBreakPoint() {
        return null;
    }
}
