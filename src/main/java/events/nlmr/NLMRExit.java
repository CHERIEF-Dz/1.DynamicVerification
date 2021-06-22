package events.nlmr;

import events.ConcreteEvent;
import structure.nlmr.NLMRStructure;
import utils.CodeLocation;

public class NLMRExit extends ConcreteEvent {
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
