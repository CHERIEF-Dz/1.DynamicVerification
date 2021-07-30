package events.nlmr;

import events.ConcreteEvent;
import structure.NLMRStructure;
import utils.CodeLocation;

public class NLMREnter extends ConcreteEvent {

    public NLMREnter(CodeLocation location) {
        super(location);
    }

    public void execute(NLMRStructure processStructure, long date) {
        this.isExecuted=true;
        //NLMRStructure toReturn = new NLMRStructure(this.location, id);
        processStructure.begin(date);
    }

    @Override
    public String generateBreakPoint() {
        return null;
    }
}
