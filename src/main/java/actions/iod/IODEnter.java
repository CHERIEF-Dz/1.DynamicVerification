package actions.iod;

import actions.ConcreteAction;
import structure.iod.OnDrawStructure;
import utils.CodeLocation;

public class IODEnter extends ConcreteAction {
    public IODEnter(CodeLocation location) {
        super(location);
    }

    public OnDrawStructure execute(String id, String date) {
        OnDrawStructure toReturn = new OnDrawStructure(this.location, id);
        toReturn.begin(date);
        return toReturn;
    }

    @Override
    public String generateBreakPoint() {
        return null;
    }
}
