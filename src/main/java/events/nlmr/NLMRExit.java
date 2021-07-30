package events.nlmr;

import events.ConcreteEvent;
import structure.NLMRStructure;
import utils.CodeLocation;

public class NLMRExit extends ConcreteEvent {
    public NLMRExit(CodeLocation location) {
        super(location);
    }

    public void execute(NLMRStructure processStructure, long date) {

        this.isExecuted=true;
        processStructure.end(date);
    }

    @Override
    public String generateBreakPoint() {
        return null;
    }
}
