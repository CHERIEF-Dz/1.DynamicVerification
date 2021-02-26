package actions.dw;

import actions.ConcreteAction;
import structure.Structure;
import structure.dw.WakeLockStructure;
import utils.CodeLocation;

public class DWImplementation extends ConcreteAction {
    private String variableName;
    public DWImplementation(CodeLocation location, String variableName) {
        super(location);
        this.variableName=variableName;
    }

    public WakeLockStructure execute(String id) {
        return new WakeLockStructure(this.location, id);
    }

    @Override
    public String generateBreakPoint() {
        return null;
    }
}
