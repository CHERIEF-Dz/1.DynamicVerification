package events.dw;

import events.ConcreteEvent;
import structure.WakeLockStructure;
import utils.CodeLocation;

public class DWRelease extends ConcreteEvent {
    private String variableName;
    public DWRelease(CodeLocation location, String variableName) {
        super(location);
        this.variableName=variableName;
    }

    public void execute(WakeLockStructure lockAcquired) {
        this.isExecuted=true;
        lockAcquired.release();
    }

    @Override
    public String generateBreakPoint() {
        return null;
    }
}
