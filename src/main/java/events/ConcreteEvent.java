package events;

import utils.CodeLocation;

public abstract class ConcreteEvent implements Event {
    protected CodeLocation location;

    public ConcreteEvent(CodeLocation location) {
        this.location = location;
    }
}
