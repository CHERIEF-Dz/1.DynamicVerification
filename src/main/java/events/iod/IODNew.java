package events.iod;

import events.ConcreteEvent;
import structure.iod.OnDrawStructure;
import utils.CodeLocation;

public class IODNew  extends ConcreteEvent {

    public IODNew(CodeLocation location) {
        super(location);
    }

    public void execute(OnDrawStructure onDrawMethod) {

        this.isExecuted=true;
        onDrawMethod.newInstance();
    }

    @Override
    public String generateBreakPoint() {
        return null;
    }
}
