package events.hp;

import events.ConcreteEvent;
import structure.HeavyProcessStructure;
import utils.CodeLocation;

public class HPEnter extends ConcreteEvent {

    public HPEnter(CodeLocation location) {
        super(location);
    }

    public void execute(HeavyProcessStructure structure, long date) {
        this.isExecuted=true;
        //HeavyProcessStructure toReturn = new HeavyProcessStructure(this.location, id);
        structure.begin(date);
        //return toReturn;
    }

    @Override
    public String generateBreakPoint() {
        return null;
    }
}
