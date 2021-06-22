package events.iod;

import events.ConcreteEvent;
import structure.iod.OnDrawStructure;
import utils.CodeLocation;

public class IODExit extends ConcreteEvent {
    public IODExit(CodeLocation location) {
        super(location);
    }

    public void execute(OnDrawStructure onDrawMethod, long date) {
        onDrawMethod.end(date);
    }

    @Override
    public String generateBreakPoint() {
        return null;
    }
}
