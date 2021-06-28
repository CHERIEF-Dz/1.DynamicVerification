package events;

import utils.CodeLocation;

public abstract class ConcreteEvent implements Event {
    protected CodeLocation location;
    public boolean isExecuted=false;

    public ConcreteEvent(CodeLocation location) {
        this.location = location;
    }
}
