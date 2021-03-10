package actions.iod;

import actions.ConcreteAction;
import utils.CodeLocation;

public class IODExit extends ConcreteAction {
    public IODExit(CodeLocation location) {
        super(location);
    }

    @Override
    public String generateBreakPoint() {
        return null;
    }
}
