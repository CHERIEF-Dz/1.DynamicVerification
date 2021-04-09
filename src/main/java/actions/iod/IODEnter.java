package actions.iod;

import actions.ConcreteAction;
import structure.iod.OnDrawStructure;
import utils.CodeLocation;

public class IODEnter extends ConcreteAction {
    public IODEnter(CodeLocation location) {
        super(location);
    }

    public void execute(OnDrawStructure structure, long date) {
        //OnDrawStructure toReturn = new OnDrawStructure(this.location, id);
        structure.begin(date);
        //return toReturn;
    }

    @Override
    public String generateBreakPoint() {
        return null;
    }
}
