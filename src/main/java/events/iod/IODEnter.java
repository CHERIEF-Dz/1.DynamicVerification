package events.iod;

import events.ConcreteEvent;
import structure.OnDrawStructure;
import utils.CodeLocation;

public class IODEnter extends ConcreteEvent {
    public IODEnter(CodeLocation location) {
        super(location);
    }

    public void execute(OnDrawStructure structure, long date) {
        this.isExecuted=true;
        //OnDrawStructure toReturn = new OnDrawStructure(this.location, id);
        structure.begin(date);
        //return toReturn;
    }

    @Override
    public String generateBreakPoint() {
        return null;
    }
}
