package actions.dw;

import actions.ConcreteAction;
import structure.dw.WakeLockStructure;
import utils.CodeLocation;

public class DWAcquire extends ConcreteAction {
    private String variableName;
    public DWAcquire(CodeLocation location, String variableName) {
        super(location);
        this.variableName=variableName;
    }

    public WakeLockStructure execute(String id) {
        WakeLockStructure toReturn = new WakeLockStructure(this.location, id);
        toReturn.acquire();
        return toReturn;
    }

    @Override
    public String generateBreakPoint() {
        return null;
    }
}
