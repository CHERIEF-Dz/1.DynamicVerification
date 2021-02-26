package structure.hmu;

import utils.CodeLocation;

public class HashMapStructure extends MapStructure {

    public int LittleHashMapThreshold = 500;

    public HashMapStructure(CodeLocation implementation, String id) {
        super(implementation, id);
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
        if (this.actualSize < LittleHashMapThreshold) {
            System.out.println("HashMap defined " + this.structureImplementation.toString() + " has HMU code smell (Too few elements).");
            this.foundCodeSmell();
        }
    }
}
