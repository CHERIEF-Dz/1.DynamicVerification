package events.iod;

import events.ConcreteEvent;
import structure.OnDrawStructure;
import utils.CodeLocation;

public class IODExit extends ConcreteEvent {
    public IODExit(CodeLocation location) {
        super(location);
    }

    public void execute(OnDrawStructure onDrawMethod, long date) {
        this.isExecuted=true;
        onDrawMethod.end(date);
    }

    @Override
    public String generateBreakPoint() {
        return null;
    }
}
