package actions.dw;

import actions.ConcreteAction;
import structure.dw.WakeLockStructure;
import utils.CodeLocation;

public class DWRelease extends ConcreteAction {
    private String variableName;
    public DWRelease(CodeLocation location, String variableName) {
        super(location);
        this.variableName=variableName;
    }

    public void execute(WakeLockStructure lockAcquired) {
        lockAcquired.release();
    }

    @Override
    public String generateBreakPoint() {
        return null;
    }
}
