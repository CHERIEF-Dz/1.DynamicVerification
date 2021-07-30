package structure;

import utils.CodeLocation;

public class HashMapStructure extends MapStructure {

    public int LittleHashMapThreshold = 500;

    public HashMapStructure(CodeLocation implementation, String id, String name) {
        super(implementation, id, name);
        this.actualSize = 0;
        this.structureImplementation = implementation;
    }

    //@Override
    //public void addElement() {
    //    super.addElement();
    //}

    @Override
    public void checkStructure() {
        super.checkStructure();
        if (this.maximumSize < LittleHashMapThreshold) {
            //System.out.println("HashMap defined " + this.structureImplementation.toString() + " has HMU code smell (Too few elements).");
            this.foundCodeSmell();
        }
    }
}
