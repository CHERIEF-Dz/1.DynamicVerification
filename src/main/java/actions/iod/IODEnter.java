package actions.iod;

import actions.ConcreteAction;
import utils.CodeLocation;

public class IODEnter extends ConcreteAction {
    public IODEnter(CodeLocation location) {
        super(location);
    }

    @Override
    public String generateBreakPoint() {
        return null;
    }
}
