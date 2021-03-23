package actions.hp;

import actions.ConcreteAction;
import structure.iod.OnDrawStructure;
import utils.CodeLocation;

public class HPEnter extends ConcreteAction {

    public HPEnter(CodeLocation location) {
        super(location);
    }

    public OnDrawStructure execute(String id, long date) {
        OnDrawStructure toReturn = new OnDrawStructure(this.location, id);
        toReturn.begin(date);
        return toReturn;
    }

    @Override
    public String generateBreakPoint() {
        return null;
    }
}
