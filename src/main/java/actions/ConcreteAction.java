package actions;

import utils.CodeLocation;

public abstract class ConcreteAction implements Action {
    protected CodeLocation location;

    public ConcreteAction(CodeLocation location) {
        this.location = location;
    }
}
