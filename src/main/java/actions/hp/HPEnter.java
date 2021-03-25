package actions.hp;

import actions.ConcreteAction;
import structure.hp.HeavyProcessStructure;
import utils.CodeLocation;

public class HPEnter extends ConcreteAction {

    public HPEnter(CodeLocation location) {
        super(location);
    }

    public HeavyProcessStructure execute(String id, long date) {
        HeavyProcessStructure toReturn = new HeavyProcessStructure(this.location, id);
        toReturn.begin(date);
        return toReturn;
    }

    @Override
    public String generateBreakPoint() {
        return null;
    }
}
