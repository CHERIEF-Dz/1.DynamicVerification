package actions.iod;

import actions.ConcreteAction;
import structure.dw.WakeLockStructure;
import structure.iod.OnDrawStructure;
import utils.CodeLocation;

public class IODExit extends ConcreteAction {
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
